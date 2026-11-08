# Rescue Grid: Game Plan (v2)

This document describes the game we are building, its rules, its class design, and how we want to build it. Read all of it before writing any code. If something here is unclear or seems wrong, ask before changing the design.

> **What changed in v2 (2026-11-08), after teacher feedback**
> - The game is now **real-time, tile by tile**. You hold WASD to walk, like a much simpler Soul Knight. Turns, action points (AP) and the End Turn button are gone.
> - You **explore**: the camera follows your rescuer through a bigger building, unexplored areas are hidden by fog, and a **minimap** shows where you are.
> - The **map keeps changing**:
>   - Aftershocks collapse cracked floor and cut off shortcuts.
>   - Fire spreads to block corridors, then burns out and reopens them.
> - **Sprites and art from online are the main look.** Plain shapes are only a stand-in until art exists.
> - Every class name from the proposal is kept. New classes: `CrackedFloor`, `BurntFloor`, `Camera`, `Minimap`, `KeyboardInput`.
> - Why we changed: see `decisions/0001` and `decisions/0002`.

---

## 1. Project Context

- **Course:** Java Object Oriented Programming (2nd year, university). This is our first Java project.
- **Assignment:** A final group project that must be a game related to disaster management or a similar theme.
- **Team:** 3 members.
- **Timeline:** 4 weeks.
- **Grading focus:** Object oriented design. The teacher grades how well we use encapsulation, inheritance, polymorphism, and abstraction. Polish matters less than clean OOP.
- **A proposal has already been submitted** to the teacher. The class names and OOP examples in Section 8 were promised in that proposal, so keep them.

### The one rule that matters most

**All game logic, rules, features, and mechanics must be written by us in Java.** This includes the grid, movement, the game loop, the camera, fog, the minimap, hazards, scoring, and the win and lose conditions.

**Visual and audio assets can come from anywhere.** Sprites, tile art, character art, backgrounds, UI images, sound effects, and music may be downloaded or made with any tool, and they are the intended look of the game. Every `draw()` method keeps a simple colored fallback anyway. That lets us test in Week 1 before art exists, and a missing file never crashes the game (see Section 10).

---

## 2. Game Summary

Rescue Grid is a real-time, top-down rescue game set inside a building that collapsed after an earthquake. The player controls a team of three rescuers with different skills and switches between them at any time. They explore the dark, damaged building, find trapped survivors, and carry them to the exit. Meanwhile fire spreads and aftershocks change the layout of the building.

The theme is based on real events. Cebu was hit by a magnitude 6.9 earthquake on September 30, 2025, and the aftershocks that followed made damaged buildings dangerous to enter.

**Core idea:** you can't see the whole building, you can only control one rescuer at a time, and the building gets worse every few seconds. The route you used a minute ago may not be there any more.

**Not Soul Knight:** there are no enemies, no shooting, no weapons, and no randomly generated maps. Fire and aftershocks are the "enemies". Each of those features would roughly double the project.

---

## 3. Tech Stack and Constraints

| Item | Choice |
|---|---|
| Language | Java 17 or later |
| UI | Java Swing (`JFrame`, `JPanel`, `paintComponent`) |
| Game loop | One `javax.swing.Timer` (about 60 updates per second) |
| Images | PNG, loaded with `ImageIO` |
| Audio | WAV, played with `javax.sound.sampled` (`Clip`) |
| Levels | `.txt` files in `resources/levels/` |
| IDE | IntelliJ IDEA |
| Version control | Git and GitHub |

**Do not use:**
- Game engines or frameworks (libGDX, LWJGL, JavaFX game libraries, etc.)
- Any external library for game logic
- Databases, networking, or multiplayer
- Threads. The one `javax.swing.Timer` runs on the Swing event thread, so the whole game runs on a single thread and we never have to think about thread safety.
- Design patterns we have not learned yet (our teacher will introduce them later). Keep the structure to plain classes, inheritance, interfaces, and composition.

**The game loop:** `GamePanel` owns exactly one timer: `new javax.swing.Timer(16, e -> { update(); repaint(); })`. Everything that changes over time (movement, cooldowns, fire, aftershocks, survivor condition) is driven from `update()`. No other timers.

---

## 4. Game Rules

All numbers marked *(tune)* are starting values. Change them during testing, and write the new value here when you do.

### 4.1 The Grid

- The building is a 2D grid of tiles: **Floor, Wall, Rubble, Fire, Exit, CrackedFloor, BurntFloor**.
- Tile size on screen: 64 by 64 pixels.
- Movement is orthogonal only (up, down, left, right). No diagonals.

| Tile | Walkable | Burns | Notes |
|---|---|---|---|
| Floor | Yes | Yes | Can collapse into Rubble during an aftershock (random) |
| CrackedFloor | Yes | Yes | **Always** collapses into Rubble on every aftershock. Placed on purpose across shortcuts |
| Wall | No | No | Never changes |
| Rubble | No | No | The Engineer can clear it into Floor |
| Fire | No | – | Spreads every world tick. Hurts rescuers next to it. Burns out after 6 ticks *(tune)* |
| BurntFloor | Yes | No | What fire leaves behind. Never burns again |
| Exit | Yes | No | Deliver survivors here |

### 4.2 Time: the world tick

The game runs in real time, but the **world** only changes on a **world tick** every **3 seconds** *(tune)*. The world tick takes the place of the old End Turn. On each tick, `TurnManager` runs these steps **in exactly this order**:

1. **Fire damage:** every active rescuer orthogonally next to a Fire tile takes 1 damage.
2. **Fire spreads:** each Fire tile tries to spread to each orthogonal neighbour that burns (Floor or CrackedFloor), with a 25% chance each *(tune)*. Fire never spreads onto a tile with a rescuer on it.
3. **Fire burns out:** each Fire tile ages by 1. At age 6 it becomes BurntFloor.
4. **Survivor condition:** every survivor not yet rescued loses 1 condition. Survivors at 0 are lost.
5. **Aftershock countdown:** drops by 1, and an aftershock happens if it reaches 0 (see 4.5).
6. **End check:** see 4.6.

The order matters. Damage is applied before fire spreads, so you are only hurt by fire you could already see.

**Beginner warning:** when changing tiles during a tick, first **collect** the changes in a list, then **apply** them all. If you change tiles while still looping over the grid, a tile that just caught fire could spread again in the same tick.

### 4.3 Rescuers

The player has three rescuers. All share these stats:
- **Health:** 5 *(tune)*
- **Walk speed:** 1 tile every 0.15 s, or every 0.30 s while carrying a survivor *(tune)*

You control **one rescuer at a time**. Press 1, 2, 3 or Tab to switch, and the camera jumps to that rescuer. The others stay where they are, and **they can still be hurt** by fire and aftershocks. The HUD shows all three health bars so you notice.

| Rescuer | Special action (Space) | Cooldown |
|---|---|---|
| Medic | Heals the survivor she is carrying, or a revealed survivor on the tile she is facing. Restores 8 condition, up to the max | 4 s *(tune)* |
| Engineer | Clears the Rubble tile he is facing into Floor | 3 s *(tune)* |
| Dog Handler | **Scent:** reveals every survivor within 8 walking steps. The scent follows corridors and does not pass through walls. Revealed survivors appear on the minimap. No target needed | 8 s *(tune)* |

**Movement rules:**
- Holding a direction key keeps walking. The rescuer always turns to face the key you pressed, even when blocked, so you can face rubble to clear it.
- A rescuer can only step onto walkable tiles, and never onto a tile with another rescuer on it.
- Walking into a wall just doesn't move you. It does **not** throw an exception, because that would happen 60 times a second while you hold a key into a wall.

**Downed rescuers:** when health reaches 0, the rescuer is downed and removed from play. A survivor they were carrying is dropped on their tile. If the downed rescuer was the one you were controlling, control switches to the next active rescuer.

### 4.4 Survivors and fog

**Fog of war:**
- At the start, every tile is **unexplored** and drawn black.
- The active rescuer **sees** 4 walking steps around them *(tune)*. Sight goes through walkable tiles and stops at anything else, so you can see a wall, rubble or fire, but not what's behind it.
- Tiles you can see right now are drawn normally. Tiles you have seen before but can't see now are drawn **dimmed**, showing their current state.
- `MovementCalculator` computes what you can see with recursion (Section 8).

**Survivors:**
- Each level has several survivors on Floor tiles.
- A survivor is **revealed** when it is inside the active rescuer's sight, or found by the Dog Handler's scent. Once revealed, it stays revealed and shows on the minimap.
- **Picking up:** walking onto a survivor's tile with free hands picks it up automatically. A rescuer carries at most one survivor.
- **Rescuing:** walking onto the Exit while carrying a survivor delivers it immediately.
- **Condition:** starts at 20 and drops by 1 every world tick, so a survivor lasts 60 s if left alone *(tune)*. At 0 the survivor is lost. Carried survivors keep losing condition too, which is why the Medic matters.
- If fire spreads onto a survivor that is not being carried, that survivor is lost.

### 4.5 Aftershocks

- Each level has an aftershock countdown measured in world ticks: Level 1 every 15 ticks (45 s), Level 2 every 12 (36 s), Level 3 every 9 (27 s) *(tune)*.
- The HUD shows "Next aftershock: Xs". At 5 s left, the screen shakes slightly and plays a warning rumble.
- When an aftershock happens:
  1. **Every CrackedFloor tile becomes Rubble.** This is the designed path-blocker.
  2. Up to 3 random Floor tiles become Rubble *(tune)*. Skip tiles that have a rescuer or survivor on them, or that are next to the Exit.
  3. Every active rescuer takes 1 damage.
  4. The countdown resets.
- A cracked tile with a rescuer standing on it still collapses. The rescuer is pushed to a free neighbouring walkable tile, or takes 1 extra damage if there is none.
- **Rubble never traps a level for good.** The Engineer can always clear it. A blocked path costs time, it is never a dead end.

### 4.6 End Conditions

The end check runs at the end of every world tick and right after every delivery. The mission ends when:
- no survivors are left in the building (all rescued or lost), or
- all rescuers are downed.

The mission is a **success** if the number rescued is at least the level's target. Otherwise it is a **failure**.

| Level | Size (approx.) | Survivors | Target | Aftershock every |
|---|---|---|---|---|
| 1 | 30 × 20 | 3 | 2 | 15 ticks |
| 2 | 35 × 25 | 4 | 3 | 12 ticks |
| 3 | 40 × 30 | 5 | 4 | 9 ticks |

### 4.7 Scoring

- 100 points per survivor rescued
- 20 points per remaining health point across all active rescuers
- 50 bonus points if every survivor in the level was rescued
- Time bonus: `max(0, 120 - secondsTaken)` points *(tune)*

---

## 5. Level Format

Levels are text files, one character per tile, stored in `resources/levels/level1.txt`, `level2.txt`, `level3.txt`.
- `LevelLoader` reads the file into a `String[]` (one string per line), then turns that array into a `GameBoard`.
- Keep those two steps as separate methods. Then you can also build a board straight from a `String[]` written in code, which is handy for quick tests.

| Character | Meaning |
|---|---|
| `#` | Wall |
| `.` | Floor |
| `C` | Cracked floor (collapses on every aftershock) |
| `R` | Rubble |
| `F` | Fire |
| `X` | Exit |
| `S` | Survivor (on Floor) |
| `M` | Medic start (on Floor) |
| `E` | Engineer start (on Floor) |
| `D` | Dog Handler start (on Floor) |

`LevelLoader` should throw a clear exception if:
- the rows have different lengths,
- the outer border is not all `#`,
- there is not exactly one exit, or
- any rescuer start is missing.

The border rule means the camera and the recursive flood fill can never walk off the map.

### Level 1 (30 wide, 20 tall, 3 survivors, target 2)

```
##############################
#........#..........#.......X#
#..S.....#..........#........#
#........#.....F....C........#
#........#.....F....C........#
#........#...S......#........#
####R#####..........#........#
#........#..........######.###
#........#..........#........#
#...................#........#
#........#..........#........#
#........#####.######........#
#........#..........#........#
#........#..........###.######
#........#..........#........#
#........#..........#........#
#..M.E.D............#.....S..#
#........#...................#
#........#..........#........#
##############################
```

**What this level teaches** (positions are `row,col`, counting from 0):
- **The Engineer matters.** The storeroom in the top left (survivor at `2,3`) has only one door, and it is Rubble (`6,4`).
- **The quake changes your route.** The fastest way from the burning central hall to the exit room is the cracked gap at `3,20` and `4,20`. When the first aftershock hits, the gap becomes rubble.
- **Fire near a survivor.** The survivor at `5,13` sits close to the fire, so there is pressure to reach them first.
- **The east wing is the long, safe route**, and the third survivor (`16,26`) is on it.

Route lengths in tiles, checked with a script:

| Survivor | To exit, gap open | To exit, after the gap collapses |
|---|---|---|
| `5,13` (central hall) | 19 | 43 (east wing detour), or clear the rubble |
| `2,3` (storeroom) | 40 | 56 |
| `16,26` (east wing) | 23 | 23 |

- The storeroom survivor can't be reached at all until the Engineer clears `6,4`.
- With the gap open, one rescuer doing all three rescues needs about 33 s of pure movement. Each survivor lasts 60 s, so the level is tight but fair, and the target is only 2 of 3.

**Design Levels 2 and 3 with the same rules.** Each level must have:
- at least one cracked-floor shortcut, plus a longer backup route,
- at least one survivor that only the Engineer can reach,
- at least one survivor in a side room that is easy to miss without the Dog Handler, and
- every survivor still reachable after every cracked tile has collapsed (counting rubble as clearable).

---

## 6. Screens and Controls

### Screens

1. **Main Menu:** title, Start, How to Play, Quit. Menu music plays.
2. **How to Play:** the rescuers, the tiles (with pictures), fog, aftershocks, and the controls.
3. **Game Screen:** the camera view on the left, the HUD panel on the right, and the minimap in the top-right corner of the camera view.
4. **Pause overlay** (Esc): Resume, Restart Level, Main Menu.
5. **Result Screen:** Success or Failure, survivors rescued, score breakdown, and buttons for Next Level, Retry, and Main Menu.

Use `CardLayout` in `GameFrame` to switch screens.

### Camera view and minimap

- The camera view is **15 × 11 tiles** (960 × 704 px), centred on the active rescuer and clamped at the map edges.
- The **minimap** draws the whole map at 4 px per tile:
  - explored tiles in their colour, unexplored tiles black,
  - the Exit in green,
  - all three rescuers as coloured dots (the active one larger),
  - revealed survivors as blinking dots.

### HUD contents

- Level number, time elapsed, next aftershock countdown
- Survivors rescued out of target, and survivors lost
- **Team list:** all three rescuers with their health bars. The active one is highlighted, and a downed one is greyed out.
- Active rescuer: name, special name, cooldown bar
- Carried survivor's condition, if carrying
- A message line for warnings and invalid actions ("Nothing to clear here", "Engineer is next to fire!")

### Controls

| Input | Action |
|---|---|
| WASD / arrow keys | Walk (hold to keep walking). Also turns to face that direction |
| 1, 2, 3 or Tab | Switch to Medic / Engineer / Dog Handler, or cycle to the next |
| Space | Special action on the faced tile (the Dog Handler needs no target) |
| Esc | Pause |

Invalid special actions (on cooldown, nothing valid on the faced tile) show a short message in the HUD and change nothing.

---

## 7. Package Structure

```
src/
  rescuegrid/
    Main.java
    ui/
      GameFrame.java
      MenuPanel.java
      HowToPlayPanel.java
      GamePanel.java        (owns the game loop Timer)
      HudPanel.java
      ResultPanel.java
      Camera.java           (new)
      Minimap.java          (new)
      KeyboardInput.java    (new)
    model/
      GameBoard.java
      Survivor.java
      Drawable.java
      GameState.java
      tiles/
        Tile.java
        Floor.java
        CrackedFloor.java   (new, extends Floor)
        BurntFloor.java     (new, extends Floor)
        Wall.java
        Rubble.java
        Fire.java
        Exit.java
      rescuers/
        Rescuer.java
        Medic.java
        Engineer.java
        DogHandler.java
    logic/
      TurnManager.java      (runs the world tick)
      HazardManager.java
      MovementCalculator.java
      LevelLoader.java
      LevelData.java
      ScoreKeeper.java
      InvalidMoveException.java
    assets/
      AssetManager.java
      SoundManager.java
resources/
  images/
  sounds/
  music/
  levels/
    level1.txt
    level2.txt
    level3.txt
CREDITS.md
decisions/
```

---

## 8. Class Design

These classes and their roles were promised in our proposal. Keep the names.

### Abstract classes

**`Tile`** (abstract, implements `Drawable`)
- Fields:
  - `protected int row, col`
  - `private boolean explored`, with `markExplored()` and `isExplored()`
- Abstract methods:
  - `boolean isWalkable()`
  - `boolean isFlammable()`
  - `Color getFallbackColor()`
  - `String getImageKey()`
- Hook methods with a default "do nothing / false" body. Subclasses override only the ones they need.
  - `boolean isClearable()`: only `Rubble` returns `true`. The Engineer checks this.
  - `boolean isBurning()`: only `Fire` returns `true`. Fire damage and fire spread check this.
  - `boolean canCollapse()`: only `Floor` returns `true`. Random aftershock collapses pick from these tiles.
  - `void onAftershock(GameBoard board)`: `CrackedFloor` replaces itself with `Rubble`.
  - `void onWorldTick(GameBoard board)`: `Fire` ages by one and replaces itself with `BurntFloor` at age 6.
- Concrete method: `draw(...)` uses the image from `AssetManager`, or the fallback colour if the image is missing.
- Subclasses:
  - `Floor`, `Wall`, `Rubble`, `Fire`, `Exit` extend `Tile`.
  - `CrackedFloor` and `BurntFloor` extend **`Floor`**, which is two-level inheritance.
  - `CrackedFloor` overrides `onAftershock`, `canCollapse` (to return `false`, since it always collapses anyway) and `getImageKey`.
  - `BurntFloor` overrides `isFlammable`, `canCollapse` and `getImageKey`.

**`Rescuer`** (abstract, implements `Drawable`)
- Private fields:
  - `row`, `col`, `health`, `carriedSurvivor`
  - `facing`, a `Direction` enum: `UP`, `DOWN`, `LEFT`, `RIGHT`
  - `specialCooldown` (seconds left) and `moveTimer` (seconds until the next step is allowed)
- Methods:
  - `void tick(double seconds)`: counts both timers down
  - `boolean canStep()` and `boolean canUseSpecial()`
  - `void startCooldown()`
  - `void takeDamage(int amount)`, `boolean isDowned()`
  - `double getStepDelay()`: 0.15 normally, 0.30 when carrying
  - `pickUp(Survivor s)`, `Survivor dropSurvivor()`
  - `moveTo(int row, int col)` and `moveTo(Tile target)`: this is the overloading example
  - `Tile getFacedTile(GameBoard board)`
- Abstract methods:
  - `void specialAction(GameBoard board) throws InvalidMoveException`. Each subclass finds its own target, usually `getFacedTile(board)`.
  - `String getSpecialName()`
  - `double getCooldownSeconds()`
- Subclasses: `Medic`, `Engineer`, `DogHandler`

### Interface

**`Drawable`**
- `void draw(Graphics2D g, int x, int y, int size, AssetManager assets)`
- Implemented by `Tile`, `Rescuer`, `Survivor`, and `Minimap`

### Model classes

**`GameBoard`**
- Holds:
  - `Tile[][] grid`, `ArrayList<Rescuer> rescuers`, `ArrayList<Survivor> survivors`
  - `boolean[][] visible`, recalculated whenever the active rescuer moves or switches, or the world ticks
- Methods: `getTile(row, col)`, `replaceTile(row, col, Tile t)`, `inBounds(row, col)`, `getRescuerAt(row, col)`, `getSurvivorAt(row, col)`, `isVisible(row, col)`
- `replaceTile` is how fire spreads, fire burns out, rubble is cleared, and cracked floor collapses. Swap the tile object for a new subclass object, and copy the `explored` flag to the new tile. Do not use a type field.

**`Survivor`**
- Private fields: `row`, `col`, `condition`, `revealed`, `carried`, `rescued`, `lost`
- Methods: `tickCondition()`, `heal(int amount)`, `reveal()`, plus getters

**`GameState`** (enum): `MENU`, `PLAYING`, `PAUSED`, `LEVEL_COMPLETE`, `GAME_OVER`

### Logic classes

- **`MovementCalculator`**
  - `boolean canStep(GameBoard board, Rescuer r, Direction d)`: a one-tile check. The target tile must be in bounds, walkable, and have no rescuer on it.
  - `void computeSight(GameBoard board, Rescuer r, int range)`: uses a **recursive depth-first flood fill**. It marks reached tiles visible and explored, and reveals survivors on them.
  - `List<Survivor> findByScent(GameBoard board, Rescuer r, int range)`: the same recursive flood fill with range 8, used by the Dog Handler.
  - How the recursion works: `explore(row, col, stepsLeft, int[][] best)`.
    - If the tile is out of bounds, or `stepsLeft` is not better than `best[row][col]`, stop.
    - Otherwise record it in `best`, mark the tile as reached, and, if it is see-through, call `explore` on all four neighbours with `stepsLeft - 1`.
    - The `best` array stops the same tile being explored again and again. It is the same idea as the movement-range algorithm in v1.
- **`TurnManager`**
  - Adds up the elapsed time.
  - Runs the world tick (Section 4.2) every 3 s, in the exact order listed there.
  - Tracks elapsed time and the aftershock countdown for the HUD.
- **`HazardManager`**: fire damage, fire spread, aftershocks. Uses one `java.util.Random` created with a seed per level, so bugs can be reproduced.
- **`LevelLoader`**: reads a `.txt` file into a `String[]`, then turns it into a `GameBoard`. Uses a `switch` on characters to build tile objects. This is the only place where tile types are chosen with a switch.
- **`LevelData`**: per-level settings (file name, target, aftershock ticks, random seed).
- **`ScoreKeeper`**: calculates the score from Section 4.7.
- **`InvalidMoveException`**: custom checked exception, thrown by special actions that can't happen (on cooldown, no valid target). The UI catches it and shows the message in the HUD.

### UI classes

- **`GameFrame`**: the `JFrame`, holds the `CardLayout`.
- **`GamePanel`**: owns the game loop timer. Each frame it does two things:
  - **`update()`:**
    - works out the seconds since the last frame using `System.nanoTime()`,
    - reads `KeyboardInput` and steps the active rescuer if `canStep()`,
    - calls `tick()` on every rescuer,
    - passes the elapsed time to `TurnManager`.
  - **`paintComponent()`:**
    - draws the tiles inside the camera window,
    - draws survivors (revealed only) and rescuers,
    - draws the fog: black for unexplored tiles, a dark see-through layer for remembered ones,
    - draws the minimap.
- **`Camera`**:
  - `follow(Rescuer r, int mapRows, int mapCols)` sets `offsetRow` and `offsetCol` so the rescuer is centred, clamped between 0 and `mapSize - viewSize`.
  - Worked example: map 40 wide, view 15 wide, rescuer at col 20 → `offsetCol = 20 - 7 = 13`. A tile at col 25 is drawn at `x = (25 - 13) * 64 = 768`.
- **`Minimap`** (implements `Drawable`): loops over the whole grid and draws a 4 px square per explored tile, then dots for the exit, rescuers and revealed survivors.
- **`KeyboardInput`** (a `KeyListener`): keeps the keys currently held down in a `HashSet<Integer>`. `isHeld(int keyCode)` lets `update()` ask "is W held?" every frame, which is what makes hold-to-walk work.
- **`HudPanel`**: shows the information from Section 6.
- **`MenuPanel`**, **`HowToPlayPanel`**, **`ResultPanel`**: the other screens.

### Asset classes

- **`AssetManager`**: loads all images once at startup into a `HashMap<String, BufferedImage>`. `getImage(key)` returns `null` if the image is missing, and never crashes.
- **`SoundManager`**: plays sound effects and loops music. If a sound file is missing, it does nothing and does not crash.

---

## 9. OOP Requirements (Do Not Break These)

| Pillar | Where it lives |
|---|---|
| **Encapsulation** | `Rescuer` keeps `health`, `specialCooldown` and `moveTimer` private. They only change through `takeDamage()`, `startCooldown()` and `tick()`, so no other class can set invalid values. `Survivor` does the same with `condition`. |
| **Inheritance** | `Medic`, `Engineer`, `DogHandler` extend `Rescuer`. `Floor`, `Wall`, `Rubble`, `Fire`, `Exit` extend `Tile`. `CrackedFloor` and `BurntFloor` extend `Floor` (two levels deep). |
| **Polymorphism** | Pressing Space calls `activeRescuer.specialAction(board)` and the right subclass version runs. The draw loop calls `tile.draw(...)` on every tile without checking its type. On an aftershock, `HazardManager` calls `tile.onAftershock(board)` on every tile: only `CrackedFloor` reacts. Fire burnout works the same way through `onWorldTick`. |
| **Abstraction** | `Tile` and `Rescuer` are abstract with abstract methods. `GameBoard` and `MovementCalculator` work with any `Tile` without knowing which kind it is. |

Other concepts we want visible in the code:
- An interface (`Drawable`, implemented by four unrelated classes)
- Composition (`GameBoard` has a grid, rescuers have a carried survivor, `GamePanel` has a `Camera`, a `Minimap` and a `KeyboardInput`)
- Enums (`GameState`, `Direction`)
- Collections and generics (`ArrayList`, `HashMap`, `HashSet`, `List<Survivor>`)
- A custom exception (`InvalidMoveException`)
- Recursion (`MovementCalculator`'s flood fill, used for sight and scent)
- Constructors calling `super(...)`
- `@Override` on every overridden method
- Method overloading (`moveTo(int row, int col)` and `moveTo(Tile target)`)

**Avoid:** long `instanceof` or `switch` chains to decide behaviour by type. If a behaviour depends on the type, it belongs in an overridden method in the subclass, like the hook methods on `Tile`. The only allowed switch on tile type is in `LevelLoader`.

---

## 10. Assets

### Rule

Sprites are the intended look. Download them or make them, and credit them. Every `draw()` method still falls back to a coloured rectangle (tiles) or a coloured circle with a letter (rescuers and survivors) when an image is missing. That way the game runs in Week 1 before any art exists, and a typo in a file name never crashes it.

Use a **top-down pixel-art** pack, so tiles and characters match.

### Images needed (PNG, 64 by 64 unless noted)

- Tiles: `floor`, `cracked_floor`, `burnt_floor`, `wall`, `rubble`, `fire` (optional second frame `fire2` for flicker), `exit`
- Rescuers: `medic`, `engineer`, `dog_handler`. Optional: 4 facing directions each, for example `medic_up`, `medic_down`.
- Survivor: `survivor`, plus a small icon to show a rescuer is carrying someone
- UI: `heart`, rescuer portraits for the HUD, menu background, game logo, button images (optional)
- Drawn in code, not as images: the fog, the minimap, the cooldown bars, the active rescuer outline, the faced-tile highlight

### Sounds needed (WAV)

- Effects: button click, switch rescuer, footsteps, clear rubble, heal, dog bark (scent), pick up survivor, survivor rescued, fire spreading, aftershock warning, aftershock rumble, floor collapsing, rescuer damaged, rescuer downed, mission success, mission failure
- Music: menu loop, gameplay loop

### Technical notes

- Java Sound plays **WAV** natively but **not MP3 or OGG**. Convert audio to WAV (16-bit, 44.1 kHz or 22.05 kHz) with Audacity or any converter.
- Music WAV files get large. Keep loops short (30 to 60 seconds) and consider mono at 22.05 kHz.
- Load files from the classpath with `getClass().getResource("/images/floor.png")` so they work both in the IDE and in a JAR. In IntelliJ, mark the `resources` folder as a Resources Root.
- Load every asset once at startup, never inside `paintComponent`.
- Only draw tiles inside the camera window, roughly 165 tiles instead of 1200. That keeps the game smooth.

### Licensing

Free sources include Kenney.nl (CC0), OpenGameArt.org, itch.io free asset packs, and Freesound.org. Check each license before use and record every asset's source, author, and license in `CREDITS.md`.

---

## 11. Build Order (4 Weeks)

Every milestone must compile and run. Build a thin working version first, then add to it.

### Week 1: Walk around
- Create all packages and class skeletons
- `Tile` hierarchy with fallback colours
- `LevelLoader` builds Level 1 from `level1.txt`
- `GamePanel` with the game loop timer, `KeyboardInput`, hold-to-walk for one rescuer
- `Camera` follows the rescuer
- **Done when:** you can walk one rescuer around the 30 × 20 Level 1 with the view scrolling, and walls block you.

### Week 2: Fog, minimap, team
- `MovementCalculator.computeSight` (recursive), explored and remembered tiles, fog drawing
- `Minimap`
- All three rescuers, switching with 1/2/3/Tab
- All three special actions with cooldowns, `InvalidMoveException` messages in the HUD
- **Done when:** you can explore with all three rescuers, clear rubble, use scent, and the minimap fills in as you explore.

### Week 3: Changing world and goals
- `TurnManager` world tick in the Section 4.2 order
- Fire spread, burnout, fire damage
- Aftershocks with `CrackedFloor` collapse and the HUD countdown
- Survivors: reveal, pick up, carry, deliver, condition, Medic healing
- Rescuer health and the downed state
- End conditions and `ScoreKeeper`
- **Done when:** Level 1 can be won and lost from start to finish, and the cracked shortcut collapses on the first aftershock.

### Week 4: Content and polish
- Levels 2 and 3 (`.txt` files)
- Menu, How to Play, Pause, and Result screens
- `AssetManager` and `SoundManager` with real sprites and sounds
- Optional: smooth sliding between tiles instead of snapping, and screen shake
- Testing and bug fixing
- **Done when:** a new player can go from the menu through all three levels without crashes.

---

## 12. Team Split

| Member | Owns |
|---|---|
| Member 1 | `Tile` hierarchy (including `CrackedFloor` and `BurntFloor`), `GameBoard`, `LevelLoader`, `LevelData`, the level `.txt` files, `AssetManager`, `Camera`, `Minimap` |
| Member 2 | `Rescuer` hierarchy, `MovementCalculator` (sight, scent, `canStep`), special actions, `Survivor`, `KeyboardInput` |
| Member 3 | `GamePanel` game loop, `TurnManager`, `HazardManager`, `ScoreKeeper`, `HudPanel` and the other panels, `SoundManager` |
| Everyone | Testing and bug fixing in Week 4, finding assets |

Agree on method signatures in Week 1 so members can work in parallel without breaking each other's code. These ones matter most:
- the `Tile` hook methods,
- `Rescuer.tick(double)`,
- `MovementCalculator.canStep(...)`,
- `TurnManager.update(double seconds)`.

---

## 13. Cut List (If Time Runs Out)

Cut from the top first:
1. Smooth animation and screen shake (snap from tile to tile)
2. Sound and music
3. Levels 2 and 3 (ship Level 1 only)
4. How to Play and Pause screens
5. Fire burning out (fire stays forever)
6. Remembered (dimmed) tiles: use only "visible" and "black"
7. Survivor condition ticking (survivors never expire)

**Never cut:**
- the `Tile` and `Rescuer` hierarchies,
- at least two rescuers with different special actions,
- switching between rescuers,
- the camera, fog and minimap,
- the recursive flood fill,
- aftershocks collapsing cracked floor,
- a win and lose condition.

These are what we are graded on and what the teacher asked for.

---

## 14. Out of Scope

- Enemies, combat, shooting, weapons
- Randomly generated maps
- Saving or loading a mission in progress
- Multiplayer or networking
- Databases
- Diagonal movement
- AI-controlled teammates (rescuers you aren't controlling just stand still)

Extras we may add only after everything in Section 11 works:
- a level select screen,
- a fourth rescuer (Firefighter, who puts out the fire tile they are facing),
- high scores saved to a text file,
- character sprites for each facing direction.

---

## 15. How to Work With Us (Instructions for the AI)

- We are 2nd-year students using Java for the first time. **Explain what each piece of code does and why**, especially anything new to beginners (the game loop, `System.nanoTime()`, key listeners, recursion).
- Build in small steps that match the milestones in Section 11. After each step, the project should compile and run.
- Keep methods short and readable. Use clear names. Add a short comment at the top of every class explaining its job.
- Every field is `private` (or `protected` in an abstract parent when subclasses need it). Use getters and setters only when needed.
- Always use `@Override` when overriding.
- Do not add libraries, frameworks, or design patterns we did not ask for.
- Do not merge the class hierarchies into one class with a type field or a big switch. The hierarchies are the point of the project.
- Do not rename the classes in Section 8 without asking, since they are in our submitted proposal.
- If a rule or number in Section 4 turns out to be unbalanced or unclear during testing, point it out and suggest a change instead of silently changing it.
- When code changes game state, show what the state was before, the code, and what the state is after, so we can follow it.
