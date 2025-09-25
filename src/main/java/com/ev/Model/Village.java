package com.ev.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@DynamicInsert
@Table(name = "village", uniqueConstraints = {
        @UniqueConstraint(columnNames = "village_name"),
        @UniqueConstraint(columnNames = "register_no"),
        @UniqueConstraint(columnNames = "contact_email"),
        @UniqueConstraint(columnNames = "contact_no"),
        @UniqueConstraint(columnNames = "acc_no")
})
public class Village {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long villageId;

    @Column(name = "registered_date")
    private LocalDateTime registeredDate;

    @Column(name = "village_name", unique = true, nullable = false)
    private String villageName;

    @Column(name = "village_email", unique = true, nullable = false)
    private String villageEmail;

    @Column(name = "village_location")
    private String villageLocation;

    @Column(name = "register_no", unique = true, nullable = false)
    private String villageRegNumber;

    @Column(name = "authority_name")
    private String villageAuthorityName;

    @Column(name = "official_document_filename")
    private String offDocFileName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String villageDescription;

    @Column(name = "representative")
    private String villageRepresentativeName;

    @Column(name = "position")
    private String representativePosition;

    @Column(name = "off_id_no")
    private String officialIdNumber;

    @Column(name = "contact_person")
    private String contactPersonName;

    @Column(name = "contact_email", unique = true, nullable = false)
    private String contactEmail;

    @Column(name = "contact_no", unique = true, nullable = false)
    private String contactNo;

    @Column(name = "alt_contact_no")
    private String altContactNo;

    @Column(name = "bank")
    private String bankName;

    @Column(name = "acc_no", unique = true, nullable = false)
    private String bankAccountNumber;

    @Column(name = "tax_info_no")
    private String tin;

    @Column(name = "stamp_seal_filename")
    private String offStampOrSealFileName;

    @Column(name = "village_type")
    private String villageType;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "current_status", nullable = false)
    @ColumnDefault("'Available'")
    @Pattern(regexp = "^(Available|Unavailable|Deactivated)$", message = "Status must be 'Available', 'Unavailable', or 'Deactivated'")
    private String villageCurrentStatus = "Available";

    @Column(name = "status", nullable = false)
    @ColumnDefault("'request-send'")
    @Pattern(regexp = "^(request-send|Approved|Rejected)$", message = "Status must be 'request-send', 'Approved', or 'Rejected'")
    private String villageStatus = "request-send";

    @Column(name = "approved_or_rejected_at")
    private LocalDateTime approvedOrRejectedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "village_photos", joinColumns = @JoinColumn(name = "village_id"))
    @Column(name = "photo_filename")
    private List<String> villagePhotos;

    @Column(name = "password")
    private String villagePassword;

    @PrePersist
    public void prePersist() {
        if (this.villageStatus == null) {
            this.villageStatus = "request-send";
        }
        if (this.villageCurrentStatus == null) {
            this.villageCurrentStatus = "Available";
        }
    }

    private String villageProfileImage;

    public Long getVillageId() {
        return villageId;
    }

    public void setVillageId(Long villageId) {
        this.villageId = villageId;
    }

    public LocalDateTime getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(LocalDateTime registeredDate) {
        this.registeredDate = registeredDate;
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

    public String getOffDocFileName() {
        return offDocFileName;
    }

    public void setOffDocFileName(String offDocFileName) {
        this.offDocFileName = offDocFileName;
    }

    public String getVillageDescription() {
        return villageDescription;
    }

    public void setVillageDescription(String villageDescription) {
        this.villageDescription = villageDescription;
    }

    public String getVillageRepresentativeName() {
        return villageRepresentativeName;
    }

    public void setVillageRepresentativeName(String villageRepresentativeName) {
        this.villageRepresentativeName = villageRepresentativeName;
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

    public String getOffStampOrSealFileName() {
        return offStampOrSealFileName;
    }

    public void setOffStampOrSealFileName(String offStampOrSealFileName) {
        this.offStampOrSealFileName = offStampOrSealFileName;
    }

    public String getVillageType() {
        return villageType;
    }

    public void setVillageType(String villageType) {
        this.villageType = villageType;
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


    public String getVillageCurrentStatus() {
        return villageCurrentStatus;
    }

    public void setVillageCurrentStatus(String villageCurrentStatus) {
        this.villageCurrentStatus = villageCurrentStatus;
    }

    public String getVillageStatus() {
        return villageStatus;
    }

    public void setVillageStatus(String villageStatus) {
        this.villageStatus = villageStatus;
    }

    public LocalDateTime getApprovedOrRejectedAt() {
        return approvedOrRejectedAt;
    }

    public void setApprovedOrRejectedAt(LocalDateTime approvedOrRejectedAt) {
        this.approvedOrRejectedAt = approvedOrRejectedAt;
    }

    public List<String> getVillagePhotos() {
        return villagePhotos;
    }

    public void setVillagePhotos(List<String> villagePhotos) {
        this.villagePhotos = villagePhotos;
    }

    public String getVillageProfileImage() {
        return villageProfileImage;
    }

    public void setVillageProfileImage(String villageProfileImage) {
        this.villageProfileImage = villageProfileImage;
    }

    public String getVillagePassword() {
        return villagePassword;
    }

    public void setVillagePassword(String villagePassword) {
        this.villagePassword = villagePassword;
    }

}

