package com.esp.esp_diplomas.service;

import com.esp.esp_diplomas.dto.SignatureDTO;
import com.esp.esp_diplomas.dto.ValidationRequestDTO;
import com.esp.esp_diplomas.model.*;
import com.esp.esp_diplomas.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private final ValidationRequestRepository requestRepository;
    private final SignatureRepository signatureRepository;
    private final NotificationRepository notificationRepository;

    public AdminService(ValidationRequestRepository requestRepository,
                        SignatureRepository signatureRepository,
                        NotificationRepository notificationRepository) {
        this.requestRepository = requestRepository;
        this.signatureRepository = signatureRepository;
        this.notificationRepository = notificationRepository;
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

        currentSignature.setStatus(action.equals("approve") ? "COMPLETED" : "REJECTED");
        currentSignature.setComments(comments);
        currentSignature.setValidator(validator);
        currentSignature.setDate(LocalDateTime.now());
        signatureRepository.save(currentSignature);

        if (action.equals("reject")) {
            request.setStatus("REJECTED");
        } else {
            boolean allCompleted = signatures.stream().allMatch(s -> s.getStatus().equals("COMPLETED"));
            if (allCompleted) {
                request.setStatus("COMPLETED");
                // Make documents available
                request.getDocuments().forEach(d -> {
                    d.setAvailable(true);
                    d.setDateGenerated(LocalDateTime.now());
                });
            }
        }

        request.setLastUpdated(LocalDateTime.now());
        requestRepository.save(request);

        // Notify student
        Notification notification = new Notification();
        notification.setUser(request.getStudent());
        notification.setMessage("Votre demande a été " + (action.equals("approve") ? "approuvée" : "rejetée") +
                " par " + validator + ": " + comments);
        notification.setDate(LocalDateTime.now());
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    private ValidationRequestDTO mapToDTO(ValidationRequest request) {
        ValidationRequestDTO dto = new ValidationRequestDTO();
        dto.setId(request.getId());
        dto.setStudent(new ValidationRequestDTO.StudentDTO(
                request.getStudent().getId(), request.getStudent().getName(),
                request.getStudent().getDepartment(), request.getStudent().getProgram(),
                request.getStudent().getEmail()
        ));
        dto.setStatus(request.getStatus());
        dto.setSubmissionDate(request.getSubmissionDate().toString());
        List<SignatureDTO> signatures = signatureRepository.findByValidationRequest(request)
                .stream()
                .map(s -> new SignatureDTO(
                        s.getId(),
                        s.getRole(),
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