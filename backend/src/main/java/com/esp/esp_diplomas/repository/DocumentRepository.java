package com.esp.esp_diplomas.repository;

import com.esp.esp_diplomas.model.Document;
import com.esp.esp_diplomas.model.ValidationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByValidationRequest(ValidationRequest validationRequest);
}