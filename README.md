# Rescue Grid

A real-time, top-down rescue game built on **libGDX** (Java). Explore a building that collapsed in an earthquake as one rescuer who can change role (Medic, Engineer, Dog Handler), and get the survivors out while fire spreads and aftershocks block your paths.

The full design is in [`RESCUE_GRID_PLAN.md`](RESCUE_GRID_PLAN.md), and the reasons behind the big decisions are in [`decisions/`](decisions/).

## Open and run

You need a JDK 17 or newer (IntelliJ can download one, for example Corretto). You don't need to install Gradle: the project brings its own (`gradlew`).

**In IntelliJ IDEA:**
1. File → Open → choose the `OOP_Project` folder. IntelliJ sees `build.gradle` and imports it as a Gradle project. The first import downloads Gradle and libGDX (about 150 MB), so it takes a few minutes.
2. If it asks for a JDK, pick 17 or newer. (Settings → Build Tools → Gradle → Gradle JVM.)
3. Press the green Run button next to **Rescue Grid** at the top right. That runs the Gradle task `lwjgl3:run`. You can also run it from the Gradle tool window: `RescueGrid → lwjgl3 → Tasks → application → run`.

**From a terminal**, in the project folder:

```
gradlew lwjgl3:run      (Windows)
./gradlew lwjgl3:run    (Mac / Linux)
gradlew test            (run the 68 rule tests)
```

On a Mac, run the game through Gradle as above, not by running `Main` directly: the window needs a special Java setting there, and the Gradle task adds it for you.

## How to win a level

Start Game begins with a short **training level** that teaches everything one step at a time (Skip Training goes straight to Level 1).

You start beside the EXIT. Survivors you haven't found yet call for **HELP** when you are close: follow the ring or the arrow. Each survivor has a triage tag:

| Tag | What to do |
|---|---|
| Green (minor) | Walk onto them: they follow you to the EXIT |
| Yellow (serious) | Walk onto them to carry them (slower) |
| Red (critical) | Change to the Medic and treat them (E), then press Q: backup carries them out 10 s later |
| Debris on them (trapped) | Change to the Engineer and lift it (E) first |

The **objective card** in the top-left corner says what to do next. The **SURVIVORS** boxes in the sidebar fill up as you save people; the solid boxes are the ones you must save, the dashed ones are optional. Once you've saved enough, step onto the EXIT with empty hands to leave, or stay and save the rest for a bonus. Survivors start in different places each time you play.

## Controls

| Key | Action |
|---|---|
| WASD / arrow keys | Walk (hold to keep walking) |
| 1 / 2 / 3, Tab | Change role to Medic / Engineer / Dog Handler, or to the next one (takes 1 s) |
| E | Special action on the tile you face (dashed outline) |
| Q | Call backup for a stabilised critical survivor |
| Esc | Pause (then R restarts, M goes to the menu) |
| Menus | Arrow keys or W/S to choose, Enter or Space to select, Esc to go back (the mouse works too) |

## Project layout

| Folder | What's in it |
|---|---|
| `core/` | The whole game: rules (`logic`, `model`), screens (`ui`), drawing (`graphics`), images and sounds (`assets`), and the JUnit tests |
| `lwjgl3/` | The desktop launcher, `rescuegrid.Main`, which opens the window |
| `assets/` | Levels, sounds, music, fonts, and images (empty until sprites are made) |
| `tools/` | `SoundMaker.java`, our sound generator (not part of the game) |

## What's used besides plain Java

- **libGDX 1.14.2** with the **LWJGL3** desktop backend: the window, drawing (`SpriteBatch`, `ShapeRenderer`), the camera, the keyboard, and sound.
- **Scene2D** (part of libGDX): menus, HUD, buttons and labels.
- **FreeType** (`gdx-freetype`): text drawn from two pixel fonts, Press Start 2P and Jersey 10.
- **Gradle**: builds the project and downloads the libraries.
- **JUnit 5**: tests for the game rules.

All the game rules (movement, sight, fire, aftershocks, survivors, scoring) are our own Java code.

## What's built

- A training level and three levels (`assets/levels/`). Survivors are placed at random from spots marked `?`, grouped in rooms; `LevelBalanceTest` checks every possible placement can be solved in time.
- **Exploring:** real-time tile movement with smooth sliding, a following camera, recursive line-of-sight fog, and a minimap.
- **Your team:** one rescuer with three roles, each with its own health, special and cooldown.
- **Survivors:** minor, serious and critical types, trapped survivors, backup, and calls for help.
- **Guidance:** an objective checklist on the map and a step-by-step training level.
- **The building:**
  - Fire spreads, hurts rescuers, and burns out.
  - Aftershocks come with a warning, collapse cracked floor, cause random collapses, and shake the screen.
- **Screens and scoring:** health, downed roles, win and lose, the score breakdown, plus menu, How to Play, pause and result screens.
- **Placeholder art drawn in code:** brick walls, rock piles, flickering fire, and rescuers with role icons.
- **Sound effects and music** generated by `tools/SoundMaker.java`.

## Not built yet

- **Sprites:** the 16 × 16 pixel-art sprites are planned in section 10 of the plan. Until they exist the game uses the placeholder art. To use a sprite, drop a PNG named after its key (for example `assets/images/medic.png`) and it appears automatically.
- **Balancing:** every number marked *(tune)* in the plan needs playtesting, especially the new survivor types and the backup time.
- **Possible extras:** box2dlights lighting, and Tiled maps once sprites exist (plan section 3).

## Remaking the sounds

From the project folder:

```
java tools/SoundMaker.java
```

This rewrites every WAV in `assets/sounds` and `assets/music`. Edit the numbers in `SoundMaker.java` to change a sound.
