import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class BattleScene extends JPanel {

    private final BattleGUI game;

    // Animation state flags (set by BattleGUI, cleared via resetPoses())
    private boolean playerAttacking = false;
    private boolean playerHurt      = false;
    private boolean enemyHurt       = false;

    // Info box colours
    private static final Color BOX_BG     = new Color(240, 250, 230);
    private static final Color BOX_BORDER = new Color(28,  28,  28);
    private static final Color BOX_SHADOW = new Color(0, 0, 0, 55);

    // Cached background images
    private BufferedImage imgBg;
    private BufferedImage imgBgBoss;

    // Cached player sprite frames
    private BufferedImage imgPlayerIdle;
    private BufferedImage imgPlayerAttack;
    private BufferedImage imgPlayerHurt;

    // Cached enemy sprite frames
    private BufferedImage imgGoblinIdle,   imgGoblinHurt;
    private BufferedImage imgSkeletonIdle, imgSkeletonHurt;
    private BufferedImage imgTrollIdle,    imgTrollHurt;
    private BufferedImage imgWitchIdle,    imgWitchHurt;

    // Default sprite display size (pixels) — change to match your art
    private static final int SPRITE_W = 200;
    private static final int SPRITE_H = 200;


    public BattleScene(BattleGUI game) {
        this.game = game;
        setPreferredSize(new Dimension(560, 288));
        loadImages();
    }

    @Override
    public Dimension getPreferredSize() {
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win != null && win.getHeight() > 0) {
            int h = (int)(win.getHeight() * 0.70);
            return new Dimension(win.getWidth(), h);
        }
        return new Dimension(560, 288);
    }


    //Image loader
    private void loadImages() {
        imgBg           = tryLoad("images/battleground.png");
        imgBgBoss       = tryLoad("images/boss_battleground.png");

        imgPlayerIdle   = tryLoad("images/hero.png");
        imgPlayerAttack = tryLoad("images/hero.png");
        imgPlayerHurt   = tryLoad("images/hero.png");

        imgGoblinIdle   = tryLoad("images/goblin.png");
        imgGoblinHurt   = tryLoad("images/goblin.png");
        imgSkeletonIdle = tryLoad("images/skeleton.png");
        imgSkeletonHurt = tryLoad("images/skeleton.png");
        imgTrollIdle    = tryLoad("images/troll.png");
        imgTrollHurt    = tryLoad("images/troll.png");
        imgWitchIdle    = tryLoad("images/witch.png");
        imgWitchHurt    = tryLoad("images/witch.png");
    }

    private static BufferedImage tryLoad(String path) {
        try {
            File f = new File(path);
            if (f.exists()) return ImageIO.read(f);
        } catch (IOException ignored) {}
        return null; // missing file → placeholder box drawn at runtime
    }

    //Animation state setters (called by BattleGUI)

    /** Player is attacking → player shows attack frame, enemy shows hurt frame. */
    public void triggerPlayerAttack() {
        playerAttacking = true;
        enemyHurt       = true;
        playerHurt      = false;
        repaint();
    }

    // Enemy is attacking → player shows hurt frame.
    public void triggerEnemyAttack() {
        playerHurt      = true;
        playerAttacking = false;
        enemyHurt       = false;
        repaint();
    }

    // Return all sprites to idle.
    public void resetPoses() {
        playerAttacking = false;
        playerHurt      = false;
        enemyHurt       = false;
        repaint();
    }

    //Paint
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int W = getWidth(), H = getHeight();
        boolean boss = game.isBossFight() && game.getCurrentEnemy() != null;

        drawBackground(g2, W, H, boss);

        Enemy  enemy  = game.getCurrentEnemy();
        Player player = game.getPlayer();

        if (enemy != null) {
            drawEnemyInfoBox(g2, enemy);
            drawEnemySprite(g2, enemy, W, H);
        }
        drawPlayerInfoBox(g2, player, W, H);
        drawPlayerSprite(g2, H);

        if (game.getGameState() == BattleGUI.GameState.GAME_OVER) {
            drawGameOverOverlay(g2, W, H);
        }
    }

    //Background
    private void drawBackground(Graphics2D g2, int W, int H, boolean boss) {
        /*
         * TODO: add your background images:
         *   images/battleground.png      (560 x 288) — normal fight
         *   images/boss_battleground.png (560 x 288) — boss fight
         */
        BufferedImage bg = boss ? imgBgBoss : imgBg;
        if (bg != null) {
            g2.drawImage(bg, 0, 0, W, H, null);
        } else {
            // Placeholder: solid colour so the game is still playable
            g2.setColor(boss ? new Color(40, 10, 60) : new Color(100, 180, 100));
            g2.fillRect(0, 0, W, H);
            g2.setColor(new Color(0, 0, 0, 80));
            g2.setFont(new Font("Monospaced", Font.BOLD, 13));
            String label = boss ? "[ images/boss_battleground.png ]"
                                : "[ images/battleground.png ]";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, (W - fm.stringWidth(label)) / 2, H / 2);
        }
    }

    //Enemy sprite
    private void drawEnemySprite(Graphics2D g2, Enemy enemy, int W, int H) {
        /*
         * Enemy sits in the top-right area (above the horizon, Pokémon style).
         * Idle frame shown normally; hurt frame shown when player attacks.
         *
         * TODO: add sprite images, e.g.:
         *   images/goblin_idle.png   images/goblin_hurt.png
         *   images/skeleton_idle.png images/skeleton_hurt.png
         *   images/troll_idle.png    images/troll_hurt.png
         *   images/witch_idle.png    images/witch_hurt.png
         */
        String n = enemy.getName().toLowerCase();

        BufferedImage idle, hurt;
        if      (n.contains("goblin"))   { idle = imgGoblinIdle;   hurt = imgGoblinHurt;   }
        else if (n.contains("skeleton")) { idle = imgSkeletonIdle; hurt = imgSkeletonHurt; }
        else if (n.contains("troll"))    { idle = imgTrollIdle;    hurt = imgTrollHurt;    }
        else if (n.contains("witch"))    { idle = imgWitchIdle;    hurt = imgWitchHurt;    }
        else                             { idle = null;             hurt = null;            }

        // Choose the right frame
        BufferedImage frame = (enemyHurt && hurt != null) ? hurt : idle;

        // Enemy position: top-right, feet roughly at horizon
        int cx = W - W / 4;
        int cy = H / 2 - 12;
        int dx = cx - SPRITE_W / 2;
        int dy = cy - SPRITE_H;

        if (frame != null) {
            if (enemyHurt) {
                // Red tint flash when hurt
                g2.drawImage(tintImage(frame, new Color(255, 60, 60, 150)), dx, dy,
                             SPRITE_W, SPRITE_H, null);
            } else {
                g2.drawImage(frame, dx, dy, SPRITE_W, SPRITE_H, null);
            }
        } else {
            // Placeholder box with label
            drawPlaceholder(g2, dx, dy, SPRITE_W, SPRITE_H,
                    "[ " + n + (enemyHurt ? "_hurt" : "_idle") + ".png ]",
                    new Color(180, 60, 180, 160));
        }
    }

    //Player sprite
    private void drawPlayerSprite(Graphics2D g2, int H) {
        /*
         * Player sits in the bottom-left area (below the horizon, Pokémon style).
         * Three frames driven by combat state:
         *
         * TODO: add sprite images:
         *   images/player_idle.png    — standing / waiting
         *   images/player_attack.png  — mid-swing
         *   images/player_hurt.png    — flinching / taking damage
         */
        int cx = 200;
        int cy = H - 26;
        int dx = cx - SPRITE_W / 2;
        int dy = cy - SPRITE_H;

        BufferedImage frame;
        String label;
        if (playerAttacking) {
            frame = imgPlayerAttack;
            label = "[ player_attack.png ]";
        } else if (playerHurt) {
            frame = imgPlayerHurt;
            label = "[ player_hurt.png ]";
        } else {
            frame = imgPlayerIdle;
            label = "[ player_idle.png ]";
        }

        if (frame != null) {
            if (playerHurt) {
                g2.drawImage(tintImage(frame, new Color(255, 60, 60, 140)), dx, dy,
                             SPRITE_W, SPRITE_H, null);
            } else {
                g2.drawImage(frame, dx, dy, SPRITE_W, SPRITE_H, null);
            }
        } else {
            drawPlaceholder(g2, dx, dy, SPRITE_W, SPRITE_H, label,
                    new Color(60, 120, 220, 160));
        }
    }

    //Placeholder box (shown when an image file is missing)
    private void drawPlaceholder(Graphics2D g2, int x, int y, int w, int h,
                                  String label, Color fill) {
        g2.setColor(fill);
        g2.fillRoundRect(x, y, w, h, 10, 10);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x, y, w, h, 10, 10);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
        FontMetrics fm = g2.getFontMetrics();
        // Word-wrap the label across the box width
        String[] words = label.split(" ");
        StringBuilder line = new StringBuilder();
        int lineY = y + 14;
        for (String word : words) {
            if (fm.stringWidth(line + word) > w - 6) {
                g2.drawString(line.toString().trim(), x + 3, lineY);
                lineY += 12;
                line = new StringBuilder();
            }
            line.append(word).append(" ");
        }
        if (line.length() > 0) g2.drawString(line.toString().trim(), x + 3, lineY);
    }

    //Red/colour tint overlay
    private static BufferedImage tintImage(BufferedImage src, Color tint) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D tg = out.createGraphics();
        tg.drawImage(src, 0, 0, null);
        tg.setColor(tint);
        tg.setComposite(AlphaComposite.SrcAtop.derive(tint.getAlpha() / 255f));
        tg.fillRect(0, 0, src.getWidth(), src.getHeight());
        tg.dispose();
        return out;
    }

    //Info boxes
    private void drawEnemyInfoBox(Graphics2D g2, Enemy enemy) {
        int x = 14, y = 14, bw = 218, bh = 74;
        drawBox(g2, x, y, bw, bh);
        g2.setColor(BOX_BORDER);
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        String label = game.isBossFight() ? "★ " + enemy.getName().toUpperCase()
                : enemy.getName().toUpperCase();
        g2.drawString(label, x + 10, y + 22);
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        g2.drawString("HP", x + 10, y + 52);
        drawBar(g2, x + 34, y + 41, 168, 14, enemy.getHp(), enemy.getMaxHp(), false);
    }

    private void drawPlayerInfoBox(Graphics2D g2, Player p, int W, int H) {
        int bw = 238, bh = 106;
        int x = W - bw - 12, y = H - bh - 12;
        drawBox(g2, x, y, bw, bh);
        g2.setColor(BOX_BORDER);
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2.drawString(p.getName().toUpperCase(), x + 10, y + 22);
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        g2.drawString("HP", x + 10, y + 46);
        drawBar(g2, x + 34, y + 36, 178, 13, p.getHp(), p.getMaxHp(), false);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.drawString(p.getHp() + "/" + p.getMaxHp(), x + bw - 66, y + 48);
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        g2.drawString("MP", x + 10, y + 66);
        drawBar(g2, x + 34, y + 56, 178, 13, p.getTotalMana(), 100, true);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.drawString(p.getTotalMana() + "/100", x + bw - 66, y + 68);
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        g2.setColor(new Color(100, 100, 100));
        g2.drawString("ATK:" + p.getAttack(), x + 10, y + 84);
        g2.setColor(new Color(180, 140, 20));
        g2.drawString("● " + p.getGold() + "g", x + 100, y + 84);
    }

    private void drawBox(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(BOX_SHADOW);
        g2.fillRoundRect(x + 3, y + 3, w, h, 12, 12);
        g2.setColor(BOX_BG);
        g2.fillRoundRect(x, y, w, h, 12, 12);
        g2.setColor(BOX_BORDER);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(x, y, w, h, 12, 12);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawBar(Graphics2D g2, int x, int y, int w, int h,
                         int val, int max, boolean isMana) {
        g2.setColor(new Color(60, 60, 60));
        g2.fillRoundRect(x, y, w, h, 5, 5);
        double ratio = Math.min(1.0, Math.max(0.0, (double) val / Math.max(1, max)));
        int fw = (int)(w * ratio);
        if (fw > 0) {
            Color fill;
            if (isMana) {
                fill = new Color(72, 160, 255);
            } else {
                if      (ratio > 0.50) fill = new Color(80,  208, 120);
                else if (ratio > 0.25) fill = new Color(240, 196,  48);
                else                   fill = new Color(240,  68,  56);
            }
            g2.setColor(fill);
            g2.fillRoundRect(x, y, fw, h, 5, 5);
            g2.setColor(new Color(255, 255, 255, 90));
            g2.fillRect(x, y, fw, h / 2);
        }
        g2.setColor(BOX_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x, y, w, h, 5, 5);
        g2.setStroke(new BasicStroke(1f));
    }

    //Game Over overlay
    private void drawGameOverOverlay(Graphics2D g2, int W, int H) {
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, W, H);
        g2.setColor(new Color(220, 60, 60));
        g2.setFont(new Font("Monospaced", Font.BOLD, 36));
        FontMetrics fm = g2.getFontMetrics();
        String txt = "GAME OVER";
        g2.drawString(txt, (W - fm.stringWidth(txt)) / 2, H / 2 - 10);
    }
}
