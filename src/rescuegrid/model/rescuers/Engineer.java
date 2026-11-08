package rescuegrid.model.rescuers;

import java.awt.Color;

import rescuegrid.logic.InvalidMoveException;
import rescuegrid.model.GameBoard;

/**
 * The Engineer clears rubble, reopening paths that aftershocks have blocked.
 */
public class Engineer extends Rescuer {

    private static final Color COLOR = new Color(234, 140, 0);

    public Engineer(int row, int col) {
        super(row, col);
    }

    @Override
    public void specialAction(GameBoard board) throws InvalidMoveException {
        // TODO (Week 2, Member 2):
        //  1. If !canUseSpecial(), throw new InvalidMoveException("Clear Rubble is recharging").
        //  2. Tile target = getFacedTile(board).
        //  3. If target is null or !target.isClearable(), throw
        //     new InvalidMoveException("Nothing to clear here").
        //  4. board.replaceTile(target.getRow(), target.getCol(), new Floor(...)).
        //  5. startCooldown().
        throw new InvalidMoveException(getSpecialName() + " is not built yet");
    }

    @Override
    public String getSpecialName() {
        return "Clear Rubble";
    }

    @Override
    public double getCooldownSeconds() {
        return 3.0;
    }

    @Override
    public String getName() {
        return "Engineer";
    }

    @Override
    public String getImageKey() {
        return "engineer";
    }

    @Override
    public Color getFallbackColor() {
        return COLOR;
    }
}
