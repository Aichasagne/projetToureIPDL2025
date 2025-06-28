package com.esp.esp_diploma_bon.controller;

import com.esp.esp_diploma_bon.dto.TrackingResponseDTO;
import com.esp.esp_diploma_bon.service.StudentTrackingService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    private final StudentTrackingService trackingService;

    public WebSocketController(StudentTrackingService trackingService) {
        this.trackingService = trackingService;
    }

    /**
     * S'abonner aux mises à jour de tracking pour une demande spécifique
     */
    @SubscribeMapping("/topic/user/{userId}/tracking-update")
    public TrackingResponseDTO subscribeToTracking(@DestinationVariable Long userId,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        // Vérifier que l'utilisateur ne s'abonne qu'à ses propres données
        Long authenticatedUserId = Long.parseLong(userDetails.getUsername());
        if (!authenticatedUserId.equals(userId)) {
            throw new SecurityException("Unauthorized subscription attempt");
        }

        // Retourner l'état actuel lors de l'abonnement
        return trackingService.getTrackingStatus(userId);
    }

    /**
     * Demander une mise à jour manuelle du statut
     */
    @MessageMapping("/tracking/refresh/{requestId}")
    @SendTo("/topic/tracking/{requestId}")
    public TrackingResponseDTO refreshTracking(@DestinationVariable Long requestId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return trackingService.getTrackingStatusByRequestId(userId, requestId);
    }
}