/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.service;

import com.backendSeaBattle.sea_battle.models.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.backendSeaBattle.sea_battle.repository.UserRepository;
import java.util.Optional;


/**
 *
 * @author Александра
 */

@Service
@AllArgsConstructor

public class UserService {
    
    private final UserRepository repository;
    public User startGame(String userName) {
        User user = new User(userName, false); 
        repository.save(user);    
        return user; 
    }

    public Optional <User> findById (Long id){
        return repository.findById(id); 
    }
    
    public User save (User user){
        return repository.save(user);
    }
    
    
}
