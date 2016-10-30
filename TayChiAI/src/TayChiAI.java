import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;

public class TayChiAI extends CKPlayer {

    private byte player;
    private byte opponent;
    private int numRows;
    private int numCols;

    public TayChiAI(byte player, BoardModel state) {
        super(player, state);
        this.teamName = "TayChiAI";
        this.player = player;
        this.opponent = getOpponent(player);
        this.numRows = state.width;
        this.numCols = state.height;
    }

    private byte getOpponent(byte player) {
        return (player != (byte) 1 ? (byte) 1 : (byte) 2);
    }

    private int getRandomInteger(int max) {
        Random r = new Random();
        return Math.abs(r.nextInt()) % max;
    }

    @Override
    public Point getMove(BoardModel state) {
        int taken = 1, maxGuess = (state.getWidth() + state.getHeight()) * 1000, x = -1, y = -1;
        Point move = null;
        while (taken != 0) {
            x = getRandomInteger(state.getWidth());
            y = getRandomInteger(state.getHeight());
            taken = state.getSpace(x, y);
        }
        move = new Point(x, y);
        return move;
    }

    @Override
    public Point getMove(BoardModel state, int deadline) {
        return minMax(state, 3);
    }
    
    private Point minMax(BoardModel state, int depth) {
        BoardModel newState = maxDecision(state, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return newState.lastMove;
    }
    
    // @todo: Add terminal state checks for decisions
    private BoardModel maxDecision(BoardModel state, int depth, int alpha, int beta) {
        if (depth <= 0) return state;
        int max = Integer.MIN_VALUE;
        BoardModel maxDecision = state;
        ArrayList<BoardModel> states = this.getStates(state);
        for (BoardModel newState : states) {
            int newStateScore = score(minDecision(newState, depth - 1, alpha, beta));
            if (newStateScore >= max) {
                max = newStateScore;
                maxDecision = newState;
            }
            alpha = Math.max(alpha, max);
            if (beta <= alpha) {
                System.out.println("Prunning search tree - max");
                break; // this will break the loop and prune the leaves.
            }
        }
        return maxDecision;
    }
    
    private BoardModel minDecision(BoardModel state, int depth, int alpha, int beta) {
        if (depth <= 0) return state;
        int min = Integer.MIN_VALUE;
        BoardModel minDecision = state;
        ArrayList<BoardModel> states = this.getStates(state);
        for (BoardModel newState : states) {
            int newStateScore = score(maxDecision(newState, depth - 1, alpha, beta));
            if (newStateScore <= min) {
                min = newStateScore;
                minDecision = newState;
            }
            beta = min;
            if (beta <= alpha) {
                System.out.println("Prunning search tree - min");
                break;
            }
        }
        return minDecision;
    }

    private BoardModel generateState(BoardModel state, int x, int y) {
        System.out.println(String.format("Attempting to generate nextState at %d, %d", x, y));
        // undocumented, but placePiece will generate a new board.
        BoardModel newState = state.placePiece(new Point(x, y), this.player);
        return newState;
    }
    
    private ArrayList<BoardModel> getStates(BoardModel state) {
        // @todo: Add utility function.
        ArrayList<BoardModel> states = new ArrayList<>();
        final int TARGET_WIDTH = state.getWidth(), TARGET_HEIGHT = state.getHeight();
        for (int i = 0; i < TARGET_WIDTH; i++) {
            for (int j = 0; j < TARGET_HEIGHT; j++) {
                if (state.getSpace(i, j) == 0) {
                    // With gravity on, we only want the lowest unfilled row in 
                    // this column. Otherwise we can consider any spot.
                    if (!state.gravityEnabled() || (j == 0 || state.getSpace(i, j - 1) != 0)) {
                        states.add(this.generateState(state, i, j));
                    }
                }
            }
        }
        return states;
    }
    
    private int score(BoardModel state) {
        // we should consider gravity when scoring. But I'm not right now.
        // suck it.
        // This simple returns the largest numbers of contigous spots.
        int n = state.getWidth(), m = state.getHeight();
        int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
        int[][] board = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                byte t = state.getSpace(i, j);
                if (state.getSpace(i, j) != this.player) {
                    board[i][j] = 0;
                } else {
                    board[i][j] = Math.max(
                            board[Math.max(i - 1, 0)][j] + 1, 
                            board[i][Math.max(j - 1, 0)] + 1
                    );
                    max = Math.max(board[i][j], max);
                }
            }
        }
        board = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                byte t = state.getSpace(i, j);
                if (state.getSpace(i, j) != this.opponent) {
                    board[i][j] = 0;
                } else {
                    board[i][j] = Math.max(
                            board[Math.max(i - 1, 0)][j] + 1, 
                            board[i][Math.max(j - 1, 0)] + 1
                    );
                    max = Math.max(board[i][j], max);
                }
            }
        }
        
        return max - min;
    }
}
