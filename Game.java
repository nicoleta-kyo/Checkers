
package checkers;

import java.util.ArrayList;
import java.util.List;
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
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;


/**
 *
 * @author 167184
 */
public class Game extends Application implements EventHandler<ActionEvent> {
    
    public static final int ROWS = 8;
    public static final int COLS = 8;
    public static final int SIZE_TILE = 80;
    
    private ArrayList<Player> players = new ArrayList();
    private int currentPlayer = 0;
    
    private Text messageField = new Text();
    private Button button = new Button();
    private Button help = new Button();
    
    private Group tiles = new Group();
    private Group pieces = new Group();
    private Board board = new Board();
    
    private ArrayList<MovesAndScores> successorEvaluations;
    private int depthMinimax;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        //show pop-up window to choose first player
        chooseFirstPlayer();
        //show pop-up window to choose difficulty
        chooseDifficultyWindow();
        
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
            primaryStage.setResizable(false);
            
            //run AI move if AI is first
            if (players.get(currentPlayer).getType() == PlayerType.AI){
                doAIMove();
            }
        }
        
    }
    
    public BorderPane setSceneGraph() {
        //create root
        BorderPane root = new BorderPane();
        //create tiles and pieces
        Boolean playable;
        for(int row=0; row < ROWS; row++){
            for(int col=0; col < COLS; col++){
                Piece piece = null;
                //create invisible pieces to keep the alignment of the group in the stack pane
                PieceInvisible invisiblePiece = null;
                if ((row%2!=0 && col%2==0) || (row%2==0 && col%2!=0)){
                    playable = true;
                } else {
                    playable = false;
                }
                Tile tile = new Tile(col, row, playable);
                board.setTile(col,row,tile);
                tiles.getChildren().add(tile);
                
                //create red and black pieces
                if (playable && row < 3){
                    piece = createPiece(PieceType.RED, col, row);
                } else if (playable && row > 4) {
                    piece = createPiece(PieceType.BLACK, col, row);
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
                    pieces.getChildren().add(piece);
                }   
            }
        }
        
        //set root
        root.setCenter(createCenterPane(tiles,pieces));
        root.setRight(createRightPane(messageField, button, help));
        return root;
    }
    
    //create the tiles and pieces pane
    private StackPane createCenterPane(Group tiles, Group pieces) {
        StackPane pane = new StackPane();
        pane.getChildren().addAll(tiles, pieces);        
        
        return pane;
    }
    
    //create the pane with buttons for end turn, show rules and for updates in the game
    private BorderPane createRightPane(Text text, Button button, Button help) {
        
        //end turn button
        button.setText("Finish Turn");
        button.setOnMouseClicked(e -> {
            endTurn();
        });
        //field for updates
        messageField.setText("");
        //rules button
        help.setText("Rules");
        help.setTextAlignment(TextAlignment.CENTER);
        help.setOnMouseClicked(e -> {
            showRules();
        });
        
        BorderPane pane = new BorderPane();
        pane.setCenter(text);
        pane.setTop(button);
        pane.setBottom(help);
        button.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(20,20,20,20));
        
        pane.setMinSize(195, ROWS*SIZE_TILE );
        pane.setMaxSize(195, ROWS*SIZE_TILE );

        return pane;
    }
    
    //create piece in the game
    public Piece createPiece(PieceType type, int col, int row){
        Piece piece = new Piece(type, col, row);
        
        piece.setOnMouseReleased( e -> {
            
            int newBoardPosX = pixelToBoard(piece.getLayoutX());
            int newBoardPosY = pixelToBoard(piece.getLayoutY());
            
            Move resultMove = createMove(piece, newBoardPosX, newBoardPosY);
            
            executeMove(resultMove, false, false);
            if (players.get(currentPlayer).getNumMovesMade() == 0){
                button.setDisable(true);
            } else {
                button.setDisable(false);
            }
            
        });
        
        return piece;
    }
    
    //helper method to convert pixel value on the board to board position
    public int pixelToBoard(double pixel){
        return (int)((pixel + Game.SIZE_TILE/2)/Game.SIZE_TILE);
    }
    
    //create a move in the game
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
                return new Move(MoveType.WALK, kingConversion, newBoardPosX, newBoardPosY, piece);
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
                return new Move(MoveType.WALK, kingConversion, newBoardPosX, newBoardPosY, piece);
            }
            else //capture
            if (Math.abs(piece.getBoardPosY() - newBoardPosY) == piece.getType().dirMove * 2 && Math.abs(piece.getBoardPosX() - newBoardPosX) == 2 && !board.getTile(newBoardPosX, newBoardPosY).hasPiece() 
                    && board.getTile(capturedBoardX, capturedBoardY).hasPiece() && board.getTile(capturedBoardX, capturedBoardY).getPiece().getType().colour != piece.getType().colour) {
                Piece capturedPiece = board.getTile(capturedBoardX, capturedBoardY).getPiece();
                return new Move(MoveType.CAPTURE, kingConversion,capturedPiece, newBoardPosX, newBoardPosY, piece);
            }
            //none
            else {
                return new Move(MoveType.NONE, piece);
            }
        }        
    }
    
    //Method for getting next available moves for a player
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
                                    //if walk
                                    if (move.getType() == MoveType.WALK){
                                        //adding array list of one item list
                                        ArrayList<Move> moveToAdd1 = new ArrayList();
                                        moveToAdd1.add(move);
                                        validMoves1.add(moveToAdd1);
                                    } else { //if capture              
                                        ArrayList<Move> moveToAdd2 = new ArrayList();
                                        //check for multiple captures
                                        while (move != null && move.getType() == MoveType.CAPTURE) {
                                            moveToAdd2.add(move);
                                            //execute simulated move
                                            executeMove(move, true, false);
                                            move = getExtraStep(player, move.getMovedPiece());
                                            }
                                        validMoves1.add(moveToAdd2);
                                        //revert the move
                                        revertMove(moveToAdd2);
                                    } 
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
    
    // check whether list of moves contains a capture move
    private boolean containsCapturingMove(ArrayList<ArrayList<Move>> movesList) {
        
        for (int i = 0; i < movesList.size(); i++) {
            if (movesList.get(i).get(0).getType() == MoveType.CAPTURE) {
                return true; 
            }
        }
        return false;
    }
    
    //get next step in a multi-step move
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
    
    //helper method to check if x and y in bounds
    public boolean inBounds(int x) {
        if (x >= 0 && x <= 7) {
            return true;
        }
        return false;
    }
    
    //execute move in the game, can be actual or simulated
    //the inExecutor boolean set to true makes sure that when used in the executor service, the execute method updates the UI
    //separately, on the Java FX thread.
    public void executeMove(Move resultMove, boolean simulate, boolean inExecutor){
        
        //get piece tried to be moved
        Piece movedPiece = resultMove.getMovedPiece();
        
        int validity = 0;
        //check validity for human player and no simulation
        if (players.get(currentPlayer).getType() == PlayerType.HUMAN && !simulate){
            validity = validMove(resultMove);
        }
        //if not valid move, stop it and notify
        if (validity != 0) {
            movedPiece.stopMove();
            switch(validity) {
                case 1: messageField.setText("Invalid move");
                break;
                case 2: messageField.setText("You don't play\nwith this colour.");
                break;
                case 3: messageField.setText("You've missed\na capturing move.");
                break;
                case 4: messageField.setText("You can't make\nanother move\nunless it is another\ncapturing move.");
                break;
                case 5: messageField.setText("You can't make\nanother move\nbecause you haven't\ncaptured a piece.");
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
            movedPiece.allowMove(newBoardPosX, newBoardPosY, simulate);

            //if no simulation make necassary adjustments to variables used to check validity
            if (!simulate) {
                //increment moves made by player
                players.get(currentPlayer).setNumMovesMade(players.get(currentPlayer).getNumMovesMade()+1);
                //clear any messages
                messageField.setText("");
            }
            
            //if move is CAPTURE....
            if(resultMove.getType() == MoveType.CAPTURE){
                
                //if no simulation make necassary adjustments to variables used to check validity
                if (!simulate) {
                    //update capture boolean to true
                    players.get(currentPlayer).setHasCaptured(true);
                    //set piece used variable of player to use it in getAvailableMoves method
                    players.get(currentPlayer).setPieceUsed(movedPiece);
                }

                //get rid of captured piece
                Piece capturedPiece = resultMove.getCapturedPiece();
                board.getTile(capturedPiece.getBoardPosX(), capturedPiece.getBoardPosY()).setPiece(null);
                // remove the piece from the GUI only if it is an actual move
                if (!simulate && !inExecutor) {
                    pieces.getChildren().remove(capturedPiece);
                }
                
            }

            // Check if piece becomes king
            if (resultMove.hasKingConversion()) {
                movedPiece.setType(movedPiece.getType() == PieceType.RED ? PieceType.RED_KING : PieceType.BLACK_KING);
                // update appearance of king piece on the GUI only if it is an actual move
                if (!simulate && !inExecutor) {
                    movedPiece.setEffect(new DropShadow(10, Color.GOLDENROD));
                }
                
            }

            //show hints for next move if the player is human and difficulty is easy
            if (players.get(currentPlayer).getType() == PlayerType.HUMAN && !simulate && depthMinimax < 8) {
                board.resetHints();
                showAvailableMoves();
            }
            
        }
    }
    
    /// check validity of move
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
    
    //method to update tiles on the board as hints to the human player for next moves
    public void showAvailableMoves(){
        
        ArrayList<Move> moves = new ArrayList();
        //if the player already made a move and made a capture, only look for more capture moves
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
        
        //update tiles where pieces can be moved to be in yellow
        int newX, newY;
        for (int i=0; i < moves.size(); i++) {
            newX = moves.get(i).getNewBoardX();
            newY = moves.get(i).getNewBoardY();
            board.getTile(newX, newY).setFill(Paint.valueOf("#fcd116"));
        }
    }
    
    // execute an AI move by implementimg minimax
    public void doAIMove(){
        
        successorEvaluations = new ArrayList();
        
        //get available moves
        ArrayList<ArrayList<Move>> movesAvailable = getAvailableMoves(players.get(currentPlayer));
        //run minimax and store evaluations
        for (int n=0; n < movesAvailable.size(); n++) {
            int score = minimax(movesAvailable.get(n), depthMinimax, currentPlayer, Integer.MIN_VALUE, Integer.MAX_VALUE);
            successorEvaluations.add(new MovesAndScores(movesAvailable.get(n), score));
        }
        
        //get best move
        ArrayList<Move> best = getBestAIMove();
        
        //execute multi-step AI moves by properly updating the UI in-between them
        if (best.size() > 1) {
            
            // create executor servise for asynchronous task execution
            ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            
            //create end turn runnable task
            Runnable taskEndTurn = new Runnable() {
                    @Override
                    public void run() {
                        try{
                            endTurn();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }   
                    }
                };
            
            //add tasks with separate steps in the multi-step move with pauses in-between
            List<Runnable> runnableTasks = new ArrayList<>();
            for (int i=0; i < best.size(); i++) {
                Move m = best.get(i);
                Piece cap = m.getCapturedPiece();
                Piece moved = m.getMovedPiece();
                Runnable taskExecute = new Runnable() {
                    @Override
                    public void run() {
                        try{
                            //execute move
                            executeMove(m, false, true);
                            //update the UI on the Java FX thread
                            Platform.runLater( () -> {
                                pieces.getChildren().remove(cap);
                                if (m.hasKingConversion()) {
                                moved.setEffect(new DropShadow(10, Color.GOLDENROD));
                            }
                            });
                            //sleep
                            TimeUnit.SECONDS.sleep(1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }   
                    }
                };
                runnableTasks.add(taskExecute);
            }
            //add end turn task to be executed last
            runnableTasks.add(taskEndTurn);
            
            //execute the tasks in the service and exit
            for (int t = 0; t < runnableTasks.size(); t++) {
                executorService.execute(runnableTasks.get(t));
            }
            executorService.shutdown();
            
        // if move is one step, just execute it normally
        } else {
            executeMove(best.get(0), false, false);
            endTurn();
        }            
        
    }
    
    // method to get the move which was evaluated as best by the minimax algo
    private ArrayList<Move> getBestAIMove() {
        
        int max = Integer.MIN_VALUE;
        int best = -1;
        for (int i = 0; i < successorEvaluations.size(); ++i) {
            if (max < successorEvaluations.get(i).getScore()) {
                max = successorEvaluations.get(i).getScore();
                best = i;
            }
        }
        return successorEvaluations.get(best).getMove();
    }
    
    //minimax algo
    private int minimax(ArrayList<Move> mmove, int depth, int player, int alpha, int beta){
        
        int bestValue = Integer.MIN_VALUE;
        
        //simulate (multi-step) move
        for (int m = 0; m < mmove.size(); m++) {
            executeMove(mmove.get(m), true, false);
        }
        
        //get children of move
        int nextPlayer = (player == 0 ? 1 : 0);
        ArrayList<ArrayList<Move>> children = getAvailableMoves(players.get(nextPlayer));
        
        //terminal test - use heuristic to evaluate the move
        if (depth == 0 || children.isEmpty()){
            return evaluateBoard();
        }
        
        if (players.get(player).getType() == PlayerType.AI){ //AI - MAX
            
            bestValue = Integer.MIN_VALUE;
            for (int c = 0; c < children.size(); c++ ){
                int eval = minimax(children.get(c), depth - 1, nextPlayer, alpha, beta);
                bestValue = Math.max(eval, bestValue);
                alpha = Math.max(eval, alpha);
                //revert child
                revertMove(children.get(c));
                
                //pruning
                if (alpha >= beta){
                    break;
                }
            }
            //revert the move which started the evaluation
            if (depth == depthMinimax) {
                revertMove(mmove);
            }
            return bestValue;
        }
        if (players.get(player).getType() == PlayerType.HUMAN){ //Human - MIN
            
            bestValue = Integer.MAX_VALUE;
            for (int c = 0; c < children.size(); c++ ){
                int eval = minimax(children.get(c), depth - 1, nextPlayer, alpha, beta);
                bestValue = Math.min(eval, bestValue);
                beta = Math.min(eval, beta);
                //revert child
                revertMove(children.get(c));
                
                //pruning
                if (alpha >= beta){
                    break;
                }
            }
            return bestValue;
        }
        
        return bestValue;
    }
    
    //evaluate the board's state and return a score used to evaluate a AI move
    private int evaluateBoard(){
        String colourAI = "";
        for (int i = 0; i < 2; i++) {
            if (players.get(i).getType() == PlayerType.AI) {
                colourAI = players.get(i).getColourPiece();
            }
        }
        int countAIPieces = 0;
        int countHumanPieces = 0;
        for (int col=0; col < 8; col++){
            for (int row=0; row < 8; row++){
                if (board.getTile(col, row).hasPiece()) {
                    if (board.getTile(col, row).getPiece().getType().colour.equals(colourAI)) {
                        countAIPieces++;
                    } else {
                        countHumanPieces++;
                    }
                }
            }
        }
        //reutrn heuristic being the number of AI pieces minus the number of Human pieces left in the game
        return countAIPieces - countHumanPieces;
    }
    
    //method for reverting a simulated move
    private void revertMove(ArrayList<Move> mmove){
        
        for (int m = 0; m < mmove.size(); m++) {
            Move resultMove = mmove.get(mmove.size()-1-m);
            
            //get moved piece
            Piece movedPiece = resultMove.getMovedPiece();

            //get old coordinates of the piece in order to update them to new
            int newBoardPosX = resultMove.getOldBoardX();
            int newBoardPosY = resultMove.getOldBoardY();

            //update board
            board.getTile(newBoardPosX, newBoardPosY).setPiece(movedPiece);
            //we set the new coordinates to null
            board.getTile(resultMove.getNewBoardX(), resultMove.getNewBoardY()).setPiece(null);
            //move piece back to old coordinates
            movedPiece.allowMove(newBoardPosX, newBoardPosY, true);

            //if move is CAPTURE....
            if(resultMove.getType() == MoveType.CAPTURE){
                //put back captured piece
                Piece capturedPiece = resultMove.getCapturedPiece();
                board.getTile(resultMove.getOldCapX(), resultMove.getOldCapY()).setPiece(capturedPiece);
                capturedPiece.setBoardPosX(resultMove.getOldCapX());
                capturedPiece.setBoardPosY(resultMove.getOldCapY());
                
            }

            // Check if piece becomes king - revert to normal
            if (resultMove.hasKingConversion()) {
                movedPiece.setType(movedPiece.getType() == PieceType.RED_KING ? PieceType.RED : PieceType.BLACK);
            }
        }
    }
    
    //successor function in the game
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
                //disable end turn
                button.setDisable(true);
                board.resetHints();
                doAIMove();
           
            } else {
                if (depthMinimax < 8) {
                    showAvailableMoves();
                }
                if (players.get(currentPlayer).getNumMovesMade() == 0){
                    button.setDisable(true);
                } else{
                    button.setDisable(false);
                }
            }
        }
    }
    
    //check whether the game ended
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
    
    //pop-up window to choose who will play first
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
    
    //pop-up window to choose game difficulty
    public void chooseDifficultyWindow() {

        Stage diffWindow = new Stage();
        diffWindow.initModality(Modality.APPLICATION_MODAL);
        
        BorderPane pane = new BorderPane();
        
        Text text = new Text("Please choose difficulty.");
        Button b1 = new Button("Easy");
        b1.setOnMouseClicked(e -> {
            //if easy, set the AI to look only four moves ahead
            depthMinimax = 4;
            diffWindow.close();
        });
        Button b2 = new Button("Medium");
        b2.setOnAction(e -> {
            //if medium, set the AI to look 8 moves ahead
            depthMinimax = 8;
            diffWindow.close();
        });
        Button b3 = new Button("Hard");
        b3.setOnAction(e -> {
            depthMinimax = 12;
            //if difficult, set the AI to look 12 moves ahead
            diffWindow.close();
        });
        
        //create hboxes
        HBox levels = new HBox();
        HBox msg = new HBox();
        msg.getChildren().add(text);
        msg.setAlignment(Pos.BOTTOM_CENTER);
        levels.getChildren().addAll(b1, b2, b3);
        levels.setAlignment(Pos.TOP_CENTER);
        levels.setSpacing(10);
        
        //set border pane
        pane.setTop(msg);
        pane.setCenter(levels);
        pane.setPadding(new Insets(40,50,40,50));
        pane.setMargin(msg, new Insets(10,10,10,10));

        //layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(pane, 300, 200);
        diffWindow.setScene(scene);
        diffWindow.showAndWait();
        
    }
    
    //method for displaying the rules of the game
    public void showRules() {
        
        //set message
        String message = "Checkers Rules\n" +
"\n" +
"Game Pieces and Board \n\n" +
"Checkers is a board game played between two people on an 8x8 checked board like the one shown \nbelow. " +
"Each player has 12 pieces that are like flat round disks that fit inside each of the boxes on \nthe board. "
                + "The pieces are placed on every other dark square and then staggered by rows, \n"
                + "like shown on the board." +
"\n" +
"Each Checkers player has different colored pieces. Sometimes the pieces are black and\n" +
"red or red and white. \n" +
"\n" +
"Taking a Turn \n" +
"\n" +
"Typically the darker color pieces moves first. Each player takes their turn by moving a piece. \n"
                + "Pieces are always moved diagonally and can be moved in the following ways:\n" +
" - Diagonally in the forward direction (towards the opponent) to the next dark square.\n" +
" - If there is one of the opponent's pieces next to a piece and an empty space on the other side,\n"
                + " you jump your opponent and remove their piece. You can do multiple jumps if they\n"
                + " are lined up in the forward direction. \n"
                + "*** note: if you have a jump, you have no choice but to take it.\n" +
"\n" +
"King Pieces \n" +
"\n" +
"The last row is called the king row. If you get a piece across the board to the opponent's king row,\n"
                + "that piece becomes a king. Another piece is placed onto that piece so it is now \n"
                + "two pieces high.\n"
                + "King pieces can move in both directions, forward and backward. \n" +
"\n" +
"Once a piece is kinged, the player must wait until the next turn to jump out of the king row. \n" +
"\n" +
"Winning the Game \n" +
"\n" +
"You win the game when the opponent has no more pieces or can't move (even if he/she still \nhas pieces). "
                + "If neither player can move then it is a draw or a tie. ";
        
        Stage showRules = new Stage();
        showRules.initModality(Modality.APPLICATION_MODAL);
        
        Text text = new Text(message);
        
        VBox layout = new VBox(10);
        layout.getChildren().addAll(text);
        layout.setAlignment(Pos.CENTER);
        
        Scene scene = new Scene(layout, 600, 600);
        showRules.setScene(scene);
        showRules.show();
        
    }
    
    //method for notifying that the game ended
    public void showGameEndWindow(int winner) {
        
        //gset message
        String message = (players.get(winner).getType() == PlayerType.AI ? "You lost!" : "You won!");
        
        Stage windowGameEnd = new Stage();
        windowGameEnd.initModality(Modality.APPLICATION_MODAL);
        
        Text text = new Text(message);
        
        VBox layout = new VBox(10);
        layout.getChildren().addAll(text);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout, 250, 200);
        windowGameEnd.setScene(scene);
        windowGameEnd.show();
        
    }    
    
    public Group getPieces() {
        return pieces;
    }
    
    @Override
    public void handle(ActionEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    }
