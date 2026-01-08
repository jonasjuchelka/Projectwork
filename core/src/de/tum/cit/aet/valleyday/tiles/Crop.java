package de.tum.cit.aet.valleyday.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Textures;

public class Crop extends Tile {
    public enum GrowthStage {
        SEED, SPROUT, MATURE, ROTTEN
    }

    private GrowthStage stage = GrowthStage.SEED;
    private float growthTimer = 0;
    private static final float TIME_PER_STAGE = 20.0f; // 20 seconds per stage

    public Crop(int x, int y) {
        super(x, y);
    }

    @Override
    public void tick(float frameTime) {
        if (stage == GrowthStage.ROTTEN) {
            return;
        }

        growthTimer += frameTime;

        if (growthTimer >= TIME_PER_STAGE) {
            growthTimer = 0;

            switch (stage) {
                case SEED -> stage = GrowthStage.SPROUT;
                case SPROUT -> stage = GrowthStage.MATURE;
                case MATURE -> stage = GrowthStage.ROTTEN;
            }
        }
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        return switch (stage) {
            case SEED -> Textures.CROP_SEED;
            case SPROUT -> Textures.CROP_SPROUT;
            case MATURE -> Textures.CROP_MATURE;
            case ROTTEN -> Textures.CROP_ROTTEN;
        };
    }

    public boolean isMature() {
        return stage == GrowthStage.MATURE;
    }

    public boolean isRotten() {
        return stage == GrowthStage.ROTTEN;
    }

    public void advanceGrowth() {
        if (stage != GrowthStage.ROTTEN && stage != GrowthStage.MATURE) {
            stage = GrowthStage.values()[stage.ordinal() + 1];
        }
    }

    public void restore() {
        if (stage == GrowthStage.ROTTEN) {
            stage = GrowthStage.MATURE;
        }
    }
}

