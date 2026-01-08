package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.tum.cit.aet.valleyday.ValleyDayGame;

public class MenuScreen implements Screen {
    private final ValleyDayGame game;
    private final SpriteBatch batch;
    private final BitmapFont font;

    public MenuScreen(ValleyDayGame game) {
        this.game = game;
        this.batch = game.getBatch();
        this.font = new BitmapFont();
    }

    @Override
    public void show() {
        Gdx.app.log("MenuScreen", "Showing main menu");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.3f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.draw(batch, "VALLEY DAY", Gdx.graphics.getWidth() / 2f - 50, Gdx.graphics.getHeight() - 100);
        font.draw(batch, "Press ENTER to Start Game", Gdx.graphics.getWidth() / 2f - 120, Gdx.graphics.getHeight() / 2f);
        font.draw(batch, "Press ESC to Exit", Gdx.graphics.getWidth() / 2f - 80, Gdx.graphics.getHeight() / 2f - 30);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.startGame("maps/map-1.properties");  // CHANGED: removed "assets/"
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        font.dispose();
    }
}
