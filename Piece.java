
package checkers;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Ellipse;

/**
 *
 * @author 167184
 */
public class Piece extends Ellipse {
    
    private double mouseX, mouseY;
    private PieceType type;
    private double x, y;
    private int boardPosX, boardPosY;
    
    //boundaries of layout - used to control piece dragging
    private static final int minX = 41;
    private static final int maxX = 604;
    private static final int minY = 41;
    private static final int maxY = 606;

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
            //if out of scene boundaries, relocate to up until the boundary
            if (e.getSceneX() < minX) {
                relocate(x + e.getSceneX() - mouseX + (minX - e.getSceneX()), y + e.getSceneY() - mouseY);
            } else if (e.getSceneX() > maxX) {
                relocate(x + e.getSceneX() - mouseX - (e.getSceneX() - maxX), y + e.getSceneY() - mouseY);
            } else if (e.getSceneY() < minY) {
                relocate(x + e.getSceneX() - mouseX, y + e.getSceneY() - mouseY + (minY - e.getSceneY()));
            } else if (e.getSceneY() > maxY){
                relocate(x + e.getSceneX() - mouseX, y + e.getSceneY() - mouseY - (e.getSceneY() - maxY));
            } else {
            
            //calculate new location by adding the difference, result of the mouse drag, to the old location of the piece
            relocate(x + e.getSceneX() - mouseX, y + e.getSceneY() - mouseY);
            }
            
        });   
        
    }
    
    //update the position variables of the piece on the board. If simulate parameter is true, won't relocate the piece in the GUI.
    public void allowMove(int newBoardPosX, int newBoardPosY, boolean simulate){
        this.x = newBoardPosX*Game.SIZE_TILE;
        this.y = newBoardPosY*Game.SIZE_TILE;
        this.boardPosX = newBoardPosX;
        this.boardPosY = newBoardPosY;
        if (!simulate) {
            relocate(x,y);
        }
        
    }
    
    //relocate the piece on its original place in the GUI
    public void stopMove(){
        relocate(x,y);
    }
    
}
