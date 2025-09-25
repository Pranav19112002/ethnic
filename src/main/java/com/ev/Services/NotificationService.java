package com.ev.Services;

import com.ev.Model.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface NotificationService {


    void sendInboxMessageToUserVillageStatusChange(Long userId,
                                                   String message,
                                                   Activity activity,
                                                   ActivityBooking booking,
                                                   Village village);

    void sendInboxMessageToUserActivityStatusChange(Long userId,
                                                    String message,
                                                    Activity activity,
                                                    ActivityBooking booking,
                                                    Village village);

    void sendInboxMessageToUserBookingCancelled(Long userId,
                                                String message,
                                                Activity activity,
                                                ActivityBooking booking,
                                                Village village);

    void sendVillageNotificationToAdmin(Long villageId, String message);
    void sendActivityNotificationToAdmin(Activity activity, String message);
    List<AdminInboxMessage> getAllNotificationsByTime();
    List<AdminInboxMessage> getAllMessages();
    void markAllAdminMessagesAsRead();
    void deleteAllAdminMessages();

    List<UserInboxMessage> getAllMessagesForUser(Long userId);

    List<UserInboxMessage> getUnreadMessagesForUser(Long userId);

    void markMessageAsRead(Long id);

    void sendVillageNotification(Village village, VillageNotificationType type, String message);

    void sendVillageNotification(Village village, VillageNotificationType type, String message, Activity activity);

    void sendInboxMessageToAllUsers(User user, String message, Activity activity, ActivityBooking booking);

    void notifyVillageOfNewBooking(ActivityBooking booking);

    void notifyVillageBookingConfirmed(ActivityBooking booking);

    void notifyVillageBookingClosed(Activity activity, LocalDate date, LocalTime slot);

    void notifyVillageBookingCancelled(Long bookingId, String cancelReason);

    List<VillageInboxMessage> getVillageMessagesByTimeStamp(Village village);

    List<VillageInboxMessage> getUnreadVillageMessagesCount(Village village);

    @Transactional
    void markAllVillageMessagesAsRead(Village village);

    @Transactional
    void markVillageMessageAsRead(Long messageId);

    @Transactional
    void markUserMessageAsRead(Long messageId);

    @Transactional
    void markAllUserMessagesAsRead(Long userId);
}
