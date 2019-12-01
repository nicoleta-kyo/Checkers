/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package checkers;

import java.util.Random;

/**
 *
 * @author Niki
 */
public class Player {
    
    private PlayerType type;
    private String colourPiece;
    private int numMovesMade;
    //put piece variable - if not null will mean that the player played with that piece now and the second move will be looked for only for this piece
    private Piece pieceUsed;
    private boolean hasCaptured = false;
    
    Player(PlayerType type, String colour){
        this.type = type;
        this.colourPiece = colour;
    }

    public Piece getPieceUsed() {
        return pieceUsed;
    }

    public void setPieceUsed(Piece pieceUsed) {
        this.pieceUsed = pieceUsed;
    }

    public void setHasCaptured(boolean hasCaptured) {
        this.hasCaptured = hasCaptured;
    }

    public boolean hasCaptured() {
        return hasCaptured;
    }

    public int getNumMovesMade() {
        return numMovesMade;
    }

    public void setNumMovesMade(int numMovesMade) {
        this.numMovesMade = numMovesMade;
    }

    public PlayerType getType() {
        return type;
    }

    public String getColourPiece() {
        return colourPiece;
    }
    
    
}
