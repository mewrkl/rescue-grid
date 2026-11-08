package rescuegrid.logic;

/**
 * The settings for each level: which map file to load, how many survivors must
 * be rescued, how often aftershocks hit, how long survivors last, and the
 * random seed.
 *
 * The seed makes fire and aftershocks happen the same way every time you play a
 * level, so a bug can be reproduced.
 */
public class LevelData {

    public static final int LEVEL_COUNT = 3;

    private final int number;
    private final String fileName;
    private final int target;
    private final int aftershockTicks;
    private final int survivorCondition;
    private final long seed;

    private LevelData(int number, String fileName, int target, int aftershockTicks,
                      int survivorCondition, long seed) {
        this.number = number;
        this.fileName = fileName;
        this.target = target;
        this.aftershockTicks = aftershockTicks;
        this.survivorCondition = survivorCondition;
        this.seed = seed;
    }

    /**
     * The settings for level 1, 2 or 3. All numbers: tune in testing.
     * Bigger levels give survivors more time (20/25/30 ticks = 60/75/90 s)
     * because there is more building to search, but aftershocks come faster.
     */
    public static LevelData forLevel(int number) {
        switch (number) {
            case 1:
                return new LevelData(1, "/levels/level1.txt", 2, 15, 20, 1001L);
            case 2:
                return new LevelData(2, "/levels/level2.txt", 3, 12, 25, 2002L);
            case 3:
                return new LevelData(3, "/levels/level3.txt", 4, 9, 30, 3003L);
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

    public long getSeed() {
        return seed;
    }
}
