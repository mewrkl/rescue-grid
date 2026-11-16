# 0011: Training is gated step by step, calm until a timed final part

- Status: Accepted
- Date: 2026-11-15
- Source: playtest of the first training level; the user chose each option in chat

## Context
The first training level (decision 0010) showed one prompt at a time but did not hold the player to it. Anything done early counted, every room was open and every key worked. In the first playtest the player wandered ahead. They reached the critical (red) survivor while carrying another one and couldn't tell why they couldn't pick them up. They also lost track of the exit and missed that they could change role. Survivors were also weakening and aftershocks were coming while the player was still reading.

## Options considered
- **Prompts in order only:** each prompt waits for its own action and nothing is locked. This is the least code, and players can still explore. But nothing stops them walking into a room before its lesson.
- **Lock untaught keys only:** E, Q and the role keys do nothing until their step. This stops actions used out of order, but the player can still walk ahead and find survivors early.
- **Lock keys and doors:** the same, plus taped-off doors (a new `Barrier` tile) that open when their step begins. The player physically can't get ahead of the lesson.
- For pressure: keep a slow drain all the way through; no pressure at all; or **calm, then a timed final part**.

## Decision
Lock keys and doors. Training is 14 strict steps:
- Directions are tried one by one, then every role.
- Each role's E unlocks at the step that teaches it, and Q unlocks at the backup step.
- Three gates open at the steps that need them.
- Steps 1–10 are calm: no drain, no fire spread or fire damage, no aftershock timer, and calls for help only during the "follow the call" step.
- From step 11 the clock runs. Survivors weaken, and one aftershock is triggered next to the player as its own step.
- If the last survivor runs out of time, the survivor and the player go back to the start of the timed part. Training itself never fails.

## Consequences
New players can't get lost or confused by things they haven't been taught yet. They still feel the clock once they know every control. The level code gains a `Barrier` tile and the `Tile.opensWithGate` hook. `TurnManager` gains switches for drain, hazards and help calls, plus a scripted aftershock. `Survivor` gains `resetForRetry`.

**Given up:**
- Free exploration in training: a confident player can't skip ahead and must try every key and role.
- Fire spread and the aftershock timer are never shown working in training. They're only mentioned in the final prompt.
- The whole-level balance check no longer covers training. A separate test checks only the timed part (17 s of the 75 s available).

## Generalizes to
**Onboarding: gated (rail-roaded) tutorial vs. sandbox tutorial.** Gate both actions and space when the game has several interacting rules and a failed first run costs more than a slower one. Bring pressure in only after every control has been used once.

This does not hold for players who already know the genre, or for games whose fun is discovery. There, a skippable sandbox or contextual hints respect the player more than a locked path.
