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
import com.backendSeaBattle.sea_battle.repository.GameRepository;
import com.backendSeaBattle.sea_battle.repository.ShipRepository;
import com.backendSeaBattle.sea_battle.service.patterns.CellFactory;
import com.backendSeaBattle.sea_battle.service.patterns.GameEvent;
import com.backendSeaBattle.sea_battle.service.patterns.GameEventPublisher;
import com.backendSeaBattle.sea_battle.service.patterns.ShipFactory;
import com.backendSeaBattle.sea_battle.service.patterns.ShotStrategy;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GameService с добавлением трёх паттернов (Factory, Strategy, Observer): –
 * ShipFactory, CellFactory (Factory Method) – ShotStrategy (Strategy) –
 * GameEventPublisher + WebSocketGameEventListener (Observer)
 *
 * При этом исходная логика и все ваши методы полностью сохранены и только
 * немного «подменены» на новые бины.
 */
@Service
@AllArgsConstructor
public class GameService {

    private final GameRepository repository;
    private final UserService userService;
    private final CellService cellService;
    private final ShipRepository shipRepository;

    // ======== Новые зависимости ========
    private final ShipFactory shipFactory;
    private final CellFactory cellFactory;
    private final ShotStrategy shotStrategy;
    private final GameEventPublisher eventPublisher;
    // ====================================

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
        Game game = repository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found: " + gameId));

        if (game.getStatus() != GameStatus.WAITINGPLAYER) {
            throw new IllegalStateException("Нельзя присоединиться, статус игры: " + game.getStatus());
        }

        User user = userService.startGame(UserName);
        game.setSecondOwner(user);
        game.setStatus(GameStatus.WAITINGREADY);
        repository.save(game);

        // Вместо ws.convertAndSend → публикуем событие
        eventPublisher.publish(new GameEvent(
                game.getGame_id(),
                "playerJoined",
                Map.of("playerId", user.getUser_id(), "userName", user.getUser_name())
        ));

        return new JoinGameResult(game.getGame_id(), user.getUser_id(), game.getFirstOwner().getUser_name(), game.getFirstOwner().getUser_id());
    }

    public record JoinGameResult(Long gameId, Long playerId, String HostName, Long HostId) {

    }

    public record ReadyGameResult(GameStatus gameStatus, boolean firstOwnerReady, boolean secondOwnerReady) {

    }

    @Transactional
    public ReadyGameResult readyGame(Long gameId, ReadyGameRequest req) {
        // 1) Загрузить game и user
        Game game = repository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found: " + gameId));
        User user = userService.findById(req.getPlayerId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + req.getPlayerId()));

        // 2) Удаляем старые корабли и клетки этого игрока
        shipRepository.deleteAllByGameAndOwner(game, user);
        cellService.clearCellsForPlayer(game, user);

        if (req.getReady()) {
            // 3) Сохраняем новые корабли и их клетки (через фабрики)
            for (ShipRequest s : req.getShips()) {
                List<Cell> cells = new ArrayList<>();

                for (CellCoords c : s.getCells()) {
                    if (c.getX() < 0 || c.getX() > 9 || c.getY() < 0 || c.getY() > 9) {
                        throw new IllegalStateException("Координата вне поля: " + c.getX() + "," + c.getY());
                    }
                    Cell cell = cellFactory.createShipCell(game, user, c.getX(), c.getY(), null); // временно null
                    cells.add(cell);
                }

                // После создания всех Cell, связываем их с Ship
                Ship ship = shipFactory.create(game, user, s.getType(), cells);
                for (Cell cell : cells) {
                    cell.setShip(ship); // установить связь обратно
                    cellService.save(cell);
                }

                shipRepository.save(ship);
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
        game.setStatus(first && second ? GameStatus.ACTIVE : GameStatus.WAITINGREADY);
        repository.save(game);

        // 5) Публикуем событие playerReady
        eventPublisher.publish(new GameEvent(
                game.getGame_id(),
                "playerReady",
                Map.of(
                        "playerId", user.getUser_id(),
                        "firstOwnerReady", first,
                        "secondOwnerReady", second,
                        "gameStatus", game.getStatus()
                )
        ));

        // 6) Если оба готовы — публикуем gameStarted
        if (game.getStatus() == GameStatus.ACTIVE) {
            eventPublisher.publish(new GameEvent(
                    game.getGame_id(),
                    "gameStarted",
                    Map.of()
            ));
        }

        // 7) Возвращаем результат
        return new ReadyGameResult(game.getStatus(), first, second);
    }

    public Optional<Game> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public FightResult fight(Long gameId, Long playerId, CellCoords coord) {
        // 1) Загрузить сущности
        User user = userService.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + playerId));
        Game game = repository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found: " + gameId));

        // 2) Проверить очередь
        if (!game.getCurrentTurn().equals(playerId)) {
            throw new IllegalStateException("Нельзя ходить, ход: " + game.getCurrentTurn());
        }

        // 3) Определить противника
        User defender = game.getFirstOwner().getUser_id().equals(playerId)
                ? game.getSecondOwner()
                : game.getFirstOwner();

        // 4) Оцениваем выстрел через стратегию
        ShotStrategy.ShotResult shotResult = shotStrategy.evaluateShot(game, user, defender, coord);

        // 5) Сохраняем новый currentTurn и game
        game.setCurrentTurn(shotResult.nextPlayerId);
        repository.save(game);

        // 6) Публикуем событие shotFired
        eventPublisher.publish(new GameEvent(
                game.getGame_id(),
                "shotFired",
                Map.of(
                        "by", playerId,
                        "x", coord.getX(),
                        "y", coord.getY(),
                        "result", shotResult.cellState.name(),
                        "nextPlayerId", shotResult.nextPlayerId
                )
        ));

        // 7) Возвращаем результат
        return new FightResult(playerId, coord, shotResult.cellState, shotResult.nextPlayerId, shotResult.shipState);
    }

    public record FightResult(Long playerId, CellCoords coord, CellState State, Long nextPlayerId, ShipState resultShipState) {

    }

    @Transactional
    public GameStatus endGame(Long gameId, Long playerId) {
        Game game = repository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found: " + gameId));
        User user = userService.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + playerId));

        User defender = game.getFirstOwner().getUser_id().equals(playerId)
                ? game.getSecondOwner()
                : game.getFirstOwner();

        if (!cellService.existsByGameAndOwnerAndStatus(game, defender, CellState.SHIP)) {
            game.setStatus(GameStatus.FINISHED);
            game.setFinishedAt(LocalDateTime.now());
            repository.save(game);
            cellService.clearCellsForPlayer(game, user);
            cellService.clearCellsForPlayer(game, defender);

            user.setReady(false);
            defender.setReady(false);

            // Публикуем событие gameFinished
            eventPublisher.publish(new GameEvent(
                    game.getGame_id(),
                    "gameFinished",
                    Map.of("winnerId", playerId)
            ));
        }

        return game.getStatus();
    }

    public Optional<Game> findOpenGame() {
        return repository.findFirstByGameTypeAndStatus(GameType.OPEN, GameStatus.WAITINGPLAYER);
    }

    @Transactional
    public void leaveGame(Long gameId, Long playerId) {
        Game game = repository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found: " + gameId));
        User user = userService.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + playerId));

        cellService.clearCellsForPlayer(game, user);
        shipRepository.deleteAllByGameAndOwner(game, user);

        boolean wasFirst = game.getFirstOwner().getUser_id().equals(playerId);
        boolean wasSecond = game.getSecondOwner() != null && game.getSecondOwner().getUser_id().equals(playerId);

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

        game.setCurrentTurn(game.getFirstOwner().getUser_id());
        repository.save(game);

        // Публикуем событие playerLeft
        eventPublisher.publish(new GameEvent(
                game.getGame_id(),
                "playerLeft",
                Map.of("playerId", playerId, "newStatus", game.getStatus())
        ));
    }

    public void sendMessage(Long gameId, Long playerId, String textMessage) {
        Game game = repository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found: " + gameId));
        User user = userService.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + playerId));

        if (!game.getFirstOwner().getUser_id().equals(playerId)
                && (game.getSecondOwner() == null || !game.getSecondOwner().getUser_id().equals(playerId))) {
            throw new IllegalStateException("User " + playerId + " is not part of game " + gameId);
        }

        User defender = game.getFirstOwner().getUser_id().equals(playerId)
                ? game.getSecondOwner()
                : game.getFirstOwner();

        // Публикуем событие chatMessage
        eventPublisher.publish(new GameEvent(
                game.getGame_id(),
                "chatMessage",
                Map.of(
                        "fromPlayer", user.getUser_name(),
                        "toPlayer", defender.getUser_name(),
                        "text", textMessage,
                        "timestamp", LocalDateTime.now().toString()
                )
        ));
    }

    public record ReplayGameResult(Long gameId, Long playerId, GameStatus gameStatus, String hostName, Long nextPlayerId, Long hostId) {

    }

    @Transactional
    public ReplayGameResult replayGame(Long gameId, Long playerId) {
        Game game = repository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found: " + gameId));
        User user = userService.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + playerId));

        // 1) Replay разрешён только после окончания игры
        if (game.getStatus() != GameStatus.FINISHED && game.getStatus() != GameStatus.WAITINGPLAYER) {
            throw new IllegalStateException("Cannot replay: game is not finished or waiting");
        }

        User second = game.getSecondOwner();
        boolean secondExists = (second != null);
        boolean secondIsRequester = secondExists && second.getUser_id().equals(playerId);

        if (game.getStatus() == GameStatus.WAITINGREADY) {
            throw new IllegalStateException("Cannot replay: another player has already joined");
        }

        // 3A) Если secondExists == true
        if (secondExists && secondIsRequester) {
            game.setFirstOwner(user);
            game.setSecondOwner(null);
            game.setStatus(GameStatus.WAITINGPLAYER);
            game.setFinishedAt(null);
            game.setCurrentTurn(user.getUser_id());
        } // 3B) Иначе (secondExists == false)
        else {
            game.setSecondOwner(user);
            game.setStatus(GameStatus.WAITINGREADY);
            eventPublisher.publish(new GameEvent(
                    game.getGame_id(),
                    "playerJoined",
                    Map.of("playerId", user.getUser_id(), "userName", user.getUser_name())
            ));
        }

        repository.save(game);

        return new ReplayGameResult(
                gameId,
                playerId,
                game.getStatus(),
                game.getFirstOwner().getUser_name(),
                game.getCurrentTurn(),
                game.getFirstOwner().getUser_id()
        );
    }
}
