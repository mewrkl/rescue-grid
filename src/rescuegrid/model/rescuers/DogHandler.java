package rescuegrid.model.rescuers;

import java.awt.Color;

import rescuegrid.logic.InvalidMoveException;
import rescuegrid.model.GameBoard;

/**
 * The Dog Handler's dog follows the scent of survivors through corridors,
 * revealing them (and showing them on the minimap) even inside the fog.
 */
public class DogHandler extends Rescuer {

    public static final int SCENT_RANGE = 8; // walking steps, tune in testing

    private static final Color COLOR = new Color(37, 99, 235);

    public DogHandler(int row, int col) {
        super(row, col);
    }

    @Override
    public void specialAction(GameBoard board) throws InvalidMoveException {
        // TODO (Week 2, Member 2):
        //  1. If !canUseSpecial(), throw new InvalidMoveException("Scent is recharging").
        //  2. Ask a MovementCalculator for findByScent(board, this, SCENT_RANGE)
        //     and call reveal() on every survivor it returns.
        //  3. startCooldown(). (No target needed, so this never fails for "no target".)
        throw new InvalidMoveException(getSpecialName() + " is not built yet");
    }

    @Override
    public String getSpecialName() {
        return "Scent";
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
}
