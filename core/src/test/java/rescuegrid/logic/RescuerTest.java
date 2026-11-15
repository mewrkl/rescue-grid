package rescuegrid.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rescuegrid.model.Direction;
import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Medic;
import rescuegrid.model.rescuers.Rescuer;

/**
 * The three specials, carrying and delivering, on the real Level 1 map.
 * The storeroom survivor at (2,3) is behind the rubble door at (6,4).
 */
class RescuerTest {

    private GameBoard board;
    private Rescuer medic;
    private Rescuer engineer;
    private Rescuer dogHandler;

    @BeforeEach
    void loadLevelOne() {
        board = new LevelLoader().load(LevelData.forLevel(1));
        medic = board.getRescuers().get(0);
        engineer = board.getRescuers().get(1);
        dogHandler = board.getRescuers().get(2);
    }

    @Test
    void rescuersAreAddedInKeyOrder() {
        assertEquals("Medic", medic.getName());
        assertEquals("Engineer", engineer.getName());
        assertEquals("Dog Handler", dogHandler.getName());
    }

    @Test
    void scentFindsTheStoreroomSurvivorThroughRubble() throws InvalidMoveException {
        dogHandler.moveTo(7, 4);
        String message = dogHandler.specialAction(board);
        assertTrue(message.contains("found"), message);
        assertTrue(board.getSurvivorAt(2, 3).isRevealed());
    }

    @Test
    void specialsHaveACooldown() throws InvalidMoveException {
        dogHandler.specialAction(board);
        InvalidMoveException e = assertThrows(InvalidMoveException.class, () -> dogHandler.specialAction(board));
        assertTrue(e.getMessage().contains("recharging"));
        dogHandler.tick(dogHandler.getCooldownSeconds());
        dogHandler.specialAction(board); // ready again
    }

    @Test
    void engineerClearsOnlyRubble() throws InvalidMoveException {
        engineer.moveTo(7, 4);
        engineer.setFacing(Direction.LEFT); // floor
        assertThrows(InvalidMoveException.class, () -> engineer.specialAction(board));

        engineer.setFacing(Direction.UP);   // the rubble door at (6,4)
        assertFalse(board.getTile(6, 4).isWalkable());
        assertEquals("Rubble cleared", engineer.specialAction(board));
        assertTrue(board.getTile(6, 4).isWalkable());
    }

    @Test
    void medicNeedsSomeoneToTreat() {
        assertThrows(InvalidMoveException.class, () -> medic.specialAction(board));
    }

    @Test
    void medicHealsTheFacedSurvivorUpToTheMaximum() throws InvalidMoveException {
        Survivor survivor = board.getSurvivorAt(2, 3); // the storeroom survivor
        for (int i = 0; i < 10; i++) {
            survivor.tickCondition();
        }
        int before = survivor.getCondition();
        medic.moveTo(2, 4);
        medic.setFacing(Direction.LEFT);
        medic.specialAction(board);
        assertEquals(Math.min(survivor.getMaxCondition(), before + Medic.HEAL_AMOUNT), survivor.getCondition());
    }

    @Test
    void carryDeliverAndScore() {
        Survivor survivor = board.getSurvivorAt(2, 3);
        medic.moveTo(2, 3);
        medic.pickUp(survivor);
        assertTrue(medic.isCarrying());
        assertNull(board.getSurvivorAt(2, 3), "a carried survivor is no longer lying on the floor");
        assertEquals(Rescuer.CARRY_STEP_DELAY, medic.getStepDelay(), "carrying is slower");

        medic.moveTo(1, 28); // the exit
        assertEquals(1, survivor.getRow());
        assertEquals(28, survivor.getCol(), "the survivor moves with the rescuer");

        medic.deliverSurvivor();
        assertTrue(survivor.isRescued());
        assertEquals(1, board.getRescuedCount());

        // 100 per rescue + 20 per health point left (3 x 5) + no all-rescued bonus + (120 - 30) time bonus
        assertEquals(100 + 15 * 20 + 0 + 90, new ScoreKeeper().calculateScore(board, 30));
    }

    @Test
    void healthNeverGoesBelowZero() {
        medic.takeDamage(Rescuer.MAX_HEALTH + 3);
        assertEquals(0, medic.getHealth());
        assertTrue(medic.isDowned());
    }
}
