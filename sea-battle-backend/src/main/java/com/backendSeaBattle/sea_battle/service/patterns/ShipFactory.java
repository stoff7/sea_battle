package com.backendSeaBattle.sea_battle.service.patterns;

import com.backendSeaBattle.sea_battle.models.entity.Cell;
import com.backendSeaBattle.sea_battle.models.entity.Game;
import com.backendSeaBattle.sea_battle.models.entity.Ship;
import com.backendSeaBattle.sea_battle.models.entity.User;
import com.backendSeaBattle.sea_battle.models.enums.ShipState;
import com.backendSeaBattle.sea_battle.models.enums.ShipType;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Фабрика создания объектов Ship с уже установленным статусом NOT_TOUCHED.
 */
@Component
public class ShipFactory {

    public Ship create(Game game, User owner, ShipType type, List<Cell> cells) {
        Ship ship = new Ship();
        ship.setGame(game);
        ship.setOwner(owner);
        ship.setType(type);
        ship.setStatus(ShipState.NOT_TOUCHED);

        // Присваиваем список клеток кораблю
        ship.setCells(cells);

        // В каждой клетке устанавливаем ссылку на вновь созданный ship
        for (Cell cell : cells) {
            cell.setShip(ship);
        }

        return ship;
    }
}
