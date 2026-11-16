package rescuegrid.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rescuegrid.model.Direction;
import rescuegrid.model.GameBoard;
import rescuegrid.model.RescueTeam;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.DogHandler;
import rescuegrid.model.rescuers.Rescuer;
import rescuegrid.model.survivors.CriticalSurvivor;

/**
 * Plays the training level the way a new player would, checking that each step
 * waits for its own action, that untaught keys and doors stay locked, that there
 * is always one thing marked to go to, that the clock only runs in the timed
 * part, and that running out of time there restarts the timed part. Walking is
 * done by teleporting (moveTo): the route isn't tested here.
 */
class TutorialTest {

    private static final double FRAME = 1.0 / 60;

    private GameBoard board;
    private TurnManager turns;
    private Tutorial tutorial;

    @BeforeEach
    void startTraining() {
        LevelData level = LevelData.forLevel(LevelData.TUTORIAL);
        board = new LevelLoader().load(level);
        turns = new TurnManager(board, new HazardManager(level), level);
        tutorial = new Tutorial();
        tutorial.begin(turns);
        assertEquals("TRAINING", level.getTitle());
    }

    @Test
    void theTrainingCanBeFinishedStepByStep() throws InvalidMoveException {
        playToTheTimedPart();

        // 8: the hidden survivor is behind rubble: only the dog can find them
        assertTrue(tutorial.isClockRunning());
        assertEquals(4, tutorial.getChapterNumber());
        assertArrayEquals(new int[] {6, 26}, tutorial.takeDoorToShow(), "the hall door nearest you is shown");
        assertTrue(tutorial.canUseAbility("Scent"));
        assertFalse(tutorial.canUseAbility("Clear Rubble"), "no digging at random: the dog finds them first");
        switchTo(2);
        Rescuer dogHandler = board.getActiveRescuer();
        dogHandler.tick(dogHandler.getCooldownSeconds()); // the practice sniff (no time passes in this test)
        dogHandler.moveTo(6, 26);
        tutorial.update(board, turns); // through the door: nothing marked while you search
        assertNull(tutorial.getMarker(board));
        dogHandler.moveTo(7, 26);
        String nothing = dogHandler.specialAction(board);
        tutorial.onAbility(dogHandler.getSpecialName());
        assertFalse(nothing.contains("found"), nothing); // only the red survivor, already found, is near
        expectStep(8);
        assertTrue(tutorial.getPrompt().startsWith("Nobody near here"), tutorial.getPrompt());

        dogHandler.tick(dogHandler.getCooldownSeconds());
        dogHandler.moveTo(9, 20);
        String found = dogHandler.specialAction(board);
        assertTrue(found.contains("found"), found);
        tutorial.onAbility(dogHandler.getSpecialName());
        expectStep(9);

        // 9: the Engineer clears the rubble in front of their room
        assertArrayEquals(new int[] {10, 16}, tutorial.getMarker(board), "the rubble to clear is marked");
        assertTrue(tutorial.canUseAbility("Clear Rubble"));
        switchTo(1);
        Rescuer engineer = board.getActiveRescuer();
        engineer.tick(engineer.getCooldownSeconds());
        engineer.moveTo(9, 16);
        engineer.setFacing(Direction.DOWN);
        engineer.specialAction(board);
        expectStep(10);

        // 10: the aftershock, warned about, then it hits; then carry them out
        assertTrue(turns.isAftershockComing());
        assertTrue(tutorial.getPrompt().startsWith("AFTERSHOCK"), tutorial.getPrompt());
        assertNull(tutorial.getMarker(board), "first: step off the orange tiles");
        runFor(TurnManager.WARNING_SECONDS + 0.5);
        assertFalse(turns.isAftershockComing());
        expectStep(10); // now carry them out (backup carries the critical one out meanwhile)
        Survivor hidden = board.getSurvivorAt(12, 16);
        assertArrayEquals(new int[] {12, 16}, tutorial.getMarker(board));
        engineer.moveTo(11, 16);
        hidden.reachedBy(engineer);
        assertNull(tutorial.getMarker(board), "carrying them: the game points at the EXIT instead");
        engineer.moveTo(1, 1);
        engineer.deliverAll();
        runFor(CriticalSurvivor.BACKUP_SECONDS + 1);
        expectStep(11);
        assertEquals(11, tutorial.getStepCount(), "17 steps merged into 11 (decision 0023)");
        assertTrue(turns.evacuate());
        assertTrue(turns.isSuccess());
    }

    @Test
    void nothingWeakensOrCollapsesBeforeTheTimedPart() {
        runFor(90);
        for (Survivor survivor : board.getSurvivors()) {
            assertEquals(survivor.getMaxCondition(), survivor.getCondition());
        }
        assertFalse(turns.isAftershockComing());
        assertTrue(board.getTile(8, 9).isBurning(), "fire stays put");
        assertTrue(board.getTile(8, 8).isWalkable(), "and doesn't spread");
    }

    @Test
    void runningOutOfTimeRestartsTheTimedPart() throws InvalidMoveException {
        playToTheTimedPart();
        Survivor hidden = board.getSurvivorAt(12, 16);
        runFor(80); // the serious survivor lasts 25 ticks = 75 s
        assertTrue(hidden.isLost());

        assertTrue(tutorial.retryIfLost(board));
        assertFalse(hidden.isLost());
        assertFalse(hidden.isRevealed());
        assertEquals(hidden.getMaxCondition(), hidden.getCondition());
        assertEquals(12, hidden.getRow());
        assertEquals(16, hidden.getCol());
        assertEquals(4, board.getActiveRescuer().getRow());
        assertEquals(27, board.getActiveRescuer().getCol());
        assertFalse(turns.isMissionOver(), "training doesn't fail: the timed part starts again");
        expectStep(8);
    }

    /** Steps 1 to 7, the calm part, ending as the Medic next to the critical survivor. */
    private void playToTheTimedPart() throws InvalidMoveException {
        expectStep(1);
        assertEquals(1, tutorial.getChapterNumber());
        assertFalse(tutorial.canChangeRole());
        assertFalse(tutorial.canUseAbility("Clear Rubble"));
        assertFalse(tutorial.canUseAbility("Treat"));
        assertFalse(tutorial.canUseAbility("Scent"));
        assertFalse(tutorial.canCallBackup());
        assertFalse(board.getTile(3, 6).isWalkable(), "the first door starts closed");
        assertNull(tutorial.getMarker(board));

        // 1: walk two ways
        tutorial.onDirection(Direction.UP);
        expectStep(1);
        tutorial.onDirection(Direction.RIGHT);
        expectStep(2);

        // 2-4: become each role and test its E in the first room
        assertTrue(tutorial.canChangeRole());
        assertTrue(tutorial.canUseAbility("Clear Rubble"));
        assertFalse(tutorial.canUseAbility("Scent"));
        assertNull(tutorial.getMarker(board), "first press 2: the rubble is marked once you're the Engineer");
        assertTrue(tutorial.getPrompt().contains("Press 2"), tutorial.getPrompt());
        switchTo(1);
        assertArrayEquals(new int[] {5, 2}, tutorial.getMarker(board), "the practice rubble");
        assertTrue(tutorial.getPrompt().contains("Test your ability"), tutorial.getPrompt());
        Rescuer practising = board.getActiveRescuer();
        practising.moveTo(4, 2);
        practising.setFacing(Direction.DOWN);
        practising.specialAction(board);
        tutorial.onAbility(practising.getSpecialName());
        expectStep(3);
        assertTrue(tutorial.canUseAbility("Scent"));
        assertFalse(tutorial.canUseAbility("Clear Rubble"));
        switchTo(2);
        Rescuer sniffing = board.getActiveRescuer();
        String scent = sniffing.specialAction(board);
        assertEquals("No scent within 8 steps. Search further in", scent,
                "the closed door keeps the scent in this room");
        assertEquals(DogHandler.MISS_COOLDOWN_SECONDS, sniffing.getSpecialCooldown(), 1e-9,
                "a sniff that finds nobody only costs a short recharge");
        tutorial.onAbility(sniffing.getSpecialName());
        expectStep(4);
        assertTrue(tutorial.canUseAbility("Treat"));
        switchTo(0);
        assertArrayEquals(new int[] {4, 4}, tutorial.getMarker(board), "the dummy");
        assertEquals("DUMMY: E", tutorial.getMarkerLabel());
        Rescuer medicPractising = board.getActiveRescuer();
        medicPractising.moveTo(4, 3);
        medicPractising.setFacing(Direction.RIGHT);
        assertTrue(medicPractising.specialAction(board).startsWith("Treated"));
        tutorial.onAbility(medicPractising.getSpecialName());
        expectStep(5);
        assertFalse(tutorial.canUseAbility("Treat"), "back to locked until the red survivor");

        // 5: a door opens: the camera is sent there once, and it stays marked until you go through
        assertEquals(2, tutorial.getChapterNumber());
        assertTrue(board.getTile(3, 6).isWalkable(), "the first door opens");
        assertArrayEquals(new int[] {3, 6}, tutorial.takeDoorToShow());
        assertNull(tutorial.takeDoorToShow(), "the camera shows it only once");
        assertArrayEquals(new int[] {3, 6}, tutorial.getMarker(board));
        assertTrue(tutorial.isShowingDoor());
        Rescuer medic = board.getActiveRescuer();
        medic.moveTo(3, 6);
        tutorial.update(board, turns);
        assertFalse(tutorial.isShowingDoor(), "through the door: the marker moves on");

        // 5, continued: find the minor survivor, they follow, lead them out (one step, three parts)
        assertTrue(tutorial.getPrompt().contains("calling for HELP"), tutorial.getPrompt());
        Survivor minor = board.getSurvivorAt(4, 10);
        minor.reveal();
        expectStep(5);
        assertTrue(tutorial.getPrompt().startsWith("Green tag"), tutorial.getPrompt());
        assertArrayEquals(new int[] {4, 10}, tutorial.getMarker(board));
        medic.moveTo(4, 9);
        minor.reachedBy(medic);
        expectStep(5);
        assertTrue(tutorial.getPrompt().startsWith("Lead them"), tutorial.getPrompt());
        assertNull(tutorial.getMarker(board), "leading them: the game points at the EXIT instead");
        medic.moveTo(1, 1);
        medic.deliverAll();
        expectStep(6);

        // 6: Engineer clears rubble, lifts the debris, then the serious survivor is carried out
        assertEquals(3, tutorial.getChapterNumber());
        assertArrayEquals(new int[] {3, 15}, tutorial.getMarker(board), "the rubble to clear is marked");
        assertTrue(tutorial.canUseAbility("Clear Rubble"));
        assertFalse(tutorial.canUseAbility("Treat"));
        switchTo(1);
        Rescuer engineer = board.getActiveRescuer();
        engineer.tick(engineer.getCooldownSeconds()); // the practice used it a moment ago (no time passes here)
        engineer.moveTo(3, 14);
        engineer.setFacing(Direction.RIGHT);
        engineer.specialAction(board);
        tutorial.onAbility(engineer.getSpecialName());
        expectStep(6);
        assertEquals("TRAPPED: E", tutorial.getMarkerLabel());
        assertArrayEquals(new int[] {4, 20}, tutorial.getMarker(board), "then the trapped survivor is marked");
        engineer.tick(engineer.getCooldownSeconds());
        engineer.moveTo(4, 19);
        engineer.setFacing(Direction.RIGHT);
        engineer.specialAction(board); // lifts the debris
        expectStep(6);
        assertEquals("WALK INTO THEM", tutorial.getMarkerLabel());
        assertArrayEquals(new int[] {4, 20}, tutorial.getMarker(board), "free: now walk into them");
        assertFalse(board.getTile(3, 24).isWalkable(), "the critical survivor's room is still closed");
        Survivor trapped = board.getSurvivorAt(4, 20);
        trapped.reachedBy(engineer);
        engineer.moveTo(1, 1);
        engineer.deliverAll();
        expectStep(7);

        // 7: the next door is shown, the Medic stabilises the critical survivor, Q calls backup
        assertTrue(board.getTile(3, 24).isWalkable(), "the critical survivor's room opens");
        assertArrayEquals(new int[] {3, 24}, tutorial.takeDoorToShow());
        assertFalse(tutorial.canCallBackup());
        switchTo(0);
        Rescuer medicAgain = board.getActiveRescuer();
        medicAgain.tick(medicAgain.getCooldownSeconds());
        medicAgain.moveTo(3, 24);
        tutorial.update(board, turns);
        assertArrayEquals(new int[] {4, 28}, tutorial.getMarker(board), "the critical survivor is marked");
        medicAgain.moveTo(4, 27);
        board.getSurvivorAt(4, 28).reveal(); // in the game, sight reveals them
        medicAgain.setFacing(Direction.RIGHT);
        medicAgain.specialAction(board);
        tutorial.onAbility(medicAgain.getSpecialName());
        expectStep(7);
        assertEquals("PRESS Q", tutorial.getMarkerLabel());
        assertTrue(tutorial.canCallBackup());
        assertFalse(tutorial.isClockRunning());
        medicAgain.callBackup(board);
        tutorial.onBackupCalled();
        expectStep(8);
    }

    private void switchTo(int role) throws InvalidMoveException {
        board.getTeam().switchTo(role);
        board.getTeam().tick(RescueTeam.GEAR_CHANGE_SECONDS);
        tutorial.update(board, turns); // GamePanel updates the tutorial every frame
    }

    private void runFor(double seconds) {
        double end = turns.getElapsedSeconds() + seconds;
        while (turns.getElapsedSeconds() < end) {
            turns.update(FRAME);
            turns.takeEvents();
            tutorial.update(board, turns);
        }
    }

    private void expectStep(int number) {
        tutorial.update(board, turns);
        assertEquals(number, tutorial.getStepNumber(), tutorial.getPrompt());
    }
}
