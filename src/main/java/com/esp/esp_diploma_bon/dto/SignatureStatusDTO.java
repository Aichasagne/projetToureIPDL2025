package com.esp.esp_diploma_bon.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignatureStatusDTO {
    private Long id;
    private String title;
    private String description;
    private String signatureRole;
    private String status; // PENDING, COMPLETED, REJECTED, NOT_STARTED
    private String validator;
    private String comments;
    private String dateCreated;
    private String dateCompleted;
    private String dateExpected;
    private int orderIndex;
    private boolean isRequired;
    private boolean isCurrentStep;
    private String department;
    private String contactInfo;
    private int estimatedProcessingDays;

    // Constructeur simplifié pour les cas basiques
    public SignatureStatusDTO(Long id, String title, String status) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.isRequired = true;
        this.estimatedProcessingDays = 3;
    }

    // Constructeur avec informations principales
    public SignatureStatusDTO(Long id, String title, String signatureRole, String status,
                              String validator, String comments, String dateCompleted) {
        this.id = id;
        this.title = title;
        this.signatureRole = signatureRole;
        this.status = status;
        this.validator = validator;
        this.comments = comments;
        this.dateCompleted = dateCompleted;
        this.isRequired = true;
        this.estimatedProcessingDays = 3;
    }

    /**
     * Vérifie si la signature est en retard
     */
    public boolean isOverdue() {
        if (dateExpected == null || !status.equals("PENDING")) {
            return false;
        }
        try {
            LocalDateTime expected = LocalDateTime.parse(dateExpected);
            return LocalDateTime.now().isAfter(expected);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Calcule le nombre de jours restants pour la signature
     */
    public long getDaysRemaining() {
        if (dateExpected == null || !status.equals("PENDING")) {
            return 0;
        }
        try {
            LocalDateTime expected = LocalDateTime.parse(dateExpected);
            LocalDateTime now = LocalDateTime.now();
            return java.time.Duration.between(now, expected).toDays();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Retourne une description du statut pour l'affichage
     */
    public String getStatusDisplayText() {
        switch (status) {
            case "PENDING":
                return "En attente de signature";
            case "COMPLETED":
                return "Signature obtenue";
            case "REJECTED":
                return "Demande rejetée";
            case "NOT_STARTED":
                return "Pas encore démarrée";
            default:
                return "Statut inconnu";
        }
    }

    /**
     * Retourne la couleur associée au statut
     */
    public String getStatusColor() {
        switch (status) {
            case "PENDING":
                return isOverdue() ? "#ff6b6b" : "#ffd93d";
            case "COMPLETED":
                return "#51cf66";
            case "REJECTED":
                return "#ff6b6b";
            case "NOT_STARTED":
                return "#868e96";
            default:
                return "#868e96";
        }
    }

    /**
     * Retourne l'icône associée au statut
     */
    public String getStatusIcon() {
        switch (status) {
            case "PENDING":
                return "clock";
            case "COMPLETED":
                return "check-circle";
            case "REJECTED":
                return "x-circle";
            case "NOT_STARTED":
                return "circle";
            default:
                return "help-circle";
        }
    }
}