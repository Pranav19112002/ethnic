package com.ev.Services;

import com.ev.Exception.ActivityBookingException;
import com.ev.Model.*;
import com.ev.Repository.ActivityBookingRepository;
import com.ev.Repository.ActivityRepository;
import com.ev.Repository.UserRepository;
import com.ev.Repository.VillageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActivityBookingServices implements ActivityBookingService{

    @Autowired
    private ActivityBookingRepository activityBookingRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VillageRepository villageRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public ActivityBooking createBooking(ActivityBookingDto activityBookingDto) throws ActivityBookingException {

        Activity activity = activityRepository.findById(activityBookingDto.getActivityId())
                .orElseThrow(() -> new ActivityBookingException("Activity not found"));

        int alreadyBooked = 0;

        if (activity.getActivityType().equalsIgnoreCase("realTimeEvent")) {
            alreadyBooked = activityBookingRepository.countConfirmedPeopleByActivityIdAndDate(
                    activity.getActivityId(), activityBookingDto.getBookingDate());

            if (alreadyBooked + activityBookingDto.getNumberOfPeople() > activity.getNoOfPeopleAllowedForDateOnly()) {
                throw new ActivityBookingException("Date is full. Only " +
                        (activity.getNoOfPeopleAllowedForDateOnly() - alreadyBooked) + " spots left.");
            }

        } else {
            // Limit based on time slot
            alreadyBooked = activityBookingRepository.countConfirmedPeopleByActivityIdDateAndSlot(
                    activity.getActivityId(), activityBookingDto.getBookingDate(), activityBookingDto.getTimeSlot());

            if (alreadyBooked + activityBookingDto.getNumberOfPeople() > activity.getNoOfPeopleAllowedForSloted()) {
                throw new ActivityBookingException("Slot is full. Only " +
                        (activity.getNoOfPeopleAllowedForSloted() - alreadyBooked) + " spots left.");
            }
        }

        ActivityBooking booking = new ActivityBooking();
        booking.setActivity(activity);
        booking.setUser(userRepository.findById(activityBookingDto.getUserId()).orElseThrow());
        booking.setVillage(villageRepository.findById(activityBookingDto.getVillageId()).orElseThrow());
        booking.setBookingDate(activityBookingDto.getBookingDate());
        if(!"realTimeEvent".equalsIgnoreCase(activity.getActivityType())) {
            booking.setTimeSlot(activityBookingDto.getTimeSlot());
        }
        else {
            booking.setTimeSlot(activity.getEventDateTime().toLocalTime());
        }
        booking.setContactNo(activityBookingDto.getContactNo());
        booking.setAltContactNo(activityBookingDto.getAltContactNo());
        booking.setNumberOfPeople(activityBookingDto.getNumberOfPeople());
        booking.setTotalAmount(activityBookingDto.getTotalAmount());
        booking.setStatus("pending");
        booking.setPaymentStatus("PENDING");// will mark as 'Approved' after payment
        booking.setCreatedAt(LocalDateTime.now());
        booking.setExpiresAt(LocalDateTime.now().plusHours(5));
        return activityBookingRepository.save(booking);
    }

    @Override
    public Map<String, Object> validateAndCancelIfOverbooked(Long bookingId) {
        ActivityBooking booking = activityBookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        Activity activity = booking.getActivity();
        String type = activity.getActivityType(); // "realTimeEvent", "seasonal", etc.

        int alreadyBooked;
        int capacity;

        if (type.equalsIgnoreCase("realTimeEvent")) {
            alreadyBooked = activityBookingRepository.countConfirmedPeopleByActivityIdAndDate(
                    activity.getActivityId(), booking.getBookingDate());

            Integer cap = activity.getNoOfPeopleAllowedForDateOnly();
            if (cap == null) {
                throw new IllegalStateException("Missing capacity for realTimeEvent activityId: " + activity.getActivityId());
            }
            capacity = cap;

        } else {
            alreadyBooked = activityBookingRepository.countConfirmedPeopleByActivityIdDateAndSlot(
                    activity.getActivityId(), booking.getBookingDate(), booking.getTimeSlot());

            Integer cap = activity.getNoOfPeopleAllowedForSloted();
            if (cap == null) {
                throw new IllegalStateException("Missing slot-based capacity for activityId: " + activity.getActivityId());
            }
            capacity = cap;
        }

        int remaining = capacity - alreadyBooked;

        if (remaining <= 0) {
            notificationService.notifyVillageBookingClosed(activity, booking.getBookingDate(), booking.getTimeSlot());
        }

        if (booking.getNumberOfPeople() > remaining) {
            booking.setStatus("cancelled");
            booking.setExpiredOrCancelReason("Booking full. Try another date or slot.");
            booking.setExpiredOrCancelledAt(LocalDateTime.now());
            activityBookingRepository.save(booking);

            return Map.of(
                    "cancelled", true,
                    "message", "Booking full. Your booking has been cancelled. Please try another date or slot."
            );
        }

        return Map.of("cancelled", false);
    }


    @Override
    public ActivityBooking getActivityBookingsById(Long bookingId) {
        Optional<ActivityBooking> optionalActivityBooking = activityBookingRepository.findById(bookingId);
        return optionalActivityBooking.orElse(null);
    }

    @Override
    public ActivityBooking getBookingsById(Long bookingId) {
        Optional<ActivityBooking> optionalActivityBooking = activityBookingRepository.findById(bookingId);
        return optionalActivityBooking.orElse(null);
    }

    @Override
    public void markAsPaid(Long bookingId, String paymentIntentId, String status, String method, String receiptUrl, Long amountInCents,
                           String currency) {

        System.out.println("💾 [DB] markAsPaid called with:");
        System.out.println("📌 Booking ID: " + bookingId);
        System.out.println("💳 PaymentIntent ID: " + paymentIntentId);
        System.out.println("💰 Payment Status: " + status);
        System.out.println("🧾 Payment Method: " + method);
        System.out.println("🔗 Receipt URL: " + receiptUrl);
        System.out.println("🧾 Currency: " + currency);
        System.out.println("🔗 Amount: " + amountInCents);

        double amount = amountInCents / 100.0;

        ActivityBooking booking = activityBookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    System.out.println("❌ Booking not found for ID: " + bookingId);
                    return new RuntimeException("Booking not found");
                });

        System.out.println("✅ Booking found: " + booking.getActivity().getActivityName());

        booking.setPaymentId(paymentIntentId);
        booking.setPaymentStatus(status.toLowerCase());
        booking.setPaymentMethod(method);
        booking.setReceiptUrl(receiptUrl);
        booking.setAmountPaid(amount);
        booking.setCurrency(currency);
        booking.setPaymentAt(LocalDateTime.now());
        booking.setPaid(true);

        if ("succeeded".equalsIgnoreCase(status)) {
            booking.setStatus("confirmed");
            notifyAllPendingUsersWithSlotStatus(booking);
            notificationService.notifyVillageBookingConfirmed(booking);
            System.out.println("✅ Payment succeeded. Booking status set to 'Confirmed'");
        } else {
            booking.setStatus("pending");
            System.out.println("⚠️ Payment not succeeded. Booking status set to 'Pending'");
        }

        activityBookingRepository.saveAndFlush(booking);
        System.out.println("💾 Booking saved to DB successfully.");
    }

    private void notifyAllPendingUsersWithSlotStatus(ActivityBooking booking) {
        Activity activity = booking.getActivity();
        LocalDate date = booking.getBookingDate();
        LocalTime slot = booking.getTimeSlot();

        int confirmed = activityBookingRepository.countConfirmedPeopleByActivityIdDateAndSlot(
                activity.getActivityId(), date, slot);

        int capacity = Optional.ofNullable(activity.getNoOfPeopleAllowedForSloted()).orElse(0);
        int remaining = capacity - confirmed;

        List<ActivityBooking> pendingBookings = activityBookingRepository
                .findPendingBookingsByActivityIdDateAndSlot(activity.getActivityId(), date, slot);

        for (ActivityBooking pending : pendingBookings) {
            String message;

            if (remaining <= 5 && remaining > 0) {
                message = "🚨 URGENT: Only " + remaining + " spots left for '" + activity.getActivityName() +
                        "' on " + date + " at " + slot + ". Complete your payment now to avoid cancellation!";
            } else {
                message = "📢 Reminder: " + remaining + " spots remain for '" + activity.getActivityName() +
                        "' on " + date + " at " + slot + ". Please complete your payment soon.";
            }

            notificationService.sendInboxMessageToAllUsers(pending.getUser(), message ,activity,pending);
        }

        System.out.println("📣 Notified " + pendingBookings.size() + " pending users with slot status.");
    }

    @Override
    public List<ActivityBooking> getBookingsByUserId(Long userId) {
        return activityBookingRepository.findByUser_UserIdOrderByBookingIdDesc(userId);
    }

    @Override
    public List<ActivityBooking> getUpcomingBookingsByUserId(String filterDate, String filterActivity, String filterVillage, Long userId) {
        List<ActivityBooking> activityBookings = activityBookingRepository.findByUser_UserIdAndStatusIgnoreCaseAndPaymentStatusIgnoreCaseOrderByBookingIdDesc(userId, "confirmed" ,"succeeded");

        return activityBookings.stream()
                .filter(ab -> {
                    if (filterDate != null && !filterDate.isEmpty()) {
                        try {
                            LocalDate selectedDate = LocalDate.parse(filterDate);
                            return ab.getBookingDate().equals(selectedDate);
                        } catch (DateTimeParseException ex) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(ab -> {
                    if (filterActivity != null && !filterActivity.isEmpty()){
                        return ab.getActivity().getActivityName().toLowerCase().startsWith(filterActivity.toLowerCase());
                    }
                    return true;
                })
                .filter(ab -> {
                    if (filterVillage != null && !filterVillage.isEmpty()){
                        return ab.getVillage().getVillageName().toLowerCase().startsWith(filterVillage.toLowerCase());
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivityBooking> getPendingBookingsByUserId(String filterDate, String filterActivity, String filterVillage, Long userId) {
        List<ActivityBooking> activityBookings = activityBookingRepository.findByUser_UserIdOrderByBookingIdDesc(userId);

        activityBookings.forEach(ab -> {
            System.out.println("Status: " + ab.getStatus());
            System.out.println("Payment Status: " + ab.getPaymentStatus());
        });

        return activityBookings.stream()
                .filter(ab -> ab.getStatus() != null && ab.getStatus().equalsIgnoreCase("pending"))
                .filter(ab -> {
                    String paymentStatus = ab.getPaymentStatus();
                    return paymentStatus == null || paymentStatus.equalsIgnoreCase("PENDING");
                })
                .filter(ab -> {
                    if (filterDate != null && !filterDate.isEmpty()) {
                        try {
                            LocalDate selectedDate = LocalDate.parse(filterDate);
                            return ab.getBookingDate().equals(selectedDate);
                        } catch (DateTimeParseException ex) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(ab -> {
                    if (filterActivity != null && !filterActivity.isEmpty()) {
                        return ab.getActivity().getActivityName().toLowerCase().startsWith(filterActivity.toLowerCase());
                    }
                    return true;
                })
                .filter(ab -> {
                    if (filterVillage != null && !filterVillage.isEmpty()) {
                        return ab.getVillage().getVillageName().toLowerCase().startsWith(filterVillage.toLowerCase());
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }


    @Override
    public List<ActivityBooking> getCancelledAndExpiredBookingsByUserId(String filterDate, String filterActivity, String filterVillage, Long userId) {
        List<String> statuses = Arrays.asList("cancelled", "expired");
        List<ActivityBooking> activityBookings = activityBookingRepository.findByUser_UserIdAndStatusInOrderByExpiredOrCancelledAtDesc(userId, statuses);

        return activityBookings.stream()
                .filter(ab -> {
                    if (filterDate != null && !filterDate.isEmpty()) {
                        try {
                            LocalDate selectedDate = LocalDate.parse(filterDate);
                            return ab.getBookingDate().equals(selectedDate);
                        } catch (DateTimeParseException ex) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(ab -> {
                    if (filterActivity != null && !filterActivity.isEmpty()){
                        return ab.getActivity().getActivityName().toLowerCase().startsWith(filterActivity.toLowerCase());
                    }
                    return true;
                })
                .filter(ab -> {
                    if (filterVillage != null && !filterVillage.isEmpty()){
                        return ab.getVillage().getVillageName().toLowerCase().startsWith(filterVillage.toLowerCase());
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivityBooking> getEndedBookingsByUserId(String filterDate, String filterActivity, String filterVillage, Long userId) {
        List<ActivityBooking> activityBookings = activityBookingRepository.findByUser_UserIdAndStatusIgnoreCaseOrderByBookingIdDesc(userId, "ended");

        return activityBookings.stream()
                .filter(ab -> {
                    if (filterDate != null && !filterDate.isEmpty()) {
                        try {
                            LocalDate selectedDate = LocalDate.parse(filterDate);
                            return ab.getBookingDate().equals(selectedDate);
                        } catch (DateTimeParseException ex) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(ab -> {
                    if (filterActivity != null && !filterActivity.isEmpty()) {
                        return ab.getActivity().getActivityName().toLowerCase().startsWith(filterActivity.toLowerCase());
                    }
                    return true;
                })
                .filter(ab -> {
                    if (filterVillage != null && !filterVillage.isEmpty()) {
                        return ab.getVillage().getVillageName().toLowerCase().startsWith(filterVillage.toLowerCase());
                    }
                    return true;
                })
                .filter(ab -> {
                    // Apply feedback check defensively
                    boolean feedbackExists = ab.getBookingFeedback() != null;
                    ab.setHasFeedback(feedbackExists);
                    return true;
                })
                .collect(Collectors.toList());

    }

    @Override
    public List<ActivityBooking> getPaymentsByUserId(String filterDate, String filterMethod, String filterStatus, Long userId) {
        List<ActivityBooking> activityBookings = activityBookingRepository.findByUser_UserIdAndStatusIgnoreCaseInOrderByBookingIdDesc(userId, List.of("ended", "pending" , "confirmed"));


        return activityBookings.stream()
                .filter(ab -> {
                    if (filterDate != null && !filterDate.isEmpty()) {
                        try {
                            LocalDate selectedDate = LocalDate.parse(filterDate);
                            LocalDateTime paymentAt = ab.getPaymentAt();
                            return paymentAt != null && paymentAt.toLocalDate().equals(selectedDate);
                        } catch (DateTimeParseException ex) {
                            return false;
                        }
                    }
                    return true;
                })

                .filter(ab -> {
                    if (filterMethod != null && !filterMethod.isEmpty()) {
                        String method = ab.getPaymentMethod();
                        return method != null && method.equalsIgnoreCase(filterMethod.toLowerCase());
                    }
                    return true;
                })
                .filter(ab -> {
                    if (filterStatus != null && !filterStatus.isEmpty()) {
                        String status = ab.getPaymentStatus();
                        return status != null && status.equalsIgnoreCase(filterStatus.toLowerCase());
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivityBooking> getFilteredConfirmedBookings(Long filterBookingId ,String filterDate, String filterActivity, Long filterVillage) {
            List<ActivityBooking> activityBookings = activityBookingRepository.findByStatusIgnoreCaseOrderByBookingIdDesc("confirmed");

            return activityBookings.stream()
                    .filter(ab -> {
                        if (filterBookingId != null) {
                            return ab.getBookingId().equals(filterBookingId);
                        }
                        return true;
                    })
                    .filter(ab -> {
                        if (filterDate != null && !filterDate.isEmpty()) {
                            try {
                                LocalDate selectedDate = LocalDate.parse(filterDate);
                                return ab.getBookingDate().equals(selectedDate);
                            } catch (DateTimeParseException ex) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .filter(ab -> {
                        if (filterActivity != null && !filterActivity.isEmpty()) {
                            return ab.getActivity().getActivityName().toLowerCase().startsWith(filterActivity.toLowerCase());
                        }
                        return true;
                    })
                    .filter(ab -> {
                        if (filterVillage != null) {
                            return ab.getVillage().getVillageId().equals(filterVillage);
                        }
                        return true;
                    })
                    .collect(Collectors.toList());

    }

    @Override
    public ActivityBookingDto getBookingDetails(Long bookingId) {
        ActivityBooking booking = activityBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        ActivityBookingDto dto = new ActivityBookingDto();

        // Core booking info
        dto.setBookingId(booking.getBookingId());
        dto.setBookingDate(booking.getBookingDate());
        dto.setTimeSlot(booking.getTimeSlot());
        dto.setNumberOfPeople(booking.getNumberOfPeople());
        dto.setContactNo(booking.getContactNo());
        dto.setAltContactNo(booking.getAltContactNo());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setPaymentId(booking.getPaymentId());
        dto.setStatus(booking.getStatus());
        dto.setPaymentReceipt(booking.getReceiptUrl());
        dto.setCancelledOrExpiredAt(booking.getExpiredOrCancelledAt());
        dto.setReason(booking.getExpiredOrCancelReason());

        // User info
        User user = booking.getUser();
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setUserProfileImage(user.getUserProfilePicName());

        // Activity info
        Activity activity = booking.getActivity();
        dto.setActivityId(activity.getActivityId());
        dto.setActivityName(activity.getActivityName());

        // Village info
        Village village = booking.getVillage();
        dto.setVillageId(village.getVillageId());
        dto.setVillageName(village.getVillageName());
        dto.setVillageProfileImage(village.getVillageProfileImage());

        return dto;
    }


    @Override
    public List<ActivityBooking> getFilteredPendingBookings(Long filterBookingId, String filterDate, String filterActivity, Long filterVillage) {
        List<ActivityBooking> activityBookings = activityBookingRepository.findAll(Sort.by(Sort.Direction.DESC, "bookingId"));

        activityBookings.forEach(ab -> {
            System.out.println("Status: " + ab.getStatus());
            System.out.println("Payment Status: " + ab.getPaymentStatus());
        });

        return activityBookings.stream()
                .filter(ab -> ab.getStatus() != null && ab.getStatus().equalsIgnoreCase("pending"))
                .filter(ab -> {
                    String paymentStatus = ab.getPaymentStatus();
                    return paymentStatus == null || paymentStatus.equalsIgnoreCase("PENDING");
                })
                .filter(ab -> {
                    if (filterBookingId != null) {
                        return ab.getBookingId().equals(filterBookingId);
                    }
                    return true;
                })
                .filter(ab -> {
                    if (filterDate != null && !filterDate.isEmpty()) {
                        try {
                            LocalDate selectedDate = LocalDate.parse(filterDate);
                            return ab.getBookingDate().equals(selectedDate);
                        } catch (DateTimeParseException ex) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(ab -> {
                    if (filterActivity != null && !filterActivity.isEmpty()) {
                        return ab.getActivity().getActivityName().toLowerCase().startsWith(filterActivity.toLowerCase());
                    }
                    return true;
                })
                .filter(ab -> {
                    if (filterVillage != null) {
                        return ab.getVillage().getVillageId().equals(filterVillage);
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivityBooking> getFilteredEndedBookings(Long filterBookingId ,String filterDate, String filterActivity, Long filterVillage) {
        List<ActivityBooking> activityBookings = activityBookingRepository.findByStatusIgnoreCaseOrderByBookingIdDesc("ended");

        return activityBookings.stream()
                .filter(ab -> {
                    if (filterBookingId != null) {
                        return ab.getBookingId().equals(filterBookingId);
                    }
                    return true;
                })
                .filter(ab -> {
                    if (filterDate != null && !filterDate.isEmpty()) {
                        try {
                            LocalDate selectedDate = LocalDate.parse(filterDate);
                            return ab.getBookingDate().equals(selectedDate);
                        } catch (DateTimeParseException ex) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(ab -> {
                    if (filterActivity != null && !filterActivity.isEmpty()) {
                        return ab.getActivity().getActivityName().toLowerCase().startsWith(filterActivity.toLowerCase());
                    }
                    return true;
                })
                .filter(ab -> {
                    if (filterVillage != null) {
                        return ab.getVillage().getVillageId().equals(filterVillage);
                    }
                    return true;
                })
                .filter(ab -> {
                    // Apply feedback check defensively
                    boolean feedbackExists = ab.getBookingFeedback() != null;
                    ab.setHasFeedback(feedbackExists);
                    return true;
                })
                .collect(Collectors.toList());

    }

    @Override
    public List<ActivityBooking> getFilteredCancelledOrExpiredBookings(Long filterBookingId, String filterDate, String filterActivity, Long filterVillage, String filterStatus) {
        List<String> statuses;

        if (filterStatus != null && !filterStatus.isEmpty()) {
            statuses = Collections.singletonList(filterStatus.toLowerCase());
        } else {
            statuses = Arrays.asList("cancelled", "expired");
        }

        List<ActivityBooking> activityBookings = activityBookingRepository
                .findByStatusIgnoreCaseInOrderByExpiredOrCancelledAtDesc(statuses);

        return activityBookings.stream()
                .filter(ab -> {
                    if (filterBookingId != null) {
                        return ab.getBookingId().equals(filterBookingId);
                    }
                    return true;
                })
                .filter(ab -> {
                    if (filterDate != null && !filterDate.isEmpty()) {
                        try {
                            LocalDate selectedDate = LocalDate.parse(filterDate);
                            return ab.getBookingDate().equals(selectedDate);
                        } catch (DateTimeParseException ex) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(ab -> {
                    if (filterActivity != null && !filterActivity.isEmpty()) {
                        return ab.getActivity().getActivityName().toLowerCase().startsWith(filterActivity.toLowerCase());
                    }
                    return true;
                })
                .filter(ab -> {
                    if (filterVillage != null) {
                        return ab.getVillage().getVillageId().equals(filterVillage);
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }


    @Override
    public boolean cancelBooking(Long bookingId, String cancelReason) {
        Optional<ActivityBooking> optionalActivityBooking = activityBookingRepository.findById(bookingId);

        if (optionalActivityBooking.isEmpty()) {
            return false;
        }

        ActivityBooking activityBooking = optionalActivityBooking.get();

        // Only allow cancel if booking is confirmed and unpaid
        if (!"pending".equalsIgnoreCase(activityBooking.getStatus())) {
            return false;
        }

        if (!"PENDING".equalsIgnoreCase(activityBooking.getPaymentStatus())) {
            return false;
        }

        if (activityBooking.getExpiredOrCancelReason() != null &&
                !activityBooking.getExpiredOrCancelReason().isEmpty()) {
            return false;
        }

        activityBooking.setStatus("cancelled");
        activityBooking.setExpiredOrCancelReason(cancelReason);
        activityBooking.setExpiredOrCancelledAt(LocalDateTime.now());
        activityBookingRepository.save(activityBooking);
        return true;
    }


}
