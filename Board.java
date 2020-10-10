
package checkers;

import java.util.*;
/**
 *
 * @author 167184
 */

//representation of the tiles and pieces in the game
public class Board {
    
    private Tile[][] board;
    
    Board(){
       board = new Tile[8][8];
    }
    
    public void setTile(int col, int row, Tile tile){
        board[col][row] = tile;
    }
    
    public Tile getTile(int col, int row){
        return board[col][row];
    }
    
    //remove any coloured tiles on the board
    public void resetHints(){
        for (int row = 0; row <= 7; row++) {
            for (int col = 0; col <= 7; col++) {
                getTile(col, row).resetColour();
            }
        }
    }
    
    
}
