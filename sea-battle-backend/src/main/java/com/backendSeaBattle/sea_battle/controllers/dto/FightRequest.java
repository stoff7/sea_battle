/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.controllers.dto;

import com.backendSeaBattle.sea_battle.models.enums.CellState;
import jakarta.validation.constraints.NotNull;
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

public class FightRequest {
    @NotNull 
    private Long playerId; 
    
    @NotNull 
    private CellCoords coord; 
    

}
