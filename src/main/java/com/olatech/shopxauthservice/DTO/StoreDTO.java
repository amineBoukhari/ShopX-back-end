package com.olatech.shopxauthservice.DTO;

import com.olatech.shopxauthservice.Model.StoreRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class StoreDTO {
    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private String logo;

    // Champs Facebook
    private String facebookBusinessId;
    private String facebookCatalogId;
    private String facebookToken;

    // Getters and Setters

    public @NotBlank @Size(min = 3, max = 100) String getName() {
        return name;
    }

    public void setName(@NotBlank @Size(min = 3, max = 100) String name) {
        this.name = name;
    }

    public @Size(max = 500) String getDescription() {
        return description;
    }

    public void setDescription(@Size(max = 500) String description) {
        this.description = description;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getFacebookBusinessId() {
        return facebookBusinessId;
    }

    public void setFacebookBusinessId(String facebookBusinessId) {
        this.facebookBusinessId = facebookBusinessId;
    }

    public String getFacebookCatalogId() {
        return facebookCatalogId;
    }

    public void setFacebookCatalogId(String facebookCatalogId) {
        this.facebookCatalogId = facebookCatalogId;
    }

    public String getFacebookToken() {
        return facebookToken;
    }

    public void setFacebookToken(String facebookToken) {
        this.facebookToken = facebookToken;
    }
}

