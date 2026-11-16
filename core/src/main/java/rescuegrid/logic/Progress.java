package rescuegrid.logic;

import java.util.Arrays;

/**
 * Which levels the player may pick, their best stars, and whether the player has
 * been through (or chose to skip) the training. Training and Level 1 are always
 * open; each later level opens when the one before it is completed.
 * SaveFile keeps this between runs.
 */
public class Progress {

    private final int[] bestStars;   // index = level number (0 = training)
    private int highestUnlocked;
    private boolean trainingDecided; // finished the training, or chose to skip it

    public Progress() {
        this.bestStars = new int[LevelData.LEVEL_COUNT + 1];
        this.highestUnlocked = 1;
        this.trainingDecided = false;
    }

    /** True once the player finished or skipped the training: PLAY stops asking about it. */
    public boolean isTrainingDecided() {
        return trainingDecided;
    }

    /** The player finished the training or chose to skip it. */
    public void markTrainingDecided() {
        trainingDecided = true;
    }

    /** True once there is anything to continue: the main menu then shows CONTINUE. */
    public boolean hasProgress() {
        if (trainingDecided || highestUnlocked > 1) {
            return true;
        }
        for (int stars : bestStars) {
            if (stars > 0) {
                return true;
            }
        }
        return false;
    }

    /** Where CONTINUE goes: the furthest level that is open. */
    public int getContinueLevel() {
        return highestUnlocked;
    }

    /** Options: forget everything (stars, open levels, the training choice). */
    public void reset() {
        Arrays.fill(bestStars, 0);
        highestUnlocked = 1;
        trainingDecided = false;
    }

    /**
     * Records a finished mission. Returns true if it beat the best stars so far
     * (the result screen then says "NEW BEST").
     */
    public boolean record(int levelNumber, int stars, boolean success) {
        if (success && levelNumber == LevelData.TUTORIAL) {
            trainingDecided = true;
        }
        if (success && levelNumber < LevelData.LEVEL_COUNT) {
            unlockUpTo(levelNumber + 1);
        }
        if (stars > bestStars[levelNumber]) {
            bestStars[levelNumber] = stars;
            return true;
        }
        return false;
    }

    public boolean isUnlocked(int levelNumber) {
        return levelNumber <= highestUnlocked;
    }

    public int getBestStars(int levelNumber) {
        return bestStars[levelNumber];
    }

    public int getHighestUnlocked() {
        return highestUnlocked;
    }

    /** Used when loading a save: opens every level up to this one (it never closes any). */
    public void unlockUpTo(int levelNumber) {
        highestUnlocked = Math.max(highestUnlocked, Math.min(levelNumber, LevelData.LEVEL_COUNT));
    }

    /** Used when loading a save. */
    public void setBestStars(int levelNumber, int stars) {
        bestStars[levelNumber] = Math.max(0, Math.min(stars, MissionReport.STAR_COUNT));
    }
}
