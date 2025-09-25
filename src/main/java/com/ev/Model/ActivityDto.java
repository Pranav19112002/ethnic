package com.ev.Model;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ActivityDto {

    private Long activityId;

    @NotBlank(message = "Activity Name is required")
    private String activityName;

    @NotBlank(message = "Activity Place is required.")
    private String activityPlace;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Village ID is required")
    private Long villageId;

    @NotBlank(message = "Activity Type is required")
    private String activityType;

    @NotNull(message = "Duration is required")
    @Min(value = 30, message = "Duration must be at least 30 minutes")
    private Integer duration;

    @NotNull(message = "Price per person is required")
    @Positive(message = "Price must be greater than zero")
    private Double price;

    @Size(max = 5, message = "Maximum 5 photos can be uploaded")
    private List<MultipartFile> activityPhotos;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private List<String> availableDays;

    private List<LocalTime> timeSlots;

    private Long alternativeActivityId;

    private Boolean notifyUsers;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime eventDateTime;


    @Min(value = 1, message = "At least 1 person must be allowed per slot")
    @Max(value = 500, message = "Maximum 500 people allowed per slot")
    private Integer noOfPeopleAllowedForSloted;

    @Min(value = 1, message = "At least 1 person must be allowed per day")
    @Max(value = 1000, message = "Maximum 1000 people allowed per day")
    private Integer noOfPeopleAllowedForDateOnly;
    private Boolean notifyUsersRealTime;

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

    public Long getVillageId() {
        return villageId;
    }

    public void setVillageId(Long villageId) {
        this.villageId = villageId;
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

    public List<MultipartFile> getActivityPhotos() {
        return activityPhotos;
    }

    public void setActivityPhotos(List<MultipartFile> activityPhotos) {
        this.activityPhotos = activityPhotos;
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

    public Long getAlternativeActivityId() {
        return alternativeActivityId;
    }

    public void setAlternativeActivityId(Long alternativeActivityId) {
        this.alternativeActivityId = alternativeActivityId;
    }

    public Boolean getNotifyUsers() {
        return notifyUsers;
    }

    public void setNotifyUsers(Boolean notifyUsers) {
        this.notifyUsers = notifyUsers;
    }

    public LocalDateTime getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(LocalDateTime eventDateTime) {
        this.eventDateTime = eventDateTime;
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

    public Boolean getNotifyUsersRealTime() {
        return notifyUsersRealTime;
    }

    public void setNotifyUsersRealTime(Boolean notifyUsersRealTime) {
        this.notifyUsersRealTime = notifyUsersRealTime;
    }
}

