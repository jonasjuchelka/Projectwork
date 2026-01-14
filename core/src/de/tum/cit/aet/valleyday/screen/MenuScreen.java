package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
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
        this.font.setColor(Color.WHITE);
        this.font.getData().setScale(2);
    }

    @Override
    public void show() {
        Gdx.app.log("MenuScreen", "Showing main menu");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.draw(batch, "VALLEY DAY", Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f + 50);
        font.getData().setScale(1);
        font.draw(batch, "Press ENTER to Start", Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f);
        font.draw(batch, "Press ESC to Exit", Gdx.graphics.getWidth() / 2f - 90, Gdx.graphics.getHeight() / 2f - 30);
        font.getData().setScale(2);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.startGame();  // ← No map path needed!
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
