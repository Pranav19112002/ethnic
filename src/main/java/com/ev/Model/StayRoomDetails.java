package com.ev.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stay_room_details")
public class StayRoomDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @Column(name = "room_name", nullable = false)
    private String roomName;

    @Column(name = "no_of_rooms")
    private int numberOfRooms;

    private boolean isAC;

    @Min(0)
    private int availableRooms;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal pricePerRoom;

    @ElementCollection
    @CollectionTable(name = "room_amenities", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "amenity")
    private List<String> amenities = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "room_type")
    private String roomType;

    @ElementCollection
    @CollectionTable(name = "room_photos", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "photo_name")
    private List<String> roomPhotoNames = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stay_id")
    private VillageStay villageStay;

    // Getters and Setters


    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
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

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public List<String> getRoomPhotoNames() {
        return roomPhotoNames;
    }

    public void setRoomPhotoNames(List<String> roomPhotoNames) {
        this.roomPhotoNames = roomPhotoNames;
    }

    public VillageStay getVillageStay() {
        return villageStay;
    }

    public void setVillageStay(VillageStay villageStay) {
        this.villageStay = villageStay;
    }
}
