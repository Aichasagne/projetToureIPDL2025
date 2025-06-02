package com.esp.esp_diplomas.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}