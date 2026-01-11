package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
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
    private static final int MAP_WIDTH = 21;
    private static final int MAP_HEIGHT = 21;

    private float physicsTime = 0;
    private final ValleyDayGame game;
    private final World world;
    private final Player player;
    private final Tile[][] tiles;
    private final GroundTile[][] groundLayer;
    private final List<GameObject> gameObjects;
    private final List<WildlifeVisitor> wildlifeVisitors;

    public GameMap(ValleyDayGame game) {
        this.game = game;
        this.world = new World(Vector2.Zero, true);

        // Start player in center of map (10.5, 10.5)
        this.player = new Player(this.world, 10.5f, 10.5f);

        this.tiles = new Tile[MAP_WIDTH][MAP_HEIGHT];
        this.groundLayer = new GroundTile[MAP_WIDTH][MAP_HEIGHT];
        this.gameObjects = new ArrayList<>();
        this.wildlifeVisitors = new ArrayList<>();

        // Initialize ground layer for all positions
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                groundLayer[x][y] = new GroundTile(x, y, MAP_WIDTH, MAP_HEIGHT);
            }
        }

        // Initialize tiles with default SoilTile
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                tiles[x][y] = new SoilTile(x, y);
            }
        }
    }

    public void loadFromProperties(FileHandle file) {
        if (file == null || !file.exists()) {
            Gdx.app.log("MapLoad", "File not found: " + file);
            return;
        }

        // Reset tiles to SoilTile
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                tiles[x][y] = new SoilTile(x, y);
            }
        }

        String content = file.readString();
        String[] lines = content.split("\\r?\\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split("=");
            if (parts.length != 2) continue;

            String[] coords = parts[0].split(",");
            if (coords.length != 2) continue;

            try {
                int x = Integer.parseInt(coords[0].trim());
                int y = Integer.parseInt(coords[1].trim());
                int type = Integer.parseInt(parts[1].trim());

                if (x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT) {
                    tiles[x][y] = createTile(x, y, type);
                }
            } catch (NumberFormatException e) {
                // skip invalid lines
            }
        }

        createWallBodies();
        spawnGameObjects();
        spawnWildlife();
        Gdx.app.log("MapLoad", "Successfully loaded: " + file.name());
    }

    private Tile createTile(int x, int y, int type) {
        return switch (type) {
            case 0 -> new Fence(x, y);
            case 1 -> new Debris(x, y, new SoilTile(x, y));
            case 2 -> new Entrance(x, y);
            case 3 -> new Exit(x, y);
            case 4 -> new ToolItem(x, y, ToolItem.ItemType.SHOVEL);
            case 5 -> new ToolItem(x, y, ToolItem.ItemType.FERTILIZER);
            case 6 -> new ToolItem(x, y, ToolItem.ItemType.WATERING_CAN);
            default -> new SoilTile(x, y);
        };
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
        for (int i = 0; i < 8; i++) {
            int x = 5 + rand.nextInt(10);
            int y = 5 + rand.nextInt(10);
            gameObjects.add(new GameObject(x, y, Textures.getRandomObject()));
        }
    }

    private void spawnWildlife() {
        wildlifeVisitors.clear();
        Random rand = new Random();
        for (int i = 0; i < 3; i++) {
            int x = 3 + rand.nextInt(15);
            int y = 3 + rand.nextInt(15);
            WildlifeVisitor.WildlifeType type = WildlifeVisitor.WildlifeType.values()[rand.nextInt(3)];
            wildlifeVisitors.add(new WildlifeVisitor(world, x, y, type));
        }
    }

    public void tick(float frameTime) {
        this.player.tick(frameTime);
        handlePlayerInteraction(); // Player interactions with tiles

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

    // PLAYER INTERACTION LOGIC
    private void handlePlayerInteraction() {
        int px = player.getTileX();
        int py = player.getTileY();

        // Pick up tools when standing on them
        Tile currentTile = getTile(px, py);
        if (currentTile instanceof ToolItem) {
            ToolItem toolItem = (ToolItem) currentTile;
            player.addItem(toolItem.getItemType());
            tiles[px][py] = new SoilTile(px, py);
            Gdx.app.log("GameMap", "Picked up tool: " + toolItem.getItemType());
        }

        // Use tool on SPACE key
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            useTool(px, py);
        }
    }

    // TOOL USAGE LOGIC
    private void useTool(int x, int y) {
        Tile tile = getTile(x, y);
        ToolItem.ItemType tool = player.getCurrentTool();

        if (tool == null) {
            Gdx.app.log("GameMap", "No tool equipped!");
            return;
        }

        switch (tool) {
            case SHOVEL:
                // Clear debris
                if (tile instanceof Debris) {
                    Debris debris = (Debris) tile;
                    tiles[x][y] = debris.getHiddenTile();
                    Gdx.app.log("GameMap", "Cleared debris!");
                }
                // Plant seeds on empty soil
                else if (tile instanceof SoilTile) {
                    SoilTile soil = (SoilTile) tile;
                    if (!soil.hasCrop()) {
                        soil.plantSeed();
                        Gdx.app.log("GameMap", "Planted seed!");
                    } else if (soil.getCrop() != null && soil.getCrop().isMature()) {
                        // Harvest mature crops
                        int coins = 10;
                        player.addCoins(coins);
                        soil.harvestCrop();
                        Gdx.app.log("GameMap", "Harvested crop! Earned " + coins + " coins");
                    }
                }
                break;

            case WATERING_CAN:
                // Water crops
                if (tile instanceof SoilTile) {
                    SoilTile soil = (SoilTile) tile;
                    if (soil.hasCrop()) {
                        soil.applyWateringCan();
                        Gdx.app.log("GameMap", "Watered crop!");
                    }
                }
                break;

            case FERTILIZER:
                // Fertilize crops
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

    // GETTERS
    public Player getPlayer() {
        return player;
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    public GroundTile[][] getGroundLayer() {
        return groundLayer;
    }

    public Tile getTile(int x, int y) {
        if (x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT) {
            return tiles[x][y];
        }
        return null;
    }

    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    public List<WildlifeVisitor> getWildlifeVisitors() {
        return wildlifeVisitors;
    }

    public int getWidth() {
        return MAP_WIDTH;
    }

    public int getHeight() {
        return MAP_HEIGHT;
    }

    public World getWorld() {
        return world;
    }
}
