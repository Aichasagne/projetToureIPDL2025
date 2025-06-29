package com.esp.esp_diploma_bon.service;

import com.esp.esp_diploma_bon.dto.SignatureStatusDTO;
import com.esp.esp_diploma_bon.model.*;
import com.esp.esp_diploma_bon.repository.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class RealTimeNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ValidationRequestRepository validationRequestRepository;
    private final SignatureRepository signatureRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public RealTimeNotificationService(SimpMessagingTemplate messagingTemplate,
                                       ValidationRequestRepository validationRequestRepository,
                                       SignatureRepository signatureRepository,
                                       NotificationRepository notificationRepository,
                                       UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.validationRequestRepository = validationRequestRepository;
        this.signatureRepository = signatureRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Envoie une notification en temps réel à un étudiant spécifique
     */
    @Async
    public void sendStudentNotification(Long studentId, String message, String type) {
        NotificationMessage notification = new NotificationMessage(
                message, type, LocalDateTime.now().toString(), false
        );

        // Envoie via WebSocket
        messagingTemplate.convertAndSend(
                "/topic/student/" + studentId + "/notifications",
                notification
        );

        // Sauvegarde en base de données
        saveNotificationToDatabase(studentId, message);
    }

    /**
     * Notifie un étudiant du changement de statut de sa demande
     */
    @Async
    public void notifyStatusChange(Long studentId, ValidationRequest request, String newStatus, String changedBy) {
        String message = createStatusChangeMessage(newStatus, changedBy);

        // Notification temps réel
        sendStudentNotification(studentId, message, "STATUS_CHANGE");

        // Mise à jour des données du dashboard
        sendDashboardUpdate(studentId);
    }

    /**
     * Notifie un étudiant qu'une signature a été complétée
     */
    @Async
    public void notifySignatureCompleted(Long studentId, Signature signature) {
        String message = String.format(
                "✅ Signature obtenue: %s par %s",
                signature.getTitle(),
                signature.getValidator()
        );

        sendStudentNotification(studentId, message, "SIGNATURE_COMPLETED");
        sendProgressUpdate(studentId);
    }

    /**
     * Notifie un étudiant qu'une signature a été rejetée
     */
    @Async
    public void notifySignatureRejected(Long studentId, Signature signature, String reason) {
        String message = String.format(
                "❌ Signature rejetée: %s - Raison: %s",
                signature.getTitle(),
                reason
        );

        sendStudentNotification(studentId, message, "SIGNATURE_REJECTED");
        sendProgressUpdate(studentId);
    }

    /**
     * Envoie une mise à jour complète du dashboard
     */
    @Async
    public void sendDashboardUpdate(Long studentId) {
        User student = userRepository.findById(studentId).orElse(null);
        if (student == null) return;

        try {
            // Récupération des données actuelles
            ValidationRequest latestRequest = validationRequestRepository
                    .findLatestByStudent(student).orElse(null);

            if (latestRequest != null) {
                List<Signature> signatures = signatureRepository.findByValidationRequest(latestRequest);

                DashboardUpdateMessage update = new DashboardUpdateMessage(
                        latestRequest.getStatus(),
                        calculateProgress(signatures),
                        getCompletedSignatureCount(signatures),
                        signatures.size(),
                        LocalDateTime.now().toString()
                );

                messagingTemplate.convertAndSend(
                        "/topic/student/" + studentId + "/dashboard",
                        update
                );
            }
        } catch (Exception e) {
            // Log l'erreur mais ne pas faire échouer le processus principal
            System.err.println("Erreur lors de l'envoi de la mise à jour dashboard: " + e.getMessage());
        }
    }

    /**
     * Envoie une mise à jour de progression
     */
    @Async
    public void sendProgressUpdate(Long studentId) {
        User student = userRepository.findById(studentId).orElse(null);
        if (student == null) return;

        ValidationRequest latestRequest = validationRequestRepository
                .findLatestByStudent(student).orElse(null);

        if (latestRequest != null) {
            List<Signature> signatures = signatureRepository.findByValidationRequest(latestRequest);
            int progress = calculateProgress(signatures);

            ProgressUpdateMessage progressUpdate = new ProgressUpdateMessage(
                    progress,
                    getCompletedSignatureCount(signatures),
                    signatures.size(),
                    getCurrentStepTitle(signatures),
                    LocalDateTime.now().toString()
            );

            messagingTemplate.convertAndSend(
                    "/topic/student/" + studentId + "/progress",
                    progressUpdate
            );
        }
    }

    /**
     * Vérifie périodiquement les signatures en retard
     */
    @Scheduled(fixedRate = 3600000) // Toutes les heures
    public void checkOverdueSignatures() {
        LocalDateTime now = LocalDateTime.now();
        List<Signature> overdueSignatures = signatureRepository.findOverdueSignatures(now);

        Map<Long, List<Signature>> signaturesByStudent = overdueSignatures.stream()
                .collect(Collectors.groupingBy(s -> s.getValidationRequest().getStudent().getId()));

        signaturesByStudent.forEach((studentId, signatures) -> {
            String message = String.format(
                    "⚠️ %d signature(s) en retard. Veuillez contacter les services concernés.",
                    signatures.size()
            );
            sendStudentNotification(studentId, message, "OVERDUE_WARNING");
        });
    }

    /**
     * Envoie un rappel quotidien aux étudiants avec des demandes en cours
     */
    @Scheduled(cron = "0 0 9 * * *") // Tous les jours à 9h
    public void sendDailyReminders() {
        List<ValidationRequest> activeRequests = validationRequestRepository.findActiveRequests();

        for (ValidationRequest request : activeRequests) {
            Long studentId = request.getStudent().getId();
            List<Signature> signatures = signatureRepository.findByValidationRequest(request);
            long pendingCount = signatures.stream()
                    .filter(s -> "PENDING".equals(s.getStatus()))
                    .count();

            if (pendingCount > 0) {
                String message = String.format(
                        "📋 Rappel: Votre demande de diplôme a %d signature(s) en attente.",
                        pendingCount
                );
                sendStudentNotification(studentId, message, "DAILY_REMINDER");
            }
        }
    }

    // Méthodes utilitaires privées

    private void saveNotificationToDatabase(Long studentId, String message) {
        User student = userRepository.findById(studentId).orElse(null);
        if (student != null) {
            Notification notification = new Notification();
            notification.setUser(student);
            notification.setMessage(message);
            notification.setDate(LocalDateTime.now());
            notification.setRead(false);
            notificationRepository.save(notification);
        }
    }

    private String createStatusChangeMessage(String newStatus, String changedBy) {
        switch (newStatus) {
            case "COMPLETED":
                return "🎉 Félicitations! Votre demande de diplôme a été validée et est maintenant complète.";
            case "REJECTED":
                return "❌ Votre demande de diplôme a été rejetée par " + changedBy + ". Veuillez consulter les commentaires.";
            case "IN_PROGRESS":
                return "🔄 Votre demande de diplôme est maintenant en cours de traitement.";
            default:
                return "ℹ️ Le statut de votre demande a été mis à jour: " + newStatus;
        }
    }

    private int calculateProgress(List<Signature> signatures) {
        if (signatures.isEmpty()) return 0;
        long completed = signatures.stream()
                .filter(s -> "COMPLETED".equals(s.getStatus()))
                .count();
        return (int) ((completed * 100) / signatures.size());
    }

    private int getCompletedSignatureCount(List<Signature> signatures) {
        return (int) signatures.stream()
                .filter(s -> "COMPLETED".equals(s.getStatus()))
                .count();
    }

    private String getCurrentStepTitle(List<Signature> signatures) {
        return signatures.stream()
                .filter(s -> "PENDING".equals(s.getStatus()))
                .findFirst()
                .map(Signature::getTitle)
                .orElse("Toutes les signatures obtenues");
    }

    // Classes de messages pour WebSocket

    public static class NotificationMessage {
        private String message;
        private String type;
        private String timestamp;
        private boolean read;

        public NotificationMessage(String message, String type, String timestamp, boolean read) {
            this.message = message;
            this.type = type;
            this.timestamp = timestamp;
            this.read = read;
        }

        // Getters
        public String getMessage() { return message; }
        public String getType() { return type; }
        public String getTimestamp() { return timestamp; }
        public boolean isRead() { return read; }
    }

    public static class DashboardUpdateMessage {
        private String status;
        private int progress;
        private int completedSteps;
        private int totalSteps;
        private String lastUpdate;

        public DashboardUpdateMessage(String status, int progress, int completedSteps,
                                      int totalSteps, String lastUpdate) {
            this.status = status;
            this.progress = progress;
            this.completedSteps = completedSteps;
            this.totalSteps = totalSteps;
            this.lastUpdate = lastUpdate;
        }

        // Getters
        public String getStatus() { return status; }
        public int getProgress() { return progress; }
        public int getCompletedSteps() { return completedSteps; }
        public int getTotalSteps() { return totalSteps; }
        public String getLastUpdate() { return lastUpdate; }
    }

    public static class ProgressUpdateMessage {
        private int percentage;
        private int completedSteps;
        private int totalSteps;
        private String currentStep;
        private String timestamp;

        public ProgressUpdateMessage(int percentage, int completedSteps, int totalSteps,
                                     String currentStep, String timestamp) {
            this.percentage = percentage;
            this.completedSteps = completedSteps;
            this.totalSteps = totalSteps;
            this.currentStep = currentStep;
            this.timestamp = timestamp;
        }

        // Getters
        public int getPercentage() { return percentage; }
        public int getCompletedSteps() { return completedSteps; }
        public int getTotalSteps() { return totalSteps; }
        public String getCurrentStep() { return currentStep; }
        public String getTimestamp() { return timestamp; }
    }
}