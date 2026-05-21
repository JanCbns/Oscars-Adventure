import java.util.Scanner;

public class Inventory {

    static Weapon[] weapons = {
            new Weapon("Sword"),
            new Weapon("Bow"),
            new Weapon("Knife"),
            new Weapon("Axe"),
            new Weapon("Magic Staff")
    };

    static Item[] items = {
            new Item("Heal Potion"),
            new Item("Mana Potion")
    };

    public static void open(Player player) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== INVENTORY ===");
        System.out.println("1. Weapons  2. Items");
        int choice = scanner.nextInt();

        if (choice == 1) {
            for (int i = 0; i < weapons.length; i++)
                System.out.println((i + 1) + ". " + weapons[i].name);
            int pick = scanner.nextInt();
            switch (pick) {
                case 1 -> { player.setAttack(player.getAttack() + 15); System.out.println("Sword equipped."); }
                case 2 -> { player.setAttack(player.getAttack() + 10); System.out.println("Bow equipped."); }
                case 3 -> { player.setAttack(player.getAttack() + 5);  System.out.println("Knife equipped."); }
                case 4 -> { player.setAttack(player.getAttack() + 20); System.out.println("Axe equipped."); }
                case 5 -> { player.setAttack(player.getAttack() + 12); player.addTempMana(20); System.out.println("Staff equipped."); }
            }
        } else if (choice == 2) {
            for (int i = 0; i < items.length; i++)
                System.out.println((i + 1) + ". " + items[i].name);
            int pick = scanner.nextInt();
            if (pick == 1) { player.setHp(player.getHp() + 50); System.out.println("+50 HP"); }
            else if (pick == 2) { player.addTempMana(40); System.out.println("+40 Mana"); }
        }
    }
}