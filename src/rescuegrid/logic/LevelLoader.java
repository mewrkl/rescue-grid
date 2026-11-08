package rescuegrid.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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
 * Turns a level text file into a GameBoard, one character per tile.
 *
 * Two steps, kept separate on purpose:
 *   1. readLines(): file -> String[]
 *   2. parse():     String[] -> GameBoard
 * So you can also build a quick test board straight from a String[] in code.
 *
 * This is the ONLY place in the game that uses a switch on the tile type.
 */
public class LevelLoader {

    /** Loads the map file named in the level's settings. */
    public GameBoard load(LevelData level) {
        return parse(readLines(level.getFileName()));
    }

    /** Reads a text file from the resources folder, skipping blank lines. */
    public String[] readLines(String resourcePath) {
        InputStream input = LevelLoader.class.getResourceAsStream(resourcePath);
        if (input == null) {
            throw new IllegalArgumentException("Level file not found: " + resourcePath
                    + " (is the resources folder marked as a Resources Root?)");
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

    /** Builds a board from rows of characters. Throws a clear error if the map is broken. */
    public GameBoard parse(String[] rows) {
        checkShape(rows);
        checkCount(rows, 'X', "exit (X)");
        checkCount(rows, 'M', "Medic start (M)");
        checkCount(rows, 'E', "Engineer start (E)");
        checkCount(rows, 'D', "Dog Handler start (D)");

        GameBoard board = new GameBoard(rows.length, rows[0].length());
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length(); col++) {
                placeTile(board, rows[row].charAt(col), row, col);
            }
        }
        addRescuers(board, rows);
        return board;
    }

    /** Creates the right Tile object for one character. */
    private void placeTile(GameBoard board, char symbol, int row, int col) {
        switch (symbol) {
            case '#':
                board.replaceTile(row, col, new Wall(row, col));
                break;
            case '.':
            case 'M':
            case 'E':
            case 'D':
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
                board.addSurvivor(new Survivor(row, col));
                break;
            default:
                throw new IllegalArgumentException("Unknown symbol '" + symbol
                        + "' at row " + row + ", col " + col);
        }
    }

    /** Adds the rescuers in a fixed order (Medic, Engineer, Dog Handler) so keys 1/2/3 always match. */
    private void addRescuers(GameBoard board, String[] rows) {
        int[] medic = findSymbol(rows, 'M');
        int[] engineer = findSymbol(rows, 'E');
        int[] dogHandler = findSymbol(rows, 'D');
        board.addRescuer(new Medic(medic[0], medic[1]));
        board.addRescuer(new Engineer(engineer[0], engineer[1]));
        board.addRescuer(new DogHandler(dogHandler[0], dogHandler[1]));
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
        int count = 0;
        for (String row : rows) {
            for (int col = 0; col < row.length(); col++) {
                if (row.charAt(col) == symbol) {
                    count++;
                }
            }
        }
        if (count != 1) {
            throw new IllegalArgumentException("Level needs exactly one " + description
                    + ", but found " + count);
        }
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
