package rescuegrid.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.Border;

/**
 * Shared colours, fonts and button styles for every screen, so the menus, HUD
 * and result screen all look like the same game. Change a colour here and it
 * changes everywhere.
 *
 * Only used inside the ui package (no "public" on the class).
 */
final class UiStyle {

    static final Color BACKGROUND = new Color(0x0A0A0A);
    static final Color SURFACE = new Color(0x161616);
    static final Color BORDER = new Color(255, 255, 255, 23);
    static final Color TEXT = new Color(0xEDEDED);
    static final Color TEXT_SECONDARY = new Color(0xA0A0A0);
    static final Color WARNING = new Color(0xF59E0B);

    /** The one accent colour: only for the main button on each screen. */
    static final Color ACCENT = new Color(0xC2410C);
    private static final Color ACCENT_HOVER = new Color(0xB23C0B);
    private static final Color ACCENT_PRESSED = new Color(0xA7380A);
    private static final Color SECONDARY_HOVER = new Color(0x1F1F1F);
    private static final Color SECONDARY_PRESSED = new Color(0x2A2A2A);
    private static final Color FOCUS_RING = new Color(0x2563EB);

    private static final String FONT_FAMILY = "Segoe UI";
    private static final Dimension BUTTON_SIZE = new Dimension(240, 32);

    private UiStyle() {
        // Only static members, never created.
    }

    static Font font(int size, boolean bold) {
        return new Font(FONT_FAMILY, bold ? Font.BOLD : Font.PLAIN, size);
    }

    static JLabel label(String text, int size, boolean bold, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font(size, bold));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /** The main action of a screen. Use at most one per screen. */
    static JButton primaryButton(String text) {
        return button(text, ACCENT, ACCENT_HOVER, ACCENT_PRESSED, Color.WHITE, ACCENT);
    }

    /** Every other button. */
    static JButton secondaryButton(String text) {
        return button(text, BACKGROUND, SECONDARY_HOVER, SECONDARY_PRESSED, TEXT, new Color(0x3A3A3A));
    }

    private static JButton button(String text, Color normal, Color hover, Color pressed,
                                  Color textColor, Color outline) {
        // A JButton that fills itself with one flat colour (Swing's default look
        // would paint a gradient instead).
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        button.setFont(font(14, true));
        button.setForeground(textColor);
        button.setBackground(normal);
        button.setContentAreaFilled(false); // stops Swing painting its default gradient
        button.setFocusPainted(false);      // we draw our own focus ring below
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setPreferredSize(BUTTON_SIZE);
        button.setMinimumSize(BUTTON_SIZE);
        button.setMaximumSize(BUTTON_SIZE);

        Border restBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(outline, 1),
                BorderFactory.createEmptyBorder(1, 17, 1, 17));
        Border focusBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FOCUS_RING, 2),
                BorderFactory.createEmptyBorder(0, 16, 0, 16));
        button.setBorder(restBorder);

        // Hover and pressed colours.
        button.getModel().addChangeListener(e -> {
            ButtonModel model = button.getModel();
            if (model.isPressed()) {
                button.setBackground(pressed);
            } else if (model.isRollover()) {
                button.setBackground(hover);
            } else {
                button.setBackground(normal);
            }
        });

        // A visible blue ring when the button has keyboard focus.
        button.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                button.setBorder(focusBorder);
            }

            @Override
            public void focusLost(FocusEvent e) {
                button.setBorder(restBorder);
            }
        });
        return button;
    }
}
