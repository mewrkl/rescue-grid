package rescuegrid.model.rescuers;

import com.badlogic.gdx.graphics.Color;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import rescuegrid.assets.AssetManager;
import rescuegrid.graphics.Painter;
import rescuegrid.logic.InvalidMoveException;
import rescuegrid.logic.MovementCalculator;
import rescuegrid.model.Direction;
import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;

/**
 * The dog follows survivors' scent through corridors and rubble (not walls)
 * and reveals them, even in the fog.
 */
public class DogHandler extends Rescuer {

    public static final int SCENT_RANGE = 8; // walking steps
    /** A sniff that finds nobody new only takes this long to recharge, not the full 8 s. */
    public static final double MISS_COOLDOWN_SECONDS = 2.0;

    private static final Color COLOR = Painter.rgb(37, 99, 235);
    private static final Color LEASH = Painter.rgb(217, 68, 43);

    private final MovementCalculator movement;

    public DogHandler(int row, int col) {
        super(row, col);
        this.movement = new MovementCalculator();
    }

    @Override
    public String specialAction(GameBoard board) throws InvalidMoveException {
        if (!canUseSpecial()) {
            throw new InvalidMoveException("Scent is recharging");
        }
        List<Survivor> found = movement.findByScent(board, this, SCENT_RANGE);
        int newlyFound = 0;
        for (Survivor survivor : found) {
            if (!survivor.isRevealed()) {
                newlyFound++;
            }
            survivor.reveal();
        }
        if (newlyFound == 0) {
            startShortCooldown(MISS_COOLDOWN_SECONDS); // a miss is cheap: try again further in
            return found.isEmpty() ? "No scent within 8 steps. Search further in"
                    : "Nobody new within 8 steps";
        }
        startCooldown();
        return "The dog found " + newlyFound + (newlyFound == 1 ? " survivor!" : " survivors!");
    }

    /** The scent's 8 steps, shown as faint paw prints on the map. */
    @Override
    public boolean[][] getAbilityReach(GameBoard board) {
        return movement.scentReach(board, this, SCENT_RANGE);
    }

    @Override
    public String getSpecialName() {
        return "Scent";
    }

    @Override
    public String getSpecialSound() {
        return "bark";
    }

    @Override
    public double getCooldownSeconds() {
        return 8.0;
    }

    @Override
    public String getName() {
        return "Dog Handler";
    }

    @Override
    public String getImageKey() {
        return "dog_handler";
    }

    /**
     * The handler and their dog. The dog walks at the handler's side, half a tile
     * over; it is drawn first when it is behind them (facing up, left or right).
     */
    @Override
    public void drawOnMap(Painter painter, float x, float y, float size, AssetManager assets) {
        Direction facing = getFacing();
        TextureRegion dog = assets.getFrame("dog", facing.ordinal(), getWalkFrame() == 0 ? 0 : getWalkFrame() - 1,
                AssetManager.TILE_PIXELS, AssetManager.TILE_PIXELS);
        if (dog == null) {
            super.drawOnMap(painter, x, y, size, assets);
            return;
        }
        float dogSize = size * 0.8f;
        float dogX = facing == Direction.RIGHT ? x - size * 0.45f : x + size * 0.45f;
        float dogY = y + size - dogSize + (facing == Direction.UP ? -size * 0.1f : size * 0.05f);
        boolean dogInFront = facing == Direction.DOWN;
        if (!dogInFront) {
            painter.image(dog, dogX, dogY, dogSize, dogSize);
            drawLeash(painter, x, y, size, dogX, dogY, dogSize, facing);
        }
        super.drawOnMap(painter, x, y, size, assets);
        if (dogInFront) {
            painter.image(dog, dogX, dogY, dogSize, dogSize);
            drawLeash(painter, x, y, size, dogX, dogY, dogSize, facing);
        }
    }

    /**
     * A red leash from the handler's hand to the dog's collar, sagging a little.
     * Positions are in sprite pixels (16 per tile) on the 16 x 24 handler and the 16 x 16 dog.
     */
    private void drawLeash(Painter painter, float x, float y, float size, float dogX, float dogY,
                           float dogSize, Direction facing) {
        float pixel = size / AssetManager.TILE_PIXELS;
        float handX;
        float collarX;
        float collarY = 6;
        switch (facing) {
            case RIGHT:
                handX = 11.5f;
                collarX = 11;
                break;
            case LEFT:
                handX = 4.5f;
                collarX = 5;
                break;
            case UP:
                handX = 11.5f;
                collarX = 8;
                collarY = 4;
                break;
            default:
                handX = 3.5f;
                collarX = 8;
                break;
        }
        float top = y + size - size * AssetManager.CHARACTER_PIXELS / AssetManager.TILE_PIXELS;
        float x1 = x + handX * pixel;
        float y1 = top + 16.5f * pixel;
        float x2 = dogX + collarX * dogSize / AssetManager.TILE_PIXELS;
        float y2 = dogY + collarY * dogSize / AssetManager.TILE_PIXELS;
        float midX = (x1 + x2) / 2;
        float midY = Math.max(y1, y2) + pixel * 1.5f;  // the leash hangs slack
        painter.line(LEASH, x1, y1, midX, midY, pixel * 0.75f);
        painter.line(LEASH, midX, midY, x2, y2, pixel * 0.75f);
    }

    @Override
    public Color getFallbackColor() {
        return COLOR;
    }

    /** A white paw print: one big pad and three toes. */
    @Override
    protected void drawRoleIcon(Painter painter, float x, float y, float size) {
        float cx = x + size / 2;
        float cy = y + size / 2;
        float pad = size / 4;
        float toe = size / 9;
        painter.fillOval(Color.WHITE, cx - pad / 2, cy - pad / 4, pad, pad * 4 / 5);
        painter.fillOval(Color.WHITE, cx - pad / 2 - toe / 2, cy - pad / 2 - toe / 2, toe, toe);
        painter.fillOval(Color.WHITE, cx - toe / 2, cy - pad * 3 / 4 - toe / 2, toe, toe);
        painter.fillOval(Color.WHITE, cx + pad / 2 - toe / 2, cy - pad / 2 - toe / 2, toe, toe);
    }
}
