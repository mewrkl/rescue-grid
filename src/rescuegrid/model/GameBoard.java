package rescuegrid.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rescuegrid.model.rescuers.Rescuer;
import rescuegrid.model.tiles.Tile;

/**
 * The whole building: the grid of tiles, plus the rescuers and survivors in it.
 *
 * GameBoard is an example of composition: it HAS a grid, it HAS rescuers, it
 * HAS survivors. It works with any Tile without knowing which kind it is.
 */
public class GameBoard {

    private final Tile[][] grid;
    private final ArrayList<Rescuer> rescuers;
    private final ArrayList<Survivor> survivors;
    private final boolean[][] visible;

    public GameBoard(int rows, int cols) {
        this.grid = new Tile[rows][cols];
        this.rescuers = new ArrayList<>();
        this.survivors = new ArrayList<>();
        this.visible = new boolean[rows][cols];
    }

    public int getRows() {
        return grid.length;
    }

    public int getCols() {
        return grid[0].length;
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < getRows() && col >= 0 && col < getCols();
    }

    /** The tile at (row, col), or null if that position is off the map. */
    public Tile getTile(int row, int col) {
        if (!inBounds(row, col)) {
            return null;
        }
        return grid[row][col];
    }

    /**
     * Swaps the tile at (row, col) for a new tile object. This is how fire
     * spreads, fire burns out, rubble is cleared and cracked floor collapses.
     * The new tile keeps the old one's "explored" flag, so the fog doesn't
     * come back over a tile just because it changed.
     */
    public void replaceTile(int row, int col, Tile newTile) {
        Tile oldTile = grid[row][col];
        if (oldTile != null && oldTile.isExplored()) {
            newTile.markExplored();
        }
        grid[row][col] = newTile;
    }

    public void addRescuer(Rescuer rescuer) {
        rescuers.add(rescuer);
    }

    public void addSurvivor(Survivor survivor) {
        survivors.add(survivor);
    }

    /**
     * Read-only view of the rescuers. Other classes can loop over it, but they
     * can't add or remove rescuers behind the board's back (encapsulation).
     */
    public List<Rescuer> getRescuers() {
        return Collections.unmodifiableList(rescuers);
    }

    public List<Survivor> getSurvivors() {
        return Collections.unmodifiableList(survivors);
    }

    public int getRescuedCount() {
        int count = 0;
        for (Survivor survivor : survivors) {
            if (survivor.isRescued()) {
                count++;
            }
        }
        return count;
    }

    /** The active (not downed) rescuer standing at (row, col), or null. */
    public Rescuer getRescuerAt(int row, int col) {
        for (Rescuer rescuer : rescuers) {
            if (!rescuer.isDowned() && rescuer.getRow() == row && rescuer.getCol() == col) {
                return rescuer;
            }
        }
        return null;
    }

    /** The survivor lying at (row, col) waiting for help, or null. */
    public Survivor getSurvivorAt(int row, int col) {
        for (Survivor survivor : survivors) {
            if (survivor.isWaiting() && survivor.getRow() == row && survivor.getCol() == col) {
                return survivor;
            }
        }
        return null;
    }

    // ----- What the active rescuer can see right now (fog of war) -----

    public boolean isVisible(int row, int col) {
        return inBounds(row, col) && visible[row][col];
    }

    public void setVisible(int row, int col) {
        visible[row][col] = true;
    }

    public void clearVisible() {
        for (boolean[] rowOfFlags : visible) {
            Arrays.fill(rowOfFlags, false);
        }
    }
}
