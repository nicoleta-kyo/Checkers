/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package checkers;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Ellipse;

/**
 *
 * @author nk331
 */
public class Piece extends Ellipse {
    
    private double mouseX, mouseY;
    private PieceType type;
    private double x, y;
    private int boardPosX, boardPosY;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setBoardPosX(int boardPosX) {
        this.boardPosX = boardPosX;
    }

    public void setBoardPosY(int boardPosY) {
        this.boardPosY = boardPosY;
    }
    
    
    
//
//    public void setBoardPosX(int pixel) {
//        this.boardPosX = (int)((pixel + Game.SIZE_TILE/2)/Game.SIZE_TILE);
//    }
//    
//    public void setBoardPosY(int pixel) {
//        this.boardPosY = (int)((pixel + Game.SIZE_TILE/2)/Game.SIZE_TILE);
//    }

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
    
    public Piece (PieceType type, int width, int height){
        
        this.type = type;
        this.boardPosX = width;
        this.boardPosY = height;
        this.x = Game.SIZE_TILE*width;
        this.y = Game.SIZE_TILE*height;
        setRadiusX(Game.SIZE_TILE*0.3);
        setRadiusY(Game.SIZE_TILE*0.25);
        
        relocate(width*Game.SIZE_TILE, height*Game.SIZE_TILE);
        setFill(type == PieceType.BLACK ? Paint.valueOf("#0b0100") : Paint.valueOf("#e31f09"));
        setStroke(Paint.valueOf("#5a5453"));
        setStrokeWidth(2);
        
        setOnMousePressed(e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });
        
        setOnMouseDragged(e -> {
            //calculate new location by adding the difference, result of the mouse drag, to the old location of the piece
            relocate(x + e.getSceneX() - mouseX, y + e.getSceneY() - mouseY);
        });   
        
    }
    
    public void allowMove(int newBoardPosX, int newBoardPosY){
        this.x = newBoardPosX*Game.SIZE_TILE;
        this.y = newBoardPosY*Game.SIZE_TILE;
        this.boardPosX = newBoardPosX;
        this.boardPosY = newBoardPosY;
        relocate(x,y);
    }
    
    //public 
    
    public void stopMove(){
        relocate(x,y);
    }
    
}
