package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.aet.valleyday.ValleyDayGame;

/**
 * The MenuScreen class is responsible for displaying the main menu of the game.
 */
public class MenuScreen implements Screen {

    private final Stage stage;

    public MenuScreen(ValleyDayGame game) {
        var camera = new OrthographicCamera();
        camera.zoom = 1.5f;

        Viewport viewport = new ScreenViewport(camera);
        stage = new Stage(viewport, game.getSpriteBatch());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        table.add(new Label("Valley Day", game.getSkin(), "title"))
                .padBottom(80)
                .row();

        Label statusLabel = new Label(game.getStatusMessage(), game.getSkin());
        statusLabel.setColor(Color.RED);

        // Go To Game button (loads map-1.properties)
        TextButton goToGameButton = new TextButton("Go To Game", game.getSkin());
        table.add(goToGameButton).width(300).row();

        goToGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean ok = game.getMap().loadFromProperties(Gdx.files.internal("maps/map-1.properties"));
                if (ok) {
                    game.setStatusMessage("");
                    game.goToGame();
                } else {
                    game.setStatusMessage("Failed to load map-1.properties");
                    statusLabel.setText(game.getStatusMessage());
                }
            }
        });

        // Load custom map via file chooser
        TextButton loadCustomMapButton = new TextButton("Load Custom Map", game.getSkin());
        table.add(loadCustomMapButton).width(300).row();

        loadCustomMapButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.loadMapFromFileChooser();
                statusLabel.setText(game.getStatusMessage());
            }
        });

        // Load Map button (loads map-2.properties)
        TextButton loadMapButton = new TextButton("Load Map", game.getSkin());
        table.add(loadMapButton).width(300).row();

        loadMapButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean ok = game.getMap().loadFromProperties(Gdx.files.internal("maps/map-2.properties"));
                if (ok) {
                    game.setStatusMessage("");
                    game.goToGame();
                } else {
                    game.setStatusMessage("Failed to load map-2.properties");
                    statusLabel.setText(game.getStatusMessage());
                }
            }
        });

        // Exit button
        TextButton exitButton = new TextButton("Exit", game.getSkin());
        table.add(exitButton).width(300).row();

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        table.row().padTop(20f);
        table.add(statusLabel).width(300).row();
    }

    @Override
    public void render(float deltaTime) {
        float frameTime = Math.min(deltaTime, 0.250f);
        ScreenUtils.clear(Color.BLACK);
        stage.act(frameTime);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}
}
