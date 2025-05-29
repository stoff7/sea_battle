package com.backendSeaBattle.sea_battle.controllers.dto;

import com.backendSeaBattle.sea_battle.models.enums.ShipType;
import java.util.List;

public class ShipRequest {
    private ShipType type;
    private List<CellCoords> cells;

    public ShipRequest() {}

    public ShipRequest(ShipType type, List<CellCoords> cells) {
        this.type = type;
        this.cells = cells;
    }

    public ShipType getType() {
        return type;
    }

    public void setType(ShipType type) {
        this.type = type;
    }

    public List<CellCoords> getCells() {
        return cells;
    }

    public void setCells(List<CellCoords> cells) {
        this.cells = cells;
    }
}
