package rescuegrid.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;

/**
 * The result screen's stars (one per goal: target, everyone, nobody down), the
 * failure reason and tip, the saved progress, and the low-time warnings.
 */
class MissionReportTest {

    private static final double FRAME = 1.0 / 60;

    private LevelData level;
    private GameBoard board;

    @BeforeEach
    void loadLevel1() {
        level = LevelData.forLevel(1); // 4 survivors, target 3
        board = new LevelLoader().load(level);
    }

    @Test
    void everyoneOutAndNobodyDownIsThreeStars() {
        rescue(4);
        MissionReport report = new MissionReport(true, board, level);
        assertEquals(3, report.getStars());
        assertNull(report.getFailReason());
        assertNull(report.getTip(), "nothing left to improve");
        assertEquals("Rescue 3 survivors", report.getGoalName(0));
        assertEquals("Rescue all 4", report.getGoalName(1));
    }

    @Test
    void eachGoalIsItsOwnStar() {
        rescue(3);
        board.getRescuers().get(1).takeDamage(Rescuer.MAX_HEALTH); // the Engineer went down
        MissionReport report = new MissionReport(true, board, level);
        assertEquals(1, report.getStars(), "only the target");
        assertTrue(report.isGoalMet(0));
        assertFalse(report.isGoalMet(1));
        assertFalse(report.isGoalMet(2));
        assertTrue(report.getTip().contains("rescue all 4"), report.getTip());

        rescue(4);
        MissionReport everyone = new MissionReport(true, board, level);
        assertEquals(2, everyone.getStars(), "target and everyone, but a role went down");
        assertTrue(everyone.getTip().contains("every role standing"), everyone.getTip());
    }

    @Test
    void aFailedMissionHasNoStarsAndSaysWhy() {
        for (Rescuer rescuer : board.getRescuers()) {
            rescuer.takeDamage(Rescuer.MAX_HEALTH);
        }
        MissionReport wipedOut = new MissionReport(false, board, level);
        assertEquals(0, wipedOut.getStars());
        assertFalse(wipedOut.isGoalMet(0));
        assertEquals("All three roles went down.", wipedOut.getFailReason());
        assertTrue(wipedOut.getTip().contains("fire"), wipedOut.getTip());

        GameBoard other = new LevelLoader().load(level);
        for (Survivor survivor : other.getSurvivors()) {
            survivor.markLost();
        }
        MissionReport lost = new MissionReport(false, other, level);
        assertTrue(lost.getFailReason().startsWith("Too many survivors were lost"), lost.getFailReason());
        assertTrue(lost.getTip().contains("Dog Handler"), lost.getTip());
    }

    @Test
    void progressOpensTheNextLevelAndKeepsTheBestStars() {
        Progress progress = new Progress();
        assertTrue(progress.isUnlocked(LevelData.TUTORIAL));
        assertTrue(progress.isUnlocked(1), "Level 1 is open even without training (SKIP TRAINING)");
        assertFalse(progress.isUnlocked(2));

        assertFalse(progress.record(1, 0, false), "a failure opens nothing and isn't a best");
        assertFalse(progress.isUnlocked(2));
        assertTrue(progress.record(1, 2, true));
        assertTrue(progress.isUnlocked(2));
        assertFalse(progress.isUnlocked(3));
        assertFalse(progress.record(1, 1, true), "fewer stars than before");
        assertEquals(2, progress.getBestStars(1));
        progress.record(2, 1, true);
        assertTrue(progress.isUnlocked(3));
        assertTrue(progress.record(3, 3, true));
        assertEquals(3, progress.getHighestUnlocked(), "there is no Level 4 to open");
    }

    @Test
    void aFoundSurvivorIsWarnedAboutAt30AndAt10SecondsLeft() {
        LevelData training = LevelData.forLevel(LevelData.TUTORIAL);
        GameBoard calm = new LevelLoader().load(training);
        TurnManager turns = new TurnManager(calm, new HazardManager(training), training);
        turns.setHazardsRunning(false); // no fire: only time runs out
        Survivor found = calm.getSurvivors().get(0);
        found.reveal();

        List<Double> warnedWithSecondsLeft = new ArrayList<>();
        while (!found.isLost() && turns.getElapsedSeconds() < 300) {
            turns.update(FRAME);
            for (GameEvent event : turns.takeEvents()) {
                if (event.getType() == GameEvent.Type.SURVIVOR_FADING) {
                    assertNotNull(event.getMessage());
                    warnedWithSecondsLeft.add(turns.getSecondsLeft(found));
                    assertTrue(turns.isFading(found));
                }
            }
        }
        assertEquals(2, warnedWithSecondsLeft.size(), "one warning at 30 s, one at 10 s, for the found one only");
        assertTrue(warnedWithSecondsLeft.get(0) <= 30 && warnedWithSecondsLeft.get(0) > 25,
                "first at 30 s: " + warnedWithSecondsLeft);
        assertTrue(warnedWithSecondsLeft.get(1) <= 10, "then at 10 s: " + warnedWithSecondsLeft);
    }

    private void rescue(int count) {
        for (int i = 0; i < count; i++) {
            board.getSurvivors().get(i).markRescued();
        }
    }
}
