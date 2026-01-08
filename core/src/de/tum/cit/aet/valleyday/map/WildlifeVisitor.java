package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.Textures;

import java.util.Random;

public class WildlifeVisitor implements Drawable {
    public enum WildlifeType {
        SNAIL, RAT, CROW
    }

    private final Body hitbox;
    private final WildlifeType type;
    private float moveTimer = 0;
    private float moveDirection = 0;
    private static final float MOVE_SPEED = 2.0f;
    private static final Random random = new Random();

    public WildlifeVisitor(World world, float x, float y, WildlifeType type) {
        this.type = type;
        this.hitbox = createHitbox(world, x, y);
    }

    private Body createHitbox(World world, float startX, float startY) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(startX, startY);
        Body body = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(0.3f);
        body.createFixture(circle, 1.0f);
        circle.dispose();
        body.setUserData(this);
        return body;
    }

    public void tick(float frameTime) {
        moveTimer -= frameTime;

        if (moveTimer <= 0) {
            moveDirection = random.nextFloat() * 360;
            moveTimer = 1.0f + random.nextFloat() * 2.0f;
        }

        float rad = (float) Math.toRadians(moveDirection);
        float vx = (float) Math.cos(rad) * MOVE_SPEED;
        float vy = (float) Math.sin(rad) * MOVE_SPEED;

        hitbox.setLinearVelocity(vx, vy);
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
