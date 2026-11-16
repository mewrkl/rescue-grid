package rescuegrid.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import rescuegrid.logic.LevelData;
import rescuegrid.logic.Progress;

/**
 * Keeps the player's Progress (open levels and best stars) between runs, in
 * libGDX Preferences: a small settings file in the user's home folder.
 */
public class SaveFile {

    private static final String NAME = "rescuegrid"; // the same file SoundManager keeps "music on" in
    private static final String UNLOCKED_KEY = "highestUnlocked";
    private static final String STARS_KEY = "bestStars"; // + the level number
    private static final String TRAINING_KEY = "trainingDecided";

    public Progress load() {
        Preferences settings = Gdx.app.getPreferences(NAME);
        Progress progress = new Progress();
        progress.unlockUpTo(settings.getInteger(UNLOCKED_KEY, 1));
        if (settings.getBoolean(TRAINING_KEY, false)) {
            progress.markTrainingDecided();
        }
        for (int level = 0; level <= LevelData.LEVEL_COUNT; level++) {
            progress.setBestStars(level, settings.getInteger(STARS_KEY + level, 0));
        }
        return progress;
    }

    public void save(Progress progress) {
        Preferences settings = Gdx.app.getPreferences(NAME);
        settings.putInteger(UNLOCKED_KEY, progress.getHighestUnlocked());
        settings.putBoolean(TRAINING_KEY, progress.isTrainingDecided());
        for (int level = 0; level <= LevelData.LEVEL_COUNT; level++) {
            settings.putInteger(STARS_KEY + level, progress.getBestStars(level));
        }
        settings.flush();
    }
}
