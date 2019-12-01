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
public class PieceBE {
    
    private PieceType type;
    private int boardPosX, boardPosY;

    public void setBoardPosX(int boardPosX) {
        this.boardPosX = boardPosX;
    }

    public void setBoardPosY(int boardPosY) {
        this.boardPosY = boardPosY;
    }

    public int getBoardPosX() {
        return boardPosX;
    }

    public int getBoardPosY() {
        return boardPosY;
    }
    
    public PieceType getType() {
        return type;
    }
    
    public void setType(PieceType type) {
        this.type = type;
    }
    
    public PieceBE (PieceType type, int width, int height){
        
        this.type = type;
        this.boardPosX = width;
        this.boardPosY = height;
        
    }
    
    public void allowMove(int newBoardPosX, int newBoardPosY){
        this.boardPosX = newBoardPosX;
        this.boardPosY = newBoardPosY;
    }
    
    
}
