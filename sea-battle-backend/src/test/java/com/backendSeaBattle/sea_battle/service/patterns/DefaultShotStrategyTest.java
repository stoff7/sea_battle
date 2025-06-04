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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для DefaultShotStrategy (паттерн Strategy).
 */
@ExtendWith(MockitoExtension.class)
class DefaultShotStrategyTest {

    @Mock private CellService cellService;
    @Mock private ShipRepository shipRepository;
    @InjectMocks private DefaultShotStrategy shotStrategy;

    private Game fakeGame;
    private User shooter;
    private User defender;
    private CellCoords coords;

    @BeforeEach
    void setup() {
        // создаём минимальный «пустой» Game, две User и координаты (x=3,y=5)
        fakeGame = new Game();
        shooter = new User();
        shooter.setUser_id(100L);
        defender = new User();
        defender.setUser_id(200L);
        coords = new CellCoords(3, 5);
    }

    @Test
    void evaluateShot_whenNoCellExists_shouldReturnMiss() {
        // 1) Настроим cellService.findByGameAndOwnerAndXAndY → Optional.empty()
        when(cellService.findByGameAndOwnerAndXAndY(fakeGame, defender, 3, 5))
                .thenReturn(Optional.empty());

        // 2) Вызов стратегии
        ShotStrategy.ShotResult result = shotStrategy.evaluateShot(fakeGame, shooter, defender, coords);

        // 3) Проверяем, что cellService.save(...) вызвали один раз с новой Cell(x=3,y=5,state=MISS)
        //    Мы не видим прямой Cell, проверим через ArgumentCaptor:
        ArgumentCaptor<Cell> captor = ArgumentCaptor.forClass(Cell.class);
        verify(cellService, times(1)).save(captor.capture());
        Cell saved = captor.getValue();

        assertThat(saved.getGame()).isEqualTo(fakeGame);
        assertThat(saved.getOwner()).isEqualTo(defender);
        assertThat(saved.getX()).isEqualTo(3);
        assertThat(saved.getY()).isEqualTo(5);
        assertThat(saved.getStatus()).isEqualTo(CellState.MISS);

        // 4) Результат: MISS и следующий ход — defender
        assertThat(result.cellState).isEqualTo(CellState.MISS);
        assertThat(result.shipState).isNull();
        assertThat(result.nextPlayerId).isEqualTo(defender.getUser_id());
    }

    @Test
    void evaluateShot_whenHitAndKillShip_shouldReturnHitKill() {
        // 1) Создадим мок Cell, который помечен SHIP
        Ship ship = new Ship();
        ship.setShip_id(555L);
        ship.setStatus(ShipState.NOT_TOUCHED);

        // создаём «другие клетки» корабля — все уже HIT, чтобы текущее попадание было «последнее»
        Cell otherCell = new Cell();
        otherCell.setCell_id(1L);
        otherCell.setStatus(CellState.HIT);
        // В реальности ship.getCells() → список из других клеток + наша «целевое»
        ship.setCells(Collections.singletonList(otherCell));

        // настраиваем cellService.findByGameAndOwnerAndXAndY → Optional.of(targetCell)
        Cell targetCell = Mockito.spy(new Cell());
        targetCell.setCell_id(2L);
        targetCell.setStatus(CellState.SHIP);
        targetCell.setShip(ship);

        when(cellService.findByGameAndOwnerAndXAndY(fakeGame, defender, 3, 5))
                .thenReturn(Optional.of(targetCell));

        // когда сохраняем клетку — пусть cellService.save возвращает тот же объект
        doAnswer(invocation -> {
            Cell arg = invocation.getArgument(0, Cell.class);
            // допустим, как будто СУБД уже присвоила статус HIT
            return arg;
        }).when(cellService).save(any(Cell.class));

        // настраиваем shipRepository.findById → Optional.of(ship)
        when(shipRepository.findById(555L)).thenReturn(Optional.of(ship));

        // 2) Вызов стратегии
        ShotStrategy.ShotResult result = shotStrategy.evaluateShot(fakeGame, shooter, defender, coords);

        // 3) Проверки:
        // 3.1. Статус targetCell должен был измениться на HIT
        assertThat(targetCell.getStatus()).isEqualTo(CellState.HIT);

        // 3.2. Статус корабля — KILL (поскольку больше не осталось SHIP)
        assertThat(ship.getStatus()).isEqualTo(ShipState.KILL);

        // 3.3. Возвращён correct ShotResult
        assertThat(result.cellState).isEqualTo(CellState.HIT);
        assertThat(result.shipState).isEqualTo(ShipState.KILL);
        // shooter сохраняет право хода (nextPlayerId == shooter.user_id)
        assertThat(result.nextPlayerId).isEqualTo(shooter.getUser_id());
    }
}
