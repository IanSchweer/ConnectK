import java.util.Random;
import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;

public class TayChiAI extends CKPlayer {

	public TayChiAI(byte player, BoardModel state) {
		super(player, state);
		teamName = "DummyAI";
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
		return getMove(state);
	}
}
