package com.ev.Services;

public interface BookingsFeedbackService {

    void saveFeedback(Long bookingId, int rating, String feedback);
}
