package com.backendSeaBattle.sea_battle.service.patterns;

import com.backendSeaBattle.sea_battle.models.entity.Cell;
import com.backendSeaBattle.sea_battle.models.entity.Game;
import com.backendSeaBattle.sea_battle.models.entity.Ship;
import com.backendSeaBattle.sea_battle.models.entity.User;
import com.backendSeaBattle.sea_battle.models.enums.CellState;
import com.backendSeaBattle.sea_battle.models.enums.ShipState;
import com.backendSeaBattle.sea_battle.models.enums.ShipType;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Простые тесты для ShipFactory и CellFactory (Factory Method).
 */
class FactoryTest {

    private final ShipFactory shipFactory = new ShipFactory();
    private final CellFactory cellFactory = new CellFactory();

@Test
void shipFactory_shouldCreateShipWithNotTouchedStatus() {
    Game game = new Game();
    game.setGame_id(1L);
    User user = new User();
    user.setUser_id(2L);

    Cell cell1 = new Cell();
    cell1.setX(0);
    cell1.setY(0);

    Cell cell2 = new Cell();
    cell2.setX(0);
    cell2.setY(1);

    List<Cell> cells = List.of(cell1, cell2);


    Ship ship = shipFactory.create(game, user, ShipType.TWO_DECK, cells);

   
    assertThat(ship.getGame()).isEqualTo(game);
    assertThat(ship.getOwner()).isEqualTo(user);
    assertThat(ship.getType()).isEqualTo(ShipType.TWO_DECK);
    assertThat(ship.getStatus()).isEqualTo(ShipState.NOT_TOUCHED);
    assertThat(ship.getShip_id()).isNull();


    assertThat(ship.getCells()).containsExactlyInAnyOrder(cell1, cell2);

    
    assertThat(cell1.getShip()).isEqualTo(ship);
    assertThat(cell2.getShip()).isEqualTo(ship);
}


    @Test
    void cellFactory_shouldCreateShipCellWithCorrectFields() {
        Game game = new Game();
        game.setGame_id(5L);
        User user = new User();
        user.setUser_id(6L);
        Ship ship = new Ship();
        ship.setShip_id(10L);

        Cell cell = cellFactory.createShipCell(game, user, 4, 7, ship);

        assertThat(cell.getGame()).isEqualTo(game);
        assertThat(cell.getOwner()).isEqualTo(user);
        assertThat(cell.getX()).isEqualTo(4);
        assertThat(cell.getY()).isEqualTo(7);
        assertThat(cell.getStatus()).isEqualTo(CellState.SHIP);
        assertThat(cell.getShip()).isEqualTo(ship);
    }

    @Test
    void cellFactory_shouldCreateMissCellWithMissState() {
        Game game = new Game();
        game.setGame_id(8L);
        User user = new User();
        user.setUser_id(9L);

        Cell cell = cellFactory.createMissCell(game, user, 1, 2);

        assertThat(cell.getGame()).isEqualTo(game);
        assertThat(cell.getOwner()).isEqualTo(user);
        assertThat(cell.getX()).isEqualTo(1);
        assertThat(cell.getY()).isEqualTo(2);
        assertThat(cell.getStatus()).isEqualTo(CellState.MISS);
        assertThat(cell.getShip()).isNull(); // у miss-Cell не должно быть привязанного ship
    }
}
