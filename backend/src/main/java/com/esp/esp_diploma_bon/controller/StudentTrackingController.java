package com.esp.esp_diploma_bon.controller;

import com.esp.esp_diploma_bon.dto.TrackingResponseDTO;
import com.esp.esp_diploma_bon.service.StudentTrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/tracking")
public class StudentTrackingController {
    private final StudentTrackingService trackingService;

    public StudentTrackingController(StudentTrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @GetMapping("/status")
    public ResponseEntity<TrackingResponseDTO> getTrackingStatus(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(trackingService.getTrackingStatus(userId));
    }

    @GetMapping("/status/{requestId}")
    public ResponseEntity<TrackingResponseDTO> getTrackingStatusByRequestId(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(trackingService.getTrackingStatusByRequestId(userId, requestId));
    }

    @GetMapping("/signatures/{requestId}")
    public ResponseEntity<TrackingResponseDTO.SignatureDetails> getSignatureDetails(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(trackingService.getSignatureDetails(userId, requestId));
    }

    @GetMapping("/timeline/{requestId}")
    public ResponseEntity<TrackingResponseDTO.Timeline> getTimeline(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(trackingService.getTimeline(userId, requestId));
    }
}