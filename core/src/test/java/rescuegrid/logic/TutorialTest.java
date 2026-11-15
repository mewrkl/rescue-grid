package rescuegrid.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rescuegrid.model.Direction;
import rescuegrid.model.GameBoard;
import rescuegrid.model.RescueTeam;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;

/**
 * Plays the training level from start to finish the way a new player would,
 * checking the prompts move on one step at a time. Walking is done by teleporting
 * (moveTo), since the route itself isn't what is being tested.
 */
class TutorialTest {

    private GameBoard board;
    private TurnManager turns;
    private Tutorial tutorial;

    @Test
    void theTrainingCanBeFinishedStepByStep() throws InvalidMoveException {
        LevelData level = LevelData.forLevel(LevelData.TUTORIAL);
        board = new LevelLoader().load(level);
        turns = new TurnManager(board, new HazardManager(level), level);
        tutorial = new Tutorial();
        assertEquals("TRAINING", level.getTitle());
        expectStep(1);

        Rescuer medic = board.getActiveRescuer();
        for (int i = 0; i < 3; i++) {
            medic.moveTo(medic.getRow() + 1, medic.getCol());
            tutorial.onStep();
        }
        expectStep(2);

        Survivor minor = board.getSurvivorAt(4, 10);
        minor.reveal();
        expectStep(3);
        medic.moveTo(4, 10);
        minor.reachedBy(medic);
        expectStep(4);
        medic.moveTo(1, 1);
        medic.deliverAll();
        expectStep(5);

        switchTo(1);
        Rescuer engineer = board.getActiveRescuer();
        engineer.moveTo(3, 14);
        engineer.setFacing(Direction.RIGHT);
        tutorial.onAbility(engineer.specialAction(board).isEmpty() ? "" : engineer.getSpecialName());
        expectStep(6);

        engineer.tick(10);
        engineer.moveTo(4, 19);
        engineer.setFacing(Direction.RIGHT);
        engineer.specialAction(board); // lifts the debris
        expectStep(7);
        Survivor trapped = board.getSurvivorAt(4, 20);
        engineer.moveTo(4, 20);
        trapped.reachedBy(engineer);
        engineer.moveTo(1, 1);
        engineer.deliverAll();
        expectStep(8);

        switchTo(0);
        Rescuer medicAgain = board.getActiveRescuer();
        medicAgain.moveTo(4, 27);
        medicAgain.setFacing(Direction.RIGHT);
        medicAgain.specialAction(board);
        tutorial.onAbility(medicAgain.getSpecialName());
        expectStep(9);
        medicAgain.callBackup(board);
        tutorial.onBackupCalled();
        expectStep(10);

        switchTo(2);
        Rescuer dogHandler = board.getActiveRescuer();
        dogHandler.moveTo(9, 20);
        String found = dogHandler.specialAction(board);
        assertTrue(found.contains("found"), found);
        tutorial.onAbility(dogHandler.getSpecialName());
        expectStep(11);

        Survivor hidden = board.getSurvivorAt(11, 21);
        dogHandler.moveTo(11, 21);
        hidden.reachedBy(dogHandler);
        dogHandler.moveTo(1, 1);
        dogHandler.deliverAll();
        while (turns.getElapsedSeconds() < 15) { // let backup arrive for the critical survivor
            turns.update(1.0 / 60);
            turns.takeEvents();
        }
        expectStep(12);
        assertTrue(turns.evacuate());
        assertTrue(turns.isSuccess());
    }

    private void switchTo(int role) throws InvalidMoveException {
        board.getTeam().switchTo(role);
        board.getTeam().tick(RescueTeam.GEAR_CHANGE_SECONDS);
    }

    private void expectStep(int number) {
        tutorial.update(board, turns);
        assertEquals(number, tutorial.getStepNumber(), tutorial.getPrompt(turns));
    }
}
