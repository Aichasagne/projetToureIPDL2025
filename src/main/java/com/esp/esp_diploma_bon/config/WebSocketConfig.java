package com.esp.esp_diploma_bon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Active un simple broker en mémoire pour envoyer des messages aux clients
        config.enableSimpleBroker("/topic", "/queue");

        // Préfixe pour les messages provenant des clients
        config.setApplicationDestinationPrefixes("/app");

        // Préfixe pour les messages personnels à un utilisateur
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint pour les connexions WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000") // Frontend React
                .withSockJS(); // Support SockJS pour les navigateurs sans WebSocket natif

        // Endpoint alternatif sans SockJS
        registry.addEndpoint("/websocket")
                .setAllowedOrigins("http://localhost:3000");
    }
}