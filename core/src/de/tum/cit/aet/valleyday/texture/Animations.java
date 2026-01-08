package de.tum.cit.aet.valleyday.texture;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Animations {
    public static Animation<TextureRegion> CHARACTER_WALK_DOWN;

    public static void initialize() {
        // Use the RED player texture
        TextureRegion[] frames = {Textures.PLAYER};
        CHARACTER_WALK_DOWN = new Animation<>(0.1f, frames);
        CHARACTER_WALK_DOWN.setPlayMode(Animation.PlayMode.LOOP);
    }
}
