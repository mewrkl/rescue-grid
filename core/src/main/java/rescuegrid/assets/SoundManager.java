package rescuegrid.assets;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Plays sound effects (libGDX Sound) and loops music (libGDX Music).
 * A missing file is skipped, so the game stays silent instead of crashing.
 */
public class SoundManager implements Disposable {

    private static final String[] SOUND_KEYS = {
        "click", "switch", "step", "denied", "bark", "clear_rubble", "heal", "pickup", "rescued",
        "fire_spread", "aftershock_warning", "aftershock", "hurt", "downed", "survivor_lost",
        "success", "failure"
    };

    private final HashMap<String, Sound> sounds;
    private Music music;
    private String musicKey;

    public SoundManager() {
        this.sounds = new HashMap<>();
        this.music = null;
        this.musicKey = null;
    }

    /** Loads every sound effect once at startup. */
    public void loadAll() {
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
        sound.play();
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
            music.play();
        } catch (GdxRuntimeException e) {
            Gdx.app.error("SoundManager", "Could not load music " + file.path() + ": " + e.getMessage());
            music = null;
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
