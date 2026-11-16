package rescuegrid.assets;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Plays sound effects (libGDX Sound) and loops music (libGDX Music).
 * A missing file is skipped, so the game stays silent instead of crashing.
 * Music and sound effects each have a volume (0 to 100 %, set in Options) that is
 * saved (libGDX Preferences) for next time.
 */
public class SoundManager implements Disposable {

    private static final String SETTINGS_NAME = "rescuegrid";
    private static final String MUSIC_ON_KEY = "musicOn";          // older saves: music on or off
    private static final String MUSIC_VOLUME_KEY = "musicVolume";
    private static final String SOUND_VOLUME_KEY = "soundVolume";
    private static final int VOLUME_STEP = 10;                      // percent per Left/Right press

    private static final String[] SOUND_KEYS = {
        "click", "switch", "step", "denied", "bark", "clear_rubble", "heal", "pickup", "rescued",
        "fire_spread", "aftershock_warning", "aftershock", "hurt", "downed", "survivor_lost",
        "success", "failure", "radio", "help_call", "time_warning", "star"
    };

    private final HashMap<String, Sound> sounds;
    private Music music;
    private String musicKey;
    private int musicVolume;   // percent
    private int soundVolume;   // percent
    private Preferences settings;

    public SoundManager() {
        this.sounds = new HashMap<>();
        this.music = null;
        this.musicKey = null;
        this.musicVolume = 80;
        this.soundVolume = 100;
    }

    /** Loads every sound effect once at startup, and the volumes saved last time. */
    public void loadAll() {
        settings = Gdx.app.getPreferences(SETTINGS_NAME);
        boolean musicWasOn = settings.getBoolean(MUSIC_ON_KEY, true);
        musicVolume = clampVolume(settings.getInteger(MUSIC_VOLUME_KEY, musicWasOn ? 80 : 0));
        soundVolume = clampVolume(settings.getInteger(SOUND_VOLUME_KEY, 100));
        for (String key : SOUND_KEYS) {
            FileHandle file = Gdx.files.internal("sounds/" + key + ".wav");
            if (!file.exists()) {
                continue;
            }
            try {
                sounds.put(key, Gdx.audio.newSound(file));
            } catch (GdxRuntimeException e) {
                Gdx.app.error("SoundManager", "Could not load sound " + file.path() + ": " + e.getMessage());
            }
        }
    }

    /** Plays a sound effect from the start. Does nothing if it wasn't loaded. */
    public void play(String key) {
        Sound sound = sounds.get(key);
        if (sound == null) {
            return;
        }
        sound.stop(); // the same effect never stacks up on itself (e.g. fast footsteps)
        if (soundVolume > 0) {
            sound.play(soundVolume / 100f);
        }
    }

    /** Starts looping a music track. Does nothing if that track is already playing. */
    public void loopMusic(String key) {
        if (key.equals(musicKey)) {
            return;
        }
        stopMusic();
        musicKey = key;
        FileHandle file = Gdx.files.internal("music/" + key + ".wav");
        if (!file.exists()) {
            return;
        }
        try {
            music = Gdx.audio.newMusic(file);
            music.setLooping(true);
            music.setVolume(musicVolume / 100f);
            music.play();
        } catch (GdxRuntimeException e) {
            Gdx.app.error("SoundManager", "Could not load music " + file.path() + ": " + e.getMessage());
            music = null;
        }
    }

    /**
     * Turns the music up (+1) or down (-1) by 10 % and saves it. Music at 0 %
     * keeps playing silently, so turning it up carries on where it is.
     */
    public void changeMusicVolume(int direction) {
        musicVolume = clampVolume(musicVolume + direction * VOLUME_STEP);
        if (music != null) {
            music.setVolume(musicVolume / 100f);
        }
        save(MUSIC_VOLUME_KEY, musicVolume);
    }

    /** Turns the sound effects up (+1) or down (-1) by 10 % and saves it. */
    public void changeSoundVolume(int direction) {
        soundVolume = clampVolume(soundVolume + direction * VOLUME_STEP);
        save(SOUND_VOLUME_KEY, soundVolume);
    }

    public int getMusicVolume() {
        return musicVolume;
    }

    public int getSoundVolume() {
        return soundVolume;
    }

    private static int clampVolume(int percent) {
        return Math.max(0, Math.min(100, percent));
    }

    private void save(String key, int value) {
        if (settings != null) {
            settings.putInteger(key, value);
            settings.flush();
        }
    }

    public void stopMusic() {
        if (music != null) {
            music.stop();
            music.dispose();
        }
        music = null;
        musicKey = null;
    }

    @Override
    public void dispose() {
        stopMusic();
        for (Sound sound : sounds.values()) {
            sound.dispose();
        }
        sounds.clear();
    }
}
