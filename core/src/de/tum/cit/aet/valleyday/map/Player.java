package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.texture.Animations;
import de.tum.cit.aet.valleyday.texture.Drawable;

/**
 * Represents the player character in the game.
 * The player has a hitbox, so it can collide with other objects in the game.
 * Movement is controlled via WASD keyboard input.
 */
public class Player implements Drawable {

    /** Total time elapsed since the game started. We use this for animating the player. */
    private float elapsedTime;

    /** The Box2D hitbox of the player, used for position and collision detection. */
    private final Body hitbox;

    /** Movement speed in tiles per second. */
    private static final float MOVEMENT_SPEED = 5.0f;

    /** Current direction the player is facing/moving. */
    private Direction currentDirection = Direction.DOWN;

    /**
     * Direction enum for animation selection.
     */
    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public Player(World world, float x, float y) {
        this.hitbox = createHitbox(world, x, y);
    }

    /**
     * Creates a Box2D body for the player.
     * This is what the physics engine uses to move the player around and detect collisions with other bodies.
     * @param world The Box2D world to add the body to.
     * @param startX The initial X position.
     * @param startY The initial Y position.
     * @return The created body.
     */
    private Body createHitbox(World world, float startX, float startY) {
        // BodyDef is like a blueprint for the movement properties of the body.
        BodyDef bodyDef = new BodyDef();
        // Dynamic bodies are affected by forces and collisions.
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        // Set the initial position of the body.
        bodyDef.position.set(startX, startY);
        // Create the body in the world using the body definition.
        Body body = world.createBody(bodyDef);

        // Now we need to give the body a shape so the physics engine knows how to collide with it.
        // We'll use a circle shape for the player.
        CircleShape circle = new CircleShape();
        // Give the circle a radius of 0.3 tiles (the player is 0.6 tiles wide).
        circle.setRadius(0.3f);
        // Attach the shape to the body as a fixture.
        // Bodies can have multiple fixtures, but we only need one for the player.
        body.createFixture(circle, 1.0f);
        // We're done with the shape, so we should dispose of it to free up memory.
        circle.dispose();

        // Set the player as the user data of the body so we can look up the player from the body later.
        body.setUserData(this);

        return body;
    }

    /**
     * Handle player input and update movement velocity.
     * Reads WASD keys and moves the player accordingly.
     * @param frameTime the time since the last frame.
     */
    public void tick(float frameTime) {
        this.elapsedTime += frameTime;

        // Read input and determine desired velocity
        float xVelocity = 0;
        float yVelocity = 0;

        // W key: move up
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            yVelocity = MOVEMENT_SPEED;
            currentDirection = Direction.UP;
        }
        // S key: move down
        else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            yVelocity = -MOVEMENT_SPEED;
            currentDirection = Direction.DOWN;
        }

        // A key: move left
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            xVelocity = -MOVEMENT_SPEED;
            currentDirection = Direction.LEFT;
        }
        // D key: move right
        else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            xVelocity = MOVEMENT_SPEED;
            currentDirection = Direction.RIGHT;
        }

        // Set the velocity on the physics body
        this.hitbox.setLinearVelocity(xVelocity, yVelocity);
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        // Return animation based on current direction
        return switch (currentDirection) {
            case UP -> Animations.CHARACTER_WALK_DOWN.getKeyFrame(this.elapsedTime, true);
            case DOWN -> Animations.CHARACTER_WALK_DOWN.getKeyFrame(this.elapsedTime, true);
            case LEFT -> Animations.CHARACTER_WALK_DOWN.getKeyFrame(this.elapsedTime, true);
            case RIGHT -> Animations.CHARACTER_WALK_DOWN.getKeyFrame(this.elapsedTime, true);
        };
    }

    @Override
    public float getX() {
        // The x-coordinate of the player is the x-coordinate of the hitbox (this can change every frame).
        return hitbox.getPosition().x;
    }

    @Override
    public float getY() {
        // The y-coordinate of the player is the y-coordinate of the hitbox (this can change every frame).
        return hitbox.getPosition().y;
    }

    /**
     * Get the player's current position as a tile coordinate (rounded down).
     */
    public int getTileX() {
        return (int) getX();
    }

    /**
     * Get the player's current position as a tile coordinate (rounded down).
     */
    public int getTileY() {
        return (int) getY();
    }
}
