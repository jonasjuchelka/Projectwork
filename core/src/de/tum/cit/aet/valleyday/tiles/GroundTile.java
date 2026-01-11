package de.tum.cit.aet.valleyday.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * GroundTile represents the base ground layer that appears everywhere on the map.
 */
public class GroundTile extends Tile {
    public GroundTile(int x, int y, int mapWidth, int mapHeight) {
        super(x, y);
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        return Textures.SOIL_EMPTY;
    }

    @Override
    public boolean isWalkable() {
        return true;
    }
}
