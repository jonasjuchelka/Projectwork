package de.tum.cit.aet.valleyday.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

/**
 * Tool or item hidden under debris (shovel, fertilizer, watering can).
 */
public class ToolItem extends Tile {

    public enum Type {
        FERTILIZER,
        WATERING_CAN,
        SHOVEL
    }

    private final Type type;
    private final TextureRegion appearance;

    public ToolItem(float x, float y, Type type) {
        super(x, y);
        this.type = type;

        int row;
        int col;

        switch (type) {
            case FERTILIZER -> {
                // Fertilizer: row 4, col 4
                row = 4;
                col = 4;
            }
            case WATERING_CAN -> {
                // Placeholder – pick any until you choose a specific cell
                row = 4;
                col = 5;
            }
            case SHOVEL -> {
                // Placeholder
                row = 4;
                col = 6;
            }
            default -> {
                row = 4;
                col = 4;
            }
        }

        this.appearance = SpriteSheet.BASIC_TILES.at(row, col);
    }

    public Type getType() {
        return type;
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        return appearance;
    }
}
