# 0014: Stars for three separate goals

- Status: Accepted (its "score stays on the screen" line superseded by 0018)
- Date: 2026-11-16
- Source: the user asked for a result screen with stars ("only got two out of three because they didn't get all the survivors") and chose this option in chat

## Context
A finished level jumped straight to a plain result screen with a score. The score (100 per rescue, 20 per health point, 50 bonus for everyone) doesn't tell a player what to do better, and a mission went straight on to the next one. The user wanted a star rating out of three, with missing survivors costing a star. The levels set the limits: Level 1 needs 3 of 4 survivors, Level 2 4 of 5, Level 3 4 of 6, so "target + 1" already means "everyone" in two of the three levels.

## Options considered
- **Three separate goals:** one star each for rescuing the target, rescuing everyone, and no role going down. Each star names its goal, so the screen says exactly what was missed. The same three goals can be shown before the level as a promise.
- **Survivors only:** 1 star for the target, 2 for one more, 3 for everyone. The simplest to read, and it matches the user's own example closely.
- **Score thresholds:** stars at, for example, 60 %, 80 % and 95 % of the best possible score. Fine-grained, and it reuses the existing score, so every point counts.

## Decision
Three separate goals. `MissionReport` works them out from the board at the end. A failed mission earns no stars, and training has none. The goals are listed on the level's briefing card before the clock starts, and ticked off one by one on the result screen. `Progress` keeps the best stars per level, saved by `SaveFile` in libGDX Preferences, and the Level Select screen shows them.

## Consequences
Every star is a sentence the player can act on ("Rescue all 4", "No role goes down"), and the result screen's tip points at the first missing one. Being careful (keeping every role standing) counts as well as rescuing. **Given up:**
- The survivors-only option's direct link between survivors and stars. Here, saving everyone while losing a role still gives 2 stars.
- The thresholds' fine-grained reward: 20 health points left and 100 left score differently but get the same stars. The score stays on the screen for players who care.
- "No role goes down" is a hard yes/no: losing one role by a single point costs a whole star.
- A save file now exists: a stale or hand-edited file could show wrong stars (values are clamped to 0–3 on load).

## Generalizes to
**Level ratings: separate named objectives vs a single scale.** When the point of a rating is to tell the player what to try next time, give one star per named, checkable goal and show the goals before play. That works for short, replayable levels with only a few things that matter. It does not hold when the goals pull against each other so much that a top score is luck, or when there is one main skill to rank. A speedrun or a score attack is better served by thresholds on one number.
