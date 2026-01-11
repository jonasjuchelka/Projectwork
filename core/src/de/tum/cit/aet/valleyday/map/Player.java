package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.texture.Animations;
import de.tum.cit.aet.valleyday.texture.Drawable;

public class Player implements Drawable {
    private float elapsedTime;
    private final Body hitbox;
    private static final float MOVEMENT_SPEED = 5.0f;
    private Direction currentDirection = Direction.DOWN;

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public Player(World world, float x, float y) {
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
        this.elapsedTime += frameTime;

        float xVelocity = 0;
        float yVelocity = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            yVelocity = MOVEMENT_SPEED;
            currentDirection = Direction.UP;
        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            yVelocity = -MOVEMENT_SPEED;
            currentDirection = Direction.DOWN;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            xVelocity = -MOVEMENT_SPEED;
            currentDirection = Direction.LEFT;
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            xVelocity = MOVEMENT_SPEED;
            currentDirection = Direction.RIGHT;
        }

        this.hitbox.setLinearVelocity(xVelocity, yVelocity);
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        Animation<TextureRegion> currentAnimation;

        switch (currentDirection) {
            case UP:
                currentAnimation = Animations.CHARACTER_WALK_UP;
                break;
            case DOWN:
                currentAnimation = Animations.CHARACTER_WALK_DOWN;
                break;
            case LEFT:
                currentAnimation = Animations.CHARACTER_WALK_LEFT;
                break;
            case RIGHT:
                currentAnimation = Animations.CHARACTER_WALK_RIGHT;
                break;
            default:
                currentAnimation = Animations.CHARACTER_WALK_DOWN;
                break;
        }

        return currentAnimation.getKeyFrame(this.elapsedTime, true);
    }

    @Override
    public float getX() {
        return hitbox.getPosition().x;
    }

    @Override
    public float getY() {
        return hitbox.getPosition().y;
    }

    public int getTileX() {
        return (int) getX();
    }

    public int getTileY() {
        return (int) getY();
    }
}
