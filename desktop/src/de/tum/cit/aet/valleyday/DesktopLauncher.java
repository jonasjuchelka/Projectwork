package de.tum.cit.aet.valleyday;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(ValleyDayGame.TITLE);
        config.setWindowedMode(ValleyDayGame.WINDOW_WIDTH, ValleyDayGame.WINDOW_HEIGHT);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new ValleyDayGame(), config);
    }



}
