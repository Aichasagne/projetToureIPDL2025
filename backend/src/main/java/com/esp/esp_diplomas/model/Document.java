<<<<<<< HEAD
package com.esp.esp_diplomas.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "validation_request_id")
    private ValidationRequest validationRequest;

    private String title;
    private String description;
    private String type; // e.g., PDF
    private LocalDateTime dateGenerated;
    private boolean available;
    private String url;
=======
package com.esp.esp_diplomas.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "validation_request_id")
    private ValidationRequest validationRequest;

    private String title;
    private String description;
    private String type; // e.g., PDF
    private LocalDateTime dateGenerated;
    private boolean available;
    private String url;
>>>>>>> ff72f9ff3699386ffcb2638e42f7767a6addeee9
}