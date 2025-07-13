package com.olatech.shopxauthservice.DTO;

import com.olatech.shopxauthservice.Model.StoreRole;

import java.time.LocalDateTime;

// StoreStaffDTO.java
public class StoreStaffDTO {
    private int userId;
    private String username;
    private String email;
    private StoreRole.StoreRoleType role;
    private LocalDateTime joinedAt;

    // Getters and Setters


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

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

    public StoreRole.StoreRoleType getRole() {
        return role;
    }

    public void setRole(StoreRole.StoreRoleType role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
