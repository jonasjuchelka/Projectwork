package de.tum.cit.aet.valleyday.texture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Enumerates all spritesheets used in the game and provides helper methods for grabbing texture regions from them.
 */
public enum SpriteSheet {

    /** The character spritesheet, which has a grid size of 16x32. */
    CHARACTER("character.png", 16, 32),
    /** The basic tiles spritesheet, which has a grid size of 16x16. */
    BASIC_TILES("basictiles.png", 16, 16);
    // If you want to use Fance_Gate later, add:
    // FENCE("Fance_Gate.jpg", 16, 16);

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
}
