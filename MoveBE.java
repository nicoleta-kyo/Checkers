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
public class MoveBE {
    
    private PieceBE movedPiece;
    private PieceBE capturedPiece;
    private MoveType type;
    private boolean kingConversion;
    private int oldBoardX;
    private int oldBoardY;
    private int oldCapX;
    private int oldCapY;
    private int newBoardX;
    private int newBoardY;

    public int getOldCapX() {
        return oldCapX;
    }

    public int getOldCapY() {
        return oldCapY;
    }

    public int getOldBoardX() {
        return oldBoardX;
    }

    public int getOldBoardY() {
        return oldBoardY;
    }

    public PieceBE getCapturedPiece() {
        return capturedPiece;
    }

    public PieceBE getMovedPiece() {
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
    
    MoveBE(MoveType type, PieceBE movedPiece){
        this.type = type;
        this.movedPiece = movedPiece;
    }
    
    MoveBE(MoveType type, boolean kingConv, PieceBE capturedPiece, int newBoardX, int newBoardY, PieceBE movedPiece){
        this.type = type;
        this.kingConversion = kingConv;
        this.capturedPiece = capturedPiece;
        this.newBoardX = newBoardX;
        this.newBoardY = newBoardY;
        this.oldBoardX = movedPiece.getBoardPosX();
        this.oldBoardY = movedPiece.getBoardPosY();
        this.oldCapX = capturedPiece.getBoardPosX();
        this.oldCapY = capturedPiece.getBoardPosY();
        this.movedPiece = movedPiece;
    }
    
    MoveBE(MoveType type, boolean kingConv, int newBoardX, int newBoardY, PieceBE movedPiece){
        this.type = type;
        this.kingConversion = kingConv;
        this.newBoardX = newBoardX;
        this.newBoardY = newBoardY;
        this.movedPiece = movedPiece;
        this.oldBoardX = movedPiece.getBoardPosX();
        this.oldBoardY = movedPiece.getBoardPosY();
    }
    
    
    
}
