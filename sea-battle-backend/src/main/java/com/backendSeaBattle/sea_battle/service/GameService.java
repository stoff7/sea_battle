/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.service;

import com.backendSeaBattle.sea_battle.controllers.dto.CellCoords;
import com.backendSeaBattle.sea_battle.models.entity.Cell;
import com.backendSeaBattle.sea_battle.models.entity.Game;
import com.backendSeaBattle.sea_battle.models.entity.User;
import com.backendSeaBattle.sea_battle.models.enums.CellState;
import com.backendSeaBattle.sea_battle.models.enums.GameStatus;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.backendSeaBattle.sea_battle.repository.GameRepository;
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
    private final SimpMessagingTemplate ws;

    public Game startGame(User user) {
        Game game = new Game(user, null, GameStatus.WAITINGPLAYER, user.getUser_id(), LocalDateTime.now(), null);
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
                Map.of("type", "playerJoined", "playerId", user.getUser_id())
        );

        return new JoinGameResult(game.getGame_id(), user.getUser_id());

    }

    public record JoinGameResult(Long gameId, Long playerId) {

    }

    public record ReadyGameResult(GameStatus gameStatus, boolean firstOwnerReady, boolean secondOwnerReady) {

    }

    @Transactional
    public ReadyGameResult readyGame(Long gameId, Long PlayerId, boolean Ready, List<CellCoords> Cells) {
        User user = userService.findById(PlayerId).orElseThrow(() -> new EntityNotFoundException("User not found: " + PlayerId));
        Game game = repository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("Game not found: " + gameId));
        cellService.clearCellsForPlayer(game, user);
        if (Ready) {
            for (int i = 0; i < Cells.size(); i++) {
                if (Cells.get(i).getX() >= 0 && (Cells.get(i).getX()) <= 9 && Cells.get(i).getY() >= 0 && (Cells.get(i).getY()) <= 9) {
                    cellService.setCellIn(game, user, Cells.get(i).getX(), Cells.get(i).getY());
                }
            }

            user.setReady(true);
            userService.save(user);

            if (game.getSecondOwner().isReady() == game.getFirstOwner().isReady()) {
                game.setStatus(GameStatus.ACTIVE);
                repository.save(game);
                return new ReadyGameResult(game.getStatus(), game.getFirstOwner().isReady(), game.getSecondOwner().isReady());
            }

        } else {
            user.setReady(false);
            userService.save(user);
        }

        return new ReadyGameResult(game.getStatus(), game.getFirstOwner().isReady(), game.getSecondOwner().isReady());

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

        if (targetCell.isPresent() && targetCell.get().getStatus() == CellState.SHIP) {
            Cell cell = targetCell.get();
            cell.setStatus(CellState.HIT);
            cellService.save(cell);
            resultState = CellState.HIT;
        } else {
            // промах — создаём запись MISS
            Cell miss = new Cell(game, defender, coord.getX(), coord.getY(), CellState.MISS);
            cellService.save(miss);
            resultState = CellState.MISS;
        }

        Long nextPlayerId = defender.getUser_id();
        game.setCurrentTurn(nextPlayerId);
        repository.save(game);

        // 6) Вернуть результат
        return new FightResult(playerId, coord, resultState, nextPlayerId);
    }

    public record FightResult(Long playerId, CellCoords coord, CellState State, Long nextPlayerId) {

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
            cellService.deleteAllByGameAndOwner(game, user); 
            cellService.deleteAllByGameAndOwner(game, defender); 

        }
        
        return game.getStatus();
    }

}
