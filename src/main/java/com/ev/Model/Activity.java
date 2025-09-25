package com.ev.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "activity")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;

    @Column(name = "activity_name", nullable = false)
    private String activityName;

    @Column(name = "activity_place")
    private String activityPlace;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "village_id", nullable = false)
    private Village village;

    @Column(name = "activity_type", nullable = false)
    private String activityType;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "price")
    private Double price;

    @ElementCollection
    @CollectionTable(name = "activity_photos", joinColumns = @JoinColumn(name = "activity_id"))
    @Column(name = "photo_url")
    private List<String> activityPhotosNames;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ElementCollection
    @CollectionTable(name = "activity_available_days", joinColumns = @JoinColumn(name = "activity_id"))
    @Column(name = "available_day")
    private List<String> availableDays;


    @ElementCollection
    @CollectionTable(name = "activity_time_slots", joinColumns = @JoinColumn(name = "activity_id"))
    @Column(name = "time_slot")
    private List<LocalTime> timeSlots;


    @Column(name = "event_date_time")
    private LocalDateTime eventDateTime;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alternative_activity_id")
    private Activity alternativeActivity;

    @Column(name = "people_allowed_sloted")
    private Integer noOfPeopleAllowedForSloted;

    @Column(name = "people_allowed_date_only")
    private Integer noOfPeopleAllowedForDateOnly;

    @Column(name = "activity_status", nullable = false)
    @ColumnDefault("'Available'")
    @Pattern(regexp = "^(Available|Unavailable|Ended)$",
            message = "Status must be 'Available' or 'Unavailable' or 'Ended'")
    private String activityStatus = "Available";


    @Column(name = "request_status", nullable = false)
    @ColumnDefault("'Request-send'")
    @Pattern(regexp = "^(Request-send|Approved|Rejected)$",
            message = "Status must be 'Request-send','Approved', or 'Rejected'")
    private String requestStatus = "Request-send";

    @Column(name = "registered_date")
    private LocalDateTime registeredDate;


    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityPlace() {
        return activityPlace;
    }

    public void setActivityPlace(String activityPlace) {
        this.activityPlace = activityPlace;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Village getVillage() {
        return village;
    }

    public void setVillage(Village village) {
        this.village = village;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<String> getActivityPhotosNames() {
        return activityPhotosNames;
    }

    public void setActivityPhotosNames(List<String> activityPhotosNames) {
        this.activityPhotosNames = activityPhotosNames;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<String> getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(List<String> availableDays) {
        this.availableDays = availableDays;
    }

    public List<LocalTime> getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(List<LocalTime> timeSlots) {
        this.timeSlots = timeSlots;
    }

    public LocalDateTime getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(LocalDateTime eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public Activity getAlternativeActivity() {
        return alternativeActivity;
    }

    public void setAlternativeActivity(Activity alternativeActivity) {
        this.alternativeActivity = alternativeActivity;
    }

    public Integer getNoOfPeopleAllowedForSloted() {
        return noOfPeopleAllowedForSloted;
    }

    public void setNoOfPeopleAllowedForSloted(Integer noOfPeopleAllowedForSloted) {
        this.noOfPeopleAllowedForSloted = noOfPeopleAllowedForSloted;
    }

    public Integer getNoOfPeopleAllowedForDateOnly() {
        return noOfPeopleAllowedForDateOnly;
    }

    public void setNoOfPeopleAllowedForDateOnly(Integer noOfPeopleAllowedForDateOnly) {
        this.noOfPeopleAllowedForDateOnly = noOfPeopleAllowedForDateOnly;
    }

    public String getActivityStatus() {
        return activityStatus;
    }

    public void setActivityStatus(String activityStatus) {
        if (activityStatus.equals("Available") || activityStatus.equals("Unavailable") || activityStatus.equals("Ended")) {
            this.activityStatus = activityStatus;
        } else {
            throw new IllegalArgumentException("Invalid status: Must be 'Available' or 'Unavailable' or 'Ended'");
        }
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        if (requestStatus.equals("Approved") || requestStatus.equals("Rejected") || requestStatus.equals("Request-send")) {
            this.requestStatus = requestStatus;
        } else {
            throw new IllegalArgumentException("Invalid status: Must be 'Approved' or 'Rejected' or 'Request-send' ");
        }
    }

    public LocalDateTime getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(LocalDateTime registeredDate) {
        this.registeredDate = registeredDate;
    }

    public boolean isAvailable() {
        return "Available".equalsIgnoreCase(this.activityStatus);
    }

    @PrePersist
    public void prePersist() {
        if (this.activityStatus == null) {
            this.activityStatus = "Available";
        }

        if (this.requestStatus == null) {
            this.requestStatus = "Request-send";
        }
    }
}

