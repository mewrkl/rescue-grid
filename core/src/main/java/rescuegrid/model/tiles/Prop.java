package rescuegrid.model.tiles;

import com.badlogic.gdx.graphics.Color;

import rescuegrid.graphics.Painter;

/**
 * Furniture: a sofa, desk, hospital bed and so on (decision 0016). It blocks
 * walking like a wall, but it is low, so sight and the dog's scent pass over it.
 * It doesn't burn or collapse. Each Kind is one frame of props.png and has its
 * own letter in the level files.
 */
public class Prop extends Tile {

    /** What the furniture is: its letter in level files, in the same order as props.png. */
    public enum Kind {
        SOFA('o'), TABLE('t'), BOOKSHELF('h'), PLANT('p'), BED('b'), DESK('k'), FILING_CABINET('f'),
        WATER_COOLER('v'), HOSPITAL_BED('n'), MEDICINE_CABINET('u'), WHEELCHAIR('w'), IV_STAND('i'), LOCKER('l');

        private final char symbol;

        Kind(char symbol) {
            this.symbol = symbol;
        }

        public char getSymbol() {
            return symbol;
        }

        /** The kind written with this letter, or null if the letter isn't furniture. */
        public static Kind forSymbol(char symbol) {
            for (Kind kind : values()) {
                if (kind.symbol == symbol) {
                    return kind;
                }
            }
            return null;
        }
    }

    private static final Color FLOOR = Painter.rgb(196, 186, 166);
    private static final Color WOOD = Painter.rgb(138, 90, 59);
    private static final Color EDGE = Painter.rgb(60, 40, 28);

    private final Kind kind;

    public Prop(int row, int col, Kind kind) {
        super(row, col);
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    public boolean isWalkable() {
        return false;
    }

    /** Low furniture: you can see (and the dog can smell) over it. */
    @Override
    public boolean isSeeThrough() {
        return true;
    }

    @Override
    public boolean isFlammable() {
        return false;
    }

    @Override
    public Color getFallbackColor() {
        return WOOD;
    }

    @Override
    public String getImageKey() {
        return "props";
    }

    /**
     * props.png has every kind intact, then every kind damaged by the earthquake.
     * About a third of the furniture is damaged, always the same pieces.
     */
    @Override
    protected int getImageFrame() {
        boolean damaged = pickVariant(2, 1) == 1;
        return kind.ordinal() + (damaged ? Kind.values().length : 0);
    }

    /** Without sprites: a wooden box on the floor. */
    @Override
    protected void drawFallback(Painter painter, float x, float y, float size) {
        painter.fillRect(FLOOR, x, y, size, size);
        float inset = size / 6;
        painter.fillRect(WOOD, x + inset, y + inset, size - 2 * inset, size - 2 * inset);
        painter.outlineRect(EDGE, x + inset, y + inset, size - 2 * inset, size - 2 * inset, 2);
    }
}
