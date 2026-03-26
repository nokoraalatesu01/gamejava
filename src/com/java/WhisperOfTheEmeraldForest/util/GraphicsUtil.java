package com.java.WhisperOfTheEmeraldForest.util;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public final class GraphicsUtil {
    private GraphicsUtil() {}

    public static void drawImage(Graphics2D g, BufferedImage image, float x, float y, float width, float height, boolean flipX, boolean flipY) {
        if (image == null) {
            return;
        }
        AffineTransform transform = new AffineTransform();
        float drawX = x;
        float drawY = y;
        float scaleX = width / image.getWidth();
        float scaleY = height / image.getHeight();

        if (flipX) {
            scaleX = -scaleX;
            drawX = x + width;
        }
        if (flipY) {
            scaleY = -scaleY;
            drawY = y + height;
        }

        transform.translate(drawX, drawY);
        transform.scale(scaleX, scaleY);
        g.drawImage(image, transform, null);
    }

    public static void drawImageRegion(Graphics2D g, BufferedImage image, int srcX, int srcY, int srcW, int srcH,
                                       float x, float y, float width, float height, boolean flipX, boolean flipY) {
        if (image == null) {
            return;
        }
        BufferedImage sub = image.getSubimage(srcX, srcY, srcW, srcH);
        drawImage(g, sub, x, y, width, height, flipX, flipY);
    }
}
