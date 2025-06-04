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
    private String hostName; 
    private Long hostId; 
 
    private String message;

    public JoinGameResponse(Long gameId, Long playerId, String message) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.message = message;
    }

    public JoinGameResponse(Long gameId, Long playerId, String hostName, Long hostId) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.hostName = hostName;
        this.hostId = hostId;
       
    }

    public JoinGameResponse(Long gameId, Long playerId, String hostName, Long hostId, String message) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.hostName = hostName;
        this.hostId = hostId;
        this.message = message;
    }
    

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
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
