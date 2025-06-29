package com.esp.esp_diploma_bon.repository;

import com.esp.esp_diploma_bon.model.Signature;
import com.esp.esp_diploma_bon.model.ValidationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SignatureRepository extends JpaRepository<Signature, Long> {

    /**
     * Trouve toutes les signatures pour une demande de validation
     */
    List<Signature> findByValidationRequest(ValidationRequest validationRequest);

    /**
     * Trouve les signatures par statut pour une demande donnée
     */
    @Query("SELECT s FROM Signature s WHERE s.validationRequest = :request AND s.status = :status ORDER BY s.id")
    List<Signature> findByValidationRequestAndStatus(@Param("request") ValidationRequest request,
                                                     @Param("status") String status);

    /**
     * Trouve la prochaine signature en attente pour une demande
     */
    @Query("SELECT s FROM Signature s WHERE s.validationRequest = :request AND s.status = 'PENDING' ORDER BY s.id LIMIT 1")
    Optional<Signature> findNextPendingSignature(@Param("request") ValidationRequest request);

    /**
     * Compte les signatures complétées pour une demande
     */
    @Query("SELECT COUNT(s) FROM Signature s WHERE s.validationRequest = :request AND s.status = 'COMPLETED'")
    long countCompletedSignatures(@Param("request") ValidationRequest request);

    /**
     * Compte les signatures en attente pour une demande
     */
    @Query("SELECT COUNT(s) FROM Signature s WHERE s.validationRequest = :request AND s.status = 'PENDING'")
    long countPendingSignatures(@Param("request") ValidationRequest request);

    /**
     * Trouve les signatures en retard (date attendue dépassée)
     */
    @Query("SELECT s FROM Signature s WHERE s.status = 'PENDING' AND s.date < :currentDate")
    List<Signature> findOverdueSignatures(@Param("currentDate") LocalDateTime currentDate);

    /**
     * Trouve toutes les signatures pour un étudiant spécifique
     */
    @Query("SELECT s FROM Signature s WHERE s.validationRequest.student.id = :studentId ORDER BY s.validationRequest.submissionDate DESC, s.id")
    List<Signature> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Trouve les signatures par rôle et statut
     */
    @Query("SELECT s FROM Signature s WHERE s.signatureRole = :role AND s.status = :status")
    List<Signature> findBySignatureRoleAndStatus(@Param("role") String role, @Param("status") String status);

    /**
     * Calcule le pourcentage de progression pour une demande
     */
    @Query("SELECT (COUNT(CASE WHEN s.status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(s)) " +
            "FROM Signature s WHERE s.validationRequest = :request")
    Double calculateProgressPercentage(@Param("request") ValidationRequest request);

    /**
     * Trouve les signatures récemment mises à jour
     */
    @Query("SELECT s FROM Signature s WHERE s.date >= :since ORDER BY s.date DESC")
    List<Signature> findRecentlyUpdated(@Param("since") LocalDateTime since);

    /**
     * Vérifie si toutes les signatures sont complétées pour une demande
     */
    @Query("SELECT CASE WHEN COUNT(s) = COUNT(CASE WHEN s.status = 'COMPLETED' THEN 1 END) THEN true ELSE false END " +
            "FROM Signature s WHERE s.validationRequest = :request")
    boolean areAllSignaturesCompleted(@Param("request") ValidationRequest request);
}
