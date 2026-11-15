package rescuegrid.logic;

/**
 * Settings for one level: map file, survivor count, target, aftershock timing,
 * survivor time and the random seed for hazards.
 */
public class LevelData {

    public static final int LEVEL_COUNT = 3;

    private final int number;
    private final String fileName;
    private final int survivorCount;
    private final int target;
    private final int aftershockTicks;
    private final int survivorCondition;
    private final String randomSurvivorTypes;
    private final long seed;

    private LevelData(int number, String fileName, int survivorCount, int target, int aftershockTicks,
                      int survivorCondition, String randomSurvivorTypes, long seed) {
        this.number = number;
        this.fileName = fileName;
        this.survivorCount = survivorCount;
        this.target = target;
        this.aftershockTicks = aftershockTicks;
        this.survivorCondition = survivorCondition;
        this.randomSurvivorTypes = randomSurvivorTypes;
        this.seed = seed;
    }

    /**
     * Later levels give survivors more time (20/25/30 ticks = 60/75/90 s) because
     * there is more building to search, but aftershocks come faster.
     */
    public static LevelData forLevel(int number) {
        switch (number) {
            case 1:
                return new LevelData(1, "/levels/level1.txt", 3, 2, 15, 20, "ms", 1001L);
            case 2:
                return new LevelData(2, "/levels/level2.txt", 4, 3, 12, 25, "msm", 2002L);
            case 3:
                return new LevelData(3, "/levels/level3.txt", 5, 4, 9, 30, "msms", 3003L);
            default:
                throw new IllegalArgumentException("There is no level " + number);
        }
    }

    public boolean hasNextLevel() {
        return number < LEVEL_COUNT;
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

    /** One letter per random survivor: 'm' minor, 's' serious, '!' critical. */
    public String getRandomSurvivorTypes() {
        return randomSurvivorTypes;
    }

    public long getSeed() {
        return seed;
    }
}
