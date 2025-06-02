package com.esp.esp_diplomas.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private Long id;
    private String name;
    private String role;

    public LoginResponse(String token, Long id, String name, String role) {
        this.token = token;
        this.id = id;
        this.name = name;
        this.role = role;
    }
}