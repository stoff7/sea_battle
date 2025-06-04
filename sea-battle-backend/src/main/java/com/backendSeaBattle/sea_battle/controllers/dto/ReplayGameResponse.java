/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.controllers.dto;

import com.backendSeaBattle.sea_battle.models.enums.CellState;
import com.backendSeaBattle.sea_battle.models.enums.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Александра
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter


public class ReplayGameResponse {

    private Long gameId;
    private Long playerId;
    private GameStatus gameStatus;
    private String hostName;
    private Long hostId;
    private String message;

}
