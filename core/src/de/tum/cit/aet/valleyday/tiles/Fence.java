package de.tum.cit.aet.valleyday.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

/**
 * Indestructible fence tile (currently same sprite as debris).
 */
public class Fence extends Tile {

    private final TextureRegion appearance;

    public Fence(float x, float y) {
        super(x, y);
        // Placeholder: same as debris (row 8, col 3)
        this.appearance = SpriteSheet.BASIC_TILES.at(8, 3);
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        return appearance;
    }
}
