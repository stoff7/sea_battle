/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.controllers;

import com.backendSeaBattle.sea_battle.controllers.dto.ChatMessageRequest;
import com.backendSeaBattle.sea_battle.controllers.dto.ChatMessageResponse;
import com.backendSeaBattle.sea_battle.controllers.dto.FightRequest;
import com.backendSeaBattle.sea_battle.controllers.dto.FightResponse;
import com.backendSeaBattle.sea_battle.controllers.dto.JoinGameRequest;
import com.backendSeaBattle.sea_battle.controllers.dto.JoinGameResponse;
import com.backendSeaBattle.sea_battle.controllers.dto.JoinRandomGameRequest;
import com.backendSeaBattle.sea_battle.controllers.dto.JoinRandomGameResponse;
import com.backendSeaBattle.sea_battle.controllers.dto.LeaveGameRequest;
import com.backendSeaBattle.sea_battle.controllers.dto.LeaveGameResponse;
import com.backendSeaBattle.sea_battle.controllers.dto.ReadyGameRequest;
import com.backendSeaBattle.sea_battle.controllers.dto.ReadyGameResponse;
import com.backendSeaBattle.sea_battle.controllers.dto.ReplayGameRequest;
import com.backendSeaBattle.sea_battle.controllers.dto.ReplayGameResponse;
import com.backendSeaBattle.sea_battle.controllers.dto.StartGameRequest;
import com.backendSeaBattle.sea_battle.controllers.dto.StartGameResponse;
import com.backendSeaBattle.sea_battle.models.entity.Game;
import com.backendSeaBattle.sea_battle.models.entity.User;
import com.backendSeaBattle.sea_battle.models.enums.GameStatus;
import com.backendSeaBattle.sea_battle.models.enums.GameType;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.backendSeaBattle.sea_battle.service.GameService;
import com.backendSeaBattle.sea_battle.service.CellService;
import com.backendSeaBattle.sea_battle.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 *
 * @author Александра
 */
@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor

public class GameController {

    private final GameService gameService;
    private final UserService userService;
    private final CellService cellService;

    @PostMapping("/start_game")
    public ResponseEntity<StartGameResponse> startGame(@RequestBody @Valid StartGameRequest req) {
        User user = userService.startGame(req.getUserName());
        Long playerId = user.getUser_id();
        Game game = gameService.startGame(user, req.getGameType());
        Long gameId = game.getGame_id();

        StartGameResponse resp = new StartGameResponse(playerId, gameId);
        return ResponseEntity.ok(resp);

    }

    @PostMapping("/join_random_game")
    public ResponseEntity<JoinRandomGameResponse> joinRandomGame(
            @RequestBody @Valid JoinRandomGameRequest req) {

        Optional<Game> optGame = gameService.findOpenGame();
        if (optGame.isEmpty()) {
            User user = userService.startGame(req.getUserName());
            Long playerId = user.getUser_id();
            Game game = gameService.startGame(user, GameType.OPEN);
            Long gameId = game.getGame_id();

            JoinRandomGameResponse resp = new JoinRandomGameResponse(gameId, playerId, null, null, null);
            return ResponseEntity.ok(resp);

        } else {
            Long gameId = optGame.get().getGame_id();

            try {
                var result = gameService.joinGame(gameId, req.getUserName());
                return ResponseEntity.ok(
                        new JoinRandomGameResponse(result.gameId(), result.playerId(), result.HostName(), result.HostId(), null)
                );
            } catch (EntityNotFoundException ex) {
                return ResponseEntity.notFound().build();

            } catch (IllegalStateException ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new JoinRandomGameResponse(null, null, null, null, ex.getMessage()));
            }
        }

    }

    @PostMapping("/join_game")
    public ResponseEntity<JoinGameResponse> joinGame(@RequestBody @Valid JoinGameRequest req) {

        Long gameId = req.getGameId();

        try {
            var result = gameService.joinGame(gameId, req.getUserName());
            return ResponseEntity.ok(
                    new JoinGameResponse(result.gameId(), result.playerId(), result.HostName(), result.HostId())
            );
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new JoinGameResponse(null, null, null, null, ex.getMessage()));
        }

    }

    @PostMapping("/{gameId}/ready_game")
    public ResponseEntity<ReadyGameResponse> readyGame(
            @PathVariable Long gameId,
            @RequestBody @Valid ReadyGameRequest req
    ) {
        try {
            var result = gameService.readyGame(
                    gameId, req
            );
            // result содержит: GameStatus, флаги готовности каждого
            ReadyGameResponse resp = new ReadyGameResponse(
                    result.gameStatus(),
                    result.firstOwnerReady(),
                    result.secondOwnerReady()
            );

            return ResponseEntity.ok(resp);

        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ReadyGameResponse(ex.getMessage()));
        }

    }

    @PatchMapping("/{gameId}/fight")
    public ResponseEntity<FightResponse> fight(
            @PathVariable Long gameId,
            @RequestBody @Valid FightRequest req
    ) {

        try {
            var result = gameService.fight(
                    gameId, req.getPlayerId(), req.getCoord());

            GameStatus gameStatus = gameService.endGame(gameId, req.getPlayerId());

            FightResponse resp = new FightResponse(result.playerId(), result.coord(), result.State(), result.nextPlayerId(), gameStatus, result.resultShipState(), result.resultShipType(), result.resultShipCoords());
            return ResponseEntity.ok(resp);

        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new FightResponse(null, null, null, null, null, ex.getMessage(), null, null, null));
        }
    }

    @PostMapping("/{gameId}/leave_game")
    public ResponseEntity<LeaveGameResponse> leaveGame(
            @PathVariable Long gameId,
            @RequestBody @Valid LeaveGameRequest req
    ) {

        try {
            gameService.leaveGame(gameId, req.getPlayerId());

            LeaveGameResponse resp = new LeaveGameResponse(null);
            return ResponseEntity.ok(resp);

        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new LeaveGameResponse(null));
        }

    }

    @PostMapping("/{gameId}/chat_message")
    public ResponseEntity<ChatMessageResponse> chatMessage(@PathVariable Long gameId, @RequestBody @Valid ChatMessageRequest req) {

        try {
            gameService.sendMessage(gameId, req.getPlayerId(), req.getTextMessage());
            return ResponseEntity.ok().build();

        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ChatMessageResponse(ex.getMessage()));
        }
    }

    @PatchMapping("/{gameId}/replay_game")
    public ResponseEntity<ReplayGameResponse> replayGame(@PathVariable Long gameId, @RequestBody @Valid ReplayGameRequest req) {

        try {
            var result = gameService.replayGame(gameId, req.getPlayerId());

            ReplayGameResponse resp = new ReplayGameResponse(result.gameId(), result.playerId(), result.gameStatus(), result.hostName(), result.hostId(), null);
            return ResponseEntity.ok(resp);

        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ReplayGameResponse(null, null, null, null, null, ex.getMessage()));
        }
    }
}
// Request:
// gameId
// playerId
// ready: TRUE, FALSE 
// ships [[], [], ]
// Response: 
// gameStatus: WAITINGREADY, ACTIVE
// firstOwnerReady: TRUE, FALSE 
// secondOwnerReady: TRUE, FALSE 
// XY - СТОЛБЕЦ, СТРОКА
// ПРИ ready: TRUE
// проверка на валидность 
// ввод в базу данных
// изменение статуса готовности игрока
// сравнение статусов 
// при совпадении меняю GAMESTATUS
// при ready false 
// обнуляю расположение для игрока 
// меняю статус готовности игрока 

