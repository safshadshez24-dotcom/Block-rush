package com.blockrush.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.blockrush.game.BlockRushGame;

public final class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("BLOCK RUSH");
        config.setWindowedMode(540, 960);
        config.useVsync(true);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new BlockRushGame(), config);
    }
}
