package rescuegrid.model;

/** What the game is doing right now. The world only moves while PLAYING. */
public enum GameState {
    MENU,
    BRIEFING,   // the level's briefing card: waits for Enter, the clock doesn't run
    PLAYING,
    ENDING,     // a moment with "MISSION COMPLETE" over the map before the result screen
    PAUSED,
    LEVEL_COMPLETE,
    GAME_OVER
}
