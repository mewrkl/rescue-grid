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
import rescuegrid.model.survivors.CriticalSurvivor;
import rescuegrid.model.survivors.MinorSurvivor;
import rescuegrid.model.survivors.SeriousSurvivor;
import rescuegrid.model.tiles.CrackedFloor;
import rescuegrid.model.tiles.Exit;
import rescuegrid.model.tiles.Fire;
import rescuegrid.model.tiles.Floor;
import rescuegrid.model.tiles.Rubble;
import rescuegrid.model.tiles.Wall;

/**
 * Reads a level text file (readLines) and turns it into a GameBoard (parse).
 * Survivors who are always there: 'm' minor, 'S' serious, '!' critical,
 * 'T' serious and trapped under debris. '?' is a possible spot: the level's
 * random survivors (their types come from LevelData) go to shuffled '?' spots.
 * This is the only switch on tile and survivor types in the game.
 */
public class LevelLoader {

    /** Loads the map file named in the level's settings; random picks which '?' spots get a survivor. */
    public GameBoard load(LevelData level, Random random) {
        return parse(readLines(level.getFileName()), level.getSurvivorCondition(),
                level.getRandomSurvivorTypes(), random);
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

    /** Builds a board with default survivor time and only the fixed survivors. Used by tests. */
    public GameBoard parse(String[] rows) {
        return parse(rows, Survivor.DEFAULT_CONDITION, "", new Random(0));
    }

    /**
     * Builds a board from rows of characters. randomTypes has one letter per random
     * survivor ('m', 's' or '!'). Throws a clear error if the map is broken.
     */
    public GameBoard parse(String[] rows, int survivorCondition, String randomTypes, Random random) {
        checkShape(rows);
        checkCount(rows, 'X', "exit (X)");
        checkCount(rows, 'P', "team start (P)");

        GameBoard board = new GameBoard(rows.length, rows[0].length());
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length(); col++) {
                placeTile(board, rows[row].charAt(col), row, col, survivorCondition);
            }
        }
        List<int[]> spots = pickSurvivorSpots(rows, randomTypes.length(), random);
        for (int i = 0; i < spots.size(); i++) {
            int[] spot = spots.get(i);
            board.addSurvivor(createSurvivor(randomTypes.charAt(i), spot[0], spot[1], survivorCondition));
        }
        addRescuers(board, rows);
        return board;
    }

    /** Chooses which '?' spots get a survivor: shuffle all the spots, then take as many as needed. */
    private List<int[]> pickSurvivorSpots(String[] rows, int needed, Random random) {
        List<int[]> spots = new ArrayList<>();
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length(); col++) {
                if (rows[row].charAt(col) == '?') {
                    spots.add(new int[] {row, col});
                }
            }
        }
        if (needed > spots.size()) {
            throw new IllegalArgumentException("Level needs " + needed + " random survivors, but has only "
                    + spots.size() + " possible spots (?)");
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
            case 'S': // a survivor who is always here
            case 'm':
            case '!':
                board.replaceTile(row, col, new Floor(row, col));
                board.addSurvivor(createSurvivor(symbol == 'S' ? 's' : symbol, row, col, survivorCondition));
                break;
            case 'T': // a serious survivor trapped under debris
                board.replaceTile(row, col, new Floor(row, col));
                Survivor trapped = createSurvivor('s', row, col, survivorCondition);
                trapped.setTrapped(true);
                board.addSurvivor(trapped);
                break;
            default:
                throw new IllegalArgumentException("Unknown symbol '" + symbol
                        + "' at row " + row + ", col " + col);
        }
    }

    /** Creates the right Survivor subclass for a type letter: 'm' minor, 's' serious, '!' critical. */
    private Survivor createSurvivor(char type, int row, int col, int survivorCondition) {
        switch (type) {
            case 'm':
                return new MinorSurvivor(row, col, survivorCondition);
            case 's':
                return new SeriousSurvivor(row, col, survivorCondition);
            case '!':
                return new CriticalSurvivor(row, col, survivorCondition);
            default:
                throw new IllegalArgumentException("Unknown survivor type '" + type + "' (use m, s or !)");
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
