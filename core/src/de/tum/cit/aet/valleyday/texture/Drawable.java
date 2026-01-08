package de.tum.cit.aet.valleyday.texture;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface Drawable {
    TextureRegion getCurrentAppearance();
    float getX();
    float getY();
}
