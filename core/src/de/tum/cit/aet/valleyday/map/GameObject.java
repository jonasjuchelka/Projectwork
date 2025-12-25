package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Drawable;

public class GameObject implements Drawable {
    private final float x;
    private final float y;
    private final TextureRegion texture;

    public GameObject(float x, float y, TextureRegion texture) {
        this.x = x;
        this.y = y;
        this.texture = texture;
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        return texture;
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
