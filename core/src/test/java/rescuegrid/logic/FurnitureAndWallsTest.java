package rescuegrid.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rescuegrid.model.Direction;
import rescuegrid.model.GameBoard;
import rescuegrid.model.rescuers.Rescuer;
import rescuegrid.model.tiles.DecoratedFloor;
import rescuegrid.model.tiles.Prop;
import rescuegrid.model.tiles.Tile;
import rescuegrid.model.tiles.Wall;

/**
 * The angled-view sprites (decision 0016): a wall picks its pieces from its
 * neighbours, furniture blocks walking but not sight or scent, and things lying
 * on the floor behave like floor.
 */
class FurnitureAndWallsTest {

    private final LevelLoader loader = new LevelLoader();
    private final MovementCalculator movement = new MovementCalculator();

    @Test
    void aWallShowsItsFrontAboveFloorAndItsTopAboveMoreWall() {
        GameBoard board = loader.parse(new String[] {
            "#######",
            "#P.#.X#",
            "#..#..#",
            "#.....#",
            "#######",
        });
        Wall stub = (Wall) board.getTile(2, 3);           // the end of a wall sticking down into the room
        assertArrayEquals(new int[] {Wall.FACE, Wall.FACE_END_LEFT, Wall.FACE_END_RIGHT}, stub.getPieces(board));
        Wall above = (Wall) board.getTile(1, 3);          // wall below it, floor left and right
        assertArrayEquals(new int[] {Wall.TOP, Wall.EDGE_LEFT, Wall.EDGE_RIGHT}, above.getPieces(board));
        Wall outer = (Wall) board.getTile(0, 1);          // outer wall above the room: off the map counts as wall
        assertArrayEquals(new int[] {Wall.FACE}, outer.getPieces(board));
        assertTrue(outer.isWall());
        assertFalse(board.getTile(3, 3).isWall());
    }

    @Test
    void furnitureBlocksTheWayButNotSightOrScent() throws InvalidMoveException {
        GameBoard board = loader.parse(new String[] {
            "########",
            "#P.k.m.#",
            "#......#",
            "#X.....#",
            "########",
        });
        Tile desk = board.getTile(1, 3);
        assertEquals(Prop.Kind.DESK, ((Prop) desk).getKind());
        assertFalse(desk.isWalkable());
        assertTrue(desk.isSeeThrough());
        assertFalse(desk.isFlammable());
        assertFalse(desk.canCollapse());

        Rescuer rescuer = board.getActiveRescuer();
        rescuer.moveTo(1, 2);
        assertFalse(movement.canStep(board, rescuer, Direction.RIGHT), "can't walk through the desk");
        movement.computeSight(board, rescuer);
        assertTrue(board.isVisible(1, 4), "but you can see over it");
        assertEquals(1, movement.findByScent(board, rescuer, 3).size(), "and the dog smells over it");
    }

    @Test
    void thingsOnTheFloorAreStillFloor() {
        GameBoard board = loader.parse(new String[] {
            "######",
            "#P,:X#",
            "#_~..#",
            "######",
        });
        DecoratedFloor papers = (DecoratedFloor) board.getTile(1, 2);
        assertEquals(DecoratedFloor.Decoration.PAPERS, papers.getDecoration());
        assertEquals(DecoratedFloor.Decoration.GLASS, ((DecoratedFloor) board.getTile(1, 3)).getDecoration());
        assertEquals(DecoratedFloor.Decoration.RUG, ((DecoratedFloor) board.getTile(2, 1)).getDecoration());
        assertEquals(DecoratedFloor.Decoration.PUDDLE, ((DecoratedFloor) board.getTile(2, 2)).getDecoration());
        assertTrue(papers.isWalkable());
        assertTrue(papers.isFlammable());
        assertTrue(papers.canCollapse());
    }

    @Test
    void everyFurnitureLetterIsKnownAndOthersAreStillMistakes() {
        for (Prop.Kind kind : Prop.Kind.values()) {
            assertEquals(kind, Prop.Kind.forSymbol(kind.getSymbol()));
            assertNull(DecoratedFloor.Decoration.forSymbol(kind.getSymbol()), "a letter means one thing");
        }
        assertNull(Prop.Kind.forSymbol('#'));
        assertThrows(IllegalArgumentException.class, () -> loader.parse(new String[] {
            "#####",
            "#PZX#",
            "#####",
        }));
    }
}
