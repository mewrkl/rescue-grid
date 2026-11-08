package rescuegrid.ui;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

import rescuegrid.graphics.Painter;

/**
 * Shared colours, fonts and button styles for every screen, so the menus, HUD
 * and result screen all look like the same game. Change a colour here and it
 * changes everywhere.
 *
 * Fonts: FreeType turns our .ttf file (assets/fonts) into a bitmap font at each
 * size we need, once, at startup.
 * Buttons and labels: Scene2D (libGDX's UI library) draws them using the
 * "styles" made here.
 */
public class UiStyle implements Disposable {

    public static final Color BACKGROUND = Painter.hex(0x0A0A0A);
    public static final Color SURFACE = Painter.hex(0x161616);
    public static final Color BORDER = Painter.rgba(255, 255, 255, 23);
    public static final Color TEXT = Painter.hex(0xEDEDED);
    public static final Color TEXT_SECONDARY = Painter.hex(0xA0A0A0);
    public static final Color WARNING = Painter.hex(0xF59E0B);
    public static final Color ERROR = Painter.hex(0xF87171);

    /** The one accent colour: only for the main button on each screen. */
    public static final Color ACCENT = Painter.hex(0xC2410C);
    private static final Color ACCENT_HOVER = Painter.hex(0xB23C0B);
    private static final Color ACCENT_PRESSED = Painter.hex(0xA7380A);
    private static final Color SECONDARY_BORDER = Painter.hex(0x3A3A3A);
    private static final Color SECONDARY_HOVER = Painter.hex(0x1F1F1F);
    private static final Color SECONDARY_PRESSED = Painter.hex(0x2A2A2A);
    private static final Color FOCUS_RING = Painter.hex(0x2563EB);

    public static final float BUTTON_WIDTH = 240;
    public static final float BUTTON_HEIGHT = 32;
    private static final int BUTTON_RADIUS = 6;
    private static final int CARD_RADIUS = 12;

    private static final String REGULAR_FONT = "fonts/Lato-Regular.ttf";
    private static final String BOLD_FONT = "fonts/Lato-Bold.ttf";
    private static final String EXTRA_CHARACTERS = "·–—’…→";

    private final HashMap<String, BitmapFont> fonts;
    private final HashMap<String, Label.LabelStyle> labelStyles;
    private final ArrayList<Texture> textures;
    private final BitmapFont signFont;
    private final TextureRegion whitePixel;
    private final TextButton.TextButtonStyle primaryStyle;
    private final TextButton.TextButtonStyle secondaryStyle;

    public UiStyle() {
        fonts = new HashMap<>();
        labelStyles = new HashMap<>();
        textures = new ArrayList<>();

        FreeTypeFontGenerator regular = new FreeTypeFontGenerator(Gdx.files.internal(REGULAR_FONT));
        FreeTypeFontGenerator bold = new FreeTypeFontGenerator(Gdx.files.internal(BOLD_FONT));
        for (int size : new int[] {12, 14, 16, 20, 24, 32}) {
            fonts.put(key(size, false), makeFont(regular, size, false));
            fonts.put(key(size, true), makeFont(bold, size, false));
        }
        signFont = makeFont(bold, 13, true); // flipped, for the y-down game world
        regular.dispose();
        bold.dispose();

        whitePixel = new TextureRegion(solidTexture(Color.WHITE));

        primaryStyle = buttonStyle(ACCENT, ACCENT_HOVER, ACCENT_PRESSED, ACCENT, Color.WHITE);
        secondaryStyle = buttonStyle(BACKGROUND, SECONDARY_HOVER, SECONDARY_PRESSED, SECONDARY_BORDER, TEXT);
    }

    // ----- Fonts and labels -----

    private static String key(int size, boolean bold) {
        return size + (bold ? "b" : "r");
    }

    private static BitmapFont makeFont(FreeTypeFontGenerator generator, int size, boolean flip) {
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.flip = flip;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + EXTRA_CHARACTERS;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        return generator.generateFont(parameter);
    }

    public BitmapFont font(int size, boolean bold) {
        BitmapFont font = fonts.get(key(size, bold));
        if (font == null) {
            throw new IllegalArgumentException("No font of size " + size);
        }
        return font;
    }

    /** The small bold font used to write on tiles (the EXIT sign). */
    public BitmapFont getSignFont() {
        return signFont;
    }

    /** A label in white; its colour is set with setColor, so it can change while the game runs. */
    public Label label(String text, int size, boolean bold, Color color) {
        Label label = new Label(text, labelStyle(size, bold));
        label.setColor(color);
        return label;
    }

    /** One shared style per font (Scene2D styles are shared between widgets, never edited). */
    public Label.LabelStyle labelStyle(int size, boolean bold) {
        return labelStyles.computeIfAbsent(key(size, bold),
                k -> new Label.LabelStyle(font(size, bold), Color.WHITE));
    }

    /** A 1 x 1 white image. Tinted and stretched, it draws any solid rectangle. */
    public TextureRegion getWhitePixel() {
        return whitePixel;
    }

    public Drawable solid(Color color) {
        return new TextureRegionDrawable(whitePixel).tint(color);
    }

    // ----- Buttons -----

    /** The main action of a screen. Use at most one per screen. */
    public TextButton primaryButton(String text) {
        return button(text, primaryStyle);
    }

    /** Every other button. */
    public TextButton secondaryButton(String text) {
        return button(text, secondaryStyle);
    }

    private TextButton button(String text, TextButton.TextButtonStyle style) {
        TextButton button = new TextButton(text, style);
        // A hand cursor while the mouse is over the button.
        button.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) { // -1 means "the mouse", not a touch
                    Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Hand);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    resetCursor();
                }
            }
        });
        return button;
    }

    public void resetCursor() {
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
    }

    /**
     * up = normal, over = mouse hovering, down = pressed, focused = chosen with
     * the keyboard (a blue ring, so keyboard players can see where they are).
     */
    private TextButton.TextButtonStyle buttonStyle(Color fill, Color hover, Color pressed,
                                                   Color border, Color textColor) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = buttonBox(fill, border, 1);
        style.over = buttonBox(hover, border, 1);
        style.down = buttonBox(pressed, border, 1);
        style.focused = buttonBox(fill, FOCUS_RING, 2);
        style.font = font(14, true);
        style.fontColor = textColor;
        return style;
    }

    private Drawable buttonBox(Color fill, Color border, int borderWidth) {
        NinePatchDrawable drawable = roundedBox(fill, border, borderWidth, BUTTON_RADIUS);
        drawable.setLeftWidth(16);
        drawable.setRightWidth(16);
        drawable.setTopHeight(4);
        drawable.setBottomHeight(4);
        return drawable;
    }

    /** Background for a floating card (the pause box): raised surface, hairline border, 24 px padding. */
    public Drawable card() {
        NinePatchDrawable drawable = roundedBox(SURFACE, BORDER, 1, CARD_RADIUS);
        drawable.setLeftWidth(24);
        drawable.setRightWidth(24);
        drawable.setTopHeight(24);
        drawable.setBottomHeight(24);
        return drawable;
    }

    /**
     * A small rounded rectangle image cut into a "nine patch": the corners stay
     * the same size and the middle stretches, so one tiny image fits any size.
     */
    private NinePatchDrawable roundedBox(Color fill, Color border, int borderWidth, int radius) {
        int size = radius * 2 + 3; // two corners plus a 1-pixel middle that stretches
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        fillRounded(pixmap, 0, size, radius, border);                                       // outer shape = border
        fillRounded(pixmap, borderWidth, size - 2 * borderWidth, radius - borderWidth, fill); // inner shape = fill
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        textures.add(texture);
        NinePatch patch = new NinePatch(texture, radius + 1, radius + 1, radius + 1, radius + 1);
        return new NinePatchDrawable(patch);
    }

    private static void fillRounded(Pixmap pixmap, int offset, int size, int radius, Color color) {
        pixmap.setColor(color);
        pixmap.fillRectangle(offset + radius, offset, size - 2 * radius, size);
        pixmap.fillRectangle(offset, offset + radius, size, size - 2 * radius);
        int far = offset + size - 1 - radius;
        pixmap.fillCircle(offset + radius, offset + radius, radius);
        pixmap.fillCircle(far, offset + radius, radius);
        pixmap.fillCircle(offset + radius, far, radius);
        pixmap.fillCircle(far, far, radius);
    }

    private Texture solidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        textures.add(texture);
        return texture;
    }

    @Override
    public void dispose() {
        for (BitmapFont font : fonts.values()) {
            font.dispose();
        }
        signFont.dispose();
        for (Texture texture : textures) {
            texture.dispose();
        }
    }
}
