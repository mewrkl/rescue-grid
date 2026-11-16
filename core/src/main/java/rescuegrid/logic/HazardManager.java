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
 * Fire damage, fire spreading and burning out, and aftershocks.
 * Uses one seeded Random per level so a level plays out the same way each time.
 * Tile changes are collected first and applied afterwards, so a tile that just
 * caught fire can't spread again in the same tick.
 */
public class HazardManager {

    public static final int RANDOM_COLLAPSES = 3;

    private final Random random;
    private final double spreadChance;
    private final List<Tile> plannedCollapses;

    /** Seed and fire spread chance come from the level (LevelData). */
    public HazardManager(long seed, double spreadChance) {
        this.random = new Random(seed);
        this.spreadChance = spreadChance;
        this.plannedCollapses = new ArrayList<>();
    }

    /** Uses the level's own seed and fire spread chance. */
    public HazardManager(LevelData level) {
        this(level.getSeed(), level.getFireSpreadChance());
    }

    /** Step 1 of the tick: the rescuer on the map loses 1 health if they stand next to fire. */
    public void applyFireDamage(GameBoard board, List<GameEvent> events) {
        Rescuer rescuer = board.getActiveRescuer();
        if (!rescuer.isDowned() && isNextToFire(board, rescuer.getRow(), rescuer.getCol())) {
            hurt(rescuer, 1, rescuer.getName() + " is burning", events);
        }
    }

    /** Step 2: each fire tries to spread to each flammable neighbour (spreadChance each). */
    public void spreadFire(GameBoard board, List<GameEvent> events) {
        // 1. Collect the tiles that will catch fire.
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
                if (random.nextDouble() < spreadChance) {
                    alreadyChosen[row][col] = true;
                    catching.add(neighbour);
                }
            }
        }
        // 2. Apply the changes.
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
            tile.onWorldTick(board); // most tiles do nothing
        }
    }

    /**
     * Chooses the random floor tiles the next aftershock will collapse. Called at
     * the warning, so the game can show the player which tiles are about to fall.
     */
    public void planCollapses(GameBoard board) {
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
        plannedCollapses.clear();
        for (int i = 0; i < RANDOM_COLLAPSES && i < candidates.size(); i++) {
            plannedCollapses.add(candidates.get(i));
        }
    }

    /**
     * Training: like planCollapses, but only floor within `radius` steps of (row, col),
     * so the player sees the orange tiles and learns to step off them.
     */
    public void planCollapsesNear(GameBoard board, int row, int col, int radius) {
        planCollapses(board);
        List<Tile> near = new ArrayList<>();
        for (Tile tile : allTiles(board)) {
            int distance = Math.abs(tile.getRow() - row) + Math.abs(tile.getCol() - col);
            if (distance <= radius && tile.canCollapse() && board.getRescuerAt(tile.getRow(), tile.getCol()) == null
                    && board.getSurvivorAt(tile.getRow(), tile.getCol()) == null
                    && !isNextToExit(board, tile.getRow(), tile.getCol())
                    && !isPassage(board, tile.getRow(), tile.getCol())) {
                near.add(tile);
            }
        }
        Collections.shuffle(near, random);
        plannedCollapses.clear();
        for (int i = 0; i < RANDOM_COLLAPSES && i < near.size(); i++) {
            plannedCollapses.add(near.get(i));
        }
    }

    /** A one-tile-wide doorway or corridor: blocked on both sides (left and right, or above and below). */
    private boolean isPassage(GameBoard board, int row, int col) {
        return (isBlocked(board, row, col - 1) && isBlocked(board, row, col + 1))
                || (isBlocked(board, row - 1, col) && isBlocked(board, row + 1, col));
    }

    private boolean isBlocked(GameBoard board, int row, int col) {
        Tile tile = board.getTile(row, col);
        return tile == null || !tile.isWalkable();
    }

    /** True if the next aftershock will collapse the tile at (row, col). */
    public boolean willCollapse(GameBoard board, int row, int col) {
        Tile tile = board.getTile(row, col);
        return tile != null && (tile.collapsesInAftershock() || isStillPlanned(board, tile));
    }

    /** A planned tile only falls if nothing replaced it meanwhile (fire, for example). */
    private boolean isStillPlanned(GameBoard board, Tile tile) {
        return plannedCollapses.contains(tile) && board.getTile(tile.getRow(), tile.getCol()) == tile;
    }

    /** Step 5: the aftershock. */
    public void triggerAftershock(GameBoard board, List<GameEvent> events) {
        if (plannedCollapses.isEmpty()) {
            planCollapses(board);
        }

        // 1. Every tile hears about the aftershock. Only CrackedFloor reacts (it becomes Rubble).
        for (Tile tile : allTiles(board)) {
            tile.onAftershock(board);
        }

        // 2. The planned floor tiles collapse too, unless a survivor has been put down there.
        for (Tile tile : plannedCollapses) {
            int row = tile.getRow();
            int col = tile.getCol();
            if (isStillPlanned(board, tile) && board.getSurvivorAt(row, col) == null) {
                board.replaceTile(row, col, new Rubble(row, col));
            }
        }
        plannedCollapses.clear();

        // 3. Anyone standing on a tile that just collapsed is pushed to a free neighbour.
        pushPeopleOffRubble(board, events);

        // 4. The rescuer on the map takes 1 damage.
        Rescuer rescuer = board.getActiveRescuer();
        if (!rescuer.isDowned()) {
            hurt(rescuer, 1, "", events);
        }
        events.add(new GameEvent(GameEvent.Type.AFTERSHOCK, ""));
    }

    /** Damages a rescuer. If that downs them, TurnManager sends in the next role. */
    private void hurt(Rescuer rescuer, int amount, String message, List<GameEvent> events) {
        rescuer.takeDamage(amount);
        if (rescuer.isDowned()) {
            events.add(new GameEvent(GameEvent.Type.RESCUER_DOWNED, rescuer.getName() + " is down"));
        } else {
            events.add(new GameEvent(GameEvent.Type.RESCUER_HURT, message));
        }
    }

    private void pushPeopleOffRubble(GameBoard board, List<GameEvent> events) {
        Rescuer rescuer = board.getActiveRescuer();
        if (!rescuer.isDowned() && !board.getTile(rescuer.getRow(), rescuer.getCol()).isWalkable()) {
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
