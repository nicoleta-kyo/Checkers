/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package checkers;

import java.util.ArrayList;
import java.util.Random;
import javafx.scene.control.*;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;


/**
 *
 * @author Niki
 */
public class Game extends Application implements EventHandler<ActionEvent> {
    
    public static final int ROWS = 8;
    public static final int COLS = 8;
    public static final int SIZE_TILE = 80;
    
    private ArrayList<Player> players = new ArrayList();
    private int currentPlayer = 0;
    
    private Text messageField = new Text();;
    private Button button = new Button();
    
    private Group tiles = new Group();
    private Group pieces = new Group();
    private Board board = new Board();
    private BoardBE boardBE = new BoardBE();
    
    PauseTransition pause = new PauseTransition(Duration.seconds(3));
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        //show pop-up window to choose first player
        chooseFirstPlayer();
        
        if (players.size() == 2) {
            //prepare scene graph
            BorderPane root = setSceneGraph();

            //prepare scene
            Scene scene = new Scene(root, COLS*SIZE_TILE + 195,  ROWS*SIZE_TILE);
            root.setPrefSize(scene.getWidth(), scene.getHeight());
            
            //prepare stage
            primaryStage.setTitle("Checkers");
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.sizeToScene();
            
            //firstMove
            if (players.get(currentPlayer).getType() == PlayerType.AI){
                doAIMove();
            }
        }
        
    }

    public Group getPieces() {
        return pieces;
    }
    
    public BorderPane setSceneGraph() {
        //create root
        BorderPane root = new BorderPane();
        //create tiles and pieces
        Boolean playable;
        for(int row=0; row < ROWS; row++){
            for(int col=0; col < COLS; col++){
                Piece piece = null;
                PieceBE pieceBE = null;
                //create invisible pieces to keep the alignment of the group in the stack pane
                PieceInvisible invisiblePiece = null;
                if ((row%2!=0 && col%2==0) || (row%2==0 && col%2!=0)){
                    playable = true;
                } else {
                    playable = false;
                }
                Tile tile = new Tile(col, row, playable);
                TileBE tileBE = new TileBE(col, row, playable);
                board.setTile(col,row,tile);
                boardBE.setTile(col,row,tileBE);
                tiles.getChildren().add(tile);
                
                //create red and black pieces
                if (playable && row < 3){
                    piece = createPiece(PieceType.RED, col, row);
                    pieceBE = new PieceBE(PieceType.RED, col, row);
                } else if (playable && row > 4) {
                    piece = createPiece(PieceType.BLACK, col, row);
                    pieceBE = new PieceBE(PieceType.BLACK, col, row);
                }
                
                //set invisible pieces to the corners of the board in order to keep the piece group alligned
                if ((row == 0 && col == 0) || (col == 0 && row == 7) || (col == 7 && row == 0) || (col == 7 && row == 7)){
                    invisiblePiece = new PieceInvisible(playable, col, row);
                }
                //add invisible piece
                if (invisiblePiece != null){
                    pieces.getChildren().add(invisiblePiece);  
                } 
                
                //add piece to tile
                if (piece != null){
                    tile.setPiece(piece);
                    tileBE.setPiece(pieceBE);
                    pieces.getChildren().add(piece);
                }   
            }
        }
        
        //create text field
        button.setText("Finish Turn");
        button.setOnMouseClicked(e -> {
            endTurn();
        });
        messageField.setText("Messages will be shown here bla bla.");
        
        //set root
        root.setCenter(createCenterPane(tiles,pieces));
        root.setRight(createRightPane(messageField, button));
        return root;
    }
    
    private StackPane createCenterPane(Group tiles, Group pieces) {
        StackPane pane = new StackPane();
        pane.getChildren().addAll(tiles, pieces);
                
        return pane;
    }
    
    private VBox createRightPane(Text text, Button button) {
        VBox pane = new VBox();
        pane.getChildren().addAll(text, button);
        
        pane.setMinSize(195, ROWS*SIZE_TILE );
        pane.setMaxSize(195, ROWS*SIZE_TILE );

        return pane;
    }
    
    public Piece createPiece(PieceType type, int col, int row){
        Piece piece = new Piece(type, col, row);
        
        piece.setOnMouseReleased( e -> {
            
            int newBoardPosX = pixelToBoard(piece.getLayoutX());
            int newBoardPosY = pixelToBoard(piece.getLayoutY());
            
            Move resultMove = createMove(piece, newBoardPosX, newBoardPosY);
            
            executeMove(resultMove);
            //update the simulated board as well
            resetSimBoard();
            
        });
        
        return piece;
    }
    

    @Override
    public void handle(ActionEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public int pixelToBoard(double pixel){
        return (int)((pixel + Game.SIZE_TILE/2)/Game.SIZE_TILE);
    }
    
    public Move createMove(Piece piece, int newBoardPosX, int newBoardPosY){
        
        boolean kingConversion = false;
        // get potential captured piece
        int capturedBoardX = (piece.getBoardPosX() + newBoardPosX)/2;
        int capturedBoardY = (piece.getBoardPosY() + newBoardPosY)/2;
       
        // for normal pieces
        if(piece.getType() == PieceType.BLACK || piece.getType() == PieceType.RED) {
             //walk
             // check for correct direction of movement, if it's diagonally and if space is free
            if (piece.getBoardPosY() - newBoardPosY == piece.getType().dirMove && Math.abs(piece.getBoardPosX()-newBoardPosX) == 1 && !board.getTile(newBoardPosX, newBoardPosY).hasPiece()) {
                //become King
                if (newBoardPosY == piece.getType().kingRow){
                    kingConversion = true;
                }
                return new Move(MoveType.WALK, kingConversion, null, newBoardPosX, newBoardPosY, piece);
            //capture
            // check for correct direction of movement, if it's diagonally, if space is free, if there is a piece to be captured that is not of the same colour
            } else if (piece.getBoardPosY() - newBoardPosY == (piece.getType().dirMove) * 2 && Math.abs(piece.getBoardPosX()-newBoardPosX) == 2 &&
                    !board.getTile(newBoardPosX, newBoardPosY).hasPiece() && board.getTile(capturedBoardX, capturedBoardY).hasPiece()
                    && board.getTile(capturedBoardX, capturedBoardY).getPiece().getType().colour != piece.getType().colour) {
                //here you can become king by also capturing a king
                        if (newBoardPosY == piece.getType().kingRow ||
                                (board.getTile(capturedBoardX, capturedBoardY).getPiece().getType() == PieceType.BLACK_KING || board.getTile(capturedBoardX, capturedBoardY).getPiece().getType() == PieceType.RED_KING)){
                            kingConversion = true;
                        }
                        Piece capturedPiece = board.getTile(capturedBoardX, capturedBoardY).getPiece();
                        return new Move(MoveType.CAPTURE, kingConversion, capturedPiece, newBoardPosX, newBoardPosY, piece);
                    }
            //else no move
            else {
                return new Move(MoveType.NONE, piece);
            }
        }
        //for kings
        else {
            //walk
            if (Math.abs(piece.getBoardPosY() - newBoardPosY) == piece.getType().dirMove && Math.abs(piece.getBoardPosX() - newBoardPosX) == 1 && !board.getTile(newBoardPosX, newBoardPosY).hasPiece()) {
                return new Move(MoveType.WALK, kingConversion, null, newBoardPosX, newBoardPosY, piece);
            }
            else //capture
            if (Math.abs(piece.getBoardPosY() - newBoardPosY) == piece.getType().dirMove * 2 && Math.abs(piece.getBoardPosX() - newBoardPosX) == 2 && !board.getTile(newBoardPosX, newBoardPosY).hasPiece() 
                    && board.getTile(capturedBoardX, capturedBoardY).hasPiece() && board.getTile(capturedBoardX, capturedBoardY).getPiece().getType().colour != piece.getType().colour) {
                Piece capturedPiece = board.getTile(capturedBoardX, capturedBoardY).getPiece();
                return new Move(MoveType.CAPTURE, kingConversion, capturedPiece, newBoardPosX, newBoardPosY, piece);
            }
            //none
            else {
                return new Move(MoveType.NONE, piece);
            }
        }        
    }
    
    public MoveBE createMoveBE(PieceBE piece, int newBoardPosX, int newBoardPosY){
        
        boolean kingConversion = false;
        // get potential captured piece
        int capturedBoardX = (piece.getBoardPosX() + newBoardPosX)/2;
        int capturedBoardY = (piece.getBoardPosY() + newBoardPosY)/2;
       
        // for normal pieces
        if(piece.getType() == PieceType.BLACK || piece.getType() == PieceType.RED) {
             //walk
             // check for correct direction of movement, if it's diagonally and if space is free
            if (piece.getBoardPosY() - newBoardPosY == piece.getType().dirMove && Math.abs(piece.getBoardPosX()-newBoardPosX) == 1 && !boardBE.getTile(newBoardPosX, newBoardPosY).hasPiece()) {
                //become King
                if (newBoardPosY == piece.getType().kingRow){
                    kingConversion = true;
                }
                return new MoveBE(MoveType.WALK, kingConversion, newBoardPosX, newBoardPosY, piece);
            //capture
            // check for correct direction of movement, if it's diagonally, if space is free, if there is a piece to be captured that is not of the same colour
            } else if (piece.getBoardPosY() - newBoardPosY == (piece.getType().dirMove) * 2 && Math.abs(piece.getBoardPosX()-newBoardPosX) == 2 &&
                    !boardBE.getTile(newBoardPosX, newBoardPosY).hasPiece() && boardBE.getTile(capturedBoardX, capturedBoardY).hasPiece()
                    && boardBE.getTile(capturedBoardX, capturedBoardY).getPiece().getType().colour != piece.getType().colour) {
                //here you can become king by also capturing a king
                        if (newBoardPosY == piece.getType().kingRow ||
                                (boardBE.getTile(capturedBoardX, capturedBoardY).getPiece().getType() == PieceType.BLACK_KING || boardBE.getTile(capturedBoardX, capturedBoardY).getPiece().getType() == PieceType.RED_KING)){
                            kingConversion = true;
                        }
                        PieceBE capturedPiece = boardBE.getTile(capturedBoardX, capturedBoardY).getPiece();
                        return new MoveBE(MoveType.CAPTURE, kingConversion, capturedPiece, newBoardPosX, newBoardPosY, piece);
                    }
            //else no move
            else {
                return new MoveBE(MoveType.NONE, piece);
            }
        }
        //for kings
        else {
            //walk
            if (Math.abs(piece.getBoardPosY() - newBoardPosY) == piece.getType().dirMove && Math.abs(piece.getBoardPosX() - newBoardPosX) == 1 && !boardBE.getTile(newBoardPosX, newBoardPosY).hasPiece()) {
                return new MoveBE(MoveType.WALK, kingConversion, newBoardPosX, newBoardPosY, piece);
            }
            else //capture
            if (Math.abs(piece.getBoardPosY() - newBoardPosY) == piece.getType().dirMove * 2 && Math.abs(piece.getBoardPosX() - newBoardPosX) == 2 && !boardBE.getTile(newBoardPosX, newBoardPosY).hasPiece() 
                    && boardBE.getTile(capturedBoardX, capturedBoardY).hasPiece() && boardBE.getTile(capturedBoardX, capturedBoardY).getPiece().getType().colour != piece.getType().colour) {
                PieceBE capturedPiece = boardBE.getTile(capturedBoardX, capturedBoardY).getPiece();
                return new MoveBE(MoveType.CAPTURE, kingConversion,capturedPiece, newBoardPosX, newBoardPosY, piece);
            }
            //none
            else {
                return new MoveBE(MoveType.NONE, piece);
            }
        }        
    }
    
    public ArrayList<ArrayList<Move>> getAvailableMoves(Player player){  
        
        ArrayList<ArrayList<Move>> validMoves1 = new ArrayList();
        ArrayList<ArrayList<Move>> validMoves2 = new ArrayList();
            
            //associate player with color of pieces
            //for each piece of that color, try all + 1 and + 2 board positions around, and store the walk or capture moves as possible moves
            for (int row=0; row <= 7; row++){
                for (int col=0; col <= 7; col++){
                    if (board.getTile(col, row).hasPiece() && board.getTile(col, row).getPiece().getType().colour == player.getColourPiece()) {

                        Piece piece = board.getTile(col, row).getPiece();

                        //store possible X and Y positions
                        ArrayList<Integer> tryNewBoardY = new ArrayList();
                        ArrayList<Integer> tryNewBoardX = new ArrayList();
                        //for normal piece
                        tryNewBoardY.add(new Integer(row-piece.getType().dirMove));
                        tryNewBoardX.add(new Integer(col - 1));
                        tryNewBoardY.add(new Integer(row-piece.getType().dirMove));
                        tryNewBoardX.add(new Integer(col + 1));
                        tryNewBoardY.add(new Integer(row-piece.getType().dirMove*2));
                        tryNewBoardX.add(new Integer(col - 2));
                        tryNewBoardY.add(new Integer(row-piece.getType().dirMove*2));
                        tryNewBoardX.add(new Integer(col + 2));
                        //for king piece - store moves with the opposite direction as well
                        if (piece.getType() == PieceType.BLACK_KING || piece.getType() == PieceType.RED_KING) {
                            tryNewBoardY.add(new Integer(row + piece.getType().dirMove));
                            tryNewBoardX.add(new Integer(col - 1));
                            tryNewBoardY.add(new Integer(row + piece.getType().dirMove));
                            tryNewBoardX.add(new Integer(col + 1));
                            tryNewBoardY.add(new Integer(row + piece.getType().dirMove*2));
                            tryNewBoardX.add(new Integer(col - 2));
                            tryNewBoardY.add(new Integer(row + piece.getType().dirMove*2));
                            tryNewBoardX.add(new Integer(col + 2));
                        }

                        //for each x + y, check bounds and store
                        for (int i = 0; i < tryNewBoardX.size() ; i++){
                            int x = tryNewBoardX.get(i);
                            int y = tryNewBoardY.get(i);
                            if (inBounds(x) && inBounds(y)) {
                                Move move = createMove(piece, x, y);
                                if (move.getType() != MoveType.NONE) {
                                    //adding array list of one item list
                                    ArrayList<Move> moveToAdd1 = new ArrayList();
                                    moveToAdd1.add(move);
                                    validMoves1.add(moveToAdd1);
                                }
                            }   
                        }
                    }
                }
            }

            //if there is at least one capturing move - take only these moves as valid
            if (containsCapturingMove(validMoves1)) {
                for (int i = 0; i < validMoves1.size() ; i++){
                    if (validMoves1.get(i).get(0).getType() == MoveType.CAPTURE) {
                        validMoves2.add(validMoves1.get(i));
                    }
                }
                return validMoves2;
            }
        return validMoves1;
    }
    
    public ArrayList<ArrayList<MoveBE>> getAvailableMovesBE(Player player){
        
        
        ArrayList<ArrayList<MoveBE>> validMoves1 = new ArrayList();
        ArrayList<ArrayList<MoveBE>> validMoves2 = new ArrayList();
            
            //associate player with color of pieces
            //for each piece of that color, try all + 1 and + 2 board positions around, and store the walk or capture moves as possible moves
            for (int row=0; row <= 7; row++){
                for (int col=0; col <= 7; col++){
                    if (boardBE.getTile(col, row).hasPiece() && boardBE.getTile(col, row).getPiece().getType().colour == player.getColourPiece()) {

                        PieceBE piece = boardBE.getTile(col, row).getPiece();

                        //store possible X and Y positions
                        ArrayList<Integer> tryNewBoardY = new ArrayList();
                        ArrayList<Integer> tryNewBoardX = new ArrayList();
                        //for normal piece
                        tryNewBoardY.add(new Integer(row-piece.getType().dirMove));
                        tryNewBoardX.add(new Integer(col - 1));
                        tryNewBoardY.add(new Integer(row-piece.getType().dirMove));
                        tryNewBoardX.add(new Integer(col + 1));
                        tryNewBoardY.add(new Integer(row-piece.getType().dirMove*2));
                        tryNewBoardX.add(new Integer(col - 2));
                        tryNewBoardY.add(new Integer(row-piece.getType().dirMove*2));
                        tryNewBoardX.add(new Integer(col + 2));
                        //for king piece - store moves with the opposite direction as well
                        if (piece.getType() == PieceType.BLACK_KING || piece.getType() == PieceType.RED_KING) {
                            tryNewBoardY.add(new Integer(row + piece.getType().dirMove));
                            tryNewBoardX.add(new Integer(col - 1));
                            tryNewBoardY.add(new Integer(row + piece.getType().dirMove));
                            tryNewBoardX.add(new Integer(col + 1));
                            tryNewBoardY.add(new Integer(row + piece.getType().dirMove*2));
                            tryNewBoardX.add(new Integer(col - 2));
                            tryNewBoardY.add(new Integer(row + piece.getType().dirMove*2));
                            tryNewBoardX.add(new Integer(col + 2));
                        }

                        //for each x + y, check bounds and store
                        for (int i = 0; i < tryNewBoardX.size() ; i++){
                            int x = tryNewBoardX.get(i);
                            int y = tryNewBoardY.get(i);
                            if (inBounds(x) && inBounds(y)) {
                                MoveBE move = createMoveBE(piece, x, y);
                                if (move.getType() != MoveType.NONE) {
                                    //if human or AI+walk, add list with single item
                                    if (player.getType() == PlayerType.HUMAN || (player.getType() == PlayerType.AI && move.getType() == MoveType.WALK)){
                                        //adding array list of one item list
                                        ArrayList<MoveBE> moveToAdd1 = new ArrayList();
                                        moveToAdd1.add(move);
                                        validMoves1.add(moveToAdd1);
                                    } else { //if ai+capture              
                                        ArrayList<MoveBE> moveToAdd2 = new ArrayList();
                                        //check for captures after captures
                                        while (move != null && move.getType() == MoveType.CAPTURE) {
                                            moveToAdd2.add(move);
                                            //!!!!! add simulation of move made - otherwise this won't work; doesn't matter for human
                                            executeMoveBE(move);
                                            move = getExtraStepBE(player, move.getMovedPiece());
                                            }
                                        //revert move
                                        resetSimBoard();
                                        validMoves1.add(moveToAdd2);
                                    } 
                                }
                            }   
                        }
                    }
                }
            }

            //if there is at least one capturing move - take only these moves as valid
            if (containsCapturingMoveBE(validMoves1)) {
                for (int i = 0; i < validMoves1.size() ; i++){
                    if (validMoves1.get(i).get(0).getType() == MoveType.CAPTURE) {
                        validMoves2.add(validMoves1.get(i));
                    }
                }
                return validMoves2;
            }
        return validMoves1;
    }
    
    public Move getExtraStep(Player player, Piece pieceUsed){
            
            Move move = null;
            int row = pieceUsed.getBoardPosY();
            int col = pieceUsed.getBoardPosX();
            
            //look for capturing moves
            //store possible X and Y positions
            ArrayList<Integer> tryNewBoardY = new ArrayList();
            ArrayList<Integer> tryNewBoardX = new ArrayList();
            //for normal piece
            tryNewBoardY.add(new Integer(row-pieceUsed.getType().dirMove*2));
            tryNewBoardX.add(new Integer(col - 2));
            tryNewBoardY.add(new Integer(row-pieceUsed.getType().dirMove*2));
            tryNewBoardX.add(new Integer(col + 2));
            //for king piece - store moves with the opposite direction as well
            if (pieceUsed.getType() == PieceType.BLACK_KING || pieceUsed.getType() == PieceType.RED_KING) {
                tryNewBoardY.add(new Integer(row + pieceUsed.getType().dirMove*2));
                tryNewBoardX.add(new Integer(col - 2));
                tryNewBoardY.add(new Integer(row + pieceUsed.getType().dirMove*2));
                tryNewBoardX.add(new Integer(col + 2));
            }
            
            //for each x + y, check bounds, check if move is a capture move and store
            //!!!! what happens if there is more than one?
            for (int i = 0; i < tryNewBoardX.size() ; i++){
                int x = tryNewBoardX.get(i);
                int y = tryNewBoardY.get(i);
                if (inBounds(x) && inBounds(y)) {
                    move = createMove(pieceUsed, x, y);
                    if (move.getType() == MoveType.CAPTURE){
                        return move;
                    }
                }
            }
            return move;
    }
    
    public MoveBE getExtraStepBE(Player player, PieceBE pieceUsed){
            
            MoveBE move = null;
            int row = pieceUsed.getBoardPosY();
            int col = pieceUsed.getBoardPosX();
            
            //look for capturing moves
            //store possible X and Y positions
            ArrayList<Integer> tryNewBoardY = new ArrayList();
            ArrayList<Integer> tryNewBoardX = new ArrayList();
            //for normal piece
            tryNewBoardY.add(new Integer(row-pieceUsed.getType().dirMove*2));
            tryNewBoardX.add(new Integer(col - 2));
            tryNewBoardY.add(new Integer(row-pieceUsed.getType().dirMove*2));
            tryNewBoardX.add(new Integer(col + 2));
            //for king piece - store moves with the opposite direction as well
            if (pieceUsed.getType() == PieceType.BLACK_KING || pieceUsed.getType() == PieceType.RED_KING) {
                tryNewBoardY.add(new Integer(row + pieceUsed.getType().dirMove*2));
                tryNewBoardX.add(new Integer(col - 2));
                tryNewBoardY.add(new Integer(row + pieceUsed.getType().dirMove*2));
                tryNewBoardX.add(new Integer(col + 2));
            }
            
            //for each x + y, check bounds, check if move is a capture move and store
            //!!!! what happens if there is more than one?
            for (int i = 0; i < tryNewBoardX.size() ; i++){
                int x = tryNewBoardX.get(i);
                int y = tryNewBoardY.get(i);
                if (inBounds(x) && inBounds(y)) {
                    move = createMoveBE(pieceUsed, x, y);
                    if (move.getType() == MoveType.CAPTURE){
                        return move;
                    }
                }
            }
            return move;
    }
    
    public boolean inBounds(int x) {
        if (x >= 0 && x <= 7) {
            return true;
        }
        return false;
    }
    
    public void executeMoveBE(MoveBE resultMove){
        
       //get piece tried to be moved
        PieceBE movedPiece = resultMove.getMovedPiece();

            //get new coordinates of the piece
            int newBoardPosX = resultMove.getNewBoardX();
            int newBoardPosY = resultMove.getNewBoardY();

            //update board
            boardBE.getTile(newBoardPosX, newBoardPosY).setPiece(movedPiece);
            boardBE.getTile(resultMove.getOldBoardX(), resultMove.getOldBoardY()).setPiece(null);
            //move piece
            movedPiece.allowMove(newBoardPosX, newBoardPosY);

            //if move is CAPTURE....
            if(resultMove.getType() == MoveType.CAPTURE){
                //get rid of captured piece
                //get rid of captured piece
                PieceBE capturedPiece = resultMove.getCapturedPiece();
                boardBE.getTile(capturedPiece.getBoardPosX(), capturedPiece.getBoardPosY()).setPiece(null);
            }

            // Check if piece becomes king
            if (resultMove.hasKingConversion()) {
                movedPiece.setType(movedPiece.getType() == PieceType.RED ? PieceType.RED_KING : PieceType.BLACK_KING);
            }
        
    }
    
    // for human - get rid of ifs
    public void executeMove(Move resultMove){
        
        //get piece tried to be moved
        Piece movedPiece = resultMove.getMovedPiece();
        
        int validity = 0;
        
        //check  for human player
        if (players.get(currentPlayer).getType() == PlayerType.HUMAN){
            validity = validMove(resultMove);
        }
        //if not valid move, stop it and notify
        if (validity != 0) {
            movedPiece.stopMove();
            switch(validity) {
                case 1: messageField.setText("Invalid move:");
                break;
                case 2: messageField.setText("You don't play with this colour.");
                break;
                case 3: messageField.setText("You've missed a capturing move.");
                break;
                case 4: messageField.setText("You can't make another move unless\nit is another capturing move.");
                break;
                case 5: messageField.setText("You can't make another move.");
                break;
            }
        //if not human player or move for human player valid, execute move
        } else {

            //get new coordinates of the piece
            int newBoardPosX = resultMove.getNewBoardX();
            int newBoardPosY = resultMove.getNewBoardY();

            //update board
            board.getTile(newBoardPosX, newBoardPosY).setPiece(movedPiece);
            board.getTile(movedPiece.getBoardPosX(), movedPiece.getBoardPosY()).setPiece(null);
            //move piece
            movedPiece.allowMove(newBoardPosX, newBoardPosY);
            //clear any messages
            messageField.setText("");

            //update num moves
            players.get(currentPlayer).setNumMovesMade(players.get(currentPlayer).getNumMovesMade()+1);

            //if move is CAPTURE....
            if(resultMove.getType() == MoveType.CAPTURE){
                //!!!!!! might need to get rid of that - update capture boolean to true
                players.get(currentPlayer).setHasCaptured(true);
                //set piece used variable of player to use it in getAvailableMoves method
                players.get(currentPlayer).setPieceUsed(movedPiece);

                //get rid of captured piece
                Piece capturedPiece = resultMove.getCapturedPiece();
                board.getTile(capturedPiece.getBoardPosX(), capturedPiece.getBoardPosY()).setPiece(null);
                pieces.getChildren().remove(capturedPiece);
            }

            // Check if piece becomes king
            if (resultMove.hasKingConversion()) {
                movedPiece.setType(movedPiece.getType() == PieceType.RED ? PieceType.RED_KING : PieceType.BLACK_KING);
                movedPiece.setEffect(new DropShadow(10, Color.GOLDENROD));
            }

            //show hints for next move if the player is human
            if (players.get(currentPlayer).getType() == PlayerType.HUMAN) {
                board.resetHints();
                showAvailableMoves();
            }
            
        }
    }
    
    public int validMove(Move resultMove) {
        
        //get variables needed for checks
        int numMoves = players.get(currentPlayer).getNumMovesMade();
        boolean lastCap = players.get(currentPlayer).hasCaptured();
        boolean hasCap = containsCapturingMove(getAvailableMoves(players.get(currentPlayer)));
        String colorPieceMoved = resultMove.getMovedPiece().getType().colour;
        MoveType type = resultMove.getType();
        
        //if none type- invalid move -- !!!maybe neet to expand??
        if (type == MoveType.NONE){
            return 1;
        }
        // check if correct colour piece
        // this wont be executed if the check for end of game is being done
        if (colorPieceMoved != players.get(currentPlayer).getColourPiece()){
            return 2;
        }
        //missed first capturing move
        // this wont be executed if we haven't set it to do this check (by the checkCap variable)
        if (numMoves == 0 && !lastCap && hasCap && type == MoveType.WALK){
            return 3;
        }
        //second move not capturing
        if (numMoves != 0 && lastCap && type == MoveType.WALK) {
            return 4;
        }
        //tried second but not allowed to
        if (numMoves != 0 && !lastCap){
            return 5;
        }
        return 0;
    }
    
    public void chooseFirstPlayer(){
        Stage dialogFirstPlayer = new Stage();
      
        dialogFirstPlayer.initModality(Modality.APPLICATION_MODAL);
        
        Text text1 = new Text("Welcome to Checkers!");
        Text text2 = new Text("You'll play against an AI...");
        Text text3 = new Text("Please choose who will play first.");

        Button button1= new Button("I go first!");
        Button button2= new Button("Let the AI go first!");
        
        button1.setOnAction(e -> {
            players.add(new Player(PlayerType.HUMAN, "black"));
            players.add(new Player(PlayerType.AI, "red"));
            dialogFirstPlayer.close();
        });
        
        button2.setOnAction(e -> {
            players.add(new Player(PlayerType.AI, "black"));
            players.add(new Player(PlayerType.HUMAN, "red"));
            dialogFirstPlayer.close();
        });
        
        VBox layout= new VBox(10);
        layout.getChildren().addAll(text1, text2, text3, button1, button2);
        layout.setAlignment(Pos.CENTER);
        Scene scene1= new Scene(layout, 350, 300);
        dialogFirstPlayer.setScene(scene1);
        dialogFirstPlayer.showAndWait();
  

    }
    
    public void showAvailableMoves(){
        
        ArrayList<Move> moves = new ArrayList();
        //
        if (players.get(currentPlayer).hasCaptured()) {
            Move move = getExtraStep(players.get(currentPlayer), players.get(currentPlayer).getPieceUsed());
            if (move != null && validMove(move) == 0){
                moves.add(move);
            } 
        } else {
            ArrayList<ArrayList<Move>> mvs = getAvailableMoves(players.get(currentPlayer));
            if (mvs.size() > 0) {
                for (int i = 0; i < mvs.size(); i++){
                    if (validMove(mvs.get(i).get(0)) == 0){
                        moves.add(mvs.get(i).get(0));
                    }
                }
            }

        }
        
        int newX, newY;
        for (int i=0; i < moves.size(); i++) {
            newX = moves.get(i).getNewBoardX();
            newY = moves.get(i).getNewBoardY();
            board.getTile(newX, newY).setFill(Paint.valueOf("#fcd116"));
        }
    }
    
    public void endTurn(){
        
        //reset moves info for player
        players.get(currentPlayer).setNumMovesMade(0);
        players.get(currentPlayer).setHasCaptured(false);
        players.get(currentPlayer).setPieceUsed(null);
        
        //check if game has finished, and if yes, notify, otherwise execute the end of turn, and the next
        int gameWinner = gameEnd();
        if(gameWinner != -1){
            showGameEndWindow(gameWinner);
        } else {
            messageField.setText("");

            //switch players
            currentPlayer = (currentPlayer == 0 ? 1 : 0);
            //if next player is AI, hide hints and do AI move, otherwise let human play and show hints
            if (players.get(currentPlayer).getType() == PlayerType.AI) {
                board.resetHints();
                doAIMove();
            } else {
                showAvailableMoves();
            }
        }
    }
    
    public void doAIMove(){
        
        //Move move = null;
        Random rand = new Random();
        ArrayList<ArrayList<MoveBE>> movesAvailable = getAvailableMovesBE(players.get(currentPlayer));
        
        //choose random move
        ArrayList<MoveBE> moves = movesAvailable.get(rand.nextInt(movesAvailable.size()));
        
        //create real move MoveType type, boolean kingConv, Piece capturedPiece, int newBoardX, int newBoardY, Piece movedPiece
//        
//      Task task = new Task<Void>() {
//            @Override public Void call() {
                        
        //execute multi-step
        for (int i=0; i < moves.size(); i++) {
            //create real move and execute
            Move move = mapMove(moves.get(i));
            executeMove(move);
            //execute the move on the simulated board
            executeMoveBE(moves.get(i));
            //sleep for a second
            pause.play();                
        }
      
        //}               
        //};                
                        
        endTurn();
    }
    
    private boolean containsCapturingMoveBE(ArrayList<ArrayList<MoveBE>> movesList) {
        
        for (int i = 0; i < movesList.size(); i++) {
            if (movesList.get(i).get(0).getType() == MoveType.CAPTURE) {
                return true; 
            }
        }
        return false;
    }

    private boolean containsCapturingMove(ArrayList<ArrayList<Move>> movesList) {
        
        for (int i = 0; i < movesList.size(); i++) {
            if (movesList.get(i).get(0).getType() == MoveType.CAPTURE) {
                return true; 
            }
        }
        return false;
    }
    
    public int gameEnd() {
        
        int winner = -1;
        int actualCurrentPlayer = currentPlayer;
        
        for (int i = 0; i <= 1; i++) {
            
            Player player = players.get(i);
            //set current to be this player in order to have validity check correct
            currentPlayer = i;
            
            //go through board to see if there is any piece of the player
            boolean hasPieces = false;
            for (int row=0; row <= 7; row++){
                for (int col=0; col <= 7; col++){
                    if (board.getTile(col, row).hasPiece() && board.getTile(col, row).getPiece().getType().colour == player.getColourPiece()) {
                        hasPieces = true;
                        break;
                    }
                }
            }

            //if player does not have pieces or player does not have available moves, they've lost
            //set winner pointer to the other player
            if (!hasPieces || getAvailableMoves(player).isEmpty()) {
                winner = (i == 0 ? 1: 0);
            }

        }
        // set currentPlayer back
        currentPlayer = actualCurrentPlayer;
        return winner;
    }
    
    public void showGameEndWindow(int winner) {
        
        //gset message
        String message = (players.get(winner).getType() == PlayerType.AI ? "You lost!" : "You won!");
        
        Stage windowGameEnd = new Stage();
        windowGameEnd.initModality(Modality.APPLICATION_MODAL);
        
        Text text = new Text(message);
        
        VBox layout = new VBox(10);
        layout.getChildren().addAll(text);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout, 350, 300);
        windowGameEnd.setScene(scene);
        windowGameEnd.show();
        
    }
    
    public void resetSimBoard(){
        for (int col=0; col < COLS; col++){
            for (int row = 0; row < ROWS; row++) {
                if(board.getTile(col, row).hasPiece()) {
                    Piece piece = board.getTile(col, row).getPiece();
                    boardBE.getTile(col, row).setPiece(new PieceBE(piece.getType(), piece.getBoardPosX(), piece.getBoardPosY()));
                } else {
                    boardBE.getTile(col, row).setPiece(null);
                }
            }
        }
    }
    
    private Move mapMove(MoveBE moveBE) {
        
        Piece capturedPiece = null;
        if (moveBE.getCapturedPiece() != null) {
            capturedPiece = board.getTile(moveBE.getOldCapX(), moveBE.getOldCapY()).getPiece();
        }
        Piece movedPiece = board.getTile(moveBE.getOldBoardX(), moveBE.getOldBoardY()).getPiece();
        return new Move(moveBE.getType(), moveBE.hasKingConversion(), capturedPiece, moveBE.getNewBoardX(), moveBE.getNewBoardY(), movedPiece);
            
    }
    
    
    }
