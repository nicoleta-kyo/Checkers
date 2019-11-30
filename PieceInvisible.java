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
 * @author Niki
 */
public class PieceInvisible extends Ellipse{
    
    public PieceInvisible(boolean dark, int width, int height){
        
        setRadiusX(Game.SIZE_TILE*0.3);
        setRadiusY(Game.SIZE_TILE*0.25);
        
        relocate(width*Game.SIZE_TILE, height*Game.SIZE_TILE);
        setFill(dark ? Paint.valueOf("#a5703a") : Paint.valueOf("#D2B48C"));
        
    }
    
    
}
