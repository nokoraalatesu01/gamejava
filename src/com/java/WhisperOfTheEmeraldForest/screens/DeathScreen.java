package com.java.WhisperOfTheEmeraldForest.screens;

import com.java.WhisperOfTheEmeraldForest.Core;
import com.java.WhisperOfTheEmeraldForest.input.InputState;
import com.java.WhisperOfTheEmeraldForest.util.Assets;
import com.java.WhisperOfTheEmeraldForest.util.LoopingSound;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class DeathScreen implements Screen {
    private static final String DEATH_MUSIC_PATH = "Sound/SFX/[Instrumental] A Knight's Lament - Dismiazs.wav";

    private enum DeathOption {
        RETRY,
        MENU
    }

    private final Core game;
    private final int retryLevel;
    private final String deathReason;

    private final BufferedImage backgroundTexture;
    private final Font font;
    private final Rectangle2D.Float retryButtonBounds;
    private final Rectangle2D.Float startButtonBounds;
    private final LoopingSound music = new LoopingSound();
    private DeathOption selectedOption = DeathOption.RETRY;

    public DeathScreen(Core game, int retryLevel, String deathReason) {
        this.game = game;
        this.retryLevel = retryLevel;
        this.deathReason = deathReason;
        if (deathReason != null && deathReason.toLowerCase().contains("duoi nuoc")) {
            this.backgroundTexture = Assets.load("water death.png");
        } else {
            this.backgroundTexture = Assets.load("beat_death.png");
        }
        this.font = new Font("Serif", Font.BOLD, 20);
        this.retryButtonBounds = new Rectangle2D.Float();
        this.startButtonBounds = new Rectangle2D.Float();
    }

    @Override
    public void onShow() {
        music.play(DEATH_MUSIC_PATH);
    }

    @Override
    public void update(float delta) {
        InputState input = game.getInput();

        updateButtonBounds();

        if (input.isKeyJustPressed(KeyEvent.VK_A)) {
            selectedOption = DeathOption.RETRY;
            return;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_D)) {
            selectedOption = DeathOption.MENU;
            return;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            activateSelected();
            return;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            game.setScreen(new StartScreen(game));
        }
    }

    @Override
    public void render(java.awt.Graphics2D g) {
        g.drawImage(backgroundTexture, 0, 0, Core.VIRTUAL_WIDTH, Core.VIRTUAL_HEIGHT, null);

        float centerX = Core.VIRTUAL_WIDTH / 2f;
        float centerY = Core.VIRTUAL_HEIGHT / 2f;

        g.setFont(font.deriveFont(Font.BOLD, 36f));
        g.setColor(new Color(255, 115, 115));
        String title = "You Died";
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(title, (int) (centerX - metrics.stringWidth(title) / 2f), toScreenYText(centerY + 120f));

        g.setFont(font.deriveFont(Font.PLAIN, 22f));
        g.setColor(new Color(255, 224, 224));
        String reason = deathReason == null ? "" : deathReason;
        metrics = g.getFontMetrics();
        g.drawString(reason, (int) (centerX - metrics.stringWidth(reason) / 2f), toScreenYText(centerY + 55f));

        g.setFont(font.deriveFont(Font.PLAIN, 24f));
        String retryText = "Retry";
        String startText = "M.Menu";
        drawButton(g, retryButtonBounds, retryText, selectedOption == DeathOption.RETRY);
        drawButton(g, startButtonBounds, startText, selectedOption == DeathOption.MENU);
    }

    private void activateSelected() {
        if (selectedOption == DeathOption.RETRY) {
            game.startLevel(retryLevel);
        } else {
            game.setScreen(new StartScreen(game));
        }
    }

    private void updateButtonBounds() {
        float buttonWidth = 170f;
        float buttonHeight = 44f;
        float padding = 30f;
        float y = Core.VIRTUAL_HEIGHT - buttonHeight - 30f;
        retryButtonBounds.setRect(padding, y, buttonWidth, buttonHeight);
        startButtonBounds.setRect(Core.VIRTUAL_WIDTH - padding - buttonWidth, y, buttonWidth, buttonHeight);
    }

    private void drawButton(java.awt.Graphics2D g, Rectangle2D.Float bounds, String label, boolean selected) {
        g.setColor(selected ? new Color(70, 110, 70, 235) : new Color(30, 30, 30, 220));
        g.fill(bounds);
        g.setColor(selected ? new Color(230, 255, 220) : new Color(255, 255, 255, 200));
        g.draw(bounds);

        g.setFont(font.deriveFont(Font.PLAIN, 24f));
        FontMetrics metrics = g.getFontMetrics();
        int textX = Math.round(bounds.x + bounds.width / 2f - metrics.stringWidth(label) / 2f);
        int textY = Math.round(bounds.y + bounds.height / 2f + metrics.getAscent() / 2f - 4f);
        g.drawString(label, textX, textY);
    }

    private int toScreenYText(float y) {
        return Math.round(Core.VIRTUAL_HEIGHT - y);
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void dispose() {
        music.stop();
    }
}
