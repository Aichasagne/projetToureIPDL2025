package com.esp.esp_diploma_bon.service;

import com.esp.esp_diploma_bon.dto.SignatureDTO;
import com.esp.esp_diploma_bon.dto.ValidationRequestDTO;
import com.esp.esp_diploma_bon.model.*;
import com.esp.esp_diploma_bon.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {
    private final ValidationRequestRepository requestRepository;
    private final SignatureRepository signatureRepository;
    private final NotificationRepository notificationRepository;
    private final DocumentRepository documentRepository;
    private final RealTimeNotificationService realTimeNotificationService;

    public AdminService(ValidationRequestRepository requestRepository,
                        SignatureRepository signatureRepository,
                        NotificationRepository notificationRepository,
                        DocumentRepository documentRepository,
                        RealTimeNotificationService realTimeNotificationService) {
        this.requestRepository = requestRepository;
        this.signatureRepository = signatureRepository;
        this.notificationRepository = notificationRepository;
        this.documentRepository = documentRepository;
        this.realTimeNotificationService = realTimeNotificationService;
    }

    /**
     * Récupère toutes les demandes de validation
     */
    public List<ValidationRequestDTO> getValidationRequests() {
        return requestRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les détails d'une demande spécifique
     */
    public ValidationRequestDTO getRequestDetails(Long id) {
        ValidationRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        return mapToDTO(request);
    }

    /**
     * Soumet une signature avec notifications en temps réel
     */
    @Transactional
    public void submitSignature(Long requestId, String action, String comments,
                                String signatureData, String validator) {
        ValidationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        List<Signature> signatures = signatureRepository.findByValidationRequest(request);
        Signature currentSignature = signatures.stream()
                .filter(s -> s.getStatus().equals("PENDING"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No pending signature found"));

        // Mise à jour de la signature
        String newStatus = action.equals("approve") ? "COMPLETED" : "REJECTED";
        currentSignature.setStatus(newStatus);
        currentSignature.setComments(comments);
        currentSignature.setValidator(validator);
        currentSignature.setDate(LocalDateTime.now());
        signatureRepository.save(currentSignature);

        // Gestion du statut de la demande
        if (action.equals("reject")) {
            request.setStatus("REJECTED");

            // Notification de rejet
            realTimeNotificationService.notifySignatureRejected(
                    request.getStudent().getId(), currentSignature, comments
            );
        } else {
            // Notification de signature complétée
            realTimeNotificationService.notifySignatureCompleted(
                    request.getStudent().getId(), currentSignature
            );

            // Vérification si toutes les signatures sont complétées
            boolean allCompleted = signatures.stream()
                    .allMatch(s -> s.getStatus().equals("COMPLETED") || s.getId().equals(currentSignature.getId()));

            if (allCompleted) {
                request.setStatus("COMPLETED");

                // Rendre les documents disponibles
                generateAndMakeDocumentsAvailable(request);

                // Notification de completion finale
                realTimeNotificationService.notifyStatusChange(
                        request.getStudent().getId(), request, "COMPLETED", validator
                );
            } else {
                // Mise à jour du statut vers IN_PROGRESS si ce n'était pas déjà le cas
                if (!request.getStatus().equals("IN_PROGRESS")) {
                    request.setStatus("IN_PROGRESS");
                    realTimeNotificationService.notifyStatusChange(
                            request.getStudent().getId(), request, "IN_PROGRESS", validator
                    );
                }
            }
        }

        request.setLastUpdated(LocalDateTime.now());
        requestRepository.save(request);

        // Envoie une mise à jour complète du dashboard
        realTimeNotificationService.sendDashboardUpdate(request.getStudent().getId());

        // Notification par base de données (sauvegarde)
        createDatabaseNotification(request.getStudent(), action, validator, comments, currentSignature.getTitle());
    }

    /**
     * Génère et rend disponibles les documents finaux
     */
    private void generateAndMakeDocumentsAvailable(ValidationRequest request) {
        List<Document> documents = documentRepository.findByValidationRequest(request);

        if (documents.isEmpty()) {
            // Créer des documents par défaut si aucun n'existe
            createDefaultDocuments(request);
        } else {
            // Rendre disponibles les documents existants
            documents.forEach(doc -> {
                doc.setAvailable(true);
                doc.setDateGenerated(LocalDateTime.now());
                doc.setUrl(generateDocumentUrl(doc, request));
            });
            documentRepository.saveAll(documents);
        }
    }

    /**
     * Crée les documents par défaut pour une demande complétée
     */
    private void createDefaultDocuments(ValidationRequest request) {
        // Document de diplôme principal
        Document diploma = new Document();
        diploma.setValidationRequest(request);
        diploma.setTitle("Diplôme Officiel");
        diploma.setDescription("Diplôme officiel validé par toutes les autorités compétentes");
        diploma.setType("PDF");
        diploma.setDateGenerated(LocalDateTime.now());
        diploma.setAvailable(true);
        diploma.setUrl(generateDocumentUrl(diploma, request));

        // Certificat de validation
        Document certificate = new Document();
        certificate.setValidationRequest(request);
        certificate.setTitle("Certificat de Validation");
        certificate.setDescription("Certificat attestant de la validation du diplôme");
        certificate.setType("PDF");
        certificate.setDateGenerated(LocalDateTime.now());
        certificate.setAvailable(true);
        certificate.setUrl(generateDocumentUrl(certificate, request));

        // Relevé de notes officiel
        Document transcript = new Document();
        transcript.setValidationRequest(request);
        transcript.setTitle("Relevé de Notes Officiel");
        transcript.setDescription("Relevé de notes certifié conforme");
        transcript.setType("PDF");
        transcript.setDateGenerated(LocalDateTime.now());
        transcript.setAvailable(true);
        transcript.setUrl(generateDocumentUrl(transcript, request));

        documentRepository.saveAll(List.of(diploma, certificate, transcript));
    }

    /**
     * Génère l'URL de téléchargement pour un document
     */
    private String generateDocumentUrl(Document document, ValidationRequest request) {
        return String.format("/api/documents/download/%d/%d",
                request.getId(), document.getId());
    }

    /**
     * Crée une notification en base de données
     */
    private void createDatabaseNotification(User student, String action, String validator,
                                            String comments, String signatureTitle) {
        Notification notification = new Notification();
        notification.setUser(student);
        notification.setDate(LocalDateTime.now());
        notification.setRead(false);

        String message;
        if (action.equals("approve")) {
            message = String.format("✅ Signature approuvée pour '%s' par %s",
                    signatureTitle, validator);
            if (comments != null && !comments.trim().isEmpty()) {
                message += ". Commentaire: " + comments;
            }
        } else {
            message = String.format("❌ Signature rejetée pour '%s' par %s",
                    signatureTitle, validator);
            if (comments != null && !comments.trim().isEmpty()) {
                message += ". Raison: " + comments;
            }
        }

        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    /**
     * Récupère les demandes en attente de signature pour un rôle spécifique
     */
    public List<ValidationRequestDTO> getPendingRequestsForRole(String role) {
        List<Signature> pendingSignatures = signatureRepository
                .findBySignatureRoleAndStatus(role, "PENDING");

        return pendingSignatures.stream()
                .map(signature -> mapToDTO(signature.getValidationRequest()))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Récupère les statistiques des demandes
     */
    public AdminStatisticsDTO getAdminStatistics() {
        long totalRequests = requestRepository.count();
        long pendingRequests = requestRepository.findByStatus("PENDING").size();
        long inProgressRequests = requestRepository.findByStatus("IN_PROGRESS").size();
        long completedRequests = requestRepository.findByStatus("COMPLETED").size();
        long rejectedRequests = requestRepository.findByStatus("REJECTED").size();

        // Calcul du temps moyen de traitement avec fallback
        Double avgProcessingTime = calculateAverageProcessingTime();

        // Signatures en attente par département
        List<Signature> allPendingSignatures = signatureRepository
                .findBySignatureRoleAndStatus("", "PENDING").stream()
                .filter(s -> "PENDING".equals(s.getStatus()))
                .collect(Collectors.toList());

        return new AdminStatisticsDTO(
                totalRequests, pendingRequests, inProgressRequests,
                completedRequests, rejectedRequests,
                avgProcessingTime != null ? avgProcessingTime : 0.0,
                allPendingSignatures.size()
        );
    }

    /**
     * Calcule le temps moyen de traitement uniquement en Java
     * Cette approche évite les problèmes de compatibilité SQL
     */
    private Double calculateAverageProcessingTime() {
        try {
            List<ValidationRequest> completedRequests = requestRepository.findCompletedRequests();

            if (completedRequests.isEmpty()) {
                return 0.0;
            }

            double totalDays = completedRequests.stream()
                    .filter(request -> request.getSubmissionDate() != null && request.getLastUpdated() != null)
                    .mapToLong(request -> ChronoUnit.DAYS.between(
                            request.getSubmissionDate(),
                            request.getLastUpdated()))
                    .average()
                    .orElse(0.0);

            return totalDays;
        } catch (Exception e) {
            // Si le calcul échoue, retourner 0
            return 0.0;
        }
    }

    /**
     * Envoie un rappel pour les signatures en retard
     */
    @Transactional
    public void sendOverdueReminders() {
        LocalDateTime overdueThreshold = LocalDateTime.now().minusDays(7); // 7 jours de retard
        List<Signature> overdueSignatures = signatureRepository.findOverdueSignatures(overdueThreshold);

        for (Signature signature : overdueSignatures) {
            ValidationRequest request = signature.getValidationRequest();

            // Notification à l'étudiant
            String message = String.format(
                    "⚠️ La signature '%s' est en retard de plus de 7 jours. " +
                            "Veuillez contacter le service concerné.",
                    signature.getTitle()
            );

            realTimeNotificationService.sendStudentNotification(
                    request.getStudent().getId(), message, "OVERDUE_ALERT"
            );

            // Créer une notification en base
            Notification notification = new Notification();
            notification.setUser(request.getStudent());
            notification.setMessage(message);
            notification.setDate(LocalDateTime.now());
            notification.setRead(false);
            notificationRepository.save(notification);
        }
    }

    /**
     * Convertit une ValidationRequest en DTO
     */
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
        dto.setLastUpdate(request.getLastUpdated().toString());

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

    /**
     * DTO pour les statistiques admin
     */
    public static class AdminStatisticsDTO {
        private long totalRequests;
        private long pendingRequests;
        private long inProgressRequests;
        private long completedRequests;
        private long rejectedRequests;
        private double averageProcessingTime;
        private long pendingSignatures;

        public AdminStatisticsDTO(long totalRequests, long pendingRequests, long inProgressRequests,
                                  long completedRequests, long rejectedRequests,
                                  double averageProcessingTime, long pendingSignatures) {
            this.totalRequests = totalRequests;
            this.pendingRequests = pendingRequests;
            this.inProgressRequests = inProgressRequests;
            this.completedRequests = completedRequests;
            this.rejectedRequests = rejectedRequests;
            this.averageProcessingTime = averageProcessingTime;
            this.pendingSignatures = pendingSignatures;
        }

        // Getters
        public long getTotalRequests() { return totalRequests; }
        public long getPendingRequests() { return pendingRequests; }
        public long getInProgressRequests() { return inProgressRequests; }
        public long getCompletedRequests() { return completedRequests; }
        public long getRejectedRequests() { return rejectedRequests; }
        public double getAverageProcessingTime() { return averageProcessingTime; }
        public long getPendingSignatures() { return pendingSignatures; }
    }
}