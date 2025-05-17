/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


/**
 *
 * @author Александра
 */
@NoArgsConstructor 
 

public class JoinGameResponse {
    private Long gameId; 
    private Long playerId; 
 
    private String message;

    public JoinGameResponse(Long gameId, Long playerId, String message) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Long getGameId() {
        return gameId;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public JoinGameResponse(Long gameId, Long playerId) {
        this.gameId = gameId;
        this.playerId = playerId;
    }
    
    
}
