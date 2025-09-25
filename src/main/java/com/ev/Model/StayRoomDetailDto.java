package com.ev.Model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

public class StayRoomDetailDto {

    private Long id;

    private Long stayId;// Optional for edit/update flows

    @NotBlank(message = "Room name is required")
    private String roomName;

    @Min(value = 1, message = "Number of rooms must be at least 1")
    private int numberOfRooms;

    private boolean isAC;

    @Min(value = 0, message = "Available rooms cannot be negative")
    private int availableRooms;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal pricePerRoom;

    @Size(max = 10, message = "Maximum 10 amenities allowed")
    private List<@NotBlank(message = "Amenity cannot be blank") String> amenities;

    @Size(max = 1000, message = "Notes must be under 1000 characters")
    private String notes;

    @Size(max = 5, message = "Maximum 5 photos can be uploaded per room")
    private List<MultipartFile> roomPhotos;

    // Optional: for filtering or future booking logic
    private String roomType;

    private List<String> tags;

    @AssertTrue(message = "Available rooms cannot exceed total number of rooms")
    public boolean isAvailabilityValid() {
        return availableRooms <= numberOfRooms;
    }

    private List<String> roomPhotoNames = new ArrayList<>();
    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStayId() {
        return stayId;
    }

    public void setStayId(Long stayId) {
        this.stayId = stayId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getNumberOfRooms() {
        return numberOfRooms;
    }

    public void setNumberOfRooms(int numberOfRooms) {
        this.numberOfRooms = numberOfRooms;
    }

    public boolean isAC() {
        return isAC;
    }

    public void setAC(boolean AC) {
        isAC = AC;
    }

    public int getAvailableRooms() {
        return availableRooms;
    }

    public void setAvailableRooms(int availableRooms) {
        this.availableRooms = availableRooms;
    }

    public BigDecimal getPricePerRoom() {
        return pricePerRoom;
    }

    public void setPricePerRoom(BigDecimal pricePerRoom) {
        this.pricePerRoom = pricePerRoom;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<MultipartFile> getRoomPhotos() {
        return roomPhotos;
    }

    public void setRoomPhotos(List<MultipartFile> roomPhotos) {
        this.roomPhotos = roomPhotos;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getRoomPhotoNames() {
        return roomPhotoNames;
    }

    public void setRoomPhotoNames(List<String> roomPhotoNames) {
        this.roomPhotoNames = roomPhotoNames;
    }
}
