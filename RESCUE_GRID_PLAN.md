# Rescue Grid: Game Plan (v4)

This document describes the game we are building, its rules, its class design, and how we want to build it. Read all of it before writing any code. If something here is unclear or seems wrong, ask before changing the design.

> **What changed in v4 (2026-11-15), after the teacher's feedback on the first showing**
> - **One rescuer on the map who changes role** (1/2/3, a 1 s gear change) instead of three rescuers to manage. Nobody is left behind any more. See `decisions/0007`.
> - **Survivors have triage injury types:** minor (green) follow you, serious (yellow) are carried, critical (red) must be stabilised by the Medic and fetched by backup (Q). Any survivor can be trapped under debris until the Engineer frees them. See `decisions/0008` and `0009`.
> - **Better level flow:** you start beside the exit, survivors are grouped in rooms, survivors you haven't found call for help, an objective checklist shows what to do, and a **training level** comes before Level 1. See `decisions/0010`.
> - The time bonus is gone from scoring.
> - **After the hand playtests (2026-11-15/16):** survivors block the way and you walk into them (decision 0012); the training points at one thing at a time (0013) and lets you test each role's ability in the first room (0015); every level starts with a briefing card; the result screen gives **up to 3 stars** for three goals (0014); a **Level Select** keeps your best stars; found survivors warn you when they are running out of time; the radio gives a hint when you are stuck.
> - **Our own sprites (2026-11-16, decision 0016):** angled RPG view, rescuers walking in 4 directions, a dusty disaster palette, and themed furniture in every level (`Prop`, `DecoratedFloor`), all drawn by `tools/SpriteMaker.java`.
> - **After an audit against game-design standards (2026-11-16):**
>   - Stars are the only grade; the points total paid for doing nothing (decision 0018).
>   - Each role has its own gear, so it has its own outline (0019).
>   - You see backup arrive, as a stretcher team walking in from the EXIT (0020).
>   - Scent shows its range and E/Q are dimmed when they can't act (0021).
>   - The checklist and minimap moved into the HUD with labels, wall tops are a lighter stone cap that no longer reads as a hole, the title screen is calmer with the team at 4x, and a puff of dust covers the role change.
> - **Menus like real games (2026-11-16, decision 0024):** Continue + Play (the training is offered once), an Options screen (music and sound volume, fullscreen, reset progress), a real pause menu with "Are you sure?", one marker per survivor (0022), and an 11-step training (0023).
> - New classes: `RescueTeam`, `MinorSurvivor`, `SeriousSurvivor`, `CriticalSurvivor`, `ObjectiveTracker`, `Objective`, `Tutorial`, `ObjectivePanel`, then `BackupTeam`, `TeamLineup`, `OptionsPanel` and `TrainingPromptPanel` (2026-11-16). `Survivor` is now abstract; `ScoreKeeper` was removed (decision 0018).

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

Rescue Grid is a real-time, top-down rescue game set inside a building that collapsed after an earthquake. The player controls one rescuer who can change role at any time: Medic, Engineer or Dog Handler. Each survivor needs something different (walk them out, carry them, stabilise them and call backup, or free them from debris), so changing to the right role is the puzzle. They explore the dark, damaged building, find trapped survivors, and carry them to the exit. Meanwhile fire spreads and aftershocks change the layout of the building.

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

1. **Fire damage:** the rescuer on the map takes 1 damage if they stand orthogonally next to a Fire tile.
2. **Fire spreads:** each Fire tile tries to spread to each orthogonal neighbour that burns (plain Floor), with the level's spread chance: 8% in training, 15% / 20% / 25% in Levels 1 / 2 / 3 *(tune)*. Fire never spreads onto a tile with a rescuer on it.
3. **Fire burns out:** each Fire tile ages by 1. At age 6 it becomes BurntFloor.
4. **Survivor condition:** every survivor not yet rescued loses condition (1, 2 or 3 points, by injury type, see 4.4). Survivors at 0 are lost.
5. **Aftershock countdown:** drops by 1, and an aftershock happens if it reaches 0 (see 4.5).
6. **End check:** see 4.6.

The order matters. Damage is applied before fire spreads, so you are only hurt by fire you could already see.

**Beginner warning:** when changing tiles during a tick, first **collect** the changes in a list, then **apply** them all. If you change tiles while still looping over the grid, a tile that just caught fire could spread again in the same tick.

### 4.3 Rescuers

The team has three roles (decision 0007). **Only one is on the map at a time.** All share these stats:
- **Health:** 5 *(tune)*
- **Walk speed:** 1 tile every 0.15 s, or every 0.30 s while carrying a survivor *(tune)*

Press 1, 2, 3 or Tab to **change role**. The new role takes over the same spot, facing, and anyone being carried or led, after a **1 s gear change** *(tune)* during which you can't move or act. Each role keeps **its own health and cooldown**; cooldowns keep recharging while a role is off the map. Fire and aftershocks only hurt the role on the map, so changing to a healthy role is a way to survive. The HUD shows all three health bars.

| Rescuer | Special action (E) | Cooldown |
|---|---|---|
| Medic | Treats the survivor she is carrying or leading, or one on her tile or the tile she faces. Restores 16 condition points (8 ticks of a serious survivor), up to the max. **Treating a critical survivor also stabilises them** | 4 s *(tune)* |
| Engineer | Clears the Rubble tile he is facing into Floor, or **lifts the debris off a trapped survivor** on his tile or the tile he faces | 3 s *(tune)* |
| Dog Handler | **Scent:** reveals every survivor within 8 walking steps. The scent follows corridors and passes through rubble, but not walls. Revealed survivors appear on the minimap. No target needed | 8 s *(tune)* |

**Movement rules:**
- Holding a direction key keeps walking. The rescuer always turns to face the key you pressed, even when blocked, so you can face rubble to clear it.
- A rescuer can only step onto walkable tiles, and never onto a tile with another rescuer or a waiting survivor on it. Survivors are in the way like real people: walking into one helps them (decision 0012). Next to a survivor, a line under them says what they need.
- Walking into a wall just doesn't move you. It does **not** throw an exception, because that would happen 60 times a second while you hold a key into a wall.

**Any role: Call backup (Q).** Facing or standing on a stabilised critical survivor, any role can radio for backup (see 4.4).

**Downed roles:** when the role on the map reaches 0 health, the next standing role takes over at once, on the same tile, keeping whoever was being carried or led. A downed role can't be picked again this level. The mission fails when all three are down.

### 4.4 Survivors and fog

**Fog of war:**
- At the start, every tile is **unexplored** and drawn black.
- The active rescuer **sees** 4 walking steps around them *(tune)*. Sight goes through walkable tiles and stops at anything else, so you can see a wall, rubble or fire, but not what's behind it.
- Tiles you can see right now are drawn normally. Tiles you have seen before but can't see now are drawn **dimmed**, showing their current state.
- `MovementCalculator` computes what you can see with recursion (Section 8).

**Survivors** (decision 0008, based on START triage colours):

| Type | Tag | What you do | Drains per tick |
|---|---|---|---|
| `MinorSurvivor` | green | Walk into them: they **follow one tile behind you**. You keep full speed and free hands. One follower at a time | 1 point |
| `SeriousSurvivor` | yellow | Walk into them to **carry** them (half speed). One carried at a time, but you can carry one and lead one | 2 points |
| `CriticalSurvivor` | red | **Can't be moved.** The Medic treats them (E), which stabilises them. Then any role presses **Q** for backup, and they are rescued **10 s** later *(tune)* (decision 0009). You don't have to wait beside them. You see it happen (decision 0020): the rescuer raises a radio, a red flare by the survivor counts down, and a two-person stretcher team in paramedic green walks in from the EXIT (`BackupTeam`, breadth-first search) to arrive at 10 s, lifts them and carries them out ("Saved!"). The team is only a picture: the countdown decides the rescue | 3 points, 1 once stabilised |
| Trapped (any type) | debris drawn over them | The Engineer lifts the debris (E), then they behave as their type | as their type |

- Condition is counted in points: a survivor starts with `2 × the level's ticks` points, so a serious survivor lasts 20 / 25 / 30 ticks (60 / 75 / 90 s) in Levels 1 / 2 / 3, a minor one twice as long, and an unstabilised critical one two thirds as long *(tune)*. Carried and following survivors keep draining too.
- A survivor is **revealed** when it is inside the rescuer's sight, or found by the Dog Handler's scent. Once revealed, it stays revealed and shows on the minimap.
- **Calls for help:** a survivor nobody has found calls for help every 6 s *(tune)* while the rescuer is within 12 tiles (through walls: you can hear them). The call shows as an expanding ring and "HELP!" where they are, even in the fog, or as an arrow at the edge of the screen if they are off screen, plus a short sound.
- **Rescuing:** walking onto the Exit delivers the carried survivor and the follower at once.
- If fire spreads onto a survivor lying on the floor, that survivor is lost. Carried and following survivors are safe from fire.

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
  3. The rescuer on the map takes 1 damage.
  4. The countdown resets.
- A cracked tile with a rescuer standing on it still collapses. The rescuer is pushed to a free neighbouring walkable tile, or takes 1 extra damage if there is none.
- **Rubble never traps a level for good.** The Engineer can always clear it. A blocked path costs time, it is never a dead end.

### 4.6 End Conditions

The end check runs every frame. The mission ends when:
- the team **leaves through the exit**: once the target is reached, the active rescuer steps onto the Exit with empty hands (added 2026-11-09, see `decisions/0005`),
- no survivors are left in the building (all rescued or lost), or
- all three roles are downed.

Leaving is refused before the target is reached; the HUD then says how many more are needed. After reaching the target the player chooses: leave now, or keep searching for the "everyone rescued" star.

The mission is a **success** if the number rescued is at least the level's target. Otherwise it is a **failure**.

| Level | Size (approx.) | Survivors | Target | Aftershock every |
|---|---|---|---|---|
| Training | 34 × 14 | 4 | 4 | never (one planned aftershock at step 15) |
| 1 | 30 × 20 | 4 | 3 | 15 ticks |
| 2 | 35 × 25 | 5 | 4 | 12 ticks |
| 3 | 40 × 30 | 6 | 4 | 9 ticks |

### 4.7 Grading: stars only

**No points total (changed 2026-11-16, decision 0018).** The old score (100 per rescue, 20 per health point, 50 for everyone) paid as much for full health as for three rescues, so a failed run with nobody rescued still showed "TOTAL 300". `ScoreKeeper` was removed, and the stars below are the only grade. The result screen still shows the time taken. The time bonus had already gone on 2026-11-15: rushing out should never beat saving everyone.

**Stars (added 2026-11-16, decision 0014).** A finished level earns up to 3 stars, one for each goal. The goals are shown on the briefing card before the level and ticked off on the result screen:
1. Rescue the level's target (so every success has at least 1 star).
2. Rescue everyone.
3. No role goes down.

A failed mission earns no stars; training has none. The best stars per level are saved (`Progress`, `SaveFile`) and shown on the Level Select screen. `MissionReport` works them out.

---

## 5. Level Format

Levels are text files, one character per tile, stored in `assets/levels/tutorial.txt`, `level1.txt`, `level2.txt`, `level3.txt`.
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
| `S` | Serious survivor who is always there (on Floor) |
| `m` | Minor survivor who is always there |
| `!` | Critical survivor who is always there |
| `T` | Serious survivor trapped under debris |
| `1` `2` `3` | Training only: a taped-off door (`Barrier`) that the Tutorial opens at its step |
| `d` | Training only: the first-aid dummy the Medic practises on |
| `o` `t` `h` `p` `b` `k` `f` `v` `n` `u` `w` `i` `l` | Furniture (`Prop`, decision 0016): sofa, table, bookshelf, plant, bed, desk, filing cabinet, water cooler, hospital bed, medicine cabinet, wheelchair, drip stand, locker. Blocks walking, not sight or scent; never burns or collapses |
| `,` `:` `_` `~` | Things on the floor (`DecoratedFloor`): papers, broken glass, rug, puddle. Behaves exactly like floor |
| `?` | Possible survivor spot: the game picks some of these at random each time the level starts; the rest are Floor. The types of the random survivors come from `LevelData` (e.g. `"ms"` = one minor, one serious) |
| `P` | Where the team starts, next to the exit (on Floor) |

`LevelLoader` should throw a clear exception if:
- the rows have different lengths,
- the outer border is not all `#`,
- there is not exactly one exit, or
- there is not exactly one team start (`P`), or
- there are not enough `?` spots to reach the level's survivor count.

**Random survivor spots (added 2026-11-09, see `decisions/0006`; grouped 2026-11-15, see `decisions/0010`):** each level has a fixed `S` (the survivor behind rubble, so the Engineer always matters), a fixed critical `!`, and several `?` spots grouped into 2 or 3 room clusters. `LevelLoader` shuffles the `?` spots with `Collections.shuffle` and fills as many as the level needs. The building stays the same, but who is where changes every play. The tests pass a seeded `Random`, so they always get the same spots.

The border rule means the camera and the recursive flood fill can never walk off the map.

### Training (34 × 14, 4 survivors, all 4 needed)

The map is in `assets/levels/tutorial.txt`. The first room has a practice rubble pile (`R`) and a first-aid dummy (`d`, a `TrainingDummy` tile) for testing each role's ability. Then comes a row of rooms from the exit, each teaching one thing, then a hall with a small fire and three small rooms behind rubble. Only one of them has someone in it, and you can't see through rubble, so the Dog Handler's scent (which goes through rubble) is the only way to know which one to dig out:

```
##################################
#X....#........#........#.......##
#P....#........#........#.......##
#.....1........R........2.......##
#...d.#...m....#....T...#...!...##
#.R...#........#........#.......##
###3######################3#######
#...............................##
#........FFF....................##
#...............................##
#####R##########R##########R######
#.........#..........#..........##
#.........#.....S....#..........##
##################################
```

The `Tutorial` class runs **11 strict steps in 4 chapters** (decisions 0011, 0013, 0015 and 0023), one prompt at a time at the bottom of the map. It had 17 steps until 2026-11-16. Steps that teach one idea are now one step, and the card changes as you do each part, so there are fewer "DONE!" stops:

*Chapter 1, Moving and roles*
1. Walk: try two directions with W, A, S, D (or the arrows).
2. Press 2 for the Engineer, then **test the ability**: clear the marked practice rubble with E.
3. Press 3 for the Dog Handler and **test the ability**: sniff with E. (Nobody is in the room, so the dog finds no scent; the next prompt says why.)
4. Press 1 for the Medic and **test the ability**: treat the marked dummy with E.

*Chapter 2, Rescuing*
5. Gate `1` opens. Follow the call for help, walk into the minor survivor (they follow you, one at a time), and lead them to the EXIT.

*Chapter 3, Engineer and Medic*
6. As the Engineer, clear the rubble door, lift the debris off the trapped survivor, then walk into them to carry them out (you can carry one and lead one at once).
7. Gate `2` opens. Stabilise the critical survivor as the Medic, then call backup with Q (Q unlocks once they are stabilised).

*Chapter 4, Search against the clock*
8. Gate `3` opens. **The clock starts.** Walk along the hall sniffing with the Dog Handler until the dog finds who is behind the rubble. The Engineer's E is locked during this step, so nobody can just dig at random.
9. Clear that room's rubble as the Engineer.
10. An aftershock collapses floor near you: step off the orange tiles (it never collapses a doorway or one-tile corridor). Then carry the survivor out.
11. Leave.

Each test step works the same way: the card first says which number to press, and only once you are that role does it say "Test your ability now" and mark the target. Each role's E is unlocked for its own test step, locked again afterwards, and unlocked for good at the step that uses it for real.

**Guiding the player (decision 0013, after the second playtest):**
- **One thing to look at.** At every moment exactly one thing pulses white with a short label (the door that just opened, the survivor or rubble the step is about), with an arrow at the edge of the view when it is off screen. While you carry or lead someone the green EXIT takes over. Calls for help wait until you are through a newly opened door.
- **Doors are shown.** When a gate opens, the camera glides to its nearest door, holds for a moment and glides back (about 2 s, the game waits meanwhile). The door stays marked "OPEN DOOR" until you walk through it.
- **Finishing feels like something.** A finished step plays a click and the card shows "DONE!" in green for a moment. Each chapter starts with a title card in the middle of the map. A row of 11 dots on the card, grouped by chapter, fills in green as you go.

Keys that haven't been taught yet do nothing ("Not yet: follow the training card"). Doors `1` to `3` (`Barrier` tiles, taped-off doorways) block walking and sight until their step. Steps 1–12 are calm: survivors don't weaken, fire doesn't spread or burn, and there is no aftershock timer. From step 13 survivors weaken, and the serious one lasts 25 ticks (75 s) *(tune)*. If they run out of time, they go back to their hiding spot and the player back to where the timed part began, and the timed part starts again. Training can't be failed.

### Level 1 (30 wide, 20 tall, 4 survivors, target 3)

```
##############################
#........#..!.......#......PX#
#..S.....#........?.#........#
#........#.....F....C........#
#........#.....F....C........#
#........#..?.......#........#
####R#####..........#........#
#........#..........######.###
#.....?..#.......?..#........#
#...................#........#
#..?.....#..........#........#
#........#####.######........#
#.....?..#..........#........#
#........#..........###.######
#........#..........#........#
#........#..........#........#
#...................#........#
#........#...................#
#........#..........#........#
##############################
```

**What this level teaches** (positions are `row,col`, counting from 0):
- **You start beside the exit** (top right).
- **The quake changes your route back.** The quickest way between the burning central hall and the exit room is the cracked gap at `3,20` and `4,20`. When the first aftershock hits, the gap becomes rubble.
- **Two clusters:** the central hall (the critical survivor at `1,12` and spots `2,18`, `5,12`, `8,17`) and the west rooms (spots `8,6`, `10,3`, `12,6`).
- **The Engineer matters.** The storeroom in the top left (survivor at `2,3`) has only one door, and it is Rubble (`6,4`).
- The 2 random survivors are one minor and one serious.

**Every level follows the same rules.** Each level must have:
- the team start (`P`) next to the exit,
- at least one cracked-floor shortcut, plus a longer backup route,
- at least one fixed survivor (`S`) that only the Engineer can reach, and one fixed critical survivor (`!`),
- at least one survivor in a side room that is easy to miss without the Dog Handler, and
- every survivor still reachable after every cracked tile has collapsed (counting rubble as clearable).

### Level 2 (35 × 25, 4 survivors, target 3): "Office floor"

The map is in `assets/levels/level2.txt`.
- **The start:** beside the exit at the east end of the central corridor.
- **The shortcut:** a 3 × 3 block of cracked floor sits across the middle of the central corridor, between the start and the west half of the building.
- **After the quake:** the backup route climbs through the north-west room into the north corridor, then comes back down east of the crack.
- **The Engineer:** the north-east room's only door is rubble, right next to the start.
- **Survivors:** the north-east room's serious survivor and a critical survivor in the south-middle room (`17,13`) are fixed; 3 more (minor, serious, minor) are picked from 6 spots in two clusters: the north rooms and the south-west rooms.

### Level 3 (40 × 30, 5 survivors, target 4): "Hospital wing"

The map is in `assets/levels/level3.txt`.
- **The start:** beside the exit at the top of the exit hall.
- **The shortcut:** the quickest way down from the exit hall to the big hall is a two-tile cracked gap right below the start.
- **After the quake:** the backup route goes round through the east room, nearly doubling the trip.
- **The Engineer:** the west room is behind rubble. More rubble doors connect the start room to the side wings as shortcuts.
- **Pressure:** this level has four fires and the fastest aftershocks (every 27 s).
- **Survivors:** the west room's serious survivor and a critical survivor in the big hall (`13,9`) are fixed; 4 more (minor, serious, minor, serious) are picked from 7 spots across the big hall, the ward and the south-west room.

**Checked by a test, not by hand.** `LevelBalanceTest` checks every level against **every possible pick** of survivor spots and **every way the random types can land** on them (1, 15, 20 and 35 picks). It assumes every cracked floor has already collapsed and fire never burns out. It plans the best order a player could rescue the target in, letting the player walk on while backup comes, and requires the plan to fit in 80 % of each survivor's time. The worst picks currently use 60 %, 74 % and 46 % (Levels 1, 2, 3). Training is calm until its timed part, so a separate test checks only that part, including two role changes, one wasted sniff and clearing the rubble: 40 s of the 75 s the survivor has. Because survivors now block the way, another test fills every possible survivor spot in every level and checks that nobody walls off a room or a tile. A second test checks that fire reaches no survivor spot in the first 40 s, which is exact because fire follows the level's seed until the first aftershock warning.

---

## 6. Screens and Controls

### Screens

1. **Main Menu** (reworked 2026-11-16, decision 0024): title, **Continue - Level n** (only once there is progress; goes to the furthest open level), **Play**, Level Select, **Options**, How to Play, Quit. Menu music plays.
   - The first time you press Play it asks **"Play the training first?"**: Yes (recommended) / Skip to Level 1. Finishing or skipping the training is saved, and Play then goes straight to Level 1. The training stays in Level Select.
   - **Options** (from the main menu or the pause menu): Music and Sound volume (0-100 %, Left/Right), Fullscreen on/off (also F11 anywhere; the game scales to fit the screen), Reset Progress (asks first and names what is lost). All saved.
1. **Level Select** (added 2026-11-16): Training and Levels 1–3, each level with its best stars. Training and Level 1 are always open; finishing a level opens the next. Picking a locked level says which one to finish first. Saved between runs.
2. **How to Play:** four columns: the controls, the three roles, the four survivor types, and the building's tiles, each with its picture.
3. **Game Screen:** the camera view on the left, the HUD panel on the right. On the camera view there is only one slim "next step" line top left, plus a prompt card near the bottom in training. The checklist and the minimap moved into the HUD on 2026-11-16, so nothing else covers the map. The camera can scroll 3 rows above the map so the line never covers the top rows, and the line goes see-through whenever the exit is under it. While you carry or lead someone, or once enough are saved, the exit pulses green, with a green EXIT arrow at the screen edge when it is off screen.
   - **Briefing card** (Levels 1–3, added 2026-11-16): before the clock starts, a card over the dimmed map gives the level's name, the target ("Rescue 3 of the 4 survivors, then leave by the EXIT"), how often aftershocks hit, one or two lines on what is new in this level (`LevelData.getWhatsNew()`), and the three star goals. Enter starts the level.
   - **The end:** when the mission ends, the game stops and "MISSION COMPLETE" (green) or "MISSION FAILED" (red) shows over the dimmed map for 1.6 s, with "Rescued 3 of 4". Then the result screen opens. It never jumps straight into the next level.
4. **Pause menu** (Esc): a list like every other menu: Resume, Restart Level, How to Play, Options, Main Menu. Restart Level and Main Menu ask "Are you sure? This level's progress is lost." (No is first). Esc resumes; How to Play and Options come back to the paused level.
5. **Result Screen:** the title, the time and survivors rescued, then three big stars that fill in one by one with a ding. Under them, the three star goals, each with a small star (filled = met). No points total (decision 0018). On a failure, a red line says why ("All three roles went down." or "Too many survivors were lost") and a tip says what to try. On a success with stars missing, the tip is about the next star. "NEW BEST" shows when the stars beat the saved best. Options: Next Level (on a success), Play Again / Try Again, Level Select, Main Menu.

`GameFrame` (a libGDX `Game`) switches screens with `setScreen(...)`.

**Menu style (2026-11-09, from researching game title screens):**
- Everything centred: a big pixel-font title, a one-line tagline, then a short list of options. Settings live in Options, not on the title screen.
- The selected option is always visible: bigger, in the highlight colour, with `>` and `<` beside it. Up/Down or W/S moves, Enter or Space chooses, Esc goes back, and the mouse works too.
- The title screen shows the real game behind the menu: a dimmed piece of Level 1 with fire flickering, and a dark band behind the title and options so they read clearly (2026-11-16). The three roles stand under the title at 4x and take turns to step forward and show their name (`TeamLineup`), a small nod to Soul Knight's lobby where the heroes stand around.
- How to Play is three columns instead of paragraphs: the controls as keyboard keys, the team with their pictures, and the building's tiles with one line each. Players skip long text.
- A small grey line at the bottom of each menu says which keys work.

### Camera view and minimap

- The camera view is **15 × 11 tiles** (960 × 704 px), centred on the active rescuer and clamped at the map edges.
- The **minimap** (at the bottom of the HUD since 2026-11-16, under "MAP") draws the whole map at the biggest whole number of pixels per tile that fits the panel (5 for Level 1, 3 for Level 3):
  - explored tiles in their colour, unexplored tiles black,
  - the Exit in green,
  - the rescuer as a dot in their role's colour,
  - revealed survivors as blinking dots.

### Objective checklist (added 2026-11-15, moved into the HUD 2026-11-16)

A checklist like a quest tracker in other games. It has three lines with a box each (filled = done): **Find survivors 2/4**, **Rescue 1/3**, **Leave by the EXIT**. It sits in the HUD under the survivor boxes. One **next step** line stays on the map, top left, in the accent colour (`ObjectivePanel`, only as wide as its text), worked out by `ObjectiveTracker`:
- while carrying or leading someone, "Take them to the EXIT";
- otherwise, what the nearest revealed survivor needs, which each survivor type answers itself (`getNeed()`), for example "Critical! Medic (1): face them, press E";
- if nobody is revealed, "Follow the HELP calls", or "Search the dark. The dog (3, E) smells 8 steps around". It no longer says "press E" at the start, where nothing is in range (decision 0021).

### HUD contents

Reworked 2026-11-09 after the first playtest. Research on game HUDs pointed the same way: small counts read faster as pictures than as sentences, show things only when they matter, and never say the same thing twice.

Reworked again 2026-11-16 after an audit: everything has a label, so nothing is told by colour alone, and actions that can't be used right now are dimmed.

- **Top:** level number and "Time 0:42" on one line.
- **NEXT AFTERSHOCK:** a bar that empties as the quake gets closer, with "6 s". Both turn orange in the last 5 s.
- **SURVIVORS** ("dashed = extra"): one box per survivor. A saved survivor fills a box with their picture, and a lost one is a red cross. Boxes for the survivors you must save are solid, the extra ones are dashed. Under them: the objective checklist.
- **TEAM HEALTH** ("1 2 3 to switch"): number key, picture, name and health squares for each role. The one on the map says "(you)" and has its number in the accent colour; a downed one says "down" in red. During the 1 s gear change, a puff of dust hides the rescuer on the map while a small bar fills over them.
- **ABILITIES:** the E keycap, the ability name ("recharging" while it does), and a bar that refills. Under it, the Q keycap: "Call backup (red)". Each row is dimmed to half while it can't do anything: E while recharging or with nothing to act on (`Rescuer.hasSpecialTarget`), Q unless a stabilised red survivor is right there (`Rescuer.canCallBackup`) (decision 0021).
- **WITH YOU:** "Carry 0/1" and "Follow 0/1" on one line, always shown so the limits are clear, with the carried survivor and the follower drawn beside them when there is one.
- **MAP:** the minimap, with "Esc Pause" beside its heading.
- **Scent range** (map, 2026-11-16): while you are the Dog Handler, faint paw prints mark every explored tile the scent reaches (8 steps), brighter when Scent is ready. A sniff that finds nobody new only takes 2 s to recharge (decision 0021).
- **Running out of time** (added 2026-11-16): when a survivor you have found is down to 30 s, then 10 s, a double beep plays and a message says so ("A serious survivor has 30 s left!"). From 30 s they pulse red on the map with the seconds under them. Treating them past a warning resets it.
- **Hint when stuck** (added 2026-11-16, not in training): if for 20 s nobody has been found, freed, stabilised, picked up or rescued, the radio crackles and the message box repeats the objective card's next step ("Radio: Search the dark. Dog Handler (3): press E"). Not while carrying or leading someone, because the green EXIT marker already shows the way.
- **Messages** about things that just happened (an invalid action, someone burning, a survivor lost) appear for 3 s in a small box at the bottom of the map, not in the sidebar. Nothing the sidebar already shows is repeated there.

### Controls

| Input | Action |
|---|---|
| WASD / arrow keys | Walk (hold to keep walking). Also turns to face that direction |
| 1, 2, 3 or Tab | Change role to Medic / Engineer / Dog Handler, or to the next one (1 s gear change) |
| E | Special action on the faced tile (the Dog Handler needs no target) |
| Q | Call backup for a stabilised critical survivor on your tile or the tile you face |
| Esc | Pause menu (Resume, Restart Level, How to Play, Options, Main Menu) |
| F11 | Fullscreen on/off (saved) |

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
      ResultPanel.java      (the result screen with stars, reason and tip)
      LevelSelectPanel.java (2026-11-16: pick a level, with its best stars)
      StarIcon.java         (2026-11-16: a pixel-art star, implements Drawable)
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
      ObjectivePanel.java   (v4: the "next step" line on the map; the checklist is in the HUD)
      BackupTeam.java       (new: the backup stretcher team walking in and out, a picture only)
      TeamLineup.java       (new: the three roles stepping forward on the title screen, implements Drawable)
      OptionsPanel.java     (new: music and sound volume, fullscreen, reset progress)
      TrainingPromptPanel.java (new: "Play the training first?", asked once)
    graphics/
      Painter.java          (new: draws images and shapes for every Drawable)
    model/
      GameBoard.java, RescueTeam.java, Survivor.java, Drawable.java, GameState.java, Direction.java
      survivors/
        MinorSurvivor.java, SeriousSurvivor.java, CriticalSurvivor.java
      tiles/
        Tile.java, Floor.java, CrackedFloor.java, BurntFloor.java, DecoratedFloor.java,
        Wall.java, Rubble.java, Fire.java, Exit.java, Barrier.java, TrainingDummy.java, Prop.java
      rescuers/
        Rescuer.java, Medic.java, Engineer.java, DogHandler.java
    logic/
      TurnManager.java      (runs the world tick)
      HazardManager.java
      GameEvent.java        (what happened in a tick, for sounds and messages)
      MovementCalculator.java
      LevelLoader.java
      LevelData.java
      InvalidMoveException.java
      ObjectiveTracker.java, Objective.java   (v4: the objective checklist)
      Tutorial.java         (v4: the training level's prompts)
      MissionReport.java    (2026-11-16: stars, why a mission failed, a tip)
      Progress.java         (2026-11-16: open levels and best stars)
    assets/
      AssetManager.java
      SoundManager.java
      SaveFile.java         (2026-11-16: saves Progress in libGDX Preferences)
  src/test/java/rescuegrid/logic/                  (JUnit tests for the rules)
lwjgl3/                                            (desktop launcher)
  build.gradle
  src/main/java/rescuegrid/Main.java               (opens the window)
assets/                                            (shared by both modules)
  levels/  tutorial.txt, level1.txt, level2.txt, level3.txt
  sounds/  music/  images/  fonts/
tools/
  SoundMaker.java           (our own synthesizer: generates every WAV, not part of the game)
  SpriteMaker.java          (draws every sprite sheet in assets/images, not part of the game)
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
  - `pickUp(Survivor s)`, `lead(Survivor s)`, `Survivor dropSurvivor()`, `Survivor deliverSurvivor()`, `int deliverAll()` (carried + follower)
  - `void takeOver(Rescuer previous)`: used when changing role; copies the position, facing and step in progress, and takes over the carried survivor and the follower
  - `Survivor findSurvivorNearby(GameBoard board)` (own tile, else the faced tile) and `String callBackup(GameBoard board)`: the Q key, the same for every role
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

**`RescueTeam`** (v4, decision 0007)
- Holds the three roles in key order, which one is on the map, and the gear-change timer.
- `switchTo(int index)` and `switchToNext()` throw `InvalidMoveException` for a downed role or the role already on the map; `replaceDowned()` sends in the next role when the one on the map is downed; `isWipedOut()`.
- `tick(seconds)` counts down every role's cooldown and the gear change.

**`GameBoard`**
- Holds:
  - `Tile[][] grid`, a `RescueTeam team`, `ArrayList<Survivor> survivors`
  - `boolean[][] visible`, recalculated whenever the active rescuer moves or switches, or the world ticks
- Methods: `getTile(row, col)`, `replaceTile(row, col, Tile t)`, `inBounds(row, col)`, `getActiveRescuer()`, `getTeam()`, `getRescuerAt(row, col)` (only the rescuer on the map), `getSurvivorAt(row, col)`, `isVisible(row, col)`
- `replaceTile` is how fire spreads, fire burns out, rubble is cleared, and cracked floor collapses. Swap the tile object for a new subclass object, and copy the `explored` flag to the new tile. Do not use a type field.

**`Survivor`** (abstract since v4, implements `Drawable`, decision 0008)
- Private fields: `row`, `col`, `condition`, `revealed`, `carried`, `following`, `trapped`, `rescued`, `lost`
- Abstract methods: `getInjuryName()`, `getTagColor()`, `getConditionLossPerTick()`, `goWith(Rescuer r)` (follow, be carried, or refuse), `whatTheyNeed()`
- Methods every type shares: `reachedBy(Rescuer r)` (refuses while trapped, then calls `goWith`), `treat(int amount)`, `callBackup()` (throws "only critical survivors need backup"), `update(double seconds)` (does nothing), `tickCondition()`, `freeFromDebris()`, `getNeed()`, `reveal()`, plus getters
- Subclasses (package `model.survivors`):
  - `MinorSurvivor`: `goWith` makes them follow; drains 1 point a tick.
  - `SeriousSurvivor`: `goWith` picks them up; drains 2.
  - `CriticalSurvivor`: `goWith` always refuses with what they need; overrides `treat` (also stabilises), `callBackup` (starts the 10 s countdown), `update` (counts it down and rescues them), `getConditionLossPerTick` (3, or 1 once stabilised), and `drawFigure` (lying down).

**`GameState`** (enum): `MENU`, `BRIEFING` (the level's briefing card, the clock waits), `PLAYING`, `ENDING` (the moment with the result over the map), `PAUSED`, `LEVEL_COMPLETE`, `GAME_OVER`

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
  - Every frame: lets each survivor `update(seconds)` (the backup countdown), works out which survivors are calling for help (`getCallingSurvivors()`), and sends in the next role if the one on the map was downed.
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
- **`ObjectiveTracker`** and **`Objective`** (v4): the objective checklist and the "next step" line (Section 6).
- **`MissionReport`** (2026-11-16): built from the board at the end of a mission. `getStars()`, `isGoalMet(i)`, `getGoalName(i)`, `getFailReason()` and `getTip()`; the static `goalsFor(level)` gives the briefing its goal list.
- **`Progress`** (2026-11-16): which levels are open and the best stars per level. `record(level, stars, success)` returns true for a new best. Plain Java, so it is tested without libGDX; `SaveFile` loads and saves it.
- **`Tutorial`** (v4): the training level's 11 strict steps in 4 chapters (17 until decision 0023); a step can have several parts (`part`), and the card and marker follow them. It also says what to point at (`getMarker`, `getMarkerLabel`, `takeDoorToShow` for the camera's look at a door that opened). Each step counts only its own action. It says which keys are unlocked (`canChangeRole`, `canUseAbility`, `canCallBackup`), opens the training doors (`GameBoard.openGate`), keeps the level calm until the timed part (`TurnManager.setSurvivorsWeaken` / `setHazardsRunning` / `setHelpCallsOn`), triggers one aftershock next to the player (`startAftershockNear`), and restarts the timed part if a survivor runs out of time (`retryIfLost`).
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
    - draws the HUD (with the minimap) and pause card (Scene2D).
- **`Camera`**:
  - `follow(row, col, mapRows, mapCols)` centres the view on the rescuer's draw position, clamped at the map edges.
  - It works in pixels, so it glides along while the rescuer slides between tiles. It then points a libGDX `OrthographicCamera` at that spot, which does the screen maths.
  - Worked example: the view is 15 tiles wide (960 px) and the rescuer is at col 20. Then `offsetX = 20 × 64 − (960 − 64) / 2 = 832`, and a tile at col 25 appears at screen `x = 25 × 64 − 832 = 768`.
- **`Minimap`** (implements `Drawable`): loops over the whole grid and draws a square per explored tile (the HUD picks the size), then dots for the exit, rescuers and revealed survivors.
- **`KeyboardInput`** (extends libGDX `InputAdapter`): keeps the keys currently held down in a `HashSet<Integer>`. `isHeld(int keycode)` lets `update()` ask "is W held?" every frame, which is what makes hold-to-walk work.
- **`ObjectivePanel`** (extends Scene2D `Table`, v4): the one-line "next step" card on the map.
- **`BackupTeam`** (2026-11-16, decision 0020): two paramedics and a stretcher. `findPath` is a breadth-first search from the EXIT to the survivor; `update` follows the survivor's backup countdown so the team arrives at 10 s; `draw` sorts the two paramedics and the stretcher by row.
- **`TeamLineup`** (implements `Drawable`): the title screen's three roles at 4x, taking turns to step forward.
- **`HudPanel`** (extends Scene2D `Table`): shows the information from Section 6. **`HealthBar`** (extends Scene2D `Actor`) draws the health squares. The timer bars are Scene2D's own `ProgressBar`. **`SurvivorPip`** and **`CarriedSurvivor`** implement `Drawable`, so the HUD draws them in icon slots like any tile.
- **`Toast`** (extends Scene2D `Label`): a message at the bottom of the map that fades out with Scene2D actions (`delay`, then `fadeOut`).
- **`MenuPanel`**, **`HowToPlayPanel`**, **`ResultPanel`**, **`LevelSelectPanel`**: the other screens. **`StarIcon`** (implements `Drawable`) draws a star from a 9 × 9 character grid, gold or faint.
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
| **Inheritance** | `Medic`, `Engineer`, `DogHandler` extend `Rescuer`. `MinorSurvivor`, `SeriousSurvivor`, `CriticalSurvivor` extend `Survivor`. `Floor`, `Wall`, `Rubble`, `Fire`, `Exit` extend `Tile`. `CrackedFloor` and `BurntFloor` extend `Floor` (two levels deep). |
| **Polymorphism** | Pressing E calls `activeRescuer.specialAction(board)` and the right subclass version runs. The draw loop calls `tile.draw(...)` on every tile without checking its type, and each tile's own `drawFallback` paints bricks, rocks or flames. On an aftershock, `HazardManager` calls `tile.onAftershock(board)` on every tile: only `CrackedFloor` reacts. Fire burnout works the same way through `onWorldTick`. Walking into a survivor calls `survivor.reachedBy(rescuer)`: a minor survivor follows, a serious one is picked up, a critical one refuses and says what it needs. The Medic's `treat()` also stabilises a critical survivor, and only `CriticalSurvivor` accepts `callBackup()`. |
| **Abstraction** | `Tile`, `Rescuer` and `Survivor` are abstract with abstract methods. `GameBoard` and `MovementCalculator` work with any `Tile` without knowing which kind it is. |

**Using the library's classes the OOP way:** our screens inherit from libGDX (`GameFrame extends Game`, `BaseScreen extends ScreenAdapter`, and the four screens extend `BaseScreen`), `HudPanel extends Table`, `HealthBar extends Actor` and overrides `draw`, and `KeyboardInput extends InputAdapter`. libGDX then calls our overridden methods (`render`, `keyDown`, `draw`) without knowing our classes, which is polymorphism from the other side.

Other concepts we want visible in the code:
- An interface (`Drawable`, implemented by four unrelated classes; we also implement libGDX's `Disposable`)
- Composition (`GameBoard` has a grid and a `RescueTeam`, rescuers have a carried survivor and a follower, `GamePanel` has a `Camera`, a `Minimap` and a `KeyboardInput`)
- Enums (`GameState`, `Direction`, `GameEvent.Type`)
- Collections and generics (`ArrayList`, `HashMap`, `HashSet`, `List<Survivor>`)
- A custom exception (`InvalidMoveException`)
- Recursion (`MovementCalculator`'s flood fill, used for sight and scent)
- Constructors calling `super(...)`
- `@Override` on every overridden method
- Method overloading (`moveTo(int row, int col)` and `moveTo(Tile target)`; `LevelLoader.load(...)` and `LevelLoader.parse(...)` with different parameters)

**Avoid:** long `instanceof` or `switch` chains to decide behaviour by type. If a behaviour depends on the type, it belongs in an overridden method in the subclass, like the hook methods on `Tile`. The only allowed switches on tile or survivor type are in `LevelLoader`. "Trapped" is a flag, not a subclass, because a survivor stops being trapped during play and an object can't change its class.

---

## 10. Assets

### Decisions (2026-11-08, see `decisions/0003`)

| What | Decision | Status |
|---|---|---|
| Art style | **2D top-down pixel art, 16 × 16 sprites drawn at 4× (64 px).** 3D would multiply the art work even in libGDX, so 2D pixel art it is. | Decided |
| Placeholder art | Shapes drawn in Java (`drawFallback`): textured floor, brick walls, rock piles, flickering flames, a cracked-floor pattern, an EXIT door, rescuers with role icons, survivors as figures with a condition bar. | **Built, used for the first showing** |
| Sprites | **Our own pixel art, drawn from code** by `tools/SpriteMaker.java` (the same idea as `SoundMaker`), in the angled RPG view (decision 0016). No licences to track. | **Built 2026-11-16 (below)** |
| Sounds and music | Generated by our own Java synthesizer, `tools/SoundMaker.java` (the same idea as the sfxr/jsfxr retro sound tools). It makes 21 effects and 2 chiptune loops. Any track can be swapped for a downloaded CC0 one by replacing the WAV file. | **Built** |

**The rule still holds:** every `draw()` falls back to the placeholder art when an image is missing, and a missing sound is simply silent. Dropping a correctly named PNG into `assets/images/` switches that object to the sprite, with no code changes.

### Sprites (built 2026-11-16, decision 0016)

**Chosen in chat:**
- All our own art, in the **angled RPG view** (walls have fronts, people stand up).
- 16 × 16 tiles and 16 × 24 people, drawn at 4×.
- Rescuers face **4 ways and walk**.
- A **dusty disaster** palette: muted building, strong colours only on people and hazards.
- **Themed furniture.**
- **Distinct outfits** for the three roles.

**How they're made:** `tools/SpriteMaker.java` (run `java tools/SpriteMaker.java` from the project folder) draws every sprite from code.
- Shapes are drawn first, then people and furniture get a 1 px outline automatically, so all poses stay consistent.
- It writes the sheets to `assets/images/` and a review picture of all of them to `tools/sprite_preview.png`.
- Random seeds are fixed, so the same PNGs come out every run.

| Sheet | Frames | Used by |
|---|---|---|
| `floor` | 4 looks (plain is the most common) | `Floor`, picked by position |
| `cracked_floor`, `burnt_floor` | 1, 2 | `CrackedFloor`, `BurntFloor` |
| `wall` | top (a cap of staggered stone slabs, lighter than the fog since 2026-11-16 so a wall never reads as a hole), front, edge up/right/left, front end left/right, shadow below, shadow right | `Wall.getPieces` picks from the neighbours; every other tile draws the shadows |
| `rubble` | 2 looks: a heap of concrete, one with a steel bar | `Rubble` |
| `fire` | 4 flicker frames | `Fire`, neighbours out of step |
| `exit`, `barrier`, `dummy` | 1 each | lit EXIT sign over a doorway, caution tape, first-aid dummy |
| `props` | 26: the 13 kinds (sofa, table, bookshelf, plant, bed, desk, filing cabinet, water cooler, hospital bed, medicine cabinet, wheelchair, drip stand, locker) intact, then the same 13 damaged | `Prop`; about a third of the furniture is damaged, picked by position |
| `decor` | 4: papers, broken glass, rug, puddle | `DecoratedFloor` |
| `medic`, `engineer`, `dog_handler` | rows up/down/left/right × stand, step A, step B (16 × 24) | `Rescuer.drawOnMap` (faces the way they face, alternate feet while stepping) |
| `dog` | rows up/down/left/right × 2 | drawn beside the Dog Handler, behind them unless they face down |
| `survivor_minor` | like the rescuers (with an arm in a sling), plus two waving frames | follows you walking; waiting, waves the free arm and shouts |
| `survivor_serious`, `survivor_critical` | 2 each | slumped holding a bleeding leg, rocking; lying pale with an arm up and blood beside them, breathing |
| `emote` | 4 | the bouncing "!" bubble by a found survivor who is still waiting (decision 0017), then 2 frames of a radio bubble over the rescuer after calling backup (0020) |
| `paramedic` | like the rescuers | the backup stretcher team, paramedic green with white helmets (0020) |
| `stretcher` | 4: across or lengthways, empty or carrying someone under a red blanket | carried between the two paramedics |
| `flare` | 2 | a red road flare by a red survivor while backup is coming, with the seconds left |
| `puff` | 4 (16 × 24) | a puff of dust over the rescuer during the 1 s role change |
| `carry_icon` | 1 | the badge on a rescuer who is carrying someone |

**Looks** (hue-shifted shading: shadows lean blue-violet and lights lean warm; outlines are a dark tone of the colour they touch, as in Stardew Valley):
- One face for all three roles, since it is one rescuer changing kit; **the gear gives each role its own outline** (decision 0019):
- **Medic:** white, with a red cross, a red cap, a big red backpack with a white cross, and a stethoscope.
- **Engineer:** orange hi-vis vest with reflective stripes, a yellow hard hat with a headlamp, a tool belt and a crowbar.
- **Dog Handler:** blue with a navy cap, a charcoal K9 vest, and a brown dog on a red leash (the game draws the leash from the hand to the collar).
- **Survivors** (decision 0017, after "they don't look like they need help"):
  - Their shirt is their triage colour.
  - Each colour has its own hurt pose, so the shape alone tells them apart.
  - Clothes are dusty and torn, and faces look worried.
  - **Green:** sling and bloodied head bandage, waving and shouting.
  - **Yellow:** slumped, holding a bleeding leg.
  - **Red:** pale, lying with an arm up, a small pool of blood.
  - A bouncing "!" bubble shows until you reach them.

**Drawing rules in the game:**
- People are 1.5 tiles tall and drawn top row first (`GamePanel.drawPeople`), so someone lower down overlaps the head of someone above.
- Marker labels sit higher, so they clear people's heads. The gear-change bar sits above the head.
- The condition bar, trapped debris, carry badge, hurt flash, faced-tile outline and markers are still drawn by code on top of the sprites.
- In the HUD and menus (`draw` in a square) people are shown standing and facing you.

**Furniture in the maps** (see section 5 for the letters): training is a training centre (lockers, plants), Level 1 a home, Level 2 an office, Level 3 a hospital.
- Furniture blocks walking, but sight and the dog's scent pass over it. It never burns or collapses.
- `LevelBalanceTest` counts furniture as blocking. Level 1's sofa was moved out of the fire's room after the fire test showed it changed how that fire spreads.

**Still open:**
- A carrying pose: carrying is still a badge.
- A playtest to check that cracked floor and the faced-tile outline still stand out.

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

> **Status (2026-11-15):** Weeks 1–4 are built on libGDX, except the sprites:
> - The whole game is playable, from the menu through the training level and all three levels, with placeholder art, sound and music.
> - 92 JUnit tests cover the rules (`gradlew test`), including a full play-through of the training, a balance check of every level, the stars, the saved progress and the low-time warnings.
> - Added after the first playtest (2026-11-09): a survivor counter in the HUD, leaving through the exit once the target is reached, and random survivor spots.
> - Added after the teacher's feedback (2026-11-15): one rescuer who changes role, triage survivor types with backup, trapped survivors, start at the exit with grouped survivors, calls for help, the objective checklist, and the training level.
> - Sprites built 2026-11-16 (section 10, decision 0016).
> - Audit fixes 2026-11-16 (decisions 0018 to 0021): stars only, role gear, the visible backup team, Scent range and dimmed keys, the reorganised HUD and a calmer title screen.
> - Still to do: playtesting by hand, and tuning the numbers marked *(tune)*.

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
- End conditions and the star grading (`MissionReport`)
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
| Member 3 | `GamePanel` game loop, `TurnManager`, `HazardManager`, `MissionReport` (stars), `HudPanel` and the other screens, `UiStyle`, `Painter`, `SoundManager` |
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
- changing role (the one rescuer with three roles),
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
