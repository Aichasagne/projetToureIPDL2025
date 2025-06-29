package com.esp.esp_diploma_bon.service;

import com.esp.esp_diploma_bon.controller.StudentController;
import com.esp.esp_diploma_bon.dto.*;
import com.esp.esp_diploma_bon.exception.ResourceNotFoundException;
import com.esp.esp_diploma_bon.model.*;
import com.esp.esp_diploma_bon.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
public class StudentService {
    private final UserRepository userRepository;
    private final ValidationRequestRepository requestRepository;
    private final SignatureRepository signatureRepository;
    private final DocumentRepository documentRepository;
    private final NotificationRepository notificationRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public StudentService(UserRepository userRepository, ValidationRequestRepository requestRepository,
                          SignatureRepository signatureRepository, DocumentRepository documentRepository,
                          NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.signatureRepository = signatureRepository;
        this.documentRepository = documentRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Récupère l'ID utilisateur à partir du nom d'utilisateur
     */
    public Long getUserIdFromUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "nom d'utilisateur", username));
    }

    /**
     * Récupère les données complètes du dashboard
     */
    public DashboardData getDashboardData(Long userId) {
        User user = getUserById(userId);
        Optional<ValidationRequest> latestRequestOpt = getLatestValidationRequest(user);

        DashboardData data = new DashboardData();
        data.setStudent(new DashboardData.StudentDTO(
                user.getName(), user.getMatricule(), user.getDepartment(),
                user.getProgram(), user.getGraduationYear()
        ));

        if (latestRequestOpt.isPresent()) {
            ValidationRequest latestRequest = latestRequestOpt.get();
            List<Signature> signatures = signatureRepository.findByValidationRequest(latestRequest);

            // Calcul des données de progression
            int completedSteps = (int) signatures.stream()
                    .filter(s -> "COMPLETED".equals(s.getStatus()))
                    .count();
            int totalSteps = signatures.size();
            int progress = totalSteps > 0 ? (completedSteps * 100 / totalSteps) : 0;

            data.setValidationStatus(new DashboardData.ValidationStatusDTO(
                    latestRequest.getStatus(),
                    progress,
                    completedSteps,
                    totalSteps,
                    formatDateTime(latestRequest.getLastUpdated())
            ));

            // Signatures en attente
            data.setPendingSignatures(signatures.stream()
                    .filter(s -> "PENDING".equals(s.getStatus()))
                    .map(this::mapToSignatureDTO)
                    .collect(Collectors.toList()));

            // Signatures complétées
            data.setCompletedSignatures(signatures.stream()
                    .filter(s -> "COMPLETED".equals(s.getStatus()))
                    .map(this::mapToSignatureDTO)
                    .collect(Collectors.toList()));
        }

        // Notifications
        data.setNotifications(notificationRepository.findByUser(user).stream()
                .map(this::mapToNotificationDTO)
                .collect(Collectors.toList()));

        return data;
    }

    /**
     * Récupère le statut détaillé de la validation
     */
    public ValidationRequestDTO getValidationStatus(Long userId) {
        User user = getUserById(userId);
        Optional<ValidationRequest> requestOpt = getLatestValidationRequest(user);

        if (requestOpt.isEmpty()) {
            return createEmptyValidationRequest();
        }

        ValidationRequest request = requestOpt.get();
        ValidationRequestDTO dto = new ValidationRequestDTO();
        dto.setId(request.getId());
        dto.setStudent(new ValidationRequestDTO.StudentDTO(
                user.getId(), user.getName(), user.getDepartment(),
                user.getProgram(), user.getEmail()
        ));
        dto.setStatus(request.getStatus());
        dto.setSubmissionDate(formatDateTime(request.getSubmissionDate()));
        dto.setLastUpdate(formatDateTime(request.getLastUpdated()));

        List<Signature> signatures = signatureRepository.findByValidationRequest(request);
        dto.setSteps(signatures.stream()
                .map(this::mapToStepDTO)
                .collect(Collectors.toList()));

        return dto;
    }

    /**
     * Récupère l'état de toutes les signatures
     */
    public List<SignatureStatusDTO> getSignaturesStatus(Long userId) {
        User user = getUserById(userId);
        Optional<ValidationRequest> requestOpt = getLatestValidationRequest(user);

        if (requestOpt.isEmpty()) {
            return List.of();
        }

        List<Signature> signatures = signatureRepository.findByValidationRequest(requestOpt.get());
        return IntStream.range(0, signatures.size())
                .mapToObj(i -> mapToSignatureStatusDTO(signatures.get(i), i, signatures.size()))
                .collect(Collectors.toList());
    }

    /**
     * Récupère les signatures en attente
     */
    public List<SignatureStatusDTO> getPendingSignatures(Long userId) {
        return getSignaturesStatus(userId).stream()
                .filter(signature -> "PENDING".equals(signature.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Récupère les signatures complétées
     */
    public List<SignatureStatusDTO> getCompletedSignatures(Long userId) {
        return getSignaturesStatus(userId).stream()
                .filter(signature -> "COMPLETED".equals(signature.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Récupère les documents disponibles
     */
    public List<DocumentDTO> getAvailableDocuments(Long userId) {
        User user = getUserById(userId);
        Optional<ValidationRequest> requestOpt = getLatestValidationRequest(user);

        if (requestOpt.isEmpty()) {
            return List.of();
        }

        return documentRepository.findByValidationRequest(requestOpt.get()).stream()
                .filter(Document::isAvailable)
                .map(this::mapToDocumentDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les données de progression
     */
    public StudentController.ProgressDTO getProgressData(Long userId) {
        User user = getUserById(userId);
        Optional<ValidationRequest> requestOpt = getLatestValidationRequest(user);

        if (requestOpt.isEmpty()) {
            return new StudentController.ProgressDTO(0, 0, 0, "Aucune demande", "NOT_STARTED", null);
        }

        ValidationRequest request = requestOpt.get();
        List<Signature> signatures = signatureRepository.findByValidationRequest(request);

        int completedSteps = (int) signatures.stream()
                .filter(s -> "COMPLETED".equals(s.getStatus()))
                .count();
        int totalSteps = signatures.size();
        int percentage = totalSteps > 0 ? (completedSteps * 100 / totalSteps) : 0;

        // Prochaine étape
        String currentStep = signatures.stream()
                .filter(s -> "PENDING".equals(s.getStatus()))
                .findFirst()
                .map(Signature::getTitle)
                .orElse("Toutes les signatures obtenues");

        // Estimation de fin (basée sur 3 jours par signature restante)
        long pendingSignatures = signatures.stream()
                .filter(s -> "PENDING".equals(s.getStatus()))
                .count();
        String estimatedCompletion = null;
        if (pendingSignatures > 0) {
            LocalDateTime estimated = LocalDateTime.now().plusDays(pendingSignatures * 3);
            estimatedCompletion = formatDateTime(estimated);
        }

        return new StudentController.ProgressDTO(
                percentage, completedSteps, totalSteps, currentStep,
                request.getStatus(), estimatedCompletion
        );
    }

    /**
     * Marque une notification comme lue
     */
    @Transactional
    public void markNotificationAsRead(Long notificationId, Long userId) {
        User user = getUserById(userId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Accès non autorisé à cette notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    // Méthodes utilitaires privées

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));
    }

    private Optional<ValidationRequest> getLatestValidationRequest(User user) {
        List<ValidationRequest> requests = requestRepository.findByStudent(user);
        return requests.isEmpty() ? Optional.empty() : Optional.of(requests.get(0));
    }

    private ValidationRequestDTO createEmptyValidationRequest() {
        ValidationRequestDTO dto = new ValidationRequestDTO();
        dto.setStatus("NOT_STARTED");
        dto.setSteps(List.of());
        return dto;
    }

    private SignatureDTO mapToSignatureDTO(Signature signature) {
        return new SignatureDTO(
                signature.getId(),
                signature.getTitle(),
                signature.getStatus(),
                signature.getValidator(),
                signature.getComments(),
                signature.getDate() != null ? formatDateTime(signature.getDate()) : null
        );
    }

    private SignatureStatusDTO mapToSignatureStatusDTO(Signature signature, int index, int totalSignatures) {
        String description = generateSignatureDescription(signature.getSignatureRole());
        String department = extractDepartment(signature.getSignatureRole());
        boolean isCurrentStep = "PENDING".equals(signature.getStatus()) &&
                index == getFirstPendingIndex(signature.getValidationRequest());

        LocalDateTime expectedDate = calculateExpectedDate(signature, index);

        return new SignatureStatusDTO(
                signature.getId(),
                signature.getTitle(),
                description,
                signature.getSignatureRole(),
                signature.getStatus(),
                signature.getValidator(),
                signature.getComments(),
                signature.getDate() != null ? formatDateTime(signature.getDate()) : null,
                signature.getDate() != null ? formatDateTime(signature.getDate()) : null,
                expectedDate != null ? formatDateTime(expectedDate) : null,
                index + 1,
                true,
                isCurrentStep,
                department,
                generateContactInfo(department),
                3
        );
    }

    private ValidationRequestDTO.StepDTO mapToStepDTO(Signature signature) {
        return new ValidationRequestDTO.StepDTO(
                signature.getId(),
                signature.getTitle(),
                signature.getStatus(),
                signature.getComments(),
                signature.getDate() != null ? formatDateTime(signature.getDate()) : null,
                signature.getValidator()
        );
    }

    private DocumentDTO mapToDocumentDTO(Document document) {
        return new DocumentDTO(
                document.getId(),
                document.getTitle(),
                document.getDescription(),
                document.getType(),
                formatDateTime(document.getDateGenerated()),
                document.isAvailable(),
                document.getUrl()
        );
    }

    private DashboardData.NotificationDTO mapToNotificationDTO(Notification notification) {
        return new DashboardData.NotificationDTO(
                notification.getId(),
                notification.getMessage(),
                formatDateTime(notification.getDate()),
                notification.isRead()
        );
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : null;
    }

    private String generateSignatureDescription(String role) {
        switch (role.toLowerCase()) {
            case "scolarite":
                return "Vérification des relevés de notes et validation académique";
            case "directeur_departement":
                return "Approbation du directeur de département";
            case "doyen":
                return "Validation finale par le doyen";
            case "rectorat":
                return "Signature officielle du rectorat";
            default:
                return "Signature requise pour la validation";
        }
    }

    private String extractDepartment(String role) {
        switch (role.toLowerCase()) {
            case "scolarite":
                return "Service Scolarité";
            case "directeur_departement":
                return "Direction Départementale";
            case "doyen":
                return "Décanat";
            case "rectorat":
                return "Rectorat";
            default:
                return "Administration";
        }
    }

    private String generateContactInfo(String department) {
        switch (department) {
            case "Service Scolarité":
                return "scolarite@esp.sn - Bureau A101";
            case "Direction Départementale":
                return "direction@esp.sn - Bureau B201";
            case "Décanat":
                return "doyen@esp.sn - Bureau C301";
            case "Rectorat":
                return "rectorat@esp.sn - Bureau Principal";
            default:
                return "info@esp.sn";
        }
    }

    private int getFirstPendingIndex(ValidationRequest request) {
        List<Signature> signatures = signatureRepository.findByValidationRequest(request);
        for (int i = 0; i < signatures.size(); i++) {
            if ("PENDING".equals(signatures.get(i).getStatus())) {
                return i;
            }
        }
        return -1;
    }

    private LocalDateTime calculateExpectedDate(Signature signature, int index) {
        if (!"PENDING".equals(signature.getStatus())) {
            return null;
        }

        // Chaque signature prend environ 3 jours à partir de la date de soumission
        ValidationRequest request = signature.getValidationRequest();
        return request.getSubmissionDate().plusDays((index + 1) * 3L);
    }
}