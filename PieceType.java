
package checkers;

/**
 *
 * @author 167184
 */
public enum PieceType {
    RED(-1, 7, "red"), BLACK(1, 0, "black"), RED_KING(1,0, "red"), BLACK_KING(1,0, "black");
    
    final int dirMove;
    final int kingRow;
    final String colour;

    PieceType(int dirMove, int kingRow, String colour){
        this.dirMove = dirMove;
        this.kingRow = kingRow;
        this.colour = colour;
    }
}
