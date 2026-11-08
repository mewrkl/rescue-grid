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
 * Answers "where can a rescuer go?" and "what can a rescuer reach?".
 *
 * The heart of this class is a RECURSIVE flood fill (explore). It spreads out
 * from the rescuer one step at a time, like water, and stops at walls. We use
 * it for two things:
 *   - sight: which tiles the active rescuer can see (fog of war)
 *   - scent: which survivors the Dog Handler's dog can smell
 */
public class MovementCalculator {

    public static final int SIGHT_RANGE = 4; // walking steps, tune in testing

    /** Is a single step in this direction allowed right now? */
    public boolean canStep(GameBoard board, Rescuer rescuer, Direction direction) {
        int row = rescuer.getRow() + direction.getRowChange();
        int col = rescuer.getCol() + direction.getColChange();
        Tile target = board.getTile(row, col);
        return target != null && target.isWalkable() && board.getRescuerAt(row, col) == null;
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
     * The recursive flood fill.
     *
     * best[row][col] holds the most steps we had left when we reached that tile
     * (-1 = not reached yet). It stops us exploring the same tile over and over.
     *
     * Example with stepsLeft = 2 from tile A in a corridor A-B-C-D:
     *   explore(A, 2) marks A, calls explore(B, 1)
     *   explore(B, 1) marks B, calls explore(C, 0) (and explore(A, 0), which stops at once)
     *   explore(C, 0) marks C, then stops because no steps are left. D is never reached.
     */
    private void explore(GameBoard board, int row, int col, int stepsLeft, int[][] best,
                         boolean passThroughRubble) {
        if (!board.inBounds(row, col) || stepsLeft <= best[row][col]) {
            return; // off the map, or already reached with at least as many steps
        }
        best[row][col] = stepsLeft; // this tile is reached

        Tile tile = board.getTile(row, col);
        boolean canContinue = tile.isWalkable() || (passThroughRubble && tile.isClearable());
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
