package rescuegrid.model.rescuers;

import com.badlogic.gdx.graphics.Color;

import rescuegrid.graphics.Painter;
import rescuegrid.logic.InvalidMoveException;
import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;
import rescuegrid.model.tiles.Tile;

/**
 * The Medic treats survivors, buying them more time before they are lost.
 * She can treat the survivor she is carrying, or one lying on the tile she faces.
 */
public class Medic extends Rescuer {

    public static final int HEAL_AMOUNT = 8; // world ticks of condition

    private static final Color COLOR = Painter.rgb(220, 38, 38);

    public Medic(int row, int col) {
        super(row, col);
    }

    @Override
    public String specialAction(GameBoard board) throws InvalidMoveException {
        if (!canUseSpecial()) {
            throw new InvalidMoveException("Treat is recharging");
        }
        Survivor patient = getCarriedSurvivor();
        if (patient == null) {
            Tile faced = getFacedTile(board);
            if (faced != null) {
                patient = board.getSurvivorAt(faced.getRow(), faced.getCol());
            }
        }
        if (patient == null) {
            throw new InvalidMoveException("Face or carry a survivor to treat");
        }
        patient.heal(HEAL_AMOUNT);
        startCooldown();
        return "Survivor treated";
    }

    @Override
    public String getSpecialName() {
        return "Treat";
    }

    @Override
    public String getSpecialSound() {
        return "heal";
    }

    @Override
    public double getCooldownSeconds() {
        return 4.0;
    }

    @Override
    public String getName() {
        return "Medic";
    }

    @Override
    public String getImageKey() {
        return "medic";
    }

    @Override
    public Color getFallbackColor() {
        return COLOR;
    }

    /** A white cross. */
    @Override
    protected void drawRoleIcon(Painter painter, float x, float y, float size) {
        float arm = size * 2 / 5;
        float thick = size / 8;
        float cx = x + size / 2;
        float cy = y + size / 2;
        painter.fillRect(Color.WHITE, cx - arm / 2, cy - thick / 2, arm, thick);
        painter.fillRect(Color.WHITE, cx - thick / 2, cy - arm / 2, thick, arm);
    }
}
