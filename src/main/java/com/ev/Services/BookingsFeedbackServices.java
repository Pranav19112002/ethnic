package com.ev.Services;

import com.ev.Model.ActivityBooking;
import com.ev.Model.BookingsFeedBack;
import com.ev.Repository.ActivityBookingRepository;
import com.ev.Repository.BookingsFeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingsFeedbackServices implements BookingsFeedbackService{

    @Autowired
    private ActivityBookingRepository activityBookingRepository;

    @Autowired
    private BookingsFeedbackRepository bookingsFeedbackRepository;

    @Override
    public void saveFeedback(Long bookingId, int rating, String feedback) {
        Optional<ActivityBooking> optionalBooking = activityBookingRepository.findById(bookingId);
        if (optionalBooking.isPresent()) {
            ActivityBooking activityBooking = optionalBooking.get();
            BookingsFeedBack bookingsFeedBack = activityBooking.getBookingFeedback();

            if (bookingsFeedBack == null) {
                bookingsFeedBack = new BookingsFeedBack();
                bookingsFeedBack.setActivityBooking(activityBooking);
                bookingsFeedBack.setSubmittedBy(activityBooking.getUser().getUserEmail());
            }

            bookingsFeedBack.setFeedback(feedback);
            bookingsFeedBack.setRating(rating);
            bookingsFeedBack.setSubmittedAt(LocalDateTime.now());

            bookingsFeedbackRepository.save(bookingsFeedBack);
        }
    }


    public List<BookingsFeedBack> getFeedbackForBooking(Long bookingId) {
        return bookingsFeedbackRepository.findByActivityBooking_BookingId(bookingId);
    }

}
