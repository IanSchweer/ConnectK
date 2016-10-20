import java.util.ArrayList;
import java.util.Random;
import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;

public class TayChiAI extends CKPlayer {

    private byte player;
	public TayChiAI(byte player, BoardModel state) {
		super(player, state);
		teamName = "DummyAI";
        player = player;
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
		ArrayList<BoardModel> states = this.getStates(state);
		return getMove(state);
	}
	
	private BoardModel generateState(BoardModel state, int x, int y) {
		BoardModel newState = state.clone();
		newState.placePiece(new Point(x, y), this.player);
		return newState;
	}
	
	private ArrayList<BoardModel> getStates(BoardModel state) {
		// @todo: Add heuristic scoring.
		ArrayList<BoardModel> states = new ArrayList<>();
		if (state.gravityEnabled()) {
			// the board will ensure points sink.
			for (int i = 0; i < state.getWidth(); i++) {
				states.add(this.generateState(state, i, state.getHeight() - 1));
			}
		} else {
			// we can consider any move.
			for (int i = 0; i < state.getWidth(); i++) {
				for (int j = 0; j < state.getHeight(); j++) {
					if (state.getSpace(i, j) == 0)
						states.add(this.generateState(state, i, j));
				}
			}
		}
		return states;
	}
}
