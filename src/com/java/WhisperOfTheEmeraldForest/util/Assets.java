package com.java.WhisperOfTheEmeraldForest.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public final class Assets {
    private static final Map<String, BufferedImage> CACHE = new HashMap<>();
    private static final Path ROOT = Paths.get(System.getProperty("user.dir"), "assets");

    private Assets() {}

    public static BufferedImage load(String relativePath) {
        return CACHE.computeIfAbsent(relativePath, Assets::loadInternal);
    }

    private static BufferedImage loadInternal(String relativePath) {
        Path path = ROOT.resolve(relativePath).normalize();
        if (!Files.exists(path)) {
            throw new IllegalStateException("Missing asset: " + path);
        }
        try {
            return ImageIO.read(path.toFile());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load asset: " + path, e);
        }
    }
}
