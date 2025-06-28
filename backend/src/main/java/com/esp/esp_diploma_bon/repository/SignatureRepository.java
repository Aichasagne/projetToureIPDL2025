package com.esp.esp_diploma_bon.repository;


import com.esp.esp_diploma_bon.model.Signature;
import com.esp.esp_diploma_bon.model.ValidationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SignatureRepository extends JpaRepository<Signature, Long> {
    List<Signature> findByValidationRequest(ValidationRequest validationRequest);
}