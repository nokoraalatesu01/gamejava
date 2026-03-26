package com.java.WhisperOfTheEmeraldForest.screens;

import com.java.WhisperOfTheEmeraldForest.Core;
import com.java.WhisperOfTheEmeraldForest.input.InputState;
import com.java.WhisperOfTheEmeraldForest.util.Animation;
import com.java.WhisperOfTheEmeraldForest.util.Assets;
import com.java.WhisperOfTheEmeraldForest.util.GraphicsUtil;
import com.java.WhisperOfTheEmeraldForest.util.LoopingSound;
import com.java.WhisperOfTheEmeraldForest.util.TextUtil;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

public class StartScreen implements Screen {
    private enum ViewMode {
        HOME,
        SUBPAGE
    }

    private enum MenuOption {
        PLAY,
        RESOLUTION,
        INTRO,
        GUIDE,
        CREDITS,
        EXIT
    }

    private enum ResolutionOption {
        SMALL,
        MEDIUM,
        LARGE,
        FULL_SCREEN
    }

    private static final float WORLD_WIDTH = Core.VIRTUAL_WIDTH;
    private static final float WORLD_HEIGHT = Core.VIRTUAL_HEIGHT;
    private static final String FONT_PATH = "C:/Windows/Fonts/segoeui.ttf";
    private static final String KNIGHT_BASE = "ASSETSGame/FreeKnight_v1/Colour2/NoOutline/120x80_PNGSheets/";
    private static final String START_MUSIC_PATH = "Sound/SFX/Kingdom Dance AMV Anime MIX - EzeAMVs.wav";

    private final Core game;
    private final MenuOption[] menuOptions = MenuOption.values();
    private final ResolutionOption[] resolutionOptions = ResolutionOption.values();
    private final LoopingSound music = new LoopingSound();

    private Font titleFont;
    private Font menuFont;
    private Font bodyFont;
    private Font smallFont;

    private ViewMode viewMode = ViewMode.HOME;
    private int keyboardFocusIndex = MenuOption.INTRO.ordinal();
    private MenuOption currentPanel = MenuOption.INTRO;
    private float previewStateTime;
    private int resolutionFocusIndex;

    private BufferedImage backgroundTexture;
    private BufferedImage idleSheet;
    private BufferedImage runSheet;
    private BufferedImage jumpSheet;
    private BufferedImage attackSheet;
    private Animation idlePreview;
    private Animation runPreview;
    private Animation jumpPreview;
    private Animation attackPreview;

    private final Rectangle2D.Float[] buttonBounds;
    private final Rectangle2D.Float[] resolutionBounds;
    private final Rectangle2D.Float backButtonBounds;

    public StartScreen(Core game) {
        this.game = game;
        this.buttonBounds = new Rectangle2D.Float[menuOptions.length];
        this.resolutionBounds = new Rectangle2D.Float[resolutionOptions.length];
        this.backButtonBounds = new Rectangle2D.Float();
        for (int i = 0; i < buttonBounds.length; i++) {
            buttonBounds[i] = new Rectangle2D.Float();
        }
        for (int i = 0; i < resolutionBounds.length; i++) {
            resolutionBounds[i] = new Rectangle2D.Float();
        }

        createFonts();
        backgroundTexture = Assets.load("start.png");
        loadGuideAnimations();
    }

    @Override
    public void onShow() {
        updateButtonBounds();
        music.play(START_MUSIC_PATH);
    }

    private void createFonts() {
        Font base = null;
        try {
            File fontFile = new File(FONT_PATH);
            if (fontFile.exists()) {
                base = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            }
        } catch (Exception ignored) {
            base = null;
        }
        if (base == null) {
            base = new Font("Serif", Font.PLAIN, 12);
        }
        titleFont = base.deriveFont(Font.BOLD, 44f);
        menuFont = base.deriveFont(Font.BOLD, 22f);
        bodyFont = base.deriveFont(Font.PLAIN, 21f);
        smallFont = base.deriveFont(Font.PLAIN, 16f);
    }

    private void loadGuideAnimations() {
        idleSheet = Assets.load(KNIGHT_BASE + "_Idle.png");
        runSheet = Assets.load(KNIGHT_BASE + "_Run.png");
        jumpSheet = Assets.load(KNIGHT_BASE + "_Jump.png");
        attackSheet = Assets.load(KNIGHT_BASE + "_Attack.png");

        idlePreview = createAnimation(idleSheet, 10, 0.12f);
        runPreview = createAnimation(runSheet, 10, 0.08f);
        jumpPreview = createAnimation(jumpSheet, 3, 0.12f);
        attackPreview = createFixedWidthAnimation(attackSheet, 120, 80, 0.08f);
    }

    private Animation createAnimation(BufferedImage sheet, int frameCount, float frameDuration) {
        int frameWidth = sheet.getWidth() / frameCount;
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, sheet.getHeight());
        }
        return new Animation(frameDuration, frames);
    }

    private Animation createFixedWidthAnimation(BufferedImage sheet, int frameWidth, int frameHeight, float frameDuration) {
        int frameCount = Math.max(1, sheet.getWidth() / frameWidth);
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
        }
        return new Animation(frameDuration, frames);
    }

    private void activate(MenuOption option) {
        switch (option) {
            case PLAY:
                game.startGame();
                break;
            case EXIT:
                System.exit(0);
                break;
            default:
                viewMode = ViewMode.SUBPAGE;
                currentPanel = option;
                keyboardFocusIndex = option.ordinal();
                if (option == MenuOption.RESOLUTION) {
                    resolutionFocusIndex = 0;
                }
                break;
        }
    }

    private void returnToHome() {
        viewMode = ViewMode.HOME;
        keyboardFocusIndex = currentPanel.ordinal();
    }

    private void updateInput() {
        InputState input = game.getInput();

        if (viewMode == ViewMode.SUBPAGE) {
            if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE) || input.isKeyJustPressed(KeyEvent.VK_BACK_SPACE)) {
                returnToHome();
                return;
            }
            if (currentPanel == MenuOption.RESOLUTION) {
                updateResolutionInput(input);
                return;
            }
            if (input.isKeyJustPressed(KeyEvent.VK_ENTER)) {
                returnToHome();
            }
            return;
        }

        if (input.isKeyJustPressed(KeyEvent.VK_UP) || input.isKeyJustPressed(KeyEvent.VK_W)) {
            keyboardFocusIndex = (keyboardFocusIndex - 1 + menuOptions.length) % menuOptions.length;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_DOWN) || input.isKeyJustPressed(KeyEvent.VK_S)) {
            keyboardFocusIndex = (keyboardFocusIndex + 1) % menuOptions.length;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            activate(menuOptions[keyboardFocusIndex]);
            return;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            System.exit(0);
            return;
        }
    }

    private void updateButtonBounds() {
        float startX = 70f;
        float startY = 320f;
        float buttonWidth = 140f;
        float buttonHeight = 30f;
        float gap = 10f;

        for (int i = 0; i < menuOptions.length; i++) {
            float y = startY - i * (buttonHeight + gap);
            buttonBounds[i].setRect(startX, y, buttonWidth, buttonHeight);
        }

        backButtonBounds.setRect(595f, 62f, 110f, 42f);

        float resStartX = 240f;
        float resStartY = 300f;
        float resWidth = 320f;
        float resHeight = 32f;
        float resGap = 10f;
        for (int i = 0; i < resolutionBounds.length; i++) {
            float y = resStartY - i * (resHeight + resGap);
            resolutionBounds[i].setRect(resStartX, y, resWidth, resHeight);
        }
    }

    @Override
    public void update(float delta) {
        previewStateTime += delta;
        updateButtonBounds();
        updateInput();
    }

    @Override
    public void render(java.awt.Graphics2D g) {
        g.drawImage(backgroundTexture, 0, 0, (int) WORLD_WIDTH, (int) WORLD_HEIGHT, null);
        drawMenuShapes(g);
        drawMenuLabels(g);
        drawPanelContent(g);
        drawBackButton(g);
    }

    private void drawMenuLabels(java.awt.Graphics2D g) {
        if (viewMode != ViewMode.HOME) {
            return;
        }

        for (int i = 0; i < menuOptions.length; i++) {
            Rectangle2D.Float bounds = buttonBounds[i];
            boolean focused = i == keyboardFocusIndex;
            g.setColor(focused ? new Color(255, 242, 173) : Color.WHITE);

            String text = menuOptions[i].name();
            Font fitted = fitFontToWidth(g, menuFont, text, bounds.width - 10f, 14f);
            g.setFont(fitted);
            FontMetrics metrics = g.getFontMetrics();
            int textWidth = metrics.stringWidth(text);
            int textX = Math.round(bounds.x + bounds.width / 2f - textWidth / 2f);
            int textY = toScreenYText(bounds.y + bounds.height / 2f + metrics.getAscent() / 2f + 2f -22f);
            g.drawString(text, textX, textY);
        }

        g.setFont(smallFont);
        g.setColor(new Color(235, 235, 235, 224));
        g.drawString("ENTER to select   |   ESC to quit", 55, toScreenYText(28));
    }

    private Font fitFontToWidth(java.awt.Graphics2D g, Font base, String text, float maxWidth, float minSize) {
        g.setFont(base);
        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        if (textWidth <= maxWidth) {
            return base;
        }
        float scale = maxWidth / Math.max(1f, textWidth);
        float newSize = Math.max(minSize, base.getSize2D() * scale);
        return base.deriveFont(base.getStyle(), newSize);
    }

    private void drawMenuShapes(java.awt.Graphics2D g) {
        if (viewMode == ViewMode.HOME) {
            for (int i = 0; i < buttonBounds.length; i++) {
                Rectangle2D.Float bounds = buttonBounds[i];
                if (i == keyboardFocusIndex) {
                    g.setColor(new Color(255, 255, 255, 26));
                    g.fill(new Rectangle2D.Float(Math.round(bounds.x), toScreenY(bounds.y, bounds.height), Math.round(bounds.width), Math.round(bounds.height)));
                }
            }
            g.setColor(new Color(255, 255, 255, 230));
            for (Rectangle2D.Float bounds : buttonBounds) {
                g.draw(new Rectangle2D.Float(Math.round(bounds.x), toScreenY(bounds.y, bounds.height), Math.round(bounds.width), Math.round(bounds.height)));
            }
            return;
        }

        g.setColor(new Color(0, 0, 0, 168));
        g.fill(new Rectangle2D.Float(85, toScreenY(40, 370), 640, 370));
        g.setColor(new Color(20, 20, 20, 235));
        g.fill(new Rectangle2D.Float(Math.round(backButtonBounds.x), toScreenY(backButtonBounds.y, backButtonBounds.height), Math.round(backButtonBounds.width), Math.round(backButtonBounds.height)));

        g.setColor(new Color(255, 255, 255, 90));
        g.draw(new Rectangle2D.Float(85, toScreenY(40, 370), 640, 370));
        g.setColor(new Color(235, 199, 97));
        g.draw(new Rectangle2D.Float(Math.round(backButtonBounds.x), toScreenY(backButtonBounds.y, backButtonBounds.height), Math.round(backButtonBounds.width), Math.round(backButtonBounds.height)));
    }

    private void drawPanelContent(java.awt.Graphics2D g) {
        if (viewMode != ViewMode.SUBPAGE) {
            return;
        }

        switch (currentPanel) {
            case RESOLUTION:
                drawResolution(g);
                break;
            case GUIDE:
                drawGuide(g);
                break;
            case CREDITS:
                drawCredits(g);
                break;
            case INTRO:
            default:
                drawIntro(g);
                break;
        }
    }

    private void drawSectionTitle(java.awt.Graphics2D g, String title) {
        g.setFont(bodyFont);
        g.setColor(new Color(255, 242, 184));
        g.drawString(title, 120, toScreenYText(380));
    }

    private void drawIntro(java.awt.Graphics2D g) {
        drawSectionTitle(g, "INTRO");
        g.setFont(bodyFont);
        g.setColor(Color.WHITE);
        TextUtil.drawWrapped(g,
            "Once upon a time there was a forest called Emerald Forest. The forest was protected by a crystal called the Forest Core.",
            120, toScreenYText(335), 560, 24);
        TextUtil.drawWrapped(g, "One day, the crystal was stolen by some unknown force.", 120, toScreenYText(235), 560, 24);
        TextUtil.drawWrapped(g, "Your mission is to find the crystal, answering the call of the forest. and restore peace to the forest.",
            120, toScreenYText(205), 560, 24);
        g.setFont(smallFont);
        g.setColor(new Color(217, 230, 230, 230));
        g.drawString("GOOD LUCK !!!!", 120, toScreenYText(120));
    }

    private void drawResolution(java.awt.Graphics2D g) {
        drawSectionTitle(g, "RESOLUTION");

        g.setFont(bodyFont);
        FontMetrics metrics = g.getFontMetrics();
        for (int i = 0; i < resolutionOptions.length; i++) {
            Rectangle2D.Float bounds = resolutionBounds[i];
            boolean focused = i == resolutionFocusIndex;
            if (focused) {
                g.setColor(new Color(255, 255, 255, 26));
                g.fill(new Rectangle2D.Float(Math.round(bounds.x), toScreenY(bounds.y, bounds.height), Math.round(bounds.width), Math.round(bounds.height)));
            }
            g.setColor(new Color(255, 255, 255, 200));
            g.draw(new Rectangle2D.Float(Math.round(bounds.x), toScreenY(bounds.y, bounds.height), Math.round(bounds.width), Math.round(bounds.height)));

            String label = getResolutionLabel(resolutionOptions[i]);
            int textX = Math.round(bounds.x + bounds.width / 2f - metrics.stringWidth(label) / 2f);
            int textY = toScreenYText(bounds.y + bounds.height / 2f + metrics.getAscent() / 2f - 20f);
            g.setColor(focused ? new Color(255, 242, 173) : Color.WHITE);
            g.drawString(label, textX, textY);
        }

        g.setFont(smallFont);
        g.setColor(new Color(235, 235, 235, 224));
        g.drawString("ENTER to apply   |   ESC to back", 120, toScreenYText(110));
    }

    private void drawGuide(java.awt.Graphics2D g) {
        drawSectionTitle(g, "GUIDE");
        drawGuideRow(g, attackPreview.getFrame(previewStateTime, true), "ATTACK", "Press ENTER to attack.", 310f);
        drawGuideRow(g, runPreview.getFrame(previewStateTime, true), "MOVE", "Press A and D to move left and right.", 230f);
        drawGuideRow(g, jumpPreview.getFrame(previewStateTime, false), "JUMP", "Press SPACE to jump.", 150f);
    }

    private void drawGuideRow(java.awt.Graphics2D g, BufferedImage frame, String title, String description, float y) {
        float iconX = 285f;
        float iconY = y - 18f;
        float scale = 0.9f;

        GraphicsUtil.drawImage(g, frame, iconX, toScreenY(iconY, frame.getHeight() * scale), frame.getWidth() * scale, frame.getHeight() * scale, false, false);

        g.setFont(bodyFont);
        g.setColor(new Color(255, 242, 184));
        g.drawString(title, 400, toScreenYText(y + 22f));
        g.setFont(smallFont);
        g.setColor(Color.WHITE);
        TextUtil.drawWrapped(g, description, 400, toScreenYText(y - 2f), 300, 20);
    }

    private void drawCredits(java.awt.Graphics2D g) {
        drawSectionTitle(g, "CREDITS");

        g.setFont(bodyFont);
        g.setColor(Color.WHITE);
        g.drawString("Founders", 120, toScreenYText(338));
        g.setFont(smallFont);
        g.setColor(new Color(242, 242, 242, 242));
        g.drawString("Nguyen Duc Truong - 241230872", 120, toScreenYText(305));
        g.drawString("XXXXX XXXXX XXXXX", 120, toScreenYText(280));
        g.drawString("XXXXX XXXXX XXXXX", 120, toScreenYText(255));

        g.setFont(bodyFont);
        g.setColor(new Color(255, 242, 184));
        String text1 = "Truong Dai Hoc Giao Thong Van Tai";
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text1, (int) (WORLD_WIDTH / 2f - metrics.stringWidth(text1) / 2f), toScreenYText(175));

        g.setColor(Color.WHITE);
        String text2 = "The Nokora's Team";
        g.drawString(text2, (int) (WORLD_WIDTH / 2f - metrics.stringWidth(text2) / 2f), toScreenYText(100));
    }

    private void drawBackButton(java.awt.Graphics2D g) {
        if (viewMode != ViewMode.SUBPAGE) {
            return;
        }

        g.setFont(menuFont);
        g.setColor(new Color(255, 242, 173));
        FontMetrics metrics = g.getFontMetrics();
        String text = "BACK";
        int textX = Math.round(backButtonBounds.x + backButtonBounds.width / 2f - metrics.stringWidth(text) / 2f);
        int textY = toScreenYText(backButtonBounds.y + backButtonBounds.height / 2f + metrics.getAscent() / 2f - 22f);
        g.drawString(text, textX, textY);
    }

    private void updateResolutionInput(InputState input) {
        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE) || input.isKeyJustPressed(KeyEvent.VK_BACK_SPACE)) {
            returnToHome();
            return;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_UP) || input.isKeyJustPressed(KeyEvent.VK_W)) {
            resolutionFocusIndex = (resolutionFocusIndex - 1 + resolutionOptions.length) % resolutionOptions.length;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_DOWN) || input.isKeyJustPressed(KeyEvent.VK_S)) {
            resolutionFocusIndex = (resolutionFocusIndex + 1) % resolutionOptions.length;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            applyResolution(resolutionOptions[resolutionFocusIndex]);
            return;
        }
    }

    private void applyResolution(ResolutionOption option) {
        switch (option) {
            case SMALL:
                game.setResolutionSmall();
                break;
            case MEDIUM:
                game.setResolutionMedium();
                break;
            case LARGE:
                game.setResolutionLarge();
                break;
            case FULL_SCREEN:
                game.setResolutionFullscreen();
                break;
            default:
                break;
        }
    }

    private String getResolutionLabel(ResolutionOption option) {
        switch (option) {
            case SMALL:
                return "SMALL (800x480)";
            case MEDIUM:
                return "MEDIUM (1200x720)";
            case LARGE:
                return "LARGE (1600x960)";
            case FULL_SCREEN:
                return "FULL SCREEN";
            default:
                return option.name();
        }
    }

    private int toScreenY(float y, float height) {
        return Math.round(WORLD_HEIGHT - y - height);
    }

    private int toScreenYText(float y) {
        return Math.round(WORLD_HEIGHT - y);
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void dispose() {
        music.stop();
    }
}
