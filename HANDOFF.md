# Handoff: 2026-11-15

## DONE THIS SESSION
Acted on the teacher's feedback from the first showing. Everything is committed on the `libgdx` branch, and 68 tests pass (`gradlew test`).
1. **One rescuer who changes role** (`model/RescueTeam.java`, decision 0007, which supersedes 0002).
   - 1/2/3/Tab changes role after a 1 s gear change.
   - Each role keeps its own health; a downed role is replaced at once.
   - Level files use one `P` start instead of `M/E/D`.
2. **Triage survivor types** (decisions 0008, 0009).
   - `Survivor` is now abstract, with `MinorSurvivor` (follows you), `SeriousSurvivor` (carried) and `CriticalSurvivor` in `model/survivors/`.
   - A critical survivor can't be moved: Medic E stabilises them, then Q calls backup, and they're rescued after 10 s.
   - Any survivor can be trapped (`T` in maps): Engineer E frees them.
   - New sounds `radio` and `help_call` (`tools/SoundMaker.java`). The existing WAVs came out byte-identical.
3. **Level flow** (decision 0010).
   - Every level starts beside the exit, with survivors grouped in clusters.
   - Survivors nobody has found call for help: a ring on the map, or an arrow at the screen edge.
   - Objective checklist card top-left (`logic/ObjectiveTracker`, `ui/ObjectivePanel`); minimap moved to the bottom-right.
   - Fire spread chance is per level (8 / 15 / 20 / 25 %).
4. **Training level** (`assets/levels/tutorial.txt`, `logic/Tutorial.java`): 12 prompts, one thing at a time. The menu has START GAME (training first) and SKIP TRAINING.
5. **Wrap-up:**
   - Time bonus removed from scoring.
   - How to Play has 4 columns: controls, roles, survivors, building.
   - Plan updated to v4; README updated.

## IN PROGRESS
Nothing half-done.

## DECISIONS MADE
- 0007: one rescuer on the map, switching role.
- 0008: triage types as subclasses, with trapped as a flag.
- 0009: backup arrives after a fixed countdown.
- 0010: start at the exit, survivors grouped in rooms.
- Untested numbers (need a playtest, all marked *(tune)* in the plan):
  - targets 3 / 4 / 4 out of 4 / 5 / 6 survivors;
  - drain 1 / 2 / 3 points per tick (1 once stabilised);
  - help range 12 tiles, a call every 6 s.

## NEXT
1. **Playtest the v4 changes by hand.** Only scripted key presses and screenshots were done. Nobody has played a full level by hand yet.
2. Tune the *(tune)* numbers from that playtest. Run `gradlew test` after any map edit, because `LevelBalanceTest` checks every level.
3. Sprites, once the team says go. The survivor sprite is now three keys (`survivor_minor`, `survivor_serious`, `survivor_critical`); plan section 10 and `AssetManager` are already updated.
4. Merge `libgdx` into `main` when happy.

## READ FIRST
- `RESCUE_GRID_PLAN.md`: the "What changed in v4" box at the top, then sections 4.3, 4.4 and 5.
- `decisions/0007`–`0010`.

## GOTCHAS
- **Building on this PC:** the `java` on PATH is a JRE 8, which can't compile. Use the Corretto JDK:
  `JAVA_HOME=/c/Users/PC/.jdks/corretto-22.0.2 ./gradlew test -Porg.gradle.java.installations.paths=C:/Users/PC/.jdks/corretto-22.0.2`
  (IntelliJ's Gradle JVM setting avoids this.)
- **Fire follows the level's seed only until the first aftershock warning.** Planning collapses uses the same `Random`, which makes the fire after that point vary. The 40-second fire test relies on this.
- **Level symbols:** `S` (serious), `m` (minor), `!` (critical) and `T` (trapped) are fixed survivors. The types of the `?` survivors come from `LevelData`'s `randomSurvivorTypes` string. `C` is cracked floor, not critical.
