package com.esp.esp_diploma_bon.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class ValidationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private com.esp.esp_diploma_bon.model.User student;

    private String status; // PENDING, COMPLETED, REJECTED
    private LocalDateTime submissionDate;
    private LocalDateTime lastUpdated;

    @OneToMany(mappedBy = "validationRequest", cascade = CascadeType.ALL)
    private List<Signature> signatures;

    @OneToMany(mappedBy = "validationRequest", cascade = CascadeType.ALL)
    private List<Document> documents;
}