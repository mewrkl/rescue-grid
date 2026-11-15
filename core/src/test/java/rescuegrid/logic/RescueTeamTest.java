package rescuegrid.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rescuegrid.model.Direction;
import rescuegrid.model.GameBoard;
import rescuegrid.model.RescueTeam;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;

/** One rescuer on the map who changes role (decision 0007). */
class RescueTeamTest {

    private GameBoard board;
    private RescueTeam team;

    @BeforeEach
    void setUp() {
        board = new LevelLoader().parse(new String[] {
            "#########",
            "#P...S.X#",
            "#.......#",
            "#########",
        });
        team = board.getTeam();
    }

    @Test
    void theMedicStartsOnTheMapAndOnlyTheActiveRoleIsFound() {
        assertEquals("Medic", board.getActiveRescuer().getName());
        assertSame(board.getActiveRescuer(), board.getRescuerAt(1, 1));
        assertEquals(3, board.getRescuers().size(), "all three roles are still part of the team");
    }

    @Test
    void theNewRoleTakesOverPlaceFacingAndCarriedSurvivor() throws InvalidMoveException {
        Rescuer medic = board.getActiveRescuer();
        Survivor survivor = board.getSurvivorAt(1, 5);
        medic.moveTo(1, 5);
        medic.setFacing(Direction.RIGHT);
        medic.pickUp(survivor);

        team.switchTo(1);
        Rescuer engineer = board.getActiveRescuer();
        assertEquals("Engineer", engineer.getName());
        assertEquals(1, engineer.getRow());
        assertEquals(5, engineer.getCol());
        assertEquals(Direction.RIGHT, engineer.getFacing());
        assertSame(survivor, engineer.getCarriedSurvivor(), "the survivor is handed over");
        assertFalse(medic.isCarrying());
    }

    @Test
    void changingGearTakesASecond() throws InvalidMoveException {
        team.switchTo(2);
        assertTrue(team.isChangingGear());
        team.tick(RescueTeam.GEAR_CHANGE_SECONDS / 2);
        assertTrue(team.isChangingGear());
        team.tick(RescueTeam.GEAR_CHANGE_SECONDS / 2);
        assertFalse(team.isChangingGear());
    }

    @Test
    void youCantSwitchToYourselfOrToADownedRole() {
        assertThrows(InvalidMoveException.class, () -> team.switchTo(0));
        board.getRescuers().get(1).takeDamage(Rescuer.MAX_HEALTH);
        InvalidMoveException e = assertThrows(InvalidMoveException.class, () -> team.switchTo(1));
        assertTrue(e.getMessage().contains("down"), e.getMessage());
    }

    @Test
    void whenTheRoleOnTheMapIsDownedTheNextOneTakesOverAndKeepsTheSurvivor() {
        Rescuer medic = board.getActiveRescuer();
        Survivor survivor = board.getSurvivorAt(1, 5);
        medic.moveTo(1, 5);
        medic.pickUp(survivor);
        medic.takeDamage(Rescuer.MAX_HEALTH);

        assertTrue(team.replaceDowned());
        assertEquals("Engineer", board.getActiveRescuer().getName());
        assertSame(survivor, board.getActiveRescuer().getCarriedSurvivor());
        assertFalse(team.isWipedOut());
    }

    @Test
    void theMissionFailsOnlyWhenAllThreeAreDown() {
        LevelData level = LevelData.forLevel(1);
        GameBoard real = new LevelLoader().load(level);
        TurnManager turns = new TurnManager(real, new HazardManager(level.getSeed()), level);
        for (int i = 0; i < 2; i++) {
            real.getActiveRescuer().takeDamage(Rescuer.MAX_HEALTH);
            turns.update(0.01);
            assertFalse(turns.isMissionOver(), "a role is still standing");
        }
        real.getActiveRescuer().takeDamage(Rescuer.MAX_HEALTH);
        turns.update(0.01);
        assertTrue(turns.isMissionOver());
        assertFalse(turns.isSuccess());
    }

    @Test
    void cooldownsKeepRechargingOffTheMap() throws InvalidMoveException {
        Rescuer dogHandler = board.getRescuers().get(2);
        team.switchTo(2);
        team.tick(RescueTeam.GEAR_CHANGE_SECONDS);
        dogHandler.specialAction(board);
        assertFalse(dogHandler.canUseSpecial());
        team.switchTo(0);
        team.tick(dogHandler.getCooldownSeconds());
        assertTrue(dogHandler.canUseSpecial());
    }
}
