package de.tum.cit.aet.valleyday.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Textures;

public class ToolItem extends Tile {
    public enum ItemType {
        SHOVEL, FERTILIZER, WATERING_CAN
    }

    private final ItemType itemType;

    public ToolItem(int x, int y, ItemType itemType) {
        super(x, y);
        this.itemType = itemType;
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        return switch (itemType) {
            case SHOVEL -> Textures.SHOVEL;
            case FERTILIZER -> Textures.FERTILIZER;
            case WATERING_CAN -> Textures.WATERING_CAN;
        };
    }

    public ItemType getItemType() {
        return itemType;
    }

    @Override
    public boolean isWalkable() {
        return true;
    }
}
