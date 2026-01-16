package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.Textures;
import de.tum.cit.aet.valleyday.tiles.Crop;
import de.tum.cit.aet.valleyday.tiles.SoilTile;
import de.tum.cit.aet.valleyday.tiles.Tile;

import java.util.Random;

public class WildlifeVisitor implements Drawable {
    public enum WildlifeType {
        SNAIL, RAT, CROW
    }

    private final Body hitbox;
    private final WildlifeType type;
    private final GameMap gameMap;
    private float moveTimer = 0;
    private float moveDirection = 0;
    private float targetX = -1;
    private float targetY = -1;
    private float searchTimer = 0;
    private static final float MOVE_SPEED = 2.0f;
    private static final float SEARCH_INTERVAL = 3.0f;
    private static final Random random = new Random();

    public WildlifeVisitor(World world, float x, float y, WildlifeType type, GameMap gameMap) {
        this.type = type;
        this.gameMap = gameMap;
        this.hitbox = createHitbox(world, x, y);
    }

    private Body createHitbox(World world, float startX, float startY) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(startX, startY);
        Body body = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(0.3f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 1.0f;
        fixtureDef.filter.categoryBits = GameMap.CATEGORY_WILDLIFE;
        fixtureDef.filter.maskBits = GameMap.MASK_WILDLIFE;  // Kollidiert mit nichts (kann durch Wände gehen)

        body.createFixture(fixtureDef);
        circle.dispose();
        body.setUserData(this);
        return body;
    }

    public void tick(float frameTime) {
        moveTimer -= frameTime;
        searchTimer -= frameTime;

        // Periodisch nach reifen Pflanzen suchen
        if (searchTimer <= 0) {
            searchTimer = SEARCH_INTERVAL;
            findNearestMatureCrop();
        }

        // Bewegungsrichtung bestimmen
        if (moveTimer <= 0) {
            if (targetX >= 0 && targetY >= 0) {
                // Intelligente Bewegung: Richtung zum Ziel berechnen
                float dx = targetX - getX();
                float dy = targetY - getY();
                moveDirection = (float) Math.toDegrees(Math.atan2(dy, dx));
            } else {
                // Zufällige Bewegung wenn kein Ziel
                moveDirection = random.nextFloat() * 360;
            }
            moveTimer = 1.0f + random.nextFloat() * 2.0f;
        }

        float rad = (float) Math.toRadians(moveDirection);
        float vx = (float) Math.cos(rad) * MOVE_SPEED;
        float vy = (float) Math.sin(rad) * MOVE_SPEED;

        hitbox.setLinearVelocity(vx, vy);

        // Prüfen ob auf reifer Pflanze - dann stehlen
        tryStealCrop();
    }

    private void findNearestMatureCrop() {
        if (gameMap == null) return;

        Tile[][] tiles = gameMap.getTiles();
        float nearestDist = Float.MAX_VALUE;
        targetX = -1;
        targetY = -1;

        for (int x = 0; x < gameMap.getWidth(); x++) {
            for (int y = 0; y < gameMap.getHeight(); y++) {
                Tile tile = tiles[x][y];
                if (tile instanceof SoilTile) {
                    SoilTile soil = (SoilTile) tile;
                    if (soil.hasCrop() && soil.getCrop().isMature()) {
                        float dx = x - getX();
                        float dy = y - getY();
                        float dist = dx * dx + dy * dy;
                        if (dist < nearestDist) {
                            nearestDist = dist;
                            targetX = x + 0.5f;
                            targetY = y + 0.5f;
                        }
                    }
                }
            }
        }
    }

    private void tryStealCrop() {
        if (gameMap == null) return;

        int tileX = (int) getX();
        int tileY = (int) getY();

        Tile tile = gameMap.getTile(tileX, tileY);
        if (tile instanceof SoilTile) {
            SoilTile soil = (SoilTile) tile;
            if (soil.hasCrop() && soil.getCrop().isMature()) {
                soil.removeCrop();
                targetX = -1;
                targetY = -1;
            }
        }
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        return switch (type) {
            case SNAIL -> Textures.SNAIL;
            case RAT -> Textures.RAT;
            case CROW -> Textures.CROW;
        };
    }

    @Override
    public float getX() {
        return hitbox.getPosition().x;
    }

    @Override
    public float getY() {
        return hitbox.getPosition().y;
    }

    public void frighten() {
        // Wildlife runs away
        moveDirection += 180;
        moveTimer = 2.0f;
    }
}
