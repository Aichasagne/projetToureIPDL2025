<<<<<<< HEAD
package com.esp.esp_diplomas.model;

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
    private User student;

    private String status; // PENDING, COMPLETED, REJECTED
    private LocalDateTime submissionDate;
    private LocalDateTime lastUpdated;

    @OneToMany(mappedBy = "validationRequest", cascade = CascadeType.ALL)
    private List<Signature> signatures;

    @OneToMany(mappedBy = "validationRequest", cascade = CascadeType.ALL)
    private List<Document> documents;
=======
package com.esp.esp_diplomas.model;

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
    private User student;

    private String status; // PENDING, COMPLETED, REJECTED
    private LocalDateTime submissionDate;
    private LocalDateTime lastUpdated;

    @OneToMany(mappedBy = "validationRequest", cascade = CascadeType.ALL)
    private List<Signature> signatures;

    @OneToMany(mappedBy = "validationRequest", cascade = CascadeType.ALL)
    private List<Document> documents;
>>>>>>> ff72f9ff3699386ffcb2638e42f7767a6addeee9
}