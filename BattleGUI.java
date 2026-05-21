import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

/**
 * BattleGUI — Pokémon-style turn-based battle interface.
 *
 * Layout (top-to-bottom):
 *   ┌────────────────────────────────────────────────────┐
 *   │         BattleScene (288px)                        │  ← sprites + HP/MP info boxes
 *   ├────────────────────────────────────────────────────┤
 *   │      Dialog box  (~80px)                           │  ← scrolling battle text
 *   ├────────────────────────────────────────────────────┤
 *   │      Action panel  (~96px)                         │  ← context-sensitive buttons
 *   └────────────────────────────────────────────────────┘
 */
public class BattleGUI extends JFrame {

    // ── Game state machine ────────────────────────────────────────
    public enum GameState {
        WORLD_MAP,      // between fights: show round options
        PLAYER_TURN,    // player chooses action in combat
        ENEMY_TURN,     // enemy acts (brief animated delay)
        SKILL_MENU,     // skill sub-menu open
        WEAPON_MENU,    // inventory → weapons
        ITEM_MENU,      // inventory → items
        SHOP,           // shop screen
        CAMP,           // camp phase after fight
        GAME_OVER
    }

    // ── Core model ────────────────────────────────────────────────
    private final Player player;
    private Enemy  currentEnemy;
    private boolean bossFight = false;
    private int     roundCounter  = 0;
    private boolean isFullscreen = false;

    // ── UI components ─────────────────────────────────────────────
    private BattleScene scene;
    private JTextArea   dialogArea;
    private JPanel      actionPanel;
    private JPanel      cardPanel;   // CardLayout host inside actionPanel

    private GameState state = GameState.WORLD_MAP;

    private static final Random RNG = new Random();

    // ── Colours / fonts ───────────────────────────────────────────
    private static final Color C_BG        = new Color(20,  24,  36);
    private static final Color C_DLG_BG    = new Color(240, 248, 230);
    private static final Color C_DLG_BOR   = new Color(28,  28,  28);
    private static final Color C_ACT_BG    = new Color(240, 248, 230);
    private static final Color C_BTN_NRM   = new Color(240, 248, 230);
    private static final Color C_BTN_HVR   = new Color(200, 228, 180);
    private static final Color C_BTN_BOR   = new Color(28,  28,  28);
    private static final Color C_BTN_TXT   = new Color(28,  28,  28);
    private static final Font  F_MONO_MED  = new Font("Monospaced", Font.BOLD, 13);
    private static final Font  F_MONO_SM   = new Font("Monospaced", Font.PLAIN, 12);

    // ── CardLayout card names ─────────────────────────────────────
    private static final String CARD_WORLD  = "WORLD";
    private static final String CARD_BATTLE = "BATTLE";
    private static final String CARD_SKILL  = "SKILL";
    private static final String CARD_WEAPON = "WEAPON";
    private static final String CARD_ITEM   = "ITEM";
    private static final String CARD_SHOP   = "SHOP";
    private static final String CARD_CAMP   = "CAMP";
    private static final String CARD_OVER   = "OVER";

    // ═══════════════════════════════════════════════════════════════
    //  Constructor
    // ═══════════════════════════════════════════════════════════════
    public BattleGUI(Player player) {
        this.player = player;

        setTitle("⚔  Adventure Quest");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(560, 480));
        getContentPane().setBackground(C_BG);
        setLayout(new BorderLayout(0, 0));

        // ── Scene ────────────────────────────────────────────────
        scene = new BattleScene(this);
        add(scene, BorderLayout.NORTH);

        // ── Dialog area ──────────────────────────────────────────
        JPanel dlgWrapper = buildDialogPanel();
        add(dlgWrapper, BorderLayout.CENTER);

        // ── Action panel ─────────────────────────────────────────
        actionPanel = new JPanel(new BorderLayout());
        actionPanel.setBackground(C_BG);
        actionPanel.setPreferredSize(new Dimension(100, 100));
        cardPanel = new JPanel(new CardLayout());
        cardPanel.setBackground(C_ACT_BG);

        cardPanel.add(buildWorldCard(),  CARD_WORLD);
        cardPanel.add(buildBattleCard(), CARD_BATTLE);
        cardPanel.add(buildSkillCard(),  CARD_SKILL);
        cardPanel.add(buildWeaponCard(), CARD_WEAPON);
        cardPanel.add(buildItemCard(),   CARD_ITEM);
        cardPanel.add(buildShopCard(),   CARD_SHOP);
        cardPanel.add(buildCampCard(),   CARD_CAMP);
        cardPanel.add(buildOverCard(),   CARD_OVER);

        // Rounded border wrapper for card panel
        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setBackground(C_BG);
        cardWrapper.setBorder(BorderFactory.createEmptyBorder(0, 6, 6, 6));
        cardWrapper.add(cardPanel, BorderLayout.CENTER);
        actionPanel.add(cardWrapper, BorderLayout.CENTER);

        add(actionPanel, BorderLayout.SOUTH);

        // ── Setup fullscreen toggle with F11 key ─────────────────
        setupFullscreenToggle();

        // Set to fullscreen by default
        setMaximizedFullscreen();

        setLocationRelativeTo(null);
        setVisible(true);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                revalidate();
                repaint();
            }
        });

        // ── Kick off ─────────────────────────────────────────────
        AudioManager.playMusic("sounds/Soundtrack.wav");
        showWorldMap();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Fullscreen methods
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Setup F11 key to toggle fullscreen mode
     */
    private void setupFullscreenToggle() {
        KeyStroke f11Key = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f11Key, "toggleFullscreen");
        getRootPane().getActionMap().put("toggleFullscreen", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleFullscreen();
            }
        });

        // Also add Escape key to exit fullscreen
        KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKey, "exitFullscreen");
        getRootPane().getActionMap().put("exitFullscreen", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isFullscreen) {
                    exitFullscreen();
                }
            }
        });
    }

    /**
     * Toggle between fullscreen and windowed mode
     */
    private void toggleFullscreen() {
        if (isFullscreen) {
            exitFullscreen();
        } else {
            setMaximizedFullscreen();
        }
    }

    /**
     * Set the window to maximized borderless fullscreen
     */
    private void setMaximizedFullscreen() {
        dispose();
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        isFullscreen = true;
        setVisible(true);
    }

    /**
     * Exit fullscreen and return to windowed mode
     */
    private void exitFullscreen() {
        dispose();
        setUndecorated(false);
        setExtendedState(JFrame.NORMAL);
        setSize(800, 600); // Default windowed size
        isFullscreen = false;
        setVisible(true);
    }

    /**
     * Set true exclusive fullscreen mode (better performance for games)
     */
    private void setExclusiveFullscreen() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        
        if (gd.isFullScreenSupported()) {
            dispose();
            setUndecorated(true);
            setResizable(false);
            gd.setFullScreenWindow(this);
            isFullscreen = true;
            setVisible(true);
        } else {
            // Fallback to maximized window
            setMaximizedFullscreen();
        }
    }

    /**
     * Exit exclusive fullscreen mode
     */
    private void exitExclusiveFullscreen() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        
        if (gd.getFullScreenWindow() != null) {
            gd.setFullScreenWindow(null);
            dispose();
            setUndecorated(false);
            setResizable(true);
            setSize(800, 600);
            isFullscreen = false;
            setVisible(true);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Dialog panel
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildDialogPanel() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(C_BG);  // ✅ background fixed
        wrap.setBorder(BorderFactory.createEmptyBorder(4, 6, 0, 6));

        dialogArea = new JTextArea(4, 80);
        dialogArea.setFont(F_MONO_SM);
        dialogArea.setEditable(false);
        dialogArea.setLineWrap(true);
        dialogArea.setWrapStyleWord(true);
        dialogArea.setBackground(C_DLG_BG);
        dialogArea.setForeground(C_DLG_BOR);
        dialogArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JScrollPane sp = new JScrollPane(dialogArea);
        sp.setBorder(new LineBorder(C_DLG_BOR, 2, true));
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        sp.setPreferredSize(new Dimension(548, 62));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        wrap.add(sp, BorderLayout.CENTER);
        return wrap;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Card builders
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildWorldCard() {
        JPanel p = menuPanel();
        p.add(makeBtn("⚔  Combat",   e -> startRound(false)));
        p.add(makeBtn("🛒  Shop",     e -> openShop()));
        p.add(makeBtn("🎒  Inventory",e -> openInventoryMenu(false)));
        return p;
    }

    private JPanel buildBattleCard() {
        JPanel p = menuPanel();
        p.add(makeBtn("⚔  Attack",   e -> playerAttack()));
        p.add(makeBtn("✨  Skills",   e -> showCard(CARD_SKILL, GameState.SKILL_MENU)));
        p.add(makeBtn("🎒  Bag",      e -> openInventoryMenu(true)));
        return p;
    }

    private JPanel buildSkillCard() {
        JPanel p = menuPanel();
        p.add(makeBtn("🔥 Fireball\n(20dmg/30mp)", e -> castSkill(20, 30, "Fireball")));
        p.add(makeBtn("🌀 Wind Slash\n(25dmg/40mp)",e -> castSkill(25, 40, "Wind Slash")));
        p.add(makeBtn("💧 Hydro Pulse\n(30dmg/50mp)",e -> castSkill(30, 50, "Hydro Pulse")));
        p.add(makeBtn("← Back",                      e -> showCard(CARD_BATTLE, GameState.PLAYER_TURN)));
        return p;
    }

    private JPanel buildWeaponCard() {
        JPanel p = menuPanel();
        p.add(makeBtn("🗡 Sword\n(+15 ATK)",      e -> equipWeapon("Sword",      15, 0,  false)));
        p.add(makeBtn("🏹 Bow\n(+10 ATK)",         e -> equipWeapon("Bow",        10, 0,  false)));
        p.add(makeBtn("🔪 Knife\n(+5 ATK)",         e -> equipWeapon("Knife",      5,  0,  false)));
        p.add(makeBtn("🪓 Axe\n(+20 ATK)",          e -> equipWeapon("Axe",        20, 0,  false)));
        p.add(makeBtn("🪄 Magic Staff\n(+12 ATK +20 MP)", e -> equipWeapon("Magic Staff", 12, 20, true)));
        p.add(makeBtn("← Back",                     e -> backFromInventory()));
        return p;
    }

    private JPanel buildItemCard() {
        JPanel p = menuPanel();
        p.add(makeBtn("🧪 Heal Potion\n(+50 HP)",  e -> useItem("Heal Potion")));
        p.add(makeBtn("🔵 Mana Potion\n(+40 MP)",  e -> useItem("Mana Potion")));
        p.add(makeBtn("← Back",                     e -> backFromInventory()));
        return p;
    }

    private JPanel buildShopCard() {
        JPanel p = menuPanel();
        p.add(makeBtn("🧪 Heal Potion\n(25g)",     e -> buyItem()));
        p.add(makeBtn("← Leave Shop",              e -> leaveShop()));
        return p;
    }

    private JPanel buildCampCard() {
        JPanel p = menuPanel();
        p.add(makeBtn("🛌 Heal Up\n(+30 HP)",      e -> campChoice(true)));
        p.add(makeBtn("🔮 Prepare\n(+20 Temp MP)", e -> campChoice(false)));
        return p;
    }

    private JPanel buildOverCard() {
        JPanel p = menuPanel();
        p.add(makeBtn("🔄 Play Again",             e -> restartGame()));
        return p;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Menu panel factory + button factory
    // ═══════════════════════════════════════════════════════════════
    private JPanel menuPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        p.setBackground(C_ACT_BG);
        p.setBorder(new CompoundBorder(
                new LineBorder(C_BTN_BOR, 2, true),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        return p;
    }

    private JButton makeBtn(String text, ActionListener al) {
        // Support two-line labels via HTML
        String html = "<html><center>" + text.replace("\n", "<br>") + "</center></html>";
        JButton btn = new JButton(html);
        btn.setFont(F_MONO_MED);
        btn.setBackground(C_BTN_NRM);
        btn.setForeground(C_BTN_TXT);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(C_BTN_BOR, 2, true),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        btn.setPreferredSize(new Dimension(148, 76));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(C_BTN_HVR); }
            public void mouseExited (MouseEvent e) { btn.setBackground(C_BTN_NRM); }
        });
        btn.addActionListener(al);
        return btn;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Card switching helper
    // ═══════════════════════════════════════════════════════════════
    private void showCard(String card, GameState s) {
        state = s;
        ((CardLayout) cardPanel.getLayout()).show(cardPanel, card);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Game flow
    // ═══════════════════════════════════════════════════════════════

    /** Show the world-map / round selection screen. */
    private void showWorldMap() {
        currentEnemy = null;
        bossFight    = false;
        roundCounter++;

        if (roundCounter % 5 == 0) {
            // Boss every 5 rounds
            log("!!! BOSS ENCOUNTER !!!\nA terrifying presence fills the air...");
            bossFight    = true;
            currentEnemy = new BossEnemy();
            scene.repaint();
            startCombat();
        } else {
            log("Round " + roundCounter + " — What will you do?");
            showCard(CARD_WORLD, GameState.WORLD_MAP);
            scene.repaint();
        }
    }

    /** Called when player clicks Combat on the world map. */
    private void startRound(boolean forced) {
        currentEnemy = generateRandomEnemy();
        bossFight    = false;
        scene.repaint();
        startCombat();
    }

    private void startCombat() {
        log("A wild " + currentEnemy.getName() + " appears!\n"
                + currentEnemy.getName() + " — HP: " + currentEnemy.getHp()
                + "/" + currentEnemy.getMaxHp());

        if (player.getSpeed() >= currentEnemy.getSpeed()) {
            showCard(CARD_BATTLE, GameState.PLAYER_TURN);
            appendLog("You move first!");
        } else {
            appendLog(currentEnemy.getName() + " moves first!");
            enemyTurnDelayed();
        }
    }

    // ─── Player actions ──────────────────────────────────────────
    private void playerAttack() {

    if (state != GameState.PLAYER_TURN) return;

    AudioManager.playSound("sounds/Attacks.wav");

    int dmg = player.getAttack();
    currentEnemy.receiveDamage(dmg);

    log("You strike " + currentEnemy.getName() + " for " + dmg + " damage!\n"
            + currentEnemy.getName() + " HP: "
            + currentEnemy.getHp() + "/" + currentEnemy.getMaxHp());

    // Show attack/hurt sprites, then reset to idle after a short delay
    scene.triggerPlayerAttack();
    Timer poseReset = new Timer(500, e -> scene.resetPoses());
    poseReset.setRepeats(false);
    poseReset.start();

    afterPlayerAction();
}

    private void castSkill(int dmg, int cost, String name) {

    if (player.getTotalMana() < cost) {
        log("Not enough mana! (Need " + cost + " MP, have "
                + player.getTotalMana() + ")");
        showCard(CARD_BATTLE, GameState.PLAYER_TURN);
        return;
    }

    AudioManager.playSound("sounds/skill.wav");

    player.useMana(cost);
    currentEnemy.receiveDamage(dmg);

    log("✨ You cast " + name + "!\n"
            + currentEnemy.getName() + " takes " + dmg + " damage!\n"
            + currentEnemy.getName() + " HP: "
            + currentEnemy.getHp() + "/" + currentEnemy.getMaxHp());

    scene.triggerPlayerAttack();
    Timer poseReset2 = new Timer(500, e -> scene.resetPoses());
    poseReset2.setRepeats(false);
    poseReset2.start();

    showCard(CARD_BATTLE, GameState.PLAYER_TURN);

    afterPlayerAction();
}

    private void afterPlayerAction() {
        if (currentEnemy.getHp() <= 0) {
            victory();
        } else {
            enemyTurnDelayed();
        }
    }

    // ─── Enemy turn ──────────────────────────────────────────────
    private void enemyTurnDelayed() {
        state = GameState.ENEMY_TURN;
        // Disable all buttons momentarily
        setAllButtonsEnabled(false);

        Timer t = new Timer(900, e -> {
            enemyAct();
            setAllButtonsEnabled(true);
        });
        t.setRepeats(false);
        t.start();
    }

    private void enemyAct() {

    if (currentEnemy == null || currentEnemy.getHp() <= 0) return;

    AudioManager.playSound("sounds/Attacks.wav");

    String msg = currentEnemy.getAttackMessage(player);

    currentEnemy.dealDamage(player);

    appendLog(msg + "\n"
            + player.getName() + " takes "
            + currentEnemy.getAttack()
            + " damage! HP: "
            + player.getHp() + "/" + player.getMaxHp());

    // Show enemy-attacks / player-hurt sprites, reset after delay
    scene.triggerEnemyAttack();
    Timer poseReset = new Timer(500, e -> scene.resetPoses());
    poseReset.setRepeats(false);
    poseReset.start();

    if (player.getHp() <= 0) {
        gameOver();
        } else {
        showCard(CARD_BATTLE, GameState.PLAYER_TURN);
        }
    }

    // ─── Victory / Camp / Game Over ──────────────────────────────
    private void victory() {
        player.flushTempMana();
        player.addGold(currentEnemy.getLootValue());
        log("🏆 Victory! +" + currentEnemy.getLootValue() + " gold.\n"
                + "Total gold: " + player.getGold() + "g");
        scene.repaint();
        showCard(CARD_CAMP, GameState.CAMP);
    }

    private void campChoice(boolean heal) {
        if (heal) {
            int old = player.getHp();
            player.setHp(player.getHp() + 30);
            log("🛌 You rest at camp and recover " + (player.getHp() - old) + " HP.");
        } else {
            player.addTempMana(20);
            log("🔮 You meditate and gain 20 temporary MP.");
        }
        scene.repaint();
        showWorldMap();
    }

    private void gameOver() {
        state = GameState.GAME_OVER;
        log("💀 You have been defeated...\n   — GAME OVER —");
        scene.repaint();
        showCard(CARD_OVER, GameState.GAME_OVER);
    }

    // ─── Shop ────────────────────────────────────────────────────
    private void openShop() {
        log("🛒 Welcome to the Shop!\nGold: " + player.getGold() + "g\n"
                + "• Heal Potion — 25g");
        showCard(CARD_SHOP, GameState.SHOP);
    }

    private void buyItem() {
        if (player.getGold() >= 25) {
            player.addGold(-25);
            player.setHp(player.getHp() + 50);
            log("You bought a Heal Potion! +50 HP restored.\nGold remaining: " + player.getGold() + "g");
            scene.repaint();
        } else {
            log("Not enough gold! (Need 25g, have " + player.getGold() + "g)");
        }
    }

    private void leaveShop() {
        log("You leave the shop. Round " + roundCounter);
        showCard(CARD_WORLD, GameState.WORLD_MAP);
    }

    // ─── Inventory ───────────────────────────────────────────────
    /** inCombat=true means return to BATTLE card, not WORLD card. */
    private boolean inventoryFromCombat = false;

    private void openInventoryMenu(boolean inCombat) {
        inventoryFromCombat = inCombat;
        log("🎒 INVENTORY\nChoose a category:");
        // Re-use weapon/item cards, but we need a mini "Inventory" chooser card.
        // We'll show a quick dialog-style prompt via temporary JPanel.
        showInventoryChooser();
    }

    private JPanel invChooserCard; // built once, lazily added

    private void showInventoryChooser() {
        if (invChooserCard == null) {
            invChooserCard = menuPanel();
            invChooserCard.add(makeBtn("🗡 Weapons", e -> showCard(CARD_WEAPON, GameState.WEAPON_MENU)));
            invChooserCard.add(makeBtn("🧪 Items",   e -> showCard(CARD_ITEM,   GameState.ITEM_MENU)));
            invChooserCard.add(makeBtn("← Back",     e -> backFromInventory()));
            cardPanel.add(invChooserCard, "INV");
        }
        showCard("INV", GameState.WORLD_MAP); // borrow WORLD_MAP state temporarily
    }

    private void backFromInventory() {
        if (inventoryFromCombat) {
            showCard(CARD_BATTLE, GameState.PLAYER_TURN);
        } else {
            showCard(CARD_WORLD, GameState.WORLD_MAP);
        }
    }

    private void equipWeapon(String name, int atkBonus, int manaBonus, boolean givesMana) {
        player.setAttack(player.getAttack() + atkBonus);
        if (givesMana) player.addTempMana(manaBonus);
        log("🗡 Equipped " + name + "!\n+ATK " + atkBonus
                + (givesMana ? "  +MP " + manaBonus : "")
                + "\nATK is now " + player.getAttack());
        scene.repaint();
        backFromInventory();
    }

    private void useItem(String name) {

        if (name.equals("Heal Potion")) {

        AudioManager.playSound("sounds/healing.wav");

        int old = player.getHp();

        player.setHp(player.getHp() + 50);

        log("🧪 Used Heal Potion! +"
                + (player.getHp() - old)
                + " HP restored.");

        } else {

        AudioManager.playSound("sounds/mana.wav");

        player.addTempMana(40);

        log("🔵 Used Mana Potion! +40 MP added.");
        }

        scene.repaint();
        backFromInventory();
    }

    // ─── Restart ─────────────────────────────────────────────────
    private void restartGame() {
        // Reset player stats
        player.setHp(100);
        player.setAttack(20);
        // Reset gold to 50 — we need a package-private helper since gold is private
        // Workaround: add gold to make it exactly 50
        int diff = 50 - player.getGold();
        player.addGold(diff);
        roundCounter = 0;
        currentEnemy = null;
        bossFight    = false;
        log("✨ A new adventure begins!\nGood luck, " + player.getName() + "!");
        scene.repaint();
        showWorldMap();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Utility helpers
    // ═══════════════════════════════════════════════════════════════
    /** Replace dialog text. */
    void log(String msg) {
        dialogArea.setText(msg);
        dialogArea.setCaretPosition(0);
    }

    /** Append to existing dialog text. */
    void appendLog(String msg) {
        dialogArea.append("\n" + msg);
    }

    private Enemy generateRandomEnemy() {
        return switch (RNG.nextInt(3)) {
            case 0 -> new Goblin();
            case 1 -> new Skeleton();
            default -> new Troll();
        };
    }

    /** Walk all buttons in the card panel and enable/disable them. */
    private void setAllButtonsEnabled(boolean enabled) {
        setEnabled(cardPanel, enabled);
    }

    private void setEnabled(Container c, boolean enabled) {
        for (Component child : c.getComponents()) {
            child.setEnabled(enabled);
            if (child instanceof Container) setEnabled((Container) child, enabled);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Accessors for BattleScene
    // ═══════════════════════════════════════════════════════════════
    public Player    getPlayer()       { return player; }
    public Enemy     getCurrentEnemy() { return currentEnemy; }
    public boolean   isBossFight()     { return bossFight; }
    public GameState getGameState()    { return state; }
}
