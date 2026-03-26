package com.java.WhisperOfTheEmeraldForest;

import com.java.WhisperOfTheEmeraldForest.input.InputState;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    private final Core core;
    private final InputState input;
    private final BufferedImage backBuffer;
    private final Object bufferLock = new Object();

    private Thread loopThread;
    private volatile boolean running;

    private float scale = 1f;
    private int offsetX;
    private int offsetY;

    public GamePanel(Core core) {
        this.core = core;
        this.input = new InputState();
        this.backBuffer = new BufferedImage(Core.VIRTUAL_WIDTH, Core.VIRTUAL_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        setPreferredSize(new Dimension(Core.VIRTUAL_WIDTH, Core.VIRTUAL_HEIGHT));
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        requestFocusInWindow();

        addKeyListener(this);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateScale();
                core.resize(Core.VIRTUAL_WIDTH, Core.VIRTUAL_HEIGHT);
            }
        });
        updateScale();
    }

    public InputState getInput() {
        return input;
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        requestFocusInWindow();
        loopThread = new Thread(this, "game-loop");
        loopThread.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            float delta = (now - lastTime) / 1_000_000_000f;
            if (delta > 0.25f) {
                delta = 0.25f;
            }
            lastTime = now;

            core.update(delta);
            renderToBuffer();
            repaint();

            try {
                Thread.sleep(2L);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void renderToBuffer() {
        synchronized (bufferLock) {
            Graphics2D g = backBuffer.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, Core.VIRTUAL_WIDTH, Core.VIRTUAL_HEIGHT);
            core.render(g);
            g.dispose();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        int drawW = Math.round(Core.VIRTUAL_WIDTH * scale);
        int drawH = Math.round(Core.VIRTUAL_HEIGHT * scale);
        synchronized (bufferLock) {
            g2.drawImage(backBuffer, offsetX, offsetY, drawW, drawH, null);
        }
    }

    private void updateScale() {
        int width = Math.max(1, getWidth());
        int height = Math.max(1, getHeight());
        float scaleX = width / (float) Core.VIRTUAL_WIDTH;
        float scaleY = height / (float) Core.VIRTUAL_HEIGHT;
        scale = Math.min(scaleX, scaleY);
        int drawW = Math.round(Core.VIRTUAL_WIDTH * scale);
        int drawH = Math.round(Core.VIRTUAL_HEIGHT * scale);
        offsetX = (width - drawW) / 2;
        offsetY = (height - drawH) / 2;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        input.setKeyDown(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        input.setKeyDown(e.getKeyCode(), false);
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
