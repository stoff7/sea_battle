/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.backendSeaBattle.sea_battle.models.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

import com.backendSeaBattle.sea_battle.models.enums.GameStatus;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


/**
 *
 * @author Александра
 */

@Entity
@Table(name = "games")
@AllArgsConstructor
@NoArgsConstructor

public class Game {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long game_id;

    public Game(User firstOwner, User secondOwner, GameStatus status, Long currentTurn, LocalDateTime createdAt, LocalDateTime finishedAt) {
        this.firstOwner = firstOwner;
        this.secondOwner = secondOwner;
        this.status = status;
        this.currentTurn = currentTurn;
        this.createdAt = createdAt;
        this.finishedAt = finishedAt;
    }
    


    // первый владелец
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
      name = "first_owner_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_game_first_owner")
    )
    private User firstOwner;

    // второй владелец
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
      name = "second_owner_id",
      nullable = true,
      foreignKey = @ForeignKey(name = "fk_game_second_owner")
    )
    private User secondOwner;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GameStatus status;
    
    @Column(name = "current_turn")
    private Long currentTurn;

//    @Column(name = "first_Owner_Ready", nullable = false)
//    private boolean firstOwnerReady;
//
//    @Column(name = "second_Owner_Ready", nullable = false)
//    private boolean secondOwnerReady;
//    
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;

    public Long getGame_id() {
        return game_id;
    }

    public void setGame_id(Long game_id) {
        this.game_id = game_id;
    }

    public User getFirstOwner() {
        return firstOwner;
    }

    public void setFirstOwner(User firstOwner) {
        this.firstOwner = firstOwner;
    }

    public User getSecondOwner() {
        return secondOwner;
    }

    public void setSecondOwner(User secondOwner) {
        this.secondOwner = secondOwner;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public Long getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(Long currentTurn) {
        this.currentTurn = currentTurn;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }


    

}
