package rescuegrid.model.survivors;

import com.badlogic.gdx.graphics.Color;

import rescuegrid.graphics.Painter;
import rescuegrid.logic.InvalidMoveException;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;

/**
 * Red tag: too badly hurt to move. The Medic must stabilise them first, then
 * anyone can radio for backup (Q), and a stretcher team carries them out after
 * BACKUP_SECONDS. Their condition drains fast until they are stabilised.
 */
public class CriticalSurvivor extends Survivor {

    public static final double BACKUP_SECONDS = 10.0;

    private static final Color RED = Painter.rgb(220, 50, 50);
    private static final Color BADGE = Painter.rgba(0, 0, 0, 190);

    private boolean stabilised;
    private double backupSecondsLeft; // 0 = backup not called

    public CriticalSurvivor(int row, int col, int ticksToLast) {
        super(row, col, ticksToLast);
        this.stabilised = false;
        this.backupSecondsLeft = 0;
    }

    @Override
    protected void goWith(Rescuer rescuer) throws InvalidMoveException {
        if (isBackupComing()) {
            throw new InvalidMoveException("Backup arrives in " + (int) Math.ceil(backupSecondsLeft) + " s");
        }
        if (!stabilised) {
            throw new InvalidMoveException("Critical: too hurt to move. The Medic must stabilise them (E)");
        }
        throw new InvalidMoveException("Stabilised. Press Q to call backup to carry them out");
    }

    /** Treating a critical survivor also stabilises them, which slows the drain. */
    @Override
    public String treat(int amount) {
        super.treat(amount);
        stabilised = true;
        return isBackupComing() ? "Survivor treated" : "Stabilised! Press Q to call backup";
    }

    @Override
    public void callBackup() throws InvalidMoveException {
        if (isTrapped()) {
            throw new InvalidMoveException("Free them from the debris first (Engineer, E)");
        }
        if (!stabilised) {
            throw new InvalidMoveException("Stabilise them first (Medic, E)");
        }
        if (isBackupComing()) {
            throw new InvalidMoveException("Backup is already on the way");
        }
        backupSecondsLeft = BACKUP_SECONDS;
    }

    /** Counts the backup down; when it reaches 0 the stretcher team has carried them out. */
    @Override
    public void update(double seconds) {
        if (!isBackupComing() || isLost()) {
            return;
        }
        backupSecondsLeft = Math.max(0, backupSecondsLeft - seconds);
        if (backupSecondsLeft == 0) {
            markRescued();
        }
    }

    @Override
    public String getInjuryName() {
        return "Critical";
    }

    @Override
    public Color getTagColor() {
        return RED;
    }

    @Override
    public int getConditionLossPerTick() {
        return stabilised ? 1 : 3;
    }

    public boolean isStabilised() {
        return stabilised;
    }

    public boolean isBackupComing() {
        return backupSecondsLeft > 0;
    }

    public double getBackupSecondsLeft() {
        return backupSecondsLeft;
    }

    /** Lying down instead of waving: they can't get up. */
    @Override
    protected void drawFigure(Painter painter, float x, float y, float size) {
        float head = size / 4;
        float bodyY = y + size / 2 - size / 10;
        painter.fillRect(RED, x + size / 3, bodyY, size / 2, size / 5);
        painter.outlineRect(getOutlineColor(), x + size / 3, bodyY, size / 2, size / 5, 1);
        painter.fillOval(getSkinColor(), x + size / 3 - head, bodyY - head / 8, head, head);
        painter.outlineOval(getOutlineColor(), x + size / 3 - head, bodyY - head / 8, head, head, 1);
    }

    /** A white cross once stabilised, and the backup countdown while they wait. */
    @Override
    protected void drawExtras(Painter painter, float x, float y, float size) {
        if (stabilised) {
            float badge = size / 4;
            float bx = x + size - badge - 3;
            float by = y + 3;
            painter.fillRect(BADGE, bx, by, badge, badge);
            painter.fillRect(Color.WHITE, bx + badge * 0.4f, by + 2, badge * 0.2f, badge - 4);
            painter.fillRect(Color.WHITE, bx + 2, by + badge * 0.4f, badge - 4, badge * 0.2f);
        }
        if (isBackupComing()) {
            painter.text((int) Math.ceil(backupSecondsLeft) + "s", Color.WHITE, x + size / 2, y + size / 8);
        }
    }
}
