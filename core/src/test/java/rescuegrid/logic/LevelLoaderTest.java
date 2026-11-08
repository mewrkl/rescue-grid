package rescuegrid.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import rescuegrid.model.GameBoard;

/** The three real levels load, and broken maps give a clear error instead of a crash. */
class LevelLoaderTest {

    private final LevelLoader loader = new LevelLoader();

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void realLevelsLoad(int levelNumber) {
        LevelData level = LevelData.forLevel(levelNumber);
        GameBoard board = loader.load(level);
        assertEquals(3, board.getRescuers().size());
        assertTrue(board.getSurvivors().size() >= level.getTarget(), "enough survivors to reach the target");
    }

    @Test
    void symbolsBecomeTheRightTiles() {
        GameBoard board = loader.parse(new String[] {
            "#######",
            "#MEDCR#",
            "#SF..X#",
            "#######",
        });
        assertTrue(board.getTile(1, 5).isClearable(), "R is rubble");
        assertTrue(board.getTile(2, 2).isBurning(), "F is fire");
        assertTrue(board.getTile(2, 5).isExit(), "X is the exit");
        assertTrue(board.getTile(1, 4).isWalkable(), "C is walkable cracked floor");
        assertEquals(1, board.getSurvivors().size(), "S adds a survivor");
    }

    @Test
    void openBorderIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> loader.parse(new String[] {
            "#####",
            "#MEDX",
            "#####",
        }));
        assertTrue(e.getMessage().contains("border"), e.getMessage());
    }

    @Test
    void missingExitIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> loader.parse(new String[] {
            "######",
            "#MED.#",
            "######",
        }));
        assertTrue(e.getMessage().contains("exit"), e.getMessage());
    }

    @Test
    void unevenRowsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> loader.parse(new String[] {
            "######",
            "#MEDX#",
            "#####",
        }));
    }

    @Test
    void unknownSymbolIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> loader.parse(new String[] {
            "#######",
            "#MEDQX#",
            "#######",
        }));
        assertTrue(e.getMessage().contains("'Q'"), e.getMessage());
    }
}
