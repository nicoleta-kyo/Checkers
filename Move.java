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
public class Move{
    
    private Piece movedPiece;
    private Piece capturedPiece;
    private MoveType type;
    private boolean kingConversion;
    private int newBoardX;
    private int newBoardY;

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public Piece getMovedPiece() {
        return movedPiece;
    }

    public MoveType getType() {
        return type;
    }

    public int getNewBoardX() {
        return newBoardX;
    }

    public int getNewBoardY() {
        return newBoardY;
    }
    
    public boolean hasKingConversion() {
        return kingConversion;
    }
    
    Move(MoveType type, Piece movedPiece){
        this.type = type;
        this.movedPiece = movedPiece;
    }
    
    Move(MoveType type, boolean kingConv, Piece capturedPiece, int newBoardX, int newBoardY, Piece movedPiece){
        this.type = type;
        this.kingConversion = kingConv;
        this.capturedPiece = capturedPiece;
        this.newBoardX = newBoardX;
        this.newBoardY = newBoardY;
        this.movedPiece = movedPiece;
    }
    
    Move(MoveType type, boolean kingConv, int newBoardX, int newBoardY, Piece movedPiece){
        this.type = type;
        this.kingConversion = kingConv;
        this.newBoardX = newBoardX;
        this.newBoardY = newBoardY;
        this.movedPiece = movedPiece;
    }
    
    
}
