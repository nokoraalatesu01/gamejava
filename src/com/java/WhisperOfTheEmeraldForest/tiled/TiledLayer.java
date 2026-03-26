package com.java.WhisperOfTheEmeraldForest.tiled;

public class TiledLayer {
    private final String name;
    private final int width;
    private final int height;
    private final int[] gids;

    public TiledLayer(String name, int width, int height, int[] gids) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.gids = gids;
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getGid(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return 0;
        }
        return gids[y * width + x];
    }
}
