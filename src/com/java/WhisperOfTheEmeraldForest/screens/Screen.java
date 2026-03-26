package com.java.WhisperOfTheEmeraldForest.screens;

import java.awt.Graphics2D;

public interface Screen {
    void onShow();
    void update(float delta);
    void render(Graphics2D g);
    void resize(int width, int height);
    void dispose();
}
