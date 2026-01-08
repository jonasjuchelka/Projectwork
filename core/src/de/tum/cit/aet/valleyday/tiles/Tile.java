package de.tum.cit.aet.valleyday.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Drawable;

public abstract class Tile implements Drawable {
    protected final int x;
    protected final int y;

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    public abstract TextureRegion getCurrentAppearance();

    public void tick(float frameTime) {
        // Override in subclasses if needed
    }

    public boolean isWalkable() {
        return true;
    }

    public boolean isDestructible() {
        return false;
    }
}
