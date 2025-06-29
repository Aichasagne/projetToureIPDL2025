package com.esp.esp_diploma_bon.controller;

import com.esp.esp_diploma_bon.model.Document;
import com.esp.esp_diploma_bon.model.ValidationRequest;
import com.esp.esp_diploma_bon.repository.DocumentRepository;
import com.esp.esp_diploma_bon.repository.ValidationRequestRepository;
import com.esp.esp_diploma_bon.service.DocumentGenerationService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:3000")
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final ValidationRequestRepository validationRequestRepository;
    private final DocumentGenerationService documentGenerationService;

    public DocumentController(DocumentRepository documentRepository,
                              ValidationRequestRepository validationRequestRepository,
                              DocumentGenerationService documentGenerationService) {
        this.documentRepository = documentRepository;
        this.validationRequestRepository = validationRequestRepository;
        this.documentGenerationService = documentGenerationService;
    }

    /**
     * Télécharge un document spécifique
     */
    @GetMapping("/download/{requestId}/{documentId}")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long requestId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            // Vérification de l'autorisation
            ValidationRequest request = validationRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document non trouvé"));

            // Vérification que le document appartient à la bonne demande
            if (!document.getValidationRequest().getId().equals(requestId)) {
                return ResponseEntity.badRequest().build();
            }

            // Vérification que l'utilisateur a le droit de télécharger ce document
            if (!canUserDownloadDocument(userDetails, request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Vérification que le document est disponible
            if (!document.isAvailable()) {
                return ResponseEntity.notFound().build();
            }

            // Génération du contenu du document
            byte[] documentContent = documentGenerationService.generateDocument(document, request);
            ByteArrayResource resource = new ByteArrayResource(documentContent);

            // Nom du fichier avec timestamp
            String filename = generateFilename(document, request);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .contentLength(documentContent.length)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Prévisualise un document (sans téléchargement)
     */
    @GetMapping("/preview/{requestId}/{documentId}")
    public ResponseEntity<Resource> previewDocument(
            @PathVariable Long requestId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            ValidationRequest request = validationRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document non trouvé"));

            if (!document.getValidationRequest().getId().equals(requestId)) {
                return ResponseEntity.badRequest().build();
            }

            if (!canUserDownloadDocument(userDetails, request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (!document.isAvailable()) {
                return ResponseEntity.notFound().build();
            }

            byte[] documentContent = documentGenerationService.generateDocument(document, request);
            ByteArrayResource resource = new ByteArrayResource(documentContent);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .contentLength(documentContent.length)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Vérifie l'état d'un document
     */
    @GetMapping("/status/{requestId}/{documentId}")
    public ResponseEntity<DocumentStatusDTO> getDocumentStatus(
            @PathVariable Long requestId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            ValidationRequest request = validationRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document non trouvé"));

            if (!canUserDownloadDocument(userDetails, request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            DocumentStatusDTO status = new DocumentStatusDTO(
                    document.getId(),
                    document.getTitle(),
                    document.isAvailable(),
                    document.getDateGenerated() != null ?
                            document.getDateGenerated().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : null,
                    document.getType(),
                    estimateFileSize(document)
            );

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Méthodes utilitaires privées

    private boolean canUserDownloadDocument(UserDetails userDetails, ValidationRequest request) {
        // L'étudiant peut télécharger ses propres documents
        if (request.getStudent().getUsername().equals(userDetails.getUsername())) {
            return true;
        }

        // Les administrateurs peuvent télécharger tous les documents
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("admin"));
    }

    private String generateFilename(Document document, ValidationRequest request) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String studentName = request.getStudent().getName().replaceAll("[^a-zA-Z0-9]", "_");
        String documentType = document.getTitle().replaceAll("[^a-zA-Z0-9]", "_");

        return String.format("%s_%s_%s.pdf", documentType, studentName, timestamp);
    }

    private long estimateFileSize(Document document) {
        // Estimation basique de la taille du fichier selon le type
        switch (document.getTitle().toLowerCase()) {
            case "diplôme officiel":
                return 512 * 1024; // 512KB
            case "certificat de validation":
                return 256 * 1024; // 256KB
            case "relevé de notes officiel":
                return 1024 * 1024; // 1MB
            default:
                return 512 * 1024; // 512KB par défaut
        }
    }

    /**
     * DTO pour le statut d'un document
     */
    public static class DocumentStatusDTO {
        private Long id;
        private String title;
        private boolean available;
        private String dateGenerated;
        private String type;
        private long estimatedSize;

        public DocumentStatusDTO(Long id, String title, boolean available,
                                 String dateGenerated, String type, long estimatedSize) {
            this.id = id;
            this.title = title;
            this.available = available;
            this.dateGenerated = dateGenerated;
            this.type = type;
            this.estimatedSize = estimatedSize;
        }

        // Getters
        public Long getId() { return id; }
        public String getTitle() { return title; }
        public boolean isAvailable() { return available; }
        public String getDateGenerated() { return dateGenerated; }
        public String getType() { return type; }
        public long getEstimatedSize() { return estimatedSize; }

        public String getFormattedSize() {
            if (estimatedSize < 1024) {
                return estimatedSize + " B";
            } else if (estimatedSize < 1024 * 1024) {
                return String.format("%.1f KB", estimatedSize / 1024.0);
            } else {
                return String.format("%.1f MB", estimatedSize / (1024.0 * 1024.0));
            }
        }
    }
}

