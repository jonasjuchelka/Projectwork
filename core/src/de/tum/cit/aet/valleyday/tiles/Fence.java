package de.tum.cit.aet.valleyday.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

/**
 * Represents an indestructible fence tile that blocks player and wildlife movement.
 * Automatically selects the correct fence sprite based on position (corners, horizontal, vertical).
 */
public class Fence extends Tile {
    private static final int MAP_WIDTH = 21;
    private static final int MAP_HEIGHT = 21;

    private final FenceType fenceType;

    private enum FenceType {
        HORIZONTAL,        // Top and bottom rows
        VERTICAL,          // Left and right columns
        CORNER_TOP_LEFT,   // Position (0, MAP_HEIGHT-1)
        CORNER_TOP_RIGHT,  // Position (MAP_WIDTH-1, MAP_HEIGHT-1)
        CORNER_BOTTOM_LEFT,  // Position (0, 0)
        CORNER_BOTTOM_RIGHT  // Position (MAP_WIDTH-1, 0)
    }

    public Fence(int x, int y) {
        super(x, y);
        this.fenceType = determineFenceType(x, y);
    }

    private FenceType determineFenceType(int x, int y) {
        // Check corners first
        if (x == 0 && y == 0) {
            return FenceType.CORNER_BOTTOM_LEFT;
        } else if (x == MAP_WIDTH - 1 && y == 0) {
            return FenceType.CORNER_BOTTOM_RIGHT;
        } else if (x == 0 && y == MAP_HEIGHT - 1) {
            return FenceType.CORNER_TOP_LEFT;
        } else if (x == MAP_WIDTH - 1 && y == MAP_HEIGHT - 1) {
            return FenceType.CORNER_TOP_RIGHT;
        }

        // Check if on top or bottom edge (horizontal fence)
        if (y == 0 || y == MAP_HEIGHT - 1) {
            return FenceType.HORIZONTAL;
        }

        // Otherwise it's on left or right edge (vertical fence)
        return FenceType.VERTICAL;
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        // All fences use the same horizontal fence texture (at(1,1))
        // Vertical fences are rotated 90 degrees in GameScreen rendering

        TextureRegion region = switch (fenceType) {
            case HORIZONTAL -> SpriteSheet.FENCE.at(1, 1);  // Horizontal fence
            case VERTICAL -> SpriteSheet.FENCE.at(1, 1);    // Same texture, will be rotated
            case CORNER_TOP_LEFT -> SpriteSheet.FENCE.at(1, 3);     // Corner
            case CORNER_TOP_RIGHT -> SpriteSheet.FENCE.at(1, 3);    // Corner (will need rotation)
            case CORNER_BOTTOM_LEFT -> SpriteSheet.FENCE.at(1, 3);  // Corner (will need rotation)
            case CORNER_BOTTOM_RIGHT -> SpriteSheet.FENCE.at(1, 3); // Corner (will need rotation)
        };

        if (region == null) {
            com.badlogic.gdx.Gdx.app.error("Fence", "TextureRegion is NULL for fence type: " + fenceType + " at (" + x + "," + y + ")");
        }

        return region;
    }

    @Override
    public boolean isWalkable() {
        return false;
    }
}

