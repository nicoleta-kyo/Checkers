
package checkers;

import java.util.ArrayList;

/**
 *
 * @author 167184
 */

//class to store moves and scores created by the minimax algo
public class MovesAndScores {
    
    private ArrayList<Move> move;
    private int score;
    
    MovesAndScores(ArrayList<Move> move, int score){
        this.move = move;
        this.score = score;
    }

    public ArrayList<Move> getMove() {
        return move;
    }

    public int getScore() {
        return score;
    }
    
}
