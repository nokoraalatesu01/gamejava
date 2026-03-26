package com.java.WhisperOfTheEmeraldForest.entities;

import com.java.WhisperOfTheEmeraldForest.util.Assets;
import com.java.WhisperOfTheEmeraldForest.util.GraphicsUtil;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

public class HealthPotion {
    private static final String TEXTURE_PATH = "Health potion.png";
    private static final float WIDTH = 20f;
    private static final float HEIGHT = 20f;
    private static final float GRAVITY = -700f;
    private static final float PICKUP_DELAY = 0.35f;

    private final BufferedImage texture;
    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private final float groundY;
    private float pickupDelayRemaining = PICKUP_DELAY;
    private boolean grounded = false;

    public HealthPotion(float centerX, float groundY) {
        this.texture = Assets.load(TEXTURE_PATH);
        this.x = centerX - WIDTH / 2f;
        this.y = groundY;
        this.groundY = groundY;
        this.velocityX = ThreadLocalRandom.current().nextFloat() * 60f - 30f;
        this.velocityY = 160f + ThreadLocalRandom.current().nextFloat() * 60f;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return WIDTH;
    }

    public float getHeight() {
        return HEIGHT;
    }

    public void draw(java.awt.Graphics2D g, float screenX, float screenY) {
        GraphicsUtil.drawImage(g, texture, screenX, screenY, WIDTH, HEIGHT, false, false);
    }

    public boolean canBePickedUp() {
        return pickupDelayRemaining <= 0f;
    }

    public void update(float delta) {
        if (pickupDelayRemaining > 0f) {
            pickupDelayRemaining = Math.max(0f, pickupDelayRemaining - delta);
        }
        if (grounded) return;
        velocityY += GRAVITY * delta;
        x += velocityX * delta;
        y += velocityY * delta;

        if (y <= groundY) {
            y = groundY;
            velocityY = 0f;
            velocityX = 0f;
            grounded = true;
        }
    }
}
