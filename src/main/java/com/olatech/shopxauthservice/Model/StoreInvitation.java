package com.olatech.shopxauthservice.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
public class StoreInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "store_invitation_sequence", sequenceName = "store_invitation_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Email
    @NotNull
    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreRole.StoreRoleType role;

    @ManyToOne
    @JoinColumn(name = "inviter_id", nullable = false)
    private Users inviter;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING;

    public enum InvitationStatus {
        PENDING,    // Invitation envoyée, en attente de réponse
        ACCEPTED,   // Invitation acceptée par l'utilisateur
        DECLINED,   // Invitation refusée par l'utilisateur
        EXPIRED,    // Invitation expirée (délai dépassé)
        CANCELLED   // Invitation annulée par l'inviteur
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = createdAt.plusDays(7); // Expiration par défaut après 7 jours
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
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

    public Users getInviter() {
        return inviter;
    }

    public void setInviter(Users inviter) {
        this.inviter = inviter;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public void setStatus(InvitationStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "StoreInvitation{" +
                "id=" + id +
                ", store=" + store.getName() +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                '}';
    }
}