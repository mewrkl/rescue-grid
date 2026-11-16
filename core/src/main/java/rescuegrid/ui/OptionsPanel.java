package rescuegrid.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

import rescuegrid.assets.SoundManager;

/**
 * Settings, reached from the main menu and from the pause menu: music and
 * sound volume, fullscreen, and resetting saved progress (after an
 * "Are you sure?"). Every setting is saved for next time.
 */
public class OptionsPanel extends BaseScreen {

    private final MenuList menu;
    private final Label message;
    private Runnable back;

    public OptionsPanel(GameFrame game) {
        super(game);
        this.back = game::showMenu;
        menu = createMenu();
        message = style.label(" ", UiStyle.TEXT_SECONDARY);
        message.setAlignment(Align.center);
        message.setWrap(true);

        root.add(style.pixelLabel("OPTIONS", 32, UiStyle.TEXT)).row();
        root.add(menu).padTop(32).row();
        root.add(message).width(560).padTop(16).row();
        root.add(hint("Left / Right to change  ·  Enter to select  ·  Esc to go back")).padTop(24).row();
    }

    /** Where BACK and Esc go: the main menu, or the paused game. */
    public void setBack(Runnable back) {
        this.back = back;
    }

    @Override
    public void show() {
        showSettings();
        super.show();
    }

    private void showSettings() {
        SoundManager sounds = game.getSounds();
        menu.clearOptions();
        Label[] music = new Label[1];
        music[0] = menu.addSetting(musicText(), () -> { }, direction -> {
            sounds.changeMusicVolume(direction);
            music[0].setText(musicText());
        });
        Label[] sound = new Label[1];
        sound[0] = menu.addSetting(soundText(), () -> { }, direction -> {
            sounds.changeSoundVolume(direction);
            sound[0].setText(soundText());
        });
        Label[] screen = new Label[1];
        Runnable toggle = () -> {
            game.toggleFullscreen();
            screen[0].setText(fullscreenText());
        };
        screen[0] = menu.addSetting(fullscreenText(), toggle, direction -> toggle.run());
        menu.addOption("RESET PROGRESS", this::askReset);
        menu.addOption("BACK", () -> back.run());
        menu.setBackAction(() -> back.run());
        message.setText(" ");
    }

    /** Resetting can't be undone, so it asks first and names what is lost. */
    private void askReset() {
        menu.clearOptions();
        menu.addOption("NO, KEEP IT", this::showSettings);
        menu.addOption("YES, RESET", () -> {
            game.resetProgress();
            showSettings();
            message.setText("Progress reset. Levels 2 and 3 are locked again.");
        });
        menu.setBackAction(this::showSettings);
        message.setText("Reset progress? This clears every star, locks Levels 2 and 3 again, "
                + "and asks about the training next time. It can't be undone.");
    }

    private String musicText() {
        return "MUSIC: " + game.getSounds().getMusicVolume() + "%";
    }

    private String soundText() {
        return "SOUND: " + game.getSounds().getSoundVolume() + "%";
    }

    private String fullscreenText() {
        return "FULLSCREEN: " + (game.isFullscreen() ? "ON" : "OFF");
    }
}
