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
public class TileBE {
    
    private int x,y;
    private boolean playable;
    private PieceBE piece;
    
    public boolean hasPiece(){
        return piece != null;
    }

    public void setPiece(PieceBE piece) {
        this.piece = piece;
    }

    public PieceBE getPiece() {
        return piece;
    }
    
    public TileBE (int x, int y, boolean playable){
        this.x = x;
        this.y = y;
        this.playable = playable;
    }
    
}
