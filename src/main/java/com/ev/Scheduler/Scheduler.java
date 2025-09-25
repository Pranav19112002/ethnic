package com.ev.Scheduler;


import com.ev.Model.Activity;
import com.ev.Model.ActivityBooking;
import com.ev.Repository.ActivityBookingRepository;
import com.ev.Repository.ActivityRepository;
import com.ev.Services.MailService;
import com.ev.Services.NotificationService;
import com.ev.Services.WeatherService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class Scheduler {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private ActivityBookingRepository activityBookingRepository;

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private NotificationService notificationService;

    @Scheduled(fixedRate = 300000) // every 5 minutes
    @Transactional
    public void updateAllActivityStatuses() {
        List<Activity> activities = activityRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Activity activity : activities) {
            String type = activity.getActivityType();
            boolean saveNeeded = false;
            String newStatus = activity.getActivityStatus();

            // ✅ Seasonal
            if ("seasonal".equalsIgnoreCase(type)) {
                if (activity.getEndDate() != null && activity.getEndDate().isBefore(now.toLocalDate())) {
                    if (!"Ended".equalsIgnoreCase(activity.getActivityStatus())) {
                        newStatus = "Ended";
                        saveNeeded = true;
                    }
                }
            }

            // ✅ Real-Time Event
            else if ("realTimeEvent".equalsIgnoreCase(type)) {
                if (activity.getEventDateTime() != null && activity.getEventDateTime().isBefore(now)) {
                    if (!"Ended".equalsIgnoreCase(activity.getActivityStatus())) {
                        newStatus = "Ended";
                        saveNeeded = true;
                    }
                }
            }

            // ✅ Weather-Dependent
            else if ("weatherDependent".equalsIgnoreCase(type)) {
                double lat = activity.getVillage().getLatitude();
                double lon = activity.getVillage().getLongitude();
                boolean badWeather = weatherService.isBadWeather(lat, lon);
                newStatus = badWeather ? "Unavailable" : "Available";

                if (!newStatus.equalsIgnoreCase(activity.getActivityStatus())) {
                    saveNeeded = true;
                }
            }

            // ✅ Save status if changed
            if (saveNeeded) {
                activity.setActivityStatus(newStatus);
                activityRepository.save(activity);
                System.out.println("🔄 [STATUS] Updated activity '" + activity.getActivityName() + "' to " + newStatus);

                List<ActivityBooking> bookings = activityBookingRepository
                        .findByActivityAndStatusInIgnoreCase(activity, List.of("confirmed", "pending"));

                for (ActivityBooking booking : bookings) {
                    Activity altActivity = activity.getAlternativeActivity();
                    boolean hasValidAlternative = altActivity != null &&
                            "Available".equalsIgnoreCase(altActivity.getActivityStatus());

                    StringBuilder msg = new StringBuilder("❌ Your booking for activity '")
                            .append(activity.getActivityName())
                            .append("' on ").append(booking.getBookingDate());

                    if (booking.getTimeSlot() != null) {
                        msg.append(" at ").append(booking.getTimeSlot());
                    }

                    msg.append(" in village '").append(activity.getVillage().getVillageName()).append("' ");

                    if ("confirmed".equalsIgnoreCase(booking.getStatus())) {
                        if (hasValidAlternative) {
                            msg.append("has been redirected to '")
                                    .append(altActivity.getActivityName())
                                    .append("'. Your ticket remains valid.");
                        } else {
                            msg.append("has been cancelled. A refund will be processed shortly. For help, contact ")
                                    .append(activity.getVillage().getVillageEmail())
                                    .append(" with your booking ID: ").append(booking.getBookingId()).append(".");
                            booking.setStatus("cancelled");
                            booking.setExpiredOrCancelledAt(LocalDateTime.now());
                            booking.setExpiredOrCancelReason("No alternative activity available");
                            activityBookingRepository.save(booking);
                        }
                    } else if ("pending".equalsIgnoreCase(booking.getStatus())) {
                        msg.append("has been cancelled as the activity is no longer available. Please book another activity.");
                        booking.setStatus("cancelled");
                        booking.setExpiredOrCancelledAt(LocalDateTime.now());
                        booking.setExpiredOrCancelReason("Activity unavailable before payment");
                        activityBookingRepository.save(booking);
                    }

                    notificationService.sendInboxMessageToUserActivityStatusChange(
                            booking.getUser().getUserId(),
                            msg.toString(),
                            activity,
                            booking,
                            activity.getVillage()
                    );

                    mailService.sendActivityCancellationEmailDueToWeather(
                            booking.getUser().getUserEmail(),
                            activity.getActivityName(),
                            booking.getBookingDate(),
                            booking.getExpiredOrCancelReason(),
                            booking.getStatus(),
                            activity.getAlternativeActivity(),
                            activity.getVillage().getVillageEmail(),
                            booking.getBookingId()
                    );


                    System.out.println("📨 [NOTIFY] Message sent to userId: " + booking.getUser().getUserId());
                    System.out.println("📧 [EMAIL] Sent to: " + booking.getUser().getUserEmail());
                }
            }
        }

        System.out.println("✔ Activity status updated by scheduler at " + now);
    }



    @Scheduled(fixedRate = 300000) // every 5 minutes
    @Transactional
    public void expireUnpaidBookings() {
        LocalDateTime now = LocalDateTime.now();

        List<ActivityBooking> overdue = activityBookingRepository
                .findByPaymentStatusIgnoreCaseAndStatusIgnoreCaseAndExpiresAtBefore("PENDING", "pending", now);

        overdue.forEach(booking -> {
            if ((booking.getExpiredOrCancelReason() == null || booking.getExpiredOrCancelReason().isBlank()) &&
                    booking.getExpiredOrCancelledAt() == null) {

                booking.setStatus("expired");
                booking.setExpiredOrCancelReason("Booking expired due to late payment.");
                booking.setExpiredOrCancelledAt(now);

                // Optional: notify user
                notificationService.sendInboxMessageToUserBookingCancelled(
                        booking.getUser().getUserId(),
                        "Your booking for activity '" + booking.getActivity().getActivityName() +
                                "' on " + booking.getBookingDate() + " has expired due to late payment.",
                        booking.getActivity(),
                        booking,
                        booking.getVillage()
                );
            }
        });

        activityBookingRepository.saveAll(overdue);
        System.out.println("✔ Scheduler ran at " + now + " — expired " + overdue.size() + " unpaid bookings.");
    }


    @Scheduled(cron = "0 0 0 * * *") // runs daily at midnight
    @Transactional
    public void markPastBookingsAsEnded() {
        LocalDate today = LocalDate.now();

        List<ActivityBooking> pastBookings = activityBookingRepository
                .findByBookingDateBeforeAndStatusIgnoreCase(today, "confirmed");

        pastBookings.forEach(booking -> {
            booking.setStatus("ended");
            booking.setExpiredOrCancelledAt(LocalDateTime.now());
            booking.setExpiredOrCancelReason("Booking marked as ended after activity date.");
        });

        activityBookingRepository.saveAll(pastBookings);
        System.out.println("✔ Ended " + pastBookings.size() + " bookings on " + today);
    }

}
