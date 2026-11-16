package rescuegrid.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import rescuegrid.assets.AssetManager;
import rescuegrid.graphics.Painter;
import rescuegrid.model.Direction;
import rescuegrid.model.Drawable;

/**
 * The three roles on the title screen at 4x, facing us. They take turns to
 * step forward, say their name and step back, so a new player meets the team.
 * Here size is ignored: the lineup is LINEUP_WIDTH x LINEUP_HEIGHT pixels.
 */
class TeamLineup implements Drawable {

    static final float LINEUP_WIDTH = 312;
    static final float LINEUP_HEIGHT = 136;

    private static final String[] KEYS = {"medic", "engineer", "dog_handler"};
    private static final String[] NAMES = {"MEDIC", "ENGINEER", "DOG HANDLER"};
    private static final float SCALE = 4;
    private static final float GAP = 48;
    private static final long TURN_MILLIS = 2400;   // each role's turn
    private static final long STEP_MILLIS = 360;    // stepping out, and back in
    private static final long FRAME_MILLIS = 120;
    private static final float STEP_PIXELS = 16;

    @Override
    public void draw(Painter painter, float x, float y, float size, AssetManager assets) {
        float width = AssetManager.TILE_PIXELS * SCALE;
        float height = AssetManager.CHARACTER_PIXELS * SCALE;
        long now = System.currentTimeMillis();
        int turn = (int) ((now / TURN_MILLIS) % KEYS.length);
        long into = now % TURN_MILLIS;
        for (int i = 0; i < KEYS.length; i++) {
            float offset = 0;
            int frame = 0;
            if (i == turn) {
                if (into < STEP_MILLIS) {                         // stepping out
                    offset = STEP_PIXELS * into / STEP_MILLIS;
                    frame = 1 + (int) ((into / FRAME_MILLIS) % 2);
                } else if (into > TURN_MILLIS - STEP_MILLIS) {    // stepping back
                    offset = STEP_PIXELS * (TURN_MILLIS - into) / STEP_MILLIS;
                    frame = 1 + (int) ((into / FRAME_MILLIS) % 2);
                } else {
                    offset = STEP_PIXELS;
                }
            }
            float left = x + i * (width + GAP);
            float top = y + offset;
            TextureRegion body = assets.getFrame(KEYS[i], Direction.DOWN.ordinal(), frame,
                    AssetManager.TILE_PIXELS, AssetManager.CHARACTER_PIXELS);
            if (body != null) {
                painter.image(body, left, top, width, height);
            }
            if (KEYS[i].equals("dog_handler")) {                  // the dog sits at their side
                TextureRegion dog = assets.getFrame("dog", Direction.DOWN.ordinal(), frame == 0 ? 0 : frame - 1,
                        AssetManager.TILE_PIXELS, AssetManager.TILE_PIXELS);
                if (dog != null) {
                    float dogSize = width * 0.8f;
                    painter.image(dog, left + width * 0.55f, top + height - dogSize, dogSize, dogSize);
                }
            }
            if (i == turn && into >= STEP_MILLIS && into <= TURN_MILLIS - STEP_MILLIS) {
                painter.text(NAMES[i], UiStyle.TEXT_SECONDARY, left + width / 2, y + height + STEP_PIXELS + 4);
            }
        }
    }
}
