package de.tum.cit.aet.valleyday.texture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Enumerates all spritesheets used in the game and provides helper methods
 * for grabbing texture regions from them.
 */
public enum SpriteSheet {



    /** The character spritesheet, which has a grid size of 16x32. */
    CHARACTER("character.png", 16, 32),

    /** The basic tiles spritesheet, which has a grid size of 16x16. */
    BASIC_TILES("basics.png", 16, 16),

    /** The basic tiles spritesheet, which has a grid size of 16x16. */
    CROPS("crops.png", 16, 16),

    /** The extended basic tiles spritesheet for ground tiles, which has a grid size of 16x16. */
    BASIC_TILES_EXTENDED("basictiles.png", 16, 16),

    /** The NPC / creatures spritesheet, which has a grid size of 17x17. */
    CREATURES("creatures.png", 17, 17),

    /** The fence/gate spritesheet, which has 4 sprites of size 48x24 in a horizontal row. */
    FENCE("Fance_Gate.png", 48, 24),

    /** The objects/decorations spritesheet, which has a grid size of 16x16. */
    OBJECTS("objects.png", 16, 16),

    /** The harvest spritesheet, which has a grid size of 16x16. */
    HARVEST("Harvest.png", 16, 16),

    SNAIL("snail.png", 48, 48);

    private final Texture spritesheet;
    private final int width;
    private final int height;

    SpriteSheet(String filename, int width, int height) {
        this.spritesheet = new Texture(Gdx.files.internal("texture/" + filename));
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the TextureRegion at the specified row and column (1-based coordinates).
     */
    public TextureRegion at(int row, int column) {
        return new TextureRegion(
                spritesheet,
                (column - 1) * this.width,
                (row - 1) * this.height,
                this.width,
                this.height
        );
    }

    public TextureRegion atVertical(int startRow, int column, int numRows) {
        return new TextureRegion(
            spritesheet,
            (column - 1) * this.width,
            (startRow - 1) * this.height,
            this.width,
            this.height * numRows
        );
    }






}
