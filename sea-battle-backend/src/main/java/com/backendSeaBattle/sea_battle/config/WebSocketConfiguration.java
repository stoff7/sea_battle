package com.backendSeaBattle.sea_battle.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Конфигурация WebSocket (STOMP). Allowed origins читаются из переменной окружения.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    // Строка вида "https://frontend.netlify.app" или "http://localhost:3000"
    @Value("${myapp.websocket.allowed-origins:}")
    private String allowedOrigins;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            // Если не задали ни одного origin, то по умолчанию OTO(?) – разрешаем все (но это не продакшен)
            registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
        } else {
            // Разбиваем строку на массив
            String[] originsArray = allowedOrigins.split(",");
            registry.addEndpoint("/ws")
                    .setAllowedOrigins(originsArray);
        }
        // Если нужен SockJS:
        // .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
}
