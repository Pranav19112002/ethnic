package com.ev.Model;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

public class UserDto {

    private Long userId;

    @Size(min = 2, max = 20, message = "Name must be between 2 and 20 characters")
    private String userName;

    @Email(message = "Invalid email format. Example: 'contact@greenwood.com'")
    private String userEmail;

    @Pattern(
            regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10}$",
            message = "Invalid mobile number format. It should be a 10-digit number or include a country code (e.g., +91 9876543210)."
    )
    private String userPhone;
    
    private MultipartFile profilePic;

    @Size(min = 8, message = "Current Password must be at least 8 characters long")
    private String userPassword;

    @Size(min = 8, message = "New Password must be at least 8 characters long")
    private String newPassword;

    @Size(min = 8, message = "Confirm Password must be at least 8 characters long")
    private String confirmPassword;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public MultipartFile getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(MultipartFile profilePic) {
        this.profilePic = profilePic;
    }
    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
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
}

