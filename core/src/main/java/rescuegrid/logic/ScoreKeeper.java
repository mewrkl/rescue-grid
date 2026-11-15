package rescuegrid.logic;

import rescuegrid.model.GameBoard;
import rescuegrid.model.rescuers.Rescuer;

/**
 * Works out the score at the end of a level. Each part has
 * its own method so the result screen can show the breakdown.
 */
public class ScoreKeeper {

    public static final int POINTS_PER_RESCUE = 100;
    public static final int POINTS_PER_HEALTH = 20;
    public static final int ALL_RESCUED_BONUS = 50;

    /** No time bonus (removed 2026-11-15): saving everyone should never lose to rushing. */
    public int calculateScore(GameBoard board) {
        return rescuePoints(board) + healthPoints(board) + allRescuedBonus(board);
    }

    /** 100 per survivor rescued. */
    public int rescuePoints(GameBoard board) {
        return board.getRescuedCount() * POINTS_PER_RESCUE;
    }

    /** 20 per health point left on the roles who are still standing. */
    public int healthPoints(GameBoard board) {
        int points = 0;
        for (Rescuer rescuer : board.getRescuers()) {
            if (!rescuer.isDowned()) {
                points += rescuer.getHealth() * POINTS_PER_HEALTH;
            }
        }
        return points;
    }

    /** 50 extra if nobody was left behind. */
    public int allRescuedBonus(GameBoard board) {
        return board.getRescuedCount() == board.getSurvivors().size() ? ALL_RESCUED_BONUS : 0;
    }
}
