package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import de.tum.cit.aet.valleyday.texture.Textures;


public class GameMap {

    static {
        com.badlogic.gdx.physics.box2d.Box2D.init();
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
    private final Chest chest;

    // 21×21 tile grid: -1=empty, 0=wall, 1=destructible, 2-6=special
    private final int[][] tileGrid;
    // List of decorative game objects on the map
    private final List<GameObject> gameObjects;



    public GameMap(ValleyDayGame game) {
        this.game = game;
        this.world = new World(Vector2.Zero, true);
        this.player = new Player(this.world, 1, 3);
        this.chest = new Chest(this.world, 3, 3);
        // Initialize game objects list
        this.gameObjects = new ArrayList<>();


        // Initialize empty grid
        this.tileGrid = new int[MAP_WIDTH][MAP_HEIGHT];
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                tileGrid[x][y] = -1;  // empty by default
            }
        }
    }

    /**
     * Load map from .properties file.
     * Format: x,y=type
     */
    public void loadFromProperties(FileHandle file) {
        if (file == null || !file.exists()) {
            Gdx.app.log("MapLoad", "File not found: " + file);
            return;
        }

        // Clear grid
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                tileGrid[x][y] = -1;
            }
        }

        // Parse file
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
                    tileGrid[x][y] = type;
                }
            } catch (NumberFormatException e) {
                // skip
            }
        }

        // Create physics bodies for walls
        createWallBodies();

        // Spawn random objects on the map
        spawnGameObjects();

        Gdx.app.log("MapLoad", "Successfully loaded: " + file.name());
    }

    /**
     * Create Box2D static bodies for all walls and destructible blocks.
     * This allows the physics engine to handle collisions automatically.
     */
    private void createWallBodies() {
        // Create a static body for each wall/destructible tile
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                int tileType = tileGrid[x][y];

                // Type 0 = wall, Type 1 = destructible block
                if (tileType == 0 || tileType == 1) {
                    createWallBody(x, y, tileType);
                }
            }
        }

        Gdx.app.log("Collision", "Created wall bodies for map");
    }

    /**
     * Spawn random game objects on empty tiles (decorations, NPCs, etc.)
     */
    private void spawnGameObjects() {
        gameObjects.clear();
        Random rand = new Random();
        int objectCount = 8; // Spawn 8 objects
        int spawnedCount = 0;

        for (int i = 0; i < objectCount; i++) {
            int x = 5 + rand.nextInt(10);  // Random x between 5-15
            int y = 5 + rand.nextInt(10);  // Random y between 5-15

            TextureRegion texture = Textures.getRandomObject();

            if (texture != null) {
                gameObjects.add(new GameObject(x, y, texture));
                spawnedCount++;
                Gdx.app.log("Objects", "Spawned object at (" + x + ", " + y + ")");
            }
        }

        Gdx.app.log("Objects", "Total spawned: " + spawnedCount);
    }



    /**
     * Create a single static Box2D body for a wall or destructible block.
     * @param x Tile X coordinate
     * @param y Tile Y coordinate
     * @param type Tile type (0=wall, 1=destructible)
     */
    private void createWallBody(int x, int y, int type) {
        // Define a static body (doesn't move)
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        // Position at tile center
        bodyDef.position.set(x + 0.5f, y + 0.5f);

        // Create the body
        Body body = world.createBody(bodyDef);

        // Create a box shape (1x1 tile size)
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f);  // 0.5 = half tile (1x1 total)

        // Attach shape to body
        body.createFixture(shape, 1.0f);

        // Clean up shape (can be disposed after creating fixture)
        shape.dispose();

        // Store tile type in user data for later reference
        body.setUserData(type);
    }

    public int getTileType(int x, int y) {
        if (x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT) {
            return tileGrid[x][y];
        }
        return -1;
    }

    public int[][] getTileGrid() {
        return tileGrid;
    }

    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    public void tick(float frameTime) {
        this.player.tick(frameTime);
        doPhysicsStep(frameTime);
    }

    private void doPhysicsStep(float frameTime) {
        this.physicsTime += frameTime;
        while (this.physicsTime >= TIME_STEP) {
            this.world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            this.physicsTime -= TIME_STEP;
        }
    }

    public Player getPlayer() {
        return player;
    }

    public Chest getChest() {
        return chest;
    }

    public int getWidth() {
        return MAP_WIDTH;
    }

    public int getHeight() {
        return MAP_HEIGHT;
    }

    @Deprecated
    public Tile getTile(int x, int y) {
        return null;  // Implement concrete Tile types later
    }
}
