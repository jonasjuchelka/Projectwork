package de.tum.cit.aet.valleyday;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.files.FileHandle;

import de.tum.cit.aet.valleyday.audio.MusicTrack;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.screen.GameScreen;
import de.tum.cit.aet.valleyday.screen.MenuScreen;

import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;

/**
 * The ValleyDayGame class represents the core of the Valley Day game.
 * It manages the screens and global resources like SpriteBatch and Skin.
 */
public class ValleyDayGame extends Game {

    /** Sprite Batch for rendering game elements. */
    private SpriteBatch spriteBatch;

    /** The game's UI skin. This is used to style the game's UI elements. */
    private Skin skin;

    /**
     * The file chooser for loading map files from the user's computer.
     * This will give you a {@link FileHandle} which can be passed to {@link GameMap#loadFromProperties(FileHandle)}.
     */
    private final NativeFileChooser fileChooser;

    /**
     * The map. This is where all the game objects are stored.
     * Owned by ValleyDayGame (not by GameScreen) so it survives screen switches.
     */
    private GameMap map;

    /**
     * Constructor for ValleyDayGame.
     *
     * @param fileChooser The file chooser for the game, typically used in desktop environment.
     */
    public ValleyDayGame(NativeFileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }

    /**
     * Called when the game is created. Initializes the SpriteBatch, Skin and an empty GameMap,
     * then shows the main menu. No default map is loaded here; the menu buttons decide
     * which map to load (map‑1, map‑2, or from file chooser).
     */
    @Override
    public void create() {
        this.spriteBatch = new SpriteBatch();
        this.skin = new Skin(Gdx.files.internal("skin/craftacular/craftacular-ui.json"));

        // Create an empty map; MenuScreen will call map.loadFromProperties(...)
        this.map = new GameMap(this);

        MusicTrack.BACKGROUND.play();
        goToMenu();
    }

    /**
     * Replaces the current map with a fresh empty GameMap.
     * You can optionally call loadFromProperties(...) on it afterwards.
     */
    public void resetMap() {
        this.map = new GameMap(this);
    }

    /** Switches to the menu screen. */
    public void goToMenu() {
        this.setScreen(new MenuScreen(this));
    }

    /** Switches to the game screen. */
    public void goToGame() {
        this.setScreen(new GameScreen(this));
    }

    /**
     * Opens a native file chooser to load a map file from disk.
     * After loading, it immediately switches to the GameScreen.
     */
    public void loadMapFromFileChooser() {
        NativeFileChooserConfiguration config = new NativeFileChooserConfiguration();
        // You can configure filters/initial directory on 'config' if needed.

        fileChooser.chooseFile(config, new NativeFileChooserCallback() {
            @Override
            public void onFileChosen(FileHandle file) {
                Gdx.app.log("MapLoad", "Selected: " + file.path());
                map.loadFromProperties(file);
                goToGame();
            }

            @Override
            public void onCancellation() {
                Gdx.app.log("MapLoad", "Cancelled");
            }

            @Override
            public void onError(Exception e) {
                Gdx.app.error("MapLoad", "Error: " + e.getMessage());
            }
        });
    }

    /** Returns the skin for UI elements. */
    public Skin getSkin() {
        return skin;
    }

    /** Returns the main SpriteBatch for rendering. */
    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    /** Returns the current map. */
    public GameMap getMap() {
        return map;
    }

    /**
     * Switches to the given screen and disposes of the previous screen.
     */
    @Override
    public void setScreen(Screen screen) {
        Screen previousScreen = super.screen;
        super.setScreen(screen);
        if (previousScreen != null) {
            previousScreen.dispose();
        }
    }

    /** Cleans up resources when the game is disposed. */
    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().hide();
            getScreen().dispose();
        }
        spriteBatch.dispose();
        skin.dispose();
    }
}
