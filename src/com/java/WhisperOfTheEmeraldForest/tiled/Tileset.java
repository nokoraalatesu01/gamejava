package com.java.WhisperOfTheEmeraldForest.tiled;

import java.awt.image.BufferedImage;

public class Tileset {
    public final int firstGid;
    public final int tileWidth;
    public final int tileHeight;
    public final int columns;
    public final int tileCount;
    public final int margin;
    public final int spacing;
    public final BufferedImage image;

    public Tileset(int firstGid, int tileWidth, int tileHeight, int columns, int tileCount, int margin, int spacing, BufferedImage image) {
        this.firstGid = firstGid;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.columns = columns;
        this.tileCount = tileCount;
        this.margin = margin;
        this.spacing = spacing;
        this.image = image;
    }

    public int getLastGid() {
        return firstGid + tileCount - 1;
    }
}
