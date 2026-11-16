package rescuegrid.assets;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Loads the sprite sheets at startup and hands out their frames by name.
 * The sprites are made by tools/SpriteMaker.java; a sheet is a grid of frames
 * (for example a character's rows are the four directions and its columns the
 * steps of the walk). A missing image gives null, and the game draws its shape
 * version instead.
 */
public class AssetManager implements Disposable {

    /** Sprite pixels: tiles are 16 x 16, characters 16 x 24 (drawn 4 times bigger). */
    public static final int TILE_PIXELS = 16;
    public static final int CHARACTER_PIXELS = 24;

    private static final String[] IMAGE_KEYS = {
        "floor", "cracked_floor", "burnt_floor", "wall", "rubble", "fire", "exit", "barrier", "dummy", "props",
        "decor", "medic", "engineer", "dog_handler", "survivor_minor", "survivor_serious", "survivor_critical",
        "dog", "carry_icon", "emote", "paramedic", "stretcher", "flare", "puff"
    };

    private final HashMap<String, Texture> textures;
    private final HashMap<String, TextureRegion> images;
    private final HashMap<String, TextureRegion[][]> frames; // each sheet cut up once, on first use

    public AssetManager() {
        this.textures = new HashMap<>();
        this.images = new HashMap<>();
        this.frames = new HashMap<>();
    }

    /** Loads every image in IMAGE_KEYS that exists. Missing files are skipped. */
    public void loadAll() {
        for (String key : IMAGE_KEYS) {
            FileHandle file = Gdx.files.internal("images/" + key + ".png");
            if (!file.exists()) {
                continue;
            }
            try {
                Texture texture = new Texture(file);
                // Nearest = keep pixel art sharp when it's scaled up 4x (no blurring).
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                TextureRegion region = new TextureRegion(texture);
                // Our cameras draw y-down (row 0 at the top), so images are flipped once here.
                region.flip(false, true);
                textures.put(key, texture);
                images.put(key, region);
            } catch (GdxRuntimeException e) {
                Gdx.app.error("AssetManager", "Could not read image " + file.path() + ": " + e.getMessage());
            }
        }
    }

    /** The whole image for this key, or null if it wasn't found. Never crashes. */
    public TextureRegion getImage(String key) {
        return images.get(key);
    }

    /**
     * One frame of a sprite sheet: the given row and column, where every frame is
     * frameWidth x frameHeight pixels. Null if the sheet is missing or too small,
     * so the caller can fall back to its shape art.
     */
    public TextureRegion getFrame(String key, int row, int col, int frameWidth, int frameHeight) {
        TextureRegion[][] grid = frames.get(key);
        if (grid == null) {
            Texture texture = textures.get(key);
            if (texture == null) {
                return null;
            }
            grid = TextureRegion.split(texture, frameWidth, frameHeight);
            for (TextureRegion[] line : grid) {
                for (TextureRegion region : line) {
                    region.flip(false, true); // our cameras draw y-down
                }
            }
            frames.put(key, grid);
        }
        if (row < 0 || row >= grid.length || col < 0 || col >= grid[row].length) {
            return null;
        }
        return grid[row][col];
    }

    /** How many frames wide a sheet is (0 if it is missing). */
    public int getFrameCount(String key, int frameWidth) {
        Texture texture = textures.get(key);
        return texture == null ? 0 : texture.getWidth() / frameWidth;
    }

    public int getLoadedCount() {
        return images.size();
    }

    /** Textures live in graphics memory, which Java's garbage collector can't free. */
    @Override
    public void dispose() {
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
        images.clear();
        frames.clear();
    }
}
