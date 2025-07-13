package com.olatech.shopxauthservice.Model;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.time.LocalDateTime;

@Entity
public class Sessions {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(
            name = "user_id"
    )
    private Users user;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

    @Column(columnDefinition = "TEXT")
    private String token;

    private String userAgent;

    private String ipAddress;
    
    private String deviceName;
    
    private String location;
    
    private Date lastActivityTime;


    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    private Date expiresAt;

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setToken(String s) {
        this.token = s;
    }

    public void setUserAgent(String header) {
        this.userAgent = header;
    }

    public void setIpAddress(String clientIP) {
        this.ipAddress = clientIP;
    }
    
    public String getDeviceName() {
        return deviceName;
    }
    
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public Date getLastActivityTime() {
        return lastActivityTime;
    }
    
    public void setLastActivityTime(Date lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public String toString() {
        return "Sessions{" +
                "id=" + id +
                ", user=" + user +
                ", refreshToken='" + refreshToken + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", token='" + token + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", location='" + location + '\'' +
                ", lastActivityTime=" + lastActivityTime +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
