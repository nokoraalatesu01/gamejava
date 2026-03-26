package com.java.WhisperOfTheEmeraldForest.tiled;

import java.util.ArrayList;
import java.util.List;

public class TiledMap {
    private final int width;
    private final int height;
    private final int tileWidth;
    private final int tileHeight;
    private final List<TiledLayer> layers;
    private final List<Tileset> tilesets;

    public TiledMap(int width, int height, int tileWidth, int tileHeight, List<TiledLayer> layers, List<Tileset> tilesets) {
        this.width = width;
        this.height = height;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.layers = new ArrayList<>(layers);
        this.tilesets = new ArrayList<>(tilesets);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public List<TiledLayer> getLayers() {
        return layers;
    }

    public TiledLayer getLayer(String name) {
        for (TiledLayer layer : layers) {
            if (layer.getName() != null && layer.getName().equalsIgnoreCase(name)) {
                return layer;
            }
        }
        return null;
    }

    public Tileset getTilesetForGid(int gid) {
        Tileset match = null;
        for (Tileset tileset : tilesets) {
            if (gid >= tileset.firstGid && gid <= tileset.getLastGid()) {
                if (match == null || tileset.firstGid > match.firstGid) {
                    match = tileset;
                }
            }
        }
        return match;
    }
}
