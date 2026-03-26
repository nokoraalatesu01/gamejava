package com.java.WhisperOfTheEmeraldForest.entities;

import com.java.WhisperOfTheEmeraldForest.input.InputState;
import com.java.WhisperOfTheEmeraldForest.util.Animation;
import com.java.WhisperOfTheEmeraldForest.util.Assets;
import com.java.WhisperOfTheEmeraldForest.util.GraphicsUtil;
import com.java.WhisperOfTheEmeraldForest.util.OneShotSound;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

public class Player {
    public enum State {
        IDLE,
        RUN,
        JUMP,
        FALL,
        ATTACK1,
        ATTACK2,
        ATTACK3,
        DEATH
    }

    public State currentState = State.IDLE;

    public float velocityX = 0;
    public float velocityY = 0;
    public boolean onGround = false;
    public boolean facingRight = true;

    private static final float MOVE_SPEED = 150f;
    private static final float JUMP_FORCE = 380f;
    public static final float GRAVITY = -900f;

    private static final int FRAME_WIDTH = 120;
    private static final int FRAME_HEIGHT = 80;
    private static final float ATTACK_FRAME_DURATION = 0.08f;
    private static final float ATTACK_HIT_START = 0.08f;
    private static final float ATTACK_HIT_END = 0.20f;
    private static final float ATTACK_BOX_WIDTH = 24f;
    private static final float ATTACK_BOX_HEIGHT = 22f;
    private static final String ATTACK_SOUND_1 = "Sound/SFX/Attacks/Sword Attacks Hits and Blocks/Sword Attack 1.wav";
    private static final String ATTACK_SOUND_2 = "Sound/SFX/Attacks/Sword Attacks Hits and Blocks/Sword Attack 2.wav";
    private static final String ATTACK_SOUND_3 = "Sound/SFX/Attacks/Sword Attacks Hits and Blocks/Sword Attack 3.wav";
    private static final String JUMP_SOUND = "Sound/SFX/Footsteps/Dirt/Dirt Jump.wav";
    private static final String LAND_SOUND = "Sound/SFX/Footsteps/Dirt/Dirt Land.wav";
    private static final String[] RUN_SOUNDS = {
        "Sound/SFX/Footsteps/Dirt/Dirt Run 1.wav",
        "Sound/SFX/Footsteps/Dirt/Dirt Run 2.wav",
        "Sound/SFX/Footsteps/Dirt/Dirt Run 3.wav",
        "Sound/SFX/Footsteps/Dirt/Dirt Run 4.wav",
        "Sound/SFX/Footsteps/Dirt/Dirt Run 5.wav"
    };
    private static final float RUN_STEP_INTERVAL = 0.28f;

    public float x, y;

    public static final float SPRITE_WIDTH = 120f * 0.75f;
    public static final float SPRITE_HEIGHT = 80f * 0.75f;
    public static final float COLLISION_WIDTH = 18f;
    public static final float COLLISION_HEIGHT = 30f;
    public static final float COLLISION_OFFSET_X = (SPRITE_WIDTH - COLLISION_WIDTH) / 2f;
    public static final float COLLISION_OFFSET_Y = 4f;
    public static final float HEAD_SENSOR_HEIGHT = 4f;
    public static final float FOOT_OFFSET = 0f;

    private BufferedImage idleTexture;
    private BufferedImage runTexture;
    private BufferedImage jumpTexture;
    private BufferedImage fallTexture;
    private BufferedImage attackTexture;
    private BufferedImage attack2Texture;
    private BufferedImage attackComboTexture;
    private BufferedImage attackNoMoveTexture;
    private BufferedImage attack2NoMoveTexture;
    private BufferedImage attackComboNoMoveTexture;
    private BufferedImage deathTexture;

    private Animation idleAnimation;
    private Animation runAnimation;
    private Animation jumpAnimation;
    private Animation fallAnimation;
    private Animation attackAnimation;
    private Animation attack2Animation;
    private Animation attackComboAnimation;
    private Animation attackNoMoveAnimation;
    private Animation attack2NoMoveAnimation;
    private Animation attackComboNoMoveAnimation;
    private Animation deathAnimation;

    private float stateTime;

    private boolean isAttacking = false;
    private boolean attackWithMovement = false;
    private int attackStage = 0;
    private int queuedAttackStage = 0;
    private int attackSerial = 0;
    private boolean dead = false;
    private String deathReason = "";
    private boolean airborneSinceGroundContact = false;
    private float runStepTimer = 0f;

    private static final float MAX_HP = 7f;
    private float hp = MAX_HP;

    public float getCollisionX() {
        return x + COLLISION_OFFSET_X;
    }

    public float getCollisionY() {
        return y + COLLISION_OFFSET_Y;
    }

    public Player() {
        idleTexture = Assets.load("ASSETSGame/FreeKnight_v1/Colour2/NoOutline/120x80_PNGSheets/_Idle.png");
        runTexture = Assets.load("ASSETSGame/FreeKnight_v1/Colour2/NoOutline/120x80_PNGSheets/_Run.png");
        jumpTexture = Assets.load("ASSETSGame/FreeKnight_v1/Colour2/NoOutline/120x80_PNGSheets/_Jump.png");
        fallTexture = Assets.load("ASSETSGame/FreeKnight_v1/Colour2/NoOutline/120x80_PNGSheets/_Fall.png");

        attackTexture = Assets.load("ASSETSGame/FreeKnight_v1/Colour2/NoOutline/120x80_PNGSheets/_Attack.png");
        attack2Texture = Assets.load("ASSETSGame/FreeKnight_v1/Colour2/NoOutline/120x80_PNGSheets/_Attack2.png");
        attackComboTexture = Assets.load("ASSETSGame/FreeKnight_v1/Colour2/NoOutline/120x80_PNGSheets/_AttackCombo.png");
        attackNoMoveTexture = Assets.load("ASSETSGame/FreeKnight_v1/Colour2/NoOutline/120x80_PNGSheets/_AttackNoMovement.png");
        attack2NoMoveTexture = Assets.load("ASSETSGame/FreeKnight_v1/Colour2/NoOutline/120x80_PNGSheets/_Attack2NoMovement.png");
        attackComboNoMoveTexture = Assets.load("ASSETSGame/FreeKnight_v1/Colour2/NoOutline/120x80_PNGSheets/_AttackComboNoMovement.png");
        deathTexture = Assets.load("ASSETSGame/FreeKnight_v1/Colour2/NoOutline/120x80_PNGSheets/_Death.png");

        idleAnimation = createAnimation(idleTexture, 10, 0.12f);
        runAnimation = createAnimation(runTexture, 10, 0.08f);
        jumpAnimation = createAnimation(jumpTexture, 3, 0.12f);
        fallAnimation = createAnimation(fallTexture, 3, 0.12f);

        attackAnimation = createSheetAnimation(attackTexture, ATTACK_FRAME_DURATION);
        attack2Animation = createSheetAnimation(attack2Texture, ATTACK_FRAME_DURATION);
        attackComboAnimation = createSheetAnimation(attackComboTexture, ATTACK_FRAME_DURATION);
        attackNoMoveAnimation = createSheetAnimation(attackNoMoveTexture, ATTACK_FRAME_DURATION);
        attack2NoMoveAnimation = createSheetAnimation(attack2NoMoveTexture, ATTACK_FRAME_DURATION);
        attackComboNoMoveAnimation = createSheetAnimation(attackComboNoMoveTexture, ATTACK_FRAME_DURATION);
        deathAnimation = createSheetAnimation(deathTexture, 0.1f);

        stateTime = 0f;
    }

    public Player(float x, float y) {
        this();
        this.x = x;
        this.y = y;
    }

    public void update(float delta, InputState input) {
        stateTime += delta;

        if (dead) {
            velocityX = 0;
            velocityY = 0;
            currentState = State.DEATH;
            return;
        }

        if (onGround) {
            if (airborneSinceGroundContact) {
                OneShotSound.play(LAND_SOUND);
                airborneSinceGroundContact = false;
            }
        } else {
            airborneSinceGroundContact = true;
        }

        if (input.isKeyJustPressed(KeyEvent.VK_ENTER)) {
            if (!isAttacking) {
                startAttack(1, input);
            } else if (attackStage < 3) {
                queuedAttackStage = Math.max(queuedAttackStage, attackStage + 1);
            }
        }

        if (isAttacking) {
            updateAttackMovement(input);
            runStepTimer = 0f;
        } else {
            updateNormalMovement(input);
            if (input.isKeyDown(KeyEvent.VK_SPACE) && onGround) {
                velocityY = JUMP_FORCE;
                onGround = false;
                airborneSinceGroundContact = true;
                runStepTimer = 0f;
                OneShotSound.play(JUMP_SOUND);
            }
        }

        if (velocityY < -600f) velocityY = -600f;
        velocityY += GRAVITY * delta;
        y += velocityY * delta;

        if (isAttacking) {
            currentState = attackStageToState(attackStage);
            Animation activeAttackAnimation = getCurrentAttackAnimation();
            if (activeAttackAnimation.isFinished(stateTime)) {
                if (queuedAttackStage > attackStage) {
                    startAttack(queuedAttackStage, input);
                } else {
                    endAttack();
                }
            }
            return;
        }

        if (!onGround) {
            if (Math.abs(velocityX) > 1f) {
                currentState = State.RUN;
            } else {
                currentState = velocityY > 0 ? State.JUMP : State.FALL;
            }
        } else {
            currentState = Math.abs(velocityX) > 1f ? State.RUN : State.IDLE;
        }

        updateRunSound(delta);
    }

    private void updateNormalMovement(InputState input) {
        velocityX = 0;

        if (input.isKeyDown(KeyEvent.VK_A)) {
            velocityX = -MOVE_SPEED;
            facingRight = false;
        }

        if (input.isKeyDown(KeyEvent.VK_D)) {
            velocityX = MOVE_SPEED;
            facingRight = true;
        }
    }

    private void updateAttackMovement(InputState input) {
        if (!attackWithMovement) {
            velocityX = 0;
            return;
        }

        velocityX = 0;
        if (input.isKeyDown(KeyEvent.VK_A)) {
            velocityX = -MOVE_SPEED;
            facingRight = false;
        }
        if (input.isKeyDown(KeyEvent.VK_D)) {
            velocityX = MOVE_SPEED;
            facingRight = true;
        }
    }

    private void startAttack(int stage, InputState input) {
        isAttacking = true;
        attackSerial++;
        attackStage = Math.max(1, Math.min(stage, 3));
        queuedAttackStage = attackStage;
        attackWithMovement = onGround && (input.isKeyDown(KeyEvent.VK_A) || input.isKeyDown(KeyEvent.VK_D));
        stateTime = 0f;
        playAttackSound(attackStage);
    }

    private void endAttack() {
        isAttacking = false;
        attackStage = 0;
        queuedAttackStage = 0;
        attackWithMovement = false;
        stateTime = 0f;
    }

    private State attackStageToState(int stage) {
        switch (stage) {
            case 2:
                return State.ATTACK2;
            case 3:
                return State.ATTACK3;
            default:
                return State.ATTACK1;
        }
    }

    private Animation getCurrentAttackAnimation() {
        switch (attackStage) {
            case 2:
                return attackWithMovement ? attack2Animation : attack2NoMoveAnimation;
            case 3:
                return attackWithMovement ? attackComboAnimation : attackComboNoMoveAnimation;
            default:
                return attackWithMovement ? attackAnimation : attackNoMoveAnimation;
        }
    }

    public void draw(java.awt.Graphics2D g, float screenX, float screenY) {
        BufferedImage frame;

        switch (currentState) {
            case RUN:
                frame = runAnimation.getFrame(stateTime, true);
                break;
            case JUMP:
                frame = jumpAnimation.getFrame(stateTime, false);
                break;
            case FALL:
                frame = fallAnimation.getFrame(stateTime, true);
                break;
            case ATTACK1:
            case ATTACK2:
            case ATTACK3:
                frame = getCurrentAttackAnimation().getFrame(stateTime, false);
                break;
            case DEATH:
                frame = deathAnimation.getFrame(stateTime, false);
                break;
            default:
                frame = idleAnimation.getFrame(stateTime, true);
                break;
        }

        float scale = 0.75f;
        float drawWidth = frame.getWidth() * scale;
        float drawHeight = frame.getHeight() * scale;
        GraphicsUtil.drawImage(g, frame, screenX, screenY, drawWidth, drawHeight, !facingRight, false);
    }

    public float getHp() {
        return hp;
    }

    public float getMaxHp() {
        return MAX_HP;
    }

    public boolean isDead() {
        return dead;
    }

    public String getDeathReason() {
        return deathReason;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public int getAttackSerial() {
        return attackSerial;
    }

    public int getAttackStage() {
        return attackStage;
    }

    public boolean overlapsAttackBox(float targetX, float targetY, float targetWidth, float targetHeight) {
        if (!isAttacking || dead) return false;
        if (stateTime < ATTACK_HIT_START || stateTime > ATTACK_HIT_END) return false;

        float attackWidth = ATTACK_BOX_WIDTH;
        float attackHeight = ATTACK_BOX_HEIGHT;
        float attackX = facingRight
            ? (getCollisionX() + COLLISION_WIDTH - 1f)
            : (getCollisionX() - attackWidth + 1f);
        float attackY = getCollisionY() + 4f;

        return attackX < targetX + targetWidth
            && attackX + attackWidth > targetX
            && attackY < targetY + targetHeight
            && attackY + attackHeight > targetY;
    }

    public void takeDamage(float amount, String reason) {
        if (dead) return;
        hp = Math.max(0f, hp - amount);
        if (hp <= 0f) {
            die(reason);
        }
    }

    public void heal(float amount) {
        if (dead) return;
        hp = Math.min(MAX_HP, hp + amount);
    }

    public void die(String reason) {
        if (dead) return;
        dead = true;
        deathReason = reason == null ? "ga" : reason;
        endAttack();
        currentState = State.DEATH;
        stateTime = 0f;
        velocityX = 0f;
        velocityY = 0f;
    }

    public boolean isDeathAnimationFinished() {
        return dead && deathAnimation.isFinished(stateTime);
    }

    public void resetForRespawn(float spawnX, float spawnY) {
        x = spawnX;
        y = spawnY;
        velocityX = 0f;
        velocityY = 0f;
        onGround = false;
        facingRight = true;
        hp = MAX_HP;
        dead = false;
        deathReason = "";
        airborneSinceGroundContact = false;
        runStepTimer = 0f;
        currentState = State.IDLE;
        endAttack();
        stateTime = 0f;
    }

    private Animation createAnimation(BufferedImage texture, int frameCount, float frameDuration) {
        int frameWidth = texture.getWidth() / frameCount;
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = texture.getSubimage(i * frameWidth, 0, frameWidth, texture.getHeight());
        }
        return new Animation(frameDuration, frames);
    }

    private Animation createSheetAnimation(BufferedImage texture, float frameDuration) {
        int frameCount = Math.max(1, texture.getWidth() / FRAME_WIDTH);
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = texture.getSubimage(i * FRAME_WIDTH, 0, FRAME_WIDTH, FRAME_HEIGHT);
        }
        return new Animation(frameDuration, frames);
    }

    private void playAttackSound(int stage) {
        switch (stage) {
            case 2:
                OneShotSound.play(ATTACK_SOUND_2);
                break;
            case 3:
                OneShotSound.play(ATTACK_SOUND_3);
                break;
            default:
                OneShotSound.play(ATTACK_SOUND_1);
                break;
        }
    }

    private void updateRunSound(float delta) {
        boolean shouldPlayRunStep = onGround && !isAttacking && Math.abs(velocityX) > 1f;
        if (!shouldPlayRunStep) {
            runStepTimer = 0f;
            return;
        }

        runStepTimer -= delta;
        if (runStepTimer <= 0f) {
            playRunSound();
            runStepTimer = RUN_STEP_INTERVAL;
        }
    }

    private void playRunSound() {
        int index = ThreadLocalRandom.current().nextInt(RUN_SOUNDS.length);
        OneShotSound.play(RUN_SOUNDS[index]);
    }
}
