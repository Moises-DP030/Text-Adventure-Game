import java.awt.*;
import java.util.Random;
import javax.swing.*;

public class Update extends JFrame {

    private enum State { MENU, BATTLE, SHOP, OVER }

    private static final Random RANDOM = new Random();

    private int health = 100;
    private int potions = 2;
    private int gold = 0;
    private int enemyHealth = 0;
    private State state = State.MENU;

    private final JTextArea output = new JTextArea();
    private final JTextField input = new JTextField();

    public Update() {
        super("The Forest of Whispers");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(640, 520);
        setLocationRelativeTo(null);

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 14);

        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        output.setFont(font);
        output.setBackground(new Color(25, 25, 30));
        output.setForeground(new Color(220, 220, 210));
        output.setMargin(new Insets(8, 8, 8, 8));

        input.setFont(font);
        input.addActionListener(e -> {
            String text = input.getText().trim().toLowerCase();
            input.setText("");
            if (!text.isEmpty()) {
                println("> " + text);
                handleInput(text);
            }
        });

        JPanel bottom = new JPanel(new BorderLayout(6, 0));
        bottom.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        bottom.add(new JLabel(">"), BorderLayout.WEST);
        bottom.add(input, BorderLayout.CENTER);

        add(new JScrollPane(output), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        printIntro();
        showMenu();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Update game = new Update();
            game.setVisible(true);
            game.input.requestFocusInWindow();
        });
    }

    // ---------- output helpers ----------

    private void println(String s) {
        output.append(s + "\n");
        output.setCaretPosition(output.getDocument().getLength());
    }

    private void printIntro() {
        println("=========================================");
        println("      THE FOREST OF WHISPERS");
        println("=========================================");
        println("You are a traveler searching for treasure.");
        println("Explore, survive battles, and collect gold.\n");
    }

    private void showMenu() {
        println("\n--- STATUS ---");
        println("Health : " + health);
        println("Potions: " + potions);
        println("Gold   : " + gold);
        println("\nChoose an action:");
        println("1) Explore");
        println("2) Rest");
        println("3) Shop");
        println("4) Quit");
    }

    private void showBattlePrompt() {
        println("\nBattle action: attack / potion / run");
    }

    // ---------- input dispatch ----------

    private void handleInput(String text) {
        switch (state) {
            case MENU:
                handleMenu(text);
                break;
            case BATTLE:
                handleBattle(text);
                break;
            case SHOP:
                handleShop(text);
                break;
            default:
                break;
        }
    }

    private void handleMenu(String choice) {
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
                println("A merchant appears. Buy potion for 15 gold? (yes/no)");
                state = State.SHOP;
                return;
            case "4":
            case "quit":
                println("You leave the forest with " + gold + " gold. Safe travels, adventurer.");
                endGame();
                return;
            default:
                println("Invalid choice. Pick 1, 2, 3, or 4.");
        }

        if (checkDeath()) {
            return;
        }
        if (state == State.MENU) {
            showMenu();
        }
    }

    // ---------- game actions ----------

    private void explore() {
        int event = RANDOM.nextInt(100);

        if (event < 50) {
            startBattle();
        } else if (event < 80) {
            int foundGold = 5 + RANDOM.nextInt(16);
            gold += foundGold;
            println("You found a hidden stash and gained " + foundGold + " gold.");
        } else {
            int damage = 5 + RANDOM.nextInt(11);
            health -= damage;
            println("A trap is triggered! You take " + damage + " damage.");
        }
    }

    private void startBattle() {
        enemyHealth = 25 + RANDOM.nextInt(26);
        println("A wild goblin appears! Enemy health: " + enemyHealth);
        state = State.BATTLE;
        showBattlePrompt();
    }

    private void handleBattle(String action) {
        switch (action) {
            case "attack":
                int playerDamage = 8 + RANDOM.nextInt(13);
                enemyHealth -= playerDamage;
                println("You strike for " + playerDamage + " damage.");
                break;
            case "potion":
                usePotion();
                break;
            case "run":
                if (RANDOM.nextBoolean()) {
                    println("You escaped successfully.");
                    state = State.MENU;
                    showMenu();
                    return;
                } else {
                    println("Escape failed!");
                }
                break;
            default:
                println("Invalid action.");
                showBattlePrompt();
                return;
        }

        if (enemyHealth > 0) {
            int enemyDamage = 6 + RANDOM.nextInt(10);
            health -= enemyDamage;
            println("Goblin hits you for " + enemyDamage + " damage.");
        }

        if (health <= 0) {
            checkDeath();
            return;
        }

        if (enemyHealth <= 0) {
            int reward = 10 + RANDOM.nextInt(16);
            gold += reward;
            println("You defeated the goblin and earned " + reward + " gold!");
            state = State.MENU;
            showMenu();
        } else {
            showBattlePrompt();
        }
    }

    private void rest() {
        int heal = 8 + RANDOM.nextInt(13);
        health = Math.min(100, health + heal);
        println("You rest by a fire and recover " + heal + " health.");
    }

    private void handleShop(String answer) {
        if (!answer.equals("yes")) {
            println("You leave the shop.");
        } else if (gold >= 15) {
            gold -= 15;
            potions++;
            println("You bought a potion. Potions now: " + potions);
        } else {
            println("Not enough gold.");
        }
        state = State.MENU;
        showMenu();
    }

    private void usePotion() {
        if (potions <= 0) {
            println("You have no potions left.");
            return;
        }

        potions--;
        int heal = 20 + RANDOM.nextInt(11);
        health = Math.min(100, health + heal);
        println("You used a potion and healed " + heal + " health.");
    }

    // ---------- end of game ----------

    private boolean checkDeath() {
        if (health <= 0) {
            println("\nYou collapse from your wounds. Game over.");
            println("Final gold: " + gold);
            endGame();
            return true;
        }
        return false;
    }

    private void endGame() {
        state = State.OVER;
        input.setEnabled(false);
        println("\n(Close the window to exit.)");
    }
}