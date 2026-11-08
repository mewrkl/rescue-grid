package rescuegrid.logic;

import rescuegrid.model.GameBoard;

/**
 * Works out the score at the end of a level (plan section 4.7).
 */
public class ScoreKeeper {

    public static final int POINTS_PER_RESCUE = 100;
    public static final int POINTS_PER_HEALTH = 20;
    public static final int ALL_RESCUED_BONUS = 50;
    public static final int TIME_BONUS_START = 120; // tune in testing

    public int calculateScore(GameBoard board, double secondsTaken) {
        // TODO (Week 3, Member 3):
        //  + POINTS_PER_RESCUE for each rescued survivor
        //  + POINTS_PER_HEALTH for each health point left on active rescuers
        //  + ALL_RESCUED_BONUS if every survivor was rescued
        //  + Math.max(0, TIME_BONUS_START - (int) secondsTaken)
        return 0;
    }
}
