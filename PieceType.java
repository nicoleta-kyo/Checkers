/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package checkers;

/**
 *
 * @author nk331
 */
public enum PieceType {
    RED(-1, 7, "red"), BLACK(1, 0, "black"), RED_KING(1,0, "red"), BLACK_KING(1,0, "black");
    
    final int dirMove;
    final int kingRow;
    final String colour;

    PieceType(int dirMove, int kingRow, String colour){
        this.dirMove = dirMove;
        this.kingRow = kingRow;
        this.colour = colour;
    }
}
