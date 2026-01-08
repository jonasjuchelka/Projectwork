package de.tum.cit.aet.valleyday.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Textures;

public class Debris extends Tile {
    private final Tile hiddenTile;
    private float removalProgress = 0;

    public Debris(int x, int y, Tile hiddenTile) {
        super(x, y);
        this.hiddenTile = hiddenTile;
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        return Textures.DEBRIS;
    }

    @Override
    public boolean isWalkable() {
        return false;
    }

    @Override
    public boolean isDestructible() {
        return true;
    }

    public void addRemovalProgress(float amount) {
        removalProgress += amount;
    }

    public float getRemovalProgress() {
        return removalProgress;
    }

    public void resetRemoval() {
        removalProgress = 0;
    }

    public Tile getHiddenTile() {
        return hiddenTile;
    }
}
