package de.tum.cit.aet.valleyday.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

/**
 * Entrance tile where the player spawns.
 */
public class Entrance extends Tile {

    private final TextureRegion appearance;

    public Entrance(float x, float y) {
        super(x, y);
        // Entrance: row 7, col 1
        this.appearance = SpriteSheet.BASIC_TILES.at(7, 1);
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        return appearance;
    }
}
