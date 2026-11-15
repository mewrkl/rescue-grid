package rescuegrid.model.survivors;

import com.badlogic.gdx.graphics.Color;

import rescuegrid.graphics.Painter;
import rescuegrid.logic.InvalidMoveException;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;

/**
 * Green tag: walking wounded. Anyone can help them: they walk behind the
 * rescuer to the exit, so the rescuer keeps full speed and free hands.
 * Their condition drains at half the usual rate.
 */
public class MinorSurvivor extends Survivor {

    private static final Color GREEN = Painter.rgb(74, 190, 110);

    public MinorSurvivor(int row, int col, int ticksToLast) {
        super(row, col, ticksToLast);
    }

    @Override
    protected void goWith(Rescuer rescuer) throws InvalidMoveException {
        if (rescuer.isLeading()) {
            throw new InvalidMoveException("Someone is already following you. Take them to the EXIT first");
        }
        rescuer.lead(this);
    }

    @Override
    public String getInjuryName() {
        return "Minor";
    }

    @Override
    public Color getTagColor() {
        return GREEN;
    }

    @Override
    public int getConditionLossPerTick() {
        return 1;
    }
}
