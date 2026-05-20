import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Player hero = new Player("Oscar");
            new BattleGUI(hero);
        });
    }
}