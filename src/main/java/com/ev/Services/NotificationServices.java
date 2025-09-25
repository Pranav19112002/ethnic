package com.ev.Services;

import com.ev.Model.*;
import com.ev.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class NotificationServices implements NotificationService {


    @Autowired
    private UserInboxRepository inboxRepository;

    @Autowired
    private AdminInboxRepository adminInboxRepository;

    @Autowired
    private UserInboxRepository userInboxRepository;

    @Autowired
    private VillageInboxRepository villageInboxRepository;

    @Autowired
    private ActivityBookingRepository activityBookingRepository;

    @Autowired
    private VillageRepository villageRepository;


    @Override
    public void sendInboxMessageToUserVillageStatusChange(Long userId,
                                                          String message,
                                                          Activity activity,
                                                          ActivityBooking booking,
                                                          Village village) {
        UserInboxMessage inboxMessage = new UserInboxMessage();
        inboxMessage.setUserId(userId);
        inboxMessage.setMessage(message);
        inboxMessage.setActivity(activity);
        inboxMessage.setBooking(booking);
        inboxMessage.setVillage(village);
        inboxMessage.setTimestamp(LocalDateTime.now());
        inboxMessage.setRead(false);

        userInboxRepository.save(inboxMessage);
        System.out.println("📨 [NOTIFY] Village status change message saved for userId: " + userId);
    }

    @Override
    public void sendInboxMessageToUserActivityStatusChange(Long userId,
                                                           String message,
                                                           Activity activity,
                                                           ActivityBooking booking,
                                                           Village village) {
        UserInboxMessage inboxMessage = new UserInboxMessage();
        inboxMessage.setUserId(userId);
        inboxMessage.setMessage(message);
        inboxMessage.setActivity(activity);
        inboxMessage.setBooking(booking);
        inboxMessage.setVillage(village);
        inboxMessage.setTimestamp(LocalDateTime.now());
        inboxMessage.setRead(false);

        userInboxRepository.save(inboxMessage);
        System.out.println("📨 [NOTIFY] Activity status change message saved for userId: " + userId);
    }


    @Override
    public void sendInboxMessageToUserBookingCancelled(Long userId,
                                                       String message,
                                                       Activity activity,
                                                       ActivityBooking booking,
                                                       Village village) {
        UserInboxMessage inboxMessage = new UserInboxMessage();
        inboxMessage.setUserId(userId);
        inboxMessage.setMessage(message);
        inboxMessage.setActivity(activity);
        inboxMessage.setBooking(booking);
        inboxMessage.setVillage(village);
        inboxMessage.setTimestamp(LocalDateTime.now());
        inboxMessage.setRead(false);

        userInboxRepository.save(inboxMessage);
        System.out.println("📨 [NOTIFY] Booking cancellation message sent to userId: " + userId);
    }

    @Override
    public void sendInboxMessageToAllUsers(User user, String message, Activity activity, ActivityBooking booking) {
        UserInboxMessage inboxMessage = new UserInboxMessage();
        inboxMessage.setUserId(user.getUserId());
        inboxMessage.setActivity(activity);
        inboxMessage.setBooking(booking);
        inboxMessage.setMessage(message);
        inboxMessage.setTimestamp(LocalDateTime.now());
        inboxMessage.setRead(false);

        userInboxRepository.save(inboxMessage);

        System.out.println("📨 Inbox message sent to userId: " + user.getUserId());
    }

    @Override
    public void sendVillageNotificationToAdmin(Long villageId, String message) {
        Village village = villageRepository.findById(villageId)
                .orElseThrow(() -> new RuntimeException("Village not found"));

        AdminInboxMessage inboxMessage = new AdminInboxMessage();
        inboxMessage.setVillage(village);
        inboxMessage.setMessage(message);
        inboxMessage.setTimestamp(LocalDateTime.now());
        adminInboxRepository.save(inboxMessage);
    }

    @Override
    public void sendActivityNotificationToAdmin(Activity activity, String message) {
        AdminInboxMessage inboxMessage = new AdminInboxMessage();
        inboxMessage.setVillage(activity.getVillage());
        inboxMessage.setMessage(message);
        inboxMessage.setTimestamp(LocalDateTime.now());
        adminInboxRepository.save(inboxMessage);
    }

    @Override
    public List<AdminInboxMessage> getAllNotificationsByTime() {
        return adminInboxRepository.findAllByOrderByTimestampDesc();
    }

    @Override
    public List<AdminInboxMessage> getAllMessages() {
        return adminInboxRepository.findAll();
    }

    @Transactional
    @Override
    public void markMessageAsRead(Long id) {
        adminInboxRepository.markAsRead(id);
    }

    @Transactional
    @Override
    public void markAllAdminMessagesAsRead() {
        adminInboxRepository.markAllRead();
    }

    @Transactional
    @Override
    public void deleteAllAdminMessages() {
        adminInboxRepository.deleteAllMessages();
    }

    @Override
    public void sendVillageNotification(Village village, VillageNotificationType type, String message) {
        VillageInboxMessage inboxMessage = new VillageInboxMessage();
        inboxMessage.setVillage(village);
        inboxMessage.setType(type);
        inboxMessage.setMessage(message);
        inboxMessage.setTimestamp(LocalDateTime.now());
        inboxMessage.setRead(false);

        villageInboxRepository.save(inboxMessage);
    }

    @Override
    public void sendVillageNotification(Village village, VillageNotificationType type, String message, Activity activity) {
        VillageInboxMessage inboxMessage = new VillageInboxMessage();
        inboxMessage.setVillage(village);
        inboxMessage.setType(type);
        inboxMessage.setMessage(message);
        inboxMessage.setTimestamp(LocalDateTime.now());
        inboxMessage.setRead(false);
        inboxMessage.setActivity(activity);

        villageInboxRepository.save(inboxMessage);
    }

    @Override
    public void notifyVillageOfNewBooking(ActivityBooking booking) {
        VillageInboxMessage villageMessage = new VillageInboxMessage();
        villageMessage.setVillage(booking.getVillage());
        villageMessage.setType(VillageNotificationType.USER_BOOKING);
        villageMessage.setActivity(booking.getActivity());
        villageMessage.setActivityBooking(booking);
        villageMessage.setTimestamp(LocalDateTime.now());
        villageMessage.setRead(false);

        StringBuilder msg = new StringBuilder("📥 New booking received: ");
        msg.append(booking.getActivity().getActivityName())
                .append(" on ").append(booking.getBookingDate());

        if (booking.getTimeSlot() != null) {
            msg.append(" at ").append(booking.getTimeSlot());
        }

        msg.append(". People: ").append(booking.getNumberOfPeople())
                .append(". Status: ").append(booking.getStatus());

        villageMessage.setMessage(msg.toString());

        villageInboxRepository.save(villageMessage);
    }

    @Override
    public void notifyVillageBookingConfirmed(ActivityBooking booking) {
        VillageInboxMessage message = new VillageInboxMessage();
        message.setVillage(booking.getVillage());
        message.setType(VillageNotificationType.USER_BOOKING);
        message.setActivity(booking.getActivity());
        message.setActivityBooking(booking);
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);

        StringBuilder msg = new StringBuilder("✅ Booking confirmed: ");
        msg.append("Activity '").append(booking.getActivity().getActivityName()).append("'");

        msg.append(" for ").append(booking.getNumberOfPeople()).append(" people");

        msg.append(" on ").append(booking.getBookingDate());
        if (booking.getTimeSlot() != null) {
            msg.append(" at ").append(booking.getTimeSlot());
        }

        msg.append(". Booking ID: ").append(booking.getBookingId());

        message.setMessage(msg.toString());

        villageInboxRepository.save(message);
        System.out.println("📬 Village notified of confirmed booking: " + booking.getBookingId());
    }


    @Override
    public void notifyVillageBookingClosed(Activity activity, LocalDate date, LocalTime slot) {
        VillageInboxMessage message = new VillageInboxMessage();
        message.setVillage(activity.getVillage());
        message.setType(VillageNotificationType.BOOKING_CLOSED); // Add this to your enum
        message.setActivity(activity);
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);

        StringBuilder msg = new StringBuilder("⛔ Booking closed for activity '");
        msg.append(activity.getActivityName()).append("' on ").append(date);

        if (slot != null) {
            msg.append(" at ").append(slot);
        }

        msg.append(". All spots are filled and no further bookings can be accepted.");

        message.setMessage(msg.toString());

        villageInboxRepository.save(message);
        System.out.println("📬 Village notified: Booking closed for " + activity.getActivityName() + " on " + date + (slot != null ? " at " + slot : ""));
    }

    @Override
    public void notifyVillageBookingCancelled(Long bookingId, String cancelReason) {
        ActivityBooking booking = activityBookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        VillageInboxMessage message = new VillageInboxMessage();
        message.setVillage(booking.getVillage());
        message.setType(VillageNotificationType.BOOKING_CANCELLED);
        message.setActivity(booking.getActivity());
        message.setActivityBooking(booking);
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);

        StringBuilder msg = new StringBuilder("❌ Booking cancelled for activity '");
        msg.append(booking.getActivity().getActivityName())
                .append("' on ").append(booking.getBookingDate());

        if (booking.getTimeSlot() != null) {
            msg.append(" at ").append(booking.getTimeSlot());
        }

        msg.append(". People: ").append(booking.getNumberOfPeople())
                .append(". Reason: ").append(cancelReason);

        message.setMessage(msg.toString());

        villageInboxRepository.save(message);
        System.out.println("📬 Village notified of cancelled booking: " + bookingId);
    }

    @Override
    public List<VillageInboxMessage> getVillageMessagesByTimeStamp(Village village) {
        return villageInboxRepository.findByVillageOrderByIsReadAscTimestampDesc(village);
    }

    @Override
    public List<VillageInboxMessage> getUnreadVillageMessagesCount(Village village) {
        return villageInboxRepository.findByVillageAndIsReadFalseOrderByTimestampDesc(village);
    }

    @Transactional
    @Override
    public void markAllVillageMessagesAsRead(Village village) {
        villageInboxRepository.markAllAsRead(village);
    }

    @Transactional
    @Override
    public void markVillageMessageAsRead(Long messageId) {
        VillageInboxMessage msg = villageInboxRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        msg.setRead(true);
        villageInboxRepository.save(msg);
    }

    @Override
    public List<UserInboxMessage> getAllMessagesForUser(Long userId) {
        return userInboxRepository.findByUserIdOrderByIsReadAscTimestampDesc(userId);
    }


    @Override
    public List<UserInboxMessage> getUnreadMessagesForUser(Long userId) {
        return userInboxRepository.findByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    @Override
    public void markUserMessageAsRead(Long messageId) {
        userInboxRepository.markMessageAsRead(messageId);
    }


    @Transactional
    @Override
    public void markAllUserMessagesAsRead(Long userId) {
        userInboxRepository.markAllMessagesAsRead(userId);
    }



}
