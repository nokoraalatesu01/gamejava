package com.java.WhisperOfTheEmeraldForest.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class OneShotSound {
    private static final Path ROOT = Paths.get(System.getProperty("user.dir"), "assets");

    private OneShotSound() {}

    public static void play(String relativePath) {
        Clip clip = playClip(relativePath);
        if (clip != null) {
            clip.start();
        }
    }

    public static Clip playClip(String relativePath) {
        Path path = ROOT.resolve(relativePath).normalize();
        if (!Files.exists(path)) {
            throw new IllegalStateException("Missing sound: " + path);
        }
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(path.toFile());
            Clip clip = AudioSystem.getClip();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    try {
                        stream.close();
                    } catch (IOException ignored) {}
                }
            });
            clip.open(stream);
            return clip;
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            throw new IllegalStateException("Failed to play sound: " + path, e);
        }
    }

    public static void stop(Clip clip) {
        if (clip == null) {
            return;
        }
        if (clip.isRunning()) {
            clip.stop();
            return;
        }
        clip.close();
    }
}
