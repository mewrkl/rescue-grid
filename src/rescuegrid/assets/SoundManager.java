package rescuegrid.assets;

import java.util.HashMap;

import javax.sound.sampled.Clip;

/**
 * Plays sound effects and loops music. Like AssetManager, a missing file is
 * simply skipped: the game stays silent instead of crashing.
 *
 * Java Sound plays WAV files, not MP3 or OGG. Convert audio with Audacity.
 */
public class SoundManager {

    private final HashMap<String, Clip> clips;
    private Clip music;

    public SoundManager() {
        this.clips = new HashMap<>();
        this.music = null;
    }

    /** Loads every sound effect once at startup. */
    public void loadAll() {
        // TODO (Week 4, Member 3): for each sound key, open "/sounds/<key>.wav" with
        //  AudioSystem.getAudioInputStream(url), then Clip clip = AudioSystem.getClip();
        //  clip.open(stream); clips.put(key, clip). Skip any file that is missing.
    }

    /** Plays a sound effect from the start. Does nothing if it wasn't loaded. */
    public void play(String key) {
        Clip clip = clips.get(key);
        if (clip == null) {
            return;
        }
        clip.setFramePosition(0);
        clip.start();
    }

    /** Starts looping a music track, replacing whatever was playing. */
    public void loopMusic(String key) {
        // TODO (Week 4, Member 3): stopMusic(), load "/music/<key>.wav" into `music`,
        //  then music.loop(Clip.LOOP_CONTINUOUSLY).
    }

    public void stopMusic() {
        if (music != null) {
            music.stop();
        }
    }
}
