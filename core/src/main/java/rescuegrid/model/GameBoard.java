package rescuegrid.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rescuegrid.model.rescuers.Rescuer;
import rescuegrid.model.tiles.Tile;

/** The building: a grid of tiles plus the rescue team and the survivors in it. */
public class GameBoard {

    private final Tile[][] grid;
    private final RescueTeam team;
    private final ArrayList<Survivor> survivors;
    private final boolean[][] visible;

    public GameBoard(int rows, int cols) {
        this.grid = new Tile[rows][cols];
        this.team = new RescueTeam();
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
     * Replaces a tile (fire spreading, rubble cleared, floor collapsing).
     * The new tile keeps the old one's explored flag so the fog doesn't return.
     */
    public void replaceTile(int row, int col, Tile newTile) {
        Tile oldTile = grid[row][col];
        if (oldTile != null && oldTile.isExplored()) {
            newTile.markExplored();
        }
        grid[row][col] = newTile;
    }

    /** Adds a role to the team. The first one added is the one on the map. */
    public void addRescuer(Rescuer rescuer) {
        team.add(rescuer);
    }

    public RescueTeam getTeam() {
        return team;
    }

    /** The rescuer on the map right now. */
    public Rescuer getActiveRescuer() {
        return team.getActive();
    }

    public void addSurvivor(Survivor survivor) {
        survivors.add(survivor);
    }

    /**
     * Read-only view of all three roles (only one is on the map). Other classes
     * can loop over it, but can't add or remove rescuers behind the board's back.
     */
    public List<Rescuer> getRescuers() {
        return team.getMembers();
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

    public int getLostCount() {
        int count = 0;
        for (Survivor survivor : survivors) {
            if (survivor.isLost()) {
                count++;
            }
        }
        return count;
    }

    /** Survivors not yet rescued and not lost: still waiting, or being carried out. */
    public int getStillInsideCount() {
        return survivors.size() - getRescuedCount() - getLostCount();
    }

    /** The rescuer on the map, if they are standing at (row, col); otherwise null. */
    public Rescuer getRescuerAt(int row, int col) {
        Rescuer active = team.getActive();
        if (!active.isDowned() && active.getRow() == row && active.getCol() == col) {
            return active;
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
