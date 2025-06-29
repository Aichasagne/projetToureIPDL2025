// Contrôleur WebSocket pour les interactions en temps réel
package com.esp.esp_diploma_bon.controller;

import com.esp.esp_diploma_bon.service.RealTimeNotificationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private final RealTimeNotificationService notificationService;

    public WebSocketController(RealTimeNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Gère les demandes de mise à jour du dashboard
     */
    @MessageMapping("/dashboard/refresh")
    @SendToUser("/queue/dashboard/update")
    public void refreshDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        // Le service se chargera d'envoyer les données mises à jour
        // via les méthodes sendDashboardUpdate
    }

    /**
     * Gère les demandes de statut de progression
     */
    @MessageMapping("/progress/request")
    @SendToUser("/queue/progress/update")
    public void requestProgressUpdate(@AuthenticationPrincipal UserDetails userDetails) {
        // Logique pour envoyer une mise à jour de progression
    }

    /**
     * Marque une notification comme lue
     */
    @MessageMapping("/notification/read")
    public void markNotificationAsRead(@Payload NotificationReadRequest request,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        // Logique pour marquer la notification comme lue
        // Le frontend enverra l'ID de la notification
    }

    // DTO pour les requêtes de lecture de notification
    public static class NotificationReadRequest {
        private Long notificationId;

        public Long getNotificationId() { return notificationId; }
        public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }
    }
}