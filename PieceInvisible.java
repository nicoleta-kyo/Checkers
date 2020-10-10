package checkers;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Ellipse;

/**
 *
 * @author 167184
 */

//helper object for layout of tiles and pieces
public class PieceInvisible extends Ellipse{
    
    public PieceInvisible(boolean dark, int width, int height){
        
        setRadiusX(Game.SIZE_TILE*0.3);
        setRadiusY(Game.SIZE_TILE*0.25);
        
        relocate(width*Game.SIZE_TILE, height*Game.SIZE_TILE);
        setFill(dark ? Paint.valueOf("#a5703a") : Paint.valueOf("#D2B48C"));
        
    }
    
    
}
