package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.ValleyDayGame;

import java.util.List;

/**
 * Represents the game map.
 * Loads tile data from .properties files and manages all entities and physics.
 */
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

    // Tile grid: stores tile type ID at each position
    private final int[][] tileGrid;

    public GameMap(ValleyDayGame game) {
        this.game = game;
        this.world = new World(Vector2.Zero, true);

        // Initialize player and chest
        this.player = new Player(this.world, 1, 3);
        this.chest = new Chest(this.world, 3, 3);

        // Initialize empty tile grid
        this.tileGrid = new int[MAP_WIDTH][MAP_HEIGHT];
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                tileGrid[x][y] = -1;  // -1 = empty/grass
            }
        }
    }

    /**
     * Load a map from a .properties file.
     * Format: x,y=type
     * Types: 0=wall, 1=destructible, 2=entrance, 3-6=special
     */
    public void loadFromProperties(FileHandle file) {
        int[][] loadedGrid = MapLoader.load(file, MAP_WIDTH, MAP_HEIGHT);
        if (loadedGrid == null) {
            return;
        }

        for (int x = 0; x < MAP_WIDTH; x++) {
            System.arraycopy(loadedGrid[x], 0, tileGrid[x], 0, MAP_HEIGHT);
        }

        Gdx.app.log("MapLoad", "Successfully loaded map: " + file.name());
    }

    /**
     * Get the tile type at a specific coordinate.
     * Returns -1 for empty, 0 for wall, 1 for destructible, etc.
     */
    public int getTileType(int x, int y) {
        if (x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT) {
            return tileGrid[x][y];
        }
        return -1;
    }

    /**
     * Check if a tile is walkable (empty or special, not wall/destructible).
     */
    public boolean isWalkable(int x, int y) {
        int type = getTileType(x, y);
        return type == -1 || type >= 2;  // empty or special tiles are walkable
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

    /**
     * Get all tile positions with their types for rendering.
     */
    public int[][] getTileGrid() {
        return tileGrid;
    }

    /**
     * Legacy method for compatibility.
     */
    @Deprecated
    public Tile getTile(int x, int y) {
        // You can implement this later if you create concrete Tile subclasses
        return null;
    }
}
