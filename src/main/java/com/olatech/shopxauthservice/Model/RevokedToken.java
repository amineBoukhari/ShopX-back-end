package com.olatech.shopxauthservice.Model;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Entité représentant un token révoqué dans le système.
 */
@Entity
@Table(name = "revoked_tokens")
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tokenId; // Identifiant unique (jti) du token JWT

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date expiryDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date revokedAt;

    @Column(length = 50)
    private String revokedBy; // Identifiant de l'utilisateur ou du système ayant révoqué le token

    @Column(length = 20)
    private String tokenType; // ACCESS, REFRESH, etc.

    public RevokedToken() {
    }

    public RevokedToken(String tokenId, Date expiryDate, String tokenType) {
        this.tokenId = tokenId;
        this.expiryDate = expiryDate;
        this.revokedAt = new Date();
        this.tokenType = tokenType;
    }

    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Date getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Date revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getRevokedBy() {
        return revokedBy;
    }

    public void setRevokedBy(String revokedBy) {
        this.revokedBy = revokedBy;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
