/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backendSeaBattle.sea_battle.controllers.dto;

/**
 *
 * @author Александра
 */

// вспомогательный класс
public class CellCoords {
    private int x;    // от 1 до 10
    private int y;    // от 1 до 10
    public CellCoords() {}
    public CellCoords(int x, int y) { this.x = x; this.y = y; }
    public int getX() { return x; }
    public int getY() { return y; }
}
