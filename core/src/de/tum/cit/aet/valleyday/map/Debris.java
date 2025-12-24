package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

/**
 * Destructible debris (branch) tile.
 * May hide another tile underneath (tool or exit).
 */
public class Debris extends Tile {

    private final TextureRegion appearance;
    private Tile hiddenTile;
    private boolean cleared = false;

    public Debris(float x, float y) {
        super(x, y);
        // Debris tile: row 8, col 3
        this.appearance = SpriteSheet.BASIC_TILES.at(8, 3);
    }

    public void setHiddenTile(Tile hiddenTile) {
        this.hiddenTile = hiddenTile;
    }

    public Tile getHiddenTile() {
        return hiddenTile;
    }

    public boolean isCleared() {
        return cleared;
    }

    public void clear() {
        this.cleared = true;
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        if (cleared && hiddenTile != null) {
            return hiddenTile.getCurrentAppearance();
        }
        return appearance;
    }
}
