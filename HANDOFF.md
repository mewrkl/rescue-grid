# Handoff: 2026-11-16

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
4. **Training level** (`assets/levels/tutorial.txt`, `logic/Tutorial.java`): first version, later 15 strict steps in 4 chapters (decisions 0011, 0013): untaught keys are locked, taped-off doors (`1`-`3` in the map, `Barrier` tile) open at their step, calm until step 11 and timed after, retry the timed part if the survivor runs out of time. The menu has START GAME (training first) and SKIP TRAINING.
5. **Wrap-up:**
   - Time bonus removed from scoring.
   - How to Play has 4 columns: controls, roles, survivors, building.
   - Plan updated to v4; README updated.

6. **After the first hand playtest of training** (not committed yet):
   - The exit is pointed out: a pulsing green outline, and a green edge arrow when it's off-screen. The objective card goes see-through over the exit, and the camera can scroll 3 rows above the map.
   - The HUD always shows "Carrying 0/1" and "Following you 0/1".
   - Training is now gated step by step (decision 0011).
   - Fixed: role keys, Q and E did nothing at all. Each key press was read twice and `KeyboardInput.wasPressed` uses it up. Each key is now read once per frame; `KeyboardInputTest` documents the trap.

7. **After the second hand playtest** (not committed yet, 77 tests pass):
   - **Survivors block the way** (decision 0012, all levels): you walk into them instead of over them. Next to one, a box under them says what they need. `survivorsNeverBlockTheWay` checks no survivor spot cuts a level off.
   - **Training guidance** (decision 0013):
     - one white marker at a time with an edge arrow;
     - the camera glides to a door that opens;
     - "DONE!" after each step;
     - 4 chapters with title cards;
     - 15 progress dots.
   - **Dog Handler step:** the hidden survivor is now behind rubble in one of three rooms under the hall. The dog finds which room, then a new step has the Engineer dig them out. The map changed at the bottom.
   - **Training card no longer hides the map:** the camera knows how much of the bottom the card covers (`Camera.setBottomCover`). It keeps the rescuer centred above the card and may scroll past the map's bottom edge. Edge arrows stay above the card too. The Dog Handler's rooms (the map's bottom rows) were hidden before.
   - **Music mute:** MUSIC: ON/OFF in the main menu, N on the pause card. Saved in libGDX Preferences (`rescuegrid`), so it's remembered. Sound effects stay on.
   - **Not seen in the real game yet:** posting keys to the window didn't get past the main menu, so only the How to Play screen was checked by screenshot.
     - Check by hand: the camera glide, the chapter card, the dots and the need box.
     - Check the pause card's size: it now sits in a `Stack` and is wrapped in a `Table` so it isn't stretched.

8. **Ability practice, stars, level select and guidance** (2026-11-16, not committed yet, 82 tests pass):
   - **Training tests each ability in the first room** (decision 0015): the roles step became three steps (17 in all). Press 2, clear the marked practice rubble; press 3, sniff; press 1, treat the marked dummy (`TrainingDummy`, symbol `d`, new `Tile.isTreatable()` hook used by `Medic`). Each E is unlocked only on its own test step, then again where it is used for real.
   - **Stars** (decision 0014): `MissionReport` gives one star each for the target, everyone, and no role down; a failure gets none, training has none. It also gives the failure reason and a tip.
   - **Result screen** rebuilt: stars fill one by one with a ding, goals with ticks next to the score, reason and tip, "NEW BEST", and Next Level / Play Again / Level Select / Main Menu. Before it, "MISSION COMPLETE/FAILED" shows over the map for 1.6 s (`GameState.ENDING`).
   - **Level Select** (`LevelSelectPanel`) with best stars; `Progress` + `SaveFile` (libGDX Preferences, file `rescuegrid`) remember open levels and stars. Training and Level 1 are always open.
   - **Briefing card** before Levels 1–3 (`GameState.BRIEFING`): target, aftershock timing, `LevelData.getWhatsNew()`, the three goals; Enter starts the clock.
   - **Warnings:** a found survivor at 30 s and 10 s left beeps (`time_warning`, new in `SoundMaker`) with a message, and pulses red on the map with the seconds. **Radio hint** after 20 s with no progress (not in training).
   - **Sprite plan** written in plan section 10: 17 sprites, one pose each, fire 2 frames, made by a planned `tools/SpriteMaker.java`. Not started: waiting for the team's go-ahead.
   - **Checked in the real game by screenshot:** main menu with LEVEL SELECT, the level select screen, the training's first room (17 steps, practice rubble), and the Level 1 briefing (the first version pushed the HUD off screen; it now floats on its own layer, with the map dimmed and the objective card hidden behind it). Enter starts the level and the clock.
   - **Not seen yet:** the result screen with stars, the end banner, the practice steps past step 1, the fading pulse and the radio hint (the game itself was not driven with keys).

9. **Our own sprites** (2026-11-16, decision 0016, not committed yet, 86 tests pass):
   - **The tool:** `tools/SpriteMaker.java` draws 19 sprite sheets into `assets/images/`, plus `tools/sprite_preview.png`.
   - **The look:** the angled RPG view, 16 × 16 tiles and 16 × 24 people, a dusty disaster palette. Rescuers face 4 ways and walk; the dog walks beside the Dog Handler.
   - **Code:**
     - `AssetManager.getFrame` cuts the sheets into frames.
     - `Tile.drawOnBoard` draws shadows under walls, and `Wall.getPieces` picks a wall's look from its neighbours.
     - `Rescuer` and `Survivor` have `drawOnMap`, and `GamePanel.drawPeople` draws people top row first.
     - Furniture is `Prop` (13 letters, blocks walking but not sight or scent) and floor decorations are `DecoratedFloor` (4 symbols). Sight and scent now use `Tile.isSeeThrough`.
   - **Maps:** furniture in all four maps. Level 1's sofa was moved after the fire test failed. `LevelBalanceTest` now counts furniture letters as blocked. `FurnitureAndWallsTest` is new.
   - **Seen in the real game:** the menu, training steps 1–5 (all three ability tests passed by hand-posted keys), Level 1 with furniture, fire and survivors, and How to Play.
   - **Screenshots:** Windows window capture returned white this time, so screenshots were taken with a temporary framebuffer dump in `GameFrame`. It has been removed.
   - **Not seen:** Levels 2 and 3 with furniture, a survivor following you, carrying, and the result screen.

10. **Survivors that look like they need help** (decision 0017, not committed yet, 86 tests pass):
    - Researched Stardew, Pokémon, RimWorld, Into the Breach and pixel art guides; the user chose every option.
    - **Survivors:**
      - green: sling, bloodied head bandage, waving and shouting;
      - yellow: slumped, holding a bleeding leg, rocking;
      - red: pale, lying, arm up, blood beside them;
      - all: dusty torn clothes, and a bouncing "!" bubble (`emote.png`, `Survivor.drawHelpBubble`) while found and waiting.
    - **Everyone:** hue-shifted shading and tinted outlines (`SpriteMaker.shade` / `light` / `outlineTinted`).
    - **Props:** redrawn, with a damaged version of each (26 frames); `Prop` shows about a third damaged. Marker labels moved up above the bubble.
    - **Seen in the real game** (with the framebuffer dump again, now removed): Level 1's critical survivor with bubble and countdown, damaged furniture.
    - **Not seen in the game:** the yellow and green survivors; only checked in zoomed sheet previews.

11. **Audit against game-design standards** (2026-11-16, decisions 0018–0021, not committed yet, 89 tests pass). The user chose the fixes in a question form, then went to sleep and said to approve the details myself:
    - **Stars only** (0018): a failed Level 2 with 0 rescued scored "TOTAL 300" from health alone. The points total and `ScoreKeeper` are gone; the result screen grades with the three stars.
    - **Scent** (0021): faint paw prints show the 8-step range on explored tiles. A miss recharges in 2 s instead of 8. The start hint explains the range instead of saying "press E" where nothing can be found.
    - **Dimmed keys** (0021): the E row is half-dimmed while recharging or with nothing to act on (`Rescuer.hasSpecialTarget`), and the Q row unless a stabilised red survivor is right there (`Rescuer.canCallBackup`, `Survivor.canCallBackup`).
    - **HUD:**
      - The objective checklist and the minimap moved into the side panel; only a one-line "next step" stays on the map.
      - Labels: "Time", "NEXT AFTERSHOCK … 6 s", "dashed = extra", "TEAM HEALTH", "(you)", "recharging", "MAP".
    - **Role gear** (0019): the Medic has a red backpack and a stethoscope, the Engineer a crowbar, tool belt and headlamp, and the Dog Handler a charcoal K9 vest with a red leash to the dog (drawn by `DogHandler.drawLeash`).
    - **Backup** (0020), new `ui/BackupTeam`:
      1. The rescuer raises a radio (bubble), and a red flare with the seconds left appears by the survivor.
      2. A green paramedic stretcher team walks in from the EXIT (breadth-first search) to arrive at 10 s.
      3. They lift the survivor and carry them out, and "Saved!" rises from the EXIT.
      - The team is only a picture; the countdown still decides the rescue.
      - The "!" bubble hides once backup is called.
    - **Art:**
      - Wall tops are now a lighter cap of stone slabs; they used to look like holes.
      - The title screen has a dark band behind the menu and the team at 4x taking turns to step forward (`ui/TeamLineup`).
      - A puff of dust covers the 1 s role change.
    - **Calls I made without asking** (the user was asleep):
      - The backup team is paramedic green, not the orange I first offered, so it can't be mistaken for the Engineer.
      - The Dog Handler's vest is charcoal, so it stands out from the blue shirt.
    - **Seen in the real game:**
      - the menu;
      - Level 1's new HUD (it fits after two layout fixes);
      - the paw prints, the puff, the leash, the Engineer's gear;
      - the whole backup sequence, set up with a temporary debug key (now removed);
      - the stars-only result screen.
    - **Not seen:**
      - the backup sequence played normally, without the debug setup;
      - training's backup step with the new visuals;
      - Level 3's smaller minimap.

12. **Menus and small things, like real games** (2026-11-16, decisions 0022–0024, not committed yet, 92 tests pass). The user asked for a critique of the small things too, then chose these fixes:
    - **Main menu:** CONTINUE - LEVEL n (only with progress), PLAY, LEVEL SELECT, OPTIONS, HOW TO PLAY, QUIT.
      - The first PLAY asks "Play the training first?" (`ui/TrainingPromptPanel`).
      - Finishing or skipping the training is saved (`Progress.trainingDecided`, `SaveFile`), and PLAY then goes straight to Level 1.
      - SKIP TRAINING and MUSIC: ON/OFF are gone from the menu.
    - **Options** (`ui/OptionsPanel`), from the menu or the pause menu:
      - Music and sound volume (Left/Right, 10 % steps, saved). An old "music off" save becomes music 0 %.
      - Fullscreen (also F11 anywhere, saved). Every stage uses a `FitViewport` at 1216 × 704, and the map's GL viewport follows it.
      - RESET PROGRESS behind an "Are you sure?".
    - **Pause menu:** a real `MenuList` (RESUME, RESTART LEVEL, HOW TO PLAY, OPTIONS, MAIN MENU).
      - Restart and Main Menu ask first ("NO, GO BACK" is the default).
      - How to Play and Options return to the paused level (`GameFrame.resumeGame`).
    - **Calmer map** (0022):
      - One marker per survivor: the red countdown or the flare replaces the "!" bubble.
      - Paw prints only while Scent is ready.
      - No message for a role change, for backup arriving, or for a low-time warning about someone on screen.
      - Training's HUD says aftershocks are "off" instead of a frozen 60 s.
    - **Training** (0023): 17 steps merged into 11 by idea, with the card changing per part (`Tutorial.part`). The three ability tests are unchanged.
    - **Seen in the real game:**
      - the new main menu and the training question;
      - Options (volumes, fullscreen);
      - the pause menu, its "Are you sure?", and Options from pause returning to the same moment;
      - fullscreen on a 2560 × 1600 screen and back;
      - the training's "STEP 1 / 11".
    - **Watch out:** running the game uses your real save file (`~/.prefs/rescuegrid`). I backed it up first and restored it after my test run.
    - **Not seen:**
      - CONTINUE with real progress;
      - RESET PROGRESS in the game;
      - the merged training steps played through by hand (the training test plays all 11).

## IN PROGRESS
Nothing half-done.

## DECISIONS MADE
- 0007: one rescuer on the map, switching role.
- 0008: triage types as subclasses, with trapped as a flag.
- 0009: backup arrives after a fixed countdown.
- 0010: start at the exit, survivors grouped in rooms.
- 0011: training gated step by step, calm until a timed final part.
- 0012: survivors block the way; walk into them to help.
- 0013: training points at one thing at a time and marks progress.
- 0014: stars for three separate goals.
- 0015: test each role's ability in the first room.
- 0016: our own pixel art in the angled view, with animation and furniture.
- 0017: survivors look like they need help (hurt poses, "!" bubble, damaged props).
- 0018: stars are the only grade (the points total is gone).
- 0019: one face, different gear for each role.
- 0020: the backup team is seen walking in, as a picture only.
- 0021: abilities show when and how far they work (scent range, cheap misses, dimmed keys).
- 0022: one marker per survivor, and fewer repeated messages.
- 0023: training merges related steps (17 to 11).
- 0024: Continue, and ask about training once (plus Options and a real pause menu).
- Untested numbers (need a playtest, all marked *(tune)* in the plan):
  - targets 3 / 4 / 4 out of 4 / 5 / 6 survivors;
  - drain 1 / 2 / 3 points per tick (1 once stabilised);
  - help range 12 tiles, a call every 6 s.

## NEXT
1. **Playtest by hand:** the training's three practice steps, a full level to the result screen (stars filling in, NEW BEST, the tip), Level Select after finishing a level, the 30 s / 10 s warnings and the radio hint.
2. Tune the *(tune)* numbers from that playtest. Run `gradlew test` after any map edit, because `LevelBalanceTest` checks every level.
3. Sprites: playtest Levels 2 and 3 for readability (cracked floor, faced-tile outline, people covering the tile above). A carrying pose is the main missing piece. Watch whether the backup team walking through fire bothers anyone (decision 0020).
4. Merge `libgdx` into `main` when happy.

## READ FIRST
- `RESCUE_GRID_PLAN.md`: the "What changed in v4" box at the top, then sections 4.3, 4.4 and 5.
- `decisions/0007`–`0024`.

## GOTCHAS
- **Building on this PC:** the `java` on PATH is a JRE 8, which can't compile. Use the Corretto JDK:
  `JAVA_HOME=/c/Users/PC/.jdks/corretto-22.0.2 ./gradlew test -Porg.gradle.java.installations.paths=C:/Users/PC/.jdks/corretto-22.0.2`
  (IntelliJ's Gradle JVM setting avoids this.)
- **Fire follows the level's seed only until the first aftershock warning.** Planning collapses uses the same `Random`, which makes the fire after that point vary. The 40-second fire test relies on this.
- **Level symbols:** `S` (serious), `m` (minor), `!` (critical) and `T` (trapped) are fixed survivors. The types of the `?` survivors come from `LevelData`'s `randomSurvivorTypes` string. `C` is cracked floor, not critical. `1` `2` `3` are training doors.
