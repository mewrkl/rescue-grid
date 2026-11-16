package rescuegrid.model.tiles;

import com.badlogic.gdx.graphics.Color;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import rescuegrid.assets.AssetManager;
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
        SOFA('o', "Sofa"), TABLE('t', "Table"), BOOKSHELF('h', "Bookshelf"), PLANT('p', "Plant"), BED('b', "Bed"),
        DESK('k', "Desk"), FILING_CABINET('f', "Filing cabinet"), WATER_COOLER('v', "Water cooler"),
        HOSPITAL_BED('n', "Hospital bed"), MEDICINE_CABINET('u', "Medicine cabinet"), WHEELCHAIR('w', "Wheelchair"),
        IV_STAND('i', "Drip stand"), LOCKER('l', "Locker");

        private final char symbol;
        private final String displayName;

        Kind(char symbol, String displayName) {
            this.symbol = symbol;
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        /**
         * Tall furniture stands 1.5 tiles high (props_tall.png) so it reads as
         * furniture, not as a door or a wall. Its frame there, or -1 if it's low.
         */
        int getTallFrame() {
            switch (this) {
                case LOCKER:
                    return 0;
                case BOOKSHELF:
                    return 1;
                case FILING_CABINET:
                    return 2;
                case MEDICINE_CABINET:
                    return 3;
                default:
                    return -1;
            }
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

    private static final int TALL_KINDS = 4; // props_tall.png: 4 intact, then the same 4 damaged

    private final Kind kind;
    private final Floor ground; // the floor the furniture stands on

    public Prop(int row, int col, Kind kind) {
        super(row, col);
        this.kind = kind;
        this.ground = new Floor(row, col);
    }

    /**
     * The floor first, then the furniture on it. The furniture sprite is see-through
     * round its edges, so without the floor those edges showed as a black box.
     */
    @Override
    public void draw(Painter painter, float x, float y, float size, AssetManager assets) {
        ground.draw(painter, x, y, size, assets);
        int tall = kind.getTallFrame();
        if (tall >= 0) {
            TextureRegion image = assets.getFrame("props_tall", 0, tall + (isDamaged() ? TALL_KINDS : 0),
                    AssetManager.TILE_PIXELS, AssetManager.CHARACTER_PIXELS);
            if (image != null) {
                // Standing up like a person: its top covers the lower half of the tile behind it.
                float height = size * AssetManager.CHARACTER_PIXELS / AssetManager.TILE_PIXELS;
                painter.image(image, x, y + size - height, size, height);
                return;
            }
        }
        super.draw(painter, x, y, size, assets);
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    public boolean isWalkable() {
        return false;
    }

    /** Walking into it says what it is, so it's clear the furniture is solid. */
    @Override
    public String getBumpName() {
        return kind.getDisplayName();
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
        return kind.ordinal() + (isDamaged() ? Kind.values().length : 0);
    }

    /** About a third of the furniture is damaged, always the same pieces. */
    private boolean isDamaged() {
        return pickVariant(2, 1) == 1;
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
