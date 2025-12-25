package de.tum.cit.aet.valleyday.texture;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Contains all texture constants used in the game.
 */
public class Textures {

    // ===== Existing Sprites =====
    public static final TextureRegion FLOWERS   = SpriteSheet.BASIC_TILES.at(2, 5);
    public static final TextureRegion CHEST     = SpriteSheet.BASIC_TILES.at(5, 5);
    public static final TextureRegion ENTRANCE  = SpriteSheet.BASIC_TILES.at(7, 1);
    public static final TextureRegion DEBRIS    = SpriteSheet.BASIC_TILES.at(8, 3);
    public static final TextureRegion FENCE     = SpriteSheet.FENCE.at(2, 2);

    // ===== Objects/Decorations (from OBJECTS sheet) =====
    public static final TextureRegion OBJECT_TORCH  = SpriteSheet.OBJECTS.at(1, 1);
    public static final TextureRegion OBJECT_DOOR   = SpriteSheet.OBJECTS.at(1, 2);
    public static final TextureRegion OBJECT_BOX    = SpriteSheet.OBJECTS.at(1, 3);
    public static final TextureRegion OBJECT_BARREL = SpriteSheet.OBJECTS.at(1, 4);
    public static final TextureRegion OBJECT_SIGN   = SpriteSheet.OBJECTS.at(1, 5);
    public static final TextureRegion OBJECT_BUSH   = SpriteSheet.OBJECTS.at(2, 1);
    public static final TextureRegion OBJECT_TREE   = SpriteSheet.OBJECTS.at(2, 2);

    // ===== NPCs (from CREATURES sheet) =====
    public static final TextureRegion NPC_VILLAGER = SpriteSheet.CREATURES.at(1, 1);
    public static final TextureRegion NPC_MERCHANT = SpriteSheet.CREATURES.at(1, 2);
    public static final TextureRegion NPC_GUARD    = SpriteSheet.CREATURES.at(1, 3);

    // ===== Tile Sprites (type 0-6) =====
    public static final TextureRegion TILE_WALL         = SpriteSheet.BASIC_TILES.at(1, 1); // 0
    public static final TextureRegion TILE_DESTRUCTIBLE = SpriteSheet.BASIC_TILES.at(1, 2); // 1
    public static final TextureRegion TILE_EXIT         = SpriteSheet.BASIC_TILES.at(7, 3); // 2
    public static final TextureRegion TILE_SPECIAL_3    = SpriteSheet.BASIC_TILES.at(1, 4); // 3
    public static final TextureRegion TILE_SPECIAL_4    = SpriteSheet.BASIC_TILES.at(1, 5); // 4
    public static final TextureRegion TILE_SPECIAL_5    = SpriteSheet.BASIC_TILES.at(2, 1); // 5
    public static final TextureRegion TILE_SPECIAL_6    = SpriteSheet.BASIC_TILES.at(2, 2); // 6

    public static TextureRegion getTileTexture(int type) {
        return switch (type) {
            case 0 -> TILE_WALL;
            case 1 -> TILE_DESTRUCTIBLE;
            case 2 -> TILE_EXIT;
            case 3 -> TILE_SPECIAL_3;
            case 4 -> TILE_SPECIAL_4;
            case 5 -> TILE_SPECIAL_5;
            case 6 -> TILE_SPECIAL_6;
            default -> null;
        };
    }

    public static TextureRegion getRandomObject() {
        int rand = (int)(Math.random() * 7);
        return switch (rand) {
            case 0 -> OBJECT_TORCH;
            case 1 -> OBJECT_DOOR;
            case 2 -> OBJECT_BOX;
            case 3 -> OBJECT_BARREL;
            case 4 -> OBJECT_SIGN;
            case 5 -> OBJECT_BUSH;
            case 6 -> OBJECT_TREE;
            default -> OBJECT_BOX;
        };
    }

    public static TextureRegion getRandomNPC() {
        int rand = (int)(Math.random() * 3);
        return switch (rand) {
            case 0 -> NPC_VILLAGER;
            case 1 -> NPC_MERCHANT;
            case 2 -> NPC_GUARD;
            default -> NPC_VILLAGER;
        };
    }
}
