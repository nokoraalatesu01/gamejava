package com.java.WhisperOfTheEmeraldForest.screens;

import com.java.WhisperOfTheEmeraldForest.input.InputState;
import com.java.WhisperOfTheEmeraldForest.util.Camera2D;
import com.java.WhisperOfTheEmeraldForest.util.TextUtil;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class MessageOverlay {
    private final Font titleFont = new Font("Serif", Font.BOLD, 30);
    private final Font promptFont = new Font("Serif", Font.PLAIN, 20);
    private final String title;
    private final String prompt;
    private final Rectangle2D.Float dialogBounds = new Rectangle2D.Float();
    private boolean visible;

    public MessageOverlay(String title, String prompt) {
        this.title = title;
        this.prompt = prompt;
    }

    public boolean isVisible() {
        return visible;
    }

    public void show() {
        visible = true;
    }

    public void hide() {
        visible = false;
    }

    public void update(InputState input) {
        if (!visible) {
            return;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            hide();
        }
    }

    public void draw(Camera2D camera, java.awt.Graphics2D g) {
        if (!visible) {
            return;
        }

        updateBounds(camera);

        float boxX = toScreenX(camera, dialogBounds.x);
        float boxY = toScreenY(camera, dialogBounds.y, dialogBounds.height);

        g.setColor(new Color(0, 0, 0, 204));
        g.fill(new Rectangle2D.Float(boxX, boxY, dialogBounds.width, dialogBounds.height));

        g.setColor(new Color(235, 228, 198));
        g.draw(new Rectangle2D.Float(boxX, boxY, dialogBounds.width, dialogBounds.height));

        g.setFont(titleFont);
        g.setColor(Color.WHITE);
        FontMetrics titleMetrics = g.getFontMetrics();
        List<String> titleLines = TextUtil.wrapText(titleMetrics, title, dialogBounds.width - 40f);
        float titleY = boxY + 52f;
        for (String line : titleLines) {
            float titleX = boxX + dialogBounds.width / 2f - titleMetrics.stringWidth(line) / 2f;
            g.drawString(line, Math.round(titleX), Math.round(titleY));
            titleY += titleMetrics.getHeight();
        }

        g.setFont(promptFont);
        FontMetrics promptMetrics = g.getFontMetrics();
        List<String> promptLines = TextUtil.wrapText(promptMetrics, prompt, dialogBounds.width - 40f);
        float promptY = boxY + dialogBounds.height - 44f;
        for (int i = promptLines.size() - 1; i >= 0; i--) {
            String line = promptLines.get(i);
            float promptX = boxX + dialogBounds.width / 2f - promptMetrics.stringWidth(line) / 2f;
            g.drawString(line, Math.round(promptX), Math.round(promptY));
            promptY -= promptMetrics.getHeight();
        }
    }

    private void updateBounds(Camera2D camera) {
        float width = 520f;
        float height = 180f;
        dialogBounds.setRect(camera.x - width / 2f, camera.y - height / 2f, width, height);
    }

    private float toScreenX(Camera2D camera, float worldX) {
        return worldX - camera.x + camera.viewportWidth / 2f;
    }

    private float toScreenY(Camera2D camera, float worldY, float height) {
        return camera.viewportHeight / 2f + camera.y - worldY - height;
    }
}
