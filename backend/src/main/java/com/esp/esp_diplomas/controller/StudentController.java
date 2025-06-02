package com.esp.esp_diplomas.controller;

import com.esp.esp_diplomas.dto.DashboardData;
import com.esp.esp_diplomas.dto.DocumentDTO;
import com.esp.esp_diplomas.dto.ValidationRequestDTO;
import com.esp.esp_diplomas.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardData> getDashboardData(@AuthenticationPrincipal UserDetails userDetails) {
        // For simplicity, assume userDetails.getUsername() is userId
        Long userId = Long.parseLong(userDetails.getUsername()); // Adjust based on actual ID retrieval
        return ResponseEntity.ok(studentService.getDashboardData(userId));
    }

    @GetMapping("/status")
    public ResponseEntity<ValidationRequestDTO> getValidationStatus(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(studentService.getValidationStatus(userId));
    }

    @GetMapping("/documents")
    public ResponseEntity<List<DocumentDTO>> getAvailableDocuments(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(studentService.getAvailableDocuments(userId));
    }
}