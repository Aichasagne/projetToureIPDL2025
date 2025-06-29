// Extension du ValidationRequestRepository corrigé
package com.esp.esp_diploma_bon.repository;

import com.esp.esp_diploma_bon.model.ValidationRequest;
import com.esp.esp_diploma_bon.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ValidationRequestRepository extends JpaRepository<ValidationRequest, Long> {

    /**
     * Trouve toutes les demandes d'un étudiant
     */
    List<ValidationRequest> findByStudent(User student);

    /**
     * Trouve la demande la plus récente d'un étudiant
     */
    @Query("SELECT vr FROM ValidationRequest vr WHERE vr.student = :student ORDER BY vr.submissionDate DESC LIMIT 1")
    Optional<ValidationRequest> findLatestByStudent(@Param("student") User student);

    /**
     * Trouve les demandes par statut
     */
    @Query("SELECT vr FROM ValidationRequest vr WHERE vr.status = :status ORDER BY vr.submissionDate DESC")
    List<ValidationRequest> findByStatus(@Param("status") String status);

    /**
     * Trouve les demandes actives (en cours de traitement)
     */
    @Query("SELECT vr FROM ValidationRequest vr WHERE vr.status IN ('PENDING', 'IN_PROGRESS') ORDER BY vr.submissionDate")
    List<ValidationRequest> findActiveRequests();

    /**
     * Trouve les demandes mises à jour récemment
     */
    @Query("SELECT vr FROM ValidationRequest vr WHERE vr.lastUpdated >= :since ORDER BY vr.lastUpdated DESC")
    List<ValidationRequest> findRecentlyUpdated(@Param("since") LocalDateTime since);

    /**
     * Compte les demandes par statut pour un étudiant
     */
    @Query("SELECT COUNT(vr) FROM ValidationRequest vr WHERE vr.student = :student AND vr.status = :status")
    long countByStudentAndStatus(@Param("student") User student, @Param("status") String status);

    /**
     * Trouve les demandes en attente de signatures
     */
    @Query("SELECT DISTINCT vr FROM ValidationRequest vr JOIN vr.signatures s WHERE s.status = 'PENDING'")
    List<ValidationRequest> findRequestsWithPendingSignatures();

    /**
     * Retourne toutes les demandes complétées pour calcul en Java
     * Cette approche évite les problèmes de compatibilité SQL
     */
    @Query("SELECT vr FROM ValidationRequest vr WHERE vr.status = 'COMPLETED'")
    List<ValidationRequest> findCompletedRequests();

    /**
     * Méthode simple qui fonctionne - retourne juste le nombre de demandes complétées
     */
    @Query("SELECT COUNT(vr) FROM ValidationRequest vr WHERE vr.status = 'COMPLETED'")
    long countCompletedRequests();

    /**
     * Méthode de fallback qui ne fait pas de calcul SQL complexe
     * Le calcul du temps moyen sera fait en Java dans le service
     */
    default Double getAverageProcessingTime() {
        // Cette méthode sera implémentée dans le service
        return null;
    }
}