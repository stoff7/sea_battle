/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.service.patterns;

/**
 *
 * @author Александра
 */

/**
 * Интерфейс для «наблюдателей» за игровыми событиями.
 * При публикации GameEvent метод onGameEvent(...) будет вызван во всех подписанных слушателях.
 */
public interface GameEventListener {
    void onGameEvent(GameEvent event);
}

