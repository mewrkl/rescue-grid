package rescuegrid.model.rescuers;

import com.badlogic.gdx.graphics.Color;
import java.util.List;

import rescuegrid.graphics.Painter;
import rescuegrid.logic.InvalidMoveException;
import rescuegrid.logic.MovementCalculator;
import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;

/**
 * The dog follows survivors' scent through corridors and rubble (not walls)
 * and reveals them, even in the fog.
 */
public class DogHandler extends Rescuer {

    public static final int SCENT_RANGE = 8; // walking steps

    private static final Color COLOR = Painter.rgb(37, 99, 235);

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
        startCooldown();
        if (found.isEmpty()) {
            return "No scent nearby";
        }
        if (newlyFound == 0) {
            return "Nobody new nearby";
        }
        return "The dog found " + newlyFound + (newlyFound == 1 ? " survivor!" : " survivors!");
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
