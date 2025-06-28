package com.esp.esp_diploma_bon.controller;

import com.esp.esp_diploma_bon.dto.ValidationRequestDTO;
import com.esp.esp_diploma_bon.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/requests")
    public ResponseEntity<List<ValidationRequestDTO>> getValidationRequests() {
        return ResponseEntity.ok(adminService.getValidationRequests());
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<ValidationRequestDTO> getRequestDetails(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getRequestDetails(id));
    }

    @PostMapping("/requests/{id}/signature")
    public ResponseEntity<Void> submitSignature(@PathVariable Long id,
                                                @RequestBody SignatureSubmission submission,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        adminService.submitSignature(id, submission.getAction(), submission.getComments(),
                submission.getSignature(), userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    private static class SignatureSubmission {
        private String action;
        private String comments;
        private String signature;

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getComments() {
            return comments;
        }

        public void setComments(String comments) {
            this.comments = comments;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }
    }
}