// Service pour la génération de documents
package com.esp.esp_diploma_bon.service;

import com.esp.esp_diploma_bon.model.Document;
import com.esp.esp_diploma_bon.model.ValidationRequest;
import com.esp.esp_diploma_bon.model.Signature;
import com.esp.esp_diploma_bon.repository.SignatureRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DocumentGenerationService {

    private final SignatureRepository signatureRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DocumentGenerationService(SignatureRepository signatureRepository) {
        this.signatureRepository = signatureRepository;
    }

    /**
     * Génère le contenu d'un document au format PDF
     */
    public byte[] generateDocument(Document document, ValidationRequest request) {
        try {
            switch (document.getTitle().toLowerCase()) {
                case "diplôme officiel":
                    return generateDiploma(request);
                case "certificat de validation":
                    return generateValidationCertificate(request);
                case "relevé de notes officiel":
                    return generateTranscript(request);
                default:
                    return generateGenericDocument(document, request);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du document", e);
        }
    }

    private byte[] generateDiploma(ValidationRequest request) {
        // Pour cette demo, nous générons un PDF simple avec du texte
        // Dans un vrai projet, vous utiliseriez iText, Apache PDFBox, etc.

        StringBuilder content = new StringBuilder();
        content.append("ÉCOLE SUPÉRIEURE POLYTECHNIQUE\n\n");
        content.append("DIPLÔME OFFICIEL\n\n");
        content.append("Il est certifié que\n\n");
        content.append(request.getStudent().getName().toUpperCase()).append("\n");
        content.append("Matricule: ").append(request.getStudent().getMatricule()).append("\n\n");
        content.append("a satisfait à toutes les exigences du programme\n");
        content.append(request.getStudent().getProgram()).append("\n");
        content.append("Département: ").append(request.getStudent().getDepartment()).append("\n\n");
        content.append("Année de graduation: ").append(request.getStudent().getGraduationYear()).append("\n\n");

        // Ajouter les signatures
        List<Signature> signatures = signatureRepository.findByValidationRequest(request);
        content.append("Signatures de validation:\n");
        for (Signature signature : signatures) {
            if ("COMPLETED".equals(signature.getStatus())) {
                content.append("- ").append(signature.getTitle())
                        .append(" par ").append(signature.getValidator())
                        .append(" le ").append(signature.getDate().format(DATE_FORMATTER))
                        .append("\n");
            }
        }

        content.append("\nDocument généré le ").append(LocalDateTime.now().format(DATE_FORMATTER));

        return convertTextToPDF(content.toString());
    }

    private byte[] generateValidationCertificate(ValidationRequest request) {
        StringBuilder content = new StringBuilder();
        content.append("CERTIFICAT DE VALIDATION\n\n");
        content.append("Ce document certifie que la demande de diplôme de\n\n");
        content.append(request.getStudent().getName()).append("\n");
        content.append("Matricule: ").append(request.getStudent().getMatricule()).append("\n");
        content.append("Programme: ").append(request.getStudent().getProgram()).append("\n");
        content.append("Département: ").append(request.getStudent().getDepartment()).append("\n\n");
        content.append("a été validée par toutes les autorités compétentes\n");
        content.append("le ").append(request.getLastUpdated().format(DATE_FORMATTER)).append("\n\n");
        content.append("Statut: ").append(request.getStatus());

        return convertTextToPDF(content.toString());
    }

    private byte[] generateTranscript(ValidationRequest request) {
        StringBuilder content = new StringBuilder();
        content.append("RELEVÉ DE NOTES OFFICIEL\n\n");
        content.append("Étudiant: ").append(request.getStudent().getName()).append("\n");
        content.append("Matricule: ").append(request.getStudent().getMatricule()).append("\n");
        content.append("Programme: ").append(request.getStudent().getProgram()).append("\n");
        content.append("Département: ").append(request.getStudent().getDepartment()).append("\n");
        content.append("Année d'inscription: ").append(request.getStudent().getEnrollmentYear()).append("\n");
        content.append("Année de graduation: ").append(request.getStudent().getGraduationYear()).append("\n\n");

        // Ici vous ajouteriez les vraies notes depuis la base de données
        content.append("RÉSULTATS ACADÉMIQUES:\n");
        content.append("(Les notes détaillées seraient récupérées depuis le système académique)\n\n");
        content.append("Document certifié conforme\n");
        content.append("Date de génération: ").append(LocalDateTime.now().format(DATE_FORMATTER));

        return convertTextToPDF(content.toString());
    }

    private byte[] generateGenericDocument(Document document, ValidationRequest request) {
        StringBuilder content = new StringBuilder();
        content.append(document.getTitle().toUpperCase()).append("\n\n");
        content.append(document.getDescription()).append("\n\n");
        content.append("Étudiant: ").append(request.getStudent().getName()).append("\n");
        content.append("Généré le: ").append(document.getDateGenerated().format(DATE_FORMATTER));

        return convertTextToPDF(content.toString());
    }

    /**
     * Convertit du texte simple en PDF
     * Dans un vrai projet, utilisez une vraie librairie PDF
     */
    private byte[] convertTextToPDF(String text) {
        // Simulation d'un PDF - dans un vrai projet, utilisez iText ou PDFBox
        String pdfHeader = "%PDF-1.4\n";
        String pdfContent = "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n" +
                "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n" +
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] " +
                "/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n" +
                "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n" +
                "5 0 obj\n<< /Length " + (text.length() + 50) + " >>\nstream\n" +
                "BT\n/F1 12 Tf\n50 750 Td\n(" + text.replace("\n", ") Tj 0 -20 Td (") + ") Tj\nET\n" +
                "endstream\nendobj\n" +
                "xref\n0 6\n0000000000 65535 f \n0000000010 00000 n \n0000000060 00000 n \n" +
                "0000000120 00000 n \n0000000250 00000 n \n0000000320 00000 n \n" +
                "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + (400 + text.length()) + "\n%%EOF";

        return (pdfHeader + pdfContent).getBytes();
    }
}