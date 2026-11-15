package rescuegrid.logic;

import java.util.ArrayList;
import java.util.List;

import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;

/**
 * Keeps time and runs the world tick: every TICK_SECONDS the fire, survivors
 * and aftershock countdown move forward, in a fixed order.
 */
public class TurnManager {

    public static final double TICK_SECONDS = 3.0;
    public static final double WARNING_SECONDS = 5.0; // aftershock warning time

    private final GameBoard board;
    private final HazardManager hazards;
    private final LevelData level;
    private final ArrayList<GameEvent> events;

    private double elapsedSeconds;
    private double secondsSinceTick;
    private int ticksUntilAftershock;
    private boolean warningGiven;
    private boolean evacuated;

    public TurnManager(GameBoard board, HazardManager hazards, LevelData level) {
        this.board = board;
        this.hazards = hazards;
        this.level = level;
        this.events = new ArrayList<>();
        this.elapsedSeconds = 0;
        this.secondsSinceTick = 0;
        this.ticksUntilAftershock = level.getAftershockTicks();
        this.warningGiven = false;
        this.evacuated = false;
    }

    /** Called every frame by GamePanel with the seconds since the last frame. */
    public void update(double seconds) {
        elapsedSeconds += seconds;
        secondsSinceTick += seconds;
        // "while", not "if": if the game ever lags by more than one tick, we catch up.
        while (secondsSinceTick >= TICK_SECONDS) {
            secondsSinceTick -= TICK_SECONDS;
            runWorldTick();
        }
        if (!warningGiven && getSecondsUntilAftershock() <= WARNING_SECONDS) {
            warningGiven = true;
            hazards.planCollapses(board);
            events.add(new GameEvent(GameEvent.Type.AFTERSHOCK_WARNING, ""));
        }
        // If fire or a collapse downed the rescuer on the map, the next role takes over.
        if (board.getTeam().replaceDowned()) {
            events.add(new GameEvent(GameEvent.Type.ROLE_TAKEOVER,
                    "The " + board.getActiveRescuer().getName() + " takes over"));
        }
    }

    /** Everything that happens to the building once per tick, in this order. */
    private void runWorldTick() {
        hazards.applyFireDamage(board, events);   // 1. damage before spreading
        hazards.spreadFire(board, events);        // 2.
        hazards.burnOutFires(board);              // 3.
        tickSurvivors();                          // 4.
        ticksUntilAftershock--;                   // 5.
        if (ticksUntilAftershock <= 0) {
            hazards.triggerAftershock(board, events);
            ticksUntilAftershock = level.getAftershockTicks();
            warningGiven = false;
        }
        // 6. The end check happens in isMissionOver(), which GamePanel asks every frame.
    }

    private void tickSurvivors() {
        for (Survivor survivor : board.getSurvivors()) {
            boolean wasLost = survivor.isLost();
            survivor.tickCondition();
            if (!wasLost && survivor.isLost()) {
                events.add(new GameEvent(GameEvent.Type.SURVIVOR_LOST, "A survivor didn't make it"));
            }
        }
        // A rescuer can't keep carrying someone who was lost.
        Rescuer rescuer = board.getActiveRescuer();
        if (rescuer.isCarrying() && rescuer.getCarriedSurvivor().isLost()) {
            rescuer.dropSurvivor();
        }
    }

    /**
     * True when the team has left through the exit, no survivors are left in the
     * building, or all three roles are downed.
     */
    public boolean isMissionOver() {
        return evacuated || board.getStillInsideCount() == 0 || board.getTeam().isWipedOut();
    }

    /** True when at least the level's target number of survivors were rescued. */
    public boolean isSuccess() {
        return isTargetReached();
    }

    public boolean isTargetReached() {
        return board.getRescuedCount() >= level.getTarget();
    }

    /** How many more survivors must be rescued before the team may leave (0 once the target is reached). */
    public int getStillNeeded() {
        return Math.max(0, level.getTarget() - board.getRescuedCount());
    }

    /**
     * The team leaves the building, ending the mission. Only allowed once enough
     * survivors are rescued, so it returns false (and does nothing) before that.
     */
    public boolean evacuate() {
        if (!isTargetReached()) {
            return false;
        }
        evacuated = true;
        return true;
    }

    /** True from the warning until the aftershock hits. */
    public boolean isAftershockComing() {
        return warningGiven;
    }

    /** True if the coming aftershock will collapse the tile at (row, col). */
    public boolean willCollapse(int row, int col) {
        return hazards.willCollapse(board, row, col);
    }

    /** Hands over everything that happened since the last call, and forgets it. */
    public List<GameEvent> takeEvents() {
        List<GameEvent> happened = new ArrayList<>(events);
        events.clear();
        return happened;
    }

    public double getElapsedSeconds() {
        return elapsedSeconds;
    }

    /** Seconds until the next aftershock, for the HUD countdown. */
    public double getSecondsUntilAftershock() {
        return ticksUntilAftershock * TICK_SECONDS - secondsSinceTick;
    }
}
