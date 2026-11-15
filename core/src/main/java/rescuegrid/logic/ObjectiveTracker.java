package rescuegrid.logic;

import java.util.ArrayList;
import java.util.List;

import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;

/**
 * Works out the objective checklist shown on the map: found, rescued, leave,
 * plus one "next step" line that tells the player what to do right now.
 * It only reads the game.
 */
public class ObjectiveTracker {

    /** The checklist: find the survivors, rescue enough, then leave. */
    public List<Objective> getObjectives(GameBoard board, TurnManager turns, LevelData level) {
        int total = board.getSurvivors().size();
        int found = 0;
        for (Survivor survivor : board.getSurvivors()) {
            if (survivor.isRevealed() || survivor.isRescued()) {
                found++;
            }
        }
        int rescued = board.getRescuedCount();
        List<Objective> list = new ArrayList<>();
        list.add(new Objective("Find survivors  " + found + "/" + total, found == total));
        list.add(new Objective("Rescue  " + rescued + "/" + level.getTarget(), turns.isTargetReached()));
        list.add(new Objective("Leave by the EXIT", false));
        return list;
    }

    /** One short instruction for what to do right now. */
    public String getNextStep(GameBoard board, TurnManager turns) {
        Rescuer rescuer = board.getActiveRescuer();
        if (rescuer.isCarrying() || rescuer.isLeading()) {
            return "Take them to the EXIT";
        }
        Survivor nearest = nearestNeedingHelp(board, rescuer);
        if (turns.isTargetReached()) {
            return nearest == null ? "Step onto the EXIT to leave"
                    : "Leave by the EXIT, or save more for a bonus";
        }
        if (nearest != null) {
            return nearest.getNeed();
        }
        if (!turns.getCallingSurvivors().isEmpty()) {
            return "Someone is calling for HELP! Follow it";
        }
        return "Search the dark. Dog Handler (3): press E";
    }

    /** The closest revealed survivor who still needs something done, or null. */
    private Survivor nearestNeedingHelp(GameBoard board, Rescuer rescuer) {
        Survivor nearest = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Survivor survivor : board.getSurvivors()) {
            if (!survivor.isWaiting() || !survivor.isRevealed() || survivor.getNeed() == null) {
                continue;
            }
            int distance = Math.abs(survivor.getRow() - rescuer.getRow())
                    + Math.abs(survivor.getCol() - rescuer.getCol());
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = survivor;
            }
        }
        return nearest;
    }
}
