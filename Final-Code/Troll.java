public class Troll extends Enemy {
    public Troll() {
        super("Troll", 65, 13, 15, 18);
    }

    public String getAttackMessage(Entity target) {
        return "The Troll roars and hurls rocks at " + target.getName() + "!";
    }
}
