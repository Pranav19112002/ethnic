package com.ev.Model;

import com.ev.ValidationGroups.EmailUpdateGroup;
import com.ev.ValidationGroups.NewEmailUpdateGroup;
import com.ev.ValidationGroups.PasswordUpdateGroup;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public class VillageDto {

    @NotBlank(message = "Village name is required. Example: 'Greenwood Village'")
    @Size(max = 100, message = "Village name must be at most 100 characters")
    private String villageName;

    @NotBlank(message = "Village email is required", groups = EmailUpdateGroup.class)
    @Email(message = "Invalid email format", groups = EmailUpdateGroup.class)
    private String villageEmail;

    @NotBlank(message = "New email is required", groups = NewEmailUpdateGroup.class)
    @Email(message = "Invalid email format", groups = NewEmailUpdateGroup.class)
    private String newEmail;

    private Long villageId;

   

    @NotBlank(message = "Village location is required. Example: 'Nairobi, Kenya'")
    private String villageLocation;

    @NotBlank(message = "Registration number is required. Example: 'REG-123456'")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Invalid registration number format. Example: 'REG-123456'")
    private String villageRegNumber;

    @NotBlank(message = "Village authority name is required. Example: 'Greenwood Council'")
    private String villageAuthorityName;

    private MultipartFile officialDoc;

    @NotBlank(message = "Village description is required. Example: 'Greenwood Village is a historical settlement known for its rich heritage.'")
    @Size(max = 500, message = "Description must be at most 500 characters")
    private String villageDescription;

    @NotBlank(message = "Legal representative name is required. Example: 'John Doe'")
    private String legalRepresentativeName;

    @NotBlank(message = "Representative position is required. Example: 'Chairperson'")
    private String representativePosition;

    @NotBlank(message = "Official ID number is required. Example: 'ID12345678'")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Invalid ID number format. Example: 'ID12345678'")
    private String officialIdNumber;

    @NotBlank(message = "Contact person name is required. Example: 'John Doe'")
    private String contactPersonName;

    @NotBlank(message = "Contact email is required. Example: 'contact@greenwood.com'")
    @Email(message = "Invalid email format. Example: 'contact@greenwood.com'")
    private String contactEmail;

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

    @NotBlank(message = "Bank name is required. Example: 'XYZ Bank'")
    private String bankName;

    @NotBlank(message = "Bank account number is required. Example: '123456789012'")
    @Pattern(regexp = "^[0-9]{8,20}$", message = "Invalid account number. It must be between 8 and 20 digits. Example: '123456789012'")
    private String bankAccountNumber;

    @NotBlank(message = "Tax info number is required. Example: 'TAX-987654321'")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Invalid tax number format. Example: 'TAX-987654321'")
    private String taxInfoNumber;

    private MultipartFile offStampOrSeal;

    @NotBlank(message = "Village type is required. Example: 'Cultural', 'Tribal', or 'Tourist'")
    private String villageType;

    @DecimalMin(value = "-90.0", inclusive = true, message = "Latitude must be >= -90.0")
    @DecimalMax(value = "90.0", inclusive = true, message = "Latitude must be <= 90.0")
    private Double latitude;

    @DecimalMin(value = "-180.0", inclusive = true, message = "Longitude must be >= -180.0")
    @DecimalMax(value = "180.0", inclusive = true, message = "Longitude must be <= 180.0")
    private Double longitude;

    @Size(max = 5, message = "Maximum 5 photos can be uploaded")
    private List<MultipartFile> villagePhotos;

    private MultipartFile villageProfileImageFile;

    @Size(min = 8, message = "Current Password must be at least 8 characters long" ,groups =  PasswordUpdateGroup.class)
    private String villagePassword;

    @Size(min = 8, message = "Current Password must be at least 8 characters long" ,groups =  PasswordUpdateGroup.class)
    private String currentPassword;

    @Size(min = 8, message = "New Password must be at least 8 characters long" , groups =  PasswordUpdateGroup.class)
    private String newPassword;

    @Size(min = 8, message = "Confirm Password must be at least 8 characters long", groups =  PasswordUpdateGroup.class)
    private String confirmPassword;

    private String officialDocPath;
    private String offStampOrSealPath;
    private List<String> villagePhotoPaths;


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

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
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

    public MultipartFile getOfficialDoc() {
        return officialDoc;
    }

    public void setOfficialDoc(MultipartFile officialDoc) {
        this.officialDoc = officialDoc;
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

    public String getTaxInfoNumber() {
        return taxInfoNumber;
    }

    public void setTaxInfoNumber(String taxInfoNumber) {
        this.taxInfoNumber = taxInfoNumber;
    }

    public MultipartFile getOffStampOrSeal() {
        return offStampOrSeal;
    }

    public void setOffStampOrSeal(MultipartFile offStampOrSeal) {
        this.offStampOrSeal = offStampOrSeal;
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

    public List<MultipartFile> getVillagePhotos() {
        return villagePhotos;
    }

    public void setVillagePhotos(List<MultipartFile> villagePhotos) {
        this.villagePhotos = villagePhotos;
    }

    public MultipartFile getVillageProfileImageFile() {
        return villageProfileImageFile;
    }

    public void setVillageProfileImageFile(MultipartFile villageProfileImageFile) {
        this.villageProfileImageFile = villageProfileImageFile;
    }

    public String getVillagePassword() {
        return villagePassword;
    }

    public void setVillagePassword(String villagePassword) {
        this.villagePassword = villagePassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getOfficialDocPath() {
        return officialDocPath;
    }

    public void setOfficialDocPath(String officialDocPath) {
        this.officialDocPath = officialDocPath;
    }

    public String getOffStampOrSealPath() {
        return offStampOrSealPath;
    }

    public void setOffStampOrSealPath(String offStampOrSealPath) {
        this.offStampOrSealPath = offStampOrSealPath;
    }

    public List<String> getVillagePhotoPaths() {
        return villagePhotoPaths;
    }

    public void setVillagePhotoPaths(List<String> villagePhotoPaths) {
        this.villagePhotoPaths = villagePhotoPaths;
    }
}


