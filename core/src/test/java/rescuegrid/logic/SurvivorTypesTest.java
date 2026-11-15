package rescuegrid.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rescuegrid.model.Direction;
import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;
import rescuegrid.model.survivors.CriticalSurvivor;

/** The triage types (decision 0008) and the backup countdown (decision 0009). */
class SurvivorTypesTest {

    private static final double FRAME = 1.0 / 60;

    // Row 1: start, minor, serious, critical, trapped serious, exit.
    private static final String[] MAP = {
        "##########",
        "#Pm.S!.TX#",
        "#........#",
        "##########",
    };

    private GameBoard board;
    private Rescuer medic;
    private Survivor minor;
    private Survivor serious;
    private CriticalSurvivor critical;
    private Survivor trapped;

    @BeforeEach
    void setUp() {
        board = new LevelLoader().parse(MAP);
        medic = board.getActiveRescuer();
        minor = board.getSurvivorAt(1, 2);
        serious = board.getSurvivorAt(1, 4);
        critical = (CriticalSurvivor) board.getSurvivorAt(1, 5);
        trapped = board.getSurvivorAt(1, 7);
    }

    @Test
    void theLoaderCreatesTheRightTypes() {
        assertEquals("Minor", minor.getInjuryName());
        assertEquals("Serious", serious.getInjuryName());
        assertEquals("Critical", critical.getInjuryName());
        assertTrue(trapped.isTrapped());
        assertEquals("Serious", trapped.getInjuryName());
    }

    @Test
    void aMinorSurvivorFollowsOneTileBehindAndIsSavedAtTheExit() throws InvalidMoveException {
        medic.moveTo(1, 2);
        minor.reachedBy(medic);
        assertTrue(medic.isLeading());
        assertFalse(medic.isCarrying(), "hands stay free");
        assertEquals(Rescuer.STEP_DELAY, medic.getStepDelay(), "full speed while leading");
        assertNull(board.getSurvivorAt(1, 2), "a follower is no longer lying on the floor");

        medic.moveTo(1, 3);
        assertEquals(2, minor.getCol(), "steps into the tile the rescuer left");
        medic.moveTo(2, 3);
        assertEquals(1, minor.getRow());
        assertEquals(3, minor.getCol());

        // Leading and carrying at the same time.
        medic.moveTo(1, 4);
        serious.reachedBy(medic);
        assertTrue(medic.isCarrying());
        assertEquals(2, medic.deliverAll());
        assertTrue(minor.isRescued());
        assertTrue(serious.isRescued());
    }

    @Test
    void onlyOneFollowerAndOneCarriedAtATime() throws InvalidMoveException {
        Survivor otherMinor = new LevelLoader().parse(new String[] {"#####", "#Pmm#", "#..X#", "#####"})
                .getSurvivorAt(1, 3);
        minor.reachedBy(medic);
        assertThrows(InvalidMoveException.class, () -> otherMinor.reachedBy(medic));
        serious.reachedBy(medic);
        assertThrows(InvalidMoveException.class, () -> trappedFreed().reachedBy(medic));
    }

    private Survivor trappedFreed() throws InvalidMoveException {
        trapped.freeFromDebris();
        return trapped;
    }

    @Test
    void typesDrainAtDifferentSpeeds() {
        assertTrue(minor.getConditionLossPerTick() < serious.getConditionLossPerTick());
        assertTrue(serious.getConditionLossPerTick() < critical.getConditionLossPerTick());
        int before = critical.getCondition();
        critical.tickCondition();
        assertEquals(before - 3, critical.getCondition());
    }

    @Test
    void aCriticalSurvivorCantBeMovedAndNeedsTheMedicThenBackup() throws InvalidMoveException {
        medic.moveTo(1, 5);
        InvalidMoveException e = assertThrows(InvalidMoveException.class, () -> critical.reachedBy(medic));
        assertTrue(e.getMessage().contains("stabilise"), e.getMessage());
        assertThrows(InvalidMoveException.class, () -> medic.callBackup(board), "must be stabilised first");

        String message = medic.specialAction(board); // the Medic treats the survivor on her tile
        assertTrue(critical.isStabilised(), message);
        assertEquals(1, critical.getConditionLossPerTick(), "stabilised: drains slowly");

        medic.callBackup(board);
        assertTrue(critical.isBackupComing());
        assertThrows(InvalidMoveException.class, () -> medic.callBackup(board), "only called once");
    }

    @Test
    void backupCarriesThemOutAfterTheCountdownEvenIfYouLeave() throws InvalidMoveException {
        LevelData level = LevelData.forLevel(1);
        TurnManager turns = new TurnManager(board, new HazardManager(level.getSeed()), level);
        critical.treat(0);
        critical.callBackup();
        medic.moveTo(2, 1); // walk away: you don't have to wait

        boolean arrived = false;
        while (turns.getElapsedSeconds() < CriticalSurvivor.BACKUP_SECONDS - 0.1) {
            turns.update(FRAME);
            arrived |= hasEvent(turns, GameEvent.Type.BACKUP_ARRIVED);
        }
        assertFalse(critical.isRescued(), "not yet");
        while (turns.getElapsedSeconds() < CriticalSurvivor.BACKUP_SECONDS + 0.1) {
            turns.update(FRAME);
            arrived |= hasEvent(turns, GameEvent.Type.BACKUP_ARRIVED);
        }
        assertTrue(critical.isRescued());
        assertTrue(arrived, "the game is told so it can play a sound");
        assertEquals(1, board.getRescuedCount());
    }

    @Test
    void onlyCriticalSurvivorsNeedBackup() {
        medic.moveTo(1, 4);
        InvalidMoveException e = assertThrows(InvalidMoveException.class, () -> medic.callBackup(board));
        assertTrue(e.getMessage().contains("critical"), e.getMessage());
    }

    @Test
    void theEngineerFreesATrappedSurvivor() throws InvalidMoveException {
        assertThrows(InvalidMoveException.class, () -> trapped.reachedBy(medic), "trapped: can't move yet");
        board.getTeam().switchTo(1);
        Rescuer engineer = board.getActiveRescuer();
        engineer.moveTo(1, 6);
        engineer.setFacing(Direction.RIGHT);
        assertTrue(engineer.specialAction(board).contains("Debris"));
        assertFalse(trapped.isTrapped());
        trapped.reachedBy(engineer);
        assertSame(trapped, engineer.getCarriedSurvivor());
    }

    @Test
    void theNextRoleKeepsLeadingTheFollower() throws InvalidMoveException {
        minor.reachedBy(medic);
        board.getTeam().switchTo(2);
        assertSame(minor, board.getActiveRescuer().getFollower());
        assertFalse(medic.isLeading());
    }

    private static boolean hasEvent(TurnManager turns, GameEvent.Type type) {
        for (GameEvent event : turns.takeEvents()) {
            if (event.getType() == type) {
                return true;
            }
        }
        return false;
    }
}
