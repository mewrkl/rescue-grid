# 0018: Stars are the only grade

- Status: Accepted
- Date: 2026-11-16
- Supersedes: 0014, only its line "the score stays on the screen for players who care"
- Source: my own audit of the game. I failed Level 2 on purpose with 0 of 5 rescued, and the result screen showed "TOTAL 300". The user chose this option in chat.

## Context
The result screen showed two grades: the three stars (decision 0014) and a points total. The total was 100 per rescue, 20 per health point left and 50 for rescuing everyone. With three roles of 5 health each, full health is worth 300 points, the same as three rescues. A player who stayed safe and rescued nobody scored as well as one who rescued three and got hurt doing it. The total also counted on a failed mission. The stars and the points could disagree, and the game's message ("save people") was muddled by a number that paid for not trying.

## Options considered
- **Stars only:**
  - One grade, so the end screen can't contradict itself.
  - Each star already names what to do better.
- **Keep the score with fixed weights** (500 per rescue, health only counting on a success):
  - Keeps a fine-grained number for players who like to compare.
  - Rescuing then clearly matters most.
- **Leave it as it was:** no work, and the score was already in the plan and the class list.

## Decision
Stars only. The result screen no longer shows points. The `ScoreKeeper` class and its score test are removed.

## Consequences
The end screen has one message: the three goals, each met or not. Nothing on screen rewards doing nothing. **Given up:**
- A fine-grained number. Two runs with the same stars now look the same, even if one finished with far more health left.
- `ScoreKeeper`, which the original plan listed as a class with one team member responsible for it. The plan and the class list are updated, but anyone marking against the old class list will see it gone.
- A natural home for a future high-score table.

## Generalizes to
**One grade or two: avoid a second number that can disagree with the first.** When a game already grades each level on named goals, a separate points total needs its own reason to exist, such as leaderboards or score attack. If it doesn't have one, it becomes a second, unchecked opinion, and its weights quietly reward the wrong play. Check every reward by asking what the laziest run earns. This does not hold for games built around chasing a number (arcade, score attack, speedrun). There the score is the game, and named goals belong in achievements.
