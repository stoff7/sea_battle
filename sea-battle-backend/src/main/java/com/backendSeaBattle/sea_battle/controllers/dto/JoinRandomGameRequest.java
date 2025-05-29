/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 *
 * @author Александра
 */
public class JoinRandomGameRequest {

    @NotBlank(message = "userName обязателен")
    private String userName;

    public JoinRandomGameRequest() {}

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
