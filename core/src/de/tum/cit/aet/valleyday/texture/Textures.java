package de.tum.cit.aet.valleyday.texture;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Contains all texture constants used in the game.
 * It is good practice to keep all textures and animations in constants to avoid loading them multiple times.
 * These can be referenced anywhere they are needed.
 */
public class Textures {

    // Tiles (from basictiles.png, 16x16 grid)
    // Map types: 0=wall, 1=destructible, 2=entrance, 3-6=special
    public static final TextureRegion TILE_WALL = SpriteSheet.BASIC_TILES.at(1, 1);           // type 0
    public static final TextureRegion TILE_DESTRUCTIBLE = SpriteSheet.BASIC_TILES.at(1, 2);   // type 1
    public static final TextureRegion TILE_ENTRANCE = SpriteSheet.BASIC_TILES.at(1, 3);       // type 2
    public static final TextureRegion TILE_SPECIAL_3 = SpriteSheet.BASIC_TILES.at(1, 4);      // type 3
    public static final TextureRegion TILE_SPECIAL_4 = SpriteSheet.BASIC_TILES.at(1, 5);      // type 4
    public static final TextureRegion TILE_SPECIAL_5 = SpriteSheet.BASIC_TILES.at(2, 1);      // type 5
    public static final TextureRegion TILE_SPECIAL_6 = SpriteSheet.BASIC_TILES.at(2, 2);      // type 6

    // Other objects
    public static final TextureRegion FLOWERS = SpriteSheet.BASIC_TILES.at(2, 5);
    public static final TextureRegion CHEST = SpriteSheet.BASIC_TILES.at(5, 5);

    /**
     * Get the texture region for a given tile type.
     * @param type the tile type (from map file: -1=empty, 0-6=specific tiles)
     * @return the corresponding TextureRegion, or null if empty
     */
    public static TextureRegion getTileTexture(int type) {
        return switch (type) {
            case 0 -> TILE_WALL;
            case 1 -> TILE_DESTRUCTIBLE;
            case 2 -> TILE_ENTRANCE;
            case 3 -> TILE_SPECIAL_3;
            case 4 -> TILE_SPECIAL_4;
            case 5 -> TILE_SPECIAL_5;
            case 6 -> TILE_SPECIAL_6;
            default -> null;  // -1 or unknown = empty, don't draw
        };
    }
}

