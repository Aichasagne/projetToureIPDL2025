package com.esp.esp_diploma_bon.dto;

import lombok.Data;
import lombok.Getter;

@Data
public class SignatureDTO {
    private final String validator;
    @Getter
    private final String comments;
    private Long id;
    private String title;
    private String status;
    private String date;

    public SignatureDTO(String validator, String comments, Long id, String title, String status) {
        this.validator = validator;
        this.comments = comments;
        this.id = id;
        this.title = title;
        this.status = status;
    }

    public SignatureDTO(String validator, String comments, Long id, String title, String status, String date) {
        this.validator = validator;
        this.comments = comments;
        this.id = id;
        this.title = title;
        this.status = status;
        this.date = date;
    }
    public SignatureDTO(Long id, String title, String status, String validator, String comments, String date) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.validator = validator;
        this.comments = comments;
        this.date = date;
    }
    public SignatureDTO(Long id, String title, String status) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.validator = null;
        this.comments = null;
    }

    public SignatureDTO(Long id, String title, String status, String string) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.validator = null;
        this.comments = null;
    }
}