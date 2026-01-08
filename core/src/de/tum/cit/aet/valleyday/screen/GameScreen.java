package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.GameObject;
import de.tum.cit.aet.valleyday.map.Player;
import de.tum.cit.aet.valleyday.map.WildlifeVisitor;
import de.tum.cit.aet.valleyday.texture.Textures;
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

        // FIX: Better camera setup
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 30, 30);  // Changed from 21, 21
        this.camera.position.set(10, 10, 0);     // Start at center of map
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
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.8f, 1f);
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
            Tile[][] tiles = gameMap.getTiles();

            // Draw all tiles
            for (int x = 0; x < gameMap.getWidth(); x++) {
                for (int y = 0; y < gameMap.getHeight(); y++) {
                    Tile tile = tiles[x][y];
                    if (tile != null && tile.getCurrentAppearance() != null) {
                        batch.draw(tile.getCurrentAppearance(), x, y, 1, 1);
                    }
                }
            }

            // Draw objects
            for (GameObject obj : gameMap.getGameObjects()) {
                if (obj.getCurrentAppearance() != null) {
                    batch.draw(obj.getCurrentAppearance(), obj.getX(), obj.getY(), 1, 1);
                }
            }

            // Draw wildlife
            for (WildlifeVisitor visitor : gameMap.getWildlifeVisitors()) {
                if (visitor.getCurrentAppearance() != null) {
                    batch.draw(visitor.getCurrentAppearance(), visitor.getX(), visitor.getY(), 1, 1);
                }
            }

            // Draw player
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
        if (player != null) {
            camera.position.set(player.getX(), player.getY(), 0);
            camera.update();
        }
    }

    @Override
    public void resize(int width, int height) {
        float aspectRatio = (float) width / (float) height;
        camera.setToOrtho(false, 30 * aspectRatio, 30);  // Better scaling
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
