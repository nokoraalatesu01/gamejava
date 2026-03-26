package com.java.WhisperOfTheEmeraldForest.util;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public final class TextUtil {
    private TextUtil() {}

    public static List<String> wrapText(FontMetrics metrics, String text, float maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }
        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (current.length() == 0) {
                current.append(word);
                continue;
            }
            String candidate = current + " " + word;
            if (metrics.stringWidth(candidate) <= maxWidth) {
                current.append(" ").append(word);
            } else {
                lines.add(current.toString());
                current = new StringBuilder(word);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    public static void drawWrapped(Graphics2D g, String text, float x, float y, float maxWidth, float lineHeight) {
        FontMetrics metrics = g.getFontMetrics();
        List<String> lines = wrapText(metrics, text, maxWidth);
        float drawY = y;
        for (String line : lines) {
            g.drawString(line, x, drawY);
            drawY += lineHeight;
        }
    }
}
