package com.esp.esp_diploma_bon.controller;

import com.esp.esp_diploma_bon.dto.DashboardData;
import com.esp.esp_diploma_bon.dto.DocumentDTO;
import com.esp.esp_diploma_bon.dto.SignatureStatusDTO;
import com.esp.esp_diploma_bon.dto.ValidationRequestDTO;
import com.esp.esp_diploma_bon.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * Récupère les données du dashboard étudiant avec état des signatures
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardData> getDashboardData(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Récupération de l'ID utilisateur depuis le token JWT
            Long userId = studentService.getUserIdFromUsername(userDetails.getUsername());
            DashboardData dashboardData = studentService.getDashboardData(userId);
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupère le statut détaillé de la validation
     */
    @GetMapping("/validation-status")
    public ResponseEntity<ValidationRequestDTO> getValidationStatus(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = studentService.getUserIdFromUsername(userDetails.getUsername());
            ValidationRequestDTO status = studentService.getValidationStatus(userId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupère l'état détaillé de toutes les signatures
     */
    @GetMapping("/signatures-status")
    public ResponseEntity<List<SignatureStatusDTO>> getSignaturesStatus(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = studentService.getUserIdFromUsername(userDetails.getUsername());
            List<SignatureStatusDTO> signaturesStatus = studentService.getSignaturesStatus(userId);
            return ResponseEntity.ok(signaturesStatus);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupère les signatures en attente
     */
    @GetMapping("/pending-signatures")
    public ResponseEntity<List<SignatureStatusDTO>> getPendingSignatures(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = studentService.getUserIdFromUsername(userDetails.getUsername());
            List<SignatureStatusDTO> pendingSignatures = studentService.getPendingSignatures(userId);
            return ResponseEntity.ok(pendingSignatures);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupère les signatures complétées
     */
    @GetMapping("/completed-signatures")
    public ResponseEntity<List<SignatureStatusDTO>> getCompletedSignatures(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = studentService.getUserIdFromUsername(userDetails.getUsername());
            List<SignatureStatusDTO> completedSignatures = studentService.getCompletedSignatures(userId);
            return ResponseEntity.ok(completedSignatures);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupère les documents disponibles
     */
    @GetMapping("/documents")
    public ResponseEntity<List<DocumentDTO>> getAvailableDocuments(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = studentService.getUserIdFromUsername(userDetails.getUsername());
            List<DocumentDTO> documents = studentService.getAvailableDocuments(userId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupère le pourcentage de progression
     */
    @GetMapping("/progress")
    public ResponseEntity<ProgressDTO> getProgress(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = studentService.getUserIdFromUsername(userDetails.getUsername());
            ProgressDTO progress = studentService.getProgressData(userId);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Marque une notification comme lue
     */
    @PutMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = studentService.getUserIdFromUsername(userDetails.getUsername());
            studentService.markNotificationAsRead(notificationId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * DTO pour les données de progression
     */
    public static class ProgressDTO {
        private int percentage;
        private int completedSteps;
        private int totalSteps;
        private String currentStep;
        private String status;
        private String estimatedCompletionDate;

        // Constructeurs
        public ProgressDTO() {}

        public ProgressDTO(int percentage, int completedSteps, int totalSteps,
                           String currentStep, String status, String estimatedCompletionDate) {
            this.percentage = percentage;
            this.completedSteps = completedSteps;
            this.totalSteps = totalSteps;
            this.currentStep = currentStep;
            this.status = status;
            this.estimatedCompletionDate = estimatedCompletionDate;
        }

        // Getters et Setters
        public int getPercentage() { return percentage; }
        public void setPercentage(int percentage) { this.percentage = percentage; }

        public int getCompletedSteps() { return completedSteps; }
        public void setCompletedSteps(int completedSteps) { this.completedSteps = completedSteps; }

        public int getTotalSteps() { return totalSteps; }
        public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }

        public String getCurrentStep() { return currentStep; }
        public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getEstimatedCompletionDate() { return estimatedCompletionDate; }
        public void setEstimatedCompletionDate(String estimatedCompletionDate) {
            this.estimatedCompletionDate = estimatedCompletionDate;
        }
    }
}