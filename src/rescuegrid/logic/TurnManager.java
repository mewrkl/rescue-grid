package rescuegrid.logic;

import rescuegrid.model.GameBoard;

/**
 * Keeps track of time and runs the "world tick": every TICK_SECONDS the
 * building gets worse (fire, survivor condition, aftershocks), always in the
 * same fixed order. The world tick replaced the old "End Turn" button.
 */
public class TurnManager {

    public static final double TICK_SECONDS = 3.0; // tune in testing

    private final GameBoard board;
    private final HazardManager hazards;
    private final LevelData level;

    private double elapsedSeconds;
    private double secondsSinceTick;
    private int ticksUntilAftershock;

    public TurnManager(GameBoard board, HazardManager hazards, LevelData level) {
        this.board = board;
        this.hazards = hazards;
        this.level = level;
        this.elapsedSeconds = 0;
        this.secondsSinceTick = 0;
        this.ticksUntilAftershock = level.getAftershockTicks();
    }

    /** Called every frame by GamePanel with the seconds since the last frame. */
    public void update(double seconds) {
        // TODO (Week 3, Member 3):
        //  1. Add `seconds` to elapsedSeconds and secondsSinceTick.
        //  2. While secondsSinceTick >= TICK_SECONDS: subtract TICK_SECONDS and call runWorldTick().
    }

    /** Everything that happens to the building once per tick, in EXACTLY this order (plan 4.2). */
    private void runWorldTick() {
        // TODO (Week 3, Member 3):
        //  1. hazards.applyFireDamage(board);
        //  2. hazards.spreadFire(board);
        //  3. hazards.burnOutFires(board);
        //  4. call tickCondition() on every survivor in board.getSurvivors();
        //  5. ticksUntilAftershock--; if it reaches 0: hazards.triggerAftershock(board)
        //     and reset it to level.getAftershockTicks();
        //  6. (the end check happens in isMissionOver(), which GamePanel asks every frame)
    }

    /** True when no survivors are left in the building, or every rescuer is downed. */
    public boolean isMissionOver() {
        // TODO (Week 3, Member 3): see plan section 4.6.
        return false;
    }

    /** True when at least the level's target number of survivors were rescued. */
    public boolean isSuccess() {
        // TODO (Week 3, Member 3): count rescued survivors and compare with level.getTarget().
        return false;
    }

    public double getElapsedSeconds() {
        return elapsedSeconds;
    }

    /** Seconds until the next aftershock, for the HUD countdown. */
    public double getSecondsUntilAftershock() {
        return ticksUntilAftershock * TICK_SECONDS - secondsSinceTick;
    }
}
