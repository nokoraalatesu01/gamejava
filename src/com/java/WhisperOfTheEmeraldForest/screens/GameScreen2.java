package com.java.WhisperOfTheEmeraldForest.screens;

import com.java.WhisperOfTheEmeraldForest.Core;
import com.java.WhisperOfTheEmeraldForest.entities.HealthPotion;
import com.java.WhisperOfTheEmeraldForest.entities.OrcEnemy;
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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GameScreen2 implements Screen {
    private static final float STEP = 1f / 60f;
    private static final float MAP1_SPAWN_X = 1920f;
    private static final float MAP1_SPAWN_Y = 90f;
    private static final float MAP3_SPAWN_X = 6f;
    private static final float MAP3_SPAWN_Y = 250f;
    private static final String GAME2_MUSIC_PATH = "Sound/BGS Loops/Forest Night/Forest Night.wav";
    private static final String POTION_PICKUP_SOUND = "Sound/SFX/Potion/power_up.wav";
    private static final String POTION_SPAWN_SOUND = "Sound/SFX/Potion/dragon-studio-pop-402324.wav";
    private final Core game;
    private final PauseOverlay pauseOverlay;
    private final MessageOverlay transitionBlockedOverlay;
    private final LoopingSound music = new LoopingSound();

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

    private final List<OrcEnemy> orcs = new ArrayList<>();
    private final List<HealthPotion> potions = new ArrayList<>();

    public GameScreen2(Core game) {
        this.game = game;
        this.pauseOverlay = new PauseOverlay(game, 2);
        this.transitionBlockedOverlay = new MessageOverlay(
            "You Have To Slay All the Enemy",
            "Enter to continue"
        );
    }

    @Override
    public void onShow() {
        music.play(GAME2_MUSIC_PATH);
        map = new TmxMapLoader().load("ASSETSGame/stringstarfields/springfield.tmx");
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

        orcs.clear();
        orcs.add(new OrcEnemy(560f, 200f, 130f));
        potions.clear();
    }

    @Override
    public void update(float delta) {
        InputState input = game.getInput();
        if (transitionBlockedOverlay.isVisible()) {
            transitionBlockedOverlay.update(input);
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

                for (int i = orcs.size() - 1; i >= 0; i--) {
                    OrcEnemy orc = orcs.get(i);
                    orc.update(STEP, player);
                    orc.x += orc.velocityX * STEP;
                    if (orc.velocityY < -600f) orc.velocityY = -600f;
                    orc.velocityY += Player.GRAVITY * STEP;
                    orc.y += orc.velocityY * STEP;
                    handleOrcCollision(orc);
                    if (orc.isRemovable() && !orc.isPotionDropped()) {
                        float dropX = orc.getCollisionX() + orc.getCollisionWidth() / 2f;
                        float dropY = orc.getCollisionY();
                        potions.add(new HealthPotion(dropX, dropY));
                        OneShotSound.play(POTION_SPAWN_SOUND);
                        orc.markPotionDropped();
                    }
                    if (orc.isRemovable()) {
                        orcs.remove(i);
                    }
                }

                for (int i = potions.size() - 1; i >= 0; i--) {
                    potions.get(i).update(STEP);
                }
                if (!player.isDead()) {
                    for (int i = potions.size() - 1; i >= 0; i--) {
                        HealthPotion potion = potions.get(i);
                        if (potion.canBePickedUp()
                            && potion.getX() < player.getCollisionX() + Player.COLLISION_WIDTH
                            && potion.getX() + potion.getWidth() > player.getCollisionX()
                            && potion.getY() < player.getCollisionY() + Player.COLLISION_HEIGHT
                            && potion.getY() + potion.getHeight() > player.getCollisionY()) {
                            int healAmount = ThreadLocalRandom.current().nextInt(1, 3);
                            player.heal(healAmount);
                            OneShotSound.play(POTION_PICKUP_SOUND);
                            potions.remove(i);
                        }
                    }
                }
                if (!player.isDead() && isTouchingWater()) {
                    killByDrowning();
                }
                accumulator -= STEP;
            }

            if (!player.isDead()) {
                checkMapTeleport();
            } else if (!deathScreenQueued && player.isDeathAnimationFinished()) {
                deathScreenQueued = true;
                game.setScreen(new DeathScreen(game, 2, getDeathMessage(player.getDeathReason())));
                return;
            }

            updateCamera();
        }
    }

    @Override
    public void render(java.awt.Graphics2D g) {
        mapRenderer.render(g, camera);

        for (OrcEnemy orc : orcs) {
            float screenX = toScreenX(orc.x);
            float screenY = toScreenY(orc.y, OrcEnemy.SPRITE_HEIGHT);
            orc.draw(g, screenX, screenY);
        }
        for (HealthPotion potion : potions) {
            float screenX = toScreenX(potion.getX());
            float screenY = toScreenY(potion.getY(), potion.getHeight());
            potion.draw(g, screenX, screenY);
        }

        float playerScreenX = toScreenX(player.x);
        float playerScreenY = toScreenY(player.y, Player.SPRITE_HEIGHT);
        player.draw(g, playerScreenX, playerScreenY);
        drawHpUI(g);

        if (pauseOverlay.isPaused()) {
            pauseOverlay.draw(camera, g);
        }
        transitionBlockedOverlay.draw(camera, g);
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void dispose() {
        music.stop();
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
            if (!canTeleportToAnotherMap()) {
                player.x = 0f;
                player.velocityX = 0f;
                transitionBlockedOverlay.show();
                return;
            }
            isTeleporting = true;
            player.x = MAP1_SPAWN_X;
            player.y = MAP1_SPAWN_Y;
            player.velocityX = 0f;
            player.velocityY = 0f;
            game.setScreen(new GameScreen(game));
            return;
        }

        if (player.x > worldWidth - Player.SPRITE_WIDTH) {
            if (!canTeleportToAnotherMap()) {
                player.x = worldWidth - Player.SPRITE_WIDTH;
                player.velocityX = 0f;
                transitionBlockedOverlay.show();
                return;
            }
            isTeleporting = true;
            player.x = MAP3_SPAWN_X;
            player.y = MAP3_SPAWN_Y;
            player.velocityX = 0f;
            player.velocityY = 0f;
            game.setScreen(new TheEndScreen(game));
        }
    }

    private boolean canTeleportToAnotherMap() {
        return orcs.isEmpty();
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
            return "Do you really think he can swim with that armor";
        }
        return "Bruh suck as a cock-a-doodle-doo";
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

    private void handleOrcCollision(OrcEnemy orc) {
        float tileWidth = map.getTileWidth();
        float tileHeight = map.getTileHeight();

        float collisionX = orc.getCollisionX();
        float collisionY = orc.getCollisionY();
        float collisionWidth = orc.getCollisionWidth();
        float collisionHeight = orc.getCollisionHeight();

        if (orc.velocityX < 0) {
            float leftX = collisionX - 1f;
            float[] checkPointsY = {collisionY + 4f, collisionY + collisionHeight / 2f, collisionY + collisionHeight - 4f};
            boolean hitWall = false;
            for (float checkY : checkPointsY) {
                int tileX = (int) (leftX / tileWidth);
                int tileY = (int) (checkY / tileHeight);
                if (tileX >= 0 && tileX < groundLayer.getWidth() && tileY >= 0 && tileY < groundLayer.getHeight()
                    && groundLayer.getGid(tileX, tileY) != 0) {
                    hitWall = true;
                    break;
                }
            }
            if (hitWall) {
                float tileRightEdge = ((int) (leftX / tileWidth) + 1) * tileWidth;
                orc.x = tileRightEdge - (orc.getCollisionX() - orc.x) + 0.1f;
                orc.velocityX = 0f;
                orc.tryJump();
            }
        }

        if (orc.velocityX > 0) {
            float rightX = collisionX + collisionWidth + 1f;
            float[] checkPointsY = {collisionY + 4f, collisionY + collisionHeight / 2f, collisionY + collisionHeight - 4f};
            boolean hitWall = false;
            for (float checkY : checkPointsY) {
                int tileX = (int) (rightX / tileWidth);
                int tileY = (int) (checkY / tileHeight);
                if (tileX >= 0 && tileX < groundLayer.getWidth() && tileY >= 0 && tileY < groundLayer.getHeight()
                    && groundLayer.getGid(tileX, tileY) != 0) {
                    hitWall = true;
                    break;
                }
            }
            if (hitWall) {
                float tileLeftEdge = ((int) (rightX / tileWidth)) * tileWidth;
                orc.x = tileLeftEdge - collisionWidth - (orc.getCollisionX() - orc.x) - 0.1f;
                orc.velocityX = 0f;
                orc.tryJump();
            }
        }

        collisionX = orc.getCollisionX();
        collisionY = orc.getCollisionY();

        boolean onGround = false;
        float feetY = collisionY - 1f;
        float fallDistance = Math.max(0f, -orc.velocityY * STEP) + 1f;
        float prevFeetY = feetY + fallDistance;
        int endTileY = (int) (feetY / tileHeight);
        int startTileY = (int) (prevFeetY / tileHeight);
        float[] checkPointsX = {collisionX + 2f, collisionX + collisionWidth / 2f, collisionX + collisionWidth - 2f};
        for (float checkX : checkPointsX) {
            int tileX = (int) (checkX / tileWidth);
            if (tileX < 0 || tileX >= groundLayer.getWidth()) continue;
            for (int tileY = startTileY; tileY >= endTileY; tileY--) {
                if (tileY < 0 || tileY >= groundLayer.getHeight()) continue;
                if (groundLayer.getGid(tileX, tileY) != 0 && orc.velocityY <= 0f) {
                    onGround = true;
                    orc.y = (tileY + 1) * tileHeight - (orc.getCollisionY() - orc.y);
                    orc.velocityY = 0f;
                    break;
                }
            }
            if (onGround) break;
        }
        orc.onGround = onGround;

        if (orc.velocityY > 0f) {
            float sensorTopY = collisionY + collisionHeight + orc.getHeadSensorHeight() - 1f;
            float[] checkPointsXHead = {collisionX + 4f, collisionX + collisionWidth / 2f, collisionX + collisionWidth - 4f};
            for (float checkX : checkPointsXHead) {
                int tileX = (int) (checkX / tileWidth);
                int tileY = (int) (sensorTopY / tileHeight);
                if (tileX >= 0 && tileX < groundLayer.getWidth() && tileY >= 0 && tileY < groundLayer.getHeight()) {
                    if (groundLayer.getGid(tileX, tileY) != 0) {
                        orc.y = tileY * tileHeight - collisionHeight - (orc.getCollisionY() - orc.y) - orc.getHeadSensorHeight();
                        orc.velocityY = 0f;
                        orc.onGround = false;
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
