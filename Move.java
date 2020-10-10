
package checkers;

/**
 *
 * @author 167184
 */
public class Move{
    
    private Piece movedPiece;
    private Piece capturedPiece;
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
        this.oldBoardX = movedPiece.getBoardPosX();
        this.oldBoardY = movedPiece.getBoardPosY();
        this.oldCapX = capturedPiece.getBoardPosX();
        this.oldCapY = capturedPiece.getBoardPosY();
        this.movedPiece = movedPiece;
    }
    
    Move(MoveType type, boolean kingConv, int newBoardX, int newBoardY, Piece movedPiece){
        this.type = type;
        this.kingConversion = kingConv;
        this.newBoardX = newBoardX;
        this.newBoardY = newBoardY;
        this.movedPiece = movedPiece;
        this.oldBoardX = movedPiece.getBoardPosX();
        this.oldBoardY = movedPiece.getBoardPosY();
    }
    
    
}
