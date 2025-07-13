package com.olatech.shopxauthservice.DTO;

import com.olatech.shopxauthservice.Model.Users;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotBlank;

public class LoginBody {

    private String username;
    private String email;
    private String phone;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    private String mode = "email"; // Valeur par défaut

    // Constructeur par défaut
    public LoginBody() {}

    // Getters et setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}