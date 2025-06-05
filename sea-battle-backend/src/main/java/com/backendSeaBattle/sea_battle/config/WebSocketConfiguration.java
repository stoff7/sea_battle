package com.backendSeaBattle.sea_battle.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Конфигурация WebSocket (STOMP): разрешаем origin только для продакшен-фронтенда
 * (и, при необходимости, можно добавить localhost для разработки).
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
            .addEndpoint("/ws")
            .setAllowedOrigins(
                "https://bestseabattle.netlify.app",
                "http://localhost:3000"
            );
            // .withSockJS(); // если нужен SockJS
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
}
