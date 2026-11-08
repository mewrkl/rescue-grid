package rescuegrid.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rescuegrid.model.Direction;
import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;

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
        // TODO (Week 1, Member 2): work out the target row/col
        //  (rescuer.getRow() + direction.getRowChange(), same for col).
        //  Return true only if the target tile is in bounds, isWalkable(),
        //  and board.getRescuerAt(...) is null (no other rescuer there).
        return false;
    }

    /** Works out what the rescuer can see, and marks it visible and explored. */
    public void computeSight(GameBoard board, Rescuer rescuer) {
        // TODO (Week 2, Member 2):
        //  1. board.clearVisible().
        //  2. int[][] best = newBestArray(board).
        //  3. explore(board, rescuer.getRow(), rescuer.getCol(), SIGHT_RANGE, best).
        //  4. For every (row, col) where best[row][col] >= 0: board.setVisible(row, col),
        //     mark the tile explored, and reveal() any survivor standing there.
    }

    /** The survivors within `range` walking steps (used by the Dog Handler). */
    public List<Survivor> findByScent(GameBoard board, Rescuer rescuer, int range) {
        List<Survivor> found = new ArrayList<>();
        // TODO (Week 2, Member 2): same flood fill as computeSight, but with `range`,
        //  then add every survivor whose tile has best[row][col] >= 0.
        return found;
    }

    /**
     * The recursive flood fill.
     *
     * best[row][col] holds the most steps we had left when we reached that tile
     * (-1 = not reached yet). It stops us exploring the same tile over and over.
     */
    private void explore(GameBoard board, int row, int col, int stepsLeft, int[][] best) {
        // TODO (Week 2, Member 2):
        //  1. Stop (return) if (row, col) is out of bounds,
        //     or if stepsLeft <= best[row][col] (we already got here with more steps).
        //  2. best[row][col] = stepsLeft.          <- the tile is reached (and seen)
        //  3. Stop if the tile is not walkable (we can see a wall, but not past it),
        //     or if stepsLeft == 0.
        //  4. Call explore(...) on the 4 neighbours with stepsLeft - 1.   <- the recursion
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
