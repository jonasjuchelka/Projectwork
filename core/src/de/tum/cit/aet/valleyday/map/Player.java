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
import de.tum.cit.aet.valleyday.tiles.ToolItem;

import java.util.ArrayList;
import java.util.List;

public class Player implements Drawable {
    private float elapsedTime;
    private final Body hitbox;
    private static final float MOVEMENT_SPEED = 5.0f;
    private Direction currentDirection = Direction.DOWN;

    // INVENTORY SYSTEM - ADDED BACK
    private final List<ToolItem.ItemType> inventory;
    private ToolItem.ItemType currentTool;
    private int coins;

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public Player(World world, float x, float y) {
        this.hitbox = createHitbox(world, x, y);
        this.inventory = new ArrayList<>();
        this.currentTool = null;
        this.coins = 0;
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
        handleToolSwitch();
    }

    private void handleToolSwitch() {
        if (inventory.isEmpty()) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && inventory.size() > 0) {
            currentTool = inventory.get(0);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && inventory.size() > 1) {
            currentTool = inventory.get(1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && inventory.size() > 2) {
            currentTool = inventory.get(2);
        }
    }

    // INVENTORY METHODS - ADDED BACK
    public void addItem(ToolItem.ItemType item) {
        if (!inventory.contains(item)) {
            inventory.add(item);
            if (currentTool == null) {
                currentTool = item;
            }
            Gdx.app.log("Player", "Picked up: " + item);
        }
    }

    public void addCoins(int amount) {
        this.coins += amount;
    }

    public boolean hasTool(ToolItem.ItemType tool) {
        return inventory.contains(tool);
    }

    public ToolItem.ItemType getCurrentTool() {
        return currentTool;
    }

    public List<ToolItem.ItemType> getInventory() {
        return inventory;
    }

    public int getCoins() {
        return coins;
    }

    public void setPosition(float x, float y) {
        hitbox.setTransform(x, y, 0);
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
