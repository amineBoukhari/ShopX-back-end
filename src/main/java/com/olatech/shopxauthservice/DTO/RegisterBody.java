package com.olatech.shopxauthservice.DTO;

public class RegisterBody {
    public String username = "";
    public String password = "";
    public String email = "";
    public String phone = "";

    public RegisterBody(String username, String password, String email, String phone, String role){
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
    }
}
