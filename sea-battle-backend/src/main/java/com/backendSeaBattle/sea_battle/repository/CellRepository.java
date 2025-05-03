/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.repository;

import com.backendSeaBattle.sea_battle.models.entity.Cell;
import com.backendSeaBattle.sea_battle.models.entity.Game;
import com.backendSeaBattle.sea_battle.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Александра
 */
public interface CellRepository  extends JpaRepository<Cell, Long> {
    void deleteAllByGameAndOwner(Game game, User owner);
    
}
