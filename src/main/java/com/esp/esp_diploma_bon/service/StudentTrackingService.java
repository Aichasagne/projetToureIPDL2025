package com.esp.esp_diploma_bon.service;

import com.esp.esp_diploma_bon.dto.TrackingResponseDTO;
import com.esp.esp_diploma_bon.exception.ResourceNotFoundException;
import com.esp.esp_diploma_bon.model.*;
import com.esp.esp_diploma_bon.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StudentTrackingService {
    private final UserRepository userRepository;
    private final ValidationRequestRepository requestRepository;
    private final SignatureRepository signatureRepository;
    private final NotificationRepository notificationRepository;

    // Temps moyen estimé par signature (en jours)
    private static final int AVERAGE_SIGNATURE_DAYS = 2;

    public StudentTrackingService(UserRepository userRepository,
                                  ValidationRequestRepository requestRepository,
                                  SignatureRepository signatureRepository,
                                  NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.signatureRepository = signatureRepository;
        this.notificationRepository = notificationRepository;
    }

    public TrackingResponseDTO getTrackingStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<ValidationRequest> requests = requestRepository.findByStudent(user);
        if (requests.isEmpty()) {
            throw new ResourceNotFoundException("No validation request found for user");
        }

        // Récupérer la demande la plus récente
        ValidationRequest latestRequest = requests.stream()
                .max(Comparator.comparing(ValidationRequest::getSubmissionDate))
                .orElseThrow();

        return buildTrackingResponse(latestRequest);
    }

    public TrackingResponseDTO getTrackingStatusByRequestId(Long userId, Long requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        ValidationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ValidationRequest", "id", requestId));

        // Vérifier que la demande appartient bien à l'utilisateur
        if (!request.getStudent().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Request does not belong to user");
        }

        return buildTrackingResponse(request);
    }

    public TrackingResponseDTO.SignatureDetails getSignatureDetails(Long userId, Long requestId) {
        TrackingResponseDTO response = getTrackingStatusByRequestId(userId, requestId);
        return response.getSignatureDetails();
    }

    public TrackingResponseDTO.Timeline getTimeline(Long userId, Long requestId) {
        TrackingResponseDTO response = getTrackingStatusByRequestId(userId, requestId);
        return response.getTimeline();
    }

    private TrackingResponseDTO buildTrackingResponse(ValidationRequest request) {
        TrackingResponseDTO response = new TrackingResponseDTO();
        response.setRequestId(request.getId());
        response.setStatus(request.getStatus());
        response.setLastUpdate(request.getLastUpdated());

        // Récupérer toutes les signatures
        List<Signature> signatures = signatureRepository.findByValidationRequest(request);

        // Calculer les détails des signatures
        response.setSignatureDetails(buildSignatureDetails(signatures));

        // Calculer le pourcentage de progression
        response.setProgressPercentage(calculateProgress(signatures));

        // Construire la timeline
        response.setTimeline(buildTimeline(request, signatures));

        // Estimer la date de fin
        response.setEstimatedCompletion(calculateEstimatedCompletion(signatures));

        // Générer les alertes
        response.setAlerts(generateAlerts(request, signatures));

        return response;
    }

    private TrackingResponseDTO.SignatureDetails buildSignatureDetails(List<Signature> signatures) {
        TrackingResponseDTO.SignatureDetails details = new TrackingResponseDTO.SignatureDetails();

        details.setTotalRequired(signatures.size());
        details.setObtained((int) signatures.stream()
                .filter(s -> "COMPLETED".equals(s.getStatus()))
                .count());
        details.setPending((int) signatures.stream()
                .filter(s -> "PENDING".equals(s.getStatus()))
                .count());
        details.setRejected((int) signatures.stream()
                .filter(s -> "REJECTED".equals(s.getStatus()))
                .count());

        // Trouver l'étape actuelle
        Optional<Signature> currentStep = signatures.stream()
                .filter(s -> "PENDING".equals(s.getStatus()))
                .findFirst();

        List<TrackingResponseDTO.SignatureInfo> signatureInfos = new ArrayList<>();
        for (int i = 0; i < signatures.size(); i++) {
            Signature signature = signatures.get(i);
            TrackingResponseDTO.SignatureInfo info = new TrackingResponseDTO.SignatureInfo();

            info.setId(signature.getId());
            info.setTitle(signature.getTitle());
            info.setRole(signature.getSignatureRole());
            info.setStatus(signature.getStatus());
            info.setValidator(signature.getValidator());
            info.setSignedDate(signature.getDate());
            info.setComments(signature.getComments());
            info.setOrderIndex(i + 1);
            info.setCurrent(currentStep.isPresent() &&
                    currentStep.get().getId().equals(signature.getId()));

            signatureInfos.add(info);
        }

        details.setSignatures(signatureInfos);
        return details;
    }

    private int calculateProgress(List<Signature> signatures) {
        if (signatures.isEmpty()) {
            return 0;
        }

        long completed = signatures.stream()
                .filter(s -> "COMPLETED".equals(s.getStatus()) || "REJECTED".equals(s.getStatus()))
                .count();

        return (int) ((completed * 100) / signatures.size());
    }

    private TrackingResponseDTO.Timeline buildTimeline(ValidationRequest request, List<Signature> signatures) {
        TrackingResponseDTO.Timeline timeline = new TrackingResponseDTO.Timeline();
        timeline.setSubmissionDate(request.getSubmissionDate());

        List<TrackingResponseDTO.TimelineEvent> events = new ArrayList<>();

        // Événement de soumission
        TrackingResponseDTO.TimelineEvent submissionEvent = new TrackingResponseDTO.TimelineEvent();
        submissionEvent.setTimestamp(request.getSubmissionDate());
        submissionEvent.setType("SUBMISSION");
        submissionEvent.setTitle("Demande soumise");
        submissionEvent.setDescription("Votre demande de validation a été soumise");
        submissionEvent.setActor(request.getStudent().getName());
        submissionEvent.setStatus("COMPLETED");
        events.add(submissionEvent);

        // Événements de signature
        for (Signature signature : signatures) {
            if (signature.getDate() != null) {
                TrackingResponseDTO.TimelineEvent signatureEvent = new TrackingResponseDTO.TimelineEvent();
                signatureEvent.setTimestamp(signature.getDate());
                signatureEvent.setType("SIGNATURE");
                signatureEvent.setTitle(signature.getTitle());
                signatureEvent.setActor(signature.getValidator());
                signatureEvent.setStatus(signature.getStatus());

                if ("COMPLETED".equals(signature.getStatus())) {
                    signatureEvent.setDescription("Signature approuvée");
                } else if ("REJECTED".equals(signature.getStatus())) {
                    signatureEvent.setDescription("Signature rejetée: " + signature.getComments());
                }

                events.add(signatureEvent);
            }
        }

        // Trier les événements par date
        events.sort(Comparator.comparing(TrackingResponseDTO.TimelineEvent::getTimestamp));
        timeline.setEvents(events);

        return timeline;
    }

    private TrackingResponseDTO.EstimatedCompletion calculateEstimatedCompletion(List<Signature> signatures) {
        TrackingResponseDTO.EstimatedCompletion completion = new TrackingResponseDTO.EstimatedCompletion();

        long pendingCount = signatures.stream()
                .filter(s -> "PENDING".equals(s.getStatus()))
                .count();

        completion.setRemainingSteps((int) pendingCount);

        if (pendingCount == 0) {
            completion.setEstimatedDate(LocalDateTime.now());
            completion.setMessage("Toutes les signatures ont été traitées");
        } else {
            // Calculer une estimation basée sur le temps moyen
            LocalDateTime estimatedDate = LocalDateTime.now()
                    .plusDays(pendingCount * AVERAGE_SIGNATURE_DAYS);
            completion.setEstimatedDate(estimatedDate);
            completion.setMessage(String.format("Estimation basée sur %d jours par signature",
                    AVERAGE_SIGNATURE_DAYS));
        }

        return completion;
    }

    private List<TrackingResponseDTO.Alert> generateAlerts(ValidationRequest request,
                                                           List<Signature> signatures) {
        List<TrackingResponseDTO.Alert> alerts = new ArrayList<>();

        // Alerte si une signature a été rejetée
        boolean hasRejection = signatures.stream()
                .anyMatch(s -> "REJECTED".equals(s.getStatus()));

        if (hasRejection) {
            TrackingResponseDTO.Alert alert = new TrackingResponseDTO.Alert();
            alert.setType("WARNING");
            alert.setMessage("Une ou plusieurs signatures ont été rejetées. " +
                    "Veuillez consulter les commentaires pour plus de détails.");
            alert.setDate(LocalDateTime.now());
            alerts.add(alert);
        }

        // Alerte si la demande est en attente depuis longtemps
        long daysSinceSubmission = ChronoUnit.DAYS.between(request.getSubmissionDate(), LocalDateTime.now());
        if (daysSinceSubmission > 10 && "PENDING".equals(request.getStatus())) {
            TrackingResponseDTO.Alert alert = new TrackingResponseDTO.Alert();
            alert.setType("INFO");
            alert.setMessage(String.format("Votre demande est en cours de traitement depuis %d jours",
                    daysSinceSubmission));
            alert.setDate(LocalDateTime.now());
            alerts.add(alert);
        }

        // Alerte si toutes les signatures sont complétées
        boolean allCompleted = signatures.stream()
                .allMatch(s -> "COMPLETED".equals(s.getStatus()));

        if (allCompleted) {
            TrackingResponseDTO.Alert alert = new TrackingResponseDTO.Alert();
            alert.setType("INFO");
            alert.setMessage("Félicitations ! Toutes les signatures ont été obtenues. " +
                    "Vos documents sont maintenant disponibles.");
            alert.setDate(LocalDateTime.now());
            alerts.add(alert);
        }

        return alerts;
    }
}