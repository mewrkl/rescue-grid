package rescuegrid.model.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import rescuegrid.assets.AssetManager;
import rescuegrid.graphics.Painter;
import rescuegrid.model.GameBoard;

/**
 * Solid wall. Nothing walks through it, nothing burns it, it never changes.
 * Sight also stops at walls.
 *
 * In the angled view a wall's look depends on its neighbours (decision 0016):
 * with floor below it you see its brick front, with more wall below you see
 * its dark top, and dark edges are drawn where it meets the floor.
 */
public class Wall extends Tile {

    // Frames of wall.png (see tools/SpriteMaker.java).
    public static final int TOP = 0;
    public static final int FACE = 1;
    public static final int EDGE_UP = 2;
    public static final int EDGE_RIGHT = 3;
    public static final int EDGE_LEFT = 4;
    public static final int FACE_END_LEFT = 5;
    public static final int FACE_END_RIGHT = 6;
    public static final int SHADOW_BELOW = 7;
    public static final int SHADOW_RIGHT = 8;

    private static final Color COLOR = Painter.rgb(70, 72, 84);
    private static final Color MORTAR = Painter.rgb(40, 41, 50);
    private static final Color HIGHLIGHT = Painter.rgba(255, 255, 255, 22);

    public Wall(int row, int col) {
        super(row, col);
    }

    @Override
    public boolean isWalkable() {
        return false;
    }

    @Override
    public boolean isFlammable() {
        return false;
    }

    @Override
    public Color getFallbackColor() {
        return COLOR;
    }

    @Override
    public String getImageKey() {
        return "wall";
    }

    @Override
    public boolean isWall() {
        return true;
    }

    /** On its own (menus, How to Play) a wall shows its brick front. */
    @Override
    protected int getImageFrame() {
        return FACE;
    }

    @Override
    public void drawOnBoard(Painter painter, float x, float y, float size, AssetManager assets, GameBoard board) {
        if (assets.getImage(getImageKey()) == null) {
            draw(painter, x, y, size, assets); // no sprite: the brick placeholder
            return;
        }
        for (int piece : getPieces(board)) {
            TextureRegion image = assets.getFrame(getImageKey(), 0, piece, AssetManager.TILE_PIXELS,
                    AssetManager.TILE_PIXELS);
            painter.image(image, x, y, size, size);
        }
    }

    /**
     * The wall.png frames to stack for this wall, bottom first. Floor below: the
     * brick FACE, with its ends darkened where the wall stops. Wall below: the TOP,
     * with an edge on each side that meets something that is not wall.
     */
    public int[] getPieces(GameBoard board) {
        boolean up = isWallAt(board, row - 1, col);
        boolean down = isWallAt(board, row + 1, col);
        boolean left = isWallAt(board, row, col - 1);
        boolean right = isWallAt(board, row, col + 1);
        int[] pieces = new int[4];
        int count = 0;
        pieces[count++] = down ? TOP : FACE;
        if (!up) {
            pieces[count++] = EDGE_UP;
        }
        if (!left) {
            pieces[count++] = down ? EDGE_LEFT : FACE_END_LEFT;
        }
        if (!right) {
            pieces[count++] = down ? EDGE_RIGHT : FACE_END_RIGHT;
        }
        int[] used = new int[count];
        System.arraycopy(pieces, 0, used, 0, count);
        return used;
    }

    /** Off the map counts as wall, so the outer walls don't get edges on the outside. */
    private static boolean isWallAt(GameBoard board, int r, int c) {
        Tile tile = board.getTile(r, c);
        return tile == null || tile.isWall();
    }

    /** A brick pattern: 4 rows of bricks, every other row shifted by half a brick. */
    @Override
    protected void drawFallback(Painter painter, float x, float y, float size) {
        painter.fillRect(COLOR, x, y, size, size);
        float brickHeight = size / 4;
        float brickWidth = size / 2;
        for (int brickRow = 0; brickRow < 4; brickRow++) {
            float top = y + brickRow * brickHeight;
            painter.fillRect(MORTAR, x, top, size, 1);          // mortar line
            painter.fillRect(HIGHLIGHT, x, top + 1, size, 1);   // light edge under it
            float shift = (brickRow % 2 == 0) ? 0 : brickWidth / 2;
            for (float left = x + shift; left < x + size; left += brickWidth) {
                painter.fillRect(MORTAR, left, top, 1, brickHeight);
            }
        }
    }
}
