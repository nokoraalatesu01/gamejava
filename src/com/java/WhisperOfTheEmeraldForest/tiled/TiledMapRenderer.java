package com.java.WhisperOfTheEmeraldForest.tiled;

import com.java.WhisperOfTheEmeraldForest.util.Camera2D;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class TiledMapRenderer {
    private static final int FLIP_H = 0x80000000;
    private static final int FLIP_V = 0x40000000;
    private static final int FLIP_D = 0x20000000;

    private final TiledMap map;

    public TiledMapRenderer(TiledMap map) {
        this.map = map;
    }

    public void render(Graphics2D g, Camera2D camera) {
        if (map == null) {
            return;
        }
        int tileWidth = map.getTileWidth();
        int tileHeight = map.getTileHeight();

        float left = camera.x - camera.viewportWidth / 2f;
        float right = camera.x + camera.viewportWidth / 2f;
        float bottom = camera.y - camera.viewportHeight / 2f;
        float top = camera.y + camera.viewportHeight / 2f;

        int startX = Math.max(0, (int) (left / tileWidth) - 1);
        int endX = Math.min(map.getWidth() - 1, (int) (right / tileWidth) + 1);
        int startY = Math.max(0, (int) (bottom / tileHeight) - 1);
        int endY = Math.min(map.getHeight() - 1, (int) (top / tileHeight) + 1);

        for (TiledLayer layer : map.getLayers()) {
            for (int y = startY; y <= endY; y++) {
                for (int x = startX; x <= endX; x++) {
                    int gidWithFlags = layer.getGid(x, y);
                    if (gidWithFlags == 0) {
                        continue;
                    }
                    boolean flipH = (gidWithFlags & FLIP_H) != 0;
                    boolean flipV = (gidWithFlags & FLIP_V) != 0;
                    boolean flipD = (gidWithFlags & FLIP_D) != 0;
                    int gid = gidWithFlags & ~(FLIP_H | FLIP_V | FLIP_D);

                    Tileset tileset = map.getTilesetForGid(gid);
                    if (tileset == null) {
                        continue;
                    }
                    int localId = gid - tileset.firstGid;
                    int srcX = (localId % tileset.columns) * (tileset.tileWidth + tileset.spacing) + tileset.margin;
                    int srcY = (localId / tileset.columns) * (tileset.tileHeight + tileset.spacing) + tileset.margin;

                    float worldX = x * tileWidth;
                    float worldY = y * tileHeight;

                    float screenX = worldX - camera.x + camera.viewportWidth / 2f;
                    float screenY = camera.viewportHeight / 2f + camera.y - worldY - tileHeight;

                    drawTile(g, tileset.image, srcX, srcY, tileset.tileWidth, tileset.tileHeight,
                        screenX, screenY, tileWidth, tileHeight, flipH, flipV, flipD);
                }
            }
        }
    }

    private void drawTile(Graphics2D g, BufferedImage image, int srcX, int srcY, int srcW, int srcH,
                          float x, float y, float width, float height, boolean flipH, boolean flipV, boolean flipD) {
        if (image == null) {
            return;
        }
        BufferedImage sub = image.getSubimage(srcX, srcY, srcW, srcH);

        float destW = width;
        float destH = height;

        float x0 = 0f;
        float y0 = 0f;
        float x1 = destW;
        float y1 = 0f;
        float x2 = 0f;
        float y2 = destH;

        if (flipD) {
            float tx0 = x0;
            x0 = y0;
            y0 = tx0;
            float tx1 = x1;
            x1 = y1;
            y1 = tx1;
            float tx2 = x2;
            x2 = y2;
            y2 = tx2;

            float tmp = destW;
            destW = destH;
            destH = tmp;
        }

        if (flipH) {
            x0 = destW - x0;
            x1 = destW - x1;
            x2 = destW - x2;
        }
        if (flipV) {
            y0 = destH - y0;
            y1 = destH - y1;
            y2 = destH - y2;
        }

        float m00 = (x1 - x0) / width;
        float m10 = (y1 - y0) / width;
        float m01 = (x2 - x0) / height;
        float m11 = (y2 - y0) / height;
        float m02 = x + x0;
        float m12 = y + y0;

        AffineTransform transform = new AffineTransform(m00, m10, m01, m11, m02, m12);
        AffineTransform old = g.getTransform();
        g.setTransform(transform);
        g.drawImage(sub, 0, 0, (int) width, (int) height, null);
        g.setTransform(old);
    }
}
