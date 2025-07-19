package com.olatech.shopxauthservice.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.olatech.shopxauthservice.Model.subscriptions.StoreSubscription;
import com.olatech.shopxauthservice.Model.subscriptions.SubscriptionStatus;
import com.olatech.shopxauthservice.Model.subscriptions.UsageMetric;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Date;

@Entity
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "store_sequence", sequenceName = "store_sequence", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String template;

    // Add getter and setter
    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }

    @Column(unique = true)
    private String slug;

    private String description;

    private String logo;

    private boolean isActive = true;

        @Column(name = "creation_method")
    private String creationMethod; // "template" or "ai"

    @Column(name = "template_id")
    private String templateId;

    @Column(name = "original_prompt", columnDefinition = "TEXT")
    private String originalPrompt; // The user's AI prompt

    @Column(name = "ai_generated_theme", columnDefinition = "TEXT")
    private String aiGeneratedTheme; // JSON string of AI theme data

    @Column(name = "last_deployed_at")
    private Date lastDeployedAt;

    @Column(name = "app_runner_url")
    private String appRunnerUrl;

    @Column(name = "created_at")
    private Date createdAt;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Users owner;

    @ManyToMany
    @JoinTable(
            name = "store_staff",
            joinColumns = @JoinColumn(name = "store_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<Users> staff = new HashSet<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<StoreRole> userRoles = new HashSet<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<StoreSubscription> subscriptions = new HashSet<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<UsageMetric> usageMetrics = new HashSet<>();

    private String facebookBusinessId;
    private String facebookCatalogId;
    @Column(length = 512)  // Token Facebook peut être long
    private String facebookToken;
    
    @Column(unique = true)
    private String subdomain;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Users getOwner() {
        return owner;
    }

    public void setOwner(Users owner) {
        this.owner = owner;
    }

    public Set<Users> getStaff() {
        return staff;
    }

    public void setStaff(Set<Users> staff) {
        this.staff = staff;
    }

    public Set<StoreRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<StoreRole> userRoles) {
        this.userRoles = userRoles;
    }

    public Set<StoreSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<StoreSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }
    // Helper method to get the active subscription ne pas afficher en JSON

    public StoreSubscription getActiveSubscription() {
        return this.subscriptions.stream()
                .filter(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE ||
                        sub.getStatus() == SubscriptionStatus.TRIAL)
                .findFirst()
                .orElse(null);
    }

    public Set<UsageMetric> getUsageMetrics() {
        return usageMetrics;
    }

    public void setUsageMetrics(Set<UsageMetric> usageMetrics) {
        this.usageMetrics = usageMetrics;
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

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getOriginalPrompt() {
        return originalPrompt;
    }

    public void setOriginalPrompt(String originalPrompt) {
        this.originalPrompt = originalPrompt;
    }

    public String getAiGeneratedTheme() {
        return aiGeneratedTheme;
    }

    public void setAiGeneratedTheme(String aiGeneratedTheme) {
        this.aiGeneratedTheme = aiGeneratedTheme;
    }

    public Date getLastDeployedAt() {
        return lastDeployedAt;
    }

    public void setLastDeployedAt(Date lastDeployedAt) {
        this.lastDeployedAt = lastDeployedAt;
    }

    public String getAppRunnerUrl() {
        return appRunnerUrl;
    }

    public void setAppRunnerUrl(String appRunnerUrl) {
        this.appRunnerUrl = appRunnerUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
public String toString() {
    return "Store{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", subdomain='" + subdomain + '\'' +
            ", slug='" + slug + '\'' +
            ", template='" + template + '\'' +
            ", isActive=" + isActive +
            ", owner=" + (owner != null ? owner.getUsername() : "null") +
            '}';
}

}