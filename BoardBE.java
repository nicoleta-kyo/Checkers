/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package checkers;

/**
 *
 * @author Niki
 */
public class BoardBE {
    
     private TileBE[][] boardBE;
    
    BoardBE(){
       boardBE = new TileBE[8][8];
    }
    
    public void setTile(int col, int row, TileBE tile){
        boardBE[col][row] = tile;
    }
    
    public TileBE getTile(int col, int row){
        return boardBE[col][row];
    }
    
    
}
