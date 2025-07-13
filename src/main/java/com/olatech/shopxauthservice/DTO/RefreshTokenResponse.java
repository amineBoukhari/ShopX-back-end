package com.olatech.shopxauthservice.DTO;

import com.olatech.shopxauthservice.Service.JWTService;

import java.util.Date;

public class RefreshTokenResponse {

    public String token;
    public Date expiry ;

    private JWTService jwtService = new JWTService();

    public RefreshTokenResponse() {

    }


    public RefreshTokenResponse(String token) {
        this.token = token;
        this.expiry = jwtService.extractExpiration(token);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpiry() {
        return expiry;
    }

    public void setExpiry(Date expiry) {
        this.expiry = expiry;
    }

    public void setAccessToken(String newAccessToken) {
        this.token = newAccessToken;
    }

    public void setExpiresIn(int i) {
        this.expiry = new Date(System.currentTimeMillis() + i * 1000);
    }
}
