/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.service;

import com.backendSeaBattle.sea_battle.controllers.dto.CellCoords;
import com.backendSeaBattle.sea_battle.controllers.dto.ReadyGameRequest;
import com.backendSeaBattle.sea_battle.controllers.dto.ShipRequest;
import com.backendSeaBattle.sea_battle.models.entity.Cell;
import com.backendSeaBattle.sea_battle.models.entity.Game;
import com.backendSeaBattle.sea_battle.models.entity.Ship;
import com.backendSeaBattle.sea_battle.models.entity.User;
import com.backendSeaBattle.sea_battle.models.enums.CellState;
import com.backendSeaBattle.sea_battle.models.enums.GameStatus;
import com.backendSeaBattle.sea_battle.models.enums.GameType;
import com.backendSeaBattle.sea_battle.models.enums.ShipState;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.backendSeaBattle.sea_battle.repository.GameRepository;
import com.backendSeaBattle.sea_battle.repository.ShipRepository;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Александра
 */
@Service
@AllArgsConstructor

public class GameService {

    private final GameRepository repository;
    private final UserService userService;
    private final CellService cellService;
    private final ShipRepository shipRepository;
    private final SimpMessagingTemplate ws;

    public Game startGame(User user, GameType gameType) {
        Game game = new Game(user, null, GameStatus.WAITINGPLAYER, user.getUser_id(), LocalDateTime.now(), null, gameType);
        repository.save(game);
        return game;
    }

    public GameRepository getRepository() {
        return repository;
    }

    @Transactional
    public JoinGameResult joinGame(Long gameId, String UserName) {

        Game game = repository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("Game not found: " + gameId));

        if (game.getStatus() != GameStatus.WAITINGPLAYER) {
            throw new IllegalStateException("Нельзя присоединиться, статус игры: " + game.getStatus());
        }

        User user = userService.startGame(UserName);

        game.setSecondOwner(user);

        game.setStatus(GameStatus.WAITINGREADY);
        repository.save(game);

        //Пушим по WebSocket
        ws.convertAndSend(
                "/topic/games/" + gameId,
                Map.of("type", "playerJoined",
                        "playerId", user.getUser_id(),
                        "userName", user.getUser_name())
        );

        return new JoinGameResult(game.getGame_id(), user.getUser_id(), game.getFirstOwner().getUser_name(), game.getFirstOwner().getUser_id());

    }

    public record JoinGameResult(Long gameId, Long playerId, String HostName, Long HostId) {

    }

    public record ReadyGameResult(GameStatus gameStatus, boolean firstOwnerReady, boolean secondOwnerReady) {

    }

    @Transactional
    public ReadyGameResult readyGame(Long gameId,
            ReadyGameRequest req) {
        // 1) Загрузить game и user
        Game game = repository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found: " + gameId));
        User user = userService.findById(req.getPlayerId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + req.getPlayerId()));

        // 2) Удаляем старые корабли и клетки этого игрока
        shipRepository.deleteAllByGameAndOwner(game, user);
        cellService.clearCellsForPlayer(game, user);

        if (req.getReady()) {
            // 3) Сохраняем новые корабли и их клетки
            for (ShipRequest s : req.getShips()) {
                // a) Создаём сущность Ship
                Ship ship = new Ship();
                ship.setGame(game);
                ship.setOwner(user);
                ship.setType(s.getType());
                ship.setStatus(ShipState.NOT_TOUCHED);
                shipRepository.save(ship);

                // b) Для каждой координаты — создаём Cell, привязываем к ship
                for (CellCoords c : s.getCells()) {
                    if (c.getX() < 0 || c.getX() > 9
                            || c.getY() < 0 || c.getY() > 9) {
                        throw new IllegalStateException(
                                "Координата вне поля: " + c.getX() + "," + c.getY());
                    }
                    Cell cell = new Cell(game, user, c.getX(), c.getY(), CellState.SHIP);
                    cell.setShip(ship);
                    cellService.save(cell);
                }
            }

            // c) Помечаем игрока готовым
            user.setReady(true);
            userService.save(user);

        } else {
            // Сброс готовности
            user.setReady(false);
            userService.save(user);
        }

        // 4) Пересчитываем статус игры
        boolean first = game.getFirstOwner().isReady();
        boolean second = game.getSecondOwner() != null && game.getSecondOwner().isReady();
        game.setStatus(first && second
                ? GameStatus.ACTIVE
                : GameStatus.WAITINGREADY);
        repository.save(game);

        // 5) Пушим событие playerReady
        ws.convertAndSend(
                "/topic/games/" + gameId,
                Map.of(
                        "type", "playerReady",
                        "playerId", user.getUser_id(),
                        "firstOwnerReady", first,
                        "secondOwnerReady", second,
                        "gameStatus", game.getStatus()
                )
        );

        // 6) Если оба готовы — пушим start
        if (game.getStatus() == GameStatus.ACTIVE) {
            ws.convertAndSend(
                    "/topic/games/" + gameId,
                    Map.of("type", "gameStarted")
            );
        }

        // 7) Возвращаем результат
        return new ReadyGameResult(
                game.getStatus(),
                first, second
        );
    }

    public Optional<Game> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional

    public FightResult fight(Long gameId, Long playerId, CellCoords coord) {
        // 1) Загрузить сущности
        User user = userService.findById(playerId).orElseThrow(() -> new EntityNotFoundException("User not found: " + playerId));
        Game game = repository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("Game not found: " + gameId));

        // 2) Проверить очередь
        if (!game.getCurrentTurn().equals(playerId)) {
            throw new IllegalStateException("Нельзя ходить, ход: " + game.getCurrentTurn());
        }

        // 3) Определить противника
        User defender = game.getFirstOwner().getUser_id().equals(playerId)
                ? game.getSecondOwner()
                : game.getFirstOwner();

        // 4) Попадание?
        Optional<Cell> targetCell = cellService
                .findByGameAndOwnerAndXAndY(game, defender, coord.getX(), coord.getY());

        CellState resultState;
        ShipState resultShipState = null;
        Long nextPlayerId;

        if (targetCell.isPresent() && targetCell.get().getStatus() == CellState.SHIP) {
            Cell cell = targetCell.get();
            cell.setStatus(CellState.HIT);
            cellService.save(cell);
            resultState = CellState.HIT;
            nextPlayerId = user.getUser_id();

            Ship ship = shipRepository.findById(cell.getShip().getShip_id()).orElseThrow(() -> new EntityNotFoundException("Ship not found: " + playerId));
            List<Cell> shipCells = ship.getCells();
            for (Cell s : shipCells) {
                if ((cellService.findById(s.getCell_id())).get().getStatus() == CellState.SHIP) {
                    ship.setStatus(ShipState.TOUCHED);
                    resultShipState = ShipState.TOUCHED;
                    break;
                }

                ship.setStatus(ShipState.KILL);
                resultShipState = ShipState.KILL;

            }

        } else if (targetCell.isPresent() && (targetCell.get().getStatus() == CellState.HIT || targetCell.get().getStatus() == CellState.MISS)) {
            throw new IllegalStateException("Нельзя ходить, в данную координату был ход");
        } else {
            // промах — создаём запись MISS
            Cell miss = new Cell(game, defender, coord.getX(), coord.getY(), CellState.MISS);
            cellService.save(miss);
            resultState = CellState.MISS;
            nextPlayerId = defender.getUser_id();
            game.setCurrentTurn(nextPlayerId);
        }

        repository.save(game);

        ws.convertAndSend(
                "/topic/games/" + gameId,
                Map.of(
                        "type", "shotFired",
                        "by", playerId,
                        "x", coord.getX(),
                        "y", coord.getY(),
                        "result", resultState.name(),
                        "nextPlayerId", nextPlayerId
                )
        );

        // 6) Вернуть результат
        return new FightResult(playerId, coord, resultState, nextPlayerId, resultShipState);
    }

    public record FightResult(Long playerId, CellCoords coord, CellState State, Long nextPlayerId, ShipState resultShipState) {

    }

    @Transactional
    public GameStatus endGame(Long gameId, Long playerId) {
        Game game = repository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("Game not found: " + gameId));
        User user = userService.findById(playerId).orElseThrow(() -> new EntityNotFoundException("User not found: " + playerId));

        User defender = game.getFirstOwner().getUser_id().equals(playerId)
                ? game.getSecondOwner()
                : game.getFirstOwner();

        if (!cellService.existsByGameAndOwnerAndStatus(game, defender, CellState.SHIP)) {
            game.setStatus(GameStatus.FINISHED);
            game.setFinishedAt(LocalDateTime.now());
            repository.save(game);
            cellService.clearCellsForPlayer(game, user);
            cellService.clearCellsForPlayer(game, defender);

            ws.convertAndSend(
                    "/topic/games/" + gameId,
                    Map.of(
                            "type", "gameFinished",
                            "winnerId", playerId
                    )
            );

        }

        return game.getStatus();
    }

    public Optional<Game> findOpenGame() {
        return repository.findFirstByGameTypeAndStatus(GameType.OPEN, GameStatus.WAITINGPLAYER);
    }

    @Transactional
    public void leaveGame(Long gameId, Long playerId) {
        Game game = repository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("Game not found: " + gameId));
        User user = userService.findById(playerId).orElseThrow(() -> new EntityNotFoundException("User not found: " + playerId));

        cellService.clearCellsForPlayer(game, user);
        shipRepository.deleteAllByGameAndOwner(game, user);

        boolean wasFirst = game.getFirstOwner().getUser_id().equals(playerId);
        boolean wasSecond = game.getSecondOwner() != null
                && game.getSecondOwner().getUser_id().equals(playerId);

        if (wasFirst) {
            game.setFirstOwner(game.getSecondOwner());
            game.setSecondOwner(null);
        } else if (wasSecond) {
            game.setSecondOwner(null);
        } else {
            throw new IllegalStateException("User " + playerId + " is not part of game " + gameId);
        }

        // 3) Изменить статус игры
        if (game.getFirstOwner() == null && game.getSecondOwner() == null) {
            repository.delete(game);
            return;
        } else if (game.getFirstOwner() == null || game.getSecondOwner() == null) {
            game.setStatus(GameStatus.WAITINGPLAYER);
        }

        repository.save(game);

        ws.convertAndSend(
                "/topic/games/" + gameId,
                Map.of(
                        "type", "playerLeft",
                        "playerId", playerId,
                        "newStatus", game.getStatus()
                )
        );
    }

}
