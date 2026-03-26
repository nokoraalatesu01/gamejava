package com.java.WhisperOfTheEmeraldForest.screens;

import com.java.WhisperOfTheEmeraldForest.Core;
import com.java.WhisperOfTheEmeraldForest.entities.Player;
import com.java.WhisperOfTheEmeraldForest.input.InputState;
import com.java.WhisperOfTheEmeraldForest.util.Camera2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;

public class PauseOverlay {
    private static final int OPTION_CONTINUE = 0;
    private static final int OPTION_RESTART = 1;
    private static final int OPTION_HOME = 2;

    private final Core game;
    private final int level;
    private final boolean allowHome;
    private final Font titleFont;
    private final Font optionFont;

    private final Rectangle2D.Float dialogBounds;
    private final Rectangle2D.Float continueBounds;
    private final Rectangle2D.Float restartBounds;
    private final Rectangle2D.Float homeBounds;

    private boolean paused;
    private int selectedIndex;
    public PauseOverlay(Core game, int level) {
        this(game, level, true);
    }

    public PauseOverlay(Core game, int level, boolean allowHome) {
        this.game = game;
        this.level = level;
        this.allowHome = allowHome;
        this.titleFont = new Font("Serif", Font.BOLD, 36);
        this.optionFont = new Font("Serif", Font.PLAIN, 22);
        this.dialogBounds = new Rectangle2D.Float();
        this.continueBounds = new Rectangle2D.Float();
        this.restartBounds = new Rectangle2D.Float();
        this.homeBounds = new Rectangle2D.Float();
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean update(Camera2D camera, Player player, InputState input) {
        if (camera == null || player == null || player.isDead()) {
            paused = false;
            return false;
        }

        updateBounds(camera);

        if (!paused) {
            if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
                paused = true;
                selectedIndex = OPTION_CONTINUE;
            }
            return false;
        }

        if (input.isKeyJustPressed(KeyEvent.VK_UP) || input.isKeyJustPressed(KeyEvent.VK_W)) {
            selectedIndex = (selectedIndex + (allowHome ? 2 : 1)) % (allowHome ? 3 : 2);
        }
        if (input.isKeyJustPressed(KeyEvent.VK_DOWN) || input.isKeyJustPressed(KeyEvent.VK_S)) {
            selectedIndex = (selectedIndex + 1) % (allowHome ? 3 : 2);
        }
        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            paused = false;
            return false;
        }

        if (input.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            return activateSelected();
        }

        return false;
    }

    private boolean activateSelected() {
        paused = false;
        if (selectedIndex == OPTION_CONTINUE) {
            return false;
        }
        if (selectedIndex == OPTION_RESTART) {
            game.startLevel(level);
        } else if (allowHome) {
            game.setScreen(new StartScreen(game));
        }
        return true;
    }

    private void updateBounds(Camera2D camera) {
        float centerX = camera.x;
        float centerY = camera.y;

        dialogBounds.setRect(centerX - 185f, centerY - 120f, 370f, 240f);
        continueBounds.setRect(centerX - 120f, centerY + 10f, 240f, 40f);
        restartBounds.setRect(centerX - 120f, centerY - 40f, 240f, 40f);
        homeBounds.setRect(centerX - 120f, centerY - 90f, 240f, 40f);
    }

    public void draw(Camera2D camera, java.awt.Graphics2D g) {
        updateBounds(camera);

        g.setColor(new Color(0, 0, 0, 204));
        g.fill(new Rectangle2D.Float(toScreenX(camera, dialogBounds.x), toScreenY(camera, dialogBounds.y, dialogBounds.height),
            dialogBounds.width, dialogBounds.height));
        g.setColor(getButtonFill(OPTION_CONTINUE));
        g.fill(new Rectangle2D.Float(toScreenX(camera, continueBounds.x), toScreenY(camera, continueBounds.y, continueBounds.height),
            continueBounds.width, continueBounds.height));
        g.setColor(getButtonFill(OPTION_RESTART));
        g.fill(new Rectangle2D.Float(toScreenX(camera, restartBounds.x), toScreenY(camera, restartBounds.y, restartBounds.height),
            restartBounds.width, restartBounds.height));
        if (allowHome) {
            g.setColor(getButtonFill(OPTION_HOME));
            g.fill(new Rectangle2D.Float(toScreenX(camera, homeBounds.x), toScreenY(camera, homeBounds.y, homeBounds.height),
                homeBounds.width, homeBounds.height));
        }

        g.setColor(Color.BLACK);
        g.draw(new Rectangle2D.Float(toScreenX(camera, dialogBounds.x), toScreenY(camera, dialogBounds.y, dialogBounds.height),
            dialogBounds.width, dialogBounds.height));
        g.draw(new Rectangle2D.Float(toScreenX(camera, continueBounds.x), toScreenY(camera, continueBounds.y, continueBounds.height),
            continueBounds.width, continueBounds.height));
        g.draw(new Rectangle2D.Float(toScreenX(camera, restartBounds.x), toScreenY(camera, restartBounds.y, restartBounds.height),
            restartBounds.width, restartBounds.height));
        if (allowHome) {
            g.draw(new Rectangle2D.Float(toScreenX(camera, homeBounds.x), toScreenY(camera, homeBounds.y, homeBounds.height),
                homeBounds.width, homeBounds.height));
        }

        g.setFont(titleFont);
        g.setColor(Color.WHITE);
        FontMetrics metrics = g.getFontMetrics();
        String title = "Pause Game";
        g.drawString(title,
            (int) (toScreenX(camera, dialogBounds.x) + dialogBounds.width / 2f - metrics.stringWidth(title) / 2f),
            toScreenYText(camera, dialogBounds.y + dialogBounds.height - 35f));

        drawOption(g, camera, continueBounds, "Continue", selectedIndex == OPTION_CONTINUE);
        drawOption(g, camera, restartBounds, "Restart", selectedIndex == OPTION_RESTART);
        if (allowHome) {
            drawOption(g, camera, homeBounds, "Home", selectedIndex == OPTION_HOME);
        }
    }

    private Color getButtonFill(int index) {
        boolean active = selectedIndex == index;
        return active ? new Color(38, 38, 38) : new Color(13, 13, 13, 245);
    }

    private void drawOption(java.awt.Graphics2D g, Camera2D camera, Rectangle2D.Float bounds, String text, boolean active) {
        g.setFont(optionFont);
        g.setColor(active ? new Color(255, 242, 173) : Color.WHITE);
        FontMetrics metrics = g.getFontMetrics();
        float textX = toScreenX(camera, bounds.x) + bounds.width / 2f - metrics.stringWidth(text) / 2f;
        float textY = toScreenYText(camera, bounds.y + bounds.height / 2f + metrics.getAscent() / 2f - 20f);
        g.drawString(text, Math.round(textX), Math.round(textY));
    }

    private float toScreenX(Camera2D camera, float worldX) {
        return worldX - camera.x + camera.viewportWidth / 2f;
    }

    private float toScreenY(Camera2D camera, float worldY, float height) {
        return camera.viewportHeight / 2f + camera.y - worldY - height;
    }

    private int toScreenYText(Camera2D camera, float worldY) {
        return Math.round(camera.viewportHeight / 2f + camera.y - worldY);
    }
}
