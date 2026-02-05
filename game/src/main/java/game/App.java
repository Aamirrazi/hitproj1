import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class App {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");
        frame.add(new GamePanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static class GamePanel extends JPanel implements ActionListener {

        static final int SCREEN_WIDTH = 600;
        static final int SCREEN_HEIGHT = 600;
        static final int UNIT_SIZE = 25;
        static final int GAME_UNITS =
                (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);

        static final int INITIAL_DELAY = 120;
        static final int MIN_DELAY = 45;

        final int[] x = new int[GAME_UNITS];
        final int[] y = new int[GAME_UNITS];

        int bodyParts;
        int applesEaten;
        int appleX, appleY;
        int bonusAppleX, bonusAppleY;
        boolean bonusAppleActive;

        int delay;
        char direction;
        boolean running;
        boolean paused;

        Timer timer;
        Random random;

        GamePanel() {
            random = new Random();
            setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
            setFocusable(true);
            addKeyListener(new MyKeyAdapter());
            startGame();
        }

        // ================= GAME CONTROL =================
        void startGame() {
            bodyParts = 6;
            applesEaten = 0;
            direction = 'R';
            running = true;
            paused = false;
            delay = INITIAL_DELAY;
            bonusAppleActive = false;

            for (int i = 0; i < bodyParts; i++) {
                x[i] = 150 - i * UNIT_SIZE;
                y[i] = 150;
            }

            newApple();

            if (timer != null) timer.stop();
            timer = new Timer(delay, this);
            timer.start();
        }

        void newApple() {
            appleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            appleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;

            if (applesEaten > 0 && applesEaten % 5 == 0) {
                bonusAppleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
                bonusAppleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
                bonusAppleActive = true;
            }
        }

        // ================= GAME LOOP =================
        @Override
        public void actionPerformed(ActionEvent e) {
            if (running && !paused) {
                move();
                checkApple();
                checkCollisions();
            }
            repaint();
        }

        void move() {
            for (int i = bodyParts; i > 0; i--) {
                x[i] = x[i - 1];
                y[i] = y[i - 1];
            }

            switch (direction) {
                case 'U' -> y[0] -= UNIT_SIZE;
                case 'D' -> y[0] += UNIT_SIZE;
                case 'L' -> x[0] -= UNIT_SIZE;
                case 'R' -> x[0] += UNIT_SIZE;
            }
        }

        void checkApple() {
            if (x[0] == appleX && y[0] == appleY) {
                bodyParts++;
                applesEaten++;

                speedUp();
                newApple();
            }

            if (bonusAppleActive && x[0] == bonusAppleX && y[0] == bonusAppleY) {
                bodyParts += 2;
                applesEaten += 3;
                speedUp();
                bonusAppleActive = false;
            }
        }

        void speedUp() {
            if (delay > MIN_DELAY) {
                delay -= 4;
                timer.setDelay(delay);
            }
        }

        void checkCollisions() {
            for (int i = bodyParts; i > 0; i--) {
                if (x[0] == x[i] && y[0] == y[i]) running = false;
            }

            if (x[0] < 0 || x[0] >= SCREEN_WIDTH ||
                y[0] < 0 || y[0] >= SCREEN_HEIGHT)
                running = false;

            if (!running) timer.stop();
        }

        // ================= RENDERING =================
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawBackground(g);
            drawGrid(g);
            drawGame(g);
        }

        void drawBackground(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(20, 20, 20),
                    0, SCREEN_HEIGHT, new Color(40, 40, 40));
            g2.setPaint(gp);
            g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }

        void drawGrid(Graphics g) {
            g.setColor(new Color(60, 60, 60));
            for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
                g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
                g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
            }
        }

        void drawGame(Graphics g) {
            if (running) {
                // Apple
                g.setColor(Color.red);
                g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

                // Bonus apple
                if (bonusAppleActive) {
                    g.setColor(Color.yellow);
                    g.fillOval(bonusAppleX, bonusAppleY, UNIT_SIZE, UNIT_SIZE);
                }

                // Snake
                for (int i = 0; i < bodyParts; i++) {
                    g.setColor(i == 0 ? Color.green : new Color(0, 180, 0));
                    g.fillRoundRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE, 10, 10);
                }

                drawHUD(g);

                if (paused) overlayText(g, "PAUSED", Color.yellow);
            } else {
                overlayText(g, "GAME OVER", Color.red);
                overlaySubText(g, "Score: " + applesEaten);
                overlaySubText(g, "Press R to Restart");
            }
        }

        void drawHUD(Graphics g) {
            g.setColor(Color.white);
            g.setFont(new Font("Consolas", Font.BOLD, 16));
            g.drawString("Score: " + applesEaten, 10, 20);
            g.drawString("Speed: " + (INITIAL_DELAY - delay), 500, 20);
        }

        void overlayText(Graphics g, String text, Color c) {
            g.setColor(c);
            g.setFont(new Font("Ink Free", Font.BOLD, 50));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text,
                    (SCREEN_WIDTH - fm.stringWidth(text)) / 2,
                    SCREEN_HEIGHT / 2);
        }

        void overlaySubText(Graphics g, String text) {
            g.setColor(Color.white);
            g.setFont(new Font("Ink Free", Font.BOLD, 24));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text,
                    (SCREEN_WIDTH - fm.stringWidth(text)) / 2,
                    SCREEN_HEIGHT / 2 + 40);
        }

        // ================= INPUT =================
        class MyKeyAdapter extends KeyAdapter {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT -> { if (direction != 'R') direction = 'L'; }
                    case KeyEvent.VK_RIGHT -> { if (direction != 'L') direction = 'R'; }
                    case KeyEvent.VK_UP -> { if (direction != 'D') direction = 'U'; }
                    case KeyEvent.VK_DOWN -> { if (direction != 'U') direction = 'D'; }
                    case KeyEvent.VK_P -> paused = !paused;
                    case KeyEvent.VK_R -> { if (!running) startGame(); }
                }
            }
        }
    }
}
