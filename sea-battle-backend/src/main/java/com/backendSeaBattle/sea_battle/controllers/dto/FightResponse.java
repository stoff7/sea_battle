/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.controllers.dto;

import com.backendSeaBattle.sea_battle.models.enums.CellState;
import com.backendSeaBattle.sea_battle.models.enums.GameStatus;
import com.backendSeaBattle.sea_battle.models.enums.ShipState;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Александра
 */


@NoArgsConstructor 
@Getter 
@Setter 

public class FightResponse {

    private Long playerId; 
    

    private CellCoords coord;

    private CellState State;
    
    private Long nextPlayerId; 
    
    private GameStatus gameStatus; 
    
    private String message;
    
    private ShipState ShipState;

    public FightResponse(Long playerId, CellCoords coord, CellState State, Long nextPlayerId, GameStatus gameStatus, ShipState ShipState) {
        this.playerId = playerId;
        this.coord = coord;
        this.State = State;
        this.nextPlayerId = nextPlayerId;
        this.gameStatus = gameStatus;
        this.ShipState = ShipState;
    }

    public FightResponse(Long playerId, CellCoords coord, CellState State, Long nextPlayerId, GameStatus gameStatus, ShipState ShipState,  String message) {
        this.playerId = playerId;
        this.coord = coord;
        this.State = State;
        this.nextPlayerId = nextPlayerId;
        this.gameStatus = gameStatus;
        this.message = message;
        this.ShipState = ShipState;
    }
    
    
    
    
    



    
    
}
