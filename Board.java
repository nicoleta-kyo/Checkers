/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package checkers;

import java.util.*;
/**
 *
 * @author nk331
 */
public class Board {
    
    private Tile[][] board;
    
    Board(){
       board = new Tile[8][8];
    }
    
    public void setTile(int col, int row, Tile tile){
        board[col][row] = tile;
    }
    
    public Tile getTile(int col, int row){
        return board[col][row];
    }
    
    public void resetHints(){
        for (int row = 0; row <= 7; row++) {
            for (int col = 0; col <= 7; col++) {
                getTile(col, row).resetColour();
            }
        }
    }
    
    
}
