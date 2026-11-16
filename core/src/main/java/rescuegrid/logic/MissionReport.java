package rescuegrid.logic;

import rescuegrid.model.GameBoard;
import rescuegrid.model.rescuers.Rescuer;

/**
 * What the result screen says about a finished mission: three star goals and
 * which were met, and, if it failed, why and one tip (decision 0014).
 *
 * The goals are separate, so a player can see what to do better:
 * 1. rescue the level's target, 2. rescue everyone, 3. no role went down.
 * A failed mission earns no stars. Training has no stars.
 */
public class MissionReport {

    public static final int STAR_COUNT = 3;

    private final boolean success;
    private final boolean[] goalsMet;
    private final String[] goalNames;
    private final boolean wipedOut;
    private final int total;

    public MissionReport(boolean success, GameBoard board, LevelData level) {
        this.success = success;
        this.goalNames = goalsFor(level);
        this.total = board.getSurvivors().size();
        this.wipedOut = board.getTeam().isWipedOut();
        boolean nobodyDowned = true;
        for (Rescuer rescuer : board.getRescuers()) {
            if (rescuer.isDowned()) {
                nobodyDowned = false;
            }
        }
        this.goalsMet = new boolean[] {
            board.getRescuedCount() >= level.getTarget(),
            board.getRescuedCount() == total,
            nobodyDowned
        };
    }

    /** The three goals as the player reads them, e.g. "Rescue 3 survivors". Also used by the briefing. */
    public static String[] goalsFor(LevelData level) {
        return new String[] {
            "Rescue " + level.getTarget() + " survivors",
            "Rescue all " + level.getSurvivorCount(),
            "No role goes down"
        };
    }

    public boolean isSuccess() {
        return success;
    }

    /** 0 to 3: one per goal met, and none at all for a failed mission. */
    public int getStars() {
        if (!success) {
            return 0;
        }
        int stars = 0;
        for (boolean met : goalsMet) {
            if (met) {
                stars++;
            }
        }
        return stars;
    }

    public String getGoalName(int index) {
        return goalNames[index];
    }

    public boolean isGoalMet(int index) {
        return success && goalsMet[index];
    }

    /** Why the mission failed, or null if it didn't. */
    public String getFailReason() {
        if (success) {
            return null;
        }
        if (wipedOut) {
            return "All three roles went down.";
        }
        return "Too many survivors were lost: not enough could be rescued.";
    }

    /** One thing to try next time, or null after a perfect (3-star) mission. */
    public String getTip() {
        if (!success && wipedOut) {
            return "Tip: fire and collapses hurt you. Walk around fire, and step off the orange tiles "
                    + "when the ground starts to shake.";
        }
        if (!success) {
            return "Tip: survivors weaken from the start. The Dog Handler (3, E) finds them faster, "
                    + "and the Medic's Treat (1, E) gives someone more time.";
        }
        if (!goalsMet[1]) {
            return "Tip: rescue all " + total + " for the next star. When someone's bar runs low, "
                    + "the Medic's Treat (1, E) gives them more time.";
        }
        if (!goalsMet[2]) {
            return "Tip: keep every role standing for the next star. Go around fire, and step off "
                    + "orange tiles before an aftershock.";
        }
        return null;
    }
}
