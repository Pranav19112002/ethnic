package com.ev.Model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class AdminDto {

    private String adminId;

    @NotBlank(message = "Admin name is required")
    @Size(min = 3, max = 50, message = "Admin name must be between 3 and 50 characters")
    private String adminName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String adminEmail;

    @Pattern(
            regexp = "^(\\+91)?[6-9][0-9]{9}$",
            message = "Contact number must be 10 digits, optionally prefixed with +91"
    )
    private String adminContactNo;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String adminPassword;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String confirmPassword;

    private String adminRole;

    private MultipartFile adminProfileImageFile;

    private String adminProfileImage;

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getAdminContactNo() {
        return adminContactNo;
    }

    public void setAdminContactNo(String adminContactNo) {
        this.adminContactNo = adminContactNo;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
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

    public String getAdminRole() {
        return adminRole;
    }

    public void setAdminRole(String adminRole) {
        this.adminRole = adminRole;
    }

    public MultipartFile getAdminProfileImageFile() {
        return adminProfileImageFile;
    }

    public void setAdminProfileImageFile(MultipartFile adminProfileImageFile) {
        this.adminProfileImageFile = adminProfileImageFile;
    }

    public String getAdminProfileImage() {
        return adminProfileImage;
    }

    public void setAdminProfileImage(String adminProfileImage) {
        this.adminProfileImage = adminProfileImage;
    }

}


