package rescuegrid.model;

/**
 * Which situation the game is in right now.
 *
 * GamePanel checks this every frame: the world only moves while PLAYING.
 */
public enum GameState {
    MENU,
    PLAYING,
    PAUSED,
    LEVEL_COMPLETE,
    GAME_OVER
}
