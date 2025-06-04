/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.models.entity;

import com.backendSeaBattle.sea_battle.models.enums.ShipState;
import com.backendSeaBattle.sea_battle.models.enums.ShipType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;

/**
 *
 * @author Александра
 */
@NoArgsConstructor
@Entity
@Table(name = "ship")
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long ship_id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "current_game",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ship_game")
    )
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ship_owner",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ship_owner")
    )
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "ship_state", nullable = false)
    private ShipState status;

    @Enumerated(EnumType.STRING)
    @Column(name = "ship_type", nullable = false)
    private ShipType type;

    // связь с клетками
    @OneToMany(mappedBy = "ship", cascade = CascadeType.ALL, orphanRemoval = true)
  
    private List<Cell> cells = new ArrayList<>();

    public Long getShip_id() {
        return ship_id;
    }

    public void setShip_id(Long ship_id) {
        this.ship_id = ship_id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public ShipState getStatus() {
        return status;
    }

    public void setStatus(ShipState status) {
        this.status = status;
    }

    public ShipType getType() {
        return type;
    }

    public void setType(ShipType type) {
        this.type = type;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public void setCells(List<Cell> cells) {
        this.cells = cells;
    }

}
