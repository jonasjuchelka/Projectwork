package de.tum.cit.aet.valleyday.texture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Textures {
    public static TextureRegion FENCE;
    public static TextureRegion DEBRIS;
    public static TextureRegion SOIL_EMPTY;
    public static TextureRegion ENTRANCE;
    public static TextureRegion EXIT;

    public static TextureRegion CROP_SEED;
    public static TextureRegion CROP_SPROUT;
    public static TextureRegion CROP_MATURE;
    public static TextureRegion CROP_ROTTEN;

    public static TextureRegion SHOVEL;
    public static TextureRegion FERTILIZER;
    public static TextureRegion WATERING_CAN;

    public static TextureRegion SNAIL;
    public static TextureRegion RAT;
    public static TextureRegion CROW;

    public static TextureRegion PLAYER;
    public static TextureRegion CHEST;
    public static TextureRegion FLOWERS;

    private static Texture placeholderTexture;

    public static void initialize() {
        Gdx.app.log("Textures", "Initializing textures...");

        // White placeholder for tiles
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 1);  // White
        pixmap.fill();
        placeholderTexture = new Texture(pixmap);
        pixmap.dispose();

        TextureRegion placeholder = new TextureRegion(placeholderTexture);

        // Create RED player texture
        Pixmap playerPixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        playerPixmap.setColor(1, 0, 0, 1);  // RED
        playerPixmap.fill();
        Texture playerTexture = new Texture(playerPixmap);
        playerPixmap.dispose();

        // Create GREEN wildlife texture
        Pixmap wildlifePixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        wildlifePixmap.setColor(0, 1, 0, 1);  // GREEN
        wildlifePixmap.fill();
        Texture wildlifeTexture = new Texture(wildlifePixmap);
        wildlifePixmap.dispose();

        TextureRegion playerRegion = new TextureRegion(playerTexture);
        TextureRegion wildlifeRegion = new TextureRegion(wildlifeTexture);

        // Assign textures
        FENCE = placeholder;
        DEBRIS = placeholder;
        SOIL_EMPTY = placeholder;
        ENTRANCE = placeholder;
        EXIT = placeholder;
        CROP_SEED = placeholder;
        CROP_SPROUT = placeholder;
        CROP_MATURE = placeholder;
        CROP_ROTTEN = placeholder;
        SHOVEL = placeholder;
        FERTILIZER = placeholder;
        WATERING_CAN = placeholder;

        SNAIL = wildlifeRegion;
        RAT = wildlifeRegion;
        CROW = wildlifeRegion;

        PLAYER = playerRegion;  // RED player!
        CHEST = placeholder;
        FLOWERS = placeholder;

        Gdx.app.log("Textures", "Textures initialized successfully!");
    }


    public static void dispose() {
        if (placeholderTexture != null) {
            placeholderTexture.dispose();
        }
    }

    public static TextureRegion getRandomObject() {
        TextureRegion[] objects = {SNAIL, DEBRIS, SHOVEL, CHEST, FLOWERS};
        return objects[(int) (Math.random() * objects.length)];
    }
}
