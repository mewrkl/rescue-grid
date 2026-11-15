package rescuegrid.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;

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
            "#P..CR#",
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
            "#P..X",
            "#####",
        }));
        assertTrue(e.getMessage().contains("border"), e.getMessage());
    }

    @Test
    void missingExitIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> loader.parse(new String[] {
            "######",
            "#P...#",
            "######",
        }));
        assertTrue(e.getMessage().contains("exit"), e.getMessage());
    }

    @Test
    void unevenRowsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> loader.parse(new String[] {
            "######",
            "#P..X#",
            "#####",
        }));
    }

    @Test
    void unknownSymbolIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> loader.parse(new String[] {
            "#######",
            "#P..QX#",
            "#######",
        }));
        assertTrue(e.getMessage().contains("'Q'"), e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void survivorsOnlyAppearOnTheirSpots(int levelNumber) {
        LevelData level = LevelData.forLevel(levelNumber);
        String[] rows = loader.readLines(level.getFileName());
        for (int seed = 0; seed < 50; seed++) {
            GameBoard board = loader.load(level, new Random(seed));
            assertEquals(level.getSurvivorCount(), board.getSurvivors().size());
            for (Survivor survivor : board.getSurvivors()) {
                char symbol = rows[survivor.getRow()].charAt(survivor.getCol());
                assertTrue(symbol == 'S' || symbol == '?', "survivor on '" + symbol + "'");
            }
        }
    }

    @Test
    void survivorsMoveBetweenPlays() {
        LevelData level = LevelData.forLevel(1);
        Set<String> layouts = new HashSet<>();
        for (int seed = 0; seed < 20; seed++) {
            StringBuilder layout = new StringBuilder();
            for (Survivor survivor : loader.load(level, new Random(seed)).getSurvivors()) {
                layout.append(survivor.getRow()).append(',').append(survivor.getCol()).append(' ');
            }
            layouts.add(layout.toString());
        }
        assertTrue(layouts.size() > 1, "different plays give different survivor spots");
    }

    @Test
    void tooFewSpotsIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> loader.parse(new String[] {
            "#######",
            "#P..SX#",
            "#?....#",
            "#######",
        }, Survivor.DEFAULT_CONDITION, 3, new Random(0)));
        assertTrue(e.getMessage().contains("possible spots"), e.getMessage());
    }
}
