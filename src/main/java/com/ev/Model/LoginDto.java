package com.ev.Model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LoginDto {

    @NotBlank(message = "Email or Admin ID cannot be empty")
    @Pattern(
            regexp = "^(admin[0-9]+|[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6})$",
            message = "Must be a valid email or Admin ID (e.g., admin1, admin2)"
    )
    private String emailOrId;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    public String getEmailOrId() {
        return emailOrId;
    }

    public void setEmailOrId(String emailOrId) {
        this.emailOrId = emailOrId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

