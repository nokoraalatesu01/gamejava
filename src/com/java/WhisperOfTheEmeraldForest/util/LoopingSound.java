package com.java.WhisperOfTheEmeraldForest.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class LoopingSound {
    private static final Path ROOT = Paths.get(System.getProperty("user.dir"), "assets");

    private Clip clip;

    public void play(String relativePath) {
        stop();
        Path path = ROOT.resolve(relativePath).normalize();
        if (!Files.exists(path)) {
            throw new IllegalStateException("Missing sound: " + path);
        }
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(path.toFile())) {
            clip = AudioSystem.getClip();
            clip.open(stream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            throw new IllegalStateException("Failed to play sound: " + path, e);
        }
    }

    public void stop() {
        if (clip == null) {
            return;
        }
        clip.stop();
        clip.close();
        clip = null;
    }
}
