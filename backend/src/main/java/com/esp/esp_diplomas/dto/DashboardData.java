package com.esp.esp_diplomas.dto;

import lombok.Data;

import java.util.List;

@Data
public class DashboardData {
    private StudentDTO student;
    private ValidationStatusDTO validationStatus;
    private List<SignatureDTO> pendingSignatures;
    private List<SignatureDTO> completedSignatures;
    private List<NotificationDTO> notifications;

    @Data
    public static class StudentDTO {
        private String name;
        private String matricule;
        private String department;
        private String program;
        private String graduationYear;

        public StudentDTO(String name, String matricule, String department, String program, String graduationYear) {
            this.name = name;
            this.matricule = matricule;
            this.department = department;
            this.program = program;
            this.graduationYear = graduationYear;
        }
    }

    @Data
    public static class ValidationStatusDTO {
        private String status;
        private int progress;
        private int completedSteps;
        private int totalSteps;
        private String latestUpdate;

        public ValidationStatusDTO(String status, int progress, int completedSteps, int totalSteps, String latestUpdate) {
            this.status = status;
            this.progress = progress;
            this.completedSteps = completedSteps;
            this.totalSteps = totalSteps;
            this.latestUpdate = latestUpdate;
        }
    }

    @Data
    public static class NotificationDTO {
        private Long id;
        private String message;
        private String date;
        private boolean read;

        public NotificationDTO(Long id, String message, String date, boolean read) {
            this.id = id;
            this.message = message;
            this.date = date;
            this.read = read;
        }
    }
}