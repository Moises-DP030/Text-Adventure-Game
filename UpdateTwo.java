import java.awt.*;
import java.util.Random;
import javax.swing.*;

public class UpdateTwo extends JFrame {

    /**
     * The game has a few major states: menu, battle, shop, and game over.
     * These states control what commands are accepted from the player.
     */
    private enum State { MENU, BATTLE, SHOP, OVER }

    private static final Random RANDOM = new Random();

    private int health = 100;
    private int potions = 2;
    private int gold = 0;
    private int enemyHealth = 0;
    private State state = State.MENU;

    private final JTextArea output = new JTextArea();
    private final JTextField input = new JTextField();
    private final JPanel rootPanel = new BackgroundPanel();

    /**
     * Constructor for the game window.
     * It sets up the frame, builds the custom UI, and starts the story.
     */
    public UpdateTwo() {
        super("The Forest of Whispers");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(720, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        setBackground(new Color(10, 14, 22));

        buildWindow();
        printIntro();
        showMenu();
    }

    /**
     * Starts the game and makes the input box active immediately.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UpdateTwo game = new UpdateTwo();
            game.setVisible(true);
            game.input.requestFocusInWindow();
        });
    }

    /**
     * Creates the custom graphical layout, sets the colors, and adds the action listener.
     */
    private void buildWindow() {
        rootPanel.setLayout(new BorderLayout(8, 8));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("THE FOREST OF WHISPERS", SwingConstants.CENTER);
        title.setFont(new Font(Font.SERIF, Font.BOLD, 28));
        title.setForeground(new Color(245, 238, 220));
        title.setBorder(BorderFactory.createEmptyBorder(8, 0, 12, 0));

        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        output.setOpaque(false);
        output.setForeground(new Color(240, 242, 245));
        output.setBackground(new Color(0, 0, 0, 0));
        output.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(output);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel inputPanel = new JPanel(new BorderLayout(8, 0));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel prompt = new JLabel(">");
        prompt.setForeground(new Color(240, 208, 120));
        prompt.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        prompt.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

        input.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        input.setBackground(new Color(31, 38, 52));
        input.setForeground(new Color(248, 250, 252));
        input.setCaretColor(new Color(248, 250, 252));
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(93, 109, 130), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        input.addActionListener(e -> {
            String text = input.getText().trim().toLowerCase();
            input.setText("");
            if (!text.isEmpty()) {
                println("> " + text);
                handleInput(text);
            }
        });

        inputPanel.add(prompt, BorderLayout.WEST);
        inputPanel.add(input, BorderLayout.CENTER);

        rootPanel.add(title, BorderLayout.NORTH);
        rootPanel.add(scrollPane, BorderLayout.CENTER);
        rootPanel.add(inputPanel, BorderLayout.SOUTH);
        setContentPane(rootPanel);
    }

    /**
     * Adds a line of story text to the output area, so the game reads like a text adventure.
     */
    private void println(String s) {
        output.append(s + "\n");
        output.setCaretPosition(output.getDocument().getLength());
    }

    /**
     * Prints the introductory message for the opening scene.
     */
    private void printIntro() {
        println("=========================================");
        println("      THE FOREST OF WHISPERS");
        println("=========================================");
        println("You are a traveler searching for treasure.");
        println("Explore, survive battles, and collect gold.\n");
    }

    /**
     * Draws the main status menu for the player.
     */
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

    /**
     * Displays the battle prompt when a fight begins.
     */
    private void showBattlePrompt() {
        println("\nBattle action: attack / potion / run");
    }

    /**
     * Handles all text input commands from the user based on the current game state.
     */
    private void handleInput(String text) {
        if (text.equals("quit") || text.equals("q") || text.equals("exit")) {
            println("You leave the forest and the window fades away.");
            closeGame();
            return;
        }

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

    /**
     * Handles menu choices such as exploring, resting, shopping, or quitting.
     */
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
                closeGame();
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

    /**
     * Explores the forest and can trigger treasure, traps, or an enemy encounter.
     */
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

    /**
     * Starts a battle encounter against a goblin and updates the state.
     */
    private void startBattle() {
        enemyHealth = 25 + RANDOM.nextInt(26);
        println("A wild goblin appears! Enemy health: " + enemyHealth);
        state = State.BATTLE;
        showBattlePrompt();
    }

    /**
     * Handles combat actions during battle: attack, use potion, or run.
     */
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

    /**
     * Restores some health to the player, but does not change the main game state.
     */
    private void rest() {
        int heal = 8 + RANDOM.nextInt(13);
        health = Math.min(100, health + heal);
        println("You rest by a fire and recover " + heal + " health.");
    }

    /**
     * Handles the shop prompt, allowing the player to buy a potion if they have enough gold.
     */
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

    /**
     * Uses a health potion and increases the player's health if a potion is available.
     */
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

    /**
     * Checks whether the player has died and ends the game if their health reaches zero.
     */
    private boolean checkDeath() {
        if (health <= 0) {
            println("\nYou collapse from your wounds. Game over.");
            println("Final gold: " + gold);
            endGame();
            return true;
        }
        return false;
    }

    /**
     * Ends the current game by disabling further input and closing the entire window.
     */
    private void endGame() {
        state = State.OVER;
        input.setEnabled(false);
        println("\n(You can close the window now.)");
        closeGame();
    }

    /**
     * Closes the game window after a quit command or a finished game.
     * This ensures the entire window closes when the user types to quit.
     */
    private void closeGame() {
        SwingUtilities.invokeLater(() -> {
            dispose();
            System.exit(0);
        });
    }

    /**
     * Custom background panel that paints a forest-like scene behind the text and input panel.
     */
    private static class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            GradientPaint sky = new GradientPaint(0, 0, new Color(9, 22, 33), 0, getHeight(), new Color(28, 60, 72));
            g2.setPaint(sky);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(255, 220, 120, 150));
            g2.fillOval(getWidth() - 180, 30, 120, 120);

            g2.setColor(new Color(18, 52, 41));
            g2.fillRect(0, getHeight() - 120, getWidth(), 120);

            g2.setColor(new Color(24, 87, 57));
            int[] hillX = {0, 120, 260, 430, 620, getWidth()};
            int[] hillY = {getHeight() - 120, getHeight() - 220, getHeight() - 150, getHeight() - 210, getHeight() - 180, getHeight() - 120};
            g2.fillPolygon(hillX, hillY, hillX.length);

            g2.setColor(new Color(17, 42, 35, 180));
            for (int i = 0; i < 18; i++) {
                int x = 20 + i * 37;
                int y = 80 + (i % 3) * 18;
                g2.fillRect(x, y, 3, 16);
            }

            g2.dispose();
        }
    }
}
