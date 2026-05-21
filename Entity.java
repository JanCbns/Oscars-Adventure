
public abstract class Entity {
    private String name;
    private int hp;
    private int maxHp;
    private int attack;
    private int speed;

    public Entity(String name, int hp, int maxHp, int attack, int speed) {
        this.name = name;
        this.hp = hp;
        this.maxHp = maxHp;
        this.attack = attack;
        this.speed = speed;
    }

    public String getName() {
        return this.name;
    }

    public int getHp() {
        return this.hp;
    }

    public int getMaxHp() {
        return this.maxHp;
    }

    public int getAttack() {
        return this.attack;
    }

    public int getSpeed() {
        return this.speed;
    }

    void setAttack(int attack) {
        this.attack = attack;
    }

    public void setHp(int hp) {
        this.hp = hp;
        if (this.hp > this.maxHp) {
            this.hp = this.maxHp;
        }

        if (this.hp < 0) {
            this.hp = 0;
        }

    }

    public void receiveDamage(int amount) {
        this.hp -= amount;
        if (this.hp < 0) {
            this.hp = 0;
        }

    }
}
