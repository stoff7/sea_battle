/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.controllers.dto;

/**
 *
 * @author Александра
 */


import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.NoArgsConstructor;

@NoArgsConstructor 

public class ReadyGameRequest {
    @NotNull
    private Long   playerId;      // чей это флот
    @NotNull
    private Boolean ready;        // true = выставил флот и жмёт Ready
    // будет null или пустым, если ready=false
    private List<CellCoords> cells;  

    public ReadyGameRequest(Long playerId, Boolean ready, List<CellCoords> cells) {
        this.playerId = playerId;
        this.ready = ready;
        this.cells = cells;
    }


    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public Boolean getReady() {
        return ready;
    }

    public void setReady(Boolean ready) {
        this.ready = ready;
    }

    public List<CellCoords> getCells() {
        return cells;
    }

    public void setCells(List<CellCoords> cells) {
        this.cells = cells;
    }
    
}

