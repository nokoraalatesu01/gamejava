package com.java.WhisperOfTheEmeraldForest;

import com.java.WhisperOfTheEmeraldForest.entities.Player;
import com.java.WhisperOfTheEmeraldForest.input.InputState;
import com.java.WhisperOfTheEmeraldForest.screens.GameScreen;
import com.java.WhisperOfTheEmeraldForest.screens.GameScreen2;
import com.java.WhisperOfTheEmeraldForest.screens.Screen;
import com.java.WhisperOfTheEmeraldForest.screens.StartScreen;
import com.java.WhisperOfTheEmeraldForest.screens.TheEndScreen;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Core {
    public static final int VIRTUAL_WIDTH = 800;
    public static final int VIRTUAL_HEIGHT = 480;

    private GamePanel panel;
    private JFrame frame;
    private GraphicsDevice device;
    private Screen currentScreen;
    private Rectangle windowedBounds;
    private boolean fullscreen;

    public final Player player;

    public Core() {
        this.player = new Player(80f, 120f);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Core().start());
    }

    private void start() {
        frame = new JFrame("Whisper Of The Emerald Forest");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        panel = new GamePanel(this);
        frame.setContentPane(panel);
        frame.setSize(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        windowedBounds = frame.getBounds();
        device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        setScreen(new StartScreen(this));
        panel.start();
    }

    public InputState getInput() {
        return panel.getInput();
    }

    public void setScreen(Screen screen) {
        if (currentScreen != null) {
            currentScreen.dispose();
        }
        currentScreen = screen;
        if (currentScreen != null) {
            currentScreen.onShow();
        }
    }

    public Screen getCurrentScreen() {
        return currentScreen;
    }

    public void update(float delta) {
        if (currentScreen != null) {
            currentScreen.update(delta);
        }
    }

    public void render(java.awt.Graphics2D g) {
        if (currentScreen != null) {
            currentScreen.render(g);
        }
    }

    public void resize(int width, int height) {
        if (currentScreen != null) {
            currentScreen.resize(width, height);
        }
    }

    public void startGame() {
        startLevel(1);
    }

    public void startLevel(int level) {
        if (level == 3) {
            player.resetForRespawn(6f, 250f);
            setScreen(new TheEndScreen(this));
        } else if (level == 2) {
            player.resetForRespawn(40f, 140f);
            setScreen(new GameScreen2(this));
        } else {
            player.resetForRespawn(50f, 120f);
            setScreen(new GameScreen(this));
        }
    }

    public void setResolutionSmall() {
        setWindowedSize(800, 480);
    }

    public void setResolutionMedium() {
        setWindowedSize(1200, 720);
    }

    public void setResolutionLarge() {
        setWindowedSize(1600, 960);
    }

    public void setResolutionFullscreen() {
        SwingUtilities.invokeLater(this::enterFullscreenInternal);
    }

    private void setWindowedSize(int width, int height) {
        SwingUtilities.invokeLater(() -> {
            exitFullscreenInternal();
            frame.setSize(width, height);
            frame.setLocationRelativeTo(null);
            windowedBounds = frame.getBounds();
        });
    }

    private void enterFullscreenInternal() {
        if (fullscreen || frame == null || device == null) {
            return;
        }
        windowedBounds = frame.getBounds();
        frame.dispose();
        frame.setUndecorated(true);
        frame.setResizable(false);
        device.setFullScreenWindow(frame);
        frame.setVisible(true);
        fullscreen = true;
    }

    private void exitFullscreenInternal() {
        if (!fullscreen || frame == null || device == null) {
            return;
        }
        device.setFullScreenWindow(null);
        frame.dispose();
        frame.setUndecorated(false);
        frame.setResizable(false);
        if (windowedBounds != null) {
            frame.setBounds(windowedBounds);
        }
        frame.setVisible(true);
        fullscreen = false;
    }
}
