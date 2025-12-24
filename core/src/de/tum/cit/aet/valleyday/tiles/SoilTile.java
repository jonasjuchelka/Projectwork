package de.tum.cit.aet.valleyday.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

/**
 * Walkable soil tile, may contain a crop.
 */
public class SoilTile extends Tile {

    private final TextureRegion appearance;

    public SoilTile(float x, float y) {
        super(x, y);
        // Soil: row 8, col 7
        this.appearance = SpriteSheet.BASIC_TILES.at(8, 7);
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        return appearance;
    }
}
