package com.backendSeaBattle.sea_battle.service;

import com.backendSeaBattle.sea_battle.models.entity.Game;
import com.backendSeaBattle.sea_battle.models.entity.User;
import com.backendSeaBattle.sea_battle.models.enums.GameStatus;
import com.backendSeaBattle.sea_battle.models.enums.GameType;
import com.backendSeaBattle.sea_battle.repository.GameRepository;
import com.backendSeaBattle.sea_battle.repository.ShipRepository;
import com.backendSeaBattle.sea_battle.service.patterns.GameEvent;
import com.backendSeaBattle.sea_battle.service.patterns.GameEventPublisher;
import com.backendSeaBattle.sea_battle.service.patterns.ShotStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для GameService.joinGame(...)
 */
@ExtendWith(MockitoExtension.class)
class GameServiceJoinGameTest {

    @Mock private GameRepository gameRepository;
    @Mock private UserService userService;
    @Mock private CellService cellService;       // не задействован в joinGame, но нужен для конструктора
    @Mock private ShipRepository shipRepository; // тоже не нужен напрямую
    @Mock private ShotStrategy shotStrategy;     // не нужен
    @Mock private GameEventPublisher eventPublisher;

    @InjectMocks private GameService gameService;

    private Game existingGame;

    @BeforeEach
    void setup() {
        // Создаём «ожидающую» игру, но НЕ настраиваем gameRepository.findById(...) здесь
        existingGame = new Game();
        existingGame.setGame_id(10L);
        existingGame.setFirstOwner(new User());
        existingGame.getFirstOwner().setUser_id(1L);
        existingGame.getFirstOwner().setUser_name("HostUser");
        existingGame.setStatus(GameStatus.WAITINGPLAYER);
        existingGame.setGameType(GameType.OPEN);
        existingGame.setCurrentTurn(existingGame.getFirstOwner().getUser_id());
        existingGame.setCreatedAt(LocalDateTime.now());

        // здесь не делаем any-мока типа when(gameRepository.findById(anyLong()))…
    }

    @Test
    void joinGame_happyPath_changesStatusAndPublishesEvent() {
        // 1) Настроим: gameRepository.findById(10L) → Optional.of(existingGame)
        when(gameRepository.findById(10L)).thenReturn(Optional.of(existingGame));

        // 2) Настроим userService.startGame("NewPlayer") → новый юзер
        User newUser = new User();
        newUser.setUser_id(20L);
        newUser.setUser_name("NewPlayer");
        when(userService.startGame("NewPlayer")).thenReturn(newUser);

        // 3) Вызов метода
        GameService.JoinGameResult result = gameService.joinGame(10L, "NewPlayer");

        // 4) Проверяем, что у existingGame теперь установлен secondOwner и статус WAITINGREADY
        assertThat(existingGame.getSecondOwner()).isEqualTo(newUser);
        assertThat(existingGame.getStatus()).isEqualTo(GameStatus.WAITINGREADY);

        // 5) Проверяем, что repository.save(...) вызвался с обновлённой игрой
        verify(gameRepository, times(1)).save(existingGame);

        // 6) Проверяем, что eventPublisher.publish(...) был вызван ровно один раз
        @SuppressWarnings("unchecked")
        ArgumentCaptor<GameEvent> eventCaptor = ArgumentCaptor.forClass(GameEvent.class);
        verify(eventPublisher, times(1)).publish(eventCaptor.capture());
        GameEvent publishedEvent = eventCaptor.getValue();

        // 6.1. Проверяем поля события
        assertThat(publishedEvent.getGameId()).isEqualTo(10L);
        assertThat(publishedEvent.getEventType()).isEqualTo("playerJoined");
        assertThat(publishedEvent.getPayload().get("playerId")).isEqualTo(newUser.getUser_id());
        assertThat(publishedEvent.getPayload().get("userName")).isEqualTo(newUser.getUser_name());

        // 7) Проверяем возвращённый JoinGameResult
        assertThat(result.gameId()).isEqualTo(10L);
        assertThat(result.playerId()).isEqualTo(20L);
        assertThat(result.HostName()).isEqualTo("HostUser");
        assertThat(result.HostId()).isEqualTo(1L);
    }

    @Test
    void joinGame_whenGameNotWaiting_throwsIllegalState() {
        // Переконфигурируем status → ACTIVE (не WAITINGPLAYER)
        existingGame.setStatus(GameStatus.ACTIVE);

        // Настроим gameRepository.findById(99L) → Optional.of(existingGame)
        when(gameRepository.findById(99L)).thenReturn(Optional.of(existingGame));

        assertThatThrownBy(() -> gameService.joinGame(99L, "Someone"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Нельзя присоединиться, статус игры:");

        // eventPublisher.publish не должен вызываться
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void joinGame_whenGameNotFound_throwsEntityNotFound() {
        // Настроим: нет игры с id=123
        when(gameRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.joinGame(123L, "Nobody"))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Game not found: 123");

        verify(eventPublisher, never()).publish(any());
    }
}
