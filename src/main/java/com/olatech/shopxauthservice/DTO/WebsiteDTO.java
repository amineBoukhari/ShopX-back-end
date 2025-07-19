package com.olatech.shopxauthservice.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class WebsiteDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Subdomain is required")
    private String subdomain;

    @NotNull(message = "Creation method is required")
    private String creationMethod; // "template" or "ai"

    // Optional - only required when creationMethod is "template"
    private String template;

    // Optional - only required when creationMethod is "ai"
    @Size(min = 10, message = "Theme prompt must be at least 10 characters")
    private String themePrompt;

    // Constructors
    public WebsiteDTO() {}

    public WebsiteDTO(String name, String subdomain, String creationMethod, String template, String themePrompt) {
        this.name = name;
        this.subdomain = subdomain;
        this.creationMethod = creationMethod;
        this.template = template;
        this.themePrompt = themePrompt;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }

    public String getCreationMethod() {
        return creationMethod;
    }

    public void setCreationMethod(String creationMethod) {
        this.creationMethod = creationMethod;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getThemePrompt() {
        return themePrompt;
    }

    public void setThemePrompt(String themePrompt) {
        this.themePrompt = themePrompt;
    }

    // Custom validation method
    public boolean isValid() {
        if (name == null || name.trim().isEmpty()) return false;
        if (subdomain == null || subdomain.trim().isEmpty()) return false;
        if (creationMethod == null || creationMethod.trim().isEmpty()) return false;

        if ("template".equals(creationMethod)) {
            return template != null && !template.trim().isEmpty();
        } else if ("ai".equals(creationMethod)) {
            return themePrompt != null && themePrompt.trim().length() >= 10;
        }

        return false;
    }

    @Override
    public String toString() {
        return "WebsiteDTO{" +
                "name='" + name + '\'' +
                ", subdomain='" + subdomain + '\'' +
                ", creationMethod='" + creationMethod + '\'' +
                ", template='" + template + '\'' +
                ", themePrompt='" + themePrompt + '\'' +
                '}';
    }
}