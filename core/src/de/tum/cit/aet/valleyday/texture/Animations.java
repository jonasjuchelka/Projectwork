package de.tum.cit.aet.valleyday.texture;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Animations {
    public static Animation<TextureRegion> CHARACTER_WALK_DOWN;
    public static Animation<TextureRegion> CHARACTER_WALK_RIGHT;
    public static Animation<TextureRegion> CHARACTER_WALK_UP;
    public static Animation<TextureRegion> CHARACTER_WALK_LEFT;

    public static void initialize() {
        // Walk Down (Zeile 1, Spalten 1-4)
        TextureRegion[] walkDownFrames = {
            SpriteSheet.CHARACTER.at(1, 1),
            SpriteSheet.CHARACTER.at(1, 2),
            SpriteSheet.CHARACTER.at(1, 3),
            SpriteSheet.CHARACTER.at(1, 4)
        };
        CHARACTER_WALK_DOWN = new Animation<>(0.1f, walkDownFrames);
        CHARACTER_WALK_DOWN.setPlayMode(Animation.PlayMode.LOOP);

        // Walk Right (Zeile 2, Spalten 1-4)
        TextureRegion[] walkRightFrames = {
            SpriteSheet.CHARACTER.at(2, 1),
            SpriteSheet.CHARACTER.at(2, 2),
            SpriteSheet.CHARACTER.at(2, 3),
            SpriteSheet.CHARACTER.at(2, 4)
        };
        CHARACTER_WALK_RIGHT = new Animation<>(0.1f, walkRightFrames);
        CHARACTER_WALK_RIGHT.setPlayMode(Animation.PlayMode.LOOP);

        // Walk Up (Zeile 3, Spalten 1-4)
        TextureRegion[] walkUpFrames = {
            SpriteSheet.CHARACTER.at(3, 1),
            SpriteSheet.CHARACTER.at(3, 2),
            SpriteSheet.CHARACTER.at(3, 3),
            SpriteSheet.CHARACTER.at(3, 4)
        };
        CHARACTER_WALK_UP = new Animation<>(0.1f, walkUpFrames);
        CHARACTER_WALK_UP.setPlayMode(Animation.PlayMode.LOOP);

        // Walk Left (Zeile 4, Spalten 1-4)
        TextureRegion[] walkLeftFrames = {
            SpriteSheet.CHARACTER.at(4, 1),
            SpriteSheet.CHARACTER.at(4, 2),
            SpriteSheet.CHARACTER.at(4, 3),
            SpriteSheet.CHARACTER.at(4, 4)
        };
        CHARACTER_WALK_LEFT = new Animation<>(0.1f, walkLeftFrames);
        CHARACTER_WALK_LEFT.setPlayMode(Animation.PlayMode.LOOP);
    }
}
