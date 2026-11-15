package rescuegrid.model.rescuers;

import com.badlogic.gdx.graphics.Color;

import rescuegrid.graphics.Painter;
import rescuegrid.logic.InvalidMoveException;
import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;
import rescuegrid.model.tiles.Floor;
import rescuegrid.model.tiles.Tile;

/**
 * The Engineer clears rubble, reopening paths that aftershocks have blocked,
 * and lifts debris off trapped survivors.
 */
public class Engineer extends Rescuer {

    private static final Color COLOR = Painter.rgb(234, 120, 0);
    private static final Color HARD_HAT = Painter.rgb(250, 214, 30);
    private static final Color HAT_EDGE = Painter.rgb(120, 90, 0);

    public Engineer(int row, int col) {
        super(row, col);
    }

    @Override
    public String specialAction(GameBoard board) throws InvalidMoveException {
        if (!canUseSpecial()) {
            throw new InvalidMoveException("Clear Rubble is recharging");
        }
        Tile target = getFacedTile(board);
        if (target != null && target.isClearable()) {
            board.replaceTile(target.getRow(), target.getCol(), new Floor(target.getRow(), target.getCol()));
            startCooldown();
            return "Rubble cleared";
        }
        Survivor survivor = findSurvivorNearby(board);
        if (survivor != null && survivor.isTrapped()) {
            survivor.freeFromDebris();
            startCooldown();
            return "Debris lifted. They can be moved now";
        }
        throw new InvalidMoveException("Face rubble or a trapped survivor");
    }

    @Override
    public String getSpecialName() {
        return "Clear Rubble";
    }

    @Override
    public String getSpecialSound() {
        return "clear_rubble";
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

    /** A yellow hard hat with a brim. */
    @Override
    protected void drawRoleIcon(Painter painter, float x, float y, float size) {
        float hatWidth = size / 2;
        float hatHeight = hatWidth * 4 / 5;
        float hatX = x + (size - hatWidth) / 2;
        float hatY = y + size / 4;
        painter.fillTopHalfOval(HARD_HAT, hatX, hatY, hatWidth, hatHeight);
        painter.fillRect(HARD_HAT, hatX - size / 12, hatY + hatHeight / 2 - 1, hatWidth + size / 6, size / 12);
        painter.outlineTopHalfOval(HAT_EDGE, hatX, hatY, hatWidth, hatHeight, 1);
        painter.line(HAT_EDGE, x + size / 2, hatY, x + size / 2, hatY + hatHeight / 2, 1);
    }
}
