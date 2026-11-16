package rescuegrid.model.survivors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import rescuegrid.assets.AssetManager;
import rescuegrid.graphics.Painter;
import rescuegrid.logic.InvalidMoveException;
import rescuegrid.model.Direction;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;

/**
 * Green tag: walking wounded. Anyone can help them: they walk behind the
 * rescuer to the exit, so the rescuer keeps full speed and free hands.
 * Their condition drains at half the usual rate.
 */
public class MinorSurvivor extends Survivor {

    private static final Color GREEN = Painter.rgb(74, 190, 110);
    private static final int WAVE_OUT_FRAME = 3;      // survivor_minor.png's 4th and 5th columns
    private static final int WAVE_UP_FRAME = 4;
    private static final long WAVE_MILLIS = 300;      // waving: arm out, arm up...

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
    protected String whatTheyNeed() {
        return "Walk into them: they follow you";
    }

    @Override
    public String getInjuryName() {
        return "Minor";
    }

    /** They can walk: following you they face the way they go and step; waiting, they wave and shout. */
    @Override
    protected TextureRegion getMapFrame(AssetManager assets, boolean moving) {
        int row;
        int frame;
        if (isWaiting()) {
            row = Direction.DOWN.ordinal();
            frame = (System.currentTimeMillis() / WAVE_MILLIS) % 2 == 0 ? WAVE_OUT_FRAME : WAVE_UP_FRAME;
        } else {
            row = getMoveDirection().ordinal();
            frame = moving ? 1 + getStepCount() % 2 : 0;
        }
        return assets.getFrame(getImageKey(), row, frame, AssetManager.TILE_PIXELS, AssetManager.CHARACTER_PIXELS);
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
