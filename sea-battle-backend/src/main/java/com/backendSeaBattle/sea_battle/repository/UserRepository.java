/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.repository;

import com.backendSeaBattle.sea_battle.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Александра
 */


public interface UserRepository  extends JpaRepository<User, Long>{
    
}
