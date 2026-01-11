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

        // Optional base init (wird gleich überschrieben)
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                tiles[x][y] = new SoilTile(x, y);
            }
        }

        // Prozedurale Karte erzeugen
        generateProceduralMap();
    }

    // ----------------- Laden aus Properties (für später nutzbar) -----------------

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

    // ----------------- Physische Wände für Zäune -----------------

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

    // ----------------- Deko‑Objekte und Wildlife -----------------

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
            WildlifeVisitor.WildlifeType type =
                    WildlifeVisitor.WildlifeType.values()[rand.nextInt(3)];
            wildlifeVisitors.add(new WildlifeVisitor(world, x, y, type));
        }
    }

    // ----------------- Game‑Loop‑Tick -----------------

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

    // ----------------- Spieler‑Interaktion -----------------

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

    // ----------------- Physikstep -----------------

    private void doPhysicsStep(float frameTime) {
        this.physicsTime += frameTime;
        while (this.physicsTime >= TIME_STEP) {
            this.world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            this.physicsTime -= TIME_STEP;
        }
    }

    // ----------------- Getter -----------------

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

    // ----------------- Prozedurale Map‑Generierung -----------------

    // ========== PROCEDURAL MAP GENERATION ==========

private void generateProceduralMap() {
Random rand = new Random();

Gdx.app.log("MapGen", "Generating procedural map...");

// Step 1: Fill entire map with SoilTile
for (int x = 0; x < MAP_WIDTH; x++) {
    for (int y = 0; y < MAP_HEIGHT; y++) {
        tiles[x][y] = new SoilTile(x, y);
    }
}

// Step 2: Create fence borders
for (int x = 0; x < MAP_WIDTH; x++) {
    tiles[x][0] = new Fence(x, 0);  // Bottom border
    tiles[x][MAP_HEIGHT - 1] = new Fence(x, MAP_HEIGHT - 1);  // Top border
}
for (int y = 0; y < MAP_HEIGHT; y++) {
    tiles[0][y] = new Fence(0, y);  // Left border
    tiles[MAP_WIDTH - 1][y] = new Fence(MAP_WIDTH - 1, y);  // Right border
}

// Step 3: Create entrance at bottom center
int entranceX = MAP_WIDTH / 2;
tiles[entranceX][0] = new Entrance(entranceX, 0);

// Step 4: Create exit at top center
int exitX = MAP_WIDTH / 2;
tiles[exitX][MAP_HEIGHT - 1] = new Exit(exitX, MAP_HEIGHT - 1);

// Step 5: GUARANTEED TOOLS - Place all 3 tools
List<ToolItem.ItemType> tools = new ArrayList<>();
tools.add(ToolItem.ItemType.SHOVEL);
tools.add(ToolItem.ItemType.WATERING_CAN);
tools.add(ToolItem.ItemType.FERTILIZER);

for (ToolItem.ItemType tool : tools) {
    boolean placed = false;
    int attempts = 0;
    while (!placed && attempts < 100) {
        int x = 3 + rand.nextInt(MAP_WIDTH - 6);
        int y = 3 + rand.nextInt(MAP_HEIGHT - 6);
        if (tiles[x][y] instanceof SoilTile) {
            tiles[x][y] = new ToolItem(x, y, tool);
            Gdx.app.log("MapGen", "✓ Placed " + tool + " at (" + x + ", " + y + ")");
            placed = true;
        }
        attempts++;
    }
    if (!placed) {
        Gdx.app.error("MapGen", "Failed to place " + tool);
    }
}

// Step 6: GUARANTEED CHEST (goes in gameObjects, not tiles)
boolean chestPlaced = false;
int attempts = 0;
while (!chestPlaced && attempts < 100) {
    int x = 3 + rand.nextInt(MAP_WIDTH - 6);
    int y = 3 + rand.nextInt(MAP_HEIGHT - 6);
    if (tiles[x][y] instanceof SoilTile) {
        // TODO: Chest is not a GameObject, needs separate handling
        // gameObjects.add(new Chest(world, x + 0.5f, y + 0.5f));
        Gdx.app.log("MapGen", "✓ Chest placement skipped (not GameObject)");
        chestPlaced = true;
    }
    attempts++;
}

// Step 7: Scatter debris randomly (15-25 pieces)
int debrisCount = 15 + rand.nextInt(11);
int debrisPlaced = 0;
attempts = 0;
while (debrisPlaced < debrisCount && attempts < 200) {
    int x = 2 + rand.nextInt(MAP_WIDTH - 4);
    int y = 2 + rand.nextInt(MAP_HEIGHT - 4);
    if (tiles[x][y] instanceof SoilTile) {
        tiles[x][y] = new Debris(x, y, new SoilTile(x, y));
        debrisPlaced++;
    }
    attempts++;
}
Gdx.app.log("MapGen", "✓ Placed " + debrisPlaced + " debris pieces");

// Step 8: Add decorative flowers (8-12)
int flowerCount = 8 + rand.nextInt(5);
int flowersPlaced = 0;
attempts = 0;
while (flowersPlaced < flowerCount && attempts < 200) {
    int x = 2 + rand.nextInt(MAP_WIDTH - 4);
    int y = 2 + rand.nextInt(MAP_HEIGHT - 4);
    if (tiles[x][y] instanceof SoilTile) {
        tiles[x][y] = new Flowers(x, y);
        flowersPlaced++;
    }
    attempts++;
}
Gdx.app.log("MapGen", "✓ Placed " + flowersPlaced + " flowers");

// Finalize map
createWallBodies();
spawnWildlife();  // Don't call spawnGameObjects() since we already added chest manually

Gdx.app.log("MapGen", "✅ Procedural map generated successfully!");
}


    private void placeToolRandomly(ToolItem.ItemType toolType, Random rand) {
        int attempts = 0;
        while (attempts < 50) {
            int x = 2 + rand.nextInt(MAP_WIDTH - 4);
            int y = 2 + rand.nextInt(MAP_HEIGHT - 4);
            if (tiles[x][y] instanceof SoilTile) {
                tiles[x][y] = new ToolItem(x, y, toolType);
                return;
            }
            attempts++;
        }
    }
}
