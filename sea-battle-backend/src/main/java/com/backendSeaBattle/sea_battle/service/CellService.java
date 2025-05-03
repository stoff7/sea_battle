/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.service;

import com.backendSeaBattle.sea_battle.models.entity.Cell;
import com.backendSeaBattle.sea_battle.models.entity.Game;
import com.backendSeaBattle.sea_battle.models.entity.User;
import com.backendSeaBattle.sea_battle.models.enums.CellState;
import com.backendSeaBattle.sea_battle.repository.CellRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Александра
 */

@Service
@AllArgsConstructor
public class CellService {
    private final CellRepository repository;
    
       public Cell setCellIn(Game gameID, User Owner, int x, int y) {
        Cell cell = new Cell(gameID, Owner, x, y, CellState.SHIP); 
        repository.save(cell);
        return cell; 
    }
       
    @Transactional
    public void clearCellsForPlayer(Game game, User player) {
        repository.deleteAllByGameAndOwner(game, player);
    }

}
