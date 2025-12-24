package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 */
public class GameScreen implements Screen {

    public static final int TILE_SIZE_PX = 16;
    public static final int SCALE = 4;

    private final ValleyDayGame game;
    private final SpriteBatch spriteBatch;
    private final GameMap map;
    private final Hud hud;
    private final OrthographicCamera mapCamera;

    public GameScreen(ValleyDayGame game) {
        this.game = game;
        this.spriteBatch = game.getSpriteBatch();
        this.map = game.getMap();
        this.hud = new Hud(spriteBatch, game.getSkin().getFont("font"));

        this.mapCamera = new OrthographicCamera();
        this.mapCamera.setToOrtho(false);
    }

    @Override
    public void render(float deltaTime) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.goToMenu();
        }

        ScreenUtils.clear(Color.BLACK);

        float frameTime = Math.min(deltaTime, 0.250f);
        map.tick(frameTime);
        updateCamera();
        renderMap();
        hud.render();
    }

    private void updateCamera() {
        mapCamera.setToOrtho(false);
        mapCamera.position.x = (map.getWidth() / 2.0f) * TILE_SIZE_PX * SCALE;
        mapCamera.position.y = (map.getHeight() / 2.0f) * TILE_SIZE_PX * SCALE;
        mapCamera.update();
    }

    private void renderMap() {
        spriteBatch.setProjectionMatrix(mapCamera.combined);
        spriteBatch.begin();

        // Draw tile grid from loaded map
        int[][] tileGrid = map.getTileGrid();
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                int tileType = tileGrid[x][y];
                if (tileType >= 0) {
                    drawTile(spriteBatch, x, y, tileType);
                }
            }
        }

        // Draw chest
        if (map.getChest() != null) {
            draw(spriteBatch, map.getChest());
        }

        // Draw player
        if (map.getPlayer() != null) {
            draw(spriteBatch, map.getPlayer());
        }

        spriteBatch.end();
    }

    private void drawTile(SpriteBatch batch, int x, int y, int type) {
        TextureRegion texture = Textures.getTileTexture(type);
        if (texture == null) return;

        float px = x * TILE_SIZE_PX * SCALE;
        float py = y * TILE_SIZE_PX * SCALE;
        float width = texture.getRegionWidth() * SCALE;
        float height = texture.getRegionHeight() * SCALE;

        batch.draw(texture, px, py, width, height);
    }

    private void draw(SpriteBatch spriteBatch, Drawable drawable) {
        TextureRegion texture = drawable.getCurrentAppearance();
        if (texture == null) return;

        float x = drawable.getX() * TILE_SIZE_PX * SCALE;
        float y = drawable.getY() * TILE_SIZE_PX * SCALE;
        float width = texture.getRegionWidth() * SCALE;
        float height = texture.getRegionHeight() * SCALE;

        spriteBatch.draw(texture, x, y, width, height);
    }

    @Override
    public void resize(int width, int height) {
        mapCamera.setToOrtho(false);
        hud.resize(width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
}
