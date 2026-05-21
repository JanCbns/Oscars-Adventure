public class BossEnemy extends Enemy {
    public BossEnemy() {
        super("Infernal Witch", 120, 20, 15, 500);
    }

    public String getAttackMessage(Entity target) {
        return "⚡ Infernal Witch unleashes a Sunstrike on " + target.getName() + "!";
    }
}
