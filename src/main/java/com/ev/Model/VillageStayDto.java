package com.ev.Model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public class VillageStayDto {

    private Long stayId;

    private Long villageId;

    @NotBlank(message = "Village stay name is required")
    private String villageStayName;

    @NotBlank(message = "Stay type is required")
    private String stayType;

    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    @Size(max = 5, message = "Maximum 5 photos can be uploaded")
    private List<MultipartFile> stayPhotos;

    @NotBlank(message = "Stay place is required")
    private String stayPlace;

    @NotBlank(message = "Contact person name is required")
    private String contactPersonName;

    @Email(message = "Contact email must be valid")
    @NotBlank(message = "Contact email is required")
    private String contactEmail;

    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Primary contact number must be a valid Indian mobile number"
    )
    @NotBlank(message = "Primary contact number is required")
    private String contactNo;

    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Alternate contact number must be a valid Indian mobile number"
    )
    private String altContactNo;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private BigDecimal priceStartsFrom;

    @Valid
    private List<StayRoomDetailDto> rooms;

    private List<String> stayPhotosNames;

    private boolean isActive = true;

    public Long getStayId() {
        return stayId;
    }

    public void setStayId(Long stayId) {
        this.stayId = stayId;
    }

    public Long getVillageId() {
        return villageId;
    }

    public void setVillageId(Long villageId) {
        this.villageId = villageId;
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

    public List<MultipartFile> getStayPhotos() {
        return stayPhotos;
    }

    public void setStayPhotos(List<MultipartFile> stayPhotos) {
        this.stayPhotos = stayPhotos;
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

    public List<StayRoomDetailDto> getRoomDetails() {
        return rooms;
    }

    public void setRoomDetails(List<StayRoomDetailDto> rooms) {
        this.rooms = rooms;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<StayRoomDetailDto> getRooms() {
        return rooms;
    }

    public void setRooms(List<StayRoomDetailDto> rooms) {
        this.rooms = rooms;
    }

    public List<String> getStayPhotosNames() {
        return stayPhotosNames;
    }

    public void setStayPhotosNames(List<String> stayPhotosNames) {
        this.stayPhotosNames = stayPhotosNames;
    }
}
