package rescuegrid.ui;

import com.badlogic.gdx.graphics.Color;

import rescuegrid.assets.AssetManager;
import rescuegrid.graphics.Painter;
import rescuegrid.model.Drawable;
import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;
import rescuegrid.model.tiles.Tile;

/**
 * The corner map: explored tiles, the exit, the rescuers and revealed
 * survivors. Here size means pixels per tile.
 */
public class Minimap implements Drawable {

    private static final Color BACKGROUND = Painter.rgba(0, 0, 0, 210);
    private static final Color FRAME = Painter.rgba(255, 255, 255, 70);
    private static final Color EXIT = Painter.rgb(74, 222, 128);
    private static final Color SURVIVOR = Painter.rgb(250, 204, 21);
    private static final int BLINK_MILLIS = 400;

    private GameBoard board;
    private Rescuer activeRescuer;

    public Minimap() {
        this.board = null;
        this.activeRescuer = null;
    }

    public void setBoard(GameBoard board) {
        this.board = board;
    }

    public void setActiveRescuer(Rescuer activeRescuer) {
        this.activeRescuer = activeRescuer;
    }

    @Override
    public void draw(Painter painter, float x, float y, float size, AssetManager assets) {
        if (board == null) {
            return;
        }
        float width = board.getCols() * size;
        float height = board.getRows() * size;
        painter.outlineRect(FRAME, x - 1, y - 1, width + 2, height + 2, 1);
        painter.fillRect(BACKGROUND, x, y, width, height);

        // 1. Explored tiles in their own colour (the exit stands out in bright green).
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                Tile tile = board.getTile(row, col);
                if (tile.isExplored()) {
                    painter.fillRect(tile.isExit() ? EXIT : tile.getFallbackColor(),
                            x + col * size, y + row * size, size, size);
                }
            }
        }

        // 2. Revealed survivors, blinking so they catch your eye.
        boolean blinkOn = (System.currentTimeMillis() / BLINK_MILLIS) % 2 == 0;
        if (blinkOn) {
            for (Survivor survivor : board.getSurvivors()) {
                if (survivor.isWaiting() && survivor.isRevealed()) {
                    painter.fillRect(SURVIVOR, x + survivor.getCol() * size, y + survivor.getRow() * size,
                            size, size);
                }
            }
        }

        // 3. Rescuers as dots in their own colour; the one you control is bigger.
        for (Rescuer rescuer : board.getRescuers()) {
            if (rescuer.isDowned()) {
                continue;
            }
            float dot = rescuer == activeRescuer ? size * 2 : size + 2;
            float centerX = x + rescuer.getCol() * size + size / 2;
            float centerY = y + rescuer.getRow() * size + size / 2;
            painter.fillOval(rescuer.getFallbackColor(), centerX - dot / 2, centerY - dot / 2, dot, dot);
            if (rescuer == activeRescuer) {
                painter.outlineOval(Color.WHITE, centerX - dot / 2, centerY - dot / 2, dot, dot, 1);
            }
        }
    }
}
