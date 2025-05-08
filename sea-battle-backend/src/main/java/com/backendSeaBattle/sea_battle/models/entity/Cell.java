/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.models.entity;

import com.backendSeaBattle.sea_battle.models.enums.CellState;
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
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/**
 *
 * @author Александра
 */
@NoArgsConstructor
@Entity
@Table(name = "cells")
public class Cell {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long cell_id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
      name = "current_game",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_ship_game")
    )
    private Game game;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
      name = "cell_owner",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_ship_owner")
    )
    private User owner;
    
    private int x; 
    private int y; 
    
    @Enumerated(EnumType.STRING)
    @Column(name = "cell_state", nullable = false)
    private CellState status;

    public Cell(Game game, User owner, int x, int y, CellState status) {
        this.game = game;
        this.owner = owner;
        this.x = x;
        this.y = y;
        this.status = status;
    }

    public Long getCell_id() {
        return cell_id;
    }

    public void setCell_id(Long cell_id) {
        this.cell_id = cell_id;
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

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public CellState getStatus() {
        return status;
    }

    public void setStatus(CellState status) {
        this.status = status;
    }
    
    
    
    
    
}
