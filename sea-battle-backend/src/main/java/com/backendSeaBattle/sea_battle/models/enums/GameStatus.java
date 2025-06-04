/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.backendSeaBattle.sea_battle.models.enums;

/**
 *
 * @author Александра
 */
public enum GameStatus {
    WAITINGPLAYER,   // игра создана, ждёт второго игрока
    WAITINGREADY, // игра ждет готовности двух игроков
    ACTIVE,    // игра в процессе
    FINISHED   // игра завершена
}
