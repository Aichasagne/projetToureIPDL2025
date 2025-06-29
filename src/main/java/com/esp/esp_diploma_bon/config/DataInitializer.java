package com.esp.esp_diploma_bon.config;

import com.esp.esp_diploma_bon.model.*;
import com.esp.esp_diploma_bon.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   RoleRepository roleRepository,
                                   ValidationRequestRepository requestRepository,
                                   SignatureRepository signatureRepository,
                                   DocumentRepository documentRepository,
                                   NotificationRepository notificationRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            // Vérifier si les données existent déjà
            if (userRepository.count() > 0) {
                return; // Données déjà initialisées
            }

            // Création des rôles
            Role adminRole = new Role();
            adminRole.setName("admin");
            roleRepository.save(adminRole);

            Role studentRole = new Role();
            studentRole.setName("student");
            roleRepository.save(studentRole);

            // Création des utilisateurs administrateurs
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("Administrateur Principal");
            admin.setEmail("admin@esp.sn");
            admin.setRole(adminRole);
            userRepository.save(admin);

            User scolarite = new User();
            scolarite.setUsername("scolarite");
            scolarite.setPassword(passwordEncoder.encode("scolarite123"));
            scolarite.setName("Service Scolarité");
            scolarite.setEmail("scolarite@esp.sn");
            scolarite.setRole(adminRole);
            userRepository.save(scolarite);

            User directeur = new User();
            directeur.setUsername("directeur");
            directeur.setPassword(passwordEncoder.encode("directeur123"));
            directeur.setName("Directeur Département");
            directeur.setEmail("directeur@esp.sn");
            directeur.setRole(adminRole);
            userRepository.save(directeur);

            User doyen = new User();
            doyen.setUsername("doyen");
            doyen.setPassword(passwordEncoder.encode("doyen123"));
            doyen.setName("Doyen de l'École");
            doyen.setEmail("doyen@esp.sn");
            doyen.setRole(adminRole);
            userRepository.save(doyen);

            // Création d'étudiants de test
            User student1 = createStudent("student1", "Amadou Diallo", "20210001",
                    "Génie Informatique", "Informatique", "2021", "2025",
                    "amadou.diallo@esp.sn", studentRole, passwordEncoder);
            userRepository.save(student1);

            User student2 = createStudent("student2", "Fatou Sall", "20210002",
                    "Génie Civil", "Génie Civil", "2021", "2025",
                    "fatou.sall@esp.sn", studentRole, passwordEncoder);
            userRepository.save(student2);

            User student3 = createStudent("student3", "Ousmane Ba", "20200015",
                    "Génie Électronique", "Électronique", "2020", "2024",
                    "ousmane.ba@esp.sn", studentRole, passwordEncoder);
            userRepository.save(student3);

            // Création des demandes de validation avec différents états

            // Demande complète pour student1
            ValidationRequest request1 = createValidationRequest(student1, "COMPLETED");
            requestRepository.save(request1);
            createCompletedSignatures(request1, signatureRepository);
            createDocuments(request1, documentRepository, true);

            // Demande en cours pour student2
            ValidationRequest request2 = createValidationRequest(student2, "IN_PROGRESS");
            requestRepository.save(request2);
            createInProgressSignatures(request2, signatureRepository);
            createDocuments(request2, documentRepository, false);

            // Demande en attente pour student3
            ValidationRequest request3 = createValidationRequest(student3, "PENDING");
            requestRepository.save(request3);
            createPendingSignatures(request3, signatureRepository);
            createDocuments(request3, documentRepository, false);

            // Création de notifications de test
            createSampleNotifications(student1, student2, student3, notificationRepository);

            System.out.println("✅ Base de données initialisée avec des données de test");
            System.out.println("👤 Utilisateurs créés:");
            System.out.println("   - admin/admin123 (Administrateur)");
            System.out.println("   - scolarite/scolarite123 (Service Scolarité)");
            System.out.println("   - directeur/directeur123 (Directeur Département)");
            System.out.println("   - doyen/doyen123 (Doyen)");
            System.out.println("   - student1/student123 (Amadou Diallo - Demande complétée)");
            System.out.println("   - student2/student123 (Fatou Sall - Demande en cours)");
            System.out.println("   - student3/student123 (Ousmane Ba - Demande en attente)");
        };
    }

    private User createStudent(String username, String name, String matricule,
                               String program, String department, String enrollmentYear,
                               String graduationYear, String email, Role role,
                               PasswordEncoder passwordEncoder) {
        User student = new User();
        student.setUsername(username);
        student.setPassword(passwordEncoder.encode("student123"));
        student.setName(name);
        student.setMatricule(matricule);
        student.setProgram(program);
        student.setDepartment(department);
        student.setEnrollmentYear(enrollmentYear);
        student.setGraduationYear(graduationYear);
        student.setEmail(email);
        student.setRole(role);
        return student;
    }

    private ValidationRequest createValidationRequest(User student, String status) {
        ValidationRequest request = new ValidationRequest();
        request.setStudent(student);
        request.setStatus(status);

        LocalDateTime submissionDate = LocalDateTime.now().minusDays(
                status.equals("COMPLETED") ? 21 : (status.equals("IN_PROGRESS") ? 14 : 7)
        );
        request.setSubmissionDate(submissionDate);

        LocalDateTime lastUpdated = status.equals("PENDING") ?
                submissionDate : LocalDateTime.now().minusDays(1);
        request.setLastUpdated(lastUpdated);

        return request;
    }

    private void createCompletedSignatures(ValidationRequest request, SignatureRepository signatureRepository) {
        List<SignatureData> signaturesData = Arrays.asList(
                new SignatureData("Service Scolarité", "scolarite", "COMPLETED",
                        "Mme. Diop", "Dossier académique vérifié et conforme", 7),
                new SignatureData("Direction Département", "directeur_departement", "COMPLETED",
                        "M. Fall", "Validation départementale approuvée", 5),
                new SignatureData("Décanat", "doyen", "COMPLETED",
                        "Pr. Niang", "Validation finale du décanat", 3),
                new SignatureData("Rectorat", "rectorat", "COMPLETED",
                        "Recteur Sow", "Signature officielle du rectorat", 1)
        );

        for (SignatureData data : signaturesData) {
            Signature signature = new Signature();
            signature.setValidationRequest(request);
            signature.setTitle(data.title);
            signature.setSignatureRole(data.role);
            signature.setStatus(data.status);
            signature.setValidator(data.validator);
            signature.setComments(data.comments);
            signature.setDate(LocalDateTime.now().minusDays(data.daysAgo));
            signatureRepository.save(signature);
        }
    }

    private void createInProgressSignatures(ValidationRequest request, SignatureRepository signatureRepository) {
        List<SignatureData> signaturesData = Arrays.asList(
                new SignatureData("Service Scolarité", "scolarite", "COMPLETED",
                        "Mme. Diop", "Relevés de notes validés", 10),
                new SignatureData("Direction Département", "directeur_departement", "COMPLETED",
                        "M. Fall", "Validation du cursus approuvée", 5),
                new SignatureData("Décanat", "doyen", "PENDING",
                        null, null, 0),
                new SignatureData("Rectorat", "rectorat", "PENDING",
                        null, null, 0)
        );

        for (SignatureData data : signaturesData) {
            Signature signature = new Signature();
            signature.setValidationRequest(request);
            signature.setTitle(data.title);
            signature.setSignatureRole(data.role);
            signature.setStatus(data.status);
            signature.setValidator(data.validator);
            signature.setComments(data.comments);
            if (data.status.equals("COMPLETED")) {
                signature.setDate(LocalDateTime.now().minusDays(data.daysAgo));
            }
            signatureRepository.save(signature);
        }
    }

    private void createPendingSignatures(ValidationRequest request, SignatureRepository signatureRepository) {
        List<SignatureData> signaturesData = Arrays.asList(
                new SignatureData("Service Scolarité", "scolarite", "PENDING",
                        null, null, 0),
                new SignatureData("Direction Département", "directeur_departement", "PENDING",
                        null, null, 0),
                new SignatureData("Décanat", "doyen", "PENDING",
                        null, null, 0),
                new SignatureData("Rectorat", "rectorat", "PENDING",
                        null, null, 0)
        );

        for (SignatureData data : signaturesData) {
            Signature signature = new Signature();
            signature.setValidationRequest(request);
            signature.setTitle(data.title);
            signature.setSignatureRole(data.role);
            signature.setStatus(data.status);
            signatureRepository.save(signature);
        }
    }

    private void createDocuments(ValidationRequest request, DocumentRepository documentRepository, boolean available) {
        List<DocumentData> documentsData = Arrays.asList(
                new DocumentData("Diplôme Officiel",
                        "Diplôme officiel certifié par l'École Supérieure Polytechnique", "PDF"),
                new DocumentData("Certificat de Validation",
                        "Certificat attestant de la validation complète du diplôme", "PDF"),
                new DocumentData("Relevé de Notes Officiel",
                        "Relevé de notes certifié conforme", "PDF"),
                new DocumentData("Attestation de Réussite",
                        "Attestation officielle de réussite au programme", "PDF")
        );

        for (DocumentData data : documentsData) {
            Document document = new Document();
            document.setValidationRequest(request);
            document.setTitle(data.title);
            document.setDescription(data.description);
            document.setType(data.type);
            document.setAvailable(available);

            if (available) {
                document.setDateGenerated(LocalDateTime.now().minusDays(1));
                document.setUrl("/api/documents/download/" + request.getId() + "/" + document.getId());
            }

            documentRepository.save(document);
        }
    }

    private void createSampleNotifications(User student1, User student2, User student3,
                                           NotificationRepository notificationRepository) {
        // Notifications pour student1 (demande complétée)
        createNotification(student1, "🎉 Félicitations! Votre diplôme a été validé par toutes les autorités.",
                LocalDateTime.now().minusDays(1), false, notificationRepository);
        createNotification(student1, "✅ Signature obtenue du Rectorat par Recteur Sow",
                LocalDateTime.now().minusDays(2), true, notificationRepository);
        createNotification(student1, "✅ Signature obtenue du Décanat par Pr. Niang",
                LocalDateTime.now().minusDays(5), true, notificationRepository);

        // Notifications pour student2 (demande en cours)
        createNotification(student2, "✅ Signature obtenue de la Direction par M. Fall",
                LocalDateTime.now().minusDays(2), false, notificationRepository);
        createNotification(student2, "🔄 Votre demande est maintenant au niveau du Décanat",
                LocalDateTime.now().minusDays(3), true, notificationRepository);
        createNotification(student2, "✅ Signature obtenue du Service Scolarité par Mme. Diop",
                LocalDateTime.now().minusDays(8), true, notificationRepository);

        // Notifications pour student3 (demande en attente)
        createNotification(student3, "📋 Votre demande de diplôme a été soumise avec succès",
                LocalDateTime.now().minusDays(5), false, notificationRepository);
        createNotification(student3, "ℹ️ Votre dossier est en cours de vérification par le Service Scolarité",
                LocalDateTime.now().minusDays(7), true, notificationRepository);
    }

    private void createNotification(User user, String message, LocalDateTime date,
                                    boolean read, NotificationRepository notificationRepository) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setDate(date);
        notification.setRead(read);
        notificationRepository.save(notification);
    }

    // Classes utilitaires pour les données
    private static class SignatureData {
        String title;
        String role;
        String status;
        String validator;
        String comments;
        int daysAgo;

        SignatureData(String title, String role, String status, String validator, String comments, int daysAgo) {
            this.title = title;
            this.role = role;
            this.status = status;
            this.validator = validator;
            this.comments = comments;
            this.daysAgo = daysAgo;
        }
    }

    private static class DocumentData {
        String title;
        String description;
        String type;

        DocumentData(String title, String description, String type) {
            this.title = title;
            this.description = description;
            this.type = type;
        }
    }
}