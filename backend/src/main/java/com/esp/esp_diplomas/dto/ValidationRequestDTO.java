package com.esp.esp_diplomas.dto;

import lombok.Data;

import java.util.List;

@Data
public class ValidationRequestDTO {
    private Long id;
    private StudentDTO student;
    private String status;
    private String submissionDate;
    private String lastUpdate;
    private List<StepDTO> steps;

    public void setOverallStatus(String status) {
        this.status = status;
    }

    public void setProgress(int progress) {
        // La progression est actuellement stockée uniquement dans le paramètre
        // Une implémentation plus complète nécessiterait un champ 'progress' dans la classe
        if (progress < 0) {
            progress = 0;
        } else if (progress > 100) {
            progress = 100;
        }

        // Met à jour le statut en fonction de la progression
        if (progress == 0) {
            this.status = "NOT_STARTED";
        } else if (progress == 100) {
            this.status = "COMPLETED";
        } else {
            this.status = "IN_PROGRESS";
        }
    }

    public void setStartDate(String string) {
        // La date de début est actuellement stockée uniquement dans le paramètre
        // Une implémentation plus complète nécessiterait un champ 'startDate' dans la classe
        this.submissionDate = string;
    }

    public void setSignatures(List<SignatureDTO> signatures) {
        // La liste des signatures est actuellement stockée uniquement dans le paramètre
        // Une implémentation plus complète nécessiterait un champ 'signatures' dans la classe
        this.steps = signatures.stream()
                .map(signature -> new StepDTO(signature.getId(), signature.getTitle(), signature.getStatus(),
                        signature.getComments(), signature.getDate(), signature.getValidator()))
                .toList();
    }

    @Data
    public static class StudentDTO {
        private Long id;
        private String name;
        private String department;
        private String program;
        private String email;

        public StudentDTO(Long id, String name, String department, String program, String email) {
            this.id = id;
            this.name = name;
            this.department = department;
            this.program = program;
            this.email = email;
        }
    }

    @Data
    public static class StepDTO {
        private Long id;
        private String title;
        private String status;
        private String comments;
        private String date;
        private String validator;

        public StepDTO(Long id, String title, String status, String comments, String date, String validator) {
            this.id = id;
            this.title = title;
            this.status = status;
            this.comments = comments;
            this.date = date;
            this.validator = validator;
        }
    }
}