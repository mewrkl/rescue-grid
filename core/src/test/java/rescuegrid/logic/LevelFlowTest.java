package rescuegrid.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;
import rescuegrid.model.tiles.Tile;

/** Start at the exit, calls for help and the objective checklist (decision 0010). */
class LevelFlowTest {

    private static final double FRAME = 1.0 / 60;

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void theTeamStartsNextToTheExit(int levelNumber) {
        GameBoard board = new LevelLoader().load(LevelData.forLevel(levelNumber));
        Rescuer rescuer = board.getActiveRescuer();
        boolean exitNextToStart = false;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                Tile tile = board.getTile(rescuer.getRow() + dr, rescuer.getCol() + dc);
                exitNextToStart |= tile != null && tile.isExit();
            }
        }
        assertTrue(exitNextToStart);
    }

    @Test
    void survivorsYouHaventFoundCallForHelpWhenYouAreClose() {
        // A wall between the rescuer and the survivor: you hear them but can't see them.
        GameBoard board = new LevelLoader().parse(new String[] {
            "###############",
            "#P...#..S.....#",
            "#....#........#",
            "#............X#",
            "###############",
        });
        LevelData level = LevelData.forLevel(1);
        TurnManager turns = new TurnManager(board, new HazardManager(level), level);
        Survivor survivor = board.getSurvivors().get(0);

        assertTrue(callsWithin(turns, survivor, TurnManager.HELP_INTERVAL), "calls within one interval");

        survivor.reveal();
        assertFalse(callsWithin(turns, survivor, TurnManager.HELP_INTERVAL), "found survivors stop calling");
    }

    @Test
    void survivorsTooFarAwayCantBeHeard() {
        StringBuilder row = new StringBuilder("#P");
        for (int i = 0; i < 20; i++) {
            row.append('.');
        }
        row.append("SX#");
        StringBuilder wall = new StringBuilder();
        for (int i = 0; i < row.length(); i++) {
            wall.append('#');
        }
        GameBoard board = new LevelLoader().parse(new String[] {wall.toString(), row.toString(), wall.toString()});
        LevelData level = LevelData.forLevel(1);
        TurnManager turns = new TurnManager(board, new HazardManager(level), level);
        assertFalse(callsWithin(turns, board.getSurvivors().get(0), TurnManager.HELP_INTERVAL * 2));
    }

    @Test
    void theNextStepFollowsWhatTheSurvivorNeeds() throws InvalidMoveException {
        GameBoard board = new LevelLoader().parse(new String[] {
            "########",
            "#P!..TX#",
            "########",
        });
        LevelData level = LevelData.forLevel(1);
        TurnManager turns = new TurnManager(board, new HazardManager(level), level);
        ObjectiveTracker tracker = new ObjectiveTracker();
        for (Survivor survivor : board.getSurvivors()) {
            survivor.reveal();
        }
        assertTrue(tracker.getNextStep(board, turns).contains("Medic"), "nearest is critical");

        Survivor critical = board.getSurvivorAt(1, 2);
        critical.treat(0);
        assertTrue(tracker.getNextStep(board, turns).contains("Q"), "stabilised: call backup");
        critical.callBackup();
        assertTrue(tracker.getNextStep(board, turns).contains("Engineer"), "backup coming: next is the trapped one");

        List<Objective> list = tracker.getObjectives(board, turns, level);
        assertEquals("Find survivors  2/2", list.get(0).getText());
        assertTrue(list.get(0).isDone());
        assertFalse(list.get(1).isDone());
    }

    private static boolean callsWithin(TurnManager turns, Survivor survivor, double seconds) {
        double end = turns.getElapsedSeconds() + seconds;
        while (turns.getElapsedSeconds() < end) {
            turns.update(FRAME);
            turns.takeEvents();
            if (turns.getCallingSurvivors().contains(survivor)) {
                return true;
            }
        }
        return false;
    }
}
