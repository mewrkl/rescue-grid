package rescuegrid.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.DogHandler;
import rescuegrid.model.rescuers.Engineer;
import rescuegrid.model.rescuers.Medic;
import rescuegrid.model.tiles.CrackedFloor;
import rescuegrid.model.tiles.Exit;
import rescuegrid.model.tiles.Fire;
import rescuegrid.model.tiles.Floor;
import rescuegrid.model.tiles.Rubble;
import rescuegrid.model.tiles.Wall;

/**
 * Reads a level text file (readLines) and turns it into a GameBoard (parse).
 * 'S' is a survivor who is always there; '?' is a possible spot, and enough of
 * them are picked at random to reach the level's survivor count.
 * This is the only switch on tile types in the game.
 */
public class LevelLoader {

    /** Loads the map file named in the level's settings; random picks which '?' spots get a survivor. */
    public GameBoard load(LevelData level, Random random) {
        return parse(readLines(level.getFileName()), level.getSurvivorCondition(),
                level.getSurvivorCount(), random);
    }

    /** Same, but always the same survivor spots (from the level's seed). Used by the tests. */
    public GameBoard load(LevelData level) {
        return load(level, new Random(level.getSeed()));
    }

    /** Reads a text file from the resources folder, skipping blank lines. */
    public String[] readLines(String resourcePath) {
        InputStream input = LevelLoader.class.getResourceAsStream(resourcePath);
        if (input == null) {
            throw new IllegalArgumentException("Level file not found: " + resourcePath
                    + " (is the assets folder on the classpath? Run the game with Gradle: lwjgl3 > run)");
        }
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            while (line != null) {
                line = line.stripTrailing();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read level file " + resourcePath, e);
        }
        return lines.toArray(new String[0]);
    }

    /** Builds a board with default survivors and only the fixed 'S' ones. Used by tests. */
    public GameBoard parse(String[] rows) {
        return parse(rows, Survivor.DEFAULT_CONDITION, countSymbol(rows, 'S'), new Random(0));
    }

    /** Builds a board from rows of characters. Throws a clear error if the map is broken. */
    public GameBoard parse(String[] rows, int survivorCondition, int survivorCount, Random random) {
        checkShape(rows);
        checkCount(rows, 'X', "exit (X)");
        checkCount(rows, 'P', "team start (P)");

        GameBoard board = new GameBoard(rows.length, rows[0].length());
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length(); col++) {
                placeTile(board, rows[row].charAt(col), row, col, survivorCondition);
            }
        }
        for (int[] spot : pickSurvivorSpots(rows, survivorCount, random)) {
            board.addSurvivor(new Survivor(spot[0], spot[1], survivorCondition));
        }
        addRescuers(board, rows);
        return board;
    }

    /**
     * Chooses which '?' spots get a survivor: shuffle all the spots, then take
     * as many from the front as we still need after the fixed 'S' survivors.
     */
    private List<int[]> pickSurvivorSpots(String[] rows, int survivorCount, Random random) {
        List<int[]> spots = new ArrayList<>();
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length(); col++) {
                if (rows[row].charAt(col) == '?') {
                    spots.add(new int[] {row, col});
                }
            }
        }
        int needed = survivorCount - countSymbol(rows, 'S');
        if (needed < 0 || needed > spots.size()) {
            throw new IllegalArgumentException("Level needs " + survivorCount + " survivors, but has "
                    + countSymbol(rows, 'S') + " fixed (S) and " + spots.size() + " possible spots (?)");
        }
        Collections.shuffle(spots, random);
        return spots.subList(0, needed);
    }

    /** Creates the right Tile object for one character. */
    private void placeTile(GameBoard board, char symbol, int row, int col, int survivorCondition) {
        switch (symbol) {
            case '#':
                board.replaceTile(row, col, new Wall(row, col));
                break;
            case '.':
            case '?': // a possible survivor spot; pickSurvivorSpots() decides if someone is here
            case 'P': // where the team starts, next to the exit
                board.replaceTile(row, col, new Floor(row, col));
                break;
            case 'C':
                board.replaceTile(row, col, new CrackedFloor(row, col));
                break;
            case 'R':
                board.replaceTile(row, col, new Rubble(row, col));
                break;
            case 'F':
                board.replaceTile(row, col, new Fire(row, col));
                break;
            case 'X':
                board.replaceTile(row, col, new Exit(row, col));
                break;
            case 'S':
                board.replaceTile(row, col, new Floor(row, col));
                board.addSurvivor(new Survivor(row, col, survivorCondition));
                break;
            default:
                throw new IllegalArgumentException("Unknown symbol '" + symbol
                        + "' at row " + row + ", col " + col);
        }
    }

    /**
     * Adds the three roles at the start in a fixed order (Medic, Engineer, Dog Handler)
     * so keys 1/2/3 always match. The Medic starts on the map.
     */
    private void addRescuers(GameBoard board, String[] rows) {
        int[] start = findSymbol(rows, 'P');
        board.addRescuer(new Medic(start[0], start[1]));
        board.addRescuer(new Engineer(start[0], start[1]));
        board.addRescuer(new DogHandler(start[0], start[1]));
    }

    /** Every row the same length, and the outer border all walls. */
    private void checkShape(String[] rows) {
        if (rows.length == 0) {
            throw new IllegalArgumentException("Level is empty");
        }
        int width = rows[0].length();
        for (int row = 0; row < rows.length; row++) {
            if (rows[row].length() != width) {
                throw new IllegalArgumentException("Row " + row + " is " + rows[row].length()
                        + " characters long, but row 0 is " + width);
            }
        }
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < width; col++) {
                boolean onBorder = row == 0 || row == rows.length - 1 || col == 0 || col == width - 1;
                if (onBorder && rows[row].charAt(col) != '#') {
                    throw new IllegalArgumentException("The outer border must be all walls (#), "
                            + "but row " + row + ", col " + col + " is '" + rows[row].charAt(col) + "'");
                }
            }
        }
    }

    /** The symbol must appear exactly once. */
    private void checkCount(String[] rows, char symbol, String description) {
        int count = countSymbol(rows, symbol);
        if (count != 1) {
            throw new IllegalArgumentException("Level needs exactly one " + description
                    + ", but found " + count);
        }
    }

    /** How many times the symbol appears in the whole map. */
    private int countSymbol(String[] rows, char symbol) {
        int count = 0;
        for (String row : rows) {
            for (int col = 0; col < row.length(); col++) {
                if (row.charAt(col) == symbol) {
                    count++;
                }
            }
        }
        return count;
    }

    /** Returns {row, col} of the first place the symbol appears. */
    private int[] findSymbol(String[] rows, char symbol) {
        for (int row = 0; row < rows.length; row++) {
            int col = rows[row].indexOf(symbol);
            if (col >= 0) {
                return new int[] {row, col};
            }
        }
        throw new IllegalArgumentException("Symbol '" + symbol + "' not found");
    }
}
