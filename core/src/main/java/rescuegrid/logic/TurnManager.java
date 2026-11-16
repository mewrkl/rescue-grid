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
    public static final double HELP_RANGE = 12;        // tiles: how far a call for help carries
    public static final double HELP_INTERVAL = 6.0;    // seconds between one survivor's calls
    public static final double HELP_CALL_SECONDS = 1.5; // how long each call shows on screen
    private static final double HELP_OFFSET = 1.9;     // so survivors don't all call at once
    /** A found survivor with this little time left gets a warning (beep, message, pulse on the map). */
    public static final double[] FADING_WARNINGS = {30, 10};

    private final GameBoard board;
    private final HazardManager hazards;
    private final LevelData level;
    private final ArrayList<GameEvent> events;

    private double elapsedSeconds;
    private double secondsSinceTick;
    private int ticksUntilAftershock;
    private boolean warningGiven;
    private boolean evacuated;
    private boolean survivorsWeaken;   // false in training until the timed part
    private boolean hazardsRunning;    // fire and the aftershock timer; false all through training
    private boolean helpCallsOn;
    private final int[] lastCall; // which call each survivor made last (for the sound)
    private final int[] warningsGiven; // how many FADING_WARNINGS each survivor has had

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
        this.survivorsWeaken = true;
        this.hazardsRunning = true;
        this.helpCallsOn = true;
        this.lastCall = new int[board.getSurvivors().size()];
        this.warningsGiven = new int[board.getSurvivors().size()];
    }

    /** Called every frame by GamePanel with the seconds since the last frame. */
    public void update(double seconds) {
        elapsedSeconds += seconds;
        secondsSinceTick += seconds;
        updateSurvivors(seconds);
        // "while", not "if": if the game ever lags by more than one tick, we catch up.
        while (secondsSinceTick >= TICK_SECONDS) {
            secondsSinceTick -= TICK_SECONDS;
            runWorldTick();
        }
        if (hazardsRunning && !warningGiven && getSecondsUntilAftershock() <= WARNING_SECONDS) {
            warningGiven = true;
            hazards.planCollapses(board);
            events.add(new GameEvent(GameEvent.Type.AFTERSHOCK_WARNING, ""));
        }
        checkHelpCalls();
        // If fire or a collapse downed the rescuer on the map, the next role takes over.
        if (board.getTeam().replaceDowned()) {
            events.add(new GameEvent(GameEvent.Type.ROLE_TAKEOVER,
                    "The " + board.getActiveRescuer().getName() + " takes over"));
        }
    }

    /** Everything that happens to the building once per tick, in this order. */
    private void runWorldTick() {
        if (hazardsRunning) {
            hazards.applyFireDamage(board, events);   // 1. damage before spreading
            hazards.spreadFire(board, events);        // 2.
            hazards.burnOutFires(board);              // 3.
        }
        if (survivorsWeaken) {
            tickSurvivors();                          // 4.
            warnFading();
        }
        if (hazardsRunning || warningGiven) {         // (training: only a planned aftershock counts down)
            ticksUntilAftershock--;                   // 5.
        }
        if (ticksUntilAftershock <= 0) {
            hazards.triggerAftershock(board, events);
            ticksUntilAftershock = level.getAftershockTicks();
            warningGiven = false;
        }
        // 6. The end check happens in isMissionOver(), which GamePanel asks every frame.
    }

    /** Every frame: lets each survivor count down (backup for critical survivors). */
    private void updateSurvivors(double seconds) {
        for (Survivor survivor : board.getSurvivors()) {
            boolean wasRescued = survivor.isRescued();
            survivor.update(seconds);
            if (!wasRescued && survivor.isRescued()) {
                events.add(new GameEvent(GameEvent.Type.BACKUP_ARRIVED, "Backup has them on the stretcher"));
            }
        }
    }

    private void tickSurvivors() {
        for (Survivor survivor : board.getSurvivors()) {
            boolean wasLost = survivor.isLost();
            survivor.tickCondition();
            if (!wasLost && survivor.isLost()) {
                events.add(new GameEvent(GameEvent.Type.SURVIVOR_LOST, "A survivor didn't make it"));
            }
        }
        // A rescuer can't keep carrying or leading someone who was lost.
        Rescuer rescuer = board.getActiveRescuer();
        if (rescuer.isCarrying() && rescuer.getCarriedSurvivor().isLost()) {
            rescuer.dropSurvivor();
        }
        if (rescuer.isLeading() && rescuer.getFollower().isLost()) {
            rescuer.releaseFollower();
        }
    }

    /**
     * Warns once at 30 s and once at 10 s left, for survivors the player has found.
     * Treating them past a warning resets it, so it can come again later.
     */
    private void warnFading() {
        List<Survivor> survivors = board.getSurvivors();
        for (int i = 0; i < survivors.size(); i++) {
            Survivor survivor = survivors.get(i);
            double secondsLeft = getSecondsLeft(survivor);
            if (survivor.isRescued() || survivor.isLost() || !survivor.isRevealed()) {
                continue;
            }
            if (secondsLeft > FADING_WARNINGS[0]) {
                warningsGiven[i] = 0;
            } else if (warningsGiven[i] < FADING_WARNINGS.length
                    && secondsLeft <= FADING_WARNINGS[warningsGiven[i]]) {
                // Skip a warning that has already passed (e.g. found with 8 s left: only "10 s").
                while (warningsGiven[i] + 1 < FADING_WARNINGS.length
                        && secondsLeft <= FADING_WARNINGS[warningsGiven[i] + 1]) {
                    warningsGiven[i]++;
                }
                events.add(new GameEvent(GameEvent.Type.SURVIVOR_FADING, "A " + survivor.getInjuryName()
                        .toLowerCase() + " survivor has " + (int) FADING_WARNINGS[warningsGiven[i]] + " s left!"));
                warningsGiven[i]++;
            }
        }
    }

    /** About how long until a survivor is lost if nobody treats them. */
    public double getSecondsLeft(Survivor survivor) {
        return survivor.getTicksLeft() * TICK_SECONDS;
    }

    /** True if a found survivor still in the building is down to the last warning's time or less. */
    public boolean isFading(Survivor survivor) {
        return survivorsWeaken && survivor.isRevealed() && !survivor.isRescued() && !survivor.isLost()
                && getSecondsLeft(survivor) <= FADING_WARNINGS[0];
    }

    /** Training: survivors keep their condition until this is turned on. */
    public void setSurvivorsWeaken(boolean on) {
        survivorsWeaken = on;
    }

    /** Training: no fire damage or spreading and no aftershock timer while this is off. */
    public void setHazardsRunning(boolean on) {
        hazardsRunning = on;
    }

    /** Training: calls for help only while the lesson about them is on screen. */
    public void setHelpCallsOn(boolean on) {
        helpCallsOn = on;
    }

    /**
     * Training: warns of an aftershock right now that collapses floor near (row, col),
     * then hits WARNING_SECONDS later. Does nothing if one is already coming.
     */
    public void startAftershockNear(int row, int col) {
        if (warningGiven) {
            return;
        }
        warningGiven = true;
        hazards.planCollapsesNear(board, row, col, 3);
        // Two ticks away, one second into the current one: 2 x 3 - 1 = 5 s (WARNING_SECONDS).
        ticksUntilAftershock = 2;
        secondsSinceTick = TICK_SECONDS * 2 - WARNING_SECONDS;
        events.add(new GameEvent(GameEvent.Type.AFTERSHOCK_WARNING, ""));
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

    /**
     * Survivors nobody has found yet call for help every HELP_INTERVAL seconds, if
     * the rescuer is within HELP_RANGE tiles (through walls: you can hear them).
     * Returns the ones calling right now, so the game can show where they are.
     */
    public List<Survivor> getCallingSurvivors() {
        List<Survivor> calling = new ArrayList<>();
        List<Survivor> survivors = board.getSurvivors();
        for (int i = 0; i < survivors.size(); i++) {
            Survivor survivor = survivors.get(i);
            double phase = (elapsedSeconds + i * HELP_OFFSET) % HELP_INTERVAL;
            if (phase < HELP_CALL_SECONDS && canCall(survivor)) {
                calling.add(survivor);
            }
        }
        return calling;
    }

    private boolean canCall(Survivor survivor) {
        if (!helpCallsOn || !survivor.isWaiting() || survivor.isRevealed()) {
            return false;
        }
        Rescuer rescuer = board.getActiveRescuer();
        double rows = survivor.getRow() - rescuer.getRow();
        double cols = survivor.getCol() - rescuer.getCol();
        return Math.sqrt(rows * rows + cols * cols) <= HELP_RANGE;
    }

    /** Records a HELP_CALL event each time a survivor starts a new call (one sound per call). */
    private void checkHelpCalls() {
        List<Survivor> survivors = board.getSurvivors();
        for (int i = 0; i < survivors.size(); i++) {
            int call = (int) ((elapsedSeconds + i * HELP_OFFSET) / HELP_INTERVAL);
            if (call != lastCall[i]) {
                lastCall[i] = call;
                if (canCall(survivors.get(i))) {
                    events.add(new GameEvent(GameEvent.Type.HELP_CALL, ""));
                }
            }
        }
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

    /** False while aftershocks are switched off (training, until its one planned aftershock). */
    public boolean isAftershockCounting() {
        return hazardsRunning || warningGiven;
    }

    /** Seconds until the next aftershock, for the HUD countdown. */
    public double getSecondsUntilAftershock() {
        if (!hazardsRunning && !warningGiven) {
            return level.getAftershockTicks() * TICK_SECONDS; // stopped (training): show a full bar
        }
        return ticksUntilAftershock * TICK_SECONDS - secondsSinceTick;
    }
}
