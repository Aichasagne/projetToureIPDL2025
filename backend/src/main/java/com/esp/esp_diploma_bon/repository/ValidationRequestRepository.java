package com.esp.esp_diploma_bon.repository;

import com.esp.esp_diploma_bon.model.ValidationRequest;
import com.esp.esp_diploma_bon.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ValidationRequestRepository extends JpaRepository<ValidationRequest, Long> {
    List<ValidationRequest> findByStudent(User student);
}