package com.olatech.shopxauthservice.DTO;

public class LoginResponse {

    public LoginResponse(String accessToken, String refreshToken) {
        this.refreshToken = new RefreshTokenResponse(refreshToken);
        this.accessToken = new RefreshTokenResponse(accessToken);
    }

    public RefreshTokenResponse refreshToken;

    public RefreshTokenResponse accessToken ;
}
