package rescuegrid.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

/**
 * Draws images and simple shapes for every Drawable, switching between
 * SpriteBatch and ShapeRenderer as needed. Coordinates are y-down: (0, 0) is
 * the top-left corner.
 */
public class Painter implements Disposable {

    private static final int OVAL_SEGMENTS = 24;

    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final BitmapFont signFont;
    private final GlyphLayout layout;
    private boolean drawingImages;
    private boolean drawingShapes;

    /** @param signFont a small font made for y-down drawing, used for text on tiles (EXIT) */
    public Painter(BitmapFont signFont) {
        this.batch = new SpriteBatch();
        this.shapes = new ShapeRenderer();
        this.signFont = signFont;
        this.layout = new GlyphLayout();
        this.drawingImages = false;
        this.drawingShapes = false;
    }

    public static Color rgb(int red, int green, int blue) {
        return new Color(red / 255f, green / 255f, blue / 255f, 1f);
    }

    public static Color rgba(int red, int green, int blue, int alpha) {
        return new Color(red / 255f, green / 255f, blue / 255f, alpha / 255f);
    }

    /** e.g. hex(0xC2410C) */
    public static Color hex(int rgb) {
        return rgb((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    /** Everything drawn until end() uses this camera's view. */
    public void begin(Matrix4 projection) {
        batch.setProjectionMatrix(projection);
        shapes.setProjectionMatrix(projection);
    }

    public void end() {
        if (drawingImages) {
            batch.end();
            drawingImages = false;
        }
        if (drawingShapes) {
            shapes.end();
            drawingShapes = false;
        }
    }

    private void useImages() {
        if (drawingShapes) {
            shapes.end();
            drawingShapes = false;
        }
        if (!drawingImages) {
            batch.begin();
            drawingImages = true;
        }
    }

    private void useShapes() {
        if (drawingImages) {
            batch.end();
            drawingImages = false;
        }
        if (!drawingShapes) {
            // See-through colours (alpha) need blending switched on.
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            drawingShapes = true;
        }
    }

    public void image(TextureRegion region, float x, float y, float width, float height) {
        useImages();
        batch.setColor(Color.WHITE);
        batch.draw(region, x, y, width, height);
    }

    public void fillRect(Color color, float x, float y, float width, float height) {
        useShapes();
        shapes.setColor(color);
        shapes.rect(x, y, width, height);
    }

    /** A rectangle whose colour blends between its corners: (x, y), (x + w, y), (x + w, y + h), (x, y + h). */
    public void gradientRect(float x, float y, float width, float height,
                             Color corner1, Color corner2, Color corner3, Color corner4) {
        useShapes();
        shapes.rect(x, y, width, height, corner1, corner2, corner3, corner4);
    }

    /** A rectangle border drawn inside the box (x, y, width, height). */
    public void outlineRect(Color color, float x, float y, float width, float height, float thickness) {
        fillRect(color, x, y, width, thickness);                                    // top
        fillRect(color, x, y + height - thickness, width, thickness);               // bottom
        fillRect(color, x, y + thickness, thickness, height - 2 * thickness);       // left
        fillRect(color, x + width - thickness, y + thickness, thickness, height - 2 * thickness); // right
    }

    /** A border made of dashes, e.g. to mark the tile a rescuer is facing. */
    public void dashedRect(Color color, float x, float y, float width, float height,
                           float thickness, float dash) {
        for (float d = 0; d < width; d += 2 * dash) {
            float length = Math.min(dash, width - d);
            fillRect(color, x + d, y, length, thickness);
            fillRect(color, x + d, y + height - thickness, length, thickness);
        }
        for (float d = 0; d < height; d += 2 * dash) {
            float length = Math.min(dash, height - d);
            fillRect(color, x, y + d, thickness, length);
            fillRect(color, x + width - thickness, y + d, thickness, length);
        }
    }

    /** An oval that fills the box (x, y, width, height). A square box gives a circle. */
    public void fillOval(Color color, float x, float y, float width, float height) {
        useShapes();
        shapes.setColor(color);
        shapes.ellipse(x, y, width, height, OVAL_SEGMENTS);
    }

    /** The border of an oval, drawn inside the box. */
    public void outlineOval(Color color, float x, float y, float width, float height, float thickness) {
        useShapes();
        shapes.setColor(color);
        float cx = x + width / 2;
        float cy = y + height / 2;
        float outerX = width / 2;
        float outerY = height / 2;
        float innerX = Math.max(0, outerX - thickness);
        float innerY = Math.max(0, outerY - thickness);
        for (int i = 0; i < OVAL_SEGMENTS; i++) {
            float a0 = MathUtils.PI2 * i / OVAL_SEGMENTS;
            float a1 = MathUtils.PI2 * (i + 1) / OVAL_SEGMENTS;
            float ox0 = cx + MathUtils.cos(a0) * outerX;
            float oy0 = cy + MathUtils.sin(a0) * outerY;
            float ox1 = cx + MathUtils.cos(a1) * outerX;
            float oy1 = cy + MathUtils.sin(a1) * outerY;
            float ix0 = cx + MathUtils.cos(a0) * innerX;
            float iy0 = cy + MathUtils.sin(a0) * innerY;
            float ix1 = cx + MathUtils.cos(a1) * innerX;
            float iy1 = cy + MathUtils.sin(a1) * innerY;
            shapes.triangle(ox0, oy0, ox1, oy1, ix1, iy1);
            shapes.triangle(ox0, oy0, ix1, iy1, ix0, iy0);
        }
    }

    /** The top half of the oval in the box (a dome, like a hard hat). */
    public void fillTopHalfOval(Color color, float x, float y, float width, float height) {
        fillPolygon(color, topHalfOvalPoints(x, y, width, height));
    }

    /** Just the curved edge of the top half of the oval. */
    public void outlineTopHalfOval(Color color, float x, float y, float width, float height, float thickness) {
        float[] points = topHalfOvalPoints(x, y, width, height);
        for (int i = 0; i + 3 < points.length; i += 2) {
            line(color, points[i], points[i + 1], points[i + 2], points[i + 3], thickness);
        }
    }

    private float[] topHalfOvalPoints(float x, float y, float width, float height) {
        int steps = OVAL_SEGMENTS / 2;
        float[] points = new float[(steps + 1) * 2];
        for (int i = 0; i <= steps; i++) {
            // From the left (angle 180) over the top to the right (angle 360). In y-down
            // drawing, sin() of these angles is negative, which is "up".
            float angle = MathUtils.PI + MathUtils.PI * i / steps;
            points[2 * i] = x + width / 2 + MathUtils.cos(angle) * width / 2;
            points[2 * i + 1] = y + height / 2 + MathUtils.sin(angle) * height / 2;
        }
        return points;
    }

    /**
     * A filled convex polygon (no dents), given as x1, y1, x2, y2, ...
     * It is cut into triangles that all share the first corner.
     */
    public void fillPolygon(Color color, float... points) {
        useShapes();
        shapes.setColor(color);
        for (int i = 2; i + 3 < points.length; i += 2) {
            shapes.triangle(points[0], points[1], points[i], points[i + 1], points[i + 2], points[i + 3]);
        }
    }

    /** The border of a polygon: a line from each corner to the next, and back to the start. */
    public void outlinePolygon(Color color, float thickness, float... points) {
        int corners = points.length / 2;
        for (int i = 0; i < corners; i++) {
            int next = (i + 1) % corners;
            line(color, points[2 * i], points[2 * i + 1], points[2 * next], points[2 * next + 1], thickness);
        }
    }

    public void line(Color color, float x1, float y1, float x2, float y2, float thickness) {
        useShapes();
        shapes.setColor(color);
        shapes.rectLine(x1, y1, x2, y2, thickness);
    }

    /** Small text centred on centerX, with its top at topY (used for the EXIT sign). */
    public void text(String text, Color color, float centerX, float topY) {
        useImages();
        signFont.setColor(color);
        layout.setText(signFont, text);
        signFont.draw(batch, layout, centerX - layout.width / 2, topY);
    }

    /** Like text(), but on a box of colour `box`, so it can be read on any tile. */
    public void textOnBox(String text, Color color, Color box, float centerX, float topY, float padding) {
        layout.setText(signFont, text);
        float width = layout.width + 2 * padding;
        float height = layout.height + 2 * padding;
        fillRect(box, centerX - width / 2, topY - padding, width, height);
        text(text, color, centerX, topY);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
    }
}
