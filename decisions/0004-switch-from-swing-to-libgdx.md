# 0004: Build on libGDX instead of Swing

- Status: Accepted
- Date: 2026-11-09
- Source: teacher's instruction; RESCUE_GRID_PLAN.md section 3 (v3). Reverses the v1/v2 plan rule "do not use game engines or frameworks (libGDX, ...)".

## Context
The original plan required plain Java Swing and banned libGDX. A full Swing version was then built and tested: menu, three levels, camera, fog, minimap, hazards, sounds. The team then reported that the teacher wants the game built on **libGDX and libraries, not Swing**, because Swing is a poor fit for games.

The game's rules lived in packages with no Swing in them (`model`, `logic`). Only the drawing, input, sound and screens used Swing. The team chose not to commit the Swing version before the switch.

## Options considered
- **Keep Swing.**
  - Already finished and tested.
  - Needs nothing beyond the JDK, and every line, including the game loop and camera maths, is ours to explain.
- **libGDX (LWJGL3 desktop backend) with Scene2D and FreeType.**
  - What the teacher asked for, and the standard Java game framework.
  - The graphics card draws, and it comes with cameras, a UI library, audio and font rendering.
  - It opens a clear path to sprites, Tiled maps and lighting (box2dlights) later.
- **JavaFX.**
  - A modern Java UI toolkit with a canvas, less clumsy than Swing.
  - Still not a game framework, and not what the teacher named.
- **libGDX plus its game-logic libraries (Ashley entity system, gdx-ai, Box2D).**
  - Least code to write ourselves.

## Decision
Use libGDX 1.14.2 with the LWJGL3 backend, Scene2D for screens and HUD, and FreeType for fonts, built with Gradle (`core` and `lwjgl3` modules).
- The rules (`model`, `logic`) stay as they were.
- All drawing goes through one new `Painter` class.
- Class names from the proposal are kept: `GameFrame` now extends `Game`, and the `*Panel` classes are Scene2D screens.
- The rules gain JUnit 5 tests.
- Ashley, gdx-ai and Box2D are ruled out, because they would replace the class hierarchies and recursion the course grades.
- The real-time decision in `0001` is unaffected. Only its Swing Timer became libGDX's `render(delta)`.

## Consequences
- **Gained:**
  - It matches the teacher's requirement.
  - Drawing is done by the graphics card, and the UI has proper widgets (buttons with hover and keyboard focus) instead of hand-patched `JButton`s.
  - Sprites, Tiled maps and lighting are now realistic extras.
  - The rules are tested by `gradlew test` without opening a window.
- **Given up:**
  - **A simple story:** "everything is the JDK" is gone. The team must now explain Gradle, the two modules, `dispose()`, y-down cameras and nine-patch images.
  - **Easy setup:** the first build needs internet (about 150 MB of Gradle and libraries).
  - **The Swing version:** it was never committed, so there is no history to compare against or fall back to.
  - **Hand-made plumbing:** the camera maths and game loop are less "ours". libGDX's `OrthographicCamera` and `render(delta)` now do part of what we wrote by hand.
  - **Name fit:** the kept Swing-era names (`GameFrame`, `*Panel`) read oddly in libGDX.
  - **All-generated assets:** the font (Lato, SIL Open Font License) is the first third-party asset.

## Generalizes to
**Switching UI or framework partway through a project: keep the domain layer framework-free so a switch only touches the edges.**
- When rules and state never import the UI toolkit, changing toolkits is a rewrite of the presentation layer only. Here that was about half the code, and the same rule checks passed before and after.
- **This does not hold** when:
  - the logic was written inside UI classes (event handlers holding rules), or
  - the new framework imposes its own architecture, such as an entity-component system or a physics engine owning movement.

  Then it is a full rewrite, and the "keep our classes" goal conflicts with the framework.
