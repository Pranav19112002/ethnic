package com.ev.Model;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ActivityBookingDto {

    private Long bookingId;

    @NotNull(message = "Booking date is required")
    @FutureOrPresent(message = "Booking date must be today or in the future")
    private LocalDate bookingDate;

    @NotNull(message = "Time slot is required")
    private LocalTime timeSlot;

    @NotNull(message = "Activity ID is required")
    private Long activityId;

    @NotNull(message = "Village ID is required")
    private Long villageId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Number of people is required")
    @Min(value = 1, message = "At least one person must be included")
    private Integer numberOfPeople;

    @NotBlank(message = "Contact number is required.")
    @Pattern(
            regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10}$",
            message = "Invalid mobile number format. It should be a 10-digit number or include a country code (e.g., +91 9876543210)."
    )
    private String contactNo;

    @Pattern(
            regexp = "^$|^(\\+\\d{1,3}[- ]?)?\\d{10}$",
            message = "Invalid alternate contact number format. It should be a 10-digit number or include a country code."
    )
    private String altContactNo;

    // totalAmount is calculated, not user-provided
    private Double totalAmount;

    private LocalDateTime expiryTime;

    private boolean isExpired;

    private String userName;
    private String userProfileImage;

    private String activityName;

    private String villageName;
    private String villageProfileImage;

    private String paymentId;
    private String status;

    private String paymentReceipt;

    private String reason;

    private LocalDateTime  cancelledOrExpiredAt;



    // Getters and Setters


    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
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

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getVillageId() {
        return villageId;
    }

    public void setVillageId(Long villageId) {
        this.villageId = villageId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
        this.isExpired = expiryTime.isBefore(LocalDateTime.now());
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public String getVillageProfileImage() {
        return villageProfileImage;
    }

    public void setVillageProfileImage(String villageProfileImage) {
        this.villageProfileImage = villageProfileImage;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentReceipt() {
        return paymentReceipt;
    }

    public void setPaymentReceipt(String paymentReceipt) {
        this.paymentReceipt = paymentReceipt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCancelledOrExpiredAt() {
        return cancelledOrExpiredAt;
    }

    public void setCancelledOrExpiredAt(LocalDateTime cancelledOrExpiredAt) {
        this.cancelledOrExpiredAt = cancelledOrExpiredAt;
    }
}
