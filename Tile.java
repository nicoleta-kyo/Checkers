
package checkers;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author 167184
 */

//!!!!do I need a tile class?
public class Tile extends Rectangle {
    private int x,y;
    private boolean playable;
    private Piece piece;
    //private String tileRepresentation;
    
    public boolean hasPiece(){
        return piece != null;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public Piece getPiece() {
        return piece;
    }
    
    public Tile(int x, int y, boolean playable){
        setWidth(Game.SIZE_TILE);
        setHeight(Game.SIZE_TILE);
        this.x = x;
        this.y = y;
        this.playable = playable;
        
        relocate(x*Game.SIZE_TILE, y*Game.SIZE_TILE);
        setFill(playable ? Paint.valueOf("#a5703a") : Paint.valueOf("#D2B48C"));
    }
    
    public void resetColour() {
        setFill(playable ? Paint.valueOf("#a5703a") : Paint.valueOf("#D2B48C"));
    }
    
    
}
