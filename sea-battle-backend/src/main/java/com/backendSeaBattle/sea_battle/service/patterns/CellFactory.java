package com.backendSeaBattle.sea_battle.service.patterns;

import com.backendSeaBattle.sea_battle.models.entity.Cell;
import com.backendSeaBattle.sea_battle.models.entity.Game;
import com.backendSeaBattle.sea_battle.models.entity.Ship;
import com.backendSeaBattle.sea_battle.models.entity.User;
import com.backendSeaBattle.sea_battle.models.enums.CellState;
import org.springframework.stereotype.Component;

/**
 * Фабрика для удобного создания Cell:
 * - Ship-клетка (CellState.SHIP + привязка к ship)
 * - Miss-клетка (CellState.MISS)
 */
@Component
public class CellFactory {
    public Cell createShipCell(Game game, User owner, int x, int y, Ship ship) {
        Cell cell = new Cell(game, owner, x, y, CellState.SHIP);
        cell.setShip(ship);
        return cell;
    }

    public Cell createMissCell(Game game, User owner, int x, int y) {
        return new Cell(game, owner, x, y, CellState.MISS);
    }
}
