import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class TitleScreen extends JFrame {

    private static final Color C_BTN_FACE   = new Color( 28,  22,   8);
    private static final Color C_BTN_HOVER  = new Color( 50,  38,  10);
    private static final Color C_BTN_TEXT   = new Color(220, 200, 120);
    private static final Color C_BTN_BORDER = new Color(160, 130,  60);

    private BufferedImage bgImage;
    private boolean       btnHover = false;
    private TitlePanel    panel;

    public TitleScreen() {
        super("Oscar's Adventure");

        try { bgImage = ImageIO.read(new File("images/titlescreen.png")); }
        catch (Exception ignored) { bgImage = null; }

        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GraphicsDevice gd = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

        panel = new TitlePanel();
        add(panel);

        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(this);
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setVisible(true);
        }

        // ESC to quit
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
             .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "quit");
        panel.getActionMap().put("quit", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { System.exit(0); }
        });
    }

    //Inner panel

    private class TitlePanel extends JPanel {

        TitlePanel() {
            setBackground(Color.BLACK);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (btnRect().contains(e.getPoint())) launchGame();
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    boolean now = btnRect().contains(e.getPoint());
                    if (now != btnHover) {
                        btnHover = now;
                        setCursor(btnHover
                            ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                            : Cursor.getDefaultCursor());
                        repaint();
                    }
                }
            });
        }

        private Rectangle btnRect() {
            int W = getWidth(), H = getHeight();
            int bw = (int)(W * 0.18);
            int bh = (int)(H * 0.08);
            return new Rectangle(W / 2 - bw / 2, (int)(H * 0.78), bw, bh);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);

            int W = getWidth(), H = getHeight();
            drawBackground(g2, W, H);
            drawVignette(g2, W, H);
            drawPlayButton(g2, H);
        }

        private void drawBackground(Graphics2D g2, int W, int H) {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, W, H);
            if (bgImage != null) {
                double ir = (double) bgImage.getWidth() / bgImage.getHeight();
                double sr = (double) W / H;
                int dw, dh;
                if (sr > ir) { dh = H; dw = (int)(H * ir); }
                else         { dw = W; dh = (int)(W / ir); }
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(bgImage, (W - dw) / 2, (H - dh) / 2, dw, dh, null);
            }
        }


        private void drawVignette(Graphics2D g2, int W, int H) {
            g2.setPaint(new GradientPaint(0, H * 0.5f, new Color(0,0,0,0), 0, H, new Color(0,0,0,220)));
            g2.fillRect(0, 0, W, H);
            g2.setPaint(new RadialGradientPaint(
                new Point(W/2, H/2), W * 0.75f,
                new float[]{0f, 1f},
                new Color[]{new Color(0,0,0,0), new Color(0,0,0,150)}));
            g2.fillRect(0, 0, W, H);
        }

        private void drawPlayButton(Graphics2D g2, int H) {
            Rectangle r  = btnRect();
            int x = r.x, y = r.y, bw = r.width, bh = r.height;
            int fontSize = Math.max(14, bh / 2);

            // Button face
            g2.setColor(btnHover ? C_BTN_HOVER : C_BTN_FACE);
            g2.fillRect(x, y, bw, bh);

            // Simple 2px border
            g2.setColor(C_BTN_BORDER);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRect(x, y, bw, bh);

            // PLAY label — centred
            g2.setFont(new Font("Monospaced", Font.BOLD, fontSize));
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (bw - fm.stringWidth("PLAY")) / 2;
            int ty = y + (bh + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(C_BTN_TEXT);
            g2.drawString("PLAY", tx, ty);
        }
    }

    //Launch game
    private void launchGame() {
        GraphicsEnvironment.getLocalGraphicsEnvironment()
                           .getDefaultScreenDevice()
                           .setFullScreenWindow(null);
        dispose();
        SwingUtilities.invokeLater(() -> new BattleGUI(new Player("Oscar")));
    }
}
