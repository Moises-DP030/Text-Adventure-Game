package GameUpdateThree;

import java.awt.*;
import java.util.Random;
import javax.swing.*;

public class UpdateThree extends JFrame {

    /**
     * Controls what input commands are valid at a given moment.
     */
    private enum State { MENU, BATTLE, SHOP, OVER }

    /**
     * Tracks the current visual scene so the background can change by situation.
     */
    private enum Scene { CAMP, FOREST, RUINS, SHOP, BATTLE, MINI_BOSS, BIG_BOSS, GAME_OVER }

    /**
     * Defines enemy templates used to build encounters.
     */
    private enum EnemyType {
        WOLF("Feral Wolf", 22, 38, 6, 12, 8, 15, false, false),
        BANDIT("Forest Bandit", 26, 44, 7, 13, 10, 18, false, false),
        SHADE("Whispering Shade", 28, 46, 8, 14, 11, 20, false, false),
        TREANT("Young Treant", 30, 52, 7, 16, 12, 22, false, false),
        MINI_BOSS("Thorn Captain", 56, 76, 10, 18, 24, 38, true, false),
        BIG_BOSS("Ancient Warden", 92, 126, 12, 22, 45, 70, false, true);

        final String name;
        final int minHealth;
        final int maxHealth;
        final int minDamage;
        final int maxDamage;
        final int minGold;
        final int maxGold;
        final boolean miniBoss;
        final boolean bigBoss;

        /**
         * Stores all combat and reward stats for a single enemy template.
         */
        EnemyType(String name, int minHealth, int maxHealth, int minDamage, int maxDamage,
                  int minGold, int maxGold, boolean miniBoss, boolean bigBoss) {
            this.name = name;
            this.minHealth = minHealth;
            this.maxHealth = maxHealth;
            this.minDamage = minDamage;
            this.maxDamage = maxDamage;
            this.minGold = minGold;
            this.maxGold = maxGold;
            this.miniBoss = miniBoss;
            this.bigBoss = bigBoss;
        }
    }

    /**
     * Represents a live enemy in the current battle.
     */
    private static class Enemy {
        String name;
        int health;
        int minDamage;
        int maxDamage;
        int rewardGold;
        boolean miniBoss;
        boolean bigBoss;
    }

    private static final Random RANDOM = new Random();

    private int health = 120;
    private int potions = 3;
    private int gold = 0;
    private int exploreCount = 0;
    private int enemyFocusBonus = 0;

    private State state = State.MENU;
    private Scene scene = Scene.CAMP;
    private Enemy currentEnemy;

    private final JTextArea output = new JTextArea();
    private final JTextField input = new JTextField();
    private final BackgroundPanel rootPanel = new BackgroundPanel();

    /**
     * Builds the game window and starts the opening menu.
     */
    public UpdateThree() {
        super("The Forest of Whispers - Update Three");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(760, 590);
        setLocationRelativeTo(null);
        setResizable(false);

        buildWindow();
        printIntro();
        showMenu();
    }

    /**
     * Launches the game and gives keyboard focus to the input field.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UpdateThree game = new UpdateThree();
            game.setVisible(true);
            game.input.requestFocusInWindow();
        });
    }

    /**
     * Creates all interface elements and wires user input handling.
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
     * Prints one line in the story window and keeps the latest text visible.
     */
    private void println(String s) {
        output.append(s + "\n");
        output.setCaretPosition(output.getDocument().getLength());
    }

    /**
     * Displays the opening narrative and controls hint text.
     */
    private void printIntro() {
        println("=========================================");
        println("      THE FOREST OF WHISPERS");
        println("              UPDATE III");
        println("=========================================");
        println("Travel deeper into the forest, survive escalating threats,");
        println("and defeat the bosses guarding hidden treasure.");
        println("Tip: in battle use attack / heavy attack / potion / run.\n");
    }

    /**
     * Shows player stats and available commands in the main menu.
     */
    private void showMenu() {
        setScene(Scene.CAMP);
        println("\n--- STATUS ---");
        println("Health : " + health + "/120");
        println("Potions: " + potions);
        println("Gold   : " + gold);
        println("Depth  : " + exploreCount + " explorations");
        println("\nChoose an action:");
        println("1) Explore");
        println("2) Rest");
        println("3) Shop");
        println("4) Stats");
        println("5) Quit");
    }

    /**
     * Displays battle commands and current enemy health.
     */
    private void showBattlePrompt() {
        println("\n" + currentEnemy.name + " HP: " + Math.max(0, currentEnemy.health));
        println("Battle action: attack / heavy attack / potion / run");
    }

    /**
     * Routes user input according to the current game state.
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
            case OVER:
            default:
                break;
        }
    }

    /**
     * Handles top-level menu commands such as explore, rest, and shop.
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
                setScene(Scene.SHOP);
                println("A lantern merchant appears from the mist.");
                println("Buy potion for 18 gold? (yes/no)");
                state = State.SHOP;
                return;
            case "4":
            case "stats":
                printDetailedStats();
                break;
            case "5":
            case "quit":
                println("You leave the forest with " + gold + " gold. Safe travels, adventurer.");
                closeGame();
                return;
            default:
                println("Invalid choice. Pick 1, 2, 3, 4, or 5.");
        }

        if (checkDeath()) {
            return;
        }
        if (state == State.MENU) {
            showMenu();
        }
    }

    /**
     * Progresses exploration and picks one of several random world events.
     */
    private void explore() {
        exploreCount++;

        if (exploreCount % 11 == 0) {
            println("The trees go silent. A colossal shadow blocks the moon.");
            startBattle(EnemyType.BIG_BOSS);
            return;
        }

        if (exploreCount % 5 == 0) {
            println("A warhorn echoes through the woods.");
            startBattle(EnemyType.MINI_BOSS);
            return;
        }

        int event = RANDOM.nextInt(100);
        if (event < 42) {
            startBattle(randomStandardEnemy());
        } else if (event < 58) {
            findTreasure();
        } else if (event < 72) {
            triggerTrap();
        } else if (event < 86) {
            shrineEvent();
        } else {
            strangerEvent();
        }
    }

    /**
     * Chooses a regular enemy type for common encounters.
     */
    private EnemyType randomStandardEnemy() {
        EnemyType[] pool = {EnemyType.WOLF, EnemyType.BANDIT, EnemyType.SHADE, EnemyType.TREANT};
        return pool[RANDOM.nextInt(pool.length)];
    }

    /**
     * Starts a battle by instantiating the chosen enemy template.
     */
    private void startBattle(EnemyType type) {
        currentEnemy = createEnemy(type);
        state = State.BATTLE;

        if (currentEnemy.bigBoss) {
            setScene(Scene.BIG_BOSS);
            println("BOSS ENCOUNTER: " + currentEnemy.name + " emerges!");
        } else if (currentEnemy.miniBoss) {
            setScene(Scene.MINI_BOSS);
            println("MINI-BOSS: " + currentEnemy.name + " challenges you!");
        } else {
            setScene(Scene.BATTLE);
            println("An enemy appears: " + currentEnemy.name + "!");
        }

        showBattlePrompt();
    }

    /**
     * Builds a live enemy from a template using randomized health and rewards.
     */
    private Enemy createEnemy(EnemyType type) {
        Enemy e = new Enemy();
        e.name = type.name;
        e.health = randomBetween(type.minHealth, type.maxHealth);
        e.minDamage = type.minDamage;
        e.maxDamage = type.maxDamage;
        e.rewardGold = randomBetween(type.minGold, type.maxGold);
        e.miniBoss = type.miniBoss;
        e.bigBoss = type.bigBoss;
        return e;
    }

    /**
     * Handles all battle commands and enemy turn resolution.
     */
    private void handleBattle(String action) {
        switch (action) {
            case "attack":
                attack(false);
                break;
            case "heavy attack":
                attack(true);
                break;
            case "potion":
                usePotion();
                break;
            case "run":
                attemptEscape();
                return;
            default:
                println("Invalid action.");
                showBattlePrompt();
                return;
        }

        if (currentEnemy.health > 0) {
            enemyTurn();
        }

        if (checkDeath()) {
            return;
        }

        if (currentEnemy.health <= 0) {
            resolveVictory();
        } else {
            showBattlePrompt();
        }
    }

    /**
     * Applies player attack damage, with heavy attacks trading consistency for power.
     */
    private void attack(boolean heavy) {
        int min = heavy ? 10 : 8;
        int max = heavy ? 24 : 17;
        int damage = randomBetween(min, max) + enemyFocusBonus;
        enemyFocusBonus = 0;

        if (heavy && RANDOM.nextInt(100) < 25) {
            println("Heavy attack misses in the fog!");
            return;
        }

        currentEnemy.health -= damage;
        println("You strike " + currentEnemy.name + " for " + damage + " damage.");
    }

    /**
     * Resolves the enemy's attack and occasional special behavior.
     */
    private void enemyTurn() {
        int enemyDamage = randomBetween(currentEnemy.minDamage, currentEnemy.maxDamage);
        if (currentEnemy.bigBoss && RANDOM.nextInt(100) < 28) {
            enemyDamage += 6;
            println(currentEnemy.name + " unleashes a crushing slam!");
        } else if (currentEnemy.miniBoss && RANDOM.nextInt(100) < 22) {
            println(currentEnemy.name + " feints, exposing a weak point for next turn.");
            enemyFocusBonus = 5;
        }

        health -= enemyDamage;
        println(currentEnemy.name + " hits you for " + enemyDamage + " damage.");
    }

    /**
     * Handles escape attempts with lower success against bosses.
     */
    private void attemptEscape() {
        int successRate = currentEnemy.bigBoss ? 10 : currentEnemy.miniBoss ? 28 : 52;
        if (RANDOM.nextInt(100) < successRate) {
            println("You escaped successfully.");
            state = State.MENU;
            setScene(Scene.FOREST);
            showMenu();
        } else {
            println("Escape failed!");
            enemyTurn();
            if (!checkDeath()) {
                showBattlePrompt();
            }
        }
    }

    /**
     * Grants rewards and returns the player to the menu after a win.
     */
    private void resolveVictory() {
        int reward = currentEnemy.rewardGold;
        gold += reward;
        println("You defeated " + currentEnemy.name + " and earned " + reward + " gold!");

        if (currentEnemy.bigBoss) {
            int bonusPotion = 1 + RANDOM.nextInt(2);
            potions += bonusPotion;
            println("Boss cache found: +" + bonusPotion + " potion(s).");
        }

        currentEnemy = null;
        state = State.MENU;
        setScene(Scene.FOREST);
        showMenu();
    }

    /**
     * Executes the treasure scenario with a variable gold reward.
     */
    private void findTreasure() {
        setScene(Scene.RUINS);
        int foundGold = randomBetween(8, 26);
        gold += foundGold;
        println("You discover old stone ruins and recover " + foundGold + " gold.");
    }

    /**
     * Executes a trap scenario that damages the player.
     */
    private void triggerTrap() {
        setScene(Scene.FOREST);
        int damage = randomBetween(7, 15);
        health -= damage;
        println("A hidden snare tightens around your leg. You take " + damage + " damage.");
    }

    /**
     * Executes a shrine scenario that heals or grants a potion.
     */
    private void shrineEvent() {
        setScene(Scene.RUINS);
        if (RANDOM.nextBoolean()) {
            int heal = randomBetween(10, 20);
            health = Math.min(120, health + heal);
            println("A moonlit shrine restores " + heal + " health.");
        } else {
            potions++;
            println("You find an untouched tonic at the shrine. Potions +1.");
        }
    }

    /**
     * Executes a wandering stranger scenario with risk or reward.
     */
    private void strangerEvent() {
        setScene(Scene.FOREST);
        if (RANDOM.nextInt(100) < 60) {
            int gift = randomBetween(6, 14);
            gold += gift;
            println("A cloaked wanderer shares a map fragment. You gain " + gift + " gold.");
        } else {
            int loss = randomBetween(4, 10);
            health -= loss;
            println("A thief ambushes you in the mist. You lose " + loss + " health.");
        }
    }

    /**
     * Restores health and slightly changes the scene atmosphere.
     */
    private void rest() {
        setScene(Scene.CAMP);
        int heal = randomBetween(10, 18);
        health = Math.min(120, health + heal);
        println("You rest by a fire and recover " + heal + " health.");
    }

    /**
     * Handles shop transactions for potion purchases.
     */
    private void handleShop(String answer) {
        if (!answer.equals("yes")) {
            println("You leave the merchant and return to the trail.");
        } else if (gold >= 18) {
            gold -= 18;
            potions++;
            println("You bought a potion. Potions now: " + potions);
        } else {
            println("Not enough gold.");
        }
        state = State.MENU;
        setScene(Scene.CAMP);
        showMenu();
    }

    /**
     * Uses a potion to heal during or outside of battle.
     */
    private void usePotion() {
        if (potions <= 0) {
            println("You have no potions left.");
            return;
        }

        potions--;
        int heal = randomBetween(20, 34);
        health = Math.min(120, health + heal);
        println("You used a potion and healed " + heal + " health.");
    }

    /**
     * Prints an expanded stat block requested from the menu.
     */
    private void printDetailedStats() {
        int miniBossIn = 5 - (exploreCount % 5);
        int bigBossIn = 11 - (exploreCount % 11);
        if (miniBossIn == 0) {
            miniBossIn = 5;
        }
        if (bigBossIn == 0) {
            bigBossIn = 11;
        }

        println("\n--- DETAILED STATS ---");
        println("Health         : " + health + "/120");
        println("Potions        : " + potions);
        println("Gold           : " + gold);
        println("Explorations   : " + exploreCount);
        println("Next Mini Boss : in " + miniBossIn + " explore(s)");
        println("Next Big Boss  : in " + bigBossIn + " explore(s)");
    }

    /**
     * Checks for player death and transitions to game-over logic.
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
     * Ends the game by disabling input and preparing to close the window.
     */
    private void endGame() {
        state = State.OVER;
        setScene(Scene.GAME_OVER);
        input.setEnabled(false);
        println("\n(You can close the window now.)");
        closeGame();
    }

    /**
     * Updates the visual scene and repaints the background panel.
     */
    private void setScene(Scene newScene) {
        scene = newScene;
        rootPanel.repaint();
    }

    /**
     * Returns a random integer within an inclusive range.
     */
    private int randomBetween(int min, int max) {
        return min + RANDOM.nextInt(max - min + 1);
    }

    /**
     * Closes the game window and exits the process.
     */
    private void closeGame() {
        SwingUtilities.invokeLater(() -> {
            dispose();
            System.exit(0);
        });
    }

    /**
     * Draws different painted backgrounds according to the active scene.
     */
    private class BackgroundPanel extends JPanel {
        /**
         * Paints layered gradients and shapes to create themed scene backdrops.
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawSky(g2);
            drawTerrain(g2);
            drawAtmosphere(g2);

            g2.dispose();
        }

        /**
         * Paints the upper gradient and main light source for each scene type.
         */
        private void drawSky(Graphics2D g2) {
            Color top;
            Color bottom;
            Color orb;

            switch (scene) {
                case SHOP:
                    top = new Color(36, 30, 25);
                    bottom = new Color(74, 51, 34);
                    orb = new Color(255, 185, 110, 170);
                    break;
                case BATTLE:
                    top = new Color(26, 21, 34);
                    bottom = new Color(61, 31, 48);
                    orb = new Color(220, 90, 90, 170);
                    break;
                case MINI_BOSS:
                    top = new Color(19, 16, 31);
                    bottom = new Color(67, 27, 54);
                    orb = new Color(255, 120, 90, 180);
                    break;
                case BIG_BOSS:
                    top = new Color(10, 10, 20);
                    bottom = new Color(46, 19, 33);
                    orb = new Color(255, 70, 70, 190);
                    break;
                case RUINS:
                    top = new Color(16, 28, 42);
                    bottom = new Color(38, 68, 75);
                    orb = new Color(183, 222, 200, 170);
                    break;
                case GAME_OVER:
                    top = new Color(12, 12, 12);
                    bottom = new Color(28, 18, 18);
                    orb = new Color(160, 40, 40, 150);
                    break;
                case FOREST:
                    top = new Color(7, 28, 34);
                    bottom = new Color(25, 68, 66);
                    orb = new Color(180, 230, 210, 160);
                    break;
                case CAMP:
                default:
                    top = new Color(9, 22, 33);
                    bottom = new Color(28, 60, 72);
                    orb = new Color(255, 220, 120, 150);
                    break;
            }

            GradientPaint sky = new GradientPaint(0, 0, top, 0, getHeight(), bottom);
            g2.setPaint(sky);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(orb);
            g2.fillOval(getWidth() - 190, 24, 130, 130);
        }

        /**
         * Paints hills, ground, and silhouette layers to frame the scene.
         */
        private void drawTerrain(Graphics2D g2) {
            Color ground;
            Color hill;

            switch (scene) {
                case SHOP:
                    ground = new Color(74, 44, 27);
                    hill = new Color(96, 58, 36);
                    break;
                case BATTLE:
                case MINI_BOSS:
                case BIG_BOSS:
                    ground = new Color(54, 26, 30);
                    hill = new Color(78, 33, 43);
                    break;
                case RUINS:
                    ground = new Color(42, 57, 52);
                    hill = new Color(61, 80, 70);
                    break;
                case GAME_OVER:
                    ground = new Color(32, 20, 20);
                    hill = new Color(52, 30, 30);
                    break;
                case FOREST:
                    ground = new Color(18, 52, 41);
                    hill = new Color(24, 87, 57);
                    break;
                case CAMP:
                default:
                    ground = new Color(16, 58, 48);
                    hill = new Color(31, 102, 66);
                    break;
            }

            g2.setColor(ground);
            g2.fillRect(0, getHeight() - 120, getWidth(), 120);

            g2.setColor(hill);
            int[] hillX = {0, 120, 260, 430, 620, getWidth()};
            int[] hillY = {
                    getHeight() - 120,
                    getHeight() - 220,
                    getHeight() - 150,
                    getHeight() - 210,
                    getHeight() - 180,
                    getHeight() - 120
            };
            g2.fillPolygon(hillX, hillY, hillX.length);
        }

        /**
         * Paints ambient elements like trees, mist, and scene accents.
         */
        private void drawAtmosphere(Graphics2D g2) {
            Color trunks = new Color(17, 42, 35, 180);
            if (scene == Scene.BATTLE || scene == Scene.MINI_BOSS || scene == Scene.BIG_BOSS) {
                trunks = new Color(60, 22, 30, 190);
            } else if (scene == Scene.SHOP) {
                trunks = new Color(94, 58, 34, 180);
            } else if (scene == Scene.GAME_OVER) {
                trunks = new Color(50, 25, 25, 190);
            }

            g2.setColor(trunks);
            for (int i = 0; i < 18; i++) {
                int x = 20 + i * 38;
                int y = 78 + (i % 3) * 18;
                g2.fillRect(x, y, 4, 18);
            }

            if (scene == Scene.SHOP) {
                g2.setColor(new Color(255, 214, 142, 180));
                g2.fillRoundRect(90, getHeight() - 200, 90, 70, 12, 12);
                g2.setColor(new Color(120, 70, 40));
                g2.fillRect(84, getHeight() - 205, 102, 8);
            }
        }
    }
}