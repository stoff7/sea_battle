/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.repository;

import com.backendSeaBattle.sea_battle.models.entity.Game;
import com.backendSeaBattle.sea_battle.models.enums.GameStatus;
import com.backendSeaBattle.sea_battle.models.enums.GameType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Александра
 */
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional <Game> findFirstByGameTypeAndStatus (GameType gameType, GameStatus status);
    
}
