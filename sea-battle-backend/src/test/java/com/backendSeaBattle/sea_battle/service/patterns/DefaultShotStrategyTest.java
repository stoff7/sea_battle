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
        
        fakeGame = new Game();
        shooter = new User();
        shooter.setUser_id(100L);
        defender = new User();
        defender.setUser_id(200L);
        coords = new CellCoords(3, 5);
    }

    @Test
    void evaluateShot_whenNoCellExists_shouldReturnMiss() {
        
        when(cellService.findByGameAndOwnerAndXAndY(fakeGame, defender, 3, 5))
                .thenReturn(Optional.empty());

        ShotStrategy.ShotResult result = shotStrategy.evaluateShot(fakeGame, shooter, defender, coords);


        ArgumentCaptor<Cell> captor = ArgumentCaptor.forClass(Cell.class);
        verify(cellService, times(1)).save(captor.capture());
        Cell saved = captor.getValue();

        assertThat(saved.getGame()).isEqualTo(fakeGame);
        assertThat(saved.getOwner()).isEqualTo(defender);
        assertThat(saved.getX()).isEqualTo(3);
        assertThat(saved.getY()).isEqualTo(5);
        assertThat(saved.getStatus()).isEqualTo(CellState.MISS);


        assertThat(result.cellState).isEqualTo(CellState.MISS);
        assertThat(result.shipState).isNull();
        assertThat(result.nextPlayerId).isEqualTo(defender.getUser_id());
    }

    @Test
    void evaluateShot_whenHitAndKillShip_shouldReturnHitKill() {
      
        Ship ship = new Ship();
        ship.setShip_id(555L);
        ship.setStatus(ShipState.NOT_TOUCHED);

 
        Cell otherCell = new Cell();
        otherCell.setCell_id(1L);
        otherCell.setStatus(CellState.HIT);

        ship.setCells(Collections.singletonList(otherCell));

        Cell targetCell = Mockito.spy(new Cell());
        targetCell.setCell_id(2L);
        targetCell.setStatus(CellState.SHIP);
        targetCell.setShip(ship);

        when(cellService.findByGameAndOwnerAndXAndY(fakeGame, defender, 3, 5))
                .thenReturn(Optional.of(targetCell));

       
        doAnswer(invocation -> {
            Cell arg = invocation.getArgument(0, Cell.class);
           
            return arg;
        }).when(cellService).save(any(Cell.class));


        when(shipRepository.findById(555L)).thenReturn(Optional.of(ship));


        ShotStrategy.ShotResult result = shotStrategy.evaluateShot(fakeGame, shooter, defender, coords);

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
