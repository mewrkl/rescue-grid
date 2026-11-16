package rescuegrid.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import rescuegrid.model.GameBoard;
import rescuegrid.model.RescueTeam;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;
import rescuegrid.model.survivors.CriticalSurvivor;
import rescuegrid.model.tiles.Prop;

/**
 * Checks every level is fair for EVERY possible pick of random survivor spots.
 * For each pick it plans the simplest route a player could take (nearest trips
 * first) and checks the target is rescued before each survivor runs out of time.
 * Every way the level's random survivor types can land on the spots is tried.
 * To be safe it assumes every cracked floor has already collapsed into rubble,
 * and fire never burns out.
 */
class LevelBalanceTest {

    private static final double RUBBLE_SECONDS = RescueTeam.GEAR_CHANGE_SECONDS * 2 + 3.0; // swap, clear, swap back
    private static final double SAFETY = 0.8; // plans must fit in 80% of the time
    private static final double FIRE_FREE_SECONDS = 40;

    /** Training is calm until the timed part (see Tutorial), so it has its own check below. */
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void everyPickOfSurvivorSpotsCanReachTheTargetInTime(int levelNumber) {
        LevelData level = LevelData.forLevel(levelNumber);
        String[] rows = new LevelLoader().readLines(level.getFileName());
        int[] start = find(rows, 'P');
        double[][] walk = walkingSeconds(rows, start);

        List<int[]> fixed = new ArrayList<>();
        List<int[]> spots = new ArrayList<>();
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < rows[r].length(); c++) {
                char ch = rows[r].charAt(c);
                if (ch == 'S' || ch == 'T' || ch == 'm' || ch == '!') {
                    fixed.add(new int[] {r, c, ch});
                } else if (ch == '?') {
                    spots.add(new int[] {r, c, 'S'});
                }
            }
        }
        int needed = level.getRandomSurvivorTypes().length();
        List<List<int[]>> picks = new ArrayList<>();
        choose(spots, needed, 0, new ArrayList<>(), picks);

        List<String> typeOrders = new ArrayList<>();
        orderings("", level.getRandomSurvivorTypes().replace('s', 'S'), typeOrders);

        double worst = 0;
        for (List<int[]> pick : picks) {
            for (String types : typeOrders) { // every way the random types can land on the spots
                List<int[]> survivors = new ArrayList<>(fixed);
                for (int i = 0; i < pick.size(); i++) {
                    survivors.add(new int[] {pick.get(i)[0], pick.get(i)[1], types.charAt(i)});
                }
                double used = planTime(survivors, walk, level);
                worst = Math.max(worst, used);
                assertTrue(used <= SAFETY, "level " + levelNumber + ": a pick needs " + Math.round(used * 100)
                        + "% of a survivor's time");
            }
        }
        System.out.println("Level " + levelNumber + ": " + picks.size() + " picks, worst uses "
                + Math.round(worst * 100) + "% of the time available");
    }

    /**
     * Training's timed part starts next to the critical survivor: change to the Dog
     * Handler, walk down the hall to the hidden survivor's room (the rubble on the
     * way costs a change to the Engineer and the clearing), wait out the aftershock,
     * and carry them to the exit. A wasted sniff and its cooldown are allowed for.
     */
    @Test
    void theTrainingTimedPartFitsInTime() {
        LevelData level = LevelData.forLevel(LevelData.TUTORIAL);
        String[] rows = new LevelLoader().readLines(level.getFileName());
        int[] hidden = find(rows, 'S');
        double there = walkingSeconds(rows, new int[] {4, 27})[hidden[0]][hidden[1]];
        double back = walkingSeconds(rows, find(rows, 'X'))[hidden[0]][hidden[1]]
                / Rescuer.STEP_DELAY * Rescuer.CARRY_STEP_DELAY;
        double wastedSniff = 8.0; // the Dog Handler's cooldown
        double total = RescueTeam.GEAR_CHANGE_SECONDS + wastedSniff + there + TurnManager.WARNING_SECONDS + back;
        double life = level.getSurvivorCondition() * TurnManager.TICK_SECONDS; // serious: 2 points a tick
        System.out.println("Training timed part: " + Math.round(total) + " s of " + Math.round(life) + " s");
        assertTrue(total <= SAFETY * life, "the timed part needs " + Math.round(total) + " s of " + life);
    }

    /**
     * Survivors are in the way (you walk into them, not over them), so none of them
     * may cut a level in two: with every possible survivor spot filled, the player
     * must still reach every other tile, and stand next to every survivor.
     */
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void survivorsNeverBlockTheWay(int levelNumber) {
        String[] rows = new LevelLoader().readLines(LevelData.forLevel(levelNumber).getFileName());
        int[] start = find(rows, 'P');
        boolean[][] open = reachable(rows, start, false);
        boolean[][] blocked = reachable(rows, start, true);
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < rows[r].length(); c++) {
                if (isSurvivorSpot(rows[r].charAt(c))) {
                    assertTrue(blocked[r - 1][c] || blocked[r + 1][c] || blocked[r][c - 1] || blocked[r][c + 1],
                            "level " + levelNumber + ": nobody can get next to the survivor at (" + r + "," + c + ")");
                } else {
                    assertEquals(open[r][c], blocked[r][c],
                            "level " + levelNumber + ": survivors cut off (" + r + "," + c + ")");
                }
            }
        }
    }

    /** Walls, fire, furniture (Prop letters) and the training dummy can't be walked through. */
    private static boolean isBlocked(char ch) {
        return ch == '#' || ch == 'F' || ch == 'd' || Prop.Kind.forSymbol(ch) != null;
    }

    private static boolean isSurvivorSpot(char ch) {
        return ch == '?' || ch == 'S' || ch == 'T' || ch == 'm' || ch == '!';
    }

    /** Tiles the player can reach (through rubble and training doors), optionally treating survivors as solid. */
    private static boolean[][] reachable(String[] rows, int[] start, boolean survivorsBlock) {
        boolean[][] seen = new boolean[rows.length][rows[0].length()];
        List<int[]> todo = new ArrayList<>();
        todo.add(start);
        seen[start[0]][start[1]] = true;
        int[][] moves = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!todo.isEmpty()) {
            int[] at = todo.remove(todo.size() - 1);
            for (int[] m : moves) {
                int r = at[0] + m[0];
                int c = at[1] + m[1];
                char ch = rows[r].charAt(c);
                if (seen[r][c] || isBlocked(ch) || (survivorsBlock && isSurvivorSpot(ch))) {
                    continue;
                }
                seen[r][c] = true;
                todo.add(new int[] {r, c});
            }
        }
        return seen;
    }

    /**
     * Fire must not reach any survivor spot early. Until the first aftershock
     * warning the fire is the same in every play (the level's seed), so one run
     * with nobody moving shows exactly when fire reaches each spot.
     */
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void fireReachesNoSurvivorSpotInTheFirstFortySeconds(int levelNumber) {
        LevelData level = LevelData.forLevel(levelNumber);
        LevelLoader loader = new LevelLoader();
        String[] rows = loader.readLines(level.getFileName());
        GameBoard board = loader.load(level);
        TurnManager turns = new TurnManager(board, new HazardManager(level), level);
        StringBuilder report = new StringBuilder();
        while (turns.getElapsedSeconds() < FIRE_FREE_SECONDS) {
            turns.update(1.0 / 60);
            turns.takeEvents();
            for (int r = 0; r < rows.length; r++) {
                for (int c = 0; c < rows[r].length(); c++) {
                    if ("Sm!T?".indexOf(rows[r].charAt(c)) >= 0 && board.getTile(r, c).isBurning()
                            && report.indexOf("(" + r + "," + c + ")") < 0) {
                        report.append(" (").append(r).append(',').append(c).append(") at ")
                                .append(Math.round(turns.getElapsedSeconds())).append(" s");
                    }
                }
            }
        }
        assertTrue(report.length() == 0, "level " + levelNumber + ": fire reaches" + report);
    }

    /**
     * Tries every order of rescuing `target` survivors (the way a good player would
     * pick) and returns the smallest "worst share of a survivor's life used" found.
     * A critical survivor is done when backup arrives, but the player walks back
     * to the exit area meanwhile instead of waiting.
     */
    private static double planTime(List<int[]> survivors, double[][] walk, LevelData level) {
        List<double[]> trips = new ArrayList<>(); // {walk there, kind, life seconds}
        for (int[] s : survivors) {
            double there = walk[s[0]][s[1]];
            assertTrue(there < Double.MAX_VALUE, "survivor at (" + s[0] + "," + s[1] + ") can't be reached");
            double ticks = level.getSurvivorCondition();
            double loss = s[2] == '!' ? 3 : s[2] == 'm' ? 1 : 2;
            double life = ticks * Survivor.POINTS_PER_TICK / loss * TurnManager.TICK_SECONDS;
            trips.add(new double[] {there + (s[2] == 'T' ? RUBBLE_SECONDS : 0), s[2], life});
        }
        return bestOrder(trips, new boolean[trips.size()], level.getTarget(), 0, 0);
    }

    private static double bestOrder(List<double[]> trips, boolean[] used, int left, double clock, double worst) {
        if (left == 0) {
            return worst;
        }
        double best = Double.MAX_VALUE;
        for (int i = 0; i < trips.size(); i++) {
            if (used[i]) {
                continue;
            }
            double[] trip = trips.get(i);
            double there = trip[0];
            double done;
            double after;
            if (trip[1] == '!') {
                done = clock + there + RescueTeam.GEAR_CHANGE_SECONDS + CriticalSurvivor.BACKUP_SECONDS;
                after = clock + there * 2 + RescueTeam.GEAR_CHANGE_SECONDS;
            } else if (trip[1] == 'm') {
                done = clock + there * 2;
                after = done;
            } else {
                done = clock + there + there / Rescuer.STEP_DELAY * Rescuer.CARRY_STEP_DELAY;
                after = done;
            }
            used[i] = true;
            best = Math.min(best, bestOrder(trips, used, left - 1, after, Math.max(worst, done / trip[2])));
            used[i] = false;
        }
        return best;
    }

    /** Seconds of walking from the start to every tile (rubble and cracked floor cost extra). */
    private static double[][] walkingSeconds(String[] rows, int[] start) {
        double[][] best = new double[rows.length][rows[0].length()];
        for (double[] row : best) {
            Arrays.fill(row, Double.MAX_VALUE);
        }
        PriorityQueue<double[]> queue = new PriorityQueue<>((a, b) -> Double.compare(a[0], b[0]));
        best[start[0]][start[1]] = 0;
        queue.add(new double[] {0, start[0], start[1]});
        int[][] moves = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            double[] item = queue.poll();
            int r = (int) item[1];
            int c = (int) item[2];
            if (item[0] > best[r][c]) {
                continue;
            }
            for (int[] m : moves) {
                int nr = r + m[0];
                int nc = c + m[1];
                char ch = rows[nr].charAt(nc);
                if (isBlocked(ch)) {
                    continue;
                }
                double cost = Rescuer.STEP_DELAY + (ch == 'R' || ch == 'C' ? RUBBLE_SECONDS : 0);
                if (item[0] + cost < best[nr][nc]) {
                    best[nr][nc] = item[0] + cost;
                    queue.add(new double[] {best[nr][nc], nr, nc});
                }
            }
        }
        return best;
    }

    /** Every distinct ordering of the letters in `rest`, e.g. "mS" gives "mS" and "Sm". */
    private static void orderings(String sofar, String rest, List<String> out) {
        if (rest.isEmpty()) {
            if (!out.contains(sofar)) {
                out.add(sofar);
            }
            return;
        }
        for (int i = 0; i < rest.length(); i++) {
            orderings(sofar + rest.charAt(i), rest.substring(0, i) + rest.substring(i + 1), out);
        }
    }

    private static void choose(List<int[]> spots, int count, int from, List<int[]> chosen, List<List<int[]>> out) {
        if (chosen.size() == count) {
            out.add(new ArrayList<>(chosen));
            return;
        }
        for (int i = from; i < spots.size(); i++) {
            chosen.add(spots.get(i));
            choose(spots, count, i + 1, chosen, out);
            chosen.remove(chosen.size() - 1);
        }
    }

    private static int[] find(String[] rows, char symbol) {
        for (int r = 0; r < rows.length; r++) {
            int c = rows[r].indexOf(symbol);
            if (c >= 0) {
                return new int[] {r, c};
            }
        }
        throw new IllegalArgumentException("no " + symbol);
    }
}
