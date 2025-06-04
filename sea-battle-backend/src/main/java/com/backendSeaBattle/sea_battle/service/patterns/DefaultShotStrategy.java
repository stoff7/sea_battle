package com.backendSeaBattle.sea_battle.service.patterns;

import com.backendSeaBattle.sea_battle.controllers.dto.CellCoords;
import com.backendSeaBattle.sea_battle.models.entity.Cell;
import com.backendSeaBattle.sea_battle.models.entity.Game;
import com.backendSeaBattle.sea_battle.models.entity.Ship;
import com.backendSeaBattle.sea_battle.models.entity.User;
import com.backendSeaBattle.sea_battle.models.enums.CellState;
import com.backendSeaBattle.sea_battle.models.enums.ShipState;
import com.backendSeaBattle.sea_battle.repository.ShipRepository;
import com.backendSeaBattle.sea_battle.service.CellService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Базовая стратегия обработки выстрела:
 * - Если попал по SHIP, ставим HIT, помечаем корабль TOUCHED или KILL
 * - Если уже стреляли в эту клетку (HIT/MISS) – бросаем исключение
 * - Иначе – создаём MISS и передаём очередь хода
 */
@Component
@AllArgsConstructor
public class DefaultShotStrategy implements ShotStrategy {

    private final CellService cellService;
    private final ShipRepository shipRepository;

    @Override
    public ShotResult evaluateShot(Game game, User shooter, User defender, CellCoords coord) {
        Optional<Cell> maybeCell = cellService
                .findByGameAndOwnerAndXAndY(game, defender, coord.getX(), coord.getY());

        // Случай: клетка существует и в ней находится корабль
        if (maybeCell.isPresent() && maybeCell.get().getStatus() == CellState.SHIP) {
            Cell cell = maybeCell.get();
            cell.setStatus(CellState.HIT);
            cellService.save(cell);

            // Ход остаётся за стрелком
            Long nextPlayer = shooter.getUser_id();

            // Получаем сам корабль
            Ship ship = shipRepository.findById(cell.getShip().getShip_id())
                    .orElseThrow(() -> new EntityNotFoundException("Ship not found: " + cell.getShip().getShip_id()));

            // Смотрим все клетки корабля и проверяем, есть ли ещё не поражённые (CellState.SHIP)
            List<Cell> shipCells = ship.getCells();
            boolean anyAlive = shipCells.stream()
                    .anyMatch(c -> c.getStatus() == CellState.SHIP);

            ShipState newState = anyAlive ? ShipState.TOUCHED : ShipState.KILL;
            ship.setStatus(newState);
            shipRepository.save(ship);

            return new ShotResult(CellState.HIT, newState, nextPlayer);

        // Случай: клетка существует, но она уже была или HIT, или MISS
        } else if (maybeCell.isPresent() &&
                   (maybeCell.get().getStatus() == CellState.HIT || maybeCell.get().getStatus() == CellState.MISS)) {
            throw new IllegalStateException("Нельзя ходить, в данную координату был ход");

        // Случай: клетки нет — это промах
        } else {
            Cell miss = new Cell();
            miss.setGame(game);
            miss.setOwner(defender);
            miss.setX(coord.getX());
            miss.setY(coord.getY());
            miss.setStatus(CellState.MISS);
            cellService.save(miss);

            // Ход переходит к защищающемуся
            return new ShotResult(CellState.MISS, null, defender.getUser_id());
        }
    }
}
