package com.java.WhisperOfTheEmeraldForest.input;

public class InputState {
    private static final int KEY_COUNT = 512;

    private final boolean[] keys = new boolean[KEY_COUNT];
    private final boolean[] justPressed = new boolean[KEY_COUNT];
    private final boolean[] justReleased = new boolean[KEY_COUNT];

    public synchronized void setKeyDown(int keyCode, boolean down) {
        if (keyCode < 0 || keyCode >= KEY_COUNT) {
            return;
        }
        if (down) {
            if (!keys[keyCode]) {
                justPressed[keyCode] = true;
            }
            keys[keyCode] = true;
        } else {
            if (keys[keyCode]) {
                justReleased[keyCode] = true;
            }
            keys[keyCode] = false;
        }
    }

    public synchronized boolean isKeyDown(int keyCode) {
        return keyCode >= 0 && keyCode < KEY_COUNT && keys[keyCode];
    }

    public synchronized boolean isKeyJustPressed(int keyCode) {
        if (keyCode < 0 || keyCode >= KEY_COUNT) {
            return false;
        }
        if (justPressed[keyCode]) {
            justPressed[keyCode] = false;
            return true;
        }
        return false;
    }

    public synchronized boolean isKeyJustReleased(int keyCode) {
        if (keyCode < 0 || keyCode >= KEY_COUNT) {
            return false;
        }
        if (justReleased[keyCode]) {
            justReleased[keyCode] = false;
            return true;
        }
        return false;
    }

    public void endFrame() {
        for (int i = 0; i < KEY_COUNT; i++) {
            justPressed[i] = false;
            justReleased[i] = false;
        }
    }
}
