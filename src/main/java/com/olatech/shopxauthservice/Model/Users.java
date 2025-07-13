package com.olatech.shopxauthservice.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Entity
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "user_sequence", sequenceName = "user_sequence", allocationSize = 1)
    private int id;

    @Column(unique=true)
    private String username;

    @Getter
    @Setter
    private String password;

    @Email
    @NotNull
    private String email;

    @Setter
    private String provider = "local";
    
    @Setter
    private String providerId; // ID fourni par le provider OAuth2
    
    @Getter
    private String name; // Nom complet fourni par OAuth2
    
    @Column(columnDefinition = "boolean default false")
    private boolean emailVerified;
    
    private String imageUrl; // URL de la photo de profil depuis OAuth2
    
    @Column(columnDefinition = "boolean default false")
    private boolean profileCompleted;

    @Getter
    @Setter
    private String firstName;

    @Getter
    @Setter
    private String lastName;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Collection<Sessions> sessions;


    @Column(unique=true)
    public @Email String getEmail() {
        return email;
    }

    public void setEmail(@Email String email) {
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public boolean isProfileCompleted() {
        return profileCompleted;
    }
    
    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }
    
    public Collection<Sessions> getSessions() {
        return sessions;
    }
    
    public void setSessions(Collection<Sessions> sessions) {
        this.sessions = sessions;
    }

    @Override
    public String toString() {
        return "Users{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", provider='" + provider + '\'' +
                ", name='" + name + '\'' +
                ", profileCompleted='" + profileCompleted + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
