package rescuegrid.assets;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Loads every image ONCE at startup and hands them out by name.
 *
 * Drop a PNG named after a key into assets/images/ (for example
 * assets/images/floor.png) and the game uses it automatically. If a file is
 * missing, getImage returns null and the game draws a stand-in made of shapes.
 *
 * (Not to be confused with libGDX's own AssetManager class: ours is the one
 * from our proposal, and it only handles our sprites.)
 */
public class AssetManager implements Disposable {

    private static final String[] IMAGE_KEYS = {
        "floor", "cracked_floor", "burnt_floor", "wall", "rubble", "fire", "fire2", "exit",
        "medic", "engineer", "dog_handler", "survivor", "carry_icon", "heart"
    };

    private final HashMap<String, Texture> textures;
    private final HashMap<String, TextureRegion> images;

    public AssetManager() {
        this.textures = new HashMap<>();
        this.images = new HashMap<>();
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

    /** The image for this key, or null if it wasn't found. Never crashes. */
    public TextureRegion getImage(String key) {
        return images.get(key);
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
    }
}
