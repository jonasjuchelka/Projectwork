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

        // Create and configure the camera for the game view
        this.mapCamera = new OrthographicCamera();
        this.mapCamera.setToOrtho(false);
    }

    /**
     * The render method is called every frame to render the game.
     *
     * @param deltaTime The time in seconds since the last render.
     */
    @Override
    public void render(float deltaTime) {
        // Check for escape key press to go back to the menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.goToMenu();
        }

        // Clear the previous frame from the screen, or else the picture smears
        ScreenUtils.clear(Color.BLACK);

        // Cap frame time to 250ms to prevent spiral of death
        float frameTime = Math.min(deltaTime, 0.250f);

        // Update the map state
        map.tick(frameTime);

        // Update the camera
        updateCamera();

        // Render the map on the screen
        renderMap();

        // Render the HUD on the screen
        hud.render();
    }

    /**
     * Updates the camera to match the current state of the game.
     * Currently, this just centers the camera at the map center.
     */
    private void updateCamera() {
        mapCamera.setToOrtho(false);
        mapCamera.position.x = map.getWidth() * TILE_SIZE_PX * SCALE / 2f;
        mapCamera.position.y = map.getHeight() * TILE_SIZE_PX * SCALE / 2f;
        mapCamera.update();
    }

    /**
     * Renders the map including all tiles, chest, game objects, and player.
     */
    private void renderMap() {
        // Use the camera
        spriteBatch.setProjectionMatrix(mapCamera.combined);
        spriteBatch.begin();

        // DEBUG: how many objects does the screen see?
        Gdx.app.log("Objects", "Objects in map when rendering: " + map.getGameObjects().size());

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

        // Draw game objects (decorations, debris, flowers, etc.)
        for (Drawable obj : map.getGameObjects()) {
            draw(spriteBatch, obj);
        }

        // Draw player on top
        if (map.getPlayer() != null) {
            draw(spriteBatch, map.getPlayer());
        }

        spriteBatch.end();
    }

    /**
     * Draw a single tile at grid position (x, y) with the given type.
     * @param batch The SpriteBatch to draw with
     * @param x Tile X coordinate
     * @param y Tile Y coordinate
     * @param tileType The tile type (0=wall, 1=destructible, etc.)
     */
    private void drawTile(SpriteBatch batch, int x, int y, int tileType) {
        // Get the texture for this tile type
        TextureRegion texture = Textures.getTileTexture(tileType);
        if (texture == null) {
            return;  // Skip if no texture available
        }

        // Convert tile coordinates to pixel coordinates
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

        // Drawable coordinates are in tiles, so we need to scale them to pixels
        float x = drawable.getX() * TILE_SIZE_PX * SCALE;
        float y = drawable.getY() * TILE_SIZE_PX * SCALE;

        // Additionally scale everything by the game scale
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
    }

    // Unused methods from the Screen interface
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
