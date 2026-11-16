package rescuegrid.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

/**
 * Tiles that are drawn on their own (not on top of floor) must have no
 * see-through pixels, or the black behind the map shows through as a dark box.
 * Bug found in play: the taped door's shadows replaced the floor instead of
 * shading it. (Furniture and decorations are see-through on purpose: Prop and
 * DecoratedFloor draw floor under them.)
 */
class SpriteSheetTest {

    private static final String[] STANDALONE = {
        "floor", "cracked_floor", "burnt_floor", "rubble", "fire", "exit", "barrier", "dummy"
    };

    @Test
    void standaloneTilesAreFullySolid() throws IOException {
        for (String key : STANDALONE) {
            try (InputStream in = getClass().getResourceAsStream("/images/" + key + ".png")) {
                assertNotNull(in, key + ".png is missing");
                BufferedImage image = ImageIO.read(in);
                int seeThrough = 0;
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        if ((image.getRGB(x, y) >>> 24) < 255) {
                            seeThrough++;
                        }
                    }
                }
                assertEquals(0, seeThrough, key + ".png has see-through pixels that would show black");
            }
        }
    }
}
