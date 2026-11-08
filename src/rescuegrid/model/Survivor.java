package rescuegrid.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import rescuegrid.assets.AssetManager;

/**
 * A trapped person. Starts hidden in the fog, loses condition every world tick,
 * and is lost at 0. A rescuer carries them to the Exit to save them.
 *
 * All fields are private: condition can only change through tickCondition()
 * and heal(), so it can never go below 0 or above MAX_CONDITION.
 */
public class Survivor implements Drawable {

    public static final int MAX_CONDITION = 20; // 20 ticks x 3 s = 60 s, tune in testing

    private static final Color FALLBACK_COLOR = new Color(250, 204, 21);

    private int row;
    private int col;
    private int condition;
    private boolean revealed;
    private boolean carried;
    private boolean rescued;
    private boolean lost;

    public Survivor(int row, int col) {
        this.row = row;
        this.col = col;
        this.condition = MAX_CONDITION;
        this.revealed = false;
        this.carried = false;
        this.rescued = false;
        this.lost = false;
    }

    /** Called once per world tick. */
    public void tickCondition() {
        // TODO (Week 3, Member 2): if already rescued or lost, do nothing.
        //  Otherwise lower condition by 1. If it reaches 0, set lost = true.
    }

    /** Called by the Medic's special action. */
    public void heal(int amount) {
        // TODO (Week 3, Member 2): raise condition by amount, but never above MAX_CONDITION.
    }

    public void reveal() {
        revealed = true;
    }

    // ----- State changes, called by Rescuer and HazardManager -----

    public void markCarried() {
        carried = true;
    }

    /** The rescuer put this survivor down (or was downed) at (row, col). */
    public void markDropped(int row, int col) {
        carried = false;
        this.row = row;
        this.col = col;
    }

    /** Keeps the survivor's position in step with the rescuer carrying them. */
    public void moveWithCarrier(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void markRescued() {
        carried = false;
        rescued = true;
    }

    public void markLost() {
        lost = true;
    }

    // ----- Getters -----

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getCondition() {
        return condition;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public boolean isCarried() {
        return carried;
    }

    public boolean isRescued() {
        return rescued;
    }

    public boolean isLost() {
        return lost;
    }

    /** Still lying in the building, waiting to be picked up. */
    public boolean isWaiting() {
        return !carried && !rescued && !lost;
    }

    /** Draws the survivor image, or a yellow circle with an "S" if it's missing. */
    @Override
    public void draw(Graphics2D g, int x, int y, int size, AssetManager assets) {
        BufferedImage image = assets.getImage("survivor");
        if (image != null) {
            g.drawImage(image, x, y, size, size, null);
            return;
        }
        int margin = size / 5;
        g.setColor(FALLBACK_COLOR);
        g.fillOval(x + margin, y + margin, size - 2 * margin, size - 2 * margin);
        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size / 3));
        FontMetrics metrics = g.getFontMetrics();
        String letter = "S";
        g.drawString(letter,
                x + (size - metrics.stringWidth(letter)) / 2,
                y + (size + metrics.getAscent() - metrics.getDescent()) / 2);
    }
}
