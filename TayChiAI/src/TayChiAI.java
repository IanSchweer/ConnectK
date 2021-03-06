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
	private int depth = 4;
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
		return minMax(state, depth);
    }

    @Override
    public Point getMove(BoardModel state, int deadline) {
        return minMax(state, depth);
    }
    
    private Point minMax(BoardModel state, int depth) {
        BoardModel newState = maxDecision(state, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return newState.lastMove;
    }
    
    // @todo: Add terminal state checks for decisions
    private BoardModel maxDecision(BoardModel state, int depth, int alpha, int beta) {
        int max = Integer.MIN_VALUE;
        BoardModel maxDecision = state;
        ArrayList<BoardModel> states = this.getStates(state, this.player);
		if (depth <= 0) {
			for (BoardModel newState : states) {
				int newStateScore = scoreSetCount(newState, this.player, this.opponent);
				if (newStateScore >= max) {
					max = newStateScore;
					maxDecision = newState;
				}
			}
			return maxDecision;
		} else {
			for (BoardModel newState : states) {
				int newStateScore = scoreSetCount(minDecision(newState, depth - 1, alpha, beta), this.player, this.opponent);
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
    }
    
    private BoardModel minDecision(BoardModel state, int depth, int alpha, int beta) {
        int min = Integer.MIN_VALUE;
        BoardModel minDecision = state;
        ArrayList<BoardModel> states = this.getStates(state, this.opponent);
        if (depth <= 0) {
			for (BoardModel newState : states) {
				int newStateScore = scoreSetCount(newState, this.opponent, this.player);
				if (newStateScore <= min) {
					min = newStateScore;
					minDecision = newState;
				}
			}
			return minDecision;
		} else {
			for (BoardModel newState : states) {
				int newStateScore = scoreSetCount(maxDecision(newState, depth - 1, alpha, beta), this.opponent, this.player);
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
    }

    private BoardModel generateState(BoardModel state, int x, int y, byte player) {
        // undocumented, but placePiece will generate a new board.
        BoardModel newState = state.placePiece(new Point(x, y), player);
        return newState;
    }
    
    private ArrayList<BoardModel> getStates(BoardModel state, byte player) {
        // @todo: Add utility function.
        ArrayList<BoardModel> states = new ArrayList<>();
        final int TARGET_WIDTH = state.getWidth(), TARGET_HEIGHT = state.getHeight();
        for (int i = 0; i < TARGET_WIDTH; i++) {
            for (int j = 0; j < TARGET_HEIGHT; j++) {
                if (state.getSpace(i, j) == 0) {
                    // With gravity on, we only want the lowest unfilled row in 
                    // this column. Otherwise we can consider any spot.
                    if (!state.gravityEnabled() || (j == 0 || state.getSpace(i, j - 1) != 0)) {
                        states.add(this.generateState(state, i, j, player));
                    }
                }
            }
        }
        return states;
    }
    
    private int scoreSetCount(BoardModel state, byte player, byte opponent) {
        // this method is a little costly BUT
        // 1. count all the groupings of 2,3,4,...,k-1 groupings in either
        //  1. Horizontal
        //  2. Vertical
        //  3. Diagonial
        // (note: a "grouping" is the maximum number of uninterrupted pieces on the board. xx, and x     x count as 2)
        // 2. return a weighted linear combination of these metrics.
        // most importantly, this is THREADABLE
        float a = horizontalScore(state, player);
        float b = horizontalScore(state, opponent);
        float c = verticalScore(state, player);
        float d = verticalScore(state, opponent);
        /*
		float maxScore = horizontalScore(state, player) +
			verticalScore(state, player) +
			diagonalScore(state, player);
		float minScore = horizontalScore(state, opponent) +
			verticalScore(state, opponent) +
			diagonalScore(state, opponent);*/
        float maxScore = a + c;
        float minScore = b + d;
        System.out.println("[horizontal,vertical] (" + a +" " + c + ") (" + b + " " + d + ")");
		return (int)Math.floor(maxScore - minScore);
    }

	private float verticalScore(BoardModel state, byte player) {
		float score = Float.MIN_VALUE;
		for (int i = 0; i < this.numRows; i++) {
			int l = 0, r = 0, distance = Integer.MAX_VALUE;
			int consecutive = 0, total_consecutive = 1;
			for (l = 0; l < this.numCols && state.getSpace(i, l) != player; l++) { }
			for (r = l + 1; r < this.numCols; r++) { 
				if (r - l - 1 == distance) {
					consecutive++;	
				}
				else if (state.getSpace(i, r) == player) {
					// count the distance, move l;
					distance = Math.min(distance, r - l);
					l = r;
					total_consecutive = Math.max(consecutive, total_consecutive);
					consecutive = 0;
				}
			}
			score = Math.max(( total_consecutive) / distance, score);
		}
		return score;
	}

	private float horizontalScore(BoardModel state, byte player) {
		float score = Float.MIN_VALUE;
		for (int i = 0; i < this.numCols; i++) {
			int l = 0, r = 0, distance = Integer.MAX_VALUE;
			int consecutive = 0, total_consecutive = 1;
			for (l = 0; l < this.numRows && state.getSpace(l, i) != player; l++) { }
			for (r = l + 1; r < this.numCols; r++) { 
				if (r - l - 1 == distance) {
					consecutive++;	
				}
				else if (state.getSpace(r, i) == player) {
					// count the distance, move l;
					distance = Math.min(distance, r - l);
					l = r;
					total_consecutive = Math.max(consecutive, total_consecutive);
					consecutive = 0;
				}
			}
			score = Math.max(( total_consecutive) / distance, score);
		}
		return score;
	}

	private float diagonalScore(BoardModel state, byte player) {
		return 0.0f;
        /*
		float score = Float.MIN_VALUE;
		for (int i = 0; i < this.numCols; i++) {
			int l = 0, r = 0, distance = Integer.MAX_VALUE;
			int consecutive = 0, total_consecutive = 1;
			for (l = 0; l < this.numRows && state.getSpace(l, i) != player; l++) { }
			for (r = l + 1; r < this.numCols; r++) { 
				if (r - l - 1 == distance) {
					consecutive++;	
				}
				else if (state.getSpace(r, i) == player) {
					// count the distance, move l;
					distance = Math.min(distance, r - l - 1);
					l = r;
					total_consecutive = Math.max(consecutive, total_consecutive);
					consecutive = 0;
				}
			}
			score = Math.max((1.0f / distance) * total_consecutive, score);
		}
		return score;*/
	}


    private int score(BoardModel state) {
        // we should consider gravity when scoring. But I'm not right now.
        // suck it.
        // This simple returns the largest numbers of contigous spots.
        int n = state.getWidth(), m = state.getHeight();
        int max = Integer.MIN_VALUE, min = Integer.MIN_VALUE;
        int[][] board = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
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
                if (state.getSpace(i, j) != this.opponent) {
                    board[i][j] = 0;
                } else {
                    board[i][j] = Math.max(
                            board[Math.max(i - 1, 0)][j] + 1, 
                            board[i][Math.max(j - 1, 0)] + 1
                    );
                    min = Math.max(board[i][j], min);
                }
            }
        }
		System.out.println("Min = " + min + " Max = " + max);
        return max - min;
    }
}
