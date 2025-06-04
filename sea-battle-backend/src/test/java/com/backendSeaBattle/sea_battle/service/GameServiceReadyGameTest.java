package com.backendSeaBattle.sea_battle.service;

import com.backendSeaBattle.sea_battle.controllers.dto.ReadyGameRequest;
import com.backendSeaBattle.sea_battle.models.entity.Game;
import com.backendSeaBattle.sea_battle.models.entity.User;
import com.backendSeaBattle.sea_battle.models.enums.GameStatus;
import com.backendSeaBattle.sea_battle.repository.GameRepository;
import com.backendSeaBattle.sea_battle.repository.ShipRepository;
import com.backendSeaBattle.sea_battle.service.patterns.CellFactory;
import com.backendSeaBattle.sea_battle.service.patterns.GameEvent;
import com.backendSeaBattle.sea_battle.service.patterns.GameEventPublisher;
import com.backendSeaBattle.sea_battle.service.patterns.ShipFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceReadyGameTest {

    @Mock private GameRepository gameRepository;
    @Mock private UserService userService;
    @Mock private CellService cellService;
    @Mock private ShipRepository shipRepository;
    @Mock private CellFactory cellFactory;
    @Mock private ShipFactory shipFactory;
    @Mock private GameEventPublisher eventPublisher;

    @InjectMocks private GameService gameService;

    private Game existingGame;
    private User firstOwner;
    private User secondOwner;

    @BeforeEach
    void setup() {
        firstOwner = new User();
        firstOwner.setUser_id(1L);
        firstOwner.setUser_name("Host");
        firstOwner.setReady(true); // допустим, первый уже готов

        secondOwner = new User();
        secondOwner.setUser_id(2L);
        secondOwner.setUser_name("Guest");

        existingGame = new Game();
        existingGame.setGame_id(10L);
        existingGame.setFirstOwner(firstOwner);
        existingGame.setSecondOwner(null);
        existingGame.setStatus(GameStatus.WAITINGPLAYER);
        existingGame.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void readyGame_whenSecondOwnerBecomesReady_andFirstIsReady_shouldActivateGame() {
        ReadyGameRequest req = new ReadyGameRequest();
        when(userService.findById(2L)).thenReturn(Optional.of(secondOwner));
        when(gameRepository.findById(10L)).thenReturn(Optional.of(existingGame));

        req.setPlayerId(2L);
        req.setReady(true);
        req.setShips(List.of());

        existingGame.setSecondOwner(secondOwner);

        GameService.ReadyGameResult result = gameService.readyGame(10L, req);

        assertThat(secondOwner.isReady()).isTrue();
        assertThat(existingGame.getStatus()).isEqualTo(GameStatus.ACTIVE);

        verify(gameRepository, times(1)).save(existingGame);

        ArgumentCaptor<GameEvent> captor = ArgumentCaptor.forClass(GameEvent.class);
        verify(eventPublisher, times(2)).publish(captor.capture());

        GameEvent evt1 = captor.getAllValues().get(0);
        assertThat(evt1.getGameId()).isEqualTo(10L);
        assertThat(evt1.getEventType()).isEqualTo("playerReady");
        assertThat(evt1.getPayload()).containsEntry("playerId", 2L);
        assertThat(evt1.getPayload()).containsEntry("firstOwnerReady", true);
        assertThat(evt1.getPayload()).containsEntry("secondOwnerReady", true);
        assertThat(evt1.getPayload()).containsEntry("gameStatus", GameStatus.ACTIVE);

        GameEvent evt2 = captor.getAllValues().get(1);
        assertThat(evt2.getGameId()).isEqualTo(10L);
        assertThat(evt2.getEventType()).isEqualTo("gameStarted");
        assertThat(evt2.getPayload()).isEmpty();

        assertThat(result.gameStatus()).isEqualTo(GameStatus.ACTIVE);
        assertThat(result.firstOwnerReady()).isTrue();
        assertThat(result.secondOwnerReady()).isTrue();
    }

    @Test
    void readyGame_whenGameNotFound_shouldThrow() {
        when(gameRepository.findById(999L)).thenReturn(Optional.empty());

        ReadyGameRequest req = new ReadyGameRequest();
        req.setPlayerId(2L);
        req.setReady(true);
        req.setShips(List.of());

        assertThatThrownBy(() -> gameService.readyGame(999L, req))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Game not found: 999");
    }

    @Test
    void readyGame_whenUserNotFound_shouldThrow() {
        when(gameRepository.findById(10L)).thenReturn(Optional.of(existingGame));
        when(userService.findById(5L)).thenReturn(Optional.empty());

        ReadyGameRequest req = new ReadyGameRequest();
        req.setPlayerId(5L);
        req.setReady(false);
        req.setShips(List.of());

        assertThatThrownBy(() -> gameService.readyGame(10L, req))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found: 5");
    }
}
