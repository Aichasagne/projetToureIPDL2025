package com.esp.esp_diplomas.repository;

import com.esp.esp_diplomas.model.User;
import com.esp.esp_diplomas.model.ValidationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ValidationRequestRepository extends JpaRepository<ValidationRequest, Long> {
    List<ValidationRequest> findByStudent(User student);
}