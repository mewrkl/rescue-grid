package rescuegrid.ui;

import java.awt.Color;
import java.awt.Graphics2D;

import rescuegrid.assets.AssetManager;
import rescuegrid.model.Drawable;
import rescuegrid.model.GameBoard;

/**
 * The small map in the corner. It shows the whole building at a few pixels per
 * tile, but only the parts the team has already explored, plus where the exit,
 * the rescuers and any revealed survivors are.
 *
 * Implements Drawable like tiles do; here "size" means pixels per tile.
 */
public class Minimap implements Drawable {

    private static final Color BACKGROUND = new Color(0, 0, 0, 200);
    private static final Color FRAME = new Color(255, 255, 255, 60);

    private GameBoard board;

    public Minimap() {
        this.board = null;
    }

    public void setBoard(GameBoard board) {
        this.board = board;
    }

    @Override
    public void draw(Graphics2D g, int x, int y, int size, AssetManager assets) {
        if (board == null) {
            return;
        }
        int width = board.getCols() * size;
        int height = board.getRows() * size;
        g.setColor(BACKGROUND);
        g.fillRect(x, y, width, height);

        // TODO (Week 2, Member 1):
        //  1. For every tile that isExplored(): fill a size x size square at
        //     (x + col * size, y + row * size) with tile.getFallbackColor().
        //  2. Draw the exit in green, every active rescuer as a dot in its colour
        //     (the active one bigger), and every revealed, waiting survivor as a yellow dot.

        g.setColor(FRAME);
        g.drawRect(x, y, width - 1, height - 1);
    }
}
