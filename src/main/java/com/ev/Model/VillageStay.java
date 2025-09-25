package com.ev.Model;


import jakarta.persistence.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class VillageStay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stayId;

    @ManyToOne
    @JoinColumn(name = "village_id", nullable = false)
    private Village village;

    @Column(name = "stay", nullable = false)
    private String villageStayName;

    @Column(name = "stay_type", nullable = false)
    private String stayType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "stay_photos", joinColumns = @JoinColumn(name = "stay_id"))
    @Column(name = "photo_url")
    private List<String> stayPhotosNames;

    @Column(name = "stay_place")
    private String stayPlace;

    @Column(name = "contact_person")
    private String contactPersonName;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column(name = "contact_no", nullable = false)
    private String contactNo;

    @Column(name = "alt_contact_no")
    private String altContactNo;

    @Column(name = "price_starts_from")
    private BigDecimal priceStartsFrom;

    @OneToMany(mappedBy = "villageStay", cascade = CascadeType.ALL, orphanRemoval = true)
    @Valid
    private List<StayRoomDetails> roomDetails = new ArrayList<>();

    @Column(name = "status")
    private boolean isActive = true;

    @Column(name = "reg_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getStayId() {
        return stayId;
    }

    public void setStayId(Long stayId) {
        this.stayId = stayId;
    }

    public Village getVillage() {
        return village;
    }

    public void setVillage(Village village) {
        this.village = village;
    }

    public String getVillageStayName() {
        return villageStayName;
    }

    public void setVillageStayName(String villageStayName) {
        this.villageStayName = villageStayName;
    }

    public String getStayType() {
        return stayType;
    }

    public void setStayType(String stayType) {
        this.stayType = stayType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getStayPhotosNames() {
        return stayPhotosNames;
    }

    public void setStayPhotosNames(List<String> stayPhotosNames) {
        this.stayPhotosNames = stayPhotosNames;
    }

    public String getStayPlace() {
        return stayPlace;
    }

    public void setStayPlace(String stayPlace) {
        this.stayPlace = stayPlace;
    }

    public String getContactPersonName() {
        return contactPersonName;
    }

    public void setContactPersonName(String contactPersonName) {
        this.contactPersonName = contactPersonName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
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

    public BigDecimal getPriceStartsFrom() {
        return priceStartsFrom;
    }

    public void setPriceStartsFrom(BigDecimal priceStartsFrom) {
        this.priceStartsFrom = priceStartsFrom;
    }

    public List<StayRoomDetails> getRoomDetails() {
        return roomDetails;
    }

    public void setRoomDetails(List<StayRoomDetails> roomDetails) {
        this.roomDetails = roomDetails;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

