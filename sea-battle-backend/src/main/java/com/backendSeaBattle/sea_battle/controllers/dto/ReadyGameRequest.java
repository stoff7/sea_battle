package com.backendSeaBattle.sea_battle.controllers.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ReadyGameRequest {
    @NotNull
    private Long playerId;

    @NotNull
    private Boolean ready;

    // Новый список кораблей с типом и клетками
    private List<ShipRequest> ships;

    public ReadyGameRequest(Long playerId, Boolean ready, List<ShipRequest> ships) {
        this.playerId = playerId;
        this.ready = ready;
        this.ships = ships;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public Boolean getReady() {
        return ready;
    }

    public void setReady(Boolean ready) {
        this.ready = ready;
    }

    public List<ShipRequest> getShips() {
        return ships;
    }

    public void setShips(List<ShipRequest> ships) {
        this.ships = ships;
    }
}
