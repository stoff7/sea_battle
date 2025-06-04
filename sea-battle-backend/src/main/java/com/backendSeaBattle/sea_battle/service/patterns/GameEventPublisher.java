/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.service.patterns;

/**
 *
 * @author Александра
 */

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Публикатор игровых событий. Хранит список слушателей и при вызове publish()
 * рассылает всеми зарегистрированным GameEventListener.
 */
@Component
public class GameEventPublisher {
    private final List<GameEventListener> listeners = new CopyOnWriteArrayList<>();

    public void register(GameEventListener listener) {
        listeners.add(listener);
    }
    public void unregister(GameEventListener listener) {
        listeners.remove(listener);
    }
    public void publish(GameEvent event) {
        for (GameEventListener l : listeners) {
            l.onGameEvent(event);
        }
    }
}
