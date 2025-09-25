package com.ev.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "activity_booking")
public class ActivityBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "village_id")
    private Village village;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "time_slot", nullable = false)
    private LocalTime timeSlot;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "number_of_people", nullable = false)
    private Integer numberOfPeople;

    @Column(name = "contact_no",unique = false, nullable = false)
    private String contactNo;

    @Column(name = "alt_contact_no",unique = false, nullable = false)
    private String altContactNo;

    @Column(name = "status", nullable = false)
    @Pattern(regexp = "^(pending|confirmed|cancelled|ended|expired)$", message = "Invalid booking status")
    private String status = "pending";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Transient
    private String formattedExpiry;

    @Column(name = "exp_or_cancel_reason")
    private String expiredOrCancelReason;

    @Column(name = "expired_or_cancelled_at")
    private LocalDateTime expiredOrCancelledAt;


    @Column(name = "payment_id")
    private String paymentId;  // Stripe session ID or charge ID

    @Column(name = "payment_status")
    private String paymentStatus;  // "PENDING", "PAID", "FAILED"

    @Column(name = "payment_method")
    private String paymentMethod;  // e.g., "card", "UPI", etc.

    @Column(name = "receipt_url", columnDefinition = "TEXT")
    private String receiptUrl;  // Stripe receipt URL if available

    @Column(name = "payment_date")
    private LocalDateTime paymentAt;

    @Column(name = "amount_paid")
    private Double amountPaid;

    @Column(name = "currency")
    private String currency;

    private Boolean paid;

    @OneToOne(mappedBy = "activityBooking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private BookingsFeedBack bookingFeedback;


    @Transient
    private boolean hasFeedback;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusHours(5); // 💡 Set expiry here
        if (this.status == null) {
            this.status = "pending";
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = "PENDING";
        }
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Village getVillage() {
        return village;
    }

    public void setVillage(Village village) {
        this.village = village;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(LocalTime timeSlot) {
        this.timeSlot = timeSlot;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(Integer numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getAltContactNo() {
        return altContactNo;
    }

    public void setAltContactNo(String altContactNo) {
        this.altContactNo = altContactNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getFormattedExpiry() {
        if (expiresAt != null) {
            return expiresAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        }
        return "";
    }

    public String getExpiredOrCancelReason() {
        return expiredOrCancelReason;
    }

    public void setExpiredOrCancelReason(String expiredOrCancelReason) {
        this.expiredOrCancelReason = expiredOrCancelReason;
    }

    public LocalDateTime getExpiredOrCancelledAt() {
        return expiredOrCancelledAt;
    }

    public void setExpiredOrCancelledAt(LocalDateTime expiredOrCancelledAt) {
        this.expiredOrCancelledAt = expiredOrCancelledAt;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReceiptUrl() {
        return receiptUrl;
    }

    public void setReceiptUrl(String receiptUrl) {
        this.receiptUrl = receiptUrl;
    }

    public LocalDateTime getPaymentAt() {
        return paymentAt;
    }

    public void setPaymentAt(LocalDateTime paymentAt) {
        this.paymentAt = paymentAt;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public BookingsFeedBack getBookingFeedback() {
        return bookingFeedback;
    }

    public void setBookingFeedback(BookingsFeedBack bookingFeedback) {
        this.bookingFeedback = bookingFeedback;
    }


    public boolean isHasFeedback() {
        return hasFeedback;
    }

    public void setHasFeedback(boolean hasFeedback) {
        this.hasFeedback = hasFeedback;
    }
}
