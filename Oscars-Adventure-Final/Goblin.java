public class Goblin extends Enemy {
    public Goblin() {
        super("Goblin", 70, 15, 10, 20);
    }

    public String getAttackMessage(Entity target) {
        return "The Goblin shrieks and swings at " + target.getName() + "!";
    }
}
