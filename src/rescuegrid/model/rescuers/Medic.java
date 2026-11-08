package rescuegrid.model.rescuers;

import java.awt.Color;

import rescuegrid.logic.InvalidMoveException;
import rescuegrid.model.GameBoard;

/**
 * The Medic treats survivors, buying them more time before they are lost.
 */
public class Medic extends Rescuer {

    public static final int HEAL_AMOUNT = 8; // tune in testing

    private static final Color COLOR = new Color(220, 38, 38);

    public Medic(int row, int col) {
        super(row, col);
    }

    @Override
    public void specialAction(GameBoard board) throws InvalidMoveException {
        // TODO (Week 2, Member 2):
        //  1. If !canUseSpecial(), throw new InvalidMoveException("Treat is recharging").
        //  2. If carrying a survivor, heal them by HEAL_AMOUNT.
        //  3. Otherwise heal the revealed survivor on the faced tile, if there is one
        //     (getFacedTile(board), then board.getSurvivorAt(row, col)).
        //  4. If there was nobody to heal, throw new InvalidMoveException("No survivor to treat").
        //  5. startCooldown().
        throw new InvalidMoveException(getSpecialName() + " is not built yet");
    }

    @Override
    public String getSpecialName() {
        return "Treat";
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
}
