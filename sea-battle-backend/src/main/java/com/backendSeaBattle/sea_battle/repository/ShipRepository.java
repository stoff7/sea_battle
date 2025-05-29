/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.backendSeaBattle.sea_battle.repository;

import com.backendSeaBattle.sea_battle.models.entity.Game;
import com.backendSeaBattle.sea_battle.models.entity.Ship;
import com.backendSeaBattle.sea_battle.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Александра
 */
public interface ShipRepository extends JpaRepository<Ship, Long>{
    void deleteAllByGameAndOwner(Game game, User owner);
}
