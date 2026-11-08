package rescuegrid.logic;

/**
 * The settings for each level: which map file to load, how many survivors must
 * be rescued, how often aftershocks hit, and the random seed.
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
    private final long seed;

    private LevelData(int number, String fileName, int target, int aftershockTicks, long seed) {
        this.number = number;
        this.fileName = fileName;
        this.target = target;
        this.aftershockTicks = aftershockTicks;
        this.seed = seed;
    }

    /** The settings for level 1, 2 or 3. All numbers: tune in testing. */
    public static LevelData forLevel(int number) {
        switch (number) {
            case 1:
                return new LevelData(1, "/levels/level1.txt", 2, 15, 1001L);
            case 2:
                return new LevelData(2, "/levels/level2.txt", 3, 12, 2002L);
            case 3:
                return new LevelData(3, "/levels/level3.txt", 4, 9, 3003L);
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

    public long getSeed() {
        return seed;
    }
}
