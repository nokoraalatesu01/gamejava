package com.java.WhisperOfTheEmeraldForest.entities;

import com.java.WhisperOfTheEmeraldForest.util.Animation;
import com.java.WhisperOfTheEmeraldForest.util.Assets;
import com.java.WhisperOfTheEmeraldForest.util.GraphicsUtil;
import com.java.WhisperOfTheEmeraldForest.util.OneShotSound;
import java.awt.image.BufferedImage;

public class OrcEnemy {
    private enum State {
        IDLE,
        WALK,
        ATTACK,
        HURT,
        DEATH
    }

    private static final String BASE_PATH = "ASSETSGame/Tiny RPG Character Asset Pack v1.03b -Free Soldier&Orc/Tiny RPG Character Asset Pack v1.03 -Free Soldier&Orc/Characters(100x100)/Orc/Orc/";
    public static final float SPRITE_WIDTH = 80f;
    public static final float SPRITE_HEIGHT = 80f;
    private static final float SPEED = 36f;
    private static final float CHASE_SPEED = 52f;
    private static final float JUMP_FORCE = 235f;
    private static final float JUMP_COOLDOWN = 0.35f;
    private static final float DRAW_Y_OFFSET = 0f;
    private static final float PATROL_IDLE_TIME = 0.8f;
    private static final float ATTACK_HIT_TIME = 0.2f;
    private static final float ATTACK_COOLDOWN = 0.7f;
    private static final float ATTACK_RANGE_X = 28f;
    private static final float ATTACK_RANGE_Y = 22f;
    private static final float CHASE_RANGE_X = 180f;
    private static final float CHASE_RANGE_Y = 60f;
    private static final float ATTACK_BOX_WIDTH = 22f;
    private static final float ATTACK_BOX_HEIGHT = 22f;
    private static final float COLLISION_WIDTH = Player.COLLISION_WIDTH;
    private static final float COLLISION_HEIGHT = Player.COLLISION_HEIGHT;
    private static final float COLLISION_OFFSET_X = (SPRITE_WIDTH - COLLISION_WIDTH) / 2f;
    private static final float COLLISION_OFFSET_Y = 34f;
    private static final float HEAD_SENSOR_HEIGHT = Player.HEAD_SENSOR_HEIGHT;
    private static final String HIT_SOUND_1 = "Sound/SFX/Attacks/Sword Attacks Hits and Blocks/Sword Impact Hit 1.wav";
    private static final String HIT_SOUND_2 = "Sound/SFX/Attacks/Sword Attacks Hits and Blocks/Sword Impact Hit 2.wav";
    private static final String HIT_SOUND_3 = "Sound/SFX/Attacks/Sword Attacks Hits and Blocks/Sword Impact Hit 3.wav";
    private static final String ORC_ATTACK_SOUND = "Sound/SFX/Attacks/Axe/freesound_community-axe-slash-1-106748.wav";

    private final BufferedImage idleTexture;
    private final BufferedImage walkTexture;
    private final BufferedImage attack1Texture;
    private final BufferedImage attack2Texture;
    private final BufferedImage hurtTexture;
    private final BufferedImage deathTexture;

    private final Animation idleAnimation;
    private final Animation walkAnimation;
    private final Animation attack1Animation;
    private final Animation attack2Animation;
    private final Animation hurtAnimation;
    private final Animation deathAnimation;

    private final float patrolMinX;
    private final float patrolMaxX;

    public float x;
    public float y;
    public float velocityX = 0f;
    public float velocityY = 0f;
    public boolean onGround = false;

    private float hp = 3f;
    private float stateTime = 0f;
    private float attackCooldown = 0f;
    private float jumpCooldown = 0f;
    private float hurtTimer = 0f;
    private float patrolPauseTimer = 0f;
    private float moveDir = 1f;
    private boolean attacking = false;
    private boolean attackHitApplied = false;
    private boolean useSecondAttack = false;
    private boolean facingRight = true;
    private int lastPlayerAttackSerial = -1;
    private boolean potionDropped = false;
    private State state = State.WALK;

    public OrcEnemy(float spawnX, float spawnY, float patrolHalfRange) {
        this.x = spawnX;
        this.y = spawnY;
        this.patrolMinX = spawnX - patrolHalfRange;
        this.patrolMaxX = spawnX + patrolHalfRange;

        idleTexture = Assets.load(BASE_PATH + "Orc-Idle.png");
        walkTexture = Assets.load(BASE_PATH + "Orc-Walk.png");
        attack1Texture = Assets.load(BASE_PATH + "Orc-Attack01.png");
        attack2Texture = Assets.load(BASE_PATH + "Orc-Attack02.png");
        hurtTexture = Assets.load(BASE_PATH + "Orc-Hurt.png");
        deathTexture = Assets.load(BASE_PATH + "Orc-Death.png");

        idleAnimation = createAnimation(idleTexture, 6, 0.12f);
        walkAnimation = createAnimation(walkTexture, 8, 0.1f);
        attack1Animation = createAnimation(attack1Texture, 6, 0.08f);
        attack2Animation = createAnimation(attack2Texture, 6, 0.08f);
        hurtAnimation = createAnimation(hurtTexture, 4, 0.1f);
        deathAnimation = createAnimation(deathTexture, 4, 0.14f);
    }

    public void update(float delta, Player player) {
        stateTime += delta;
        if (attackCooldown > 0f) attackCooldown -= delta;
        if (jumpCooldown > 0f) jumpCooldown -= delta;

        handleHitFromPlayer(player);

        if (isDead()) {
            state = State.DEATH;
            velocityX = 0f;
            velocityY = 0f;
            return;
        }

        if (hurtTimer > 0f) {
            hurtTimer -= delta;
            state = State.HURT;
            if (hurtTimer <= 0f) {
                stateTime = 0f;
                state = State.WALK;
            }
            velocityX = 0f;
            return;
        }

        float playerCenterX = player.x + Player.SPRITE_WIDTH * 0.5f;
        float orcCenterX = x + SPRITE_WIDTH * 0.5f;

        float dx = playerCenterX - orcCenterX;
        boolean playerInAttackRange = isPlayerInAttackRange(player) && !player.isDead();
        boolean playerDetected = isPlayerInChaseRange(player) && !player.isDead();
        facingRight = (playerDetected || playerInAttackRange) ? dx >= 0f : moveDir >= 0f;

        if (attacking) {
            state = State.ATTACK;
            Animation attackAnim = useSecondAttack ? attack2Animation : attack1Animation;
            velocityX = 0f;

            if (!attackHitApplied && stateTime >= ATTACK_HIT_TIME && !player.isDead()) {
                float attackWidth = ATTACK_BOX_WIDTH;
                float attackHeight = ATTACK_BOX_HEIGHT;
                float attackX = facingRight
                    ? (getCollisionX() + COLLISION_WIDTH - 1f)
                    : (getCollisionX() - attackWidth + 1f);
                float attackY = getCollisionY() + 4f;
                if (overlaps(attackX, attackY, attackWidth, attackHeight,
                    player.getCollisionX(), player.getCollisionY(), Player.COLLISION_WIDTH, Player.COLLISION_HEIGHT)) {
                    player.takeDamage(1f, "ga");
                }
                attackHitApplied = true;
            }

            if (attackAnim.isFinished(stateTime)) {
                attacking = false;
                attackCooldown = ATTACK_COOLDOWN;
                stateTime = 0f;
                state = State.WALK;
            }
            return;
        }

        if (playerInAttackRange && attackCooldown <= 0f) {
            attacking = true;
            attackHitApplied = false;
            useSecondAttack = !useSecondAttack;
            stateTime = 0f;
            state = State.ATTACK;
            OneShotSound.play(ORC_ATTACK_SOUND);
            return;
        }

        if (playerDetected) {
            state = State.WALK;
            velocityX = (dx >= 0f ? 1f : -1f) * CHASE_SPEED;
            return;
        }

        if (patrolPauseTimer > 0f) {
            patrolPauseTimer -= delta;
            velocityX = 0f;
            state = State.IDLE;
            if (patrolPauseTimer <= 0f) {
                stateTime = 0f;
            }
            return;
        }

        state = State.WALK;

        if (x < patrolMinX) {
            moveDir = 1f;
        } else if (x > patrolMaxX) {
            moveDir = -1f;
        }

        velocityX = moveDir * SPEED;
        facingRight = moveDir >= 0f;

        if (moveDir < 0f && x <= patrolMinX) {
            x = patrolMinX;
            moveDir = 1f;
            patrolPauseTimer = PATROL_IDLE_TIME;
            stateTime = 0f;
            state = State.IDLE;
            velocityX = 0f;
        } else if (moveDir > 0f && x >= patrolMaxX) {
            x = patrolMaxX;
            moveDir = -1f;
            patrolPauseTimer = PATROL_IDLE_TIME;
            stateTime = 0f;
            state = State.IDLE;
            velocityX = 0f;
        }
    }

    private void handleHitFromPlayer(Player player) {
        if (isDead() || hurtTimer > 0f || !player.isAttacking()) return;
        if (player.getAttackSerial() == lastPlayerAttackSerial) return;

        if (player.overlapsAttackBox(getCollisionX(), getCollisionY(), COLLISION_WIDTH, COLLISION_HEIGHT)) {
            lastPlayerAttackSerial = player.getAttackSerial();
            playHitSound(player.getAttackStage());
            hp -= 1f;
            attacking = false;
            attackHitApplied = false;
            stateTime = 0f;
            if (hp <= 0f) {
                state = State.DEATH;
            } else {
                hurtTimer = 0.24f;
                state = State.HURT;
            }
        }
    }

    public boolean isDead() {
        return hp <= 0f;
    }

    public boolean isPotionDropped() {
        return potionDropped;
    }

    public void markPotionDropped() {
        potionDropped = true;
    }

    public boolean isRemovable() {
        return isDead() && deathAnimation.isFinished(stateTime);
    }

    public void draw(java.awt.Graphics2D g, float screenX, float screenY) {
        BufferedImage frame;
        switch (state) {
            case WALK:
                frame = walkAnimation.getFrame(stateTime, true);
                break;
            case ATTACK:
                frame = (useSecondAttack ? attack2Animation : attack1Animation).getFrame(stateTime, false);
                break;
            case HURT:
                frame = hurtAnimation.getFrame(stateTime, false);
                break;
            case DEATH:
                frame = deathAnimation.getFrame(stateTime, false);
                break;
            default:
                frame = idleAnimation.getFrame(stateTime, true);
                break;
        }

        GraphicsUtil.drawImage(g, frame, screenX, screenY + DRAW_Y_OFFSET, SPRITE_WIDTH, SPRITE_HEIGHT, !facingRight, false);
    }

    public float getCollisionX() {
        return x + COLLISION_OFFSET_X;
    }

    public float getCollisionY() {
        return y + COLLISION_OFFSET_Y;
    }

    public float getCollisionWidth() {
        return COLLISION_WIDTH;
    }

    public float getCollisionHeight() {
        return COLLISION_HEIGHT;
    }

    public float getHeadSensorHeight() {
        return HEAD_SENSOR_HEIGHT;
    }

    public boolean tryJump() {
        if (!onGround || jumpCooldown > 0f || isDead() || hurtTimer > 0f || attacking) {
            return false;
        }
        velocityY = JUMP_FORCE;
        onGround = false;
        jumpCooldown = JUMP_COOLDOWN;
        return true;
    }

    private Animation createAnimation(BufferedImage texture, int frameCount, float frameDuration) {
        int frameWidth = texture.getWidth() / frameCount;
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = texture.getSubimage(i * frameWidth, 0, frameWidth, texture.getHeight());
        }
        return new Animation(frameDuration, frames);
    }

    private boolean isPlayerInAttackRange(Player player) {
        float rangeX = ATTACK_RANGE_X;
        float rangeY = ATTACK_RANGE_Y;
        return Math.abs((player.getCollisionX() + Player.COLLISION_WIDTH * 0.5f) - (getCollisionX() + COLLISION_WIDTH * 0.5f)) <= rangeX
            && Math.abs((player.getCollisionY() + Player.COLLISION_HEIGHT * 0.5f) - (getCollisionY() + COLLISION_HEIGHT * 0.5f)) <= rangeY;
    }

    private boolean isPlayerInChaseRange(Player player) {
        float rangeX = CHASE_RANGE_X;
        float rangeY = CHASE_RANGE_Y;
        return Math.abs((player.getCollisionX() + Player.COLLISION_WIDTH * 0.5f) - (getCollisionX() + COLLISION_WIDTH * 0.5f)) <= rangeX
            && Math.abs((player.getCollisionY() + Player.COLLISION_HEIGHT * 0.5f) - (getCollisionY() + COLLISION_HEIGHT * 0.5f)) <= rangeY;
    }

    private boolean overlaps(float ax, float ay, float aw, float ah, float bx, float by, float bw, float bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    private void playHitSound(int attackStage) {
        switch (attackStage) {
            case 2:
                OneShotSound.play(HIT_SOUND_2);
                break;
            case 3:
                OneShotSound.play(HIT_SOUND_3);
                break;
            default:
                OneShotSound.play(HIT_SOUND_1);
                break;
        }
    }
}
