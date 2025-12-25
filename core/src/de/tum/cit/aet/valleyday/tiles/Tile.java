package de.tum.cit.aet.valleyday.tiles;

import de.tum.cit.aet.valleyday.texture.Drawable;

/**
 * A single tile on the map grid.
 */
public abstract class Tile implements Drawable {

    protected final float x;
    protected final float y;

    protected Tile(float x, float y) {
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
}
