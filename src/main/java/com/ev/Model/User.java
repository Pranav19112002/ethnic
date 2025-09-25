package com.ev.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "name")
    private String userName;

    @Column(name = "email", unique = true)
    private String userEmail;

    @Column(name = "phone")
    private String userPhone;

    @Column(name = "password")
    private String userPassword;

    @Column(name = "profile_picture")
    private String userProfilePicName;

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

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserProfilePicName() {
        return userProfilePicName;
    }

    public void setUserProfilePicName(String userProfilePicName) {
        this.userProfilePicName = userProfilePicName;
    }


}

