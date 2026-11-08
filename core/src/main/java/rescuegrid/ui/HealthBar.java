package rescuegrid.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

import rescuegrid.graphics.Painter;

/**
 * A row of small squares, one per health point: filled = health left, faint = lost.
 *
 * Extends Actor, the base class of everything in a Scene2D stage, and overrides
 * draw() to paint itself (inheritance and polymorphism inside the library).
 */
class HealthBar extends Actor {

    private static final float SQUARE = 10;
    private static final float GAP = 4;
    private static final Color LOST = Painter.rgba(255, 255, 255, 40);

    private final TextureRegion pixel;
    private final int maxHealth;
    private int health;
    private Color filledColor;

    HealthBar(TextureRegion pixel, int maxHealth) {
        this.pixel = pixel;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.filledColor = UiStyle.TEXT;
        setSize(maxHealth * SQUARE + (maxHealth - 1) * GAP, SQUARE);
    }

    void setHealth(int health, Color filledColor) {
        this.health = health;
        this.filledColor = filledColor;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        for (int i = 0; i < maxHealth; i++) {
            Color color = i < health ? filledColor : LOST;
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            batch.draw(pixel, getX() + i * (SQUARE + GAP), getY(), SQUARE, SQUARE);
        }
        batch.setColor(Color.WHITE);
    }
}
