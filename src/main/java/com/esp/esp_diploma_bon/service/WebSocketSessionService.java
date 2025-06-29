// Service pour gérer les connexions WebSocket actives
package com.esp.esp_diploma_bon.service;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketSessionService {

    // Map pour stocker les sessions actives par utilisateur
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : null;

        if (username != null) {
            userSessions.put(username, sessionId);
            System.out.println("Utilisateur connecté: " + username + " - Session: " + sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // Suppression de la session des utilisateurs connectés
        userSessions.entrySet().removeIf(entry -> entry.getValue().equals(sessionId));
        System.out.println("Session déconnectée: " + sessionId);
    }

    /**
     * Vérifie si un utilisateur est actuellement connecté
     */
    public boolean isUserConnected(String username) {
        return userSessions.containsKey(username);
    }

    /**
     * Récupère l'ID de session d'un utilisateur
     */
    public String getUserSessionId(String username) {
        return userSessions.get(username);
    }

    /**
     * Récupère tous les utilisateurs connectés
     */
    public Map<String, String> getConnectedUsers() {
        return new ConcurrentHashMap<>(userSessions);
    }
}
