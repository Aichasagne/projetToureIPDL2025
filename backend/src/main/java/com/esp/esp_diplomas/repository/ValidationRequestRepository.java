<<<<<<< HEAD
package com.esp.esp_diplomas.repository;

import com.esp.esp_diplomas.model.User;
import com.esp.esp_diplomas.model.ValidationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ValidationRequestRepository extends JpaRepository<ValidationRequest, Long> {
    List<ValidationRequest> findByStudent(User student);
=======
package com.esp.esp_diplomas.repository;

import com.esp.esp_diplomas.model.User;
import com.esp.esp_diplomas.model.ValidationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ValidationRequestRepository extends JpaRepository<ValidationRequest, Long> {
    List<ValidationRequest> findByStudent(User student);
>>>>>>> ff72f9ff3699386ffcb2638e42f7767a6addeee9
}