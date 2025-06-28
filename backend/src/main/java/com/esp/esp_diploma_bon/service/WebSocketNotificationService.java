package com.esp.esp_diploma_bon.service;

import com.esp.esp_diploma_bon.dto.TrackingResponseDTO;
import com.esp.esp_diploma_bon.model.ValidationRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final StudentTrackingService trackingService;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate,
                                        StudentTrackingService trackingService) {
        this.messagingTemplate = messagingTemplate;
        this.trackingService = trackingService;
    }

    /**
     * Envoie une notification de mise à jour du statut à un utilisateur spécifique
     */
    public void notifyStatusUpdate(Long userId, Long requestId, String status) {
        StatusUpdateNotification notification = new StatusUpdateNotification();
        notification.setRequestId(requestId);
        notification.setStatus(status);
        notification.setTimestamp(System.currentTimeMillis());
        notification.setMessage("Le statut de votre demande a été mis à jour");

        messagingTemplate.convertAndSend(
                "/topic/user/" + userId + "/status-update",
                notification
        );
    }

    /**
     * Envoie une notification de nouvelle signature
     */
    public void notifySignatureUpdate(Long userId, Long requestId, SignatureUpdateNotification update) {
        messagingTemplate.convertAndSend(
                "/topic/user/" + userId + "/signature-update",
                update
        );
    }

    /**
     * Envoie les données de tracking complètes
     */
    public void sendTrackingUpdate(Long userId, ValidationRequest request) {
        try {
            TrackingResponseDTO trackingData = trackingService
                    .getTrackingStatusByRequestId(userId, request.getId());

            messagingTemplate.convertAndSend(
                    "/topic/user/" + userId + "/tracking-update",
                    trackingData
            );
        } catch (Exception e) {
            // Log l'erreur mais ne pas interrompre le flux
            e.printStackTrace();
        }
    }

    /**
     * Notification de document disponible
     */
    public void notifyDocumentAvailable(Long userId, DocumentNotification notification) {
        messagingTemplate.convertAndSend(
                "/topic/user/" + userId + "/document-available",
                notification
        );
    }

    // Classes internes pour les notifications

    public static class StatusUpdateNotification {
        private Long requestId;
        private String status;
        private String message;
        private Long timestamp;

        // Getters et setters
        public Long getRequestId() { return requestId; }
        public void setRequestId(Long requestId) { this.requestId = requestId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    }

    public static class SignatureUpdateNotification {
        private Long requestId;
        private Long signatureId;
        private String signatureTitle;
        private String status;
        private String validator;
        private String comments;
        private Long timestamp;

        // Getters et setters
        public Long getRequestId() { return requestId; }
        public void setRequestId(Long requestId) { this.requestId = requestId; }
        public Long getSignatureId() { return signatureId; }
        public void setSignatureId(Long signatureId) { this.signatureId = signatureId; }
        public String getSignatureTitle() { return signatureTitle; }
        public void setSignatureTitle(String signatureTitle) { this.signatureTitle = signatureTitle; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getValidator() { return validator; }
        public void setValidator(String validator) { this.validator = validator; }
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    }

    public static class DocumentNotification {
        private Long documentId;
        private String documentTitle;
        private String documentType;
        private String message;
        private Long timestamp;

        // Getters et setters
        public Long getDocumentId() { return documentId; }
        public void setDocumentId(Long documentId) { this.documentId = documentId; }
        public String getDocumentTitle() { return documentTitle; }
        public void setDocumentTitle(String documentTitle) { this.documentTitle = documentTitle; }
        public String getDocumentType() { return documentType; }
        public void setDocumentType(String documentType) { this.documentType = documentType; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    }
}