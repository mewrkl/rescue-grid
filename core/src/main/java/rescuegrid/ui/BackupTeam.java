package rescuegrid.ui;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import rescuegrid.assets.AssetManager;
import rescuegrid.graphics.Painter;
import rescuegrid.model.Direction;
import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;
import rescuegrid.model.survivors.CriticalSurvivor;
import rescuegrid.model.tiles.Tile;

/**
 * The stretcher team you radio for (decision 0020): two paramedics walk in from
 * the EXIT so they reach the red survivor just as the backup countdown ends,
 * lift them onto the stretcher, and carry them back out ("Saved!").
 * Only a picture: the countdown in CriticalSurvivor decides the rescue, and the
 * team walks through fire and never blocks the player.
 */
class BackupTeam {

    private static final double MAX_STEP_SECONDS = 0.4;
    private static final double OUT_STEP_FACTOR = 1.15; // a little slower with someone on the stretcher
    private static final double LIFT_SECONDS = 0.8;
    private static final double SAVED_SECONDS = 1.2;
    private static final long STEP_FRAME_MILLIS = 180;
    private static final Color SAVED = Painter.rgb(74, 222, 128);
    private static final Color SAVED_BOX = Painter.rgba(10, 10, 10, 200);

    private enum Phase { WAITING, WALKING_IN, LIFTING, WALKING_OUT, SAVED, DONE }

    private final Survivor patient;
    private final List<int[]> path;      // {row, col} from the EXIT (index 0) to the survivor
    private final double stepSeconds;
    private final double appearAt;       // seconds after the call when they step out of the EXIT
    private Phase phase;
    private double clock;                // seconds in the current phase (after arrival)
    private double position;             // how far along the path the front of the team is
    private boolean carrying;

    BackupTeam(Survivor patient, GameBoard board, int exitRow, int exitCol) {
        this.patient = patient;
        this.path = findPath(board, exitRow, exitCol, patient.getRow(), patient.getCol());
        int steps = Math.max(0, path.size() - 1);
        this.stepSeconds = steps == 0 ? MAX_STEP_SECONDS
                : Math.min(MAX_STEP_SECONDS, (CriticalSurvivor.BACKUP_SECONDS - 0.5) / steps);
        this.appearAt = CriticalSurvivor.BACKUP_SECONDS - steps * stepSeconds;
        this.phase = Phase.WAITING;
        this.position = 0;
        this.carrying = false;
    }

    boolean isDone() {
        return phase == Phase.DONE;
    }

    /** Moves the team on. Called every frame while the game is playing. */
    void update(double seconds) {
        int last = path.size() - 1;
        switch (phase) {
            case WAITING:
            case WALKING_IN:
                if (patient.isLost()) {                 // too late: go back out empty-handed
                    phase = phase == Phase.WAITING ? Phase.DONE : Phase.WALKING_OUT;
                    position += 1;                      // walking out, the front is one step behind
                    break;
                }
                if (patient.isRescued()) {              // the countdown ended: they're here
                    position = last;
                    phase = Phase.LIFTING;
                    clock = 0;
                    carrying = true;
                    break;
                }
                double sinceCall = CriticalSurvivor.BACKUP_SECONDS - patient.getBackupSecondsLeft();
                if (sinceCall >= appearAt) {
                    phase = Phase.WALKING_IN;
                    position = Math.min(last, (sinceCall - appearAt) / stepSeconds);
                }
                break;
            case LIFTING:
                clock += seconds;
                if (clock >= LIFT_SECONDS) {
                    phase = last == 0 ? Phase.SAVED : Phase.WALKING_OUT;
                    clock = 0;
                }
                break;
            case WALKING_OUT:
                position -= seconds / (stepSeconds * OUT_STEP_FACTOR);
                if (position <= 0) {
                    position = 0;
                    phase = carrying ? Phase.SAVED : Phase.DONE;
                    clock = 0;
                }
                break;
            case SAVED:
                clock += seconds;
                if (clock >= SAVED_SECONDS) {
                    phase = Phase.DONE;
                }
                break;
            default:
                break;
        }
    }

    /** Two paramedics with the stretcher between them, or "Saved!" rising from the EXIT. */
    void draw(Painter painter, Camera camera, AssetManager assets, GameBoard board, int size) {
        if (phase == Phase.WAITING || phase == Phase.DONE) {
            return;
        }
        if (phase == Phase.SAVED) {
            int[] exit = path.get(0);
            float rise = (float) (clock / SAVED_SECONDS) * size * 0.6f;
            painter.textOnBox("Saved!", SAVED, SAVED_BOX, camera.toMapX(exit[1]) + size / 2f,
                    camera.toMapY(exit[0]) - size * 0.6f - rise, 4);
            return;
        }
        boolean in = phase == Phase.WALKING_IN;
        boolean moving = phase != Phase.LIFTING;
        // Walking in, the front is at `position`; walking out, the front is one step nearer the EXIT.
        double front = in ? position : position - 1;
        double back = in ? position - 1 : position;
        if (phase == Phase.LIFTING) {
            front = path.size() - 1;
            back = front - 1;
        }
        double[] frontSpot = spot(front);
        double[] backSpot = spot(back);
        Direction facing = heading(back, front);
        boolean showFront = front >= 0 && isSeen(board, frontSpot);
        boolean showBack = back >= 0 && isSeen(board, backSpot);

        List<Runnable> drawings = new ArrayList<>();
        List<double[]> order = new ArrayList<>();
        if (showFront) {
            order.add(new double[] {frontSpot[0], drawings.size()});
            drawings.add(() -> drawMedic(painter, camera, assets, frontSpot, facing, moving, 0, size));
        }
        if (showBack) {
            order.add(new double[] {backSpot[0], drawings.size()});
            drawings.add(() -> drawMedic(painter, camera, assets, backSpot, facing, moving, 1, size));
        }
        if (showFront || showBack) {
            double[] middle = {(frontSpot[0] + backSpot[0]) / 2, (frontSpot[1] + backSpot[1]) / 2};
            boolean across = facing == Direction.LEFT || facing == Direction.RIGHT;
            order.add(new double[] {middle[0] + 0.01, drawings.size()});
            drawings.add(() -> drawStretcher(painter, camera, assets, middle, across, size));
        }
        order.sort((a, b) -> Double.compare(a[0], b[0])); // further up the screen is drawn first
        for (double[] entry : order) {
            drawings.get((int) entry[1]).run();
        }
    }

    private void drawMedic(Painter painter, Camera camera, AssetManager assets, double[] spot,
                           Direction facing, boolean moving, int offset, int size) {
        int frame = moving ? 1 + (int) ((System.currentTimeMillis() / STEP_FRAME_MILLIS + offset) % 2) : 0;
        TextureRegion image = assets.getFrame("paramedic", facing.ordinal(), frame,
                AssetManager.TILE_PIXELS, AssetManager.CHARACTER_PIXELS);
        if (image == null) {
            return;
        }
        float height = size * AssetManager.CHARACTER_PIXELS / (float) AssetManager.TILE_PIXELS;
        painter.image(image, camera.toMapX(spot[1]), camera.toMapY(spot[0]) + size - height, size, height);
    }

    private void drawStretcher(Painter painter, Camera camera, AssetManager assets, double[] spot,
                               boolean across, int size) {
        int frame = (across ? 0 : 2) + (carrying ? 1 : 0);
        TextureRegion image = assets.getFrame("stretcher", 0, frame, AssetManager.TILE_PIXELS,
                AssetManager.TILE_PIXELS);
        if (image != null) {
            painter.image(image, camera.toMapX(spot[1]), camera.toMapY(spot[0]) - size * 0.2f, size, size);
        }
    }

    /** Where on the path a position is, as a fractional {row, col}. */
    private double[] spot(double along) {
        double clamped = Math.max(0, Math.min(path.size() - 1, along));
        int i = (int) Math.floor(clamped);
        int j = Math.min(path.size() - 1, i + 1);
        double f = clamped - i;
        int[] a = path.get(i);
        int[] b = path.get(j);
        return new double[] {a[0] + (b[0] - a[0]) * f, a[1] + (b[1] - a[1]) * f};
    }

    /**
     * The way the team faces: from the back one towards the front one (in towards
     * the survivor, out towards the EXIT). If they share a spot, the path decides.
     */
    private Direction heading(double back, double front) {
        double[] from = spot(back);
        double[] to = spot(front);
        if (from[0] == to[0] && from[1] == to[1]) {
            boolean towardsExit = phase == Phase.WALKING_OUT;
            from = spot(towardsExit ? front + 1 : front - 1);
        }
        double dRow = to[0] - from[0];
        double dCol = to[1] - from[1];
        if (Math.abs(dCol) >= Math.abs(dRow)) {
            return dCol >= 0 ? Direction.RIGHT : Direction.LEFT;
        }
        return dRow >= 0 ? Direction.DOWN : Direction.UP;
    }

    private static boolean isSeen(GameBoard board, double[] spot) {
        int row = (int) Math.round(spot[0]);
        int col = (int) Math.round(spot[1]);
        return board.inBounds(row, col) && board.getTile(row, col).isExplored();
    }

    /**
     * The shortest way from the EXIT to the survivor (breadth-first search) over
     * floor, fire and rubble, around walls and furniture. If there is none, the
     * team simply arrives at the survivor's tile.
     */
    static List<int[]> findPath(GameBoard board, int fromRow, int fromCol, int toRow, int toCol) {
        int rows = board.getRows();
        int cols = board.getCols();
        int[][] cameFrom = new int[rows * cols][];
        boolean[] seen = new boolean[rows * cols];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[] {fromRow, fromCol});
        seen[fromRow * cols + fromCol] = true;
        while (!queue.isEmpty()) {
            int[] here = queue.poll();
            if (here[0] == toRow && here[1] == toCol) {
                List<int[]> path = new ArrayList<>();
                for (int[] at = here; at != null; at = cameFrom[at[0] * cols + at[1]]) {
                    path.add(at);
                }
                Collections.reverse(path);
                return path;
            }
            for (Direction direction : Direction.values()) {
                int row = here[0] + direction.getRowChange();
                int col = here[1] + direction.getColChange();
                if (!board.inBounds(row, col) || seen[row * cols + col]) {
                    continue;
                }
                Tile tile = board.getTile(row, col);
                boolean target = row == toRow && col == toCol;
                if (target || tile.isWalkable() || tile.isClearable()) {
                    seen[row * cols + col] = true;
                    cameFrom[row * cols + col] = here;
                    queue.add(new int[] {row, col});
                }
            }
        }
        List<int[]> onTheSpot = new ArrayList<>();
        onTheSpot.add(new int[] {toRow, toCol});
        return onTheSpot;
    }
}
