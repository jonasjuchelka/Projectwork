package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.texture.Textures;
import de.tum.cit.aet.valleyday.tiles.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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

    // Collision Categories für Box2D Filtering
    public static final short CATEGORY_WALL = 0x0001;
    public static final short CATEGORY_PLAYER = 0x0002;
    public static final short CATEGORY_WILDLIFE = 0x0004;

    // Collision Masks - definiert womit jede Kategorie kollidiert
    public static final short MASK_WALL = CATEGORY_PLAYER;  // Wände kollidieren nur mit Spieler
    public static final short MASK_PLAYER = CATEGORY_WALL;  // Spieler kollidiert mit Wänden
    public static final short MASK_WILDLIFE = 0;  // Wildlife kollidiert mit nichts

    // TMX maps only
    private static final String[] TMX_MAPS = {
            "maps/map-1.tmx",
            "maps/map-3.tmx",
            "maps/map-4.tmx"
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

    // Game Over state
    private boolean gameOver = false;

    // Chaser Zombie System
    private ChaserZombie chaserZombie;
    private float gameTime = 0;
    private static final float CHASER_SPAWN_DELAY = 5.0f;
    private static final float CHASER_FOLLOW_DELAY = 5.0f;
    private static final float POSITION_RECORD_INTERVAL = 0.1f;
    private float positionRecordTimer = 0;

    // Position History für den Chaser (speichert Positionen mit Zeitstempel)
    private final Queue<float[]> positionHistory = new LinkedList<>();


    public GameMap(ValleyDayGame game) {
        this.game = game;
        this.world = new World(Vector2.Zero, true);
        this.player = new Player(this.world, 7f, 30f);
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
        Random rand = new Random();
        int mapIndex = rand.nextInt(TMX_MAPS.length);  // Random map!
        // Always load map-1 for now (change to random later)
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
        // Nur die äußeren Ränder der Map als Wände erstellen (Zeile/Spalte 0 und 31)
        // Der Spieler kann sich frei innerhalb der Map bewegen

        // Oberer und unterer Rand
        for (int x = 0; x < MAP_WIDTH; x++) {
            createWallBody(x, 0);           // Unterer Rand (y=0)
            createWallBody(x, MAP_HEIGHT - 1);  // Oberer Rand (y=31)
        }

        // Linker und rechter Rand (ohne Ecken, die sind schon oben erstellt)
        for (int y = 1; y < MAP_HEIGHT - 1; y++) {
            createWallBody(0, y);           // Linker Rand (x=0)
            createWallBody(MAP_WIDTH - 1, y);  // Rechter Rand (x=31)
        }

        Gdx.app.log("MapLoader", "Wall bodies created for map borders only");
    }

    private void createWallBody(int x, int y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x + 0.5f, y + 0.5f);
        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.filter.categoryBits = CATEGORY_WALL;
        fixtureDef.filter.maskBits = MASK_WALL;  // Kollidiert nur mit Spieler

        body.createFixture(fixtureDef);
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

        // 10 RATs (Zombies) zufällig auf der Map spawnen
        for (int i = 0; i < 10; i++) {
            int x = 5 + rand.nextInt(22);  // Zwischen 5 und 26
            int y = 5 + rand.nextInt(22);
            wildlifeVisitors.add(new WildlifeVisitor(world, x, y, WildlifeVisitor.WildlifeType.RAT, this));
        }

        // 10 CROWs zufällig auf der Map spawnen
        for (int i = 0; i < 10; i++) {
            int x = 5 + rand.nextInt(22);
            int y = 5 + rand.nextInt(22);
            wildlifeVisitors.add(new WildlifeVisitor(world, x, y, WildlifeVisitor.WildlifeType.CROW, this));
        }
    }

    public void tick(float frameTime) {
        if (gameOver) return;

        gameTime += frameTime;

        this.player.tick(frameTime);
        handlePlayerInteraction();

        // Spieler-Position aufzeichnen für Chaser
        recordPlayerPosition(frameTime);

        // Chaser Zombie spawnen nach 5 Sekunden
        if (chaserZombie == null && gameTime >= CHASER_SPAWN_DELAY) {
            spawnChaserZombie();
        }

        // Chaser Zombie updaten
        if (chaserZombie != null) {
            updateChaserZombie(frameTime);
            chaserZombie.tick(frameTime);
        }

        checkGameOver();

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

    private void recordPlayerPosition(float frameTime) {
        positionRecordTimer += frameTime;
        if (positionRecordTimer >= POSITION_RECORD_INTERVAL) {
            positionRecordTimer = 0;
            // Position mit Zeitstempel speichern: [x, y, time]
            positionHistory.add(new float[]{player.getX(), player.getY(), gameTime});
        }
    }

    private void spawnChaserZombie() {
        // Chaser spawnt am Eingang (7, 30 - gleiche Position wie Spieler-Start)
        chaserZombie = new ChaserZombie(world, 7f, 30f);
        chaserZombie.activate();
        Gdx.app.log("GameMap", "Chaser Zombie spawned!");
    }

    private void updateChaserZombie(float frameTime) {
        // Chaser folgt der Position von vor 5 Sekunden
        float targetTime = gameTime - CHASER_FOLLOW_DELAY;

        // Alte Positionen entfernen und die richtige Zielposition finden
        while (!positionHistory.isEmpty()) {
            float[] pos = positionHistory.peek();
            if (pos[2] <= targetTime) {
                chaserZombie.setTarget(pos[0], pos[1]);
                positionHistory.poll();
            } else {
                break;
            }
        }
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

        // E-Taste zum Verscheuchen von Wildlife
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            shooWildlife();
        }
    }

    private void shooWildlife() {
        float playerX = player.getX();
        float playerY = player.getY();
        float shooRange = 3.0f;

        for (WildlifeVisitor visitor : wildlifeVisitors) {
            float dx = playerX - visitor.getX();
            float dy = playerY - visitor.getY();
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < shooRange) {
                visitor.frighten();
                Gdx.app.log("GameMap", "Shooed wildlife!");
            }
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

    private void checkGameOver() {
        float playerX = player.getX();
        float playerY = player.getY();
        int px = player.getTileX();
        int py = player.getTileY();

        // Game Over bei Exit Position (1, 3)
        if (px == 1 && py == 3) {
            gameOver = true;
            player.getHitbox().setLinearVelocity(0, 0);
            return;
        }

        // Kollision mit Wildlife prüfen
        for (WildlifeVisitor visitor : wildlifeVisitors) {
            float dx = playerX - visitor.getX();
            float dy = playerY - visitor.getY();
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < 0.6f) {
                gameOver = true;
                player.getHitbox().setLinearVelocity(0, 0);
                Gdx.app.log("GameMap", "Game Over: Caught by wildlife!");
                return;
            }
        }

        // Kollision mit Chaser Zombie prüfen
        if (chaserZombie != null && chaserZombie.isActive()) {
            float dx = playerX - chaserZombie.getX();
            float dy = playerY - chaserZombie.getY();
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < 0.6f) {
                gameOver = true;
                player.getHitbox().setLinearVelocity(0, 0);
                Gdx.app.log("GameMap", "Game Over: Caught by Chaser Zombie!");
            }
        }
    }

    // Getters
    public boolean isGameOver() { return gameOver; }
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
    public ChaserZombie getChaserZombie() { return chaserZombie; }
    public int getWidth() { return MAP_WIDTH; }
    public int getHeight() { return MAP_HEIGHT; }
    public World getWorld() { return world; }
    public boolean hasTmxMap() { return tiledMap != null; }

    public void dispose() {
        if (tiledMap != null) tiledMap.dispose();
        if (tiledMapRenderer != null) tiledMapRenderer.dispose();
    }
}
