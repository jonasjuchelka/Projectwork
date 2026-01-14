package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.texture.Textures;
import de.tum.cit.aet.valleyday.tiles.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameMap {
    static {
        Box2D.init();
    }

    private static final float TIME_STEP = 1f / 60f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    private static final int MAP_WIDTH = 32;
    private static final int MAP_HEIGHT = 32;

    // TMX maps only
    private static final String[] TMX_MAPS = {
            "maps/map-1.tmx"
            // Add more later: "maps/map-2.tmx", etc.
    };

    private float physicsTime = 0;
    private final ValleyDayGame game;
    private final World world;
    private final Player player;
    private final Tile[][] tiles;
    private final GroundTile[][] groundLayer;
    private final List<GameObject> gameObjects;
    private final List<WildlifeVisitor> wildlifeVisitors;

    // TMX rendering
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private final float UNIT_SCALE = 1f / 16f;

    public GameMap(ValleyDayGame game) {
        this.game = game;
        this.world = new World(Vector2.Zero, true);
        this.player = new Player(this.world, 16f, 16f);
        this.tiles = new Tile[MAP_WIDTH][MAP_HEIGHT];
        this.groundLayer = new GroundTile[MAP_WIDTH][MAP_HEIGHT];
        this.gameObjects = new ArrayList<>();
        this.wildlifeVisitors = new ArrayList<>();

        // Initialize ground layer
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                groundLayer[x][y] = new GroundTile(x, y, MAP_WIDTH, MAP_HEIGHT);
            }
        }

        // Initialize tiles with SoilTile
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                tiles[x][y] = new SoilTile(x, y);
            }
        }

        // Load TMX map
        loadRandomMap();
    }

    private void loadRandomMap() {
        int mapIndex = 0;  // Always load map-1 for now (change to random later)
        String tmxPath = TMX_MAPS[mapIndex];

        Gdx.app.log("MapLoader", "Loading TMX map: " + tmxPath);

        // Load the TMX map
        loadTmxMap(tmxPath);

        // Create walls from fence tiles (if needed)
        createWallBodies();

        // Spawn objects and wildlife
        spawnGameObjects();
        spawnWildlife();
    }

    private void loadTmxMap(String tmxPath) {
        try {
            FileHandle tmxFile = Gdx.files.internal(tmxPath);
            if (!tmxFile.exists()) {
                Gdx.app.error("MapLoader", "TMX file not found: " + tmxPath);
                return;
            }

            tiledMap = new TmxMapLoader().load(tmxPath);
            tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, UNIT_SCALE);
            Gdx.app.log("MapLoader", "✅ TMX map loaded successfully: " + tmxPath);

        } catch (Exception e) {
            Gdx.app.error("MapLoader", "Failed to load TMX: " + e.getMessage());
            e.printStackTrace();
            tiledMap = null;
            tiledMapRenderer = null;
        }
    }

    public void renderTmxBackground(OrthographicCamera camera) {
        if (tiledMapRenderer != null) {
            tiledMapRenderer.setView(camera);
            tiledMapRenderer.render();
        }
    }

    private void createWallBodies() {
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                Tile tile = tiles[x][y];
                if (tile instanceof Fence) {
                    createWallBody(x, y);
                }
            }
        }
    }

    private void createWallBody(int x, int y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x + 0.5f, y + 0.5f);
        Body body = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f);
        body.createFixture(shape, 1.0f);
        shape.dispose();
    }

    private void spawnGameObjects() {
        gameObjects.clear();
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            int x = 5 + rand.nextInt(20);
            int y = 5 + rand.nextInt(20);
            gameObjects.add(new GameObject(x, y, Textures.getRandomObject()));
        }
    }

    private void spawnWildlife() {
        wildlifeVisitors.clear();
        Random rand = new Random();
        for (int i = 0; i < 3; i++) {
            int x = 3 + rand.nextInt(25);
            int y = 3 + rand.nextInt(25);
            WildlifeVisitor.WildlifeType type =
                    WildlifeVisitor.WildlifeType.values()[rand.nextInt(3)];
            wildlifeVisitors.add(new WildlifeVisitor(world, x, y, type));
        }
    }

    public void tick(float frameTime) {
        this.player.tick(frameTime);
        handlePlayerInteraction();

        for (WildlifeVisitor visitor : wildlifeVisitors) {
            visitor.tick(frameTime);
        }

        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                tiles[x][y].tick(frameTime);
            }
        }

        doPhysicsStep(frameTime);
    }

    private void handlePlayerInteraction() {
        int px = player.getTileX();
        int py = player.getTileY();
        Tile currentTile = getTile(px, py);

        if (currentTile instanceof ToolItem) {
            ToolItem toolItem = (ToolItem) currentTile;
            player.addItem(toolItem.getItemType());
            tiles[px][py] = new SoilTile(px, py);
            Gdx.app.log("GameMap", "Picked up tool: " + toolItem.getItemType());
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            useTool(px, py);
        }
    }

    private void useTool(int x, int y) {
        Tile tile = getTile(x, y);
        ToolItem.ItemType tool = player.getCurrentTool();

        if (tool == null) {
            Gdx.app.log("GameMap", "No tool equipped!");
            return;
        }

        switch (tool) {
            case SHOVEL:
                if (tile instanceof Debris) {
                    Debris debris = (Debris) tile;
                    tiles[x][y] = debris.getHiddenTile();
                    Gdx.app.log("GameMap", "Cleared debris!");
                } else if (tile instanceof SoilTile) {
                    SoilTile soil = (SoilTile) tile;
                    if (!soil.hasCrop()) {
                        soil.plantSeed();
                        Gdx.app.log("GameMap", "Planted seed!");
                    } else if (soil.getCrop() != null && soil.getCrop().isMature()) {
                        int coins = 10;
                        player.addCoins(coins);
                        soil.harvestCrop();
                        Gdx.app.log("GameMap", "Harvested crop! Earned " + coins + " coins");
                    }
                }
                break;

            case WATERING_CAN:
                if (tile instanceof SoilTile) {
                    SoilTile soil = (SoilTile) tile;
                    if (soil.hasCrop()) {
                        soil.applyWateringCan();
                        Gdx.app.log("GameMap", "Watered crop!");
                    }
                }
                break;

            case FERTILIZER:
                if (tile instanceof SoilTile) {
                    SoilTile soil = (SoilTile) tile;
                    if (soil.hasCrop()) {
                        soil.applyFertilizer();
                        Gdx.app.log("GameMap", "Fertilized crop!");
                    }
                }
                break;
        }
    }

    private void doPhysicsStep(float frameTime) {
        this.physicsTime += frameTime;
        while (this.physicsTime >= TIME_STEP) {
            this.world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            this.physicsTime -= TIME_STEP;
        }
    }

    // Getters
    public Player getPlayer() { return player; }
    public Tile[][] getTiles() { return tiles; }
    public GroundTile[][] getGroundLayer() { return groundLayer; }

    public Tile getTile(int x, int y) {
        if (x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT) {
            return tiles[x][y];
        }
        return null;
    }

    public List<GameObject> getGameObjects() { return gameObjects; }
    public List<WildlifeVisitor> getWildlifeVisitors() { return wildlifeVisitors; }
    public int getWidth() { return MAP_WIDTH; }
    public int getHeight() { return MAP_HEIGHT; }
    public World getWorld() { return world; }
    public boolean hasTmxMap() { return tiledMap != null; }

    public void dispose() {
        if (tiledMap != null) tiledMap.dispose();
        if (tiledMapRenderer != null) tiledMapRenderer.dispose();
    }
}
