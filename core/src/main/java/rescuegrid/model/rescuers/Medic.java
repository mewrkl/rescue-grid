package rescuegrid.model.rescuers;

import com.badlogic.gdx.graphics.Color;

import rescuegrid.graphics.Painter;
import rescuegrid.logic.InvalidMoveException;
import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;
import rescuegrid.model.tiles.Tile;

/**
 * The Medic treats survivors, buying them more time before they are lost, and
 * stabilises critical survivors so backup can fetch them. She treats the survivor
 * she is carrying or leading, or one lying on her tile or the tile she faces.
 * In training she can also practise on a dummy (a tile that isTreatable()).
 */
public class Medic extends Rescuer {

    public static final int HEAL_AMOUNT = 8 * Survivor.POINTS_PER_TICK; // 8 world ticks of a serious survivor

    private static final Color COLOR = Painter.rgb(220, 38, 38);

    public Medic(int row, int col) {
        super(row, col);
    }

    /** Someone carried, following, here or faced, or the training dummy. */
    @Override
    public boolean hasSpecialTarget(GameBoard board) {
        if (getCarriedSurvivor() != null || getFollower() != null || findSurvivorNearby(board) != null) {
            return true;
        }
        Tile faced = getFacedTile(board);
        return faced != null && faced.isTreatable();
    }

    @Override
    public String specialAction(GameBoard board) throws InvalidMoveException {
        if (!canUseSpecial()) {
            throw new InvalidMoveException("Treat is recharging");
        }
        // The person you face (or stand on) comes first: someone following you must
        // never stop you treating a red survivor in front of you. Otherwise Treat
        // goes to the one you carry, then the one you lead.
        Survivor patient = findSurvivorNearby(board);
        if (patient == null) {
            patient = getCarriedSurvivor();
        }
        if (patient == null) {
            patient = getFollower();
        }
        if (patient == null) {
            Tile faced = getFacedTile(board);
            if (faced != null && faced.isTreatable()) {
                startCooldown();
                return "Treated! On a real survivor, Treat gives them more time";
            }
            throw new InvalidMoveException("Face or carry a survivor to treat");
        }
        String result = patient.treat(HEAL_AMOUNT); // a critical survivor is also stabilised
        startCooldown();
        return result;
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
