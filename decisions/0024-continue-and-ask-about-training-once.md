# 0024: Continue, and ask about training once

- Status: Accepted
- Date: 2026-11-16
- Source: user feedback ("skip training... I don't think that's something real games do", "music off should be in an options menu"), my own critique, and the user's choices in chat

## Context
The main menu had START GAME, SKIP TRAINING, LEVEL SELECT, HOW TO PLAY, MUSIC: ON/OFF and QUIT. START GAME always started the training, even for a player who had finished it, so a returning player had to know to use Level Select. SKIP TRAINING sat on the menu forever, and the only setting (music on/off) was a menu item. Most PC games instead offer:
- CONTINUE once a save exists;
- a tutorial offered once, to new players;
- settings in an OPTIONS screen.

## Options considered
- **CONTINUE + PLAY, ask once:**
  - CONTINUE appears once there is progress and goes to the furthest open level.
  - The first PLAY asks "Play the training first? Yes (recommended) / Skip to Level 1".
  - The answer, or finishing the training, is saved, and PLAY then goes straight to Level 1.
- **PLAY asks every time:** simpler, and no saved flag. But a returning player answers the same question on every run.
- **Keep START GAME and move SKIP TRAINING into Options:** the least change, but it still sends returning players to the tutorial by default.

## Decision
CONTINUE + PLAY, asked once. The menu is CONTINUE - LEVEL n (only with progress), PLAY, LEVEL SELECT, OPTIONS, HOW TO PLAY, QUIT.
- `Progress` keeps `trainingDecided`, saved by `SaveFile`; `GameFrame.play()` and `continueGame()` use it.
- **OPTIONS:** music and sound volume (saved), fullscreen (also F11, saved) and RESET PROGRESS behind an "Are you sure?".
- **The pause menu** became a real list (RESUME, RESTART LEVEL, HOW TO PLAY, OPTIONS, MAIN MENU), with confirmation before throwing a level away.

## Consequences
New players are offered the training once; returning players start where they left off in one press. Settings live where players look for them. **Given up:**
- **A direct route back to training from the title screen:** after the first time, it is only in Level Select.
- **One more saved flag that can go stale:** a player who skipped and then forgot the controls isn't asked again. How to Play and Level Select are the way back.
- **Pixel-perfect fullscreen:** it scales by whatever factor fits the screen (about 2.1x on a 2560 x 1600 display), so pixels and fonts are slightly uneven, not crisp whole-pixel steps.

## Generalizes to
**First-run vs returning-player flows: offer onboarding once, and lead with Continue.** Store whether the player has made the onboarding choice, not just whether they finished it, and make the top menu item what a returning player wants. Put settings in one Options screen reachable from both the title and pause. This does not hold for very short arcade games with no saved progress, where a single PLAY is the whole menu, or for games whose tutorial unlocks rules the levels depend on. There, the tutorial must be finished, not skipped.
