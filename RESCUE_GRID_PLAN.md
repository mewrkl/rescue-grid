# Rescue Grid: Game Plan (v3)

This document describes the game we are building, its rules, its class design, and how we want to build it. Read all of it before writing any code. If something here is unclear or seems wrong, ask before changing the design.

> **What changed in v3 (2026-11-09): Swing → libGDX.** Our teacher asked us to use libGDX and libraries instead of Swing. The game rules, class names and OOP design are unchanged; only the window, drawing, input, sound and screens moved to libGDX. See section 3 and `decisions/0004`.

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

**All game logic, rules, features, and mechanics must be written by us in Java.** This includes the grid, movement, the game loop logic, where the camera looks, fog, the minimap, hazards, scoring, and the win and lose conditions. libGDX gives us the window, drawing, keyboard, sound and UI widgets; it does not play the game for us.

**Visual and audio assets can come from anywhere.** Sprites, tile art, character art, backgrounds, UI images, sound effects, and music may be downloaded or made with any tool, and they are the intended look of the game. Every `draw()` method keeps a simple colored fallback anyway. That lets us test in Week 1 before art exists, and a missing file never crashes the game (see Section 10).

---

## 2. Game Summary

Rescue Grid is a real-time, top-down rescue game set inside a building that collapsed after an earthquake. The player controls a team of three rescuers with different skills and switches between them at any time. They explore the dark, damaged building, find trapped survivors, and carry them to the exit. Meanwhile fire spreads and aftershocks change the layout of the building.

The theme is based on real events. Cebu was hit by a magnitude 6.9 earthquake on September 30, 2025, and the aftershocks that followed made damaged buildings dangerous to enter.

**Core idea:** you can't see the whole building, you can only control one rescuer at a time, and the building gets worse every few seconds. The route you used a minute ago may not be there any more.

**Not Soul Knight:** there are no enemies, no shooting, no weapons, and no randomly generated maps. Fire and aftershocks are the "enemies". Each of those features would roughly double the project.

---

## 3. Tech Stack and Constraints

Our teacher asked us to build on **libGDX** and use libraries rather than plain Swing (2026-11-09, see `decisions/0004`).

| Item | Choice |
|---|---|
| Language | Java 17 or later (the build compiles with `release 17`) |
| Framework | **libGDX 1.14.2**, desktop backend **LWJGL3** (opens the window, OpenGL, OpenAL) |
| Build | **Gradle 8.14** with the wrapper (`gradlew`), two modules: `core` (the game) and `lwjgl3` (the desktop launcher) |
| Screens and UI | **Scene2D**, libGDX's UI library: `Stage`, `Table`, `Label`, `TextButton` |
| Fonts | **FreeType** extension (`gdx-freetype`): two pixel fonts: **Press Start 2P** (wide, chunky) for titles and menus, **Jersey 10** (narrower, readable) for sentences and the HUD |
| Drawing | `SpriteBatch` for images and `ShapeRenderer` for the placeholder shapes, both wrapped in our `Painter` class |
| Camera | libGDX `OrthographicCamera` (y-down, so row 0 is at the top), steered by our `Camera` class |
| Game loop | libGDX calls `render(delta)` about 60 times a second |
| Images | PNG, loaded as libGDX `Texture`s |
| Audio | WAV, played with libGDX `Sound` (effects) and `Music` (looping tracks) |
| Levels | `.txt` files in `assets/levels/` |
| Tests | **JUnit 5**, `gradlew test` (tests the rules without opening a window) |
| IDE | IntelliJ IDEA, opened as a Gradle project |
| Version control | Git and GitHub |

**Possible extras, not built:**
- **box2dlights:** a dark building with a light around each rescuer and glowing fires. It would suit the fog well. Kept as a "potential" until the core game is finished.
- **Tiled maps (`.tmx`):** levels painted in the free Tiled editor and loaded with `TmxMapLoader`. Worth it once the real sprites give us a tileset to paint with. Until then we keep the `.txt` levels.

**Do not use:**
- Other engines or frameworks on top of libGDX.
- libGDX's ready-made game-logic libraries: **Ashley** (entity system), **gdx-ai** (AI and pathfinding), **Box2D** physics. They would replace the class hierarchies, rules and recursion we are graded on.
- Databases, networking, or multiplayer.
- Threads. libGDX calls `render()` on one thread, so the whole game is single-threaded and we never have to think about thread safety.
- Design patterns we have not learned yet (our teacher will introduce them later). Keep the structure to plain classes, inheritance, interfaces, and composition.

**The game loop:** libGDX calls `GamePanel.render(delta)` about 60 times a second, where `delta` is the seconds since the last frame. `render` first calls `update(delta)`, which moves everything that changes over time (movement, cooldowns, fire, aftershocks, survivor condition), then draws the new picture. No other timers.

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
| CrackedFloor | Yes | No | **Always** collapses into Rubble on every aftershock. Placed on purpose across shortcuts. It doesn't burn, so fire can't remove the shortcut before the quake does |
| Wall | No | No | Never changes |
| Rubble | No | No | The Engineer can clear it into Floor |
| Fire | No | – | Spreads every world tick. Hurts rescuers next to it. Burns out after 6 ticks *(tune)* |
| BurntFloor | Yes | No | What fire leaves behind. Never burns again |
| Exit | Yes | No | Deliver survivors here |

### 4.2 Time: the world tick

The game runs in real time, but the **world** only changes on a **world tick** every **3 seconds** *(tune)*. The world tick takes the place of the old End Turn. On each tick, `TurnManager` runs these steps **in exactly this order**:

1. **Fire damage:** every active rescuer orthogonally next to a Fire tile takes 1 damage.
2. **Fire spreads:** each Fire tile tries to spread to each orthogonal neighbour that burns (plain Floor), with a 25% chance each *(tune)*. Fire never spreads onto a tile with a rescuer on it.
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

| Rescuer | Special action (E) | Cooldown |
|---|---|---|
| Medic | Heals the survivor she is carrying, or a revealed survivor on the tile she is facing. Restores 8 condition, up to the max | 4 s *(tune)* |
| Engineer | Clears the Rubble tile he is facing into Floor | 3 s *(tune)* |
| Dog Handler | **Scent:** reveals every survivor within 8 walking steps. The scent follows corridors and passes through rubble, but not walls. Revealed survivors appear on the minimap. No target needed | 8 s *(tune)* |

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
- **Condition:** drops by 1 every world tick. It starts at 20 / 25 / 30 in Levels 1 / 2 / 3, so a survivor lasts 60 / 75 / 90 s if left alone *(tune)*. Bigger buildings need more search time. At 0 the survivor is lost. Carried survivors keep losing condition too, which is why the Medic matters.
- If fire spreads onto a survivor that is not being carried, that survivor is lost.

### 4.5 Aftershocks

- Each level has an aftershock countdown measured in world ticks: Level 1 every 15 ticks (45 s), Level 2 every 12 (36 s), Level 3 every 9 (27 s) *(tune)*.
- The HUD shows a bar and the seconds left. At 5 s left (the warning):
  - the bar and number turn orange, the screen shakes slightly, and a warning rumble plays;
  - the random floor tiles that will collapse are chosen now, and every tile about to collapse (cracked floor and the chosen ones) pulses orange on the map, the way Into the Breach marks tiles before they are hit;
  - a faint orange glow pulses at the edges of the map.
  Orange is used for danger that is coming; red stays for damage. Added 2026-11-09 after playtesting.
- When an aftershock happens:
  1. **Every CrackedFloor tile becomes Rubble.** This is the designed path-blocker.
  2. The up to 3 Floor tiles chosen at the warning become Rubble *(tune)*. They are chosen from tiles with no rescuer or survivor on them and not next to the Exit. A chosen tile that caught fire in the meantime stays fire, and one where a survivor was put down doesn't collapse.
  3. Every active rescuer takes 1 damage.
  4. The countdown resets.
- A cracked tile with a rescuer standing on it still collapses. The rescuer is pushed to a free neighbouring walkable tile, or takes 1 extra damage if there is none.
- **Rubble never traps a level for good.** The Engineer can always clear it. A blocked path costs time, it is never a dead end.

### 4.6 End Conditions

The end check runs every frame. The mission ends when:
- the team **leaves through the exit**: once the target is reached, the active rescuer steps onto the Exit with empty hands (added 2026-11-09, see `decisions/0005`),
- no survivors are left in the building (all rescued or lost), or
- all rescuers are downed.

Leaving is refused before the target is reached; the HUD then says how many more are needed. After reaching the target the player chooses: leave now, or keep searching for the "everyone rescued" bonus.

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

Levels are text files, one character per tile, stored in `assets/levels/level1.txt`, `level2.txt`, `level3.txt`.
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
| `S` | Survivor who is always there (on Floor) |
| `?` | Possible survivor spot: the game picks some of these at random each time the level starts; the rest are Floor |
| `M` | Medic start (on Floor) |
| `E` | Engineer start (on Floor) |
| `D` | Dog Handler start (on Floor) |

`LevelLoader` should throw a clear exception if:
- the rows have different lengths,
- the outer border is not all `#`,
- there is not exactly one exit, or
- any rescuer start is missing, or
- there are not enough `?` spots to reach the level's survivor count.

**Random survivor spots (added 2026-11-09, see `decisions/0006`):** each level has one fixed `S` (the survivor behind rubble, so the Engineer always matters) and several `?` spots. `LevelLoader` shuffles the `?` spots with `Collections.shuffle` and fills as many as the level needs. The building stays the same, but who is where changes every play. The tests pass a seeded `Random`, so they always get the same spots.

The border rule means the camera and the recursive flood fill can never walk off the map.

### Level 1 (30 wide, 20 tall, 3 survivors, target 2)

```
##############################
#........#..........#.......X#
#..S.....#........?.#........#
#........#.....F....C........#
#........#.....F....C........#
#........#...?......#........#
####R#####..........#........#
#........#..........######.###
#........#..........#........#
#...................#....?...#
#........#..........#........#
#.?......#####.######........#
#........#..........#........#
#........#..........###.######
#........#......?...#........#
#........#..........#........#
#..M.E.D............#.....?..#
#........#...................#
#........#..........#........#
##############################
```

**What this level teaches** (positions are `row,col`, counting from 0):
- **The Engineer matters.** The storeroom in the top left (survivor at `2,3`) has only one door, and it is Rubble (`6,4`).
- **The quake changes your route.** The fastest way from the burning central hall to the exit room is the cracked gap at `3,20` and `4,20`. When the first aftershock hits, the gap becomes rubble.
- **Survivors:** the storeroom survivor (`S`) is always there. The other two are picked from six `?` spots: the central hall next to the fire (`5,13`), the top of the hall (`2,18`), the lower hall (`14,16`), the west room (`11,2`), and the east wing (`9,25`, `16,26`).

- The storeroom survivor can't be reached at all until the Engineer clears `6,4`.
- Every one of the 15 possible picks was checked with a route script. The slowest pick needs about 27 s of pure movement to rescue the target of 2. Each survivor lasts 60 s.

**Every level follows the same rules.** Each level must have:
- at least one cracked-floor shortcut, plus a longer backup route,
- at least one fixed survivor (`S`) that only the Engineer can reach,
- at least one survivor in a side room that is easy to miss without the Dog Handler, and
- every survivor still reachable after every cracked tile has collapsed (counting rubble as clearable).

### Level 2 (35 × 25, 4 survivors, target 3): "Office floor"

The map is in `assets/levels/level2.txt`.
- **The shortcut:** a central corridor runs from the start straight to the exit, but a 3 × 3 block of cracked floor sits across its middle.
- **After the quake:** the backup route climbs through the north-west room into the north corridor, then comes back down east of the crack.
- **The Engineer:** the north-east room's only door is rubble.
- **Side rooms:** the two south-side rooms are reached through other rooms, and a fire sits right next to the south-east survivor.

Start to exit is 29 tiles with the shortcut and 51 without it.

**Survivors:** the north-east room's survivor is fixed; 3 more are picked from 6 spots (the north-west and north rooms, the west rooms, the south-east room, and the south room behind rubble).

### Level 3 (40 × 30, 5 survivors, target 4): "Hospital wing"

The map is in `assets/levels/level3.txt`.
- **The shortcut:** the exit hall is at the top. The quickest way up from the ward is a two-tile cracked gap.
- **After the quake:** the backup route goes round through the east room, nearly doubling the trip (32 tiles → 60).
- **The Engineer:** the west room is behind rubble. More rubble doors connect the start room to the side wings as shortcuts.
- **Pressure:** this level has four fires and the fastest aftershocks (every 27 s).
- **Survivors:** the west room's survivor is fixed; 4 more are picked from 7 spots across the wings, the big hall and the ward rooms.

All three levels were checked with a route script against **every possible pick** of survivor spots (15, 20 and 35 picks): every spot can be reached, also after the quake, and every rule above holds. For the slowest pick it estimates about 27 s, 44 s and 52 s of movement to rescue the target number, against 60, 75 and 90 s of survivor time.

---

## 6. Screens and Controls

### Screens

1. **Main Menu:** title, Start, How to Play, Quit. Menu music plays.
2. **How to Play:** the rescuers, the tiles (with pictures), fog, aftershocks, and the controls.
3. **Game Screen:** the camera view on the left, the HUD panel on the right, and the minimap in the top-right corner of the camera view.
4. **Pause overlay** (Esc): Resume, Restart Level, Main Menu.
5. **Result Screen:** Success or Failure, survivors rescued, score breakdown, and buttons for Next Level, Retry, and Main Menu.

`GameFrame` (a libGDX `Game`) switches screens with `setScreen(...)`.

**Menu style (2026-11-09, from researching game title screens):**
- Everything centred: a big pixel-font title, a one-line tagline, then a short list of options (3 on the title screen).
- The selected option is always visible: bigger, in the highlight colour, with `>` and `<` beside it. Up/Down or W/S moves, Enter or Space chooses, Esc goes back, and the mouse works too.
- The title screen shows the real game behind the menu (a dimmed piece of Level 1, fire flickering) and the three rescuers under the title, a small nod to Soul Knight's lobby where the heroes stand around.
- How to Play is three columns instead of paragraphs: the controls as keyboard keys, the team with their pictures, and the building's tiles with one line each. Players skip long text.
- A small grey line at the bottom of each menu says which keys work.

### Camera view and minimap

- The camera view is **15 × 11 tiles** (960 × 704 px), centred on the active rescuer and clamped at the map edges.
- The **minimap** draws the whole map at 4 px per tile:
  - explored tiles in their colour, unexplored tiles black,
  - the Exit in green,
  - all three rescuers as coloured dots (the active one larger),
  - revealed survivors as blinking dots.

### HUD contents

Reworked 2026-11-09 after the first playtest. Research on game HUDs pointed the same way: small counts read faster as pictures than as sentences, show things only when they matter, and never say the same thing twice.

- **Top:** level number and elapsed time on one line.
- **AFTERSHOCK:** a bar that empties as the quake gets closer, with the seconds left. Both turn orange in the last 5 s.
- **SURVIVORS:** one box per survivor. A saved survivor fills a box with their picture, and a lost one is a red cross. Boxes for the survivors you must save are solid, the optional ones are dashed. Under them: "Save 1 more", then "Leave by the EXIT" in the accent colour.
- **TEAM:** number key, picture, name and health pips for each rescuer. The active one's number is in the accent colour; a downed one says "down" in red.
- **ABILITY:** the E keycap, the ability name, and a bar that refills while it recharges.
- **Carrying:** the carried survivor with their condition bar, shown only while carrying.
- **Messages** about things that just happened (an invalid action, someone burning, a survivor lost) appear for 3 s in a small box at the bottom of the map, not in the sidebar. Nothing the sidebar already shows is repeated there.

### Controls

| Input | Action |
|---|---|
| WASD / arrow keys | Walk (hold to keep walking). Also turns to face that direction |
| 1, 2, 3 or Tab | Switch to Medic / Engineer / Dog Handler, or cycle to the next |
| E | Special action on the faced tile (the Dog Handler needs no target) |
| Esc | Pause |

**Why E (changed from Space on 2026-11-09):** in PC games Q and E are the usual ability keys, and Space usually means jump or dash. Genshin Impact is the closest match to our design: a team switched with the number keys, and E for the active character's skill.

Invalid special actions (on cooldown, nothing valid on the faced tile) show a short message in the HUD and change nothing.

---

## 7. Package Structure

```
settings.gradle, build.gradle, gradle.properties   (Gradle build: versions live in gradle.properties)
gradlew, gradlew.bat, gradle/wrapper/              (run Gradle without installing it)
core/                                              (the whole game, no desktop code)
  build.gradle
  src/main/java/rescuegrid/
    ui/
      GameFrame.java        (extends libGDX Game: creates shared things, switches screens)
      BaseScreen.java       (new: what every screen shares: Stage, root Table, keyboard buttons)
      MenuPanel.java
      HowToPlayPanel.java
      GamePanel.java        (the game loop: update + draw)
      HudPanel.java         (extends Scene2D Table)
      ResultPanel.java
      Camera.java
      Minimap.java
      KeyboardInput.java
      UiStyle.java          (colours, FreeType fonts, Scene2D button and label styles)
      MenuList.java         (new: the centred, keyboard-and-mouse game menu)
      IconSlot.java         (new: a space in a menu where game art is drawn)
      HealthBar.java        (new: a Scene2D Actor that draws health squares)
      SurvivorPip.java      (new: one survivor box in the HUD, implements Drawable)
      CarriedSurvivor.java  (new: draws the carried survivor in the HUD, implements Drawable)
      Toast.java            (new: a short message over the map that fades out)
    graphics/
      Painter.java          (new: draws images and shapes for every Drawable)
    model/
      GameBoard.java, Survivor.java, Drawable.java, GameState.java, Direction.java
      tiles/
        Tile.java, Floor.java, CrackedFloor.java, BurntFloor.java,
        Wall.java, Rubble.java, Fire.java, Exit.java
      rescuers/
        Rescuer.java, Medic.java, Engineer.java, DogHandler.java
    logic/
      TurnManager.java      (runs the world tick)
      HazardManager.java
      GameEvent.java        (what happened in a tick, for sounds and messages)
      MovementCalculator.java
      LevelLoader.java
      LevelData.java
      ScoreKeeper.java
      InvalidMoveException.java
    assets/
      AssetManager.java
      SoundManager.java
  src/test/java/rescuegrid/logic/                  (JUnit tests for the rules)
lwjgl3/                                            (desktop launcher)
  build.gradle
  src/main/java/rescuegrid/Main.java               (opens the window)
assets/                                            (shared by both modules)
  levels/  level1.txt, level2.txt, level3.txt
  sounds/  music/  images/  fonts/
tools/
  SoundMaker.java           (our own synthesizer: generates every WAV, not part of the game)
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
  - `Color getFallbackColor()` (a libGDX `Color`)
  - `String getImageKey()`
- Hook methods with a default "do nothing / false" body. Subclasses override only the ones they need.
  - `boolean isClearable()`: only `Rubble` returns `true`. The Engineer checks this.
  - `boolean isBurning()`: only `Fire` returns `true`. Fire damage and fire spread check this.
  - `boolean isExit()`: only `Exit` returns `true`. Delivering survivors checks this.
  - `boolean canCollapse()`: only `Floor` returns `true`. Random aftershock collapses pick from these tiles.
  - `void onAftershock(GameBoard board)`: `CrackedFloor` replaces itself with `Rubble`.
  - `void onWorldTick(GameBoard board)`: `Fire` ages by one and replaces itself with `BurntFloor` at age 6.
- Concrete method: `draw(...)` uses the image from `AssetManager`. If the image is missing, it calls `drawFallback(...)`, the placeholder art made of shapes. Each subclass overrides `drawFallback` with its own details: bricks for `Wall`, rocks for `Rubble`, flickering flames for `Fire`, cracks for `CrackedFloor`.
- Subclasses:
  - `Floor`, `Wall`, `Rubble`, `Fire`, `Exit` extend `Tile`.
  - `CrackedFloor` and `BurntFloor` extend **`Floor`**, which is two-level inheritance.
  - `CrackedFloor` overrides `onAftershock`, `isFlammable`, `canCollapse` (both `false`) and `getImageKey`.
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
  - `pickUp(Survivor s)`, `Survivor dropSurvivor()`, `Survivor deliverSurvivor()`
  - `moveTo(int row, int col)` and `moveTo(Tile target)`: this is the overloading example
  - `Tile getFacedTile(GameBoard board)`
  - `double getDrawRow()` and `getDrawCol()`: where to draw the rescuer mid-step. This gives smooth sliding, for example 5.5 halfway between rows 5 and 6.
- Abstract methods:
  - `String specialAction(GameBoard board) throws InvalidMoveException`. Each subclass finds its own target, usually `getFacedTile(board)`, and returns a short message for the HUD.
  - `String getSpecialName()`, `String getSpecialSound()`, `double getCooldownSeconds()`
  - `String getName()`, `String getImageKey()`, `Color getFallbackColor()`
  - `void drawRoleIcon(...)`: each rescuer draws its own placeholder symbol (a cross, a hard hat or a paw).
- Subclasses: `Medic`, `Engineer`, `DogHandler`

### Interface

**`Drawable`**
- `void draw(Painter painter, float x, float y, float size, AssetManager assets)`
- `Painter` hides libGDX's two drawing tools (`SpriteBatch` for images, `ShapeRenderer` for shapes) behind simple methods: `image`, `fillRect`, `fillOval`, `fillPolygon`, `line`, `text`. Coordinates are y-down, like the level file.
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
- **`GameEvent`**: a small class with a `Type` enum (`RESCUER_HURT`, `AFTERSHOCK`, ...) and a message.
  - The logic classes only *record* what happened during a tick.
  - `GamePanel` then turns each event into a sound, a HUD message and screen shake.
  - This keeps sound and drawing code out of the game rules.
- **`LevelLoader`**: reads a `.txt` file into a `String[]`, then turns it into a `GameBoard`. Uses a `switch` on characters to build tile objects. This is the only place where tile types are chosen with a switch.
  - `load(LevelData level)` / `load(LevelData level, Random random)` and `parse(String[] rows)` / `parse(String[] rows, int survivorCondition, int survivorCount, Random random)` are more overloading examples.
  - Picks which `?` spots get a survivor (shuffle, then take as many as needed).
- **`LevelData`**: per-level settings (file name, survivor count, target, aftershock ticks, survivor condition, random seed).
- **`ScoreKeeper`**: calculates the score from Section 4.7.
- **`InvalidMoveException`**: custom checked exception, thrown by special actions that can't happen (on cooldown, no valid target). The UI catches it and shows the message in the HUD.

### UI classes

- **`GameFrame`** (extends libGDX `Game`): creates the shared `AssetManager`, `SoundManager`, `UiStyle` and `Painter` once, and switches screens with `setScreen(...)`. The name is kept from the proposal; in the Swing version it was the `JFrame`.
- **`BaseScreen`** (abstract, extends libGDX `ScreenAdapter`): each screen has a Scene2D `Stage` and a root `Table`. `MenuPanel`, `HowToPlayPanel`, `GamePanel` and `ResultPanel` extend it.
- **`GamePanel`**: libGDX calls its `render(delta)` each frame. It does two things:
  - **`update(delta)`:**
    - reads `KeyboardInput` and steps the active rescuer if `canStep()`,
    - calls `tick()` on every rescuer,
    - passes the elapsed time to `TurnManager`, then turns its `GameEvent`s into sounds, messages and shake.
  - **Drawing:**
    - draws the tiles inside the camera window,
    - draws survivors (revealed only) and rescuers,
    - draws the fog: black for unexplored tiles, a dark see-through layer for remembered ones,
    - draws the minimap, then the HUD and pause card (Scene2D).
- **`Camera`**:
  - `follow(row, col, mapRows, mapCols)` centres the view on the rescuer's draw position, clamped at the map edges.
  - It works in pixels, so it glides along while the rescuer slides between tiles. It then points a libGDX `OrthographicCamera` at that spot, which does the screen maths.
  - Worked example: the view is 15 tiles wide (960 px) and the rescuer is at col 20. Then `offsetX = 20 × 64 − (960 − 64) / 2 = 832`, and a tile at col 25 appears at screen `x = 25 × 64 − 832 = 768`.
- **`Minimap`** (implements `Drawable`): loops over the whole grid and draws a 4 px square per explored tile, then dots for the exit, rescuers and revealed survivors.
- **`KeyboardInput`** (extends libGDX `InputAdapter`): keeps the keys currently held down in a `HashSet<Integer>`. `isHeld(int keycode)` lets `update()` ask "is W held?" every frame, which is what makes hold-to-walk work.
- **`HudPanel`** (extends Scene2D `Table`): shows the information from Section 6. **`HealthBar`** (extends Scene2D `Actor`) draws the health squares. The timer bars are Scene2D's own `ProgressBar`. **`SurvivorPip`** and **`CarriedSurvivor`** implement `Drawable`, so the HUD draws them in icon slots like any tile.
- **`Toast`** (extends Scene2D `Label`): a message at the bottom of the map that fades out with Scene2D actions (`delay`, then `fadeOut`).
- **`MenuPanel`**, **`HowToPlayPanel`**, **`ResultPanel`**: the other screens.
- **`UiStyle`**: colours, the two FreeType fonts, keycaps and label styles.
- **`MenuList`** (extends Scene2D `Table`): the game menu. It keeps track of the selected option and handles both keyboard and mouse.
- **`IconSlot`** (extends Scene2D `Actor`): an empty space in a layout. After the UI is drawn, the screen draws a tile or rescuer there with the same `draw()` the game uses.

### Asset classes

- **`AssetManager`**: loads all images once at startup into a `HashMap<String, TextureRegion>`. `getImage(key)` returns `null` if the image is missing, and never crashes. (Not libGDX's own class of the same name: ours is from the proposal.)
- **`SoundManager`**: plays sound effects (libGDX `Sound`) and loops music (libGDX `Music`). If a sound file is missing, it does nothing and does not crash.
- Both implement libGDX's `Disposable`: textures and sounds live outside Java's normal memory, so `GameFrame.dispose()` frees them when the window closes.

---

## 9. OOP Requirements (Do Not Break These)

| Pillar | Where it lives |
|---|---|
| **Encapsulation** | `Rescuer` keeps `health`, `specialCooldown` and `moveTimer` private. They only change through `takeDamage()`, `startCooldown()` and `tick()`, so no other class can set invalid values. `Survivor` does the same with `condition`. |
| **Inheritance** | `Medic`, `Engineer`, `DogHandler` extend `Rescuer`. `Floor`, `Wall`, `Rubble`, `Fire`, `Exit` extend `Tile`. `CrackedFloor` and `BurntFloor` extend `Floor` (two levels deep). |
| **Polymorphism** | Pressing E calls `activeRescuer.specialAction(board)` and the right subclass version runs. The draw loop calls `tile.draw(...)` on every tile without checking its type, and each tile's own `drawFallback` paints bricks, rocks or flames. On an aftershock, `HazardManager` calls `tile.onAftershock(board)` on every tile: only `CrackedFloor` reacts. Fire burnout works the same way through `onWorldTick`. |
| **Abstraction** | `Tile` and `Rescuer` are abstract with abstract methods. `GameBoard` and `MovementCalculator` work with any `Tile` without knowing which kind it is. |

**Using the library's classes the OOP way:** our screens inherit from libGDX (`GameFrame extends Game`, `BaseScreen extends ScreenAdapter`, and the four screens extend `BaseScreen`), `HudPanel extends Table`, `HealthBar extends Actor` and overrides `draw`, and `KeyboardInput extends InputAdapter`. libGDX then calls our overridden methods (`render`, `keyDown`, `draw`) without knowing our classes, which is polymorphism from the other side.

Other concepts we want visible in the code:
- An interface (`Drawable`, implemented by four unrelated classes; we also implement libGDX's `Disposable`)
- Composition (`GameBoard` has a grid, rescuers have a carried survivor, `GamePanel` has a `Camera`, a `Minimap` and a `KeyboardInput`)
- Enums (`GameState`, `Direction`, `GameEvent.Type`)
- Collections and generics (`ArrayList`, `HashMap`, `HashSet`, `List<Survivor>`)
- A custom exception (`InvalidMoveException`)
- Recursion (`MovementCalculator`'s flood fill, used for sight and scent)
- Constructors calling `super(...)`
- `@Override` on every overridden method
- Method overloading (`moveTo(int row, int col)` and `moveTo(Tile target)`; `LevelLoader.load(...)` and `LevelLoader.parse(...)` with different parameters)

**Avoid:** long `instanceof` or `switch` chains to decide behaviour by type. If a behaviour depends on the type, it belongs in an overridden method in the subclass, like the hook methods on `Tile`. The only allowed switch on tile type is in `LevelLoader`.

---

## 10. Assets

### Decisions (2026-11-08, see `decisions/0003`)

| What | Decision | Status |
|---|---|---|
| Art style | **2D top-down pixel art, 16 × 16 sprites drawn at 4× (64 px).** 3D would multiply the art work even in libGDX, so 2D pixel art it is. | Decided |
| Placeholder art | Shapes drawn in Java (`drawFallback`): textured floor, brick walls, rock piles, flickering flames, a cracked-floor pattern, an EXIT door, rescuers with role icons, survivors as figures with a condition bar. | **Built, used for the first showing** |
| Sprites | **Custom sprites made for us by Claude.** Each is written as a 16 × 16 grid of characters and turned into a PNG by a small Java tool (`tools/SpriteMaker.java`), the same idea as `SoundMaker`. No licences to track. | **Planned, not made yet. Wait for the team's go-ahead** |
| Sounds and music | Generated by our own Java synthesizer, `tools/SoundMaker.java` (the same idea as the sfxr/jsfxr retro sound tools). It makes 17 effects and 2 chiptune loops. Any track can be swapped for a downloaded CC0 one by replacing the WAV file. | **Built** |

**The rule still holds:** every `draw()` falls back to the placeholder art when an image is missing, and a missing sound is simply silent. Dropping a correctly named PNG into `assets/images/` switches that object to the sprite, with no code changes.

### Sprite plan (16 × 16, saved as PNG)

| Key | Look |
|---|---|
| `floor` | Grey-beige concrete, a few darker specks |
| `cracked_floor` | Same concrete with dark zig-zag cracks |
| `burnt_floor` | Charcoal floor with soot patches |
| `wall` | Dark blue-grey bricks with a lighter top edge |
| `rubble` | Pile of 4–5 grey-brown broken blocks |
| `fire`, `fire2` | Two frames of orange/yellow flames on a dark-red base (they alternate for flicker) |
| `exit` | Green door with a small white EXIT sign |
| `medic` | Person in white with a red cross, red cap |
| `engineer` | Person in an orange vest with a yellow hard hat |
| `dog_handler` | Person in blue with a small brown dog beside them |
| `survivor` | Person in a yellow shirt, one arm raised waving |
| `carry_icon` | Tiny survivor head in a yellow circle (shown on a rescuer who is carrying someone) |

Palette: muted greys and browns for the building; strong single colours for people (red, orange, blue, yellow) so they pop against the floor. Facing directions (`medic_up` etc.) are an extra, only if time allows.

### Sound list (all built, in `assets/sounds` and `assets/music`)

- **Interface:** `click`, `switch`, `denied`
- **Rescuers:** `step`, `bark` (Scent), `clear_rubble`, `heal`, `pickup`, `rescued`, `hurt`, `downed`
- **Building:** `fire_spread` (soft crackle), `aftershock_warning` (rising rumble), `aftershock` (crash and rumble), `survivor_lost`
- **End:** `success` (fanfare), `failure` (falling notes)
- **Music:**
  - `menu`: calm, about 46 s, A minor arpeggios with a melody on the second half.
  - `gameplay`: tense, about 29 s, 132 BPM, with a driving bass, fast arpeggios and drums. A lead line joins halfway.

To change a sound, edit its numbers in `tools/SoundMaker.java` and run `java tools/SoundMaker.java` from the project folder.

### Technical notes

- libGDX plays **WAV, OGG and MP3**. Our generated files are WAV. A downloaded OGG works too: change the extension in `SoundManager`.
- `Sound` loads an effect fully into memory (keep effects short). `Music` streams from the file (use it for long loops).
- Assets live in `assets/` and are loaded with `Gdx.files.internal("images/floor.png")`. Gradle runs the game with `assets/` as the working folder and also puts it on the classpath.
- Load every asset once at startup, never inside `render`. Textures, fonts and sounds must be `dispose()`d.
- Pixel art: textures use the `Nearest` filter so 16 × 16 sprites stay sharp when drawn at 64 × 64.
- Only draw tiles inside the camera window, roughly 190 tiles instead of 1200.
- Fonts: **Press Start 2P** for titles and menus (always at a multiple of 8 px, unsmoothed, so its pixels stay square) and **Jersey 10** for longer text, always at 19 px, the size where its pixels line up with the screen's. Both use the SIL Open Font License and live in `assets/fonts/`.

### Licensing

Our generated sounds and planned sprites are original, so there's nothing to license. If anything is ever downloaded instead, good free sources are Kenney.nl (CC0), OpenGameArt.org, itch.io free asset packs and Freesound.org. Check each licence before use, and record every asset's source, author and licence in `CREDITS.md`.

---

## 11. Build Order (4 Weeks)

Every milestone must compile and run. Build a thin working version first, then add to it.

> **Status (2026-11-09):** Weeks 1–4 are built on libGDX, except the sprites:
> - The whole game is playable, from the menu through all three levels, with placeholder art, sound and music.
> - 31 JUnit tests cover the rules (`gradlew test`).
> - Added after the first playtest: a survivor counter in the HUD, leaving through the exit once the target is reached, and random survivor spots.
> - Still to do: the sprites (waiting for the go-ahead), playtesting, and tuning the numbers marked *(tune)*.

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
| Member 3 | `GamePanel` game loop, `TurnManager`, `HazardManager`, `ScoreKeeper`, `HudPanel` and the other screens, `UiStyle`, `Painter`, `SoundManager` |
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

- We are 2nd-year students using Java for the first time. **Explain what each piece of code does and why**, especially anything new to beginners (the game loop and `delta`, libGDX screens and Scene2D, input processors, recursion).
- Build in small steps that match the milestones in Section 11. After each step, the project should compile and run.
- Keep methods short and readable. Use clear names. Add a short comment at the top of every class explaining its job.
- Every field is `private` (or `protected` in an abstract parent when subclasses need it). Use getters and setters only when needed.
- Always use `@Override` when overriding.
- Use libGDX and the libraries listed in Section 3. Ask before adding any other library, framework, or design pattern.
- Do not merge the class hierarchies into one class with a type field or a big switch. The hierarchies are the point of the project.
- Do not rename the classes in Section 8 without asking, since they are in our submitted proposal.
- If a rule or number in Section 4 turns out to be unbalanced or unclear during testing, point it out and suggest a change instead of silently changing it.
- When code changes game state, show what the state was before, the code, and what the state is after, so we can follow it.
