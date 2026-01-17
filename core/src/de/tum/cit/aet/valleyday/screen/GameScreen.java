package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.map.ChaserZombie;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.GameObject;
import de.tum.cit.aet.valleyday.map.Player;
import de.tum.cit.aet.valleyday.map.WildlifeVisitor;
import de.tum.cit.aet.valleyday.tiles.GroundTile;
import de.tum.cit.aet.valleyday.tiles.Tile;

public class GameScreen implements Screen {
    private final ValleyDayGame game;
    private final SpriteBatch batch;
    private OrthographicCamera camera;
    private GameMap gameMap;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private GlyphLayout glyphLayout;

    // Spawn delayAber
    private float spawnDelay = 1.5f;
    private boolean playerVisible = false;

    public GameScreen(ValleyDayGame game) {
        this.game = game;
        this.batch = game.getBatch();

        // Camera setup - zoomed in to show ~60% of map (20 units instead of 32)
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 20, 20);
        this.camera.update();

        this.font = new BitmapFont();
        this.shapeRenderer = new ShapeRenderer();
        this.glyphLayout = new GlyphLayout();
        Gdx.app.log("GameScreen", "Creating GameScreen");

        // GameMap handles everything internally
        this.gameMap = new GameMap(game);
    }

    @Override
    public void show() {
        Gdx.app.log("GameScreen", "Showing gameplay");
        // Center camera on map
        if (gameMap != null) {
            float mapCenterX = gameMap.getWidth() / 2f;
            float mapCenterY = gameMap.getHeight() / 2f;
            camera.position.set(mapCenterX, mapCenterY, 0);
            camera.update();
        }
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f);
        Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);

        // Handle spawn delay
        if (!playerVisible) {
            spawnDelay -= delta;
            if (spawnDelay <= 0) {
                playerVisible = true;
            }
        }

        if (gameMap != null) {
            // Level-Übergang: Warte auf Enter-Taste
            if (gameMap.isLevelComplete() && !gameMap.isGameOver()) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    gameMap.startNextLevel();
                    spawnDelay = 1.5f;
                    playerVisible = false;
                }
            }

            if (playerVisible && !gameMap.isGameOver() && !gameMap.isLevelComplete()) {
                gameMap.tick(delta);
            }
            updateCamera();

            // ========== RENDER ORDER (LAYERED) ==========
            // LAYER 1: TMX Background (pretty graphics from Tiled)
            gameMap.renderTmxBackground(camera);

            // LAYER 2: Ground tiles (ONLY if no TMX map loaded)
            if (!gameMap.hasTmxMap()) {
                batch.setProjectionMatrix(camera.combined);
                batch.begin();
                renderGroundLayer();
                batch.end();
            }

            // LAYER 3: Gameplay objects (tiles, player, etc.)
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            renderGameplayLayer();
            batch.end();
        }

        // HUD - uses screen coordinates (only show if not game over and not level complete)
        if (gameMap == null || (!gameMap.isGameOver() && !gameMap.isLevelComplete())) {
            batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setProjectionMatrix(batch.getProjectionMatrix());
            batch.begin();

            // Level-Anzeige oben links mit Schatten
            if (gameMap != null) {
                String levelText = "Level " + gameMap.getCurrentLevel() + "/" + gameMap.getMaxLevel();

                // Schatten
                font.setColor(0.2f, 0.2f, 0.2f, 0.6f);
                font.draw(batch, levelText, 11, Gdx.graphics.getHeight() - 11);

                // Level-Text in Gelb für bessere Sichtbarkeit
                font.setColor(Color.YELLOW);
                font.draw(batch, levelText, 10, Gdx.graphics.getHeight() - 10);
            }

            font.setColor(Color.WHITE);
            font.draw(batch, "WASD to move, E to shoo wildlife, ESC to menu", 10, Gdx.graphics.getHeight() - 30);

            // Timer-Anzeige oben rechts
            if (gameMap != null) {
                float remainingTime = gameMap.getRemainingTime();
                int minutes = (int) (remainingTime / 60);
                int seconds = (int) (remainingTime % 60);
                String timerText = String.format("%d:%02d", minutes, seconds);

                // Timer-Text messen für rechte Ausrichtung
                glyphLayout.setText(font, timerText);
                float timerX = Gdx.graphics.getWidth() - glyphLayout.width - 15;
                float timerY = Gdx.graphics.getHeight() - 15;

                // Leichter Hintergrund für bessere Lesbarkeit
                font.setColor(0.2f, 0.2f, 0.2f, 0.6f);
                font.draw(batch, timerText, timerX + 1, timerY - 1);

                // Timer-Text in Weiß
                font.setColor(Color.WHITE);
                font.draw(batch, timerText, timerX, timerY);
            }
            batch.end();
        }

        // Overlays
        if (gameMap != null) {
            if (gameMap.isLevelComplete() && !gameMap.isGameOver()) {
                renderLevelCompleteOverlay();
            } else if (gameMap.isGameOver()) {
                renderGameOverOverlay();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.returnToMenu();
        }
    }

    private void renderLevelCompleteOverlay() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        // Draw semi-transparent black overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Draw text
        batch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);
        batch.setProjectionMatrix(batch.getProjectionMatrix());
        batch.begin();

        // Congratulations message
        font.setColor(Color.GREEN);
        String congratsText = "Congratulations!";
        glyphLayout.setText(font, congratsText);
        float congratsX = (screenWidth - glyphLayout.width) / 2;
        float congratsY = screenHeight / 2 + 40;
        font.draw(batch, congratsText, congratsX, congratsY);

        // Level info
        font.setColor(Color.WHITE);
        String levelText = "Level " + gameMap.getCurrentLevel() + " Complete!";
        glyphLayout.setText(font, levelText);
        float levelX = (screenWidth - glyphLayout.width) / 2;
        float levelY = screenHeight / 2 + 10;
        font.draw(batch, levelText, levelX, levelY);

        // Continue instruction
        font.setColor(Color.YELLOW);
        String continueText = "Press ENTER to start the next level";
        glyphLayout.setText(font, continueText);
        float continueX = (screenWidth - glyphLayout.width) / 2;
        float continueY = screenHeight / 2 - 30;
        font.draw(batch, continueText, continueX, continueY);

        batch.end();
    }

    private void renderGameOverOverlay() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        // Draw semi-transparent black overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Draw text
        batch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);
        batch.setProjectionMatrix(batch.getProjectionMatrix());
        batch.begin();

        // Different messages based on game over reason
        String mainText;
        String reasonText = "";
        GameMap.GameOverReason reason = gameMap.getGameOverReason();

        if (reason == GameMap.GameOverReason.GAME_WON) {
            // Game Won - All levels complete
            font.setColor(Color.GREEN);
            mainText = "Congratulations!";
            reasonText = "You completed all " + gameMap.getMaxLevel() + " levels!";
        } else {
            // Game Over
            font.setColor(Color.RED);
            mainText = "Game Over";

            switch (reason) {
                case WILDLIFE_CONTACT:
                    reasonText = "You were caught by wildlife!";
                    break;
                case CHASER_CAUGHT:
                    reasonText = "The chaser zombie caught you!";
                    break;
                case TIME_EXPIRED:
                    reasonText = "Time ran out!";
                    break;
                default:
                    reasonText = "Better luck next time!";
                    break;
            }
        }

        // Main text
        glyphLayout.setText(font, mainText);
        float mainX = (screenWidth - glyphLayout.width) / 2;
        float mainY = screenHeight / 2 + 40;
        font.draw(batch, mainText, mainX, mainY);

        // Reason text
        font.setColor(Color.WHITE);
        glyphLayout.setText(font, reasonText);
        float reasonX = (screenWidth - glyphLayout.width) / 2;
        float reasonY = screenHeight / 2 + 10;
        font.draw(batch, reasonText, reasonX, reasonY);

        // Exit instruction
        font.setColor(Color.GRAY);
        String escText = "Press ESC to return to menu";
        glyphLayout.setText(font, escText);
        float escX = (screenWidth - glyphLayout.width) / 2;
        float escY = screenHeight / 2 - 30;
        font.draw(batch, escText, escX, escY);

        batch.end();
    }

    private void renderGroundLayer() {
        GroundTile[][] groundLayer = gameMap.getGroundLayer();
        for (int x = 0; x < gameMap.getWidth(); x++) {
            for (int y = 0; y < gameMap.getHeight(); y++) {
                GroundTile ground = groundLayer[x][y];
                if (ground != null && ground.getCurrentAppearance() != null) {
                    batch.draw(ground.getCurrentAppearance(), x, y, 1, 1);
                }
            }
        }
    }

    private void renderGameplayLayer() {
        try {
            Tile[][] tiles = gameMap.getTiles();

            // STEP 1: Render tiles (Fence, Debris, Tools, etc.)
            for (int x = 0; x < gameMap.getWidth(); x++) {
                for (int y = 0; y < gameMap.getHeight(); y++) {
                    Tile tile = tiles[x][y];
                    if (tile != null && tile.getCurrentAppearance() != null) {
                        TextureRegion region = tile.getCurrentAppearance();

                        // Special rendering for Fence tiles
                        if (tile instanceof de.tum.cit.aet.valleyday.tiles.Fence) {
                            boolean isVertical = (x == 0 || x == gameMap.getWidth() - 1);
                            if (isVertical) {
                                batch.draw(region,
                                        (float) x - 0.5f, (float) y,
                                        1f, 0.5f,
                                        2f, 1f,
                                        1f, 1f,
                                        90f);
                            } else {
                                batch.draw(region, x - 0.5f, y, 2f, 1f);
                            }
                        } else {
                            // Normal tiles: render as 1x1
                            batch.draw(region, x, y, 1, 1);
                        }
                    }
                }
            }

            // STEP 2: Render game objects
            for (GameObject obj : gameMap.getGameObjects()) {
                if (obj.getCurrentAppearance() != null) {
                    batch.draw(obj.getCurrentAppearance(), obj.getX(), obj.getY(), 1, 1);
                }
            }

            // STEP 3: Render wildlife
            for (WildlifeVisitor visitor : gameMap.getWildlifeVisitors()) {
                if (visitor.getCurrentAppearance() != null) {
                    batch.draw(visitor.getCurrentAppearance(), visitor.getX(), visitor.getY(), 1, 1);
                }
            }

            // STEP 3.5: Render Chaser Zombie
            ChaserZombie chaser = gameMap.getChaserZombie();
            if (chaser != null && chaser.isActive() && chaser.getCurrentAppearance() != null) {
                batch.draw(chaser.getCurrentAppearance(), chaser.getX(), chaser.getY(), 1, 1);
            }

            // STEP 4: Render player (on top of everything) - only if visible and not game over
            if (playerVisible && !gameMap.isGameOver()) {
                Player player = gameMap.getPlayer();
                if (player != null && player.getCurrentAppearance() != null) {
                    batch.draw(player.getCurrentAppearance(), player.getX(), player.getY(), 1, 1);
                }
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

        // Kamera folgt dem Spieler direkt - Spieler bleibt in der Mitte
        float cameraX = player.getX();
        float cameraY = player.getY();

        // Kamera innerhalb der Map-Grenzen halten (keine schwarzen Bereiche)
        float viewportWidth = camera.viewportWidth;
        float viewportHeight = camera.viewportHeight;
        float mapWidth = gameMap.getWidth();
        float mapHeight = gameMap.getHeight();

        float minCameraX = viewportWidth / 2;
        float maxCameraX = mapWidth - viewportWidth / 2;
        float minCameraY = viewportHeight / 2;
        float maxCameraY = mapHeight - viewportHeight / 2;

        cameraX = Math.max(minCameraX, Math.min(maxCameraX, cameraX));
        cameraY = Math.max(minCameraY, Math.min(maxCameraY, cameraY));

        camera.position.set(cameraX, cameraY, 0);
        camera.update();
    }

    @Override
    public void resize(int width, int height) {
        float aspectRatio = (float) width / (float) height;
        camera.setToOrtho(false, 20 * aspectRatio, 20);  // Zoomed in view
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
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (gameMap != null) gameMap.dispose();
    }
}
