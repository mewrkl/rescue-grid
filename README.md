# Rescue Grid

A real-time, top-down rescue game in Java Swing. The full design is in [`RESCUE_GRID_PLAN.md`](RESCUE_GRID_PLAN.md), and the reasons behind the big decisions are in [`decisions/`](decisions/).

## Open and run

1. In IntelliJ IDEA, open this folder (File → Open → `OOP_Project`).
2. If IntelliJ says "Project JDK is not defined", choose any JDK 17 or newer, or let it download one (Corretto 22 works). The language level is already set to 17.
3. Press the green Run button next to **Rescue Grid** at the top right, or run `rescuegrid.Main`.

The `resources` folder is already marked as a Resources Root, so `level1.txt` and any images you add are found automatically.

## What works now (skeleton)

- All packages and classes from Section 7 of the plan, each with a comment explaining its job.
- The menu, How to Play screen, game screen with HUD, and result screen. `CardLayout` switches between them.
- `LevelLoader` builds Level 1 from `resources/levels/level1.txt` and gives a clear error for broken maps.
- The full `Tile` hierarchy, drawn with fallback colours until sprites are added.
- The game loop timer is running, the camera is centred on the active rescuer, and the HUD updates every frame.
- Pressing Space already calls `specialAction()` and shows its message in the HUD. This is the polymorphism and exception wiring.

## What to build next

Search the code for `TODO (Week 1`. Each TODO says who owns it and gives the steps to follow.

| Week 1 TODO | Owner | File |
|---|---|---|
| Hold-to-walk | Member 2 | `GamePanel.handleMovement()` |
| `tick`, `getStepDelay`, `moveTo` | Member 2 | `Rescuer` |
| One-step check | Member 2 | `MovementCalculator.canStep()` |

**Week 1 is done when** you can walk a rescuer around Level 1 with the camera scrolling and walls blocking you.

## Adding art

Put PNG files in `resources/images/`, named after the keys in `AssetManager` (for example `floor.png`, `medic.png`). They show up the next time you run. Record every asset in [`CREDITS.md`](CREDITS.md).
