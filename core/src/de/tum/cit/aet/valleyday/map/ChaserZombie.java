package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.Textures;


public class ChaserZombie implements Drawable {
    private static final float MOVE_SPEED = 5.0f;
    private static final float POSITION_RECORD_INTERVAL = 0.1f;

    private final Body hitbox;
    private float targetX;
    private float targetY;
    private boolean active = false;

    public ChaserZombie(World world, float startX, float startY) {
        this.hitbox = createHitbox(world, startX, startY);
        this.targetX = startX;
        this.targetY = startY;
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

    public void activate() {
        active = true;
    }

    public boolean isActive() {
        return active;
    }

    public void setTarget(float x, float y) {
        this.targetX = x;
        this.targetY = y;
    }

    public void tick(float frameTime) {
        if (!active) {
            hitbox.setLinearVelocity(0, 0);
            return;
        }

        float dx = targetX - getX();
        float dy = targetY - getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.1f) {
            float vx = (dx / distance) * MOVE_SPEED;
            float vy = (dy / distance) * MOVE_SPEED;
            hitbox.setLinearVelocity(vx, vy);
        } else {
            hitbox.setLinearVelocity(0, 0);
        }
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        return Textures.RAT;
    }

    @Override
    public float getX() {
        return hitbox.getPosition().x;
    }

    @Override
    public float getY() {
        return hitbox.getPosition().y;
    }
}
