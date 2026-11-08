# 0002: Control a team of three rescuers by switching, not one rescuer per level

- Status: Accepted
- Date: 2026-11-08
- Source: team discussion after 0001 (real-time movement)

## Context
After the move to real-time movement (0001), the player can only steer one character at a time, the way Soul Knight works. The game has three rescuer subclasses promised in the course proposal:
- **Medic:** heals survivors.
- **Engineer:** clears rubble.
- **Dog Handler:** reveals hidden survivors.

The question was whether all three should be on the map at once, or whether the player should pick one per level. The team asked for a recommendation rather than having a strong preference.

## Options considered
- **One rescuer per level, chosen before the level starts.**
  - The simplest controls, and the closest to Soul Knight.
  - No inactive characters to worry about, and the HUD only needs one health bar.
- **Team of three, switch with 1/2/3 or Tab.**
  - Every ability is available in every level, so levels can require combining them.
  - Switching live shows polymorphism very directly: the same Space key runs a different `specialAction()`.
- **Team of three with AI teammates that follow you.**
  - Feels the most alive.
  - The player steers one rescuer while the others help on their own.

## Decision
A team of three with switching. Inactive rescuers stand still, and they can still be hurt by fire and aftershocks.

## Consequences
- **Gained:**
  - Levels can rely on teamwork. For example, a survivor behind rubble needs the Engineer, and a hidden side room needs the Dog Handler.
  - After an aftershock blocks a path, there is always a way forward (the Engineer clears it), but it costs time.
  - It matches the proposal's three-subclass design.
- **What we gave up:**
  - **Simpler controls:** players must manage three characters and may forget one standing next to fire. The HUD needs all three health bars and warnings to cover this.
  - **AI teammates:** the "alive" feel of followers. Pathfinding AI was too much for the time and skill budget.
  - **Replay variety:** one-rescuer-per-level would have let a player replay a level with a different character.

## Generalizes to
**Party control in single-input games: switch-control vs. pick-one vs. AI companions.** Choose switch-control when level design needs several distinct abilities in the same space and AI companions are too costly to build.

This does not hold when:
- the game is about fast moment-to-moment action, where switching breaks the flow (pick one character, or build companions), or
- abilities are meant as a replay choice rather than a puzzle requirement (pick one per run).
