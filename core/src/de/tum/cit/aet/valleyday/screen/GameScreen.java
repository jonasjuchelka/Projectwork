package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.GameObject;
import de.tum.cit.aet.valleyday.map.Player;
import de.tum.cit.aet.valleyday.map.WildlifeVisitor;
import de.tum.cit.aet.valleyday.texture.Textures;
import de.tum.cit.aet.valleyday.tiles.GroundTile;
import de.tum.cit.aet.valleyday.tiles.Tile;

public class GameScreen implements Screen {
    private final ValleyDayGame game;
    private final SpriteBatch batch;
    private OrthographicCamera camera;
    private GameMap gameMap;
    private BitmapFont font;
    private boolean mapLoaded = false;

    public GameScreen(ValleyDayGame game, String mapFilePath) {
        this.game = game;
        this.batch = game.getBatch();

        // Camera setup
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 21, 21);
        // Camera will be centered on player in updateCamera()
        this.camera.update();

        this.font = new BitmapFont();

        Gdx.app.log("GameScreen", "Creating GameScreen with map: " + mapFilePath);

        this.gameMap = new GameMap(game);

        if (mapFilePath != null) {
            try {
                FileHandle mapFile = Gdx.files.internal(mapFilePath);
                Gdx.app.log("GameScreen", "Map file exists: " + mapFile.exists());
                Gdx.app.log("GameScreen", "Map file path: " + mapFile.path());

                if (mapFile.exists()) {
                    gameMap.loadFromProperties(mapFile);
                    mapLoaded = true;
                    Gdx.app.log("GameScreen", "Map loaded successfully!");
                } else {
                    Gdx.app.error("GameScreen", "Map file not found!");
                }
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Error loading map: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void show() {
        Gdx.app.log("GameScreen", "Showing gameplay. Map loaded: " + mapLoaded);

        // Center camera on map (21x21 map, center at 10.5, 10.5)
        if (gameMap != null) {
            float mapCenterX = gameMap.getWidth() / 2f;
            float mapCenterY = gameMap.getHeight() / 2f;
            camera.position.set(mapCenterX, mapCenterY, 0);
            camera.update();
        }
    }

    @Override
    public void render(float delta) {
        // Brown background color (dark brown/dirt color)
        Gdx.gl.glClearColor(0.4f, 0.3f, 0.2f, 1f);
        Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);

        if (gameMap != null) {
            gameMap.tick(delta);
            updateCamera();

            // IMPORTANT: Set projection BEFORE batch.begin()
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            renderGame();
            batch.end();
        }

        // HUD - uses screen coordinates (different projection)
        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(batch.getProjectionMatrix());
        batch.begin();
        font.draw(batch, "Valley Day - GameScreen Active", 10, Gdx.graphics.getHeight() - 10);
        font.draw(batch, "Map loaded: " + mapLoaded, 10, Gdx.graphics.getHeight() - 30);
        font.draw(batch, "WASD to move, ESC to menu", 10, Gdx.graphics.getHeight() - 50);

        if (gameMap != null && gameMap.getPlayer() != null) {
            Player p = gameMap.getPlayer();
            font.draw(batch, "Player: (" + (int)p.getX() + ", " + (int)p.getY() + ")", 10, Gdx.graphics.getHeight() - 70);
        }
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.returnToMenu();
        }
    }


    private void renderGame() {
        try {
            GroundTile[][] groundLayer = gameMap.getGroundLayer();
            Tile[][] tiles = gameMap.getTiles();

            // STEP 1: Render GROUND LAYER (everywhere)
            for (int x = 0; x < gameMap.getWidth(); x++) {
                for (int y = 0; y < gameMap.getHeight(); y++) {
                    GroundTile ground = groundLayer[x][y];
                    if (ground != null && ground.getCurrentAppearance() != null) {
                        batch.draw(ground.getCurrentAppearance(), x, y, 1, 1);
                    }
                }
            }

            // STEP 2: Render TILES on top (Fence, SoilTile, Debris, etc.)
            for (int x = 0; x < gameMap.getWidth(); x++) {
                for (int y = 0; y < gameMap.getHeight(); y++) {
                    Tile tile = tiles[x][y];
                    if (tile != null && tile.getCurrentAppearance() != null) {
                        TextureRegion region = tile.getCurrentAppearance();

                        // Special rendering for Fence tiles
                        if (tile instanceof de.tum.cit.aet.valleyday.tiles.Fence) {
                            // Fence sprites are 48x24 pixels, need to scale to 2x1 units
                            // Vertical fences need to be rotated 90 degrees
                            boolean isVertical = (x == 0 || x == gameMap.getWidth() - 1);

                            if (isVertical) {
                                // Rotate 90 degrees: same 2x1 sprite, rotated around center
                                batch.draw(region,
                                    (float) x - 0.5f, (float) y,  // position (offset to center)
                                    1f, 0.5f,                      // origin (center of 2x1 sprite)
                                    2f, 1f,                        // width, height (same as horizontal!)
                                    1f, 1f,                        // scale
                                    90f);                          // rotation
                            } else {
                                // Horizontal fence: render as 2x1 (wide)
                                batch.draw(region, x - 0.5f, y, 2f, 1f);
                            }
                        } else {
                            // Normal tiles: render as 1x1
                            batch.draw(region, x, y, 1, 1);
                        }
                    }
                }
            }

            // STEP 3: Render game objects
            for (GameObject obj : gameMap.getGameObjects()) {
                if (obj.getCurrentAppearance() != null) {
                    batch.draw(obj.getCurrentAppearance(), obj.getX(), obj.getY(), 1, 1);
                }
            }

            // STEP 4: Render wildlife
            for (WildlifeVisitor visitor : gameMap.getWildlifeVisitors()) {
                if (visitor.getCurrentAppearance() != null) {
                    batch.draw(visitor.getCurrentAppearance(), visitor.getX(), visitor.getY(), 1, 1);
                }
            }

            // STEP 5: Render player
            Player player = gameMap.getPlayer();
            if (player != null && player.getCurrentAppearance() != null) {
                batch.draw(player.getCurrentAppearance(), player.getX(), player.getY(), 1, 1);
            }

        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error rendering: " + e.getMessage());
        }
    }


    private void updateCamera() {
        Player player = gameMap.getPlayer();
        if (player == null) {
            return;
        }

        float playerX = player.getX();
        float playerY = player.getY();

        // Viewport dimensions in world coordinates
        float viewportWidth = camera.viewportWidth;
        float viewportHeight = camera.viewportHeight;

        // Dead zone = middle 80% of viewport
        float deadZoneWidth = viewportWidth * 0.8f;
        float deadZoneHeight = viewportHeight * 0.8f;

        // Current camera position
        float cameraX = camera.position.x;
        float cameraY = camera.position.y;

        // Dead zone boundaries (relative to camera position)
        float deadZoneLeft = cameraX - deadZoneWidth / 2;
        float deadZoneRight = cameraX + deadZoneWidth / 2;
        float deadZoneBottom = cameraY - deadZoneHeight / 2;
        float deadZoneTop = cameraY + deadZoneHeight / 2;

        // Move camera only if player leaves the dead zone
        if (playerX < deadZoneLeft) {
            cameraX = playerX + deadZoneWidth / 2;
        } else if (playerX > deadZoneRight) {
            cameraX = playerX - deadZoneWidth / 2;
        }

        if (playerY < deadZoneBottom) {
            cameraY = playerY + deadZoneHeight / 2;
        } else if (playerY > deadZoneTop) {
            cameraY = playerY - deadZoneHeight / 2;
        }

        // Clamp camera to map boundaries
        float mapWidth = gameMap.getWidth();
        float mapHeight = gameMap.getHeight();

        // If map is smaller than viewport, center the map
        if (mapWidth < viewportWidth) {
            cameraX = mapWidth / 2f;
        } else {
            float minCameraX = viewportWidth / 2;
            float maxCameraX = mapWidth - viewportWidth / 2;
            cameraX = Math.max(minCameraX, Math.min(maxCameraX, cameraX));
        }

        if (mapHeight < viewportHeight) {
            cameraY = mapHeight / 2f;
        } else {
            float minCameraY = viewportHeight / 2;
            float maxCameraY = mapHeight - viewportHeight / 2;
            cameraY = Math.max(minCameraY, Math.min(maxCameraY, cameraY));
        }

        // Set new camera position
        camera.position.set(cameraX, cameraY, 0);
        camera.update();
    }

    @Override
    public void resize(int width, int height) {
        float aspectRatio = (float) width / (float) height;
        camera.setToOrtho(false, 21 * aspectRatio, 21);  // Better scaling
        camera.update();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (font != null) font.dispose();
    }
}
