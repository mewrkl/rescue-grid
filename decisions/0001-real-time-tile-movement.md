# 0001: Real-time tile movement instead of turn-based action points

- Status: Accepted
- Date: 2026-11-08
- Source: teacher feedback on RESCUE_GRID_PLAN.md v1; team discussion

## Context
Rescue Grid is a 4-week, first-Java-project game for an OOP course, written in plain Swing with no game engine. In v1 of the plan:
- The whole building was visible at once.
- Each rescuer spent action points (AP) to move.
- The world changed only when the player pressed End Turn.

The teacher asked for two things:
- **Exploration:** you shouldn't see the full map. The camera follows the player, and a minimap shows where they are.
- **A map that visibly changes during a rescue.** Aftershocks and fire should block paths you were using.

The team pictures the game as a much simpler Soul Knight: walk around a bigger map in real time. This record is not superseding an earlier one. The turn-based design was only in the v1 plan document, never in a record.

## Options considered
- **Keep turn-based, add camera and fog.**
  - Deterministic and easy to test and to explain.
  - The v1 rules and the recursive AP movement range carry over unchanged.
- **Step-based (roguelike):** each key press moves one tile, and the world updates every few steps.
  - Feels more like exploring.
  - Still fully deterministic, and bugs reproduce exactly.
- **Real-time on tiles:** hold a key to walk tile by tile, and the world updates on a timer (a "world tick" every 3 s).
  - Matches what the team pictured and what the teacher described.
  - Movement stays on the grid, so no collision maths is needed.
  - One `javax.swing.Timer` drives everything on the Swing thread, so there are no threads to manage.

## Decision
Real-time tile movement. A single Swing Timer runs the game loop. Hazards fire, survivor condition, and aftershocks all run on a 3-second world tick, in the same fixed order v1 used for End Turn.

## Consequences
- **Gained:**
  - It feels like the game the team pictured.
  - Fog and a following camera make sense, because you physically walk into the dark.
  - Aftershocks hit while you're mid-rescue, which is exactly the "path got blocked" moment the teacher asked for.
- **What we gave up:**
  - **AP is gone**, along with the AP turn system that v1 listed as never-cut. Cooldowns replace it.
  - **Reproducibility:** the same inputs no longer give the same game, because timing depends on human reaction. The seeded `Random` helps but doesn't fully fix this.
  - **The "plan your whole turn" puzzle feel** is gone.
- **New work:** about 25–35% more, mostly a game loop, held-key input, a camera, fog and a minimap.
- **Recursion needed a new home.** The recursive flood fill moved from computing movement range to computing line of sight and the Dog Handler's scent.
- **Proposal risk:** the submitted proposal may describe the game as turn-based. Confirm the change with the teacher.

## Generalizes to
**Discrete vs. continuous time in a simulation or game.** Choose real-time-on-a-grid when the experience depends on pressure and reaction, and when a single-threaded tick loop is available, so concurrency costs nothing extra.

This does not hold when:
- exact replay or determinism is a requirement (automated tests of whole sessions, competitive fairness, debugging by replay), or
- the core interest is planning rather than reacting.

In those cases, prefer turn-based or step-based time, which can still use a scrolling camera and fog.
