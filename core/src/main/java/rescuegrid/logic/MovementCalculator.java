package rescuegrid.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rescuegrid.model.Direction;
import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;
import rescuegrid.model.tiles.Tile;

/**
 * Movement checks, plus a recursive flood fill used for sight (fog of war)
 * and for the Dog Handler's scent.
 */
public class MovementCalculator {

    public static final int SIGHT_RANGE = 4; // walking steps

    /**
     * Is a single step in this direction allowed right now? A survivor waiting
     * there is in the way, like a person would be: walking into them helps them.
     */
    public boolean canStep(GameBoard board, Rescuer rescuer, Direction direction) {
        int row = rescuer.getRow() + direction.getRowChange();
        int col = rescuer.getCol() + direction.getColChange();
        Tile target = board.getTile(row, col);
        return target != null && target.isWalkable() && board.getRescuerAt(row, col) == null
                && board.getSurvivorAt(row, col) == null;
    }

    /** The survivor waiting on the next tile in this direction, or null. */
    public Survivor survivorAhead(GameBoard board, Rescuer rescuer, Direction direction) {
        return board.getSurvivorAt(rescuer.getRow() + direction.getRowChange(),
                rescuer.getCol() + direction.getColChange());
    }

    /** Works out what the rescuer can see, and marks it visible and explored. */
    public void computeSight(GameBoard board, Rescuer rescuer) {
        board.clearVisible();
        int[][] best = newBestArray(board);
        explore(board, rescuer.getRow(), rescuer.getCol(), SIGHT_RANGE, best, false);
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                if (best[row][col] >= 0) {
                    board.setVisible(row, col);
                    board.getTile(row, col).markExplored();
                    Survivor survivor = board.getSurvivorAt(row, col);
                    if (survivor != null) {
                        survivor.reveal();
                    }
                }
            }
        }
    }

    /** Every tile the scent reaches within `range` steps (true = reached). Used to show the range. */
    public boolean[][] scentReach(GameBoard board, Rescuer rescuer, int range) {
        int[][] best = newBestArray(board);
        explore(board, rescuer.getRow(), rescuer.getCol(), range, best, true);
        boolean[][] reached = new boolean[board.getRows()][board.getCols()];
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                reached[row][col] = best[row][col] >= 0;
            }
        }
        return reached;
    }

    /** The waiting survivors within `range` steps. Scent passes through rubble, not walls. */
    public List<Survivor> findByScent(GameBoard board, Rescuer rescuer, int range) {
        int[][] best = newBestArray(board);
        explore(board, rescuer.getRow(), rescuer.getCol(), range, best, true);
        List<Survivor> found = new ArrayList<>();
        for (Survivor survivor : board.getSurvivors()) {
            if (survivor.isWaiting() && best[survivor.getRow()][survivor.getCol()] >= 0) {
                found.add(survivor);
            }
        }
        return found;
    }

    /**
     * Recursive flood fill. best[row][col] is the most steps we had left when we
     * reached that tile (-1 = not reached), so no tile is explored twice for nothing.
     */
    private void explore(GameBoard board, int row, int col, int stepsLeft, int[][] best,
                         boolean passThroughRubble) {
        if (!board.inBounds(row, col) || stepsLeft <= best[row][col]) {
            return; // off the map, or already reached with at least as many steps
        }
        best[row][col] = stepsLeft; // this tile is reached

        Tile tile = board.getTile(row, col);
        boolean canContinue = tile.isSeeThrough() || (passThroughRubble && tile.isClearable());
        if (!canContinue || stepsLeft == 0) {
            return; // we can see a wall, but not past it
        }
        for (Direction direction : Direction.values()) {
            explore(board, row + direction.getRowChange(), col + direction.getColChange(),
                    stepsLeft - 1, best, passThroughRubble); // the recursive call
        }
    }

    /** A best[][] array the size of the board, filled with -1 ("not reached"). */
    private int[][] newBestArray(GameBoard board) {
        int[][] best = new int[board.getRows()][board.getCols()];
        for (int[] row : best) {
            Arrays.fill(row, -1);
        }
        return best;
    }
}
