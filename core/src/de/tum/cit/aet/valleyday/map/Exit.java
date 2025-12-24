package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

/**
 * Exit (farm gate) tile.
 */
public class Exit extends Tile {

    private final TextureRegion appearance;

    public Exit(float x, float y) {
        super(x, y);
        // Exit: row 7, col 3
        this.appearance = SpriteSheet.BASIC_TILES.at(7, 3);
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        return appearance;
    }
}
