package com.java.WhisperOfTheEmeraldForest.util;

import java.awt.image.BufferedImage;

public class Animation {
    private final BufferedImage[] frames;
    private final float frameDuration;

    public Animation(float frameDuration, BufferedImage[] frames) {
        this.frameDuration = frameDuration;
        this.frames = frames;
    }

    public BufferedImage getFrame(float stateTime, boolean loop) {
        if (frames.length == 0) {
            return null;
        }
        int frameIndex = (int) (stateTime / frameDuration);
        if (loop) {
            frameIndex = frameIndex % frames.length;
        } else {
            frameIndex = Math.min(frames.length - 1, frameIndex);
        }
        return frames[frameIndex];
    }

    public boolean isFinished(float stateTime) {
        return stateTime >= frameDuration * frames.length;
    }
}
