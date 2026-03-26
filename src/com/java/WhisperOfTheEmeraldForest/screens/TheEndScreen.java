package com.java.WhisperOfTheEmeraldForest.screens;

import com.java.WhisperOfTheEmeraldForest.Core;
import com.java.WhisperOfTheEmeraldForest.entities.Player;
import com.java.WhisperOfTheEmeraldForest.input.InputState;
import com.java.WhisperOfTheEmeraldForest.tiled.TiledLayer;
import com.java.WhisperOfTheEmeraldForest.tiled.TiledMap;
import com.java.WhisperOfTheEmeraldForest.tiled.TiledMapRenderer;
import com.java.WhisperOfTheEmeraldForest.tiled.TmxMapLoader;
import com.java.WhisperOfTheEmeraldForest.util.Assets;
import com.java.WhisperOfTheEmeraldForest.util.Camera2D;
import com.java.WhisperOfTheEmeraldForest.util.GraphicsUtil;
import com.java.WhisperOfTheEmeraldForest.util.LoopingSound;
import com.java.WhisperOfTheEmeraldForest.util.OneShotSound;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.sound.sampled.Clip;

public class TheEndScreen implements Screen {
    private static final float STEP = 1f / 60f;
    private static final float MAP2_SPAWN_X = 1920f;
    private static final float MAP2_SPAWN_Y = 110f;
    private static final float EMERALD_BASE_WIDTH = 48f;
    private static final String THE_END_MUSIC_PATH = "Sound/BGS Loops/Forest Night/Forest Night.wav";
    private static final String EMERALD_PICKUP_SOUND = "Sound/SFX/tunetank-winner-awards-logo-484334.wav";
    private final Core game;
    private final PauseOverlay pauseOverlay;
    private final LoopingSound music = new LoopingSound();
    private enum EndOption {
        REPLAY,
        MENU
    }

    private Camera2D camera;
    private TiledMap map;
    private TiledMapRenderer mapRenderer;
    private Player player;
    private TiledLayer groundLayer;
    private TiledLayer waterLayer;
    private float worldWidth;
    private float worldHeight;
    private float accumulator = 0f;
    private boolean deathScreenQueued = false;
    private boolean isTeleporting = false;

    private BufferedImage hpBarFrameTexture;
    private BufferedImage[] hpBarFrames;
    private BufferedImage emeraldTexture;
    private Clip emeraldPickupClip;
    private boolean emeraldCollected = false;
    private EndOption selectedEndOption = EndOption.REPLAY;

    private Font titleFont;
    private Font subtitleFont;
    private Font buttonFont;
    private final Rectangle2D.Float replayButtonBounds = new Rectangle2D.Float();
    private final Rectangle2D.Float menuButtonBounds = new Rectangle2D.Float();

    public TheEndScreen(Core game) {
        this.game = game;
        this.pauseOverlay = new PauseOverlay(game, 3);
        createFonts();
    }

    @Override
    public void onShow() {
        music.play(THE_END_MUSIC_PATH);
        map = new TmxMapLoader().load("ASSETSGame/stringstarfields/TheEnd.tmx");
        worldWidth = map.getWidth() * map.getTileWidth();
        worldHeight = map.getHeight() * map.getTileHeight();

        camera = new Camera2D(Core.VIRTUAL_WIDTH, Core.VIRTUAL_HEIGHT);
        camera.x = 400;
        camera.y = 240;

        mapRenderer = new TiledMapRenderer(map);
        player = game.player;
        groundLayer = map.getLayer("ground");
        waterLayer = findWaterLayer();

        hpBarFrameTexture = Assets.load("ASSETSGame/Dragonhpbar.png");
        hpBarFrames = splitHpBar(hpBarFrameTexture);
        emeraldTexture = Assets.load("Emerald.png");
    }

    @Override
    public void update(float delta) {
        InputState input = game.getInput();
        if (emeraldCollected) {
            updateEndOverlayInput(input);
            return;
        }

        if (pauseOverlay.update(camera, player, input)) {
            return;
        }

        if (!pauseOverlay.isPaused()) {
            accumulator += Math.min(delta, 0.25f);
            while (accumulator >= STEP) {
                player.update(STEP, input);
                player.x += player.velocityX * STEP;
                handlePlayerCollision(player);

                checkEmeraldPickup();

                if (!player.isDead() && isTouchingWater()) {
                    killByDrowning();
                }
                accumulator -= STEP;
            }

            if (!player.isDead()) {
                checkMapTeleport();
            } else if (!deathScreenQueued && player.isDeathAnimationFinished()) {
                deathScreenQueued = true;
                game.setScreen(new DeathScreen(game, 3, getDeathMessage(player.getDeathReason())));
                return;
            }

            updateCamera();
        }
    }

    @Override
    public void render(java.awt.Graphics2D g) {
        mapRenderer.render(g, camera);

        drawEmerald(g);

        float playerScreenX = toScreenX(player.x);
        float playerScreenY = toScreenY(player.y, Player.SPRITE_HEIGHT);
        player.draw(g, playerScreenX, playerScreenY);
        drawHpUI(g);

        if (pauseOverlay.isPaused()) {
            pauseOverlay.draw(camera, g);
        }

        if (emeraldCollected) {
            drawEndOverlay(g);
        }
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void dispose() {
        music.stop();
        OneShotSound.stop(emeraldPickupClip);
        emeraldPickupClip = null;
    }

    private void updateCamera() {
        float halfViewW = camera.viewportWidth / 2f;
        float halfViewH = camera.viewportHeight / 2f;

        camera.x = player.x + Player.SPRITE_WIDTH / 2f;
        camera.y = player.y + Player.SPRITE_HEIGHT / 2f;

        if (camera.viewportWidth < worldWidth) {
            camera.x = Math.max(halfViewW, Math.min(camera.x, worldWidth - halfViewW));
        } else {
            camera.x = worldWidth / 2f;
        }

        if (camera.viewportHeight < worldHeight) {
            camera.y = Math.max(halfViewH, Math.min(camera.y, worldHeight - halfViewH));
        } else {
            camera.y = worldHeight / 2f;
        }
    }

    private void checkMapTeleport() {
        if (player.isDead()) return;
        if (isTeleporting) return;

        if (player.x < 0) {
            isTeleporting = true;
            player.x = MAP2_SPAWN_X;
            player.y = MAP2_SPAWN_Y;
            player.velocityX = 0f;
            player.velocityY = 0f;
            game.setScreen(new GameScreen2(game));
            return;
        }

        if (player.x > worldWidth - Player.SPRITE_WIDTH) {
            player.x = worldWidth - Player.SPRITE_WIDTH;
            player.velocityX = 0f;
        }
    }

    private TiledLayer findWaterLayer() {
        TiledLayer directLayer = map.getLayer("Water");
        if (directLayer != null) {
            return directLayer;
        }
        for (TiledLayer layer : map.getLayers()) {
            if (layer.getName() != null && layer.getName().toLowerCase().contains("water")) {
                return layer;
            }
        }
        return null;
    }

    private boolean isTouchingWater() {
        if (waterLayer == null) return false;

        float tileWidth = map.getTileWidth();
        float tileHeight = map.getTileHeight();

        float collisionX = player.getCollisionX();
        float collisionY = player.getCollisionY();
        float collisionWidth = Player.COLLISION_WIDTH;
        float collisionHeight = Player.COLLISION_HEIGHT;

        float[] pointsX = {
            collisionX + 2f,
            collisionX + collisionWidth / 2f,
            collisionX + collisionWidth - 2f
        };
        float[] pointsY = {
            collisionY + 2f,
            collisionY + collisionHeight / 2f,
            collisionY + collisionHeight - 2f
        };

        for (float px : pointsX) {
            for (float py : pointsY) {
                int tileX = (int) (px / tileWidth);
                int tileY = (int) (py / tileHeight);

                if (tileX >= 0 && tileX < waterLayer.getWidth()
                    && tileY >= 0 && tileY < waterLayer.getHeight()
                    && waterLayer.getGid(tileX, tileY) != 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private void killByDrowning() {
        if (player.isDead()) return;
        player.takeDamage(player.getMaxHp(), "duoi nuoc");
    }

    private String getDeathMessage(String reason) {
        if ("duoi nuoc".equalsIgnoreCase(reason)) {
            return "Ban da chet vi bi duoi nuoc.";
        }
        return "Ban da chet vi ga.";
    }

    private void drawHpUI(java.awt.Graphics2D g) {
        int maxHits = (int) player.getMaxHp();
        int hitsTaken = (int) (player.getMaxHp() - player.getHp());
        hitsTaken = Math.max(0, Math.min(maxHits, hitsTaken));
        BufferedImage hpFrame = hpBarFrames[hitsTaken];

        float uiX = camera.x - camera.viewportWidth / 2f + 16f;
        float uiY = camera.y + camera.viewportHeight / 2f - hpFrame.getHeight() - 16f;

        float screenX = toScreenX(uiX);
        float screenY = toScreenY(uiY, hpFrame.getHeight());
        GraphicsUtil.drawImage(g, hpFrame, screenX, screenY, hpFrame.getWidth(), hpFrame.getHeight(), false, false);
    }

    private void drawEmerald(java.awt.Graphics2D g) {
        if (emeraldTexture == null) {
            return;
        }
        float gemWidth = EMERALD_BASE_WIDTH;
        float gemHeight = gemWidth * emeraldTexture.getHeight() / emeraldTexture.getWidth();
        float worldX = worldWidth / 2f - gemWidth / 2f;
        float worldY = worldHeight / 2f - gemHeight / 2f;

        float screenX = toScreenX(worldX);
        float screenY = toScreenY(worldY, gemHeight);
        GraphicsUtil.drawImage(g, emeraldTexture, screenX, screenY, gemWidth, gemHeight, false, false);
    }

    private void checkEmeraldPickup() {
        if (emeraldCollected || player.isDead() || emeraldTexture == null) {
            return;
        }
        float gemWidth = EMERALD_BASE_WIDTH;
        float gemHeight = gemWidth * emeraldTexture.getHeight() / emeraldTexture.getWidth();
        float gemX = worldWidth / 2f - gemWidth / 2f;
        float gemY = worldHeight / 2f - gemHeight / 2f;

        float playerX = player.getCollisionX();
        float playerY = player.getCollisionY();
        float playerW = Player.COLLISION_WIDTH;
        float playerH = Player.COLLISION_HEIGHT;

        if (playerX < gemX + gemWidth && playerX + playerW > gemX
            && playerY < gemY + gemHeight && playerY + playerH > gemY) {
            emeraldCollected = true;
            selectedEndOption = EndOption.REPLAY;
            player.velocityX = 0f;
            player.velocityY = 0f;
            emeraldPickupClip = OneShotSound.playClip(EMERALD_PICKUP_SOUND);
            if (emeraldPickupClip != null) {
                emeraldPickupClip.start();
            }
        }
    }

    private void drawEndOverlay(java.awt.Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, Core.VIRTUAL_WIDTH, Core.VIRTUAL_HEIGHT);

        String title = "YOU SAVED THE FOREST";
        String subtitle = "Thank you for playing";

        g.setFont(titleFont);
        FontMetrics titleMetrics = g.getFontMetrics();
        int titleX = (Core.VIRTUAL_WIDTH - titleMetrics.stringWidth(title)) / 2;
        int titleY = 150;
        g.setColor(new Color(235, 255, 220));
        g.drawString(title, titleX, titleY);

        g.setFont(subtitleFont);
        FontMetrics subtitleMetrics = g.getFontMetrics();
        int subtitleX = (Core.VIRTUAL_WIDTH - subtitleMetrics.stringWidth(subtitle)) / 2;
        int subtitleY = titleY + 40;
        g.setColor(new Color(210, 230, 210));
        g.drawString(subtitle, subtitleX, subtitleY);

        updateEndButtonBounds();
        drawButton(g, replayButtonBounds, "Replay");
        drawButton(g, menuButtonBounds, "M.Menu");
    }

    private void drawButton(java.awt.Graphics2D g, Rectangle2D.Float bounds, String label) {
        boolean selected =
            ("Replay".equals(label) && selectedEndOption == EndOption.REPLAY)
            || ("M.Menu".equals(label) && selectedEndOption == EndOption.MENU);
        g.setColor(selected ? new Color(70, 110, 70, 235) : new Color(30, 30, 30, 220));
        g.fill(bounds);
        g.setColor(selected ? new Color(230, 255, 220) : new Color(255, 255, 255, 200));
        g.draw(bounds);

        g.setFont(buttonFont);
        FontMetrics metrics = g.getFontMetrics();
        int textX = Math.round(bounds.x + bounds.width / 2f - metrics.stringWidth(label) / 2f);
        int textY = Math.round(bounds.y + bounds.height / 2f + metrics.getAscent() / 2f - 2f);
        g.drawString(label, textX, textY);
    }

    private void updateEndButtonBounds() {
        float buttonWidth = 170f;
        float buttonHeight = 44f;
        float padding = 30f;
        float y = Core.VIRTUAL_HEIGHT - buttonHeight - 30f;
        replayButtonBounds.setRect(padding, y, buttonWidth, buttonHeight);
        menuButtonBounds.setRect(Core.VIRTUAL_WIDTH - padding - buttonWidth, y, buttonWidth, buttonHeight);
    }

    private void updateEndOverlayInput(InputState input) {
        if (input.isKeyJustPressed(KeyEvent.VK_A)) {
            selectedEndOption = EndOption.REPLAY;
            return;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_D)) {
            selectedEndOption = EndOption.MENU;
            return;
        }
        if (input.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            OneShotSound.stop(emeraldPickupClip);
            emeraldPickupClip = null;
            if (selectedEndOption == EndOption.REPLAY) {
                game.startLevel(1);
            } else {
                game.setScreen(new StartScreen(game));
            }
        }
    }

    private void createFonts() {
        titleFont = new Font("Serif", Font.BOLD, 32);
        subtitleFont = new Font("Serif", Font.PLAIN, 20);
        buttonFont = new Font("Serif", Font.BOLD, 18);
    }

    private BufferedImage[] splitHpBar(BufferedImage sheet) {
        int frames = 8;
        int frameWidth = sheet.getWidth() / frames;
        BufferedImage[] result = new BufferedImage[frames];
        for (int i = 0; i < frames; i++) {
            result[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, sheet.getHeight());
        }
        return result;
    }

    private void handlePlayerCollision(Player player) {
        float tileWidth = map.getTileWidth();
        float tileHeight = map.getTileHeight();

        float collisionX = player.getCollisionX();
        float collisionY = player.getCollisionY();
        float collisionWidth = Player.COLLISION_WIDTH;
        float collisionHeight = Player.COLLISION_HEIGHT;

        if (player.velocityX < 0) {
            float leftX = collisionX - 1;
            float[] checkPointsY = {
                collisionY + 5,
                collisionY + collisionHeight / 2,
                collisionY + collisionHeight - 8
            };

            boolean hitWall = false;
            for (float checkY : checkPointsY) {
                int tileX = (int) (leftX / tileWidth);
                int tileY = (int) (checkY / tileHeight);

                if (tileX >= 0 && tileX < groundLayer.getWidth() &&
                    tileY >= 0 && tileY < groundLayer.getHeight()) {
                    if (groundLayer.getGid(tileX, tileY) != 0) {
                        hitWall = true;
                        break;
                    }
                }
            }

            if (hitWall) {
                float tileRightEdge = ((int)(leftX / tileWidth) + 1) * tileWidth;
                player.x = tileRightEdge - Player.COLLISION_OFFSET_X + 0.1f;
                player.velocityX = 0;
            }
        }

        if (player.velocityX > 0) {
            float rightX = collisionX + collisionWidth + 1;
            float[] checkPointsY = {
                collisionY + 5,
                collisionY + collisionHeight / 2,
                collisionY + collisionHeight - 5
            };

            boolean hitWall = false;
            for (float checkY : checkPointsY) {
                int tileX = (int) (rightX / tileWidth);
                int tileY = (int) (checkY / tileHeight);

                if (tileX >= 0 && tileX < groundLayer.getWidth() &&
                    tileY >= 0 && tileY < groundLayer.getHeight()) {
                    if (groundLayer.getGid(tileX, tileY) != 0) {
                        hitWall = true;
                        break;
                    }
                }
            }

            if (hitWall) {
                float tileLeftEdge = ((int)(rightX / tileWidth)) * tileWidth;
                player.x = tileLeftEdge - Player.COLLISION_WIDTH - Player.COLLISION_OFFSET_X - 0.1f;
                player.velocityX = 0;
            }
        }

        collisionX = player.getCollisionX();
        collisionY = player.getCollisionY();

        boolean onGround = false;
        float feetY = collisionY - 1;
        float[] checkPointsX = {
            collisionX + 2,
            collisionX + collisionWidth / 2,
            collisionX + collisionWidth - 2
        };

        for (float checkX : checkPointsX) {
            int tileX = (int) (checkX / tileWidth);
            int tileY = (int) (feetY / tileHeight);

            if (tileX >= 0 && tileX < groundLayer.getWidth() &&
                tileY >= 0 && tileY < groundLayer.getHeight()) {

                if (groundLayer.getGid(tileX, tileY) != 0 && player.velocityY <= 0) {
                    onGround = true;
                    player.y = (tileY + 1) * tileHeight - Player.COLLISION_OFFSET_Y;
                    player.velocityY = 0;
                    break;
                }
            }
        }

        player.onGround = onGround;

        if (player.velocityY > 0) {
            float headBaseY = collisionY + Player.COLLISION_HEIGHT;
            float sensorTopY = headBaseY + Player.HEAD_SENSOR_HEIGHT - 1 - 15;

            float[] checkPointsXHead = {
                collisionX + 4,
                collisionX + collisionWidth / 2,
                collisionX + collisionWidth - 4
            };

            for (float checkX : checkPointsXHead) {
                int tileX = (int) (checkX / tileWidth);
                int tileY = (int) (sensorTopY / tileHeight);

                if (tileX >= 0 && tileX < groundLayer.getWidth() &&
                    tileY >= 0 && tileY < groundLayer.getHeight()) {

                    if (groundLayer.getGid(tileX, tileY) != 0) {
                        player.y = tileY * tileHeight
                            - Player.COLLISION_HEIGHT
                            - Player.HEAD_SENSOR_HEIGHT;

                        player.velocityY = 0;
                        player.onGround = false;
                        break;
                    }
                }
            }
        }
    }

    private float toScreenX(float worldX) {
        return worldX - camera.x + camera.viewportWidth / 2f;
    }

    private float toScreenY(float worldY, float height) {
        return camera.viewportHeight / 2f + camera.y - worldY - height;
    }
}
