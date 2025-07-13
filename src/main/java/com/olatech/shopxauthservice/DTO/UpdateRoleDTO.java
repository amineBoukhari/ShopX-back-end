package com.olatech.shopxauthservice.DTO;

import com.olatech.shopxauthservice.Model.StoreRole;
import jakarta.validation.constraints.NotNull;

// UpdateRoleDTO.java
public class UpdateRoleDTO {
    @NotNull
    private StoreRole.StoreRoleType role;

    // Getters and Setters

    public @NotNull StoreRole.StoreRoleType getRole() {
        return role;
    }

    public void setRole(@NotNull StoreRole.StoreRoleType role) {
        this.role = role;
    }
}
