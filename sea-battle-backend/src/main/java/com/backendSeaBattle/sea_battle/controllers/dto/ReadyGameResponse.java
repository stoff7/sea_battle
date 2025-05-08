/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.controllers.dto;

import com.backendSeaBattle.sea_battle.models.enums.GameStatus;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 *
 * @author Александра
 */

@NoArgsConstructor 
@AllArgsConstructor 

public class ReadyGameResponse {
    GameStatus gameStatus; 
    boolean firstOwnerReady; 
    boolean secondOwnerReady;
    String message; 

    public ReadyGameResponse(GameStatus gameStatus, boolean firstOwnerReady, boolean secondOwnerReady) {
        this.gameStatus = gameStatus;
        this.firstOwnerReady = firstOwnerReady;
        this.secondOwnerReady = secondOwnerReady;
    }

    public ReadyGameResponse(String message) {
        this.message = message;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public boolean isFirstOwnerReady() {
        return firstOwnerReady;
    }

    public void setFirstOwnerReady(boolean firstOwnerReady) {
        this.firstOwnerReady = firstOwnerReady;
    }

    public boolean isSecondOwnerReady() {
        return secondOwnerReady;
    }

    public void setSecondOwnerReady(boolean secondOwnerReady) {
        this.secondOwnerReady = secondOwnerReady;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    
    
    
}
