package rescuegrid.assets;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;

/**
 * Loads every image ONCE at startup and hands them out by name.
 *
 * Drop a PNG named after a key into resources/images/ (for example
 * resources/images/floor.png) and the game uses it automatically. If a file is
 * missing, getImage returns null and the game draws a coloured stand-in instead.
 */
public class AssetManager {

    private static final String[] IMAGE_KEYS = {
        "floor", "cracked_floor", "burnt_floor", "wall", "rubble", "fire", "fire2", "exit",
        "medic", "engineer", "dog_handler", "survivor", "carry_icon", "heart"
    };

    private final HashMap<String, BufferedImage> images;

    public AssetManager() {
        this.images = new HashMap<>();
    }

    /** Loads every image in IMAGE_KEYS that exists. Missing files are skipped. */
    public void loadAll() {
        for (String key : IMAGE_KEYS) {
            BufferedImage image = loadImage("/images/" + key + ".png");
            if (image != null) {
                images.put(key, image);
            }
        }
    }

    /** The image for this key, or null if it wasn't found. Never crashes. */
    public BufferedImage getImage(String key) {
        return images.get(key);
    }

    public int getLoadedCount() {
        return images.size();
    }

    private BufferedImage loadImage(String path) {
        URL url = AssetManager.class.getResource(path);
        if (url == null) {
            return null;
        }
        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            System.err.println("Could not read image " + path + ": " + e.getMessage());
            return null;
        }
    }
}
