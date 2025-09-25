package com.ev.Model;
import jakarta.persistence.*;

@Entity
@Table(name = "admin")
public class Admin {

    @Id
    private String adminId;

    @Column(nullable = false)
    private String adminName;

    @Column(unique = true, nullable = false)
    private String adminEmail;

    private String adminContactNo;

    @Column(nullable = false)
    private String adminPassword;

    @Column(nullable = false)
    private String adminRole = "Admin";

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

    public String getAdminRole() {
        return adminRole;
    }

    public void setAdminRole(String adminRole) {
        this.adminRole = adminRole;
    }

    public String getAdminProfileImage() {
        return adminProfileImage;
    }

    public void setAdminProfileImage(String adminProfileImage) {
        this.adminProfileImage = adminProfileImage;
    }

}




