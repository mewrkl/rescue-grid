package rescuegrid.model;

/** What the game is doing right now. The world only moves while PLAYING. */
public enum GameState {
    MENU,
    PLAYING,
    PAUSED,
    LEVEL_COMPLETE,
    GAME_OVER
}
