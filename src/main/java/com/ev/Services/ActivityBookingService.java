package com.ev.Services;

import com.ev.Exception.ActivityBookingException;
import com.ev.Model.ActivityBooking;
import com.ev.Model.ActivityBookingDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ActivityBookingService {

    ActivityBooking createBooking(ActivityBookingDto activityBookingDto) throws ActivityBookingException;

    Map<String, Object> validateAndCancelIfOverbooked(Long bookingId);

    ActivityBooking getActivityBookingsById(Long bookingId);

    ActivityBooking getBookingsById(Long bookingId);

    void markAsPaid(Long bookingId, String paymentIntentId, String status, String method, String receiptUrl ,Long amountInCents, String currency);

    List<ActivityBooking> getBookingsByUserId(Long userId);

    List<ActivityBooking> getUpcomingBookingsByUserId(String filterDate, String filterActivity, String filterVillage,Long userId);
    List<ActivityBooking> getPendingBookingsByUserId(String filterDate, String filterActivity, String filterVillage,Long userId);
    List<ActivityBooking> getCancelledAndExpiredBookingsByUserId(String filterDate, String filterActivity, String filterVillage,Long userId);
    List<ActivityBooking> getEndedBookingsByUserId(String filterDate, String filterActivity, String filterVillage,Long userId);
    List<ActivityBooking> getPaymentsByUserId(String filterDate, String filterMethod, String filterStatus,Long userId);

    List<ActivityBooking> getFilteredConfirmedBookings(Long filterBookingId ,String filterDate, String filterActivity , Long filterVillage);
    ActivityBookingDto getBookingDetails(Long bookingId);

    List<ActivityBooking> getFilteredPendingBookings(Long filterBookingId ,String filterDate, String filterActivity , Long filterVillage);
    List<ActivityBooking> getFilteredEndedBookings(Long filterBookingId ,String filterDate, String filterActivity , Long filterVillage);
    List<ActivityBooking> getFilteredCancelledOrExpiredBookings(Long filterBookingId ,String filterDate, String filterActivity , Long filterVillage ,String filterStatus);


    boolean cancelBooking(Long bookingId , String cancelReason);


}
