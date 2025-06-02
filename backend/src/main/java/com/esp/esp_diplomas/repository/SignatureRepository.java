package com.esp.esp_diplomas.repository;

import com.esp.esp_diplomas.model.Signature;
import com.esp.esp_diplomas.model.ValidationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SignatureRepository extends JpaRepository<Signature, Long> {
    List<Signature> findByValidationRequest(ValidationRequest validationRequest);
}