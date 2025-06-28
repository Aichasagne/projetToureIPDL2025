package com.esp.esp_diploma_bon.service;

import com.esp.esp_diploma_bon.dto.SignatureDTO;
import com.esp.esp_diploma_bon.dto.ValidationRequestDTO;
import com.esp.esp_diploma_bon.model.*;
import com.esp.esp_diploma_bon.repository.*;
import com.esp.esp_diploma_bon.service.WebSocketNotificationService.SignatureUpdateNotification;
import com.esp.esp_diploma_bon.service.WebSocketNotificationService.DocumentNotification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {
    private final ValidationRequestRepository requestRepository;
    private final SignatureRepository signatureRepository;
    private final NotificationRepository notificationRepository;
    private final WebSocketNotificationService webSocketService;

    public AdminService(ValidationRequestRepository requestRepository,
                        SignatureRepository signatureRepository,
                        NotificationRepository notificationRepository,
                        WebSocketNotificationService webSocketService) {
        this.requestRepository = requestRepository;
        this.signatureRepository = signatureRepository;
        this.notificationRepository = notificationRepository;
        this.webSocketService = webSocketService;
    }

    public List<ValidationRequestDTO> getValidationRequests() {
        return requestRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ValidationRequestDTO getRequestDetails(Long id) {
        ValidationRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        return mapToDTO(request);
    }

    public void submitSignature(Long requestId, String action, String comments, String signatureData, String validator) {
        ValidationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        List<Signature> signatures = signatureRepository.findByValidationRequest(request);
        Signature currentSignature = signatures.stream()
                .filter(s -> s.getStatus().equals("PENDING"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No pending signature found"));

        // Mettre à jour la signature
        currentSignature.setStatus(action.equals("approve") ? "COMPLETED" : "REJECTED");
        currentSignature.setComments(comments);
        currentSignature.setValidator(validator);
        currentSignature.setDate(LocalDateTime.now());
        signatureRepository.save(currentSignature);

        // Préparer la notification WebSocket pour la signature
        SignatureUpdateNotification signatureNotification = new SignatureUpdateNotification();
        signatureNotification.setRequestId(requestId);
        signatureNotification.setSignatureId(currentSignature.getId());
        signatureNotification.setSignatureTitle(currentSignature.getTitle());
        signatureNotification.setStatus(currentSignature.getStatus());
        signatureNotification.setValidator(validator);
        signatureNotification.setComments(comments);
        signatureNotification.setTimestamp(System.currentTimeMillis());

        // Envoyer la notification de signature
        webSocketService.notifySignatureUpdate(
                request.getStudent().getId(),
                requestId,
                signatureNotification
        );

        // Mettre à jour le statut de la demande
        if (action.equals("reject")) {
            request.setStatus("REJECTED");
        } else {
            boolean allCompleted = signatures.stream()
                    .allMatch(s -> s.getStatus().equals("COMPLETED"));

            if (allCompleted) {
                request.setStatus("COMPLETED");

                // Rendre les documents disponibles
                request.getDocuments().forEach(d -> {
                    d.setAvailable(true);
                    d.setDateGenerated(LocalDateTime.now());

                    // Notifier pour chaque document disponible
                    DocumentNotification docNotification = new DocumentNotification();
                    docNotification.setDocumentId(d.getId());
                    docNotification.setDocumentTitle(d.getTitle());
                    docNotification.setDocumentType(d.getType());
                    docNotification.setMessage("Le document " + d.getTitle() + " est maintenant disponible");
                    docNotification.setTimestamp(System.currentTimeMillis());

                    webSocketService.notifyDocumentAvailable(
                            request.getStudent().getId(),
                            docNotification
                    );
                });
            }
        }

        request.setLastUpdated(LocalDateTime.now());
        requestRepository.save(request);

        // Notification de mise à jour du statut
        webSocketService.notifyStatusUpdate(
                request.getStudent().getId(),
                requestId,
                request.getStatus()
        );

        // Envoyer les données de tracking complètes
        webSocketService.sendTrackingUpdate(
                request.getStudent().getId(),
                request
        );

        // Créer une notification persistante
        Notification notification = new Notification();
        notification.setUser(request.getStudent());
        notification.setMessage("Votre demande a été " +
                (action.equals("approve") ? "approuvée" : "rejetée") +
                " par " + validator + ": " + comments);
        notification.setDate(LocalDateTime.now());
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    private ValidationRequestDTO mapToDTO(ValidationRequest request) {
        ValidationRequestDTO dto = new ValidationRequestDTO();
        dto.setId(request.getId());
        dto.setStudent(new ValidationRequestDTO.StudentDTO(
                request.getStudent().getId(),
                request.getStudent().getName(),
                request.getStudent().getDepartment(),
                request.getStudent().getProgram(),
                request.getStudent().getEmail()
        ));
        dto.setStatus(request.getStatus());
        dto.setSubmissionDate(request.getSubmissionDate().toString());
        dto.setLastUpdate(request.getLastUpdated() != null ?
                request.getLastUpdated().toString() : null);

        List<SignatureDTO> signatures = signatureRepository.findByValidationRequest(request)
                .stream()
                .map(s -> new SignatureDTO(
                        s.getId(),
                        s.getTitle(),
                        s.getStatus(),
                        s.getValidator(),
                        s.getComments(),
                        s.getDate() != null ? s.getDate().toString() : null
                ))
                .collect(Collectors.toList());
        dto.setSignatures(signatures);
        return dto;
    }
}