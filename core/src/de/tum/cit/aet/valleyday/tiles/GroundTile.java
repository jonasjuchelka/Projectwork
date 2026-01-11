package de.tum.cit.aet.valleyday.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

/**
 * Represents a ground tile that renders different textures based on whether it's inside or outside the fence border.
 * Ground tiles form the base layer of the map, rendered beneath all other tiles and objects.
 */
public class GroundTile extends Tile {
    private final boolean isInsideFence;

    /**
     * Creates a new ground tile at the specified position.
     * @param x The x-coordinate of the tile
     * @param y The y-coordinate of the tile
     * @param mapWidth The total width of the map
     * @param mapHeight The total height of the map
     */
    public GroundTile(int x, int y, int mapWidth, int mapHeight) {
        super(x, y);
        // Check if tile is inside the fence border
        // Fence border = outermost ring (x=0, x=width-1, y=0, y=height-1)
        this.isInsideFence = (x > 0 && x < mapWidth - 1 && y > 0 && y < mapHeight - 1);
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        if (isInsideFence) {
            // Inside fence: green grass (row 2, column 4)
            return SpriteSheet.BASIC_TILES_EXTENDED.at(2, 4);
        } else {
            // Outside fence: brown dirt (row 3, column 6)
            return SpriteSheet.BASIC_TILES_EXTENDED.at(3, 6);
        }
    }

    @Override
    public boolean isWalkable() {
        return true;  // Ground is always walkable
    }
}
