package com.ev.Repository;

import com.ev.Model.BookingsFeedBack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingsFeedbackRepository extends JpaRepository<BookingsFeedBack, Long> {

    List<BookingsFeedBack> findByActivityBooking_BookingId(Long bookingId);

    @Query("SELECT COALESCE(SUM(b.rating), 0) FROM BookingsFeedBack b")
    long getTotalStarsReceived();

}
