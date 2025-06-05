/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.service.patterns;

/**
 *
 * @author Александра
 */
import com.backendSeaBattle.sea_battle.controllers.dto.CellCoords;
import com.backendSeaBattle.sea_battle.models.entity.Game;
import com.backendSeaBattle.sea_battle.models.entity.Ship;
import com.backendSeaBattle.sea_battle.models.entity.User;
import com.backendSeaBattle.sea_battle.models.enums.CellState;
import com.backendSeaBattle.sea_battle.models.enums.ShipState;

/**
 * Стратегия обработки одного выстрела: возвращает результат (какое состояние
 * клетки, состояние корабля и кто ходит следующим).
 */
public interface ShotStrategy {

    class ShotResult {

        public final CellState cellState;
        public final ShipState shipState; // TOUCHED или KILL или null
        public final Long nextPlayerId;
        public final Ship ship;

        
        public ShotResult(CellState cellState, ShipState shipState, Long nextPlayerId, Ship ship) {
            this.cellState = cellState;
            this.shipState = shipState;
            this.nextPlayerId = nextPlayerId;
            this.ship = ship;
        }
                
        public ShotResult(CellState cellState, ShipState shipState, Long nextPlayerId) {
            this.cellState = cellState;
            this.shipState = shipState;
            this.nextPlayerId = nextPlayerId;
            this.ship = null;
        }


    }

    ShotResult evaluateShot(Game game, User shooter, User defender, CellCoords coord);
}
