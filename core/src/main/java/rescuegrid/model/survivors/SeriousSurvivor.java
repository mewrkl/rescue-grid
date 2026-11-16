package rescuegrid.model.survivors;

import com.badlogic.gdx.graphics.Color;

import rescuegrid.graphics.Painter;
import rescuegrid.logic.InvalidMoveException;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;

/**
 * Yellow tag: can't walk, but can be moved. A rescuer with free hands carries
 * them, at half walking speed. Their condition drains at the usual rate.
 */
public class SeriousSurvivor extends Survivor {

    private static final Color YELLOW = Painter.rgb(236, 196, 40);

    public SeriousSurvivor(int row, int col, int ticksToLast) {
        super(row, col, ticksToLast);
    }

    @Override
    protected void goWith(Rescuer rescuer) throws InvalidMoveException {
        if (rescuer.isCarrying()) {
            throw new InvalidMoveException("Your hands are full. Take them to the EXIT first");
        }
        rescuer.pickUp(this);
    }

    @Override
    protected String whatTheyNeed() {
        return "Walk into them to carry them";
    }

    @Override
    public String getInjuryName() {
        return "Serious";
    }

    @Override
    public Color getTagColor() {
        return YELLOW;
    }

    @Override
    public int getConditionLossPerTick() {
        return 2;
    }
}
