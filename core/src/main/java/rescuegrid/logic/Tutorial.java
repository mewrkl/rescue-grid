package rescuegrid.logic;

import java.util.ArrayList;
import java.util.List;

import rescuegrid.model.Direction;
import rescuegrid.model.GameBoard;
import rescuegrid.model.RescueTeam;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;
import rescuegrid.model.tiles.Tile;

/**
 * The training level, one step at a time, in four short chapters. Each step
 * teaches one idea and the game waits until the player has done it. Keys that
 * haven't been taught yet do nothing, and taped-off doors (Barrier tiles) only
 * open when their step comes, so nobody can wander ahead and get lost.
 *
 * There is always one thing to look at: a door that just opened, or the
 * survivor, rubble or exit this step is about (getMarker). Steps 1 to 7 are
 * calm; from step 8 the clock runs (decision 0011). If the last survivor runs
 * out of time, the timed part starts again from its beginning.
 *
 * Right after learning to change role, the player tests each role's E in the
 * first room: practice rubble, a sniff, and a dummy for the Medic (decision 0015).
 * Steps that belong together are one step whose card changes as the player goes,
 * e.g. hear the call, walk into them, lead them out (decision 0023): 11 steps, not 17.
 */
public class Tutorial {

    private static final int WALK = 0;
    private static final int TRY_ENGINEER = 1;
    private static final int TRY_DOG = 2;
    private static final int TRY_MEDIC = 3;
    private static final int GREEN = 4;       // hear the call, they follow you, lead them out
    private static final int YELLOW = 5;      // clear the rubble, lift the debris, carry them out
    private static final int RED = 6;         // the Medic stabilises them, Q calls backup
    private static final int SEARCH = 7;      // the clock starts here
    private static final int DIG = 8;
    private static final int BRING_OUT = 9;   // an aftershock, then carry them out
    private static final int FINISH = 10;
    private static final int STEP_COUNT = 11;

    private static final int DIRECTIONS_TO_TRY = 2;

    // RescueTeam's order (LevelLoader adds the roles like this).
    private static final int MEDIC = 0;
    private static final int ENGINEER = 1;
    private static final int DOG_HANDLER = 2;
    private static final int CHANGING = -1; // putting on the next role's gear

    /** The first step of each chapter, and the chapters' names. */
    private static final int[] CHAPTER_STARTS = {WALK, GREEN, YELLOW, SEARCH};
    private static final String[] CHAPTER_NAMES = {"MOVING AND ROLES", "RESCUING", "ENGINEER AND MEDIC",
        "SEARCH AGAINST THE CLOCK"};

    private static final int ROOM_2_GATE = 1;
    private static final int CRITICAL_ROOM_GATE = 2;
    private static final int HALL_GATE = 3;
    private static final int DIG_REACH = 3; // the rubble to clear is this close to the hidden survivor

    private int step;
    private int part;                    // where the player is inside a step that has several parts
    private final boolean[] directionsTried;
    private int activeRole;              // MEDIC, ENGINEER, DOG_HANDLER or CHANGING, seen at the last update
    private boolean practisedClear;      // the first room's E tests
    private boolean practisedScent;
    private boolean practisedTreat;
    private boolean clearedRubble;
    private boolean treated;
    private boolean calledBackup;
    private boolean usedScent;
    private Survivor focus;              // the survivor the current step is about
    private List<int[]> openedDoors;     // the doors of the gate that opened last
    private int[] doorToShow;            // the nearest of them, until the player walks through
    private boolean doorPanPending;      // the game should show the door with the camera
    // Where the timed part starts, for a retry.
    private int checkpointRow;
    private int checkpointCol;
    private int[][] hidingSpots;

    public Tutorial() {
        this.step = WALK;
        this.part = 0;
        this.directionsTried = new boolean[Direction.values().length];
        this.activeRole = MEDIC;
        this.clearedRubble = false;
        this.treated = false;
        this.calledBackup = false;
        this.usedScent = false;
        this.openedDoors = new ArrayList<>();
    }

    /** Called once when the training level starts: everything is calm. */
    public void begin(TurnManager turns) {
        turns.setSurvivorsWeaken(false);
        turns.setHazardsRunning(false);
        turns.setHelpCallsOn(false);
    }

    // ----- What the player is allowed to do -----

    public boolean canChangeRole() {
        return step >= TRY_ENGINEER;
    }

    /**
     * Each role's E works on its own test step in the first room, then again from
     * the step that uses it for real. abilityName is getSpecialName(). While
     * searching, the Engineer waits: the dog has to find who is behind the rubble first.
     */
    public boolean canUseAbility(String abilityName) {
        if (abilityName.equals("Clear Rubble")) {
            return step == TRY_ENGINEER || (step >= YELLOW && step != SEARCH);
        }
        if (abilityName.equals("Treat")) {
            return step == TRY_MEDIC || step >= RED;
        }
        if (abilityName.equals("Scent")) {
            return step == TRY_DOG || step >= SEARCH;
        }
        return false;
    }

    /** Q works once the red survivor has been stabilised. */
    public boolean canCallBackup() {
        return step > RED || (step == RED && treated);
    }

    // ----- What the player did (only counts on the step that asks for it) -----

    /** A direction key was pressed (even if a wall stopped the step). */
    public void onDirection(Direction direction) {
        if (step == WALK) {
            directionsTried[direction.ordinal()] = true;
        }
    }

    /** A special action worked; abilityName is the rescuer's getSpecialName(). */
    public void onAbility(String abilityName) {
        if (step == TRY_ENGINEER && abilityName.equals("Clear Rubble")) {
            practisedClear = true;
        } else if (step == TRY_DOG && abilityName.equals("Scent")) {
            practisedScent = true;
        } else if (step == TRY_MEDIC && abilityName.equals("Treat")) {
            practisedTreat = true;
        } else if (step == YELLOW && abilityName.equals("Clear Rubble")) {
            clearedRubble = true;
        } else if (step == RED && abilityName.equals("Treat")) {
            treated = true;
        } else if (step == SEARCH && abilityName.equals("Scent")) {
            usedScent = true;
        }
    }

    public void onBackupCalled() {
        if (step == RED) {
            calledBackup = true;
        }
    }

    // ----- Moving through the steps -----

    /** Called every frame: moves on once the current step is done, and works out its part. */
    public void update(GameBoard board, TurnManager turns) {
        RescueTeam team = board.getTeam();
        activeRole = team.isChangingGear() ? CHANGING : team.getActiveIndex();
        if (doorToShow != null && isOnAnOpenedDoor(board.getActiveRescuer())) {
            doorToShow = null; // they've gone through: point at the next thing
        }
        while (step < FINISH && isDone(step, board, turns)) {
            step++;
            enter(step, board, turns);
        }
        follow(board, turns);
        part = partOf(board, turns);
    }

    /** Inside a step: who it's about, once they can be named. */
    private void follow(GameBoard board, TurnManager turns) {
        if (step == GREEN && focus == null && countRevealed(board) > 0) {
            focus = firstSurvivor(board, true);
            turns.setHelpCallsOn(false); // found them: the calls for help stop
        }
        if (step == YELLOW && focus == null) {
            focus = firstTrapped(board);
        }
    }

    /** Which part of a several-part step the player is on (0 = the first). */
    private int partOf(GameBoard board, TurnManager turns) {
        switch (step) {
            case GREEN:
                if (focus == null) {
                    return 0;                                         // follow the call
                }
                return board.getActiveRescuer().isLeading() ? 2 : 1;  // walk into them, then lead them out
            case YELLOW:
                if (!clearedRubble) {
                    return 0;                                         // clear the rubble
                }
                return countTrapped(board) > 0 ? 1 : 2;               // lift the debris, then carry them
            case RED:
                return treated ? 1 : 0;                               // treat, then Q
            case BRING_OUT:
                return turns.isAftershockComing() ? 0 : 1;            // step off, then carry them out
            default:
                return 0;
        }
    }

    private boolean isDone(int stepNumber, GameBoard board, TurnManager turns) {
        switch (stepNumber) {
            case WALK:
                return countTrue(directionsTried) >= DIRECTIONS_TO_TRY;
            case TRY_ENGINEER:
                return practisedClear;
            case TRY_DOG:
                return practisedScent;
            case TRY_MEDIC:
                return practisedTreat;
            case GREEN:
                return board.getRescuedCount() >= 1;
            case YELLOW:
                return board.getRescuedCount() >= 2;
            case RED:
                return calledBackup;
            case SEARCH:
                return usedScent && (focus == null || focus.isRevealed() || !focus.isWaiting());
            case DIG:
                return digTarget(board) == null;
            case BRING_OUT:
                return !turns.isAftershockComing() && turns.isTargetReached();
            default:
                return false;
        }
    }

    /** What changes in the building when a step begins. */
    private void enter(int stepNumber, GameBoard board, TurnManager turns) {
        Rescuer rescuer = board.getActiveRescuer();
        switch (stepNumber) {
            case GREEN:
                openGate(board, ROOM_2_GATE);
                turns.setHelpCallsOn(true);
                focus = null;
                break;
            case YELLOW:
                focus = null;
                break;
            case RED:
                openGate(board, CRITICAL_ROOM_GATE);
                focus = firstNeedingBackup(board);
                break;
            case SEARCH:
                openGate(board, HALL_GATE);
                turns.setSurvivorsWeaken(true);
                focus = firstSurvivor(board, false); // the one nobody has seen yet
                rememberCheckpoint(board);
                break;
            case BRING_OUT:
                turns.startAftershockNear(rescuer.getRow(), rescuer.getCol());
                break;
            default:
                break;
        }
    }

    /** Opens a gate and remembers its door nearest the player, to show it and point at it. */
    private void openGate(GameBoard board, int gate) {
        openedDoors = board.openGate(gate);
        Rescuer rescuer = board.getActiveRescuer();
        doorToShow = null;
        int best = Integer.MAX_VALUE;
        for (int[] door : openedDoors) {
            int distance = Math.abs(door[0] - rescuer.getRow()) + Math.abs(door[1] - rescuer.getCol());
            if (distance < best) {
                best = distance;
                doorToShow = door;
            }
        }
        if (doorToShow != null) {
            doorPanPending = true;
            // Light up the doorway so the camera has something to show in the dark.
            for (int row = doorToShow[0] - 1; row <= doorToShow[0] + 1; row++) {
                for (int col = doorToShow[1] - 1; col <= doorToShow[1] + 1; col++) {
                    if (board.inBounds(row, col)) {
                        board.getTile(row, col).markExplored();
                    }
                }
            }
        }
    }

    private boolean isOnAnOpenedDoor(Rescuer rescuer) {
        for (int[] door : openedDoors) {
            if (door[0] == rescuer.getRow() && door[1] == rescuer.getCol()) {
                return true;
            }
        }
        return false;
    }

    /** Where everyone is when the timed part starts. */
    private void rememberCheckpoint(GameBoard board) {
        Rescuer rescuer = board.getActiveRescuer();
        checkpointRow = rescuer.getRow();
        checkpointCol = rescuer.getCol();
        List<Survivor> survivors = board.getSurvivors();
        hidingSpots = new int[survivors.size()][];
        for (int i = 0; i < survivors.size(); i++) {
            Survivor survivor = survivors.get(i);
            hidingSpots[i] = new int[] {survivor.getRow(), survivor.getCol()};
        }
    }

    /**
     * In the timed part, a survivor who ran out of time sends everything back to
     * the start of the timed part. Returns true if that just happened.
     */
    public boolean retryIfLost(GameBoard board) {
        if (step < SEARCH || board.getLostCount() == 0) {
            return false;
        }
        List<Survivor> survivors = board.getSurvivors();
        for (int i = 0; i < survivors.size(); i++) {
            if (survivors.get(i).isLost()) {
                survivors.get(i).resetForRetry(hidingSpots[i][0], hidingSpots[i][1]);
            }
        }
        board.getActiveRescuer().moveTo(checkpointRow, checkpointCol);
        usedScent = false;
        step = SEARCH;
        return true;
    }

    // ----- What to point at -----

    /**
     * Training wants the camera to show a door that just opened. Returns it once
     * ({row, col}), then null until the next door opens.
     */
    public int[] takeDoorToShow() {
        if (!doorPanPending) {
            return null;
        }
        doorPanPending = false;
        return doorToShow;
    }

    /**
     * The one thing to look at right now ({row, col}), or null: a door that just
     * opened comes first, then what this step is about. The game draws a pulsing
     * outline there, or an arrow at the edge of the view if it's off screen.
     */
    public int[] getMarker(GameBoard board) {
        if (doorToShow != null) {
            return doorToShow;
        }
        switch (step) {
            case TRY_ENGINEER: // marked once they are the Engineer: first the key, then the target
                return activeRole == ENGINEER ? nearest(board, true) : null;
            case TRY_MEDIC:
                return activeRole == MEDIC ? nearest(board, false) : null;
            case YELLOW:
                return part == 0 ? firstRubble(board) : focusSpot();
            case GREEN:
            case RED:
                return focusSpot();
            case BRING_OUT:
                return part == 0 ? null : focusSpot();
            case DIG:
                return digTarget(board);
            default:
                return null;
        }
    }

    private int[] focusSpot() {
        return focus != null && focus.isWaiting() ? new int[] {focus.getRow(), focus.getCol()} : null;
    }

    /** The word shown with the marker. */
    public String getMarkerLabel() {
        if (doorToShow != null) {
            return "OPEN DOOR";
        }
        switch (step) {
            case TRY_ENGINEER:
            case DIG:
                return "RUBBLE: E";
            case TRY_MEDIC:
                return "DUMMY: E";
            case YELLOW:
                return part == 0 ? "RUBBLE: E" : part == 1 ? "TRAPPED: E" : "WALK INTO THEM";
            case RED:
                return part == 0 ? "MEDIC: E" : "PRESS Q";
            default:
                return "WALK INTO THEM";
        }
    }

    /** True while a door is being pointed out (calls for help wait until the player is through). */
    public boolean isShowingDoor() {
        return doorToShow != null;
    }

    // ----- What to show on the training card -----

    public String getPrompt() {
        switch (step) {
            case WALK:
                return "Walk with W A S D or the arrow keys. Hold a key to keep walking.";
            case TRY_ENGINEER:
                if (activeRole != ENGINEER) {
                    return "You are one rescuer with three roles: Medic (1), Engineer (2) and Dog Handler (3). "
                            + "Each has its own ability on E. Press 2 to become the Engineer.";
                }
                return "ENGINEER: clears rubble. Test your ability now: walk up to the marked rubble, "
                        + "face it and press E.";
            case TRY_DOG:
                if (activeRole != DOG_HANDLER) {
                    return "Rubble cleared! Now press 3 to become the Dog Handler.";
                }
                return "DOG HANDLER: the dog smells hidden people up to 8 steps away, even through rubble. "
                        + "Test your ability now: press E.";
            case TRY_MEDIC:
                if (activeRole != MEDIC) {
                    return "Nobody is hiding in this room, so the dog found no scent. "
                            + "Press 1 to become the Medic.";
                }
                return "MEDIC: treats the injured, which gives them more time. Test your ability now: "
                        + "face the marked dummy and press E.";
            case GREEN:
                if (part == 0) {
                    return "A door has opened. Someone is calling for HELP: go through the door and walk "
                            + "towards the call.";
                }
                if (part == 1) {
                    return "Green tag = minor injury: they can walk. Walk into them and they follow you.";
                }
                return "Lead them to the EXIT, the green door where you started. Follow the green EXIT marker.";
            case YELLOW:
                if (part == 0) {
                    return "The marked RUBBLE blocks the next room. Press 2 for the Engineer, walk up to it, "
                            + "face it and press E.";
                }
                if (part == 1) {
                    return "The marked survivor is TRAPPED. As the Engineer, face them and press E to lift "
                            + "the debris.";
                }
                return "Yellow tag = serious injury: they can't walk. Walk into them to carry them to the EXIT.";
            case RED:
                if (part == 0) {
                    return "A door has opened. Red tag = critical: too hurt to move. "
                            + "Press 1 for the Medic, face them and press E.";
                }
                return "Stabilised! Now press Q next to them to call backup. A stretcher team carries them out, "
                        + "so you can move on.";
            case SEARCH:
                if (usedScent) {
                    return "Nobody near here. Walk further along the hall and press E again: "
                            + "the scent reaches 8 steps.";
                }
                return "The clock is running: survivors get weaker now. Someone is stuck in one of the rooms "
                        + "below the hall, but you can't see through rubble. Press 3 for the Dog Handler, "
                        + "walk along the hall and press E: the dog smells through rubble.";
            case DIG:
                return "The dog found them! Press 2 for the Engineer and clear the marked rubble (E).";
            case BRING_OUT:
                if (part == 0) {
                    return "AFTERSHOCK coming! The orange tiles will collapse: step off them.";
                }
                return "Walk into them to carry them to the EXIT before their time runs out!";
            default:
                return "Everyone is safe! In real levels the clock runs from the start, fire spreads and "
                        + "aftershocks keep coming. Step onto the EXIT to finish.";
        }
    }

    /** True from the moment survivors start to weaken. */
    public boolean isClockRunning() {
        return step >= SEARCH;
    }

    /** 1-based, for "STEP 3 / 11". */
    public int getStepNumber() {
        return step + 1;
    }

    public int getStepCount() {
        return STEP_COUNT;
    }

    /** 1-based chapter of a 1-based step number. */
    public int getChapterOf(int stepNumber) {
        int chapter = 0;
        for (int i = 0; i < CHAPTER_STARTS.length; i++) {
            if (stepNumber - 1 >= CHAPTER_STARTS[i]) {
                chapter = i;
            }
        }
        return chapter + 1;
    }

    public int getChapterNumber() {
        return getChapterOf(getStepNumber());
    }

    public int getChapterCount() {
        return CHAPTER_STARTS.length;
    }

    public String getChapterName() {
        return CHAPTER_NAMES[getChapterNumber() - 1];
    }

    // ----- Helpers -----

    private static int countTrue(boolean[] values) {
        int count = 0;
        for (boolean value : values) {
            if (value) {
                count++;
            }
        }
        return count;
    }

    private int countRevealed(GameBoard board) {
        int count = 0;
        for (Survivor survivor : board.getSurvivors()) {
            if (survivor.isRevealed()) {
                count++;
            }
        }
        return count;
    }

    private int countTrapped(GameBoard board) {
        int count = 0;
        for (Survivor survivor : board.getSurvivors()) {
            if (survivor.isTrapped()) {
                count++;
            }
        }
        return count;
    }

    /** The first waiting survivor that has (or hasn't) been seen; null if none. */
    private Survivor firstSurvivor(GameBoard board, boolean revealed) {
        for (Survivor survivor : board.getSurvivors()) {
            if (survivor.isWaiting() && survivor.isRevealed() == revealed) {
                return survivor;
            }
        }
        return null;
    }

    private Survivor firstTrapped(GameBoard board) {
        for (Survivor survivor : board.getSurvivors()) {
            if (survivor.isTrapped()) {
                return survivor;
            }
        }
        return null;
    }

    private Survivor firstNeedingBackup(GameBoard board) {
        for (Survivor survivor : board.getSurvivors()) {
            if (survivor.isWaiting() && survivor.needsBackup()) {
                return survivor;
            }
        }
        return null;
    }

    /**
     * The first room's practice target nearest the rescuer: rubble (clearable)
     * or the dummy (treatable). Null if there is none.
     */
    private int[] nearest(GameBoard board, boolean rubble) {
        Rescuer rescuer = board.getActiveRescuer();
        int[] best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                Tile tile = board.getTile(row, col);
                boolean wanted = rubble ? tile.isClearable() : tile.isTreatable();
                int distance = Math.abs(row - rescuer.getRow()) + Math.abs(col - rescuer.getCol());
                if (wanted && distance < bestDistance) {
                    bestDistance = distance;
                    best = new int[] {row, col};
                }
            }
        }
        return best;
    }

    private int[] firstRubble(GameBoard board) {
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                if (board.getTile(row, col).isClearable()) {
                    return new int[] {row, col};
                }
            }
        }
        return null;
    }

    /** The rubble closest to the hidden survivor (within DIG_REACH), or null once it's cleared. */
    private int[] digTarget(GameBoard board) {
        if (focus == null || !focus.isWaiting()) {
            return null;
        }
        int[] best = null;
        int bestDistance = DIG_REACH + 1;
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                int distance = Math.abs(row - focus.getRow()) + Math.abs(col - focus.getCol());
                if (distance < bestDistance && board.getTile(row, col).isClearable()) {
                    bestDistance = distance;
                    best = new int[] {row, col};
                }
            }
        }
        return best;
    }
}
