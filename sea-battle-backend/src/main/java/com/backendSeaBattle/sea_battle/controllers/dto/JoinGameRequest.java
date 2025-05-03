/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.controllers.dto;

/**
 *
 * @author Александра
 */

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class JoinGameRequest {
  
    @NotNull(message = "gameId обязателен")
    private Long gameId;

    @NotBlank(message = "userName обязателен")
    private String userName;

    public JoinGameRequest() {}

    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}