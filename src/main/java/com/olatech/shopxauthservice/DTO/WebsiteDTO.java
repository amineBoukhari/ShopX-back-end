package com.olatech.shopxauthservice.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class WebsiteDTO {
    
    @NotNull(message = "Website name cannot be null")
    @NotBlank(message = "Website name cannot be blank")
    @Size(min = 3, max = 100, message = "Website name must be between 3 and 100 characters")
    private String name;
    
    @NotNull(message = "Subdomain cannot be null")
    @NotBlank(message = "Subdomain cannot be blank")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Subdomain must contain only lowercase letters, numbers, and hyphens")
    @Size(min = 3, max = 63, message = "Subdomain must be between 3 and 63 characters")
    private String subdomain;
    
    @NotNull(message = "Template cannot be null")
    @NotBlank(message = "Template cannot be blank")
    private String template;
    
    // Default constructor
    public WebsiteDTO() {}
    
    // Constructor with parameters
    public WebsiteDTO(String name, String subdomain, String template) {
        this.name = name;
        this.subdomain = subdomain;
        this.template = template;
    }
    
    // Getters and Setters
    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name != null ? name.trim() : null; 
    }
    
    public String getSubdomain() { 
        return subdomain; 
    }
    
    public void setSubdomain(String subdomain) { 
        this.subdomain = subdomain != null ? subdomain.trim().toLowerCase() : null; 
    }
    
    public String getTemplate() { 
        return template; 
    }
    
    public void setTemplate(String template) { 
        this.template = template != null ? template.trim() : null; 
    }
    
    @Override
    public String toString() {
        return "WebsiteDTO{" +
                "name='" + name + '\'' +
                ", subdomain='" + subdomain + '\'' +
                ", template='" + template + '\'' +
                '}';
    }
}