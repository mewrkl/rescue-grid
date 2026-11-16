import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * Makes every sprite in assets/images, in our own "dusty disaster" pixel art
 * (decision 0016). Not part of the game: run it from the project folder with
 *
 *     java tools/SpriteMaker.java
 *
 * Tiles are 16 x 16 pixels, drawn at 4x in the game. Characters are 16 x 24, so
 * their head reaches into the tile above (the angled RPG view). Everything is
 * built from shapes, then people and props get a 1 px outline automatically, so
 * every pose stays consistent. Fixed random seeds: the same PNGs every run.
 *
 * Sheet layouts (the game cuts them up in AssetManager.getFrame):
 *   floor 4 variants | cracked_floor 1 | burnt_floor 2 | rubble 2 | fire 4 frames
 *   wall: top, face, edge N, edge E, edge W, face end W, face end E, shadow N, shadow W
 *   exit, barrier, dummy 1 | props 26 (13 intact, then the same 13 damaged) | decor 4
 *   medic, engineer, dog_handler, survivor_minor: rows UP, DOWN, LEFT, RIGHT;
 *     columns stand, step A, step B (survivor_minor has two more: waving out, waving up)
 *   survivor_serious, survivor_critical: 2 frames | emote: "!" bubble x 2, radio bubble x 2
 *   paramedic: like medic | stretcher: across, across+person, lengthways, lengthways+person
 *   flare: 2 frames | puff: 4 frames, 16 x 24 (changing gear)
 *   dog: rows UP, DOWN, LEFT, RIGHT x 2 frames
 * It also writes tools/sprite_preview.png: every sheet at 4x, to check them in one look.
 */
public class SpriteMaker {

    static final int T = 16;   // tile size
    static final int CH = 24;  // character height

    enum Dir { UP, DOWN, LEFT, RIGHT } // the game's Direction order

    // ----- Palette: dusty disaster. Muted building, strong colours only on people and hazards -----
    static final int CLEAR = 0;
    static final int FLOOR = hex(0xB5AA98);
    static final int FLOOR_LIGHT = hex(0xC4BAA8);
    static final int GROUT = hex(0x968B7A);
    static final int DUST = hex(0xA59A88);
    static final int DUST_DARK = hex(0x877C6C);
    static final int PEBBLE = hex(0x72685C);
    static final int WALL_TOP = hex(0x5A5157);        // the cap: clearly lighter than the dark of the fog
    static final int WALL_TOP_LIGHT = hex(0x766C70);
    static final int WALL_TOP_DARK = hex(0x463F45);
    static final int WALL_EDGE = hex(0x141117);
    static final int BRICK = hex(0x7C6F67);
    static final int BRICK_LIGHT = hex(0x8E8279);
    static final int BRICK_DARK = hex(0x695D56);
    static final int MORTAR = hex(0x564D49);
    static final int BASEBOARD = hex(0x3B3430);
    static final int CRACK = hex(0x3F3731);
    static final int CRACK_LIP = hex(0xD2C8B6);
    static final int BURNT = hex(0x3A3330);
    static final int BURNT_DARK = hex(0x2A2421);
    static final int SOOT = hex(0x1C1816);
    static final int EMBER = hex(0x9A4424);
    static final int ROCK = hex(0x85827E);
    static final int ROCK_LIGHT = hex(0xADAAA4);
    static final int ROCK_DARK = hex(0x5E5A56);
    static final int ROCK_EDGE = hex(0x2E2A28);
    static final int REBAR = hex(0x5E4A3C);
    static final int FLAME_WHITE = hex(0xFFF4C7);
    static final int FLAME_YELLOW = hex(0xFDD34D);
    static final int FLAME_ORANGE = hex(0xF97316);
    static final int FLAME_RED = hex(0xD8301F);
    static final int FLAME_DARK = hex(0x7A1E14);
    static final int EXIT_GREEN = hex(0x22C55E);
    static final int EXIT_DARK = hex(0x14532D);
    static final int EXIT_MID = hex(0x16803C);
    static final int EXIT_LIGHT = hex(0x86EFAC);
    static final int WHITE = hex(0xF4F2EC);
    static final int TAPE = hex(0xFACC15);
    static final int TAPE_DARK = hex(0x221C18);
    static final int OUTLINE = hex(0x211A1C);
    static final int SKIN = hex(0xE6B188);
    static final int SKIN_SHADE = hex(0xC48A64);
    static final int HAIR = hex(0x5A3A22);
    static final int HAIR_DARK = hex(0x3B2515);
    static final int GROUND_SHADOW = argb(0x000000, 70);
    static final int WOOD = hex(0x8A5A3B);
    static final int WOOD_LIGHT = hex(0xA8734C);
    static final int WOOD_DARK = hex(0x5F3D27);
    static final int METAL = hex(0x8D959E);
    static final int METAL_LIGHT = hex(0xB6BDC5);
    static final int METAL_DARK = hex(0x5E656E);
    static final int SHEET = hex(0xE4E0D6);
    static final int SHEET_SHADE = hex(0xC2BDB1);
    static final int TEAL = hex(0x447370);
    static final int TEAL_LIGHT = hex(0x5C918C);
    static final int TEAL_DARK = hex(0x2F514F);
    static final int LEAF = hex(0x4E7C3A);
    static final int LEAF_LIGHT = hex(0x6E9F51);
    static final int LEAF_DARK = hex(0x37592A);
    static final int POT = hex(0x9C5B3A);
    static final int SCREEN = hex(0x1C2836);
    static final int SCREEN_GLOW = hex(0x5A86AE);
    static final int WATER = hex(0x7EB6D8);
    static final int WATER_LIGHT = hex(0xB5DAEE);
    static final int CROSS_RED = hex(0xDC2626);
    static final int BOOK_RED = hex(0x9B3B35);
    static final int BOOK_BLUE = hex(0x3E5F8A);
    static final int BOOK_GREEN = hex(0x4F7A4A);
    static final int BOOK_YELLOW = hex(0xC29A3A);
    static final int RUG = hex(0x8A4B42);
    static final int RUG_DARK = hex(0x6A3732);
    static final int RUG_LIGHT = hex(0xB87A5E);
    static final int GLASS = hex(0xCFE8F2);
    static final int GLASS_EDGE = hex(0x8FB6C6);
    static final int PAPER = hex(0xEEE9DC);
    static final int PAPER_LINE = hex(0xA9A396);
    static final int SHADOW_DARK = argb(0x140E14, 110);

    static final List<String> PREVIEW_NAMES = new ArrayList<>();
    static final List<Img[][]> PREVIEW_SHEETS = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        File images = new File("assets/images");
        images.mkdirs();

        // ----- The building -----
        save(images, "floor", row(floor(0), floor(1), floor(2), floor(3)));
        save(images, "cracked_floor", row(crackedFloor()));
        save(images, "burnt_floor", row(burntFloor(0), burntFloor(1)));
        save(images, "wall", row(wallTop(), wallFace(), wallEdge(Dir.UP), wallEdge(Dir.RIGHT), wallEdge(Dir.LEFT),
                wallFaceEnd(Dir.LEFT), wallFaceEnd(Dir.RIGHT), shadow(Dir.UP), shadow(Dir.LEFT)));
        save(images, "rubble", row(rubble(0), rubble(1)));
        save(images, "fire", row(fire(0), fire(1), fire(2), fire(3)));
        save(images, "exit", row(exit()));
        save(images, "barrier", row(barrier()));
        save(images, "dummy", row(dummy()));
        Img[] props = new Img[26];
        for (int damaged = 0; damaged < 2; damaged++) { // 13 intact, then the same 13 damaged
            boolean d = damaged == 1;
            Img[] set = {sofa(d), table(d), bookshelf(d), plant(d), bed(d), desk(d), filingCabinet(d),
                waterCooler(d), hospitalBed(d), medicineCabinet(d), wheelchair(d), ivStand(d), locker(d)};
            System.arraycopy(set, 0, props, damaged * 13, 13);
        }
        save(images, "props", row(props));
        save(images, "decor", row(papers(), glass(), rug(), puddle()));

        // ----- People -----
        // One rescuer, one face; each role's gear gives it its own outline (decision 0019).
        save(images, "medic", walkSheet(new Look(WHITE, hex(0x34405A), Hat.CAP, CROSS_RED, Badge.CROSS,
                CROSS_RED, false, SKIN).with(Gear.MEDIC_PACK)));
        save(images, "engineer", walkSheet(new Look(hex(0xF97316), hex(0x3F4A5A), Hat.HARD_HAT, hex(0xFACC15),
                Badge.STRIPES, hex(0xFDE68A), false, SKIN).with(Gear.ENGINEER_KIT)));
        save(images, "dog_handler", walkSheet(new Look(hex(0x2563EB), hex(0x5C5040), Hat.CAP, hex(0x1E3A8A),
                Badge.PATCH, hex(0xFACC15), false, SKIN).with(Gear.K9_VEST)));
        // The backup stretcher team: paramedic green (not orange: that is the Engineer) and white helmets.
        save(images, "paramedic", walkSheet(new Look(hex(0x2F7D57), hex(0x22553D), Hat.HARD_HAT, WHITE,
                Badge.NONE, 0, false, SKIN).with(Gear.PARAMEDIC)));
        save(images, "stretcher", row(stretcher(0), stretcher(1), stretcher(2), stretcher(3)));
        save(images, "flare", row(flare(0), flare(1)));
        save(images, "puff", row(puff(0), puff(1), puff(2), puff(3)));
        save(images, "survivor_minor", walkSheet(new Look(hex(0x4ADE80), hex(0x3B5B8C), Hat.BANDAGE, WHITE,
                Badge.NONE, 0, true, SKIN)));
        Look serious = new Look(hex(0xFACC15), hex(0x3B5B8C), Hat.NONE, 0, Badge.NONE, 0, true, SKIN);
        save(images, "survivor_serious", row(sitting(serious, 0), sitting(serious, 1)));
        Look critical = new Look(hex(0xEF4444), hex(0x4A4A52), Hat.NONE, 0, Badge.NONE, 0, true, SKIN_PALE);
        save(images, "survivor_critical", row(lying(critical, 0), lying(critical, 1)));
        save(images, "emote", row(emote(0), emote(1), radioEmote(0), radioEmote(1)));
        Img[][] dog = new Img[4][2];
        for (Dir dir : Dir.values()) {
            for (int frame = 0; frame < 2; frame++) {
                dog[dir.ordinal()][frame] = dir == Dir.LEFT ? dog(Dir.RIGHT, frame).mirror() : dog(dir, frame);
            }
        }
        save(images, "dog", dog);
        save(images, "carry_icon", row(carryIcon()));

        writePreview(new File("tools/sprite_preview.png"));
        System.out.println("Done. Sprites written to assets/images, preview to tools/sprite_preview.png.");
    }

    // =====================================================================
    // Tiles
    // =====================================================================

    /** Dusty ceramic floor: four 8 px tiles with grout, a light top-left edge, specks of dust. */
    static Img floor(int variant) {
        Img img = new Img(T, T);
        img.fill(FLOOR);
        for (int oy = 0; oy < T; oy += 8) {
            for (int ox = 0; ox < T; ox += 8) {
                img.rect(ox, oy, 7, 1, FLOOR_LIGHT);
                img.rect(ox, oy, 1, 7, FLOOR_LIGHT);
            }
        }
        for (int i = 0; i < T; i++) {
            img.set(7, i, GROUT);
            img.set(15, i, GROUT);
            img.set(i, 7, GROUT);
            img.set(i, 15, GROUT);
        }
        Random random = new Random(100 + variant);
        for (int k = 0; k < 7; k++) {
            img.set(1 + random.nextInt(14), 1 + random.nextInt(14), random.nextBoolean() ? DUST : DUST_DARK);
        }
        if (variant == 1) { // a few pebbles of fallen plaster
            img.set(10, 4, PEBBLE);
            img.set(11, 4, DUST_DARK);
            img.set(4, 11, PEBBLE);
        }
        if (variant == 2) { // one dusty tile
            img.rect(8, 8, 7, 7, DUST);
            img.rect(8, 8, 7, 1, FLOOR);
            img.set(11, 11, DUST_DARK);
        }
        if (variant == 3) { // a chipped corner
            img.set(6, 6, PEBBLE);
            img.set(5, 6, DUST_DARK);
            img.set(6, 5, DUST_DARK);
        }
        return img;
    }

    /** Floor with deep dark cracks and a light lip: it collapses in the next aftershock. */
    static Img crackedFloor() {
        Img img = floor(0);
        int[][] main = {{0, 5}, {4, 7}, {7, 5}, {10, 9}, {15, 11}};
        int[][] branchA = {{7, 5}, {9, 0}};
        int[][] branchB = {{10, 9}, {9, 15}};
        int[][] branchC = {{4, 7}, {2, 12}};
        for (int[][] path : new int[][][] {main, branchA, branchB, branchC}) {
            for (int i = 0; i + 1 < path.length; i++) {
                crackLine(img, path[i][0], path[i][1], path[i + 1][0], path[i + 1][1], path == main);
            }
        }
        return img;
    }

    static void crackLine(Img img, int x0, int y0, int x1, int y1, boolean wide) {
        for (int[] p : line(x0, y0, x1, y1)) {
            if (img.get(p[0], p[1] + 1) != CRACK) {
                img.set(p[0], p[1] + 1, CRACK_LIP);
            }
            img.set(p[0], p[1], CRACK);
            if (wide) {
                img.set(p[0] + 1, p[1], CRACK);
            }
        }
    }

    /** Charcoal floor with soot patches where a fire burned out. */
    static Img burntFloor(int variant) {
        Img img = new Img(T, T);
        img.fill(BURNT);
        for (int i = 0; i < T; i++) {
            img.set(7, i, BURNT_DARK);
            img.set(15, i, BURNT_DARK);
            img.set(i, 7, BURNT_DARK);
            img.set(i, 15, BURNT_DARK);
        }
        Random random = new Random(200 + variant);
        for (int k = 0; k < 5; k++) {
            int cx = 2 + random.nextInt(12);
            int cy = 2 + random.nextInt(12);
            blob(img, cx, cy, 1 + random.nextInt(2), SOOT, random);
        }
        img.set(3 + variant * 6, 10, EMBER);
        return img;
    }

    /**
     * The top of a wall seen from above: a cap of stone slabs (8 x 4, staggered)
     * with a light top edge and dark joints, like the wall tops in Stardew Valley
     * or Pokemon interiors. Lighter than the fog, so a wall never reads as a hole.
     */
    static Img wallTop() {
        Img img = new Img(T, T);
        img.fill(WALL_TOP);
        Random random = new Random(300);
        for (int course = 0; course < 4; course++) {
            int top = course * 4;
            img.rect(0, top, T, 1, WALL_TOP_LIGHT);          // each slab's lit top edge
            img.rect(0, top + 3, T, 1, WALL_TOP_DARK);       // the joint below it
            int offset = course % 2 == 0 ? 0 : 4;
            for (int x = offset; x < T; x += 8) {
                img.rect(x, top, 1, 4, WALL_TOP_DARK);       // the joint between slabs
                if (random.nextInt(3) == 0) {
                    img.set(x + 2 + random.nextInt(4), top + 1 + random.nextInt(2), WALL_TOP_LIGHT);
                }
            }
        }
        return img;
    }

    /** The front of a wall: the top cap, then three courses of bricks and a baseboard. */
    static Img wallFace() {
        Img img = new Img(T, T);
        img.rect(0, 0, T, 3, WALL_TOP);
        img.rect(0, 2, T, 1, WALL_TOP_LIGHT);
        img.rect(0, 3, T, 1, WALL_EDGE);
        Random random = new Random(301);
        for (int course = 0; course < 3; course++) {
            int top = 4 + course * 4;
            img.rect(0, top, T, 3, BRICK);
            img.rect(0, top + 3, T, 1, MORTAR);
            int offset = course % 2 == 0 ? 3 : 7;
            for (int x = offset; x < T + 8; x += 8) {
                int start = x - 8;
                int tint = random.nextInt(3);
                int fillColor = tint == 0 ? BRICK_DARK : BRICK;
                img.rect(start + 1, top, 7, 3, fillColor);
                img.rect(start + 1, top, 7, 1, tint == 0 ? BRICK : BRICK_LIGHT);
                img.rect(x, top, 1, 3, MORTAR);
            }
        }
        // a crack running down through the bricks
        int[][] crack = {{10, 5}, {9, 8}, {11, 11}, {10, 14}};
        for (int i = 0; i + 1 < crack.length; i++) {
            for (int[] p : line(crack[i][0], crack[i][1], crack[i + 1][0], crack[i + 1][1])) {
                img.set(p[0], p[1], MORTAR);
            }
        }
        img.rect(0, 15, T, 1, BASEBOARD);
        return img;
    }

    /** A dark edge drawn on a wall top where the next tile in that direction is not wall. */
    static Img wallEdge(Dir side) {
        Img img = new Img(T, T);
        if (side == Dir.UP) {
            img.rect(0, 0, T, 1, WALL_EDGE);
            img.rect(0, 1, T, 1, BRICK_LIGHT);                // a bright lip where the cap ends
        } else if (side == Dir.RIGHT) {
            img.rect(15, 0, 1, T, WALL_EDGE);
            img.rect(14, 0, 1, T, WALL_TOP_DARK);
        } else {
            img.rect(0, 0, 1, T, WALL_EDGE);
            img.rect(1, 0, 1, T, BRICK_LIGHT);
        }
        return img;
    }

    /** The end of a run of wall front: a dark outline and some shade. */
    static Img wallFaceEnd(Dir side) {
        Img img = new Img(T, T);
        int edge = side == Dir.LEFT ? 0 : 15;
        int shade = side == Dir.LEFT ? 1 : 14;
        img.rect(edge, 0, 1, T, WALL_EDGE);
        for (int y = 4; y < 15; y++) {
            img.set(shade, y, argb(0x1A1418, 80));
        }
        return img;
    }

    /** The shadow a wall casts on the floor below it (UP) or to its right (LEFT means wall on the left). */
    static Img shadow(Dir wallSide) {
        Img img = new Img(T, T);
        int[] alphas = {110, 70, 35};
        for (int i = 0; i < alphas.length; i++) {
            if (wallSide == Dir.UP) {
                img.rect(0, i, T, 1, argb(0x140E14, alphas[i]));
            } else {
                img.rect(i, 0, 1, T, argb(0x140E14, alphas[i] * 2 / 3));
            }
        }
        return img;
    }

    /**
     * A heap of broken concrete: one mound with an outline, the chunks in it shown
     * by light top-left and dark bottom-right edges. Variant 1 has a bent steel bar.
     */
    static Img rubble(int variant) {
        Img img = floor(variant == 0 ? 1 : 3);
        Random random = new Random(400 + variant);
        groundShadow(img, 1, 14, 14);
        groundShadow(img, 2, 15, 12);
        Img heap = new Img(T, T);
        for (int x = 1; x < 15; x++) {
            double fromMiddle = Math.abs(x - 7.5) / 7.5;
            int top = (int) Math.round(2 + Math.pow(fromMiddle, 1.5) * 7) + random.nextInt(2);
            heap.rect(x, top, 1, 14 - top, ROCK);
        }
        int[][] chunks = variant == 0
                ? new int[][] {{5, 3, 4}, {2, 7, 4}, {7, 6, 5}, {11, 8, 3}, {4, 10, 4}, {9, 11, 4}}
                : new int[][] {{6, 2, 4}, {3, 6, 3}, {8, 6, 4}, {1, 10, 4}, {6, 10, 4}, {11, 10, 3}};
        for (int[] c : chunks) {
            int x = c[0];
            int y = c[1];
            int w = c[2];
            paintIfSolid(heap, x, y, w, 1, ROCK_LIGHT);          // lit top
            paintIfSolid(heap, x, y + 1, 1, 1, ROCK_LIGHT);      // lit left
            paintIfSolid(heap, x, y + 2, w, 1, ROCK_DARK);       // shaded bottom
            paintIfSolid(heap, x + w, y, 1, 3, ROCK_EDGE);       // gap to the next chunk
        }
        heap.outline(ROCK_EDGE);
        img.draw(heap, 0, 0);
        if (variant == 1) { // a bent steel bar sticking out of the heap
            for (int[] p : line(10, 5, 13, 1)) {
                img.set(p[0], p[1], REBAR);
            }
            img.set(14, 1, REBAR);
        }
        int[][] bits = {{0, 13}, {15, 12}, {1, 2}, {14, 3}};
        for (int[] bit : bits) {
            img.set(bit[0], bit[1], ROCK_DARK);
        }
        return img;
    }

    static void paintIfSolid(Img img, int x, int y, int width, int height, int color) {
        for (int yy = y; yy < y + height; yy++) {
            for (int xx = x; xx < x + width; xx++) {
                if (((img.get(xx, yy) >>> 24) & 0xFF) == 255) {
                    img.set(xx, yy, color);
                }
            }
        }
    }

    /** Flames on burnt floor; the four frames move the tongues so the fire flickers. */
    static Img fire(int frame) {
        Img img = burntFloor(0);
        Random random = new Random(500 + frame);
        for (int x = 1; x < 15; x++) {
            double center = 1 - Math.abs(x - 7.5) / 7.0;
            double height = 3 + center * 9 + 1.8 * Math.sin(x * 1.3 + frame * Math.PI / 2) + random.nextInt(3) - 1;
            int top = (int) Math.round(14 - height);
            for (int y = Math.max(0, top); y <= 14; y++) {
                double t = (14 - y) / Math.max(1.0, height);
                int color;
                if (t > 0.8) {
                    color = FLAME_RED;
                } else if (t > 0.5) {
                    color = FLAME_ORANGE;
                } else if (center > 0.55 && t < 0.35) {
                    color = FLAME_WHITE;
                } else {
                    color = center > 0.35 ? FLAME_YELLOW : FLAME_ORANGE;
                }
                img.set(x, y, color);
            }
        }
        img.rect(1, 15, 14, 1, FLAME_DARK);
        img.set(3 + frame * 3, 1 + frame % 2, FLAME_YELLOW); // sparks
        img.set(12 - frame * 2, 2 + (frame + 1) % 2, FLAME_ORANGE);
        return img;
    }

    /** A lit emergency exit: a green EXIT sign over a doorway with an arrow. */
    static Img exit() {
        Img img = floor(0);
        img.rect(1, 0, 15, 7, EXIT_GREEN);
        img.rect(1, 6, 15, 1, EXIT_MID);
        letters(img, 2, 1, WHITE, "E", "X", "I", "T");
        img.rect(3, 7, 10, 9, EXIT_DARK);
        img.rect(2, 7, 1, 9, EXIT_MID);
        img.rect(13, 7, 1, 9, EXIT_MID);
        img.rect(3, 13, 10, 3, EXIT_MID);
        img.rect(4, 15, 8, 1, EXIT_GREEN);
        // arrow pointing out
        img.rect(7, 9, 2, 5, EXIT_LIGHT);
        img.rect(6, 10, 4, 1, EXIT_LIGHT);
        img.rect(5, 11, 1, 1, EXIT_LIGHT);
        img.rect(10, 11, 1, 1, EXIT_LIGHT);
        img.set(7, 8, EXIT_LIGHT);
        img.set(8, 8, EXIT_LIGHT);
        return img;
    }

    /** Training doors: a doorway crossed by two bands of caution tape. */
    static Img barrier() {
        Img img = floor(2);
        img.rect(0, 0, T, 2, argb(0x140E14, 90));
        tapeBand(img, 3, 10);
        tapeBand(img, 11, 5);
        return img;
    }

    static void tapeBand(Img img, int leftY, int rightY) {
        List<int[]> points = line(0, leftY, 15, rightY);
        for (int i = 0; i < points.size(); i++) {
            int[] p = points.get(i);
            int color = (p[0] / 2) % 2 == 0 ? TAPE : TAPE_DARK;
            img.set(p[0], p[1], color);
            img.set(p[0], p[1] + 1, color);
            img.set(p[0], p[1] + 2, argb(0x000000, 60));
        }
    }

    /** A first-aid dummy: a tan mannequin on a stand with a red cross on its chest. */
    static Img dummy() {
        Img img = floor(0);
        Img figure = new Img(T, T);
        int body = hex(0xD9C197);
        int bodyShade = hex(0xB89E74);
        figure.rect(5, 14, 6, 1, WOOD_DARK);
        figure.rect(7, 11, 2, 3, WOOD);
        figure.rect(5, 6, 6, 5, body);
        figure.rect(10, 6, 1, 5, bodyShade);
        figure.rect(4, 6, 1, 3, body);
        figure.rect(11, 6, 1, 3, bodyShade);
        figure.rect(6, 1, 4, 4, body);
        figure.rect(9, 1, 1, 4, bodyShade);
        figure.rect(7, 5, 2, 1, bodyShade);
        figure.rect(7, 7, 2, 3, CROSS_RED);
        figure.rect(6, 8, 4, 1, CROSS_RED);
        figure.outline(OUTLINE);
        groundShadow(img, 4, 15, 8);
        img.draw(figure, 0, 0);
        return img;
    }

    // =====================================================================
    // Props (they block the way like walls; drawn on floor, seen from the angled view)
    // Each has an intact and a damaged look (the earthquake). Shading is hue-shifted:
    // shadows lean blue-violet, lights lean warm, outlines are a dark tone of the colour.
    // =====================================================================

    static Img prop(Img thing, int shadowX, int shadowWidth) {
        Img img = new Img(T, T);
        thing.outlineTinted();
        groundShadow(img, shadowX, 15, shadowWidth);
        img.draw(thing, 0, 0);
        return img;
    }

    /** A box seen from the angled view: a lit top face, a darker front face, the right side darkest. */
    static void box(Img s, int x, int y, int w, int top, int front, int color) {
        s.rect(x, y, w, top, color);
        s.rect(x, y, w, 1, light(color));
        s.rect(x, y + top, w, front, shade(color));
        s.rect(x + w - 1, y, 1, top, shade(color));
        s.rect(x + w - 1, y + top, 1, front, shade(shade(color)));
    }

    /** A few broken bits of concrete that fell from the ceiling. */
    static void debris(Img s, int x, int y) {
        s.rect(x, y, 3, 2, ROCK_LIGHT);
        s.rect(x, y + 2, 3, 1, ROCK_DARK);
        s.set(x + 3, y + 1, ROCK);
        s.set(x + 4, y + 2, ROCK_DARK);
    }

    static Img sofa(boolean broken) {
        Img s = new Img(T, T);
        box(s, 1, 3, 14, 2, 4, TEAL);          // backrest
        box(s, 1, 9, 14, 2, 3, TEAL_LIGHT);    // seat
        s.rect(7, 9, 1, 2, TEAL);              // two cushions
        box(s, 1, 6, 2, 2, 6, TEAL);           // arms
        box(s, 13, 6, 2, 2, 6, TEAL);
        s.set(2, 14, WOOD_DARK);
        s.set(13, 14, WOOD_DARK);
        if (broken) {
            s.rect(9, 9, 3, 2, WHITE);          // torn cushion, stuffing out
            s.set(10, 11, WHITE);
            debris(s, 3, 8);
        }
        return prop(s, 1, 14);
    }

    static Img table(boolean broken) {
        Img s = new Img(T, T);
        if (!broken) {
            box(s, 1, 5, 14, 5, 2, WOOD_LIGHT);
            s.rect(2, 12, 1, 3, shade(WOOD));
            s.rect(13, 12, 1, 3, shade(shade(WOOD)));
            s.rect(4, 6, 3, 2, WHITE);          // a plate
            s.set(10, 6, WHITE);                // a cup
            s.set(10, 7, SHEET_SHADE);
        } else {                                // a leg gave way: the right half sags
            box(s, 1, 5, 7, 5, 2, WOOD_LIGHT);
            box(s, 8, 7, 7, 5, 2, WOOD_LIGHT);
            s.rect(8, 7, 1, 7, WOOD_DARK);      // the crack
            s.rect(2, 12, 1, 3, shade(WOOD));
            s.set(4, 6, WHITE);                 // broken plate
            s.set(11, 14, WHITE);
            s.set(13, 15, SHEET_SHADE);
        }
        return prop(s, 1, 14);
    }

    static Img bookshelf(boolean broken) {
        Img s = new Img(T, T);
        int[] colors = {BOOK_RED, BOOK_BLUE, BOOK_GREEN, BOOK_YELLOW, BOOK_BLUE, BOOK_RED};
        if (!broken) {
            box(s, 2, 0, 12, 2, 13, WOOD);
            s.rect(3, 2, 10, 12, WOOD_DARK);
            for (int shelf = 0; shelf < 3; shelf++) {
                int top = 2 + shelf * 4;
                s.rect(3, top + 3, 10, 1, WOOD);
                for (int b = 0; b < 5; b++) {
                    int color = colors[(b + shelf) % colors.length];
                    s.rect(3 + b * 2, top + (b % 2), 2, 3 - (b % 2), color);
                    s.set(3 + b * 2, top + (b % 2), light(color));
                }
            }
        } else {                                // knocked over onto its front, books everywhere
            box(s, 0, 6, 16, 4, 3, WOOD);
            s.rect(1, 7, 14, 1, shade(WOOD));
            int[][] spilled = {{1, 3}, {5, 2}, {10, 4}, {13, 2}, {3, 14}, {8, 14}, {12, 13}};
            for (int i = 0; i < spilled.length; i++) {
                s.rect(spilled[i][0], spilled[i][1], 2, 1, colors[i % colors.length]);
                s.set(spilled[i][0], spilled[i][1] + 1, shade(colors[i % colors.length]));
            }
        }
        return prop(s, broken ? 0 : 2, broken ? 16 : 12);
    }

    static Img plant(boolean broken) {
        Img s = new Img(T, T);
        Random random = new Random(600);
        if (!broken) {
            box(s, 5, 10, 6, 2, 3, POT);
            blob(s, 8, 6, 4, LEAF, random);
            blob(s, 6, 4, 2, LEAF_LIGHT, random);
            blob(s, 10, 7, 2, shade(LEAF), random);
            s.set(12, 9, shade(LEAF));
        } else {                                // tipped over, soil spilled
            box(s, 2, 9, 6, 3, 2, POT);
            s.rect(1, 10, 1, 3, shade(POT));
            blob(s, 11, 10, 3, LEAF, random);
            blob(s, 12, 9, 1, LEAF_LIGHT, random);
            s.rect(8, 13, 4, 1, hex(0x4A3424)); // soil
            s.set(12, 14, hex(0x4A3424));
        }
        return prop(s, broken ? 1 : 4, broken ? 14 : 8);
    }

    static Img bed(boolean broken) {
        Img s = new Img(T, T);
        box(s, 2, 1, 12, 1, 2, WOOD);          // headboard
        box(s, 2, 4, 12, 8, 2, SHEET);
        s.rect(4, 4, 8, 2, WHITE);              // pillow
        int blanket = hex(0x5E7FA6);
        box(s, 2, 7, 12, 5, 2, blanket);
        s.set(2, 14, WOOD_DARK);
        s.set(13, 14, WOOD_DARK);
        if (broken) {
            debris(s, 5, 8);
            debris(s, 9, 10);
            s.set(4, 5, DUST_DARK);
        }
        return prop(s, 2, 12);
    }

    static Img desk(boolean broken) {
        Img s = new Img(T, T);
        box(s, 0, 8, 16, 4, 2, METAL_LIGHT);   // desk top
        s.set(1, 14, METAL_DARK);
        s.set(14, 14, METAL_DARK);
        s.rect(4, 1, 8, 6, SCREEN);             // monitor
        s.rect(5, 2, 6, 4, SCREEN_GLOW);
        s.rect(5, 2, 2, 1, light(SCREEN_GLOW));
        s.rect(7, 7, 2, 1, METAL_DARK);
        s.rect(5, 9, 6, 1, METAL_DARK);         // keyboard
        if (!broken) {
            s.rect(12, 9, 3, 2, PAPER);
        } else {
            s.rect(5, 2, 6, 4, SCREEN);         // the screen is dead and cracked
            for (int[] p : line(5, 2, 10, 5)) {
                s.set(p[0], p[1], METAL_LIGHT);
            }
            s.set(7, 5, METAL_LIGHT);
            s.rect(11, 13, 3, 2, PAPER);        // papers fell off
            s.rect(2, 13, 2, 2, PAPER);
        }
        return prop(s, 0, 16);
    }

    static Img filingCabinet(boolean broken) {
        Img s = new Img(T, T);
        if (!broken) {
            box(s, 3, 1, 10, 2, 12, METAL);
            for (int d = 0; d < 3; d++) {
                int top = 3 + d * 4;
                s.rect(3, top + 3, 9, 1, METAL_DARK);
                s.rect(7, top + 1, 2, 1, METAL_LIGHT);
            }
        } else {                                // fell on its side, a drawer spilled
            box(s, 1, 6, 12, 4, 4, METAL);
            s.rect(5, 6, 1, 4, METAL_DARK);
            s.rect(9, 6, 1, 4, METAL_DARK);
            s.rect(13, 8, 2, 2, PAPER);
            s.rect(11, 13, 3, 2, PAPER);
            s.set(3, 14, PAPER);
        }
        return prop(s, broken ? 1 : 2, broken ? 14 : 12);
    }

    static Img waterCooler(boolean broken) {
        Img s = new Img(T, T);
        box(s, 4, 5, 8, 2, 8, WHITE);
        s.rect(5, 9, 2, 1, CROSS_RED);          // hot tap
        s.rect(9, 9, 2, 1, BOOK_BLUE);          // cold tap
        if (!broken) {
            s.rect(5, 0, 6, 5, WATER);          // the bottle
            s.rect(6, 1, 1, 3, WATER_LIGHT);
        } else {                                // the bottle fell off and leaked
            Random random = new Random(610);
            blob(s, 11, 13, 2, argb(0x5C93B8, 200), random);
            s.rect(9, 12, 6, 3, WATER);
            s.rect(10, 12, 3, 1, WATER_LIGHT);
        }
        return prop(s, 4, 8);
    }

    static Img hospitalBed(boolean broken) {
        Img s = new Img(T, T);
        box(s, 1, 1, 14, 1, 1, METAL);         // head rail
        box(s, 2, 3, 12, 9, 2, SHEET);
        s.rect(4, 3, 8, 2, WHITE);              // pillow
        int blanket = hex(0x9CC9C2);
        box(s, 2, 7, 12, 5, 2, blanket);
        s.rect(1, 3, 1, 11, METAL_DARK);        // side rails
        s.rect(14, 3, 1, 11, METAL_DARK);
        s.set(2, 14, METAL_DARK);
        s.set(13, 14, METAL_DARK);
        if (broken) {
            s.rect(14, 3, 1, 11, CLEAR);        // a bent rail
            for (int[] p : line(14, 4, 15, 12)) {
                s.set(p[0], p[1], METAL_DARK);
            }
            debris(s, 6, 9);
        }
        return prop(s, 1, 14);
    }

    static Img medicineCabinet(boolean broken) {
        Img s = new Img(T, T);
        box(s, 2, 0, 12, 1, 14, WHITE);
        s.rect(3, 2, 9, 4, GLASS);              // glass top with bottles
        s.rect(4, 3, 1, 3, BOOK_YELLOW);
        s.rect(6, 2, 1, 4, WATER);
        s.rect(9, 4, 2, 2, BOOK_RED);
        s.rect(2, 6, 11, 1, SHEET_SHADE);
        s.rect(7, 8, 2, 5, CROSS_RED);          // red cross on the door
        s.rect(5, 10, 6, 1, CROSS_RED);
        if (broken) {
            s.rect(3, 2, 9, 4, SCREEN);         // the glass is gone
            s.set(4, 3, GLASS);
            s.set(10, 2, GLASS);
            s.set(1, 14, GLASS);                // shards and a bottle on the floor
            s.set(14, 13, GLASS);
            s.rect(12, 14, 2, 1, BOOK_YELLOW);
        }
        return prop(s, 2, 12);
    }

    static Img wheelchair(boolean broken) {
        Img s = new Img(T, T);
        if (!broken) {
            s.rect(4, 2, 2, 7, METAL_DARK);     // backrest frame
            box(s, 5, 3, 6, 1, 4, BOOK_BLUE);   // back
            box(s, 5, 8, 8, 2, 1, BOOK_BLUE);   // seat
            Img wheel = new Img(T, T);
            circle(wheel, 7, 10, 4, METAL_DARK);
            circle(wheel, 7, 10, 3, CLEAR);
            wheel.set(7, 10, METAL);
            s.draw(wheel, 0, 0);
            s.rect(12, 10, 1, 3, METAL);
            s.rect(12, 13, 2, 1, METAL_DARK);
        } else {                                // tipped over on its side
            Img wheel = new Img(T, T);
            circle(wheel, 6, 10, 4, METAL_DARK);
            circle(wheel, 6, 10, 3, CLEAR);
            wheel.set(6, 10, METAL);
            box(s, 7, 8, 7, 2, 2, BOOK_BLUE);
            s.rect(8, 5, 6, 2, METAL_DARK);
            s.draw(wheel, 0, 0);
            s.set(14, 13, METAL);
        }
        return prop(s, 2, 12);
    }

    static Img ivStand(boolean broken) {
        Img s = new Img(T, T);
        if (!broken) {
            s.rect(7, 2, 1, 12, METAL);
            s.rect(4, 2, 7, 1, METAL_LIGHT);
            s.rect(4, 14, 8, 1, METAL_DARK);    // feet
            s.rect(9, 3, 3, 5, GLASS);          // the bag
            s.rect(9, 5, 3, 3, WATER);
            s.set(10, 8, GLASS_EDGE);
            s.set(10, 9, GLASS_EDGE);
            s.set(11, 10, GLASS_EDGE);
        } else {                                // knocked down, the bag burst
            for (int[] p : line(2, 4, 13, 13)) {
                s.set(p[0], p[1], METAL);
            }
            s.rect(12, 12, 3, 2, METAL_DARK);
            Random random = new Random(620);
            blob(s, 4, 12, 2, argb(0x7EB6D8, 170), random);
            s.rect(3, 11, 3, 2, GLASS);
        }
        return prop(s, broken ? 2 : 4, broken ? 12 : 8);
    }

    static Img locker(boolean broken) {
        Img s = new Img(T, T);
        int steel = hex(0x5D7383);
        box(s, 2, 0, 12, 1, 14, steel);
        s.rect(7, 1, 1, 14, shade(shade(steel)));
        for (int door = 0; door < 2; door++) {
            int left = 3 + door * 5;
            for (int v = 0; v < 3; v++) {
                s.rect(left, 2 + v * 2, 3, 1, shade(steel));
            }
            s.set(left + (door == 0 ? 3 : 0), 9, METAL_LIGHT);
        }
        if (broken) {
            s.rect(8, 1, 5, 14, SCREEN);        // a door hangs open: dark inside
            s.rect(13, 2, 2, 12, light(steel));
            s.set(4, 6, shade(shade(steel)));   // dents
            s.set(5, 11, shade(shade(steel)));
        }
        return prop(s, 2, 12);
    }

    // ----- Decorations: on the floor, they never block -----

    static Img papers() {
        Img img = new Img(T, T);
        int[][] sheets = {{2, 3}, {8, 6}, {4, 10}};
        for (int[] s : sheets) {
            img.rect(s[0], s[1], 5, 4, PAPER);
            img.rect(s[0] + 1, s[1] + 1, 3, 1, PAPER_LINE);
            img.rect(s[0] + 1, s[1] + 2, 2, 1, PAPER_LINE);
            img.rect(s[0], s[1] + 4, 5, 1, argb(0x000000, 50));
        }
        return img;
    }

    static Img glass() {
        Img img = new Img(T, T);
        int[][] shards = {{3, 4}, {9, 3}, {6, 9}, {12, 11}, {4, 13}};
        for (int[] s : shards) {
            img.set(s[0], s[1], GLASS);
            img.set(s[0] + 1, s[1], GLASS_EDGE);
            img.set(s[0], s[1] + 1, GLASS_EDGE);
        }
        img.set(9, 3, WHITE);
        img.set(12, 11, WHITE);
        return img;
    }

    static Img rug() {
        Img img = new Img(T, T);
        img.rect(1, 1, 14, 14, RUG);
        img.rect(1, 1, 14, 1, RUG_DARK);
        img.rect(1, 14, 14, 1, RUG_DARK);
        img.rect(1, 1, 1, 14, RUG_DARK);
        img.rect(14, 1, 1, 14, RUG_DARK);
        img.rect(3, 3, 10, 10, RUG_LIGHT);
        img.rect(4, 4, 8, 8, RUG);
        img.rect(7, 6, 2, 4, RUG_LIGHT);
        img.rect(6, 7, 4, 2, RUG_LIGHT);
        img.rect(10, 11, 3, 2, argb(0x6B6157, 140)); // dust on it
        return img;
    }

    static Img puddle() {
        Img img = new Img(T, T);
        Random random = new Random(700);
        blob(img, 7, 9, 4, argb(0x5C93B8, 170), random);
        blob(img, 11, 6, 2, argb(0x5C93B8, 170), random);
        img.set(5, 8, WATER_LIGHT);
        img.set(6, 8, WATER_LIGHT);
        img.set(11, 5, WATER_LIGHT);
        return img;
    }

    // =====================================================================
    // People. Shading is hue-shifted and outlines are tinted, like Stardew Valley's.
    // Survivors must read as people who need help (decision 0017): hurt poses,
    // dusty torn clothes, pained faces, mild blood and bandages.
    // =====================================================================

    enum Hat { NONE, CAP, HARD_HAT, BANDAGE }

    enum Badge { NONE, CROSS, STRIPES, PATCH }

    /** What a role carries, so each has its own outline (decision 0019). */
    enum Gear { NONE, MEDIC_PACK, ENGINEER_KIT, K9_VEST, PARAMEDIC }

    static final int PACK_RED = hex(0xC2342B);
    static final int PACK_RED_LIGHT = hex(0xE0564A);
    static final int LAMP = hex(0xFFF6C4);
    static final int LAMP_HOUSING = hex(0x3A3A40);
    static final int BELT = hex(0x4A3424);
    static final int POUCH = hex(0x6B4B30);
    static final int CROWBAR = hex(0x4B5560);
    static final int CROWBAR_LIGHT = hex(0x8A96A3);
    static final int VEST = hex(0x25282F);
    static final int VEST_LIGHT = hex(0x3D424C);
    static final int LEASH = hex(0xD9442B);
    static final int REFLECTIVE = hex(0xE8EEF2);

    /** How a standing person holds their arms. */
    enum Arms { NORMAL, SLING, SLING_WAVE_OUT, SLING_WAVE_UP }

    static final int SKIN_PALE = hex(0xD9C4B6);       // the critical survivor has lost colour
    static final int BLOOD = hex(0x8E1B1B);
    static final int BLOOD_STAIN = argb(0x7A1414, 190);
    static final int DUST_ON_CLOTHES = argb(0x6E6458, 150);
    static final int SWEAT = hex(0xA8D8F0);

    /** What one person wears. Shades are worked out from the main colours (hue shifting). */
    static final class Look {
        final int shirt;
        final int shirtShade;
        final int pants;
        final int pantsShade;
        final Hat hat;
        final int hatColor;
        final int hatShade;
        final Badge badge;
        final int badgeColor;
        final boolean survivor;   // dusty torn clothes and a worried face
        final int skin;
        final int skinShade;
        Gear gear = Gear.NONE;

        /** The same look carrying this role's gear. */
        Look with(Gear gear) {
            this.gear = gear;
            return this;
        }

        Look(int shirt, int pants, Hat hat, int hatColor, Badge badge, int badgeColor, boolean survivor, int skin) {
            this.shirt = shirt;
            this.shirtShade = shade(shirt);
            this.pants = pants;
            this.pantsShade = shade(pants);
            this.hat = hat;
            this.hatColor = hatColor;
            this.hatShade = shade(hatColor);
            this.badge = badge;
            this.badgeColor = badgeColor;
            this.survivor = survivor;
            this.skin = skin;
            this.skinShade = shade(skin);
        }
    }

    /**
     * Rows UP, DOWN, LEFT, RIGHT; columns stand, step A, step B. A survivor's
     * sheet has two more columns: waving with the free arm, out and then up.
     */
    static Img[][] walkSheet(Look look) {
        int columns = look.survivor ? 5 : 3;
        Arms walking = look.survivor ? Arms.SLING : Arms.NORMAL;
        Img[][] sheet = new Img[4][columns];
        for (Dir dir : Dir.values()) {
            for (int frame = 0; frame < 3; frame++) {
                sheet[dir.ordinal()][frame] = dir == Dir.LEFT
                        ? person(look, Dir.RIGHT, frame, walking).mirror()
                        : person(look, dir, frame, walking);
            }
            if (look.survivor) {
                boolean front = dir == Dir.DOWN;
                sheet[dir.ordinal()][3] = front ? person(look, dir, 0, Arms.SLING_WAVE_OUT) : sheet[dir.ordinal()][0];
                sheet[dir.ordinal()][4] = front ? person(look, dir, 0, Arms.SLING_WAVE_UP) : sheet[dir.ordinal()][0];
            }
        }
        return sheet;
    }

    /** A standing person, 16 x 24. frame 0 stands, 1 and 2 are the two steps of the walk. */
    static Img person(Look look, Dir dir, int frame, Arms arms) {
        Img p = new Img(T, CH);
        boolean side = dir == Dir.RIGHT;
        int bob = frame == 0 ? 0 : 1; // the body dips a pixel mid-step
        int shoe = hex(0x2A2226);

        // legs
        if (side) {
            if (frame == 0) {
                p.rect(7, 18, 3, 3, look.pants);
                p.rect(9, 18, 1, 3, look.pantsShade);
                p.rect(7, 21, 4, 1, shoe);
            } else {
                int near = frame == 1 ? look.pants : look.pantsShade;
                int far = frame == 1 ? look.pantsShade : look.pants;
                p.rect(5, 18, 2, 3, far);
                p.rect(4, 21, 3, 1, shoe);
                p.rect(9, 18, 2, 3, near);
                p.rect(9, 21, 3, 1, shoe);
            }
        } else {
            int leftDrop = frame == 1 ? -1 : 0;
            int rightDrop = frame == 2 ? -1 : 0;
            p.rect(6, 18, 2, 3 + leftDrop, look.pants);
            p.rect(6, 21 + leftDrop, 2, 1, shoe);
            p.rect(8, 18, 2, 3 + rightDrop, look.pantsShade);
            p.rect(8, 21 + rightDrop, 2, 1, shoe);
        }

        // body and arms
        int top = 12 + bob;
        if (side) {
            p.rect(6, top, 5, 6 - bob, look.shirt);
            p.rect(6, top, 1, 6 - bob, look.shirtShade);
            p.rect(6, top, 5, 1, light(look.shirt));
            gearUnder(p, look, dir, top, bob);
            if (arms == Arms.NORMAL) {
                int armX = frame == 1 ? 9 : frame == 2 ? 7 : 8;
                p.rect(armX, top + 1, 2, 3, look.shirtShade);
                p.rect(armX, top + 4, 2, 1, look.skin);
            } else {
                p.rect(8, top + 2, 3, 2, WHITE);        // the sling
                p.set(10, top + 2, look.skin);
            }
        } else {
            p.rect(5, top, 6, 6 - bob, look.shirt);
            p.rect(5, top, 6, 1, light(look.shirt));
            p.rect(10, top, 1, 6 - bob, look.shirtShade);
            p.rect(5, 17, 6, 1, look.shirtShade);
            gearUnder(p, look, dir, top, bob);
            int leftArm = frame == 1 ? 3 : 4;
            int rightArm = frame == 2 ? 3 : 4;
            if (arms == Arms.NORMAL) {
                p.rect(4, top, 1, leftArm, look.shirt);
                p.rect(4, top + leftArm, 1, 1, look.skin);
            } else if (dir == Dir.DOWN) {                // the left arm rests in a white sling
                p.rect(4, top, 1, 2, look.shirt);
                p.rect(4, top + 2, 3, 1, WHITE);
                p.rect(5, top + 3, 3, 1, WHITE);
                p.set(7, top + 2, look.skin);
            } else {                                      // from behind: the sling's strap
                p.rect(4, top, 1, 3, look.shirt);
                p.set(6, top, WHITE);
                p.set(7, top + 1, WHITE);
                p.set(8, top + 2, WHITE);
            }
            if (arms == Arms.SLING_WAVE_OUT) {           // the free arm waves above the head
                p.rect(11, 8, 1, 5, look.shirt);
                p.rect(12, 5, 1, 3, look.shirt);
                p.set(13, 4, look.skin);
                p.set(14, 3, look.skin);
                p.set(13, 3, look.skin);
            } else if (arms == Arms.SLING_WAVE_UP) {
                p.rect(11, 8, 1, 5, look.shirt);
                p.rect(12, 4, 1, 4, look.shirt);
                p.rect(12, 2, 1, 2, look.skin);
                p.set(11, 2, look.skin);
            } else {
                p.rect(11, top, 1, rightArm, look.shirtShade);
                p.rect(11, top + rightArm, 1, 1, look.skin);
            }
        }
        badge(p, look, dir, top);
        if (look.survivor) {
            dustAndTears(p, look, side ? 6 : 5, top, side ? 5 : 6);
        }
        gearOver(p, look, dir, top, frame);

        // head
        int headTop = 3 + bob;
        head(p, look, dir, headTop, arms == Arms.SLING_WAVE_OUT || arms == Arms.SLING_WAVE_UP);
        hat(p, look, dir, headTop);
        gearOnHead(p, look, dir, headTop);

        p.outlineTinted();
        groundShadow(p, 4, 22, 8);
        return p;
    }

    /** Gear worn on the body, drawn before the arms: the K9 vest and the paramedic's stripes. */
    static void gearUnder(Img p, Look look, Dir dir, int top, int bob) {
        boolean side = dir == Dir.RIGHT;
        if (look.gear == Gear.K9_VEST) {
            if (side) {
                p.rect(6, top, 5, 5 - bob, VEST);
                p.rect(6, top, 5, 1, VEST_LIGHT);
                p.rect(9, top + 2, 2, 2, VEST_LIGHT);          // a chest pocket
            } else if (dir == Dir.DOWN) {
                p.rect(5, top, 2, 5 - bob, VEST);              // two panels, the blue shirt between
                p.rect(9, top, 2, 5 - bob, VEST);
                p.set(5, top + 2, VEST_LIGHT);                 // pockets
                p.set(10, top + 2, VEST_LIGHT);
            } else {
                p.rect(5, top, 6, 5 - bob, VEST);
                p.rect(6, top + 1, 4, 1, TAPE);                // the yellow "K9" strip on the back
            }
        } else if (look.gear == Gear.PARAMEDIC) {
            p.rect(side ? 6 : 5, top + 3, side ? 5 : 6, 1, REFLECTIVE); // reflective band
        }
    }

    /** Gear over the body and in the hands, drawn after the arms. */
    static void gearOver(Img p, Look look, Dir dir, int top, int frame) {
        boolean side = dir == Dir.RIGHT;
        switch (look.gear) {
            case MEDIC_PACK:
                // A big red medic backpack with a white cross, and a stethoscope round the neck.
                if (dir == Dir.UP) {
                    p.rect(5, top - 1, 6, 6, PACK_RED);
                    p.rect(5, top - 1, 6, 1, PACK_RED_LIGHT);
                    p.rect(7, top, 2, 4, WHITE);
                    p.rect(6, top + 1, 4, 2, WHITE);
                } else if (dir == Dir.DOWN) {
                    p.rect(3, top + 1, 1, 4, PACK_RED);        // the pack shows past both shoulders
                    p.rect(12, top + 1, 1, 4, shade(PACK_RED));
                    p.rect(5, top, 1, 2, PACK_RED);            // straps
                    p.rect(10, top, 1, 2, PACK_RED);
                    p.set(6, top, METAL_DARK);                 // stethoscope round the neck
                    p.set(9, top, METAL_DARK);
                    p.set(10, top + 1, METAL_LIGHT);           // its end, clear of the cross
                } else {
                    p.rect(3, top - 1, 3, 6, PACK_RED);        // on the back, behind them
                    p.rect(3, top - 1, 3, 1, PACK_RED_LIGHT);
                    p.set(4, top + 1, WHITE);
                    p.rect(6, top, 1, 3, shade(PACK_RED));     // strap
                    p.set(10, top, METAL_DARK);                // stethoscope
                    p.set(10, top + 1, METAL_LIGHT);
                }
                break;
            case ENGINEER_KIT:
                // A tool belt with pouches, and a crowbar in the hand.
                if (side) {
                    p.rect(6, 17, 5, 1, BELT);
                    p.rect(6, 16, 2, 2, POUCH);
                    int hand = frame == 1 ? 10 : frame == 2 ? 8 : 9;
                    for (int[] q : line(hand + 1, top + 2, hand + 3, 21)) {
                        p.set(q[0], q[1], CROWBAR);
                    }
                    p.set(hand + 1, top + 1, CROWBAR_LIGHT);   // the hooked end
                    p.set(hand, top + 1, CROWBAR_LIGHT);
                } else {
                    p.rect(5, 17, 6, 1, BELT);
                    if (dir == Dir.DOWN) {
                        p.rect(5, 16, 2, 2, POUCH);
                        p.rect(9, 16, 2, 2, POUCH);
                        p.rect(12, top + 2, 1, 22 - top - 2, CROWBAR);  // crowbar down the right side
                        p.set(12, top + 2, CROWBAR_LIGHT);
                        p.set(13, top + 1, CROWBAR_LIGHT);
                    } else {
                        p.rect(3, top + 2, 1, 22 - top - 2, CROWBAR);   // from behind: on the left
                        p.set(3, top + 2, CROWBAR_LIGHT);
                        p.set(2, top + 1, CROWBAR_LIGHT);
                    }
                }
                break;
            case K9_VEST:
                // The leash is in the hand; the game draws it on to the dog's collar.
                if (dir == Dir.DOWN) {
                    p.rect(3, top + 3, 1, 2, LEASH);
                } else if (side) {
                    p.set(11, top + 4, LEASH);
                }
                break;
            default:
                break;
        }
    }

    /** The engineer's headlamp on the front of the hard hat. */
    static void gearOnHead(Img p, Look look, Dir dir, int headTop) {
        if (look.gear != Gear.ENGINEER_KIT) {
            return;
        }
        if (dir == Dir.DOWN) {
            p.rect(6, headTop - 1, 4, 2, LAMP_HOUSING);
            p.rect(7, headTop - 1, 2, 1, LAMP);
        } else if (dir == Dir.RIGHT) {
            p.rect(11, headTop - 1, 2, 2, LAMP_HOUSING);
            p.set(12, headTop - 1, LAMP);
        } else {
            p.rect(5, headTop, 6, 1, LAMP_HOUSING);            // the strap at the back
        }
    }

    /** Hair, face and expression. A survivor looks worried, and shouts while waving. */
    static void head(Img p, Look look, Dir dir, int headTop, boolean shouting) {
        boolean side = dir == Dir.RIGHT;
        int hairLight = light(HAIR);
        if (side) {
            p.rect(5, headTop, 7, 8, HAIR);
            p.rect(6, headTop, 4, 1, hairLight);
            p.set(5, headTop, CLEAR);
            p.set(11, headTop, CLEAR);
            p.rect(8, headTop + 3, 4, 5, look.skin);
            p.set(5, headTop + 7, CLEAR);
            p.set(11, headTop + 7, look.skinShade);
            p.set(12, headTop + 5, look.skin);         // nose
            p.set(10, headTop + 4, OUTLINE);            // eye
            if (look.survivor) {
                p.set(10, headTop + 3, OUTLINE);        // a worried brow
            }
            p.set(7, headTop + 4, look.skinShade);      // ear
            p.rect(8, headTop + 8, 2, 1, look.skinShade);
        } else if (dir == Dir.UP) {
            p.rect(4, headTop, 8, 8, HAIR);
            p.rect(5, headTop, 4, 1, hairLight);
            p.rect(4, headTop + 6, 8, 2, HAIR_DARK);
            p.set(4, headTop, CLEAR);
            p.set(11, headTop, CLEAR);
            p.set(4, headTop + 4, look.skinShade);      // ears
            p.set(11, headTop + 4, look.skinShade);
            p.rect(7, headTop + 8, 2, 1, look.skinShade);
        } else {
            p.rect(4, headTop, 8, 8, HAIR);
            p.rect(5, headTop, 4, 1, hairLight);
            p.set(4, headTop, CLEAR);
            p.set(11, headTop, CLEAR);
            p.rect(5, headTop + 3, 6, 5, look.skin);
            p.set(4, headTop + 7, CLEAR);
            p.set(11, headTop + 7, CLEAR);
            p.rect(5, headTop + 7, 6, 1, look.skinShade);
            p.set(6, headTop + 4, OUTLINE);             // eyes
            p.set(9, headTop + 4, OUTLINE);
            if (look.survivor) {
                p.set(5, headTop + 3, OUTLINE);         // worried brows, slanting down outwards
                p.set(10, headTop + 3, OUTLINE);
            }
            if (shouting) {
                p.rect(7, headTop + 6, 2, 1, BLOOD);    // open mouth: "Help!"
            }
            p.rect(7, headTop + 8, 2, 1, look.skinShade);
        }
    }

    /** Dust on a survivor's clothes and a tear showing skin. */
    static void dustAndTears(Img p, Look look, int x, int top, int width) {
        p.blend(x + 1, top + 2, DUST_ON_CLOTHES);
        p.blend(x + width - 2, top + 4, DUST_ON_CLOTHES);
        p.blend(x + 2, top + 5, DUST_ON_CLOTHES);
        p.set(x + width - 2, top + 1, look.skin);       // torn sleeve
        p.blend(6, 19, DUST_ON_CLOTHES);
        p.blend(9, 18, DUST_ON_CLOTHES);
    }

    static void hat(Img p, Look look, Dir dir, int headTop) {
        boolean side = dir == Dir.RIGHT;
        int left = side ? 5 : 4;
        int right = 11;
        switch (look.hat) {
            case CAP:
                p.rect(left, headTop - 1, right - left + 1, 3, look.hatColor);
                p.rect(left + 1, headTop - 1, 3, 1, light(look.hatColor));
                p.set(left, headTop - 1, CLEAR);
                p.set(right, headTop - 1, CLEAR);
                if (dir == Dir.DOWN) {
                    p.rect(4, headTop + 2, 8, 1, look.hatShade); // the peak, towards us
                } else if (side) {
                    p.rect(9, headTop + 2, 4, 1, look.hatShade);
                }
                break;
            case HARD_HAT:
                p.rect(left, headTop - 2, right - left + 1, 4, look.hatColor);
                p.set(left, headTop - 2, CLEAR);
                p.set(right, headTop - 2, CLEAR);
                p.rect(left + 2, headTop - 1, 2, 1, light(look.hatColor));
                p.rect(side ? 4 : 3, headTop + 2, 10, 1, look.hatShade);
                break;
            case BANDAGE:
                p.rect(left, headTop + 1, right - left + 1, 2, WHITE);
                p.rect(left, headTop + 2, right - left + 1, 1, SHEET_SHADE);
                if (dir != Dir.UP) {
                    p.set(9, headTop + 1, BLOOD);       // a little blood coming through
                    p.set(10, headTop + 1, CROSS_RED);
                }
                break;
            default:
                break;
        }
    }

    static void badge(Img p, Look look, Dir dir, int top) {
        boolean side = dir == Dir.RIGHT;
        switch (look.badge) {
            case CROSS:
                if (side) {
                    p.rect(8, top + 1, 2, 1, look.badgeColor); // armband
                } else {
                    p.rect(7, top + 1, 2, 3, look.badgeColor);
                    p.rect(6, top + 2, 4, 1, look.badgeColor);
                }
                break;
            case STRIPES:
                for (int y : new int[] {top + 2, top + 4}) {
                    p.rect(side ? 7 : 5, y, side ? 4 : 6, 1, look.badgeColor);
                }
                break;
            case PATCH:
                if (dir == Dir.DOWN) {
                    p.set(6, top + 1, look.badgeColor);
                } else if (dir == Dir.UP) {
                    p.rect(6, top + 1, 4, 2, hex(0xDBEAFE)); // "K9" panel on the back
                }
                p.rect(side ? 6 : 5, 17, side ? 5 : 6, 1, hex(0x2A2420)); // belt
                break;
            default:
                break;
        }
    }

    /**
     * A serious survivor slumped on the floor, both hands on a bleeding, bandaged
     * leg, the other knee pulled up. frame 1 rocks forward, with a drop of sweat.
     */
    static Img sitting(Look look, int frame) {
        Img p = new Img(T, CH);
        int r = frame;
        int shoe = hex(0x2A2226);
        p.rect(2, 19, 7, 2, look.pants);            // the hurt leg, straight out
        p.rect(2, 20, 7, 1, look.pantsShade);
        p.rect(1, 19, 1, 2, shoe);
        p.rect(2, 19, 5, 1, WHITE);                 // bandage, blood soaking through
        p.rect(3, 19, 2, 1, CROSS_RED);
        p.set(4, 20, BLOOD);
        p.set(5, 20, BLOOD);
        p.rect(9, 16, 3, 4, look.pants);            // the other knee, pulled up
        p.rect(11, 16, 1, 4, look.pantsShade);
        p.rect(10, 20, 3, 1, shoe);
        p.rect(5, 13 + r, 6, 6 - r, look.shirt);    // slumped body
        p.rect(5, 13 + r, 6, 1, light(look.shirt));
        p.rect(10, 13 + r, 1, 6 - r, look.shirtShade);
        p.rect(4, 14 + r, 1, 4 - r, look.shirt);    // arms reaching down to the leg
        p.rect(4, 18, 2, 1, look.skin);
        p.rect(8, 15 + r, 1, 3 - r, look.shirtShade);
        p.rect(6, 18, 2, 1, look.skin);
        dustAndTears(p, look, 5, 13 + r, 6);
        int headTop = 6 + r;                        // head hanging forward
        p.rect(4, headTop, 8, 7, HAIR);
        p.rect(5, headTop, 4, 1, light(HAIR));
        p.set(4, headTop, CLEAR);
        p.set(11, headTop, CLEAR);
        p.rect(5, headTop + 3, 6, 4, look.skin);
        p.set(4, headTop + 6, CLEAR);
        p.set(11, headTop + 6, CLEAR);
        p.rect(5, headTop + 6, 6, 1, look.skinShade);
        p.rect(5, headTop + 4, 2, 1, OUTLINE);      // eyes squeezed shut
        p.rect(9, headTop + 4, 2, 1, OUTLINE);
        p.set(5, headTop + 3, OUTLINE);             // pained brows
        p.set(10, headTop + 3, OUTLINE);
        p.rect(7, headTop + 5, 2, 1, BLOOD);        // gritted mouth
        if (frame == 1) {
            p.set(12, headTop + 2, SWEAT);
            p.set(12, headTop + 3, SWEAT);
        }
        p.outlineTinted();
        groundShadow(p, 1, 22, 13);
        return p;
    }

    /**
     * A critical survivor flat on their back: pale, eyes closed, a small pool of
     * blood beside them, an arm flopped out. frame 1 is a slow breath.
     */
    static Img lying(Look look, int frame) {
        Img p = new Img(T, CH);
        Random random = new Random(800);
        Img stain = new Img(T, CH);
        blob(stain, 11, 21, 3, BLOOD_STAIN, random);         // a pool beside them
        blob(stain, 7, 22, 1, BLOOD_STAIN, random);
        p.rect(1, 15, 5, 6, HAIR);                  // head, hair to the left
        p.rect(3, 16, 3, 4, look.skin);
        p.rect(5, 16, 1, 4, look.skinShade);
        p.rect(4, 17, 1, 1, OUTLINE);               // closed eyes
        p.rect(4, 19, 1, 1, OUTLINE);
        p.set(3, 16, hex(0x9E7E9A));               // a bruise
        p.set(1, 15, CLEAR);
        p.set(1, 20, CLEAR);
        p.rect(6, 16 - frame, 6, 5 + frame, look.shirt);
        p.rect(6, 16 - frame, 6, 1, light(look.shirt));
        p.rect(6, 20, 6, 1, look.shirtShade);
        p.rect(7, 18, 2, 1, BLOOD);                 // blood on the shirt
        p.blend(10, 17, DUST_ON_CLOTHES);
        p.set(11, 16, look.skin);                   // torn
        p.rect(8, 13, 1, 3, look.shirt);            // one arm reaching up for help
        p.rect(8, 11, 1, 2, look.skin);
        p.set(9, 11, look.skin);
        p.rect(12, 16, 2, 4, look.pants);
        p.rect(12, 19, 2, 1, look.pantsShade);
        p.rect(14, 16, 1, 4, hex(0x2A2226));        // shoes
        p.outlineTinted();
        Img img = new Img(T, CH);
        img.draw(stain, 0, 0);
        groundShadow(img, 1, 22, 14);
        img.draw(p, 0, 0);
        return img;
    }

    /** The "needs help" bubble: white with a red "!", bouncing a pixel between frames. */
    static Img emote(int frame) {
        Img e = new Img(T, T);
        int y = 2 - frame;
        e.rect(3, y, 9, 8, WHITE);
        e.set(3, y, CLEAR);
        e.set(11, y, CLEAR);
        e.set(3, y + 7, CLEAR);
        e.set(11, y + 7, CLEAR);
        e.rect(4, y + 8, 2, 1, WHITE);              // the tail, pointing at the head below-left
        e.set(4, y + 9, WHITE);
        e.rect(7, y + 1, 2, 4, CROSS_RED);          // "!"
        e.rect(7, y + 6, 2, 1, CROSS_RED);
        e.outline(OUTLINE);
        return e;
    }

    /** Radio bubble (emote frames 2 and 3): a walkie-talkie with signal waves. */
    static Img radioEmote(int frame) {
        Img e = new Img(T, T);
        e.rect(2, 1, 12, 9, WHITE);
        e.set(2, 1, CLEAR);
        e.set(13, 1, CLEAR);
        e.set(2, 9, CLEAR);
        e.set(13, 9, CLEAR);
        e.rect(4, 10, 2, 1, WHITE);
        e.set(4, 11, WHITE);
        e.rect(4, 3, 3, 6, hex(0x2A2A30));         // the radio
        e.rect(5, 1, 1, 2, hex(0x2A2A30));         // its aerial
        e.set(5, 4, hex(0x4ADE80));                // a lit green light
        e.rect(5, 6, 1, 2, METAL);
        int wave = hex(0x2563EB);
        e.rect(8, 4, 1, 3, wave);                  // waves going out
        if (frame == 1) {
            e.rect(10, 3, 1, 5, wave);
            e.set(9, 2, wave);
            e.set(9, 8, wave);
        } else {
            e.set(9, 3, wave);
            e.set(9, 7, wave);
        }
        e.outline(OUTLINE);
        return e;
    }

    /** A puff of dust (4 frames, 16 x 24) that hides the rescuer while they change gear. */
    static Img puff(int frame) {
        Img e = new Img(T, CH);
        Random random = new Random(900 + frame);
        int[] radius = {3, 5, 6, 5};
        int[] alpha = {230, 220, 170, 90};
        int dust = argb(0xD8CFC0, alpha[frame]);
        int dustDark = argb(0xA89E8E, alpha[frame]);
        int[][] centres = {{8, 16}, {5, 13}, {11, 13}, {8, 9}, {4, 19}, {12, 19}};
        for (int i = 0; i < centres.length; i++) {
            int r = Math.max(1, radius[frame] - (i % 3));
            blob(e, centres[i][0], centres[i][1], r, i % 2 == 0 ? dust : dustDark, random);
        }
        if (frame >= 2) {                           // sparkles: the new gear is on
            e.set(3, 6, WHITE);
            e.set(13, 4, WHITE);
            e.set(12, 10 - frame, LAMP);
        }
        return e;
    }

    /** A red road flare by a critical survivor: backup is coming. Frame 1 flickers. */
    static Img flare(int frame) {
        Img e = new Img(T, T);
        e.rect(7, 7, 2, 7, hex(0xB91C1C));         // the stick
        e.rect(7, 7, 1, 7, hex(0xDC2626));
        e.rect(7, 13, 2, 1, hex(0x5A1010));
        int tip = frame == 0 ? 5 : 4;
        e.rect(6, tip, 4, 3, FLAME_ORANGE);        // the burning tip
        e.rect(7, tip, 2, 2, FLAME_WHITE);
        e.set(5 + frame * 5, tip - 1, FLAME_YELLOW); // sparks
        e.set(10 - frame * 4, tip - 2, FLAME_RED);
        e.outlineTinted();
        e.blend(8, 1 + frame, argb(0xF5B0A0, 120)); // a little smoke
        e.blend(9, 0, argb(0xF5B0A0, 80));
        groundShadow(e, 6, 14, 4);
        return e;
    }

    /**
     * The backup team's stretcher. 0: across, empty; 1: across, carrying someone
     * under a red blanket; 2: lengthways, empty; 3: lengthways, carrying someone.
     */
    static Img stretcher(int frame) {
        Img e = new Img(T, T);
        boolean across = frame < 2;
        boolean carrying = frame % 2 == 1;
        int canvas = hex(0xE7E2D6);
        int pole = METAL_DARK;
        if (across) {
            e.rect(0, 6, T, 1, pole);
            e.rect(0, 11, T, 1, pole);
            e.rect(1, 7, 14, 4, canvas);
            if (carrying) {
                e.rect(1, 7, 3, 4, HAIR);              // head at the left
                e.rect(2, 8, 2, 2, SKIN_PALE);
                e.rect(4, 6, 10, 5, hex(0xC0392B));     // red blanket
                e.rect(4, 6, 10, 1, hex(0xE05A4A));
            }
        } else {
            e.rect(5, 0, 1, T, pole);
            e.rect(10, 0, 1, T, pole);
            e.rect(6, 1, 4, 14, canvas);
            if (carrying) {
                e.rect(6, 1, 4, 3, HAIR);              // head at the top
                e.rect(7, 2, 2, 2, SKIN_PALE);
                e.rect(5, 4, 6, 10, hex(0xC0392B));
                e.rect(5, 4, 1, 10, hex(0xE05A4A));
            }
        }
        e.outlineTinted();
        return e;
    }

    /** The Dog Handler's dog, 16 x 16, drawn beside them. frame 1 moves the legs (and tail). */
    static Img dog(Dir dir, int frame) {
        Img d = new Img(T, T);
        int fur = hex(0x8B5A2B);
        int lightFur = light(fur);
        int dark = shade(fur);
        int collar = hex(0x2563EB);
        if (dir == Dir.RIGHT) {
            d.rect(3, 8, 8, 4, fur);
            d.rect(3, 8, 8, 1, lightFur);
            d.rect(3, 11, 8, 1, dark);
            d.rect(10, 5, 4, 5, fur);
            d.rect(13, 7, 2, 2, lightFur);
            d.set(14, 7, OUTLINE);                  // nose
            d.set(12, 6, OUTLINE);                  // eye
            d.rect(10, 4, 2, 2, dark);              // ear
            d.rect(10, 9, 1, 1, collar);
            d.rect(1, frame == 0 ? 6 : 7, 2, 2, fur); // tail
            int a = frame == 0 ? 0 : 1;
            d.rect(4 + a, 12, 1, 2, dark);
            d.rect(6 - a, 12, 1, 2, fur);
            d.rect(8 + a, 12, 1, 2, dark);
            d.rect(10 - a, 12, 1, 2, fur);
        } else if (dir == Dir.DOWN) {
            d.rect(5, 9, 6, 4, fur);
            d.rect(5, 3, 6, 6, fur);
            d.rect(6, 3, 4, 1, lightFur);
            d.rect(4, 3, 1, 3, dark);               // ears
            d.rect(11, 3, 1, 3, dark);
            d.rect(6, 6, 4, 3, lightFur);           // muzzle
            d.rect(7, 6, 2, 1, OUTLINE);            // nose
            d.set(6, 4, OUTLINE);                   // eyes
            d.set(9, 4, OUTLINE);
            d.rect(5, 9, 6, 1, collar);
            int a = frame == 0 ? 0 : 1;
            d.rect(6, 13, 1, 1 + a, fur);
            d.rect(9, 13, 1, 2 - a, fur);
        } else { // UP: from behind
            d.rect(5, 5, 6, 8, fur);
            d.rect(5, 2, 6, 4, fur);
            d.rect(6, 2, 4, 1, lightFur);
            d.rect(4, 2, 1, 3, dark);
            d.rect(11, 2, 1, 3, dark);
            d.rect(5, 6, 6, 1, collar);
            d.rect(frame == 0 ? 7 : 8, 12, 1, 3, fur); // wagging tail
            d.rect(5, 13, 1, 1, dark);
            d.rect(10, 13, 1, 1, dark);
        }
        d.outlineTinted();
        Img img = new Img(T, T);
        groundShadow(img, 3, 15, 10);
        img.draw(d, 0, 0);
        return img;
    }

    /** A survivor's head in a yellow circle: shown on a rescuer who is carrying someone. */
    static Img carryIcon() {
        Img img = new Img(T, T);
        circle(img, 7, 7, 7, hex(0xFACC15));
        circle(img, 7, 7, 6, hex(0xFDE047));
        img.rect(4, 3, 7, 4, HAIR);
        img.rect(5, 6, 5, 5, SKIN);
        img.set(6, 8, OUTLINE);
        img.set(8, 8, OUTLINE);
        img.outline(OUTLINE);
        return img;
    }

    // =====================================================================
    // Drawing helpers
    // =====================================================================

    /** A shadow tone: darker, a bit more saturated, shifted towards blue-violet (hue shifting). */
    static int shade(int color) {
        return shift(color, 0.70f, 0.05f, 0.08f, 0.74f);
    }

    /** A light tone: brighter, shifted towards warm yellow. */
    static int light(int color) {
        return shift(color, 0.13f, 0.04f, -0.06f, 1.16f);
    }

    /** An outline for a colour: a very dark tone of the same family (coloured outlines, not black). */
    static int outlineOf(int color) {
        return shift(color, 0.75f, 0.06f, 0.10f, 0.32f);
    }

    /** Moves a colour's hue towards targetHue (by at most hueAmount), then changes saturation and brightness. */
    static int shift(int color, float targetHue, float hueAmount, float satChange, float brightness) {
        float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
        float hue = hsb[0];
        float sat = hsb[1];
        if (sat < 0.08f) {
            hue = targetHue;                  // greys take the light's or shadow's tint
            sat = sat + Math.max(0, satChange) * 0.6f;
        } else {
            float diff = targetHue - hue;
            if (diff > 0.5f) {
                diff -= 1;
            }
            if (diff < -0.5f) {
                diff += 1;
            }
            hue += Math.signum(diff) * Math.min(Math.abs(diff), hueAmount);
            hue = (hue % 1 + 1) % 1;
            sat = sat + satChange;
        }
        sat = Math.max(0, Math.min(1, sat));
        float bri = Math.max(0, Math.min(1, hsb[2] * brightness));
        return (color & 0xFF000000) | (Color.HSBtoRGB(hue, sat, bri) & 0xFFFFFF);
    }

    static int hex(int rgb) {
        return 0xFF000000 | rgb;
    }

    static int argb(int rgb, int alpha) {
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    /** A soft dark oval on the floor under a person or object. */
    static void groundShadow(Img img, int x, int y, int width) {
        for (int i = 0; i < width; i++) {
            boolean end = i == 0 || i == width - 1;
            img.blend(x + i, y, argb(0x000000, end ? 35 : 70));
            if (!end && y + 1 < img.h) {
                img.blend(x + i, y + 1, argb(0x000000, 30));
            }
        }
    }

    static void blob(Img img, int cx, int cy, int radius, int color, Random random) {
        for (int y = cy - radius; y <= cy + radius; y++) {
            for (int x = cx - radius; x <= cx + radius; x++) {
                double dx = x - cx;
                double dy = y - cy;
                if (dx * dx + dy * dy <= radius * radius + random.nextInt(2)) {
                    img.set(x, y, color);
                }
            }
        }
    }

    static void circle(Img img, int cx, int cy, int radius, int color) {
        for (int y = cy - radius; y <= cy + radius; y++) {
            for (int x = cx - radius; x <= cx + radius; x++) {
                if ((x - cx) * (x - cx) + (y - cy) * (y - cy) <= radius * radius + radius / 2) {
                    img.set(x, y, color);
                }
            }
        }
    }

    /** The points of a straight line (Bresenham). */
    static List<int[]> line(int x0, int y0, int x1, int y1) {
        List<int[]> points = new ArrayList<>();
        int dx = Math.abs(x1 - x0);
        int dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        while (true) {
            points.add(new int[] {x0, y0});
            if (x0 == x1 && y0 == y1) {
                break;
            }
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y0 += sy;
            }
        }
        return points;
    }

    /** 3 x 5 pixel letters for signs. */
    static void letters(Img img, int x, int y, int color, String... chars) {
        for (String c : chars) {
            String[] shape;
            switch (c) {
                case "E":
                    shape = new String[] {"###", "#..", "##.", "#..", "###"};
                    break;
                case "X":
                    shape = new String[] {"#.#", "#.#", ".#.", "#.#", "#.#"};
                    break;
                case "I":
                    shape = new String[] {"#", "#", "#", "#", "#"};
                    break;
                default: // T
                    shape = new String[] {"###", ".#.", ".#.", ".#.", ".#."};
                    break;
            }
            for (int row = 0; row < shape.length; row++) {
                for (int col = 0; col < shape[row].length(); col++) {
                    if (shape[row].charAt(col) == '#') {
                        img.set(x + col, y + row, color);
                    }
                }
            }
            x += shape[0].length() + 1;
        }
    }

    static Img[][] row(Img... frames) {
        return new Img[][] {frames};
    }

    // =====================================================================
    // Images and files
    // =====================================================================

    /** A picture as ARGB pixels. */
    static final class Img {
        final int w;
        final int h;
        final int[] px;

        Img(int w, int h) {
            this.w = w;
            this.h = h;
            this.px = new int[w * h];
        }

        int get(int x, int y) {
            return x < 0 || y < 0 || x >= w || y >= h ? CLEAR : px[y * w + x];
        }

        void set(int x, int y, int color) {
            if (x >= 0 && y >= 0 && x < w && y < h) {
                px[y * w + x] = color;
            }
        }

        void rect(int x, int y, int width, int height, int color) {
            for (int yy = y; yy < y + height; yy++) {
                for (int xx = x; xx < x + width; xx++) {
                    set(xx, yy, color);
                }
            }
        }

        void fill(int color) {
            rect(0, 0, w, h, color);
        }

        /** Paints a see-through colour over what is there. */
        void blend(int x, int y, int color) {
            if (x < 0 || y < 0 || x >= w || y >= h) {
                return;
            }
            int under = get(x, y);
            int a = (color >>> 24) & 0xFF;
            int ua = (under >>> 24) & 0xFF;
            if (ua == 0) {
                set(x, y, color);
                return;
            }
            int r = mix((color >> 16) & 0xFF, (under >> 16) & 0xFF, a);
            int g = mix((color >> 8) & 0xFF, (under >> 8) & 0xFF, a);
            int b = mix(color & 0xFF, under & 0xFF, a);
            int outA = Math.min(255, a + ua * (255 - a) / 255);
            set(x, y, (outA << 24) | (r << 16) | (g << 8) | b);
        }

        static int mix(int top, int bottom, int alpha) {
            return (top * alpha + bottom * (255 - alpha)) / 255;
        }

        /** Draws another picture on top (see-through pixels blend). */
        void draw(Img other, int dx, int dy) {
            for (int y = 0; y < other.h; y++) {
                for (int x = 0; x < other.w; x++) {
                    int color = other.px[y * other.w + x];
                    int a = (color >>> 24) & 0xFF;
                    if (a == 255) {
                        set(x + dx, y + dy, color);
                    } else if (a > 0) {
                        blend(x + dx, y + dy, color);
                    }
                }
            }
        }

        Img mirror() {
            Img m = new Img(w, h);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    m.px[y * w + (w - 1 - x)] = px[y * w + x];
                }
            }
            return m;
        }

        /** Every empty pixel touching a solid one becomes the outline colour. */
        void outline(int color) {
            int[] copy = px.clone();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (((copy[y * w + x] >>> 24) & 0xFF) != 0) {
                        continue;
                    }
                    if (solid(copy, x - 1, y) || solid(copy, x + 1, y) || solid(copy, x, y - 1)
                            || solid(copy, x, y + 1)) {
                        px[y * w + x] = color;
                    }
                }
            }
        }

        /** Like outline(), but each outline pixel is a dark tone of the colour it touches. */
        void outlineTinted() {
            int[] copy = px.clone();
            int[][] around = {{0, 1}, {-1, 0}, {1, 0}, {0, -1}};
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (((copy[y * w + x] >>> 24) & 0xFF) != 0) {
                        continue;
                    }
                    for (int[] d : around) {
                        if (solid(copy, x + d[0], y + d[1])) {
                            px[y * w + x] = outlineOf(copy[(y + d[1]) * w + x + d[0]]);
                            break;
                        }
                    }
                }
            }
        }

        private boolean solid(int[] pixels, int x, int y) {
            return x >= 0 && y >= 0 && x < w && y < h && ((pixels[y * w + x] >>> 24) & 0xFF) == 255;
        }

        BufferedImage toBuffered() {
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, w, h, px, 0, w);
            return image;
        }
    }

    /** Joins frames into one sheet (rows x columns, all frames the same size) and saves it. */
    static void save(File folder, String key, Img[][] frames) throws IOException {
        int fw = frames[0][0].w;
        int fh = frames[0][0].h;
        int cols = 0;
        for (Img[] r : frames) {
            cols = Math.max(cols, r.length);
        }
        Img sheet = new Img(fw * cols, fh * frames.length);
        for (int r = 0; r < frames.length; r++) {
            for (int c = 0; c < frames[r].length; c++) {
                sheet.draw(frames[r][c], c * fw, r * fh);
            }
        }
        ImageIO.write(sheet.toBuffered(), "png", new File(folder, key + ".png"));
        PREVIEW_NAMES.add(key);
        PREVIEW_SHEETS.add(frames);
        System.out.printf("  %-18s %d x %d frames of %d x %d%n", key + ".png", frames.length, cols, fw, fh);
    }

    /** Every sheet at 4x on a dark checkered background, with its name, in one picture. */
    static void writePreview(File file) throws IOException {
        int scale = 4;
        int width = 1200;
        int x = 12;
        int y = 12;
        int lineHeight = 0;
        List<int[]> places = new ArrayList<>();
        for (Img[][] frames : PREVIEW_SHEETS) {
            int sheetW = 0;
            for (Img[] r : frames) {
                int rowW = 0;
                for (Img f : r) {
                    rowW += f.w * scale + 4;
                }
                sheetW = Math.max(sheetW, rowW);
            }
            int sheetH = 0;
            for (Img[] r : frames) {
                sheetH += r[0].h * scale + 4;
            }
            if (x + sheetW > width) {
                x = 12;
                y += lineHeight + 28;
                lineHeight = 0;
            }
            places.add(new int[] {x, y});
            x += sheetW + 20;
            lineHeight = Math.max(lineHeight, sheetH);
        }
        int height = y + lineHeight + 40;
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        for (int cy = 0; cy < height; cy += 16) {
            for (int cx = 0; cx < width; cx += 16) {
                g.setColor(((cx + cy) / 16) % 2 == 0 ? new Color(0x2E2E2E) : new Color(0x363636));
                g.fillRect(cx, cy, 16, 16);
            }
        }
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        for (int i = 0; i < PREVIEW_SHEETS.size(); i++) {
            Img[][] frames = PREVIEW_SHEETS.get(i);
            int px = places.get(i)[0];
            int py = places.get(i)[1];
            int fy = py;
            for (Img[] r : frames) {
                int fx = px;
                for (Img f : r) {
                    g.drawImage(f.toBuffered(), fx, fy, f.w * scale, f.h * scale, null);
                    fx += f.w * scale + 4;
                }
                fy += r[0].h * scale + 4;
            }
            g.setColor(Color.WHITE);
            g.drawString(PREVIEW_NAMES.get(i), px, fy + 12);
        }
        g.dispose();
        ImageIO.write(out, "png", file);
    }
}
