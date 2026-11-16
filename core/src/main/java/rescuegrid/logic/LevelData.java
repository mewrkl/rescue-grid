package rescuegrid.logic;

/**
 * Settings for one level: map file, survivor count, target, aftershock timing,
 * survivor time and the random seed for hazards.
 */
public class LevelData {

    public static final int LEVEL_COUNT = 3;
    public static final int TUTORIAL = 0; // the training level, played before Level 1

    private final int number;
    private final String fileName;
    private final int survivorCount;
    private final int target;
    private final int aftershockTicks;
    private final int survivorCondition;
    private final String randomSurvivorTypes;
    private final double fireSpreadChance;
    private final long seed;
    private final String whatsNew;

    private LevelData(int number, String fileName, int survivorCount, int target, int aftershockTicks,
                      int survivorCondition, String randomSurvivorTypes, double fireSpreadChance, long seed,
                      String whatsNew) {
        this.number = number;
        this.fileName = fileName;
        this.survivorCount = survivorCount;
        this.target = target;
        this.aftershockTicks = aftershockTicks;
        this.survivorCondition = survivorCondition;
        this.randomSurvivorTypes = randomSurvivorTypes;
        this.fireSpreadChance = fireSpreadChance;
        this.seed = seed;
        this.whatsNew = whatsNew;
    }

    /**
     * Later levels give survivors more time (20/25/30 ticks = 60/75/90 s) because
     * there is more building to search, but aftershocks come faster.
     */
    public static LevelData forLevel(int number) {
        switch (number) {
            case TUTORIAL: // survivors only weaken in the timed part at the end (see Tutorial)
                return new LevelData(0, "/levels/tutorial.txt", 4, 4, 20, 25, "", 0.08, 7L, "");
            case 1:
                return new LevelData(1, "/levels/level1.txt", 4, 3, 15, 20, "ms", 0.15, 1001L,
                        "Your first real call-out: no training card this time. Fire spreads, and nobody "
                        + "tells you where survivors are, so listen for calls for HELP.");
            case 2:
                return new LevelData(2, "/levels/level2.txt", 5, 4, 12, 25, "msm", 0.20, 2002L,
                        "Lots of cracked floor: every aftershock turns it into rubble, so step off it when "
                        + "the ground shakes. The fire spreads faster.");
            case 3:
                return new LevelData(3, "/levels/level3.txt", 6, 4, 9, 30, "msms", 0.25, 3003L,
                        "The biggest building, with 6 survivors and the fastest fire. Send the dog ahead "
                        + "(3, E) to find people quickly.");
            default:
                throw new IllegalArgumentException("There is no level " + number);
        }
    }

    public boolean hasNextLevel() {
        return number < LEVEL_COUNT;
    }

    /** What the HUD calls this level. */
    public String getTitle() {
        return isTutorial() ? "TRAINING" : "LEVEL " + number;
    }

    public boolean isTutorial() {
        return number == TUTORIAL;
    }

    public int getNumber() {
        return number;
    }

    public String getFileName() {
        return fileName;
    }

    /** How many survivors are in the building (fixed ones plus randomly placed ones). */
    public int getSurvivorCount() {
        return survivorCount;
    }

    /** How many survivors must be rescued before the team may leave. */
    public int getTarget() {
        return target;
    }

    public int getAftershockTicks() {
        return aftershockTicks;
    }

    /** How many world ticks a survivor lasts before being lost (without healing). */
    public int getSurvivorCondition() {
        return survivorCondition;
    }

    /** The chance (0 to 1) that a fire spreads to each neighbouring floor tile per world tick. */
    public double getFireSpreadChance() {
        return fireSpreadChance;
    }

    /** One letter per random survivor: 'm' minor, 's' serious, '!' critical. */
    public String getRandomSurvivorTypes() {
        return randomSurvivorTypes;
    }

    public long getSeed() {
        return seed;
    }

    /** One or two sentences for the briefing card: what is new or hard about this level. */
    public String getWhatsNew() {
        return whatsNew;
    }

    /** Seconds between aftershocks. */
    public double getAftershockSeconds() {
        return aftershockTicks * TurnManager.TICK_SECONDS;
    }
}
