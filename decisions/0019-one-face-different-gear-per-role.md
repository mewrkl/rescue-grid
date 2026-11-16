# 0019: One face, different gear for each role

- Status: Accepted
- Date: 2026-11-16
- Source: my own audit of the game against how TF2 and Overwatch make roles readable from their outline. The user chose this option in chat.

## Context
The player controls one rescuer who changes role (decision 0007). The first sprites (decision 0016) drew the Medic, Engineer and Dog Handler with the same body, face, hair and build. Only the shirt colour, the hat and a small badge differed. In games with roles, you can usually tell a role from its outline alone. That matters here because the role decides what E does, and the player glances at the rescuer to check.

## Options considered
- **Same face, different gear:**
  - It is one person putting on different kit, so the face stays.
  - The gear changes the outline.
- **Three different people**, each with a different hairstyle, skin tone and build, plus gear:
  - The easiest to tell apart.
  - More variety on screen.
- **Leave them as they were:** no work, and colour already tells them apart for most players.

## Decision
Same face, different gear:
- **Medic:** a big red backpack with a white cross, wider than the shoulders from the front and showing at the back from the side, and a stethoscope.
- **Engineer:** a tool belt with pouches, a crowbar in hand and a headlamp on the hard hat.
- **Dog Handler:** a charcoal K9 vest over the blue shirt, and a red leash that the game draws from the hand to the dog's collar.
- **The backup stretcher team (decision 0020):** paramedic green with white helmets. Not orange, which would look like the Engineer.

## Consequences
Each role now has its own shape: a pack bump, a long bar, and a leash leading to the dog. Colour stops being the only way to tell them apart, which also helps colour-blind players. **Given up:**
- The variety of three different people.
- A strict "one person" look: the gear makes the roles read almost as three characters anyway.
- At 16 px, details like the stethoscope and headlamp are a pixel or two and easy to miss; the pack and crowbar do the real work.
- The leash is drawn by the game, not the sprite, so it is one more thing to keep in step with the dog's position.

## Generalizes to
**Readable roles: change the outline, not just the colour.** When players must tell several classes apart at a glance, give each one a prop that changes its outline (something carried, worn on the back, or attached), and keep colour as a second cue. When the classes are the same person switching kit, keep the face and change the gear, so the story and the readability agree. This does not hold when roles are meant to be hidden (stealth, social deduction), or when the sprites are so small that an outline change is lost. There, use an icon over the head instead.
