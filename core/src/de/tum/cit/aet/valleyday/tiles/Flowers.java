package de.tum.cit.aet.valleyday.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * Flowers are a static object without any special properties.
 * They do not have a hitbox, so the player does not collide with them.
 * They are purely decorative and serve as a nice floor decoration.
 */
public class Flowers extends Tile {  // ← FIXED: Removed "implements Drawable", now properly extends Tile

    public Flowers(int x, int y) {
        super(x, y);  // ← FIXED: Call super constructor from Tile
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        return Textures.FLOWERS;
    }

    @Override
    public boolean isWalkable() {
        return true;  // Player can walk over flowers (no collision)
    }
}
