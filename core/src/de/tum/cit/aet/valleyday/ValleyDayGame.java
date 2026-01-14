package de.tum.cit.aet.valleyday;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.tum.cit.aet.valleyday.screen.GameScreen;
import de.tum.cit.aet.valleyday.screen.MenuScreen;
import de.tum.cit.aet.valleyday.texture.Textures;
import de.tum.cit.aet.valleyday.texture.Animations;

public class ValleyDayGame extends Game {
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;
    public static final String TITLE = "Valley Day";

    private SpriteBatch batch;

    @Override
    public void create() {
        Gdx.app.log("ValleyDay", "Creating game...");
        this.batch = new SpriteBatch();
        Textures.initialize();
        Animations.initialize();
        setScreen(new MenuScreen(this));
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render();
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        Textures.dispose();
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    // ========== REMOVED MAP PATH PARAMETER ==========
    public void startGame() {
        setScreen(new GameScreen(this));  // ← No parameter needed!
    }

    public void returnToMenu() {
        setScreen(new MenuScreen(this));
    }
}
