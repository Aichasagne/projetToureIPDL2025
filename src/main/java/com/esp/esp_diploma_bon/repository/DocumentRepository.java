package com.esp.esp_diploma_bon.repository;

import com.esp.esp_diploma_bon.model.Document;
import com.esp.esp_diploma_bon.model.ValidationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByValidationRequest(ValidationRequest validationRequest);
}