package de.tum.cit.aet.valleyday.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Textures;

public class Entrance extends Tile {
    public Entrance(int x, int y) {
        super(x, y);
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        return Textures.ENTRANCE;
    }

    @Override
    public boolean isWalkable() {
        return true;
    }
}
