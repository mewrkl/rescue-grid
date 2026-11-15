import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * Makes every sound effect and both music loops for Rescue Grid, as WAV files.
 *
 * This is a small "chiptune" synthesizer, the same idea as the sfxr/jsfxr tools
 * used for retro games: every sound is built from a few basic waves
 * (square, triangle, saw, noise) whose pitch and volume change over time.
 *
 * It is NOT part of the game. Run it once from the project folder and it writes
 * the files into assets/sounds and assets/music:
 *
 *     java tools/SoundMaker.java
 *
 * Change a number below and run it again to tweak a sound.
 */
public class SoundMaker {

    static final int RATE = 22050;               // samples per second
    static final Random RANDOM = new Random(7);  // fixed seed: same sounds every run

    enum Wave { SQUARE, TRIANGLE, SAW, NOISE }

    public static void main(String[] args) throws IOException {
        File sounds = new File("assets/sounds");
        File music = new File("assets/music");
        sounds.mkdirs();
        music.mkdirs();

        // ----- Interface -----
        save(sounds, "click", tone(Wave.SQUARE, 1400, 1100, 0.025, 1), 0.35);
        save(sounds, "switch", concat(tone(Wave.SQUARE, 660, 660, 0.05, 1), tone(Wave.SQUARE, 990, 990, 0.07, 1)), 0.35);
        save(sounds, "denied", concat(tone(Wave.SQUARE, 160, 140, 0.07, 1), silence(0.03),
                tone(Wave.SQUARE, 140, 120, 0.09, 1)), 0.4);

        // ----- Rescuers -----
        save(sounds, "step", mix(tone(Wave.NOISE, 1800, 600, 0.05, 0.5), tone(Wave.TRIANGLE, 150, 80, 0.05, 1)), 0.18);
        double[] barkPart = mix(tone(Wave.SAW, 620, 260, 0.11, 1), tone(Wave.NOISE, 4000, 2000, 0.11, 0.35));
        save(sounds, "bark", concat(barkPart, silence(0.06), barkPart), 0.6);
        double[] rubble = mix(lowpass(tone(Wave.NOISE, 3000, 400, 0.45, 1), 0.35), tone(Wave.SQUARE, 110, 50, 0.3, 0.4));
        for (int i = 0; i < 5; i++) {
            mixInto(rubble, tone(Wave.NOISE, 6000, 5000, 0.03, 0.6), (int) (RATE * (0.05 + i * 0.07)));
        }
        save(sounds, "clear_rubble", rubble, 0.6);
        save(sounds, "heal", concat(note(Wave.TRIANGLE, 72, 0.07, 1.0), note(Wave.TRIANGLE, 76, 0.07, 1.0),
                note(Wave.TRIANGLE, 79, 0.07, 1.0), note(Wave.TRIANGLE, 84, 0.2, 1.0)), 0.6);
        save(sounds, "pickup", concat(tone(Wave.SQUARE, 440, 880, 0.12, 1), tone(Wave.SQUARE, 880, 880, 0.06, 0.7)), 0.35);
        double[] jingle = concat(note(Wave.SQUARE, 72, 0.08, 1.0), note(Wave.SQUARE, 76, 0.08, 1.0),
                note(Wave.SQUARE, 79, 0.08, 1.0), note(Wave.SQUARE, 84, 0.32, 0.8));
        mixInto(jingle, concat(note(Wave.TRIANGLE, 60, 0.24, 0.5), note(Wave.TRIANGLE, 72, 0.32, 0.8)), 0);
        save(sounds, "rescued", jingle, 0.45);
        save(sounds, "hurt", tone(Wave.SQUARE, 320, 110, 0.18, 1), 0.4);
        save(sounds, "downed", mix(tone(Wave.SQUARE, 420, 50, 0.7, 1), tone(Wave.TRIANGLE, 210, 40, 0.7, 1)), 0.45);

        // ----- The building -----
        double[] crackle = lowpass(tone(Wave.NOISE, 2500, 1500, 0.3, 0.4), 0.5);
        for (int i = 0; i < 6; i++) {
            mixInto(crackle, tone(Wave.NOISE, 8000, 8000, 0.015, 1), RANDOM.nextInt((int) (RATE * 0.27)));
        }
        save(sounds, "fire_spread", crackle, 0.18);
        save(sounds, "aftershock_warning", rumble(1.4, 0.9, false), 0.55);
        save(sounds, "aftershock", rumble(1.8, 1.0, true), 0.9);
        save(sounds, "survivor_lost", concat(note(Wave.TRIANGLE, 67, 0.18, 1.0), note(Wave.TRIANGLE, 64, 0.18, 1.0),
                note(Wave.TRIANGLE, 60, 0.4, 0.8)), 0.55);

        // ----- End of mission -----
        double[] fanfare = concat(note(Wave.SQUARE, 72, 0.1, 1.0), note(Wave.SQUARE, 76, 0.1, 1.0),
                note(Wave.SQUARE, 79, 0.1, 1.0), note(Wave.SQUARE, 84, 0.14, 1.0),
                note(Wave.SQUARE, 79, 0.1, 1.0), note(Wave.SQUARE, 84, 0.5, 0.6));
        mixInto(fanfare, concat(note(Wave.TRIANGLE, 48, 0.4, 0.3), note(Wave.TRIANGLE, 55, 0.24, 0.5),
                note(Wave.TRIANGLE, 60, 0.5, 0.6)), 0);
        save(sounds, "success", fanfare, 0.5);
        save(sounds, "failure", concat(note(Wave.TRIANGLE, 67, 0.22, 0.8), note(Wave.TRIANGLE, 65, 0.22, 0.8),
                note(Wave.TRIANGLE, 63, 0.22, 0.8), note(Wave.TRIANGLE, 60, 0.7, 0.6)), 0.6);

        // ----- Music -----
        save(music, "menu", menuMusic(), 0.5);
        save(music, "gameplay", gameplayMusic(), 0.45);
        // Added 2026-11-15, after the music so the earlier sounds stay exactly the same.
        save(sounds, "radio", concat(tone(Wave.NOISE, 3000, 2000, 0.06, 0.4), tone(Wave.SQUARE, 1200, 1200, 0.05, 1),
                silence(0.03), tone(Wave.SQUARE, 1500, 1500, 0.08, 1)), 0.3);
        save(sounds, "help_call", concat(tone(Wave.TRIANGLE, 700, 900, 0.12, 1), silence(0.05),
                tone(Wave.TRIANGLE, 900, 650, 0.2, 1)), 0.22);
        System.out.println("Done. Files written to assets/sounds and assets/music.");
    }

    // =====================================================================
    // Music
    // =====================================================================

    /** Calm loop: A minor arpeggios over a soft bass, melody on the second pass. About 46 s. */
    static double[] menuMusic() {
        double eighth = 60.0 / 84 / 2;                    // 84 beats per minute
        int[][] chords = {{57, 60, 64}, {53, 57, 60}, {60, 64, 67}, {55, 59, 62}}; // Am F C G
        int[] arpPattern = {0, 1, 2, 3, 2, 1, 3, 2};      // 3 = root an octave up
        int[][] melody = {
            {76, 2}, {74, 1}, {72, 1}, {71, 2}, {69, 2},
            {69, 2}, {72, 2}, {77, 2}, {76, 2},
            {76, 2}, {79, 2}, {76, 1}, {74, 1}, {72, 2},
            {71, 2}, {74, 2}, {79, 3}, {0, 1}
        };
        double[] song = silence(2 * chords.length * 16 * eighth);
        int position = 0;
        for (int pass = 0; pass < 2; pass++) {
            for (int[] chord : chords) {
                for (int bar = 0; bar < 2; bar++) {
                    mixInto(song, note(Wave.TRIANGLE, chord[0] - 12, 8 * eighth, 0.4), position, 0.45);
                    for (int step = 0; step < 8; step++) {
                        int index = arpPattern[step];
                        int pitch = index == 3 ? chord[0] + 12 : chord[index];
                        mixInto(song, note(Wave.TRIANGLE, pitch + 12, eighth, 1.4), position + samples(step * eighth), 0.3);
                    }
                    position += samples(8 * eighth);
                }
            }
        }
        // Melody only on the second pass, so the loop has a quieter and a fuller half.
        int melodyPosition = samples(chords.length * 16 * eighth);
        for (int[] part : melody) {
            double length = part[1] * 2 * eighth;
            if (part[0] > 0) {
                mixInto(song, lowpass(note(Wave.SQUARE, part[0], length, 0.7), 0.4), melodyPosition, 0.14);
            }
            melodyPosition += samples(length);
        }
        return song;
    }

    /** Tense loop: driving bass, fast arpeggios and drums; lead joins halfway. About 29 s. */
    static double[] gameplayMusic() {
        double sixteenth = 60.0 / 132 / 4;                // 132 beats per minute
        int[][] chords = {{69, 72, 76}, {69, 72, 76}, {65, 69, 72}, {64, 68, 71}}; // Am Am F E
        int[] bassRoots = {45, 45, 41, 40};
        int[] lead = {76, 74, 72, 71, 76, 79, 77, 76};
        int bars = 16;
        double[] song = silence(bars * 16 * sixteenth);
        for (int bar = 0; bar < bars; bar++) {
            int chordIndex = bar % 4;
            int[] chord = chords[chordIndex];
            int barStart = samples(bar * 16 * sixteenth);
            for (int step = 0; step < 16; step++) {
                int at = barStart + samples(step * sixteenth);
                // Arpeggio on every sixteenth: root, third, fifth, third...
                int[] arp = {0, 1, 2, 1};
                mixInto(song, note(Wave.TRIANGLE, chord[arp[step % 4]], sixteenth, 1.6), at, 0.22);
                // Pulsing bass on every eighth, jumping an octave on the off-beats.
                if (step % 2 == 0) {
                    int pitch = bassRoots[chordIndex] + (step % 4 == 2 ? 12 : 0);
                    mixInto(song, lowpass(note(Wave.SQUARE, pitch, 2 * sixteenth, 2.0), 0.3), at, 0.25);
                }
                // Drums: kick on beats 1 and 3, snare on 2 and 4, hi-hat on the off-beats.
                if (step % 8 == 0) {
                    mixInto(song, tone(Wave.TRIANGLE, 150, 40, 0.12, 1), at, 0.9);
                } else if (step % 8 == 4) {
                    mixInto(song, tone(Wave.NOISE, 5000, 3000, 0.1, 1), at, 0.3);
                } else if (step % 4 == 2) {
                    mixInto(song, tone(Wave.NOISE, 9000, 9000, 0.03, 1), at, 0.12);
                }
            }
            // Second half: a long lead note per bar raises the tension.
            if (bar >= 8) {
                mixInto(song, lowpass(note(Wave.SQUARE, lead[bar - 8], 16 * sixteenth, 0.6), 0.35), barStart, 0.12);
            }
        }
        return song;
    }

    // =====================================================================
    // Building blocks
    // =====================================================================

    /** A sound whose pitch slides from startHz to endHz, with a quick attack and a fade-out. */
    static double[] tone(Wave wave, double startHz, double endHz, double seconds, double volume) {
        int count = samples(seconds);
        double[] out = new double[count];
        double phase = 0;
        long lastCycle = -1;
        double held = 0;
        for (int i = 0; i < count; i++) {
            double progress = (double) i / count;
            phase += (startHz + (endHz - startHz) * progress) / RATE;
            double value;
            if (wave == Wave.NOISE) {
                // Noise holds one random value per "cycle": higher Hz = hissier, lower = rumblier.
                if ((long) phase != lastCycle) {
                    lastCycle = (long) phase;
                    held = RANDOM.nextDouble() * 2 - 1;
                }
                value = held;
            } else {
                value = oscillator(wave, phase);
            }
            double envelope = Math.min(1, i / (0.005 * RATE)) * (1 - progress);
            out[i] = volume * envelope * value;
        }
        return out;
    }

    /** A musical note (MIDI number, 69 = A4 = 440 Hz). Higher decay = more "plucky". */
    static double[] note(Wave wave, int midi, double seconds, double decay) {
        double hz = 440 * Math.pow(2, (midi - 69) / 12.0);
        int count = samples(seconds);
        double[] out = new double[count];
        double phase = 0;
        for (int i = 0; i < count; i++) {
            double progress = (double) i / count;
            phase += hz / RATE;
            double envelope = Math.min(1, i / (0.01 * RATE)) * Math.pow(1 - progress, decay);
            out[i] = envelope * oscillator(wave, phase);
        }
        return out;
    }

    /** One cycle of each wave shape, for phase 0..1. Output is between -1 and 1. */
    static double oscillator(Wave wave, double phase) {
        double p = phase - Math.floor(phase);
        switch (wave) {
            case SQUARE:
                return p < 0.5 ? 1 : -1;
            case TRIANGLE:
                return 1 - 4 * Math.abs(p - 0.5);
            case SAW:
                return 2 * p - 1;
            default:
                return RANDOM.nextDouble() * 2 - 1;
        }
    }

    /** A deep earthquake rumble. The big version starts with a crash and fades out. */
    static double[] rumble(double seconds, double volume, boolean crash) {
        int count = samples(seconds);
        double[] out = new double[count];
        double smooth = 0;
        double phase = 0;
        for (int i = 0; i < count; i++) {
            double progress = (double) i / count;
            double time = (double) i / RATE;
            smooth += 0.04 * ((RANDOM.nextDouble() * 2 - 1) - smooth);  // very low-passed noise
            phase += (crash ? 40 - 15 * progress : 45) / RATE;
            double wobble = 0.6 + 0.4 * Math.sin(2 * Math.PI * 6 * time);
            double envelope = crash
                    ? Math.min(1, i / (0.01 * RATE)) * Math.pow(1 - progress, 1.5)
                    : Math.sin(Math.PI * progress);
            out[i] = volume * envelope * (0.55 * oscillator(Wave.TRIANGLE, phase) * wobble + 2.2 * smooth);
        }
        if (crash) {
            mixInto(out, lowpass(tone(Wave.NOISE, 3000, 500, 0.6, 1), 0.3), 0, 0.8);
        }
        return out;
    }

    /** Softens a sound by removing high frequencies (smaller alpha = darker). */
    static double[] lowpass(double[] in, double alpha) {
        double[] out = new double[in.length];
        double y = 0;
        for (int i = 0; i < in.length; i++) {
            y += alpha * (in[i] - y);
            out[i] = y;
        }
        return out;
    }

    static double[] silence(double seconds) {
        return new double[samples(seconds)];
    }

    static double[] concat(double[]... parts) {
        int total = 0;
        for (double[] part : parts) {
            total += part.length;
        }
        double[] out = new double[total];
        int position = 0;
        for (double[] part : parts) {
            System.arraycopy(part, 0, out, position, part.length);
            position += part.length;
        }
        return out;
    }

    /** Plays two sounds at the same time. */
    static double[] mix(double[] a, double[] b) {
        double[] out = new double[Math.max(a.length, b.length)];
        mixInto(out, a, 0);
        mixInto(out, b, 0);
        return out;
    }

    static void mixInto(double[] target, double[] sound, int offset) {
        mixInto(target, sound, offset, 1.0);
    }

    /** Adds a sound into a longer one, starting at sample `offset`. */
    static void mixInto(double[] target, double[] sound, int offset, double volume) {
        for (int i = 0; i < sound.length && offset + i < target.length; i++) {
            target[offset + i] += sound[i] * volume;
        }
    }

    static int samples(double seconds) {
        return (int) Math.round(seconds * RATE);
    }

    /** Scales the sound so its loudest point is `peak`, then writes a 16-bit mono WAV. */
    static void save(File folder, String name, double[] sound, double peak) throws IOException {
        double loudest = 1e-9;
        for (double sample : sound) {
            loudest = Math.max(loudest, Math.abs(sample));
        }
        byte[] bytes = new byte[sound.length * 2];
        for (int i = 0; i < sound.length; i++) {
            int value = (int) Math.round(sound[i] / loudest * peak * 32767);
            value = Math.max(-32768, Math.min(32767, value));
            bytes[2 * i] = (byte) value;            // little-endian: low byte first
            bytes[2 * i + 1] = (byte) (value >> 8);
        }
        AudioFormat format = new AudioFormat(RATE, 16, 1, true, false);
        try (AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(bytes), format, sound.length)) {
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, new File(folder, name + ".wav"));
        }
        System.out.printf("  %-20s %5.2f s%n", name + ".wav", (double) sound.length / RATE);
    }
}
