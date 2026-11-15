package rescuegrid.logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rescuegrid.model.Direction;
import rescuegrid.model.GameBoard;
import rescuegrid.model.rescuers.Rescuer;

/** One-tile steps and the recursive flood fill used for sight. */
class MovementCalculatorTest {

    private final LevelLoader loader = new LevelLoader();
    private final MovementCalculator movement = new MovementCalculator();

    /** A small room: walls around, a wall in the middle, rubble on the right. */
    private static final String[] ROOM = {
        "#########",
        "#P..#..R#",
        "#...#...#",
        "#.......#",
        "#......X#",
        "#########",
    };

    @Test
    void wallsBlockSteps() {
        GameBoard board = loader.parse(ROOM);
        Rescuer medic = board.getActiveRescuer(); // at (1,1)
        assertFalse(movement.canStep(board, medic, Direction.UP), "wall");
        assertFalse(movement.canStep(board, medic, Direction.LEFT), "wall");
        assertTrue(movement.canStep(board, medic, Direction.RIGHT), "floor");
    }

    @Test
    void sightStopsAtWalls() {
        GameBoard board = loader.parse(ROOM);
        Rescuer medic = board.getRescuers().get(0);
        movement.computeSight(board, medic);
        assertTrue(board.isVisible(1, 1), "own tile");
        assertTrue(board.isVisible(1, 3), "3 steps away in the same room");
        assertTrue(board.isVisible(1, 4), "the wall itself is seen");
        assertFalse(board.isVisible(1, 5), "behind the wall");
        assertTrue(board.getTile(1, 3).isExplored(), "seen tiles are remembered");
    }

    @Test
    void cannotSeeTheStoreroomFromTheStartOfLevelOne() {
        GameBoard board = loader.load(LevelData.forLevel(1));
        movement.computeSight(board, board.getRescuers().get(0));
        assertFalse(board.isVisible(2, 3));
    }
}
