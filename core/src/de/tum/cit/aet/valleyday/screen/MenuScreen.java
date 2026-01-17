package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.tum.cit.aet.valleyday.ValleyDayGame;

public class MenuScreen implements Screen {
    private final ValleyDayGame game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final ShapeRenderer shapeRenderer;
    private final GlyphLayout glyphLayout;

    public MenuScreen(ValleyDayGame game) {
        this.game = game;
        this.batch = game.getBatch();
        this.font = new BitmapFont();
        this.shapeRenderer = new ShapeRenderer();
        this.glyphLayout = new GlyphLayout();
    }

    @Override
    public void show() {
        Gdx.app.log("MenuScreen", "Showing main menu");
    }

    @Override
    public void render(float delta) {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        // Dark blue background
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw decorative box
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        float boxWidth = 450;
        float boxHeight = 300;
        float boxX = (screenWidth - boxWidth) / 2;
        float boxY = (screenHeight - boxHeight) / 2;

        // Box background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.15f, 0.25f, 0.95f);
        shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight);
        shapeRenderer.end();

        // Box border (golden)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.85f, 0.65f, 0.13f, 1f);
        shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight);
        shapeRenderer.rect(boxX + 3, boxY + 3, boxWidth - 6, boxHeight - 6);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Draw text
        batch.begin();

        // Title "VALLEY DAY" - large and golden
        font.getData().setScale(3f);
        font.setColor(0.85f, 0.65f, 0.13f, 1f);
        String titleText = "VALLEY DAY";
        glyphLayout.setText(font, titleText);
        float titleX = (screenWidth - glyphLayout.width) / 2;
        float titleY = boxY + boxHeight - 40;
        font.draw(batch, titleText, titleX, titleY);

        // Subtitle
        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
        String subtitleText = "- MAIN MENU -";
        glyphLayout.setText(font, subtitleText);
        float subtitleX = (screenWidth - glyphLayout.width) / 2;
        float subtitleY = titleY - 50;
        font.draw(batch, subtitleText, subtitleX, subtitleY);

        // Instructions
        font.getData().setScale(1f);
        float instructionY = boxY + boxHeight / 2 - 20;

        // Start instruction (green)
        font.setColor(0.3f, 0.9f, 0.3f, 1f);
        String startText = "Press ENTER to start the game";
        glyphLayout.setText(font, startText);
        float startX = (screenWidth - glyphLayout.width) / 2;
        font.draw(batch, startText, startX, instructionY);

        // Exit instruction (red)
        font.setColor(0.9f, 0.3f, 0.3f, 1f);
        String exitText = "Press ESC to exit";
        glyphLayout.setText(font, exitText);
        float exitX = (screenWidth - glyphLayout.width) / 2;
        font.draw(batch, exitText, exitX, instructionY - 30);

        // Controls hint at bottom
        font.setColor(0.6f, 0.6f, 0.6f, 1f);
        String controlsText = "WASD = Move | SPACE = Pause";
        glyphLayout.setText(font, controlsText);
        float controlsX = (screenWidth - glyphLayout.width) / 2;
        font.draw(batch, controlsText, controlsX, boxY + 30);

        font.getData().setScale(1f);
        batch.end();

        // Handle input
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.startGame();
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
        shapeRenderer.dispose();
    }
}
