/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Александра
 */
@Getter
@Setter
@AllArgsConstructor 
@NoArgsConstructor


public class JoinRandomGameResponse {

    private Long gameId;
    private Long playerId;
    private String hostName;
    private Long hostId;
    private String message; 
    
    

}
