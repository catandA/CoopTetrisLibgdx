package me.catand.cooptetris.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;

import me.catand.cooptetris.util.TetrisSettings;

public class DesktopWindowListener implements Lwjgl3WindowListener {

    @Override
    public void created(Lwjgl3Window lwjgl3Window) {
    }

    @Override
    public void maximized(boolean b) {
        TetrisSettings.windowMaximized(b);
        if (b) {
            TetrisSettings.windowResolution(DesktopPlatformSupport.previousSizes[1]);
        }
    }

    @Override
    public void iconified(boolean b) {
    }

    public void focusLost() {
    }

    public void focusGained() {
    }

    public boolean closeRequested() {
        return true;
    }

    public void filesDropped(String[] strings) {
    }

    public void refreshRequested() {
    }
}
