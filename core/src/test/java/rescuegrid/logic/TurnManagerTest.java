package rescuegrid.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;

/**
 * Runs each level's world tick for up to 5 minutes with nobody moving, the same
 * way the game loop does (60 small steps per second).
 */
class TurnManagerTest {

    private static final double FRAME = 1.0 / 60;

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void aftershockComesOnTimeWithAWarningAndCollapsesCrackedFloor(int levelNumber) {
        LevelData level = LevelData.forLevel(levelNumber);
        LevelLoader loader = new LevelLoader();
        GameBoard board = loader.load(level);
        String[] rows = loader.readLines(level.getFileName());
        TurnManager turns = new TurnManager(board, new HazardManager(level), level);

        double aftershockAt = -1;
        double warningAt = -1;
        while (aftershockAt < 0 && turns.getElapsedSeconds() < 300) {
            turns.update(FRAME);
            for (GameEvent event : turns.takeEvents()) {
                if (event.getType() == GameEvent.Type.AFTERSHOCK_WARNING && warningAt < 0) {
                    warningAt = turns.getElapsedSeconds();
                }
                if (event.getType() == GameEvent.Type.AFTERSHOCK) {
                    aftershockAt = turns.getElapsedSeconds();
                }
            }
        }

        double expected = level.getAftershockTicks() * TurnManager.TICK_SECONDS;
        assertEquals(expected, aftershockAt, 0.1, "first aftershock");
        assertEquals(expected - TurnManager.WARNING_SECONDS, warningAt, 0.1, "warning 5 s before");
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length(); col++) {
                if (rows[row].charAt(col) == 'C') {
                    assertTrue(board.getTile(row, col).isClearable(),
                            "cracked floor at (" + row + "," + col + ") became rubble");
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void doingNothingFailsTheMissionAndFireBurnsOut(int levelNumber) {
        LevelData level = LevelData.forLevel(levelNumber);
        GameBoard board = new LevelLoader().load(level);
        TurnManager turns = new TurnManager(board, new HazardManager(level), level);

        while (!turns.isMissionOver() && turns.getElapsedSeconds() < 300) {
            turns.update(FRAME);
            turns.takeEvents();
        }

        assertTrue(turns.isMissionOver(), "survivors run out of time");
        assertFalse(turns.isSuccess());
        // The mission ends when the longest-lasting survivor runs out (fire can only make it sooner).
        assertTrue(turns.getElapsedSeconds() <= longestLife(level) * TurnManager.TICK_SECONDS + 0.1);

        int burnt = 0;
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                if (board.getTile(row, col).getImageKey().equals("burnt_floor")) {
                    burnt++;
                }
            }
        }
        assertTrue(burnt > 0, "some fire has burned out into burnt floor");
    }

    @Test
    void theTeamCanOnlyLeaveOnceTheTargetIsRescued() {
        LevelData level = LevelData.forLevel(1);
        GameBoard board = new LevelLoader().load(level);
        TurnManager turns = new TurnManager(board, new HazardManager(level), level);

        assertFalse(turns.evacuate(), "can't leave with nobody rescued");
        assertFalse(turns.isMissionOver());
        assertEquals(level.getTarget(), turns.getStillNeeded());

        for (int i = 0; i < level.getTarget(); i++) {
            board.getSurvivors().get(i).markRescued();
        }
        assertEquals(0, turns.getStillNeeded());
        assertFalse(turns.isMissionOver(), "a survivor is still inside, so the level waits for the player");
        assertEquals(level.getSurvivorCount() - level.getTarget(), board.getStillInsideCount());

        assertTrue(turns.evacuate());
        assertTrue(turns.isMissionOver());
        assertTrue(turns.isSuccess());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void theWarningShowsExactlyTheTilesThatCollapse(int levelNumber) {
        LevelData level = LevelData.forLevel(levelNumber);
        GameBoard board = new LevelLoader().load(level);
        TurnManager turns = new TurnManager(board, new HazardManager(level), level);
        while (!turns.isAftershockComing()) {
            turns.update(FRAME);
        }
        assertTrue(countRandomCollapses(board, turns) > 0, "some floor is marked as well as cracked floor");
        assertTrue(countRandomCollapses(board, turns) <= HazardManager.RANDOM_COLLAPSES);

        // What the player sees on the last frame before the aftershock is what collapses.
        List<int[]> marked = new ArrayList<>();
        while (turns.isAftershockComing()) {
            marked = markedTiles(board, turns);
            turns.update(FRAME);
        }
        assertFalse(marked.isEmpty());
        for (int[] tile : marked) {
            // Fire can still spread onto a marked tile in the same tick, just before the quake.
            boolean collapsed = board.getTile(tile[0], tile[1]).isClearable();
            boolean burning = board.getTile(tile[0], tile[1]).isBurning();
            assertTrue(collapsed || burning, "marked tile (" + tile[0] + "," + tile[1] + ") became rubble");
        }
    }

    /** World ticks until the slowest-draining survivor in the level is lost, if nobody helps. */
    private static int longestLife(LevelData level) {
        int longest = 0;
        for (Survivor survivor : new LevelLoader().load(level).getSurvivors()) {
            int loss = survivor.getConditionLossPerTick();
            longest = Math.max(longest, (survivor.getMaxCondition() + loss - 1) / loss);
        }
        return longest;
    }

    private static List<int[]> markedTiles(GameBoard board, TurnManager turns) {
        List<int[]> marked = new ArrayList<>();
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                if (turns.willCollapse(row, col)) {
                    marked.add(new int[] {row, col});
                }
            }
        }
        return marked;
    }

    private static int countRandomCollapses(GameBoard board, TurnManager turns) {
        int count = 0;
        for (int[] tile : markedTiles(board, turns)) {
            if (!board.getTile(tile[0], tile[1]).collapsesInAftershock()) {
                count++;
            }
        }
        return count;
    }
}
