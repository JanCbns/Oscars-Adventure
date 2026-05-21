public class Skeleton extends Enemy {
    public Skeleton() {
        super("Skeleton", 55, 12, 15, 15);
    }

    public String getAttackMessage(Entity target) {
        return "The Skeleton draws a bow and aims at " + target.getName() + "!";
    }
}
