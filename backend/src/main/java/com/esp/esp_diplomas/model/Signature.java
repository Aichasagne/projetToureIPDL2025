package com.esp.esp_diplomas.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Data
public class Signature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "validation_request_id")
    private ValidationRequest validationRequest;

    @Getter
    private String signatureRole;
    private String title; // e.g., "Service Scolarité"
    private String status; // PENDING, COMPLETED, REJECTED
    private String comments;
    private String validator;
    private LocalDateTime date;

    public String getRole() {
        return this.signatureRole;
    }
}