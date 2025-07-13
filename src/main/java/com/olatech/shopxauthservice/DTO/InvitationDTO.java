package com.olatech.shopxauthservice.DTO;

import com.olatech.shopxauthservice.Model.StoreRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// InvitationDTO.java
public class InvitationDTO {
    @NotBlank
    @Email
    private String email;

    @NotNull
    private StoreRole.StoreRoleType role;

    // Getters and Setters
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

    @Override
    public String toString() {
        return "InvitationDTO{" +
                "email='" + email + '\'' +
                ", role=" + role +
                '}';
    }

}
