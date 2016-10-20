import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;

public class TayChiAI extends CKPlayer {

    private byte player;
	public TayChiAI(byte player, BoardModel state) {
		super(player, state);
		teamName = "DummyAI";
        this.player = player;
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
		return minMax(state, 5);
	}
	
	private Point minMax(BoardModel state, int depth) {
		BoardModel newState = maxDecision(state, depth - 1);
		return newState.lastMove;
	}
	
	// @todo: Add terminal state checks for decisions
	private BoardModel maxDecision(BoardModel state, int depth) {
		if (depth <= 0) return state;
		int max = Integer.MIN_VALUE;
		BoardModel maxDecision = state;
		ArrayList<BoardModel> states = this.getStates(state);
		for (BoardModel newState : states) {
			int newStateScore = score(minDecision(newState, depth - 1));
			if (newStateScore >= max) {
				max = newStateScore;
				maxDecision = newState;
			}
		}
		return maxDecision;
	}
	
	private BoardModel minDecision(BoardModel state, int depth) {
		if (depth <= 0) return state;
		int min = Integer.MIN_VALUE;
		BoardModel minDecision = state;
		ArrayList<BoardModel> states = this.getStates(state);
		for (BoardModel newState : states) {
			int newStateScore = score(maxDecision(newState, depth - 1));
			if (newStateScore <= min) {
				min = newStateScore;
				minDecision = newState;
			}
		}
		return minDecision;
	}
	private BoardModel generateState(BoardModel state, int x, int y) {
		// undocumented, but placePiece will generate a new board.
		BoardModel newState = state.placePiece(new Point(x, y), this.player);
		return newState;
	}
	
	private ArrayList<BoardModel> getStates(BoardModel state) {
		// @todo: Add utility function.
		ArrayList<BoardModel> states = new ArrayList<>();
		if (state.gravityEnabled()) {
			// the board will ensure points sink.
			for (int i = 0; i < state.getWidth(); i++) {
				if (state.getSpace(i, 0) == 0) {
					states.add(this.generateState(state, i, 0));
					System.out.println(score(states.get(states.size() - 1)));
				}
			}
		} else {
			// we can consider any move.
			for (int i = 0; i < state.getWidth(); i++) {
				for (int j = 0; j < state.getHeight(); j++) {
					if (state.getSpace(i, j) == 0) {
						states.add(this.generateState(state, i, j));
						System.out.println(score(states.get(states.size() - 1)));
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
		int max = Integer.MIN_VALUE;
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
		
		return max;
	}
}
