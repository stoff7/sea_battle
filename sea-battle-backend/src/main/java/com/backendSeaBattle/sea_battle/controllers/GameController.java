/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.controllers;

import com.backendSeaBattle.sea_battle.controllers.dto.FightRequest;
import com.backendSeaBattle.sea_battle.controllers.dto.FightResponse;
import com.backendSeaBattle.sea_battle.controllers.dto.JoinGameRequest;
import com.backendSeaBattle.sea_battle.controllers.dto.JoinGameResponse;
import com.backendSeaBattle.sea_battle.controllers.dto.ReadyGameRequest;
import com.backendSeaBattle.sea_battle.controllers.dto.ReadyGameResponse;
import com.backendSeaBattle.sea_battle.models.entity.Game;
import com.backendSeaBattle.sea_battle.models.entity.User;
import java.util.Map;
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

import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Map<String, Long>> startGame(@RequestBody Map<String, String> body ) {
        String userName = body.get("userName");
        User user = userService.startGame(userName);
        Long playerId = user.getUser_id();
        Game game = gameService.startGame(user);
        Long gameId = game.getGame_id();
        
        Map<String, Long> resp = Map.of(
                "playerId", playerId,
                "gameId", gameId
        );

        return ResponseEntity.ok(resp);
        
    }
    
    @PostMapping("/join_game")
    public ResponseEntity<JoinGameResponse> joinGame(@RequestBody @Valid JoinGameRequest req) {

        Long gameId = req.getGameId();
        
        try {
            var result = gameService.joinGame(gameId, req.getUserName());
            return ResponseEntity.ok(
              new JoinGameResponse(result.gameId(), result.playerId())
            );
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body(new JoinGameResponse(null, null, ex.getMessage()));
        }
        
    }
    
    @PostMapping("/{gameId}/ready_game")
    public ResponseEntity<ReadyGameResponse> readyGame(
            @PathVariable Long gameId, 
            @RequestBody @Valid ReadyGameRequest req
    ){
        var result = gameService.readyGame(
                gameId, req.getPlayerId(), req.getReady(), req.getCells()
        );
        // result содержит: GameStatus, флаги готовности каждого
        ReadyGameResponse resp = new ReadyGameResponse(
                result.gameStatus(),
                result.firstOwnerReady(), 
                result.secondOwnerReady()
        );

        
            return ResponseEntity.ok(resp); 
    }
    
    @PostMapping("/{gameId}/fight")
    public ResponseEntity <FightResponse> fight (
            @PathVariable Long gameId, 
            @RequestBody @Valid FightRequest req
    
    ){
        return ResponseEntity.ok(resp)
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
    
    
    
    
}
