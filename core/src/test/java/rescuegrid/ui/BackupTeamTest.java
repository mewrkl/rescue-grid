package rescuegrid.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import rescuegrid.logic.InvalidMoveException;
import rescuegrid.logic.LevelLoader;
import rescuegrid.model.Direction;
import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;
import rescuegrid.model.survivors.CriticalSurvivor;

/**
 * The audit fixes (decisions 0019, 0020): E and Q know when they can act, and
 * the backup team finds its way in, arrives as the countdown ends, and leaves.
 */
class BackupTeamTest {

    private final LevelLoader loader = new LevelLoader();

    /** Exit top-left; a wall with one gap; the red survivor behind it, next to a desk. */
    private GameBoard board() {
        return loader.parse(new String[] {
            "#########",
            "#XP.#...#",
            "#...#.!k#",
            "#.......#",
            "#########",
        });
    }

    @Test
    void theTeamWalksAroundWallsAndFurnitureToTheSurvivor() {
        GameBoard board = board();
        List<int[]> path = BackupTeam.findPath(board, 1, 1, 2, 6);
        assertArrayEquals(new int[] {1, 1}, path.get(0), "starts at the EXIT");
        assertArrayEquals(new int[] {2, 6}, path.get(path.size() - 1), "ends at the survivor");
        assertEquals(9, path.size(), "down round the end of the wall: 8 steps");
        for (int[] step : path.subList(1, path.size() - 1)) {
            assertTrue(board.getTile(step[0], step[1]).isWalkable(), "never through a wall or the desk");
        }
    }

    @Test
    void qAndEAreOnlyReadyWhenTheyCanDoSomething() throws InvalidMoveException {
        GameBoard board = board();
        Rescuer medic = board.getRescuers().get(0);
        Rescuer engineer = board.getRescuers().get(1);
        Survivor red = board.getSurvivorAt(2, 6);
        medic.moveTo(3, 6);
        medic.setFacing(Direction.LEFT);
        assertFalse(medic.hasSpecialTarget(board), "facing empty floor: nothing to treat");
        medic.setFacing(Direction.UP);
        assertTrue(medic.hasSpecialTarget(board), "facing the red survivor");
        assertFalse(medic.canCallBackup(board), "not stabilised yet");
        medic.specialAction(board);
        assertTrue(medic.canCallBackup(board), "stabilised: Q is ready");
        medic.callBackup(board);
        assertFalse(medic.canCallBackup(board), "already on the way");
        assertTrue(red.isBackupComing());

        engineer.moveTo(3, 6);
        engineer.setFacing(Direction.UP);
        assertFalse(engineer.hasSpecialTarget(board), "no rubble and nobody trapped");
    }

    @Test
    void theTeamArrivesAsTheCountdownEndsThenCarriesThemOut() throws InvalidMoveException {
        GameBoard board = board();
        Rescuer medic = board.getRescuers().get(0);
        Survivor red = board.getSurvivorAt(2, 6);
        medic.moveTo(3, 6);
        medic.setFacing(Direction.UP);
        medic.specialAction(board);
        medic.callBackup(board);
        BackupTeam team = new BackupTeam(red, board, 1, 1);

        double step = 0.1;
        for (double t = 0; t < CriticalSurvivor.BACKUP_SECONDS + 0.05; t += step) {
            red.update(step);       // the model's countdown decides the rescue
            team.update(step);
        }
        assertTrue(red.isRescued(), "backup got them at 10 s");
        for (int i = 0; i < 200 && !team.isDone(); i++) {
            team.update(step);       // lift, walk out, "Saved!"
        }
        assertTrue(team.isDone(), "the team leaves by the EXIT and is gone");
    }
}
