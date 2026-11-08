package rescuegrid.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import rescuegrid.model.Direction;
import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;
import rescuegrid.model.tiles.Fire;
import rescuegrid.model.tiles.Rubble;
import rescuegrid.model.tiles.Tile;

/**
 * Makes the building dangerous: fire damage, fire spreading and burning out,
 * and aftershocks that turn floor into rubble.
 *
 * Uses one Random with a fixed seed per level, so the same level plays out the
 * same way each time. That makes bugs reproducible.
 *
 * IMPORTANT: when changing tiles, we first COLLECT the changes in a list, then
 * APPLY them. If we changed tiles while still looping over the grid, a tile that
 * just caught fire could spread again in the same tick.
 */
public class HazardManager {

    public static final double SPREAD_CHANCE = 0.25; // tune in testing
    public static final int RANDOM_COLLAPSES = 3;    // tune in testing

    private final Random random;

    public HazardManager(long seed) {
        this.random = new Random(seed);
    }

    /** Step 1 of the tick: every active rescuer next to fire loses 1 health. */
    public void applyFireDamage(GameBoard board, List<GameEvent> events) {
        for (Rescuer rescuer : board.getRescuers()) {
            if (!rescuer.isDowned() && isNextToFire(board, rescuer.getRow(), rescuer.getCol())) {
                hurt(rescuer, 1, rescuer.getName() + " is getting burned! Move away from the fire", events);
            }
        }
    }

    /** Step 2: each fire tries to spread to each flammable neighbour (SPREAD_CHANCE each). */
    public void spreadFire(GameBoard board, List<GameEvent> events) {
        // 1. COLLECT the tiles that will catch fire.
        List<Tile> catching = new ArrayList<>();
        boolean[][] alreadyChosen = new boolean[board.getRows()][board.getCols()];
        for (Tile tile : allTiles(board)) {
            if (!tile.isBurning()) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                int row = tile.getRow() + direction.getRowChange();
                int col = tile.getCol() + direction.getColChange();
                Tile neighbour = board.getTile(row, col);
                if (neighbour == null || !neighbour.isFlammable() || alreadyChosen[row][col]
                        || board.getRescuerAt(row, col) != null) {
                    continue;
                }
                if (random.nextDouble() < SPREAD_CHANCE) {
                    alreadyChosen[row][col] = true;
                    catching.add(neighbour);
                }
            }
        }
        // 2. APPLY the changes.
        for (Tile tile : catching) {
            board.replaceTile(tile.getRow(), tile.getCol(), new Fire(tile.getRow(), tile.getCol()));
            Survivor survivor = board.getSurvivorAt(tile.getRow(), tile.getCol());
            if (survivor != null) {
                survivor.markLost();
                events.add(new GameEvent(GameEvent.Type.SURVIVOR_LOST, "A survivor was caught by the fire"));
            }
        }
        if (!catching.isEmpty()) {
            events.add(new GameEvent(GameEvent.Type.FIRE_SPREAD, ""));
        }
    }

    /** Step 3: call onWorldTick on every tile. Only Fire reacts (it burns out). */
    public void burnOutFires(GameBoard board) {
        for (Tile tile : allTiles(board)) {
            tile.onWorldTick(board); // polymorphism: most tiles do nothing
        }
    }

    /** Step 5: the aftershock (plan section 4.5). */
    public void triggerAftershock(GameBoard board, List<GameEvent> events) {
        // 1. Every tile hears about the aftershock. Only CrackedFloor reacts (it becomes Rubble).
        for (Tile tile : allTiles(board)) {
            tile.onAftershock(board);
        }

        // 2. A few random floor tiles collapse too.
        List<Tile> candidates = new ArrayList<>();
        for (Tile tile : allTiles(board)) {
            int row = tile.getRow();
            int col = tile.getCol();
            if (tile.canCollapse() && board.getRescuerAt(row, col) == null
                    && board.getSurvivorAt(row, col) == null && !isNextToExit(board, row, col)) {
                candidates.add(tile);
            }
        }
        Collections.shuffle(candidates, random);
        for (int i = 0; i < RANDOM_COLLAPSES && i < candidates.size(); i++) {
            Tile tile = candidates.get(i);
            board.replaceTile(tile.getRow(), tile.getCol(), new Rubble(tile.getRow(), tile.getCol()));
        }

        // 3. Anyone standing on a tile that just collapsed is pushed to a free neighbour.
        pushPeopleOffRubble(board, events);

        // 4. Everyone still standing takes 1 damage.
        for (Rescuer rescuer : board.getRescuers()) {
            if (!rescuer.isDowned()) {
                hurt(rescuer, 1, "", events);
            }
        }
        events.add(new GameEvent(GameEvent.Type.AFTERSHOCK,
                "AFTERSHOCK! Cracked floors collapsed. Find another way, or clear the rubble"));
    }

    // ----- Helpers -----

    /** Damages a rescuer. If that downs them, they drop whoever they were carrying. */
    private void hurt(Rescuer rescuer, int amount, String message, List<GameEvent> events) {
        rescuer.takeDamage(amount);
        if (rescuer.isDowned()) {
            rescuer.dropSurvivor();
            events.add(new GameEvent(GameEvent.Type.RESCUER_DOWNED, rescuer.getName() + " is down!"));
        } else {
            events.add(new GameEvent(GameEvent.Type.RESCUER_HURT, message));
        }
    }

    private void pushPeopleOffRubble(GameBoard board, List<GameEvent> events) {
        for (Rescuer rescuer : board.getRescuers()) {
            if (rescuer.isDowned() || board.getTile(rescuer.getRow(), rescuer.getCol()).isWalkable()) {
                continue;
            }
            Tile free = findFreeNeighbour(board, rescuer.getRow(), rescuer.getCol());
            if (free != null) {
                rescuer.moveTo(free);
            } else {
                hurt(rescuer, 1, rescuer.getName() + " was caught in the collapse", events);
            }
        }
        for (Survivor survivor : board.getSurvivors()) {
            if (!survivor.isWaiting() || board.getTile(survivor.getRow(), survivor.getCol()).isWalkable()) {
                continue;
            }
            Tile free = findFreeNeighbour(board, survivor.getRow(), survivor.getCol());
            if (free != null) {
                survivor.moveTo(free.getRow(), free.getCol());
            } else {
                survivor.markLost();
                events.add(new GameEvent(GameEvent.Type.SURVIVOR_LOST, "A survivor was buried by the collapse"));
            }
        }
    }

    private Tile findFreeNeighbour(GameBoard board, int row, int col) {
        for (Direction direction : Direction.values()) {
            int r = row + direction.getRowChange();
            int c = col + direction.getColChange();
            Tile tile = board.getTile(r, c);
            if (tile != null && tile.isWalkable() && board.getRescuerAt(r, c) == null) {
                return tile;
            }
        }
        return null;
    }

    private boolean isNextToFire(GameBoard board, int row, int col) {
        for (Direction direction : Direction.values()) {
            Tile tile = board.getTile(row + direction.getRowChange(), col + direction.getColChange());
            if (tile != null && tile.isBurning()) {
                return true;
            }
        }
        return false;
    }

    private boolean isNextToExit(GameBoard board, int row, int col) {
        for (Direction direction : Direction.values()) {
            Tile tile = board.getTile(row + direction.getRowChange(), col + direction.getColChange());
            if (tile != null && tile.isExit()) {
                return true;
            }
        }
        return false;
    }

    /** Every tile, collected into a list first so we can safely replace tiles afterwards. */
    private List<Tile> allTiles(GameBoard board) {
        List<Tile> tiles = new ArrayList<>();
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                tiles.add(board.getTile(row, col));
            }
        }
        return tiles;
    }
}
