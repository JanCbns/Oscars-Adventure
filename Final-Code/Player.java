public class Player extends Entity {
    private int mana = 50;
    private int tempMana = 0;
    private int gold = 50;

    public Player(String name) {
        super(name, 100, 100, 20, 12);
    }

    public int getTotalMana() {
        return this.mana + this.tempMana;
    }

    public void useMana(int amount) {
        if (this.tempMana >= amount) {
            this.tempMana -= amount;
        } else {
            int remaining = amount - this.tempMana;
            this.tempMana = 0;
            this.mana -= remaining;
            if (this.mana < 0) {
                this.mana = 0;
            }
        }

    }

    public void flushTempMana() {
        this.tempMana = 0;
    }

    public void addTempMana(int amt) {
        this.tempMana += amt;
    }

    public void addGold(int amount) {
        this.gold += amount;
    }

    public int getGold() {
        return this.gold;
    }
}
