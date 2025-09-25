package com.ev.Model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class BookingsFeedBack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to booking
    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private ActivityBooking activityBooking;


    // ⭐ Rating and feedback
    @Column(name = "rating")
    private Integer rating;

    @Column(name = "feedback", length = 500)
    private String feedback;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    // Optional: who submitted it (for admin tools)
    @Column(name = "submitted_by")
    private String submittedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ActivityBooking getActivityBooking() {
        return activityBooking;
    }

    public void setActivityBooking(ActivityBooking activityBooking) {
        this.activityBooking = activityBooking;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }
}
