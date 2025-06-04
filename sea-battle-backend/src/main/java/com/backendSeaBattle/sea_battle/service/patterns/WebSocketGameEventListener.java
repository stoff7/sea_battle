/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.service.patterns;

/**
 *
 * @author Александра
 */

import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * Реализация GameEventListener, которая отправляет сообщение по WebSocket:
 * Тема: "/topic/games/{gameId}"
 * Формат payload: { "type": eventType, ... payload }
 */
@Component
@AllArgsConstructor
public class WebSocketGameEventListener implements GameEventListener {

    private final GameEventPublisher publisher;
    private final SimpMessagingTemplate ws;

    @PostConstruct
    private void init() {
        publisher.register(this);
    }

    @Override
    public void onGameEvent(GameEvent event) {
        String destination = "/topic/games/" + event.getGameId();
        // Оборачиваем event.payload в ключи плюс добавляем «type»
        Map<String, Object> message = Map.of("type", event.getEventType(), "data", event.getPayload());
        ws.convertAndSend(destination, message);
    }
}
