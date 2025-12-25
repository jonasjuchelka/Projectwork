package de.tum.cit.aet.valleyday.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

/**
 * Indestructible fence tile.
 */
public class Fence extends Tile {
    private final TextureRegion appearance;

    public Fence(float x, float y) {
        super(x, y);
        // Fence: Fance_Gate.png row 2, col 2
        this.appearance = SpriteSheet.FENCE.at(2, 2);
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        return appearance;
    }
}
