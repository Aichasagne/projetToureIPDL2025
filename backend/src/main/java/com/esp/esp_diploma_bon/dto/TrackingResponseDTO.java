package com.esp.esp_diploma_bon.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TrackingResponseDTO {
    private Long requestId;
    private String status;
    private int progressPercentage;
    private LocalDateTime lastUpdate;
    private SignatureDetails signatureDetails;
    private Timeline timeline;
    private EstimatedCompletion estimatedCompletion;
    private List<Alert> alerts;

    @Data
    public static class SignatureDetails {
        private int totalRequired;
        private int obtained;
        private int pending;
        private int rejected;
        private List<SignatureInfo> signatures;
    }

    @Data
    public static class SignatureInfo {
        private Long id;
        private String title;
        private String role;
        private String status; // PENDING, COMPLETED, REJECTED
        private String validator;
        private LocalDateTime signedDate;
        private String comments;
        private boolean isCurrent; // Indique si c'est l'étape actuelle
        private int orderIndex; // Position dans le workflow
    }

    @Data
    public static class Timeline {
        private LocalDateTime submissionDate;
        private List<TimelineEvent> events;
    }

    @Data
    public static class TimelineEvent {
        private LocalDateTime timestamp;
        private String type; // SUBMISSION, SIGNATURE, REJECTION, COMMENT, STATUS_CHANGE
        private String title;
        private String description;
        private String actor;
        private String status;
    }

    @Data
    public static class EstimatedCompletion {
        private LocalDateTime estimatedDate;
        private int remainingSteps;
        private String message;
    }

    @Data
    public static class Alert {
        private String type; // INFO, WARNING, ERROR
        private String message;
        private LocalDateTime date;
    }
}