package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.audio.MusicTrack;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {

    /**
     * The size of a grid cell in pixels. This allows us to think of coordinates in terms of square grid tiles,
     * e.g. (x1, y1) is the bottom left corner of the map rather than absolute pixel coordinates.
     */
    public static final int TILE_SIZE_PX = 16;

    /**
     * The scale of the game. This is used to make everything in the game look bigger or smaller.
     */
    public static final int SCALE = 4;

    private final ValleyDayGame game;
    private final SpriteBatch spriteBatch;
    private final GameMap map;
    private final Hud hud;
    private final OrthographicCamera mapCamera;
    private final Stage uiStage;
    private final TextButton musicToggleButton;
    private ShapeRenderer tileDebugRenderer;
    private boolean showTileDebug = false;

    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(ValleyDayGame game) {
        this.game = game;
        this.spriteBatch = game.getSpriteBatch();
        this.map = game.getMap();
        this.hud = new Hud(spriteBatch, game.getSkin().getFont("font"));

        this.mapCamera = new OrthographicCamera();
        this.mapCamera.setToOrtho(false);

        this.uiStage = new Stage(new ScreenViewport());
        Table uiTable = new Table();
        uiTable.setFillParent(true);
        uiTable.top().right().pad(10f);

        this.musicToggleButton = new TextButton("", game.getSkin());
        updateMusicButtonLabel();
        musicToggleButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                MusicTrack.BACKGROUND.toggleMute();
                updateMusicButtonLabel();
            }
        });

        uiTable.add(musicToggleButton).width(220f).height(48f);
        uiStage.addActor(uiTable);
    }

    /**
     * The render method is called every frame to render the game.
     *
     * @param deltaTime The time in seconds since the last render.
     */
    @Override
    public void render(float deltaTime) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.goToMenu();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            showTileDebug = !showTileDebug;
        }

        ScreenUtils.clear(Color.BLACK);

        float frameTime = Math.min(deltaTime, 0.250f);
        map.tick(frameTime);
        updateCamera();
        renderMap();
        if (showTileDebug) {
            renderTileDebugOverlay();
        }
        hud.render();
        uiStage.act(frameTime);
        uiStage.draw();
    }

    /**
     * Updates the camera to match the current state of the game.
     */
    private void updateCamera() {
        // Keep viewport in sync with the window size so the clamp math below stays correct
        mapCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if (map.getPlayer() == null) {
            mapCamera.position.x = map.getWidth() * TILE_SIZE_PX * SCALE / 2f;
            mapCamera.position.y = map.getHeight() * TILE_SIZE_PX * SCALE / 2f;
            mapCamera.update();
            return;
        }

        float worldWidthPx = map.getWidth() * TILE_SIZE_PX * SCALE;
        float worldHeightPx = map.getHeight() * TILE_SIZE_PX * SCALE;

        float targetX = map.getPlayer().getX() * TILE_SIZE_PX * SCALE;
        float targetY = map.getPlayer().getY() * TILE_SIZE_PX * SCALE;

        float halfWidth = mapCamera.viewportWidth / 2f;
        float halfHeight = mapCamera.viewportHeight / 2f;

        // Clamp camera so it never shows outside the map
        mapCamera.position.x = MathUtils.clamp(targetX, halfWidth, worldWidthPx - halfWidth);
        mapCamera.position.y = MathUtils.clamp(targetY, halfHeight, worldHeightPx - halfHeight);
        mapCamera.update();
    }

    /**
     * Renders the map including all tiles, chest, game objects, and player.
     */
    private void renderMap() {
        // Use the camera
        spriteBatch.setProjectionMatrix(mapCamera.combined);
        spriteBatch.begin();

        // Draw all tiles from the grid
        int[][] tileGrid = map.getTileGrid();
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                int tileType = tileGrid[x][y];

                // Only draw tiles with valid types (not -1 for empty)
                if (tileType >= 0) {
                    drawTile(spriteBatch, x, y, tileType);
                }
            }
        }

        // Draw chest
        if (map.getChest() != null) {
            draw(spriteBatch, map.getChest());
        }

        for (Drawable obj : map.getGameObjects()) {
            draw(spriteBatch, obj);
        }

        // Draw player on top
        if (map.getPlayer() != null) {
            draw(spriteBatch, map.getPlayer());
        }

        spriteBatch.end();
    }

    private void renderTileDebugOverlay() {
        if (tileDebugRenderer == null) {
            tileDebugRenderer = new ShapeRenderer();
        }
        tileDebugRenderer.setProjectionMatrix(mapCamera.combined);
        tileDebugRenderer.begin(ShapeRenderer.ShapeType.Filled);

        int[][] tileGrid = map.getTileGrid();
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                int type = tileGrid[x][y];
                if (type < 0) continue;

                tileDebugRenderer.setColor(colorForType(type));
                float px = x * TILE_SIZE_PX * SCALE;
                float py = y * TILE_SIZE_PX * SCALE;
                tileDebugRenderer.rect(px, py, TILE_SIZE_PX * SCALE, TILE_SIZE_PX * SCALE);
            }
        }

        tileDebugRenderer.end();
    }

    private Color colorForType(int type) {
        return switch (type) {
            case 0 -> new Color(1f, 0f, 0f, 0.25f);      // wall
            case 1 -> new Color(1f, 0.5f, 0f, 0.25f);    // destructible
            case 2 -> new Color(0f, 0.8f, 0f, 0.25f);    // entrance
            case 3 -> new Color(0f, 0.6f, 1f, 0.25f);
            case 4 -> new Color(0.6f, 0f, 1f, 0.25f);
            case 5 -> new Color(1f, 1f, 0f, 0.25f);
            case 6 -> new Color(1f, 0f, 1f, 0.25f);
            default -> new Color(1f, 1f, 1f, 0.25f);
        };
    }

    private void updateMusicButtonLabel() {
        musicToggleButton.setText(MusicTrack.BACKGROUND.isMuted() ? "Music: Off" : "Music: On");
    }

    /**
     * Draw a single tile at grid position (x, y) with the given type.
     * @param batch The SpriteBatch to draw with
     * @param x Tile X coordinate
     * @param y Tile Y coordinate
     * @param tileType The tile type (0=wall, 1=destructible, etc.)
     */
    private void drawTile(SpriteBatch batch, int x, int y, int tileType) {
        TextureRegion texture = Textures.getTileTexture(tileType);
        if (texture == null) {
            return;
        }
        float px = x * TILE_SIZE_PX * SCALE;
        float py = y * TILE_SIZE_PX * SCALE;

        // Get texture dimensions and scale
        float width = texture.getRegionWidth() * SCALE;
        float height = texture.getRegionHeight() * SCALE;

        // Draw the tile
        batch.draw(texture, px, py, width, height);
    }

    /**
     * Draws this object on the screen. The texture will be scaled by the game scale and the tile size.
     * This should only be called between spriteBatch.begin() and spriteBatch.end(), e.g. in the renderMap() method.
     *
     * @param spriteBatch The SpriteBatch to draw with.
     * @param drawable The drawable object to render.
     */
    private static void draw(SpriteBatch spriteBatch, Drawable drawable) {
        TextureRegion texture = drawable.getCurrentAppearance();
        if (texture == null) return;

        float renderX = drawable.getX();
        float renderY = drawable.getY();

        // Center player sprite on its physics body so visual matches collisions
        if (drawable instanceof de.tum.cit.aet.valleyday.map.Player) {
            renderX -= 0.5f;
            renderY -= 0.5f;
        }

        float x = renderX * TILE_SIZE_PX * SCALE;
        float y = renderY * TILE_SIZE_PX * SCALE;
        float width = texture.getRegionWidth() * SCALE;
        float height = texture.getRegionHeight() * SCALE;

        spriteBatch.draw(texture, x, y, width, height);
    }

    /**
     * Called when the window is resized. This is where the camera is updated to match the new window size.
     *
     * @param width The new window width.
     * @param height The new window height.
     */
    @Override
    public void resize(int width, int height) {
        mapCamera.setToOrtho(false);
        hud.resize(width, height);
        uiStage.getViewport().update(width, height, true);
    }

    // Unused methods from the Screen interface
    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void show() {
        MusicTrack.BACKGROUND.resume();
        Gdx.input.setInputProcessor(uiStage);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (tileDebugRenderer != null) {
            tileDebugRenderer.dispose();
        }
        uiStage.dispose();
    }
}
