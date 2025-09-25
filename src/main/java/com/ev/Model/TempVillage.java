package com.ev.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "temp_village")
public class TempVillage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long villageId;

    private String villageName;
    private String villageEmail;
    private String villageLocation;
    private String villageRegNumber;
    private String villageAuthorityName;
    private String villageDescription;
    private String legalRepresentativeName;
    private String representativePosition;
    private String officialIdNumber;
    private String contactPersonName;
    private String contactEmail;
    private String contactNo;
    private String altContactNo;
    private String bankName;
    private String bankAccountNumber;
    private String tin;
    private String villageType;

    private Double latitude;
    private Double longitude;

    private String offDocFileName;
    private String offStampOrSealFileName;


    // Status: PENDING, APPROVED, REJECTED
    @Column(nullable = false)
    @ColumnDefault("'Pending'")
    @Pattern(regexp = "^(Pending|Approved|Rejected)$", message = "Status must be 'Pending', 'Approved', or 'Rejected'")
    private String status = "Pending";

    private LocalDate requestedAt = LocalDate.now();

    private LocalDate reviewedAt;

    private String adminComment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVillageId() {
        return villageId;
    }

    public void setVillageId(Long villageId) {
        this.villageId = villageId;
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public String getVillageEmail() {
        return villageEmail;
    }

    public void setVillageEmail(String villageEmail) {
        this.villageEmail = villageEmail;
    }

    public String getVillageLocation() {
        return villageLocation;
    }

    public void setVillageLocation(String villageLocation) {
        this.villageLocation = villageLocation;
    }

    public String getVillageRegNumber() {
        return villageRegNumber;
    }

    public void setVillageRegNumber(String villageRegNumber) {
        this.villageRegNumber = villageRegNumber;
    }

    public String getVillageAuthorityName() {
        return villageAuthorityName;
    }

    public void setVillageAuthorityName(String villageAuthorityName) {
        this.villageAuthorityName = villageAuthorityName;
    }

    public String getVillageDescription() {
        return villageDescription;
    }

    public void setVillageDescription(String villageDescription) {
        this.villageDescription = villageDescription;
    }

    public String getLegalRepresentativeName() {
        return legalRepresentativeName;
    }

    public void setLegalRepresentativeName(String legalRepresentativeName) {
        this.legalRepresentativeName = legalRepresentativeName;
    }

    public String getRepresentativePosition() {
        return representativePosition;
    }

    public void setRepresentativePosition(String representativePosition) {
        this.representativePosition = representativePosition;
    }

    public String getOfficialIdNumber() {
        return officialIdNumber;
    }

    public void setOfficialIdNumber(String officialIdNumber) {
        this.officialIdNumber = officialIdNumber;
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

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getTin() {
        return tin;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }

    public String getVillageType() {
        return villageType;
    }

    public void setVillageType(String villageType) {
        this.villageType = villageType;
    }

    public String getOffDocFileName() {
        return offDocFileName;
    }

    public void setOffDocFileName(String offDocFileName) {
        this.offDocFileName = offDocFileName;
    }

    public String getOffStampOrSealFileName() {
        return offStampOrSealFileName;
    }

    public void setOffStampOrSealFileName(String offStampOrSealPath) {
        this.offStampOrSealFileName = offStampOrSealPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDate requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDate getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDate reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
