import java.util.Random;
import java.util.Scanner;

public class TextAdventureGame {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final Random RANDOM = new Random();

    private static int health = 100;
    private static int potions = 2;
    private static int gold = 0;

    public static void main(String[] args) {
        printIntro();

        boolean playing = true;
        while (playing && health > 0) {
            printStatus();
            printMenu();

            String choice = SCANNER.nextLine().trim().toLowerCase();
            switch (choice) {
                case "1":
                case "explore":
                    explore();
                    break;
                case "2":
                case "rest":
                    rest();
                    break;
                case "3":
                case "shop":
                    shop();
                    break;
                case "4":
                case "quit":
                    playing = false;
                    System.out.println("You leave the forest with " + gold + " gold. Safe travels, adventurer.");
                    break;
                default:
                    System.out.println("Invalid choice. Pick 1, 2, 3, or 4.");
            }
        }

        if (health <= 0) {
            System.out.println("\nYou collapse from your wounds. Game over.");
            System.out.println("Final gold: " + gold);
        }

        SCANNER.close();
    }

    private static void printIntro() {
        System.out.println("=========================================");
        System.out.println("      THE FOREST OF WHISPERS");
        System.out.println("=========================================");
        System.out.println("You are a traveler searching for treasure.");
        System.out.println("Explore, survive battles, and collect gold.\n");
    }

    private static void printStatus() {
        System.out.println("\n--- STATUS ---");
        System.out.println("Health : " + health);
        System.out.println("Potions: " + potions);
        System.out.println("Gold   : " + gold);
    }

    private static void printMenu() {
        System.out.println("\nChoose an action:");
        System.out.println("1) Explore");
        System.out.println("2) Rest");
        System.out.println("3) Shop");
        System.out.println("4) Quit");
        System.out.print("> ");
    }

    private static void explore() {
        int event = RANDOM.nextInt(100);

        if (event < 50) {
            battle();
        } else if (event < 80) {
            int foundGold = 5 + RANDOM.nextInt(16);
            gold += foundGold;
            System.out.println("You found a hidden stash and gained " + foundGold + " gold.");
        } else {
            int damage = 5 + RANDOM.nextInt(11);
            health -= damage;
            System.out.println("A trap is triggered! You take " + damage + " damage.");
        }
    }

    private static void battle() {
        int enemyHealth = 25 + RANDOM.nextInt(26);
        System.out.println("A wild goblin appears! Enemy health: " + enemyHealth);

        while (enemyHealth > 0 && health > 0) {
            System.out.println("\nBattle action: attack / potion / run");
            System.out.print("> ");
            String action = SCANNER.nextLine().trim().toLowerCase();

            switch (action) {
                case "attack":
                    int playerDamage = 8 + RANDOM.nextInt(13);
                    enemyHealth -= playerDamage;
                    System.out.println("You strike for " + playerDamage + " damage.");
                    break;
                case "potion":
                    usePotion();
                    break;
                case "run":
                    if (RANDOM.nextBoolean()) {
                        System.out.println("You escaped successfully.");
                        return;
                    } else {
                        System.out.println("Escape failed!");
                    }
                    break;
                default:
                    System.out.println("Invalid action.");
                    continue;
            }

            if (enemyHealth > 0) {
                int enemyDamage = 6 + RANDOM.nextInt(10);
                health -= enemyDamage;
                System.out.println("Goblin hits you for " + enemyDamage + " damage.");
            }
        }

        if (health > 0) {
            int reward = 10 + RANDOM.nextInt(16);
            gold += reward;
            System.out.println("You defeated the goblin and earned " + reward + " gold!");
        }
    }

    private static void rest() {
        int heal = 8 + RANDOM.nextInt(13);
        health += heal;
        if (health > 100) {
            health = 100;
        }
        System.out.println("You rest by a fire and recover " + heal + " health.");
    }

    private static void shop() {
        System.out.println("A merchant appears. Buy potion for 15 gold? (yes/no)");
        System.out.print("> ");
        String answer = SCANNER.nextLine().trim().toLowerCase();

        if (!answer.equals("yes")) {
            System.out.println("You leave the shop.");
            return;
        }

        if (gold >= 15) {
            gold -= 15;
            potions++;
            System.out.println("You bought a potion. Potions now: " + potions);
        } else {
            System.out.println("Not enough gold.");
        }
    }

    private static void usePotion() {
        if (potions <= 0) {
            System.out.println("You have no potions left.");
            return;
        }

        potions--;
        int heal = 20 + RANDOM.nextInt(11);
        health += heal;
        if (health > 100) {
            health = 100;
        }
        System.out.println("You used a potion and healed " + heal + " health.");
    }
}
