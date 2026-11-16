package rescuegrid.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The main menu's start flow (decision 0024): PLAY asks about the training
 * only until it is finished or skipped, and CONTINUE appears once there is
 * progress and goes to the furthest open level.
 */
class StartFlowTest {

    @Test
    void aNewPlayerHasNothingToContinueAndIsAskedAboutTraining() {
        Progress progress = new Progress();
        assertFalse(progress.hasProgress(), "no CONTINUE on a first run");
        assertFalse(progress.isTrainingDecided(), "PLAY asks about the training");
    }

    @Test
    void skippingOrFinishingTheTrainingStopsTheQuestion() {
        Progress skipped = new Progress();
        skipped.markTrainingDecided();
        assertTrue(skipped.isTrainingDecided());
        assertTrue(skipped.hasProgress(), "CONTINUE appears");
        assertEquals(1, skipped.getContinueLevel());

        Progress finished = new Progress();
        finished.record(LevelData.TUTORIAL, 0, true);
        assertTrue(finished.isTrainingDecided(), "finishing the training counts too");
        Progress failed = new Progress();
        failed.record(LevelData.TUTORIAL, 0, false);
        assertFalse(failed.isTrainingDecided(), "leaving it unfinished does not");
    }

    @Test
    void continueGoesToTheFurthestOpenLevelAndResetForgetsEverything() {
        Progress progress = new Progress();
        progress.markTrainingDecided();
        progress.record(1, 2, true);
        progress.record(2, 1, true);
        assertEquals(3, progress.getContinueLevel());

        progress.reset();
        assertFalse(progress.hasProgress());
        assertFalse(progress.isTrainingDecided());
        assertFalse(progress.isUnlocked(2));
        assertEquals(0, progress.getBestStars(1));
    }
}
