package rescuegrid.logic;

import java.util.ArrayList;
import java.util.List;

import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;

/**
 * Keeps track of time and runs the "world tick": every TICK_SECONDS the
 * building gets worse (fire, survivor condition, aftershocks), always in the
 * same fixed order. The world tick replaced the old "End Turn" button.
 */
public class TurnManager {

    public static final double TICK_SECONDS = 3.0;    // tune in testing
    public static final double WARNING_SECONDS = 5.0; // aftershock warning time

    private final GameBoard board;
    private final HazardManager hazards;
    private final LevelData level;
    private final ArrayList<GameEvent> events;

    private double elapsedSeconds;
    private double secondsSinceTick;
    private int ticksUntilAftershock;
    private boolean warningGiven;

    public TurnManager(GameBoard board, HazardManager hazards, LevelData level) {
        this.board = board;
        this.hazards = hazards;
        this.level = level;
        this.events = new ArrayList<>();
        this.elapsedSeconds = 0;
        this.secondsSinceTick = 0;
        this.ticksUntilAftershock = level.getAftershockTicks();
        this.warningGiven = false;
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
            events.add(new GameEvent(GameEvent.Type.AFTERSHOCK_WARNING, "Aftershock in 5 seconds! Get clear"));
        }
    }

    /** Everything that happens to the building once per tick, in EXACTLY this order (plan 4.2). */
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
        for (Rescuer rescuer : board.getRescuers()) {
            if (rescuer.isCarrying() && rescuer.getCarriedSurvivor().isLost()) {
                rescuer.dropSurvivor();
            }
        }
    }

    /** True when no survivors are left in the building, or every rescuer is downed. */
    public boolean isMissionOver() {
        boolean survivorsLeft = false;
        for (Survivor survivor : board.getSurvivors()) {
            if (!survivor.isRescued() && !survivor.isLost()) {
                survivorsLeft = true;
            }
        }
        boolean rescuersLeft = false;
        for (Rescuer rescuer : board.getRescuers()) {
            if (!rescuer.isDowned()) {
                rescuersLeft = true;
            }
        }
        return !survivorsLeft || !rescuersLeft;
    }

    /** True when at least the level's target number of survivors were rescued. */
    public boolean isSuccess() {
        return board.getRescuedCount() >= level.getTarget();
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
