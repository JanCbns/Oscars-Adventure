
public class Enemy extends Entity {
    private int lootValue;

    public Enemy(String name, int hp, int attack, int speed, int gold) {
        super(name, hp, hp, attack, speed);
        this.lootValue = gold;
    }

    public String getAttackMessage(Entity target) {
        String var10000 = this.getName();
        return var10000 + " attacks " + target.getName() + "!";
    }

    public void dealDamage(Entity target) {
        target.receiveDamage(this.getAttack());
    }

    public void takeTurn(Entity target) {
        this.dealDamage(target);
    }

    public int getLootValue() {
        return this.lootValue;
    }
}
