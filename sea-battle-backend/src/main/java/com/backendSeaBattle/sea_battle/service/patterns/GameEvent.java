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
import lombok.Getter;

import java.util.Map;

/**
 * Обобщённое игровое событие для передачи в WebSocket:
 * – gameId: id игры
 * – eventType: строка ("playerJoined", "shotFired", и т.д.)
 * – payload: остальные данные (Map<"ключ", Object>)
 */
@AllArgsConstructor
@Getter
public class GameEvent {
    private final Long gameId;
    private final String eventType;
    private final Map<String, Object> payload;
}
