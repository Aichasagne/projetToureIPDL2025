package com.esp.esp_diploma_bon.service;

import com.esp.esp_diploma_bon.dto.DashboardData;
import com.esp.esp_diploma_bon.dto.DocumentDTO;
import com.esp.esp_diploma_bon.dto.SignatureDTO;
import com.esp.esp_diploma_bon.dto.ValidationRequestDTO;
import com.esp.esp_diploma_bon.model.*;
import com.esp.esp_diploma_bon.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {
    private final UserRepository userRepository;
    private final ValidationRequestRepository requestRepository;
    private final SignatureRepository signatureRepository;
    private final DocumentRepository documentRepository;
    private final NotificationRepository notificationRepository;

    public StudentService(UserRepository userRepository, ValidationRequestRepository requestRepository,
                          SignatureRepository signatureRepository, DocumentRepository documentRepository,
                          NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.signatureRepository = signatureRepository;
        this.documentRepository = documentRepository;
        this.notificationRepository = notificationRepository;
    }

    public DashboardData getDashboardData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ValidationRequest> requests = requestRepository.findByStudent(user);
        ValidationRequest latestRequest = requests.isEmpty() ? null : requests.get(0);

        DashboardData data = new DashboardData();
        data.setStudent(new DashboardData.StudentDTO(
                user.getName(), user.getMatricule(), user.getDepartment(),
                user.getProgram(), user.getGraduationYear()
        ));

        if (latestRequest != null) {
            List<Signature> signatures = signatureRepository.findByValidationRequest(latestRequest);
            int completedSteps = (int) signatures.stream().filter(s -> s.getStatus().equals("COMPLETED")).count();
            int totalSteps = signatures.size();
            int progress = totalSteps > 0 ? (completedSteps * 100 / totalSteps) : 0;

            data.setValidationStatus(new DashboardData.ValidationStatusDTO(
                    latestRequest.getStatus(), progress, completedSteps, totalSteps,
                    latestRequest.getLastUpdated().toString()
            ));

            data.setPendingSignatures(signatures.stream()
                    .filter(s -> s.getStatus().equals("PENDING"))
                    .map(s -> new SignatureDTO(s.getId(), s.getTitle(), s.getStatus()))
                    .collect(Collectors.toList()));

            data.setCompletedSignatures(signatures.stream()
                    .filter(s -> s.getStatus().equals("COMPLETED"))
                    .map(s -> new SignatureDTO(s.getId(), s.getTitle(), s.getStatus(), s.getDate().toString()))
                    .collect(Collectors.toList()));
        }

        data.setNotifications(notificationRepository.findByUser(user).stream()
                .map(n -> new DashboardData.NotificationDTO(n.getId(), n.getMessage(), n.getDate().toString(), n.isRead()))
                .collect(Collectors.toList()));

        return data;
    }

    public ValidationRequestDTO getValidationStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ValidationRequest> requests = requestRepository.findByStudent(user);
        ValidationRequest request = requests.isEmpty() ? null : requests.get(0);

        if (request == null) {
            return new ValidationRequestDTO();
        }

        ValidationRequestDTO dto = new ValidationRequestDTO();
        dto.setOverallStatus(request.getStatus());
        dto.setProgress(calculateProgress(request));
        dto.setStartDate(request.getSubmissionDate().toString());
        return getValidationRequestDTO(request, dto, signatureRepository);
    }

    static ValidationRequestDTO getValidationRequestDTO(ValidationRequest request, ValidationRequestDTO dto, SignatureRepository signatureRepository) {
        dto.setLastUpdate(request.getLastUpdated().toString());

        List<Signature> signatures = signatureRepository.findByValidationRequest(request);
        dto.setSteps(signatures.stream()
                .map(s -> new ValidationRequestDTO.StepDTO(
                        s.getId(), s.getTitle(), s.getStatus(), s.getComments(),
                        s.getDate() != null ? s.getDate().toString() : null,
                        s.getValidator()))
                .collect(Collectors.toList()));

        return dto;
    }

    public List<DocumentDTO> getAvailableDocuments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ValidationRequest> requests = requestRepository.findByStudent(user);
        ValidationRequest request = requests.isEmpty() ? null : requests.get(0);

        if (request == null) {
            return List.of();
        }

        return documentRepository.findByValidationRequest(request).stream()
                .filter(Document::isAvailable)
                .map(d -> new DocumentDTO(d.getId(), d.getTitle(), d.getDescription(), d.getType(),
                        d.getDateGenerated().toString(), true, d.getUrl()))
                .collect(Collectors.toList());
    }

    private int calculateProgress(ValidationRequest request) {
        List<Signature> signatures = signatureRepository.findByValidationRequest(request);
        int completed = (int) signatures.stream().filter(s -> s.getStatus().equals("COMPLETED")).count();
        int total = signatures.size();
        return total > 0 ? (completed * 100 / total) : 0;
    }
}