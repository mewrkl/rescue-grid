package rescuegrid.model.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import rescuegrid.assets.AssetManager;
import rescuegrid.graphics.Painter;

/**
 * Floor with something lying on it: papers, broken glass, a rug or a puddle
 * (decision 0016). It behaves exactly like Floor (walkable, burns, collapses),
 * and the decoration is gone once the tile burns or collapses. Each Decoration
 * is one frame of decor.png and has its own symbol in the level files.
 */
public class DecoratedFloor extends Floor {

    /** What lies on the floor: its symbol in level files, in the same order as decor.png. */
    public enum Decoration {
        PAPERS(','), GLASS(':'), RUG('_'), PUDDLE('~');

        private final char symbol;

        Decoration(char symbol) {
            this.symbol = symbol;
        }

        public char getSymbol() {
            return symbol;
        }

        /** The decoration written with this symbol, or null if there is none. */
        public static Decoration forSymbol(char symbol) {
            for (Decoration decoration : values()) {
                if (decoration.symbol == symbol) {
                    return decoration;
                }
            }
            return null;
        }
    }

    private static final Color MARK = Painter.rgba(255, 255, 255, 60);

    private final Decoration decoration;

    public DecoratedFloor(int row, int col, Decoration decoration) {
        super(row, col);
        this.decoration = decoration;
    }

    public Decoration getDecoration() {
        return decoration;
    }

    /** The floor, then the decoration on top of it. */
    @Override
    public void draw(Painter painter, float x, float y, float size, AssetManager assets) {
        super.draw(painter, x, y, size, assets);
        TextureRegion image = assets.getFrame("decor", 0, decoration.ordinal(), AssetManager.TILE_PIXELS,
                AssetManager.TILE_PIXELS);
        if (image != null) {
            painter.image(image, x, y, size, size);
        } else {
            painter.fillRect(MARK, x + size / 4, y + size / 3, size / 3, size / 4); // a faint mark
        }
    }
}
