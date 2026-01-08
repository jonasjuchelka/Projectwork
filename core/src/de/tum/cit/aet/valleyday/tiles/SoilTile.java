package de.tum.cit.aet.valleyday.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Textures;

public class SoilTile extends Tile {
    private Crop crop;

    public SoilTile(int x, int y) {
        super(x, y);
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        if (crop != null) {
            return crop.getCurrentAppearance();
        }
        return Textures.SOIL_EMPTY;
    }

    @Override
    public void tick(float frameTime) {
        if (crop != null) {
            crop.tick(frameTime);
        }
    }

    public void plantSeed() {
        if (crop == null) {
            crop = new Crop(x, y);
        }
    }

    public void harvestCrop() {
        if (crop != null && crop.isMature()) {
            crop = null;
        }
    }

    public boolean hasCrop() {
        return crop != null;
    }

    public Crop getCrop() {
        return crop;
    }

    public void applyFertilizer() {
        if (crop != null) {
            crop.advanceGrowth();
        }
    }

    public void applyWateringCan() {
        if (crop != null && crop.isRotten()) {
            crop.restore();
        }
    }
}

