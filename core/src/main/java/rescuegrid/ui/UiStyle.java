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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import rescuegrid.graphics.Painter;

/**
 * Colours, fonts and widget styles shared by every screen.
 * Pixel fonts only stay sharp at whole-pixel sizes: multiples of 8 for
 * Press Start 2P, and 19 for Jersey 10.
 */
public class UiStyle implements Disposable {

    public static final Color BACKGROUND = Painter.hex(0x0A0A0A);
    public static final Color SURFACE = Painter.hex(0x161616);
    public static final Color BORDER = Painter.rgba(255, 255, 255, 23);
    public static final Color TEXT = Painter.hex(0xEDEDED);
    public static final Color TEXT_SECONDARY = Painter.hex(0xA0A0A0);
    public static final Color WARNING = Painter.hex(0xF59E0B);
    public static final Color ERROR = Painter.hex(0xF87171);

    /** The one accent colour: the menu option that is selected right now. */
    public static final Color SELECTED = Painter.hex(0xFBBF24);

    private static final Color KEYCAP_FILL = Painter.hex(0x1F1F1F);
    private static final Color KEYCAP_BORDER = Painter.rgba(255, 255, 255, 51);
    private static final int CARD_RADIUS = 12;
    private static final int KEYCAP_RADIUS = 6;
    private static final int TOAST_RADIUS = 8;
    private static final float METER_HEIGHT = 6;
    private static final Color METER_TRACK = Painter.rgba(255, 255, 255, 40);

    private static final String TEXT_FONT = "fonts/Jersey10-Regular.ttf";
    private static final String PIXEL_FONT = "fonts/PressStart2P-Regular.ttf";
    private static final String EXTRA_CHARACTERS = "·–—’…→";
    private static final int TEXT_SIZE = 19;
    private static final int[] PIXEL_SIZES = {8, 16, 24, 32, 48};

    private final HashMap<String, BitmapFont> fonts;
    private final HashMap<String, Label.LabelStyle> labelStyles;
    private final ArrayList<Texture> textures;
    private final BitmapFont signFont;
    private final TextureRegion whitePixel;
    private final Drawable keycapBackground;
    private final Label.LabelStyle toastStyle;
    private final ProgressBar.ProgressBarStyle meterStyle;

    public UiStyle() {
        fonts = new HashMap<>();
        labelStyles = new HashMap<>();
        textures = new ArrayList<>();

        FreeTypeFontGenerator text = new FreeTypeFontGenerator(Gdx.files.internal(TEXT_FONT));
        FreeTypeFontGenerator pixel = new FreeTypeFontGenerator(Gdx.files.internal(PIXEL_FONT));
        fonts.put(TEXT_KEY, makeFont(text, TEXT_SIZE, false));
        for (int size : PIXEL_SIZES) {
            fonts.put(pixelKey(size), makeFont(pixel, size, false));
        }
        signFont = makeFont(pixel, 8, true); // flipped, for the y-down game world
        text.dispose();
        pixel.dispose();

        whitePixel = new TextureRegion(solidTexture(Color.WHITE));
        keycapBackground = padded(roundedBox(KEYCAP_FILL, KEYCAP_BORDER, 1, KEYCAP_RADIUS), 8, 4);
        toastStyle = new Label.LabelStyle(find(TEXT_KEY), TEXT);
        toastStyle.background = padded(roundedBox(SURFACE, BORDER, 1, TOAST_RADIUS), 16, 8);
        meterStyle = new ProgressBar.ProgressBarStyle();
        meterStyle.background = solid(METER_TRACK);
        meterStyle.background.setMinHeight(METER_HEIGHT);
        meterStyle.knobBefore = solid(Color.WHITE);
        meterStyle.knobBefore.setMinHeight(METER_HEIGHT);
    }

    private static final String TEXT_KEY = "text";

    private static String pixelKey(int size) {
        return size + "p";
    }

    /** Pixel fonts are drawn without smoothing ("mono"), so their square pixels stay square. */
    private static BitmapFont makeFont(FreeTypeFontGenerator generator, int size, boolean flip) {
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.flip = flip;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + EXTRA_CHARACTERS;
        parameter.minFilter = Texture.TextureFilter.Nearest;
        parameter.magFilter = Texture.TextureFilter.Nearest;
        parameter.mono = true;
        parameter.hinting = FreeTypeFontGenerator.Hinting.None;
        return generator.generateFont(parameter);
    }

    private BitmapFont find(String key) {
        BitmapFont font = fonts.get(key);
        if (font == null) {
            throw new IllegalArgumentException("No font " + key);
        }
        return font;
    }

    /** The small pixel font used to write on tiles (the EXIT sign). */
    public BitmapFont getSignFont() {
        return signFont;
    }

    /** Body text in Jersey 10. The colour can be changed later with setColor. */
    public Label label(String text, Color color) {
        Label label = new Label(text, textStyle());
        label.setColor(color);
        return label;
    }

    /** Pixel-font text for titles, headings and menu options. Size must be 8, 16, 24, 32 or 48. */
    public Label pixelLabel(String text, int size, Color color) {
        Label label = new Label(text, pixelStyle(size));
        label.setColor(color);
        return label;
    }

    /** One shared style per font (Scene2D styles are shared between widgets, never edited). */
    public Label.LabelStyle textStyle() {
        return labelStyles.computeIfAbsent(TEXT_KEY, k -> new Label.LabelStyle(find(TEXT_KEY), Color.WHITE));
    }

    public Label.LabelStyle pixelStyle(int size) {
        String key = pixelKey(size);
        return labelStyles.computeIfAbsent(key, k -> new Label.LabelStyle(find(key), Color.WHITE));
    }

    /** A keyboard key drawn as a small rounded "keycap" with its name on it, e.g. [W] or [ESC]. */
    public Label keycap(String key) {
        Label.LabelStyle style = new Label.LabelStyle(find(TEXT_KEY), Color.WHITE);
        style.background = keycapBackground;
        Label label = new Label(key, style);
        label.setColor(TEXT);
        label.setAlignment(Align.center);
        return label;
    }

    /** A short message floating over the map (see Toast). */
    public Label.LabelStyle toastStyle() {
        return toastStyle;
    }

    /** A thin bar for Scene2D's ProgressBar: faint track, filled part tinted by the bar's colour. */
    public ProgressBar.ProgressBarStyle meterStyle() {
        return meterStyle;
    }

    /** A 1 x 1 white image. Tinted and stretched, it draws any solid rectangle. */
    public TextureRegion getWhitePixel() {
        return whitePixel;
    }

    public Drawable solid(Color color) {
        return new TextureRegionDrawable(whitePixel).tint(color);
    }

    /** Background for a floating card (the pause box): raised surface, hairline border, 24 px padding. */
    public Drawable card() {
        return padded(roundedBox(SURFACE, BORDER, 1, CARD_RADIUS), 24, 24);
    }

    /** A smaller floating card (the objective tracker): same look, 16 px padding. */
    public Drawable smallCard() {
        return padded(roundedBox(SURFACE, BORDER, 1, CARD_RADIUS), 16, 16);
    }

    private static NinePatchDrawable padded(NinePatchDrawable drawable, float horizontal, float vertical) {
        drawable.setLeftWidth(horizontal);
        drawable.setRightWidth(horizontal);
        drawable.setTopHeight(vertical);
        drawable.setBottomHeight(vertical);
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

    public void handCursor() {
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Hand);
    }

    public void resetCursor() {
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
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
