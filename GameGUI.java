import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Random;

public class GameGUI extends JFrame {

    private static final Color BG = new Color(0x1B4332);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color CARD_BACK = new Color(0x1565C0);
    private static final Color GOLD = new Color(0xFFD700);
    private static final Color MSG_BG = new Color(0x081C15);
    private static final Color LOG_BG = new Color(0x081C15);

    private GameState state;
    private GameEngine engine;
    private Difficulty difficulty = Difficulty.MEDIUM;
    private GameState.Turn trickLeader;

    private boolean interactionLocked = false;

    private JLabel scoreLabel;
    private JPanel cpuHandPanel;
    private JPanel playerHandPanel;
    private JLabel cpuCardLabel;
    private JLabel playerCardLabel;
    private JLabel messageLabel;
    private JTextArea logArea;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameGUI().setVisible(true));
    }

    public GameGUI() {
        setTitle("Card Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 700);
        setMinimumSize(new Dimension(750, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        buildUI();
        showDifficultyDialog();
    }



    private void buildUI() {
        add(buildScoreBar(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildLogPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildScoreBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(MSG_BG);
        bar.setBorder(new EmptyBorder(6, 12, 6, 12));

        scoreLabel = new JLabel("Round 1  |  You: 0 tricks   CPU: 0 tricks", JLabel.CENTER);
        scoreLabel.setForeground(GOLD);
        scoreLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        bar.add(scoreLabel, BorderLayout.CENTER);

        JButton newGame = new JButton("New Game");
        styleButton(newGame);
        newGame.addActionListener(e -> showDifficultyDialog());
        bar.add(newGame, BorderLayout.EAST);

        return bar;
    }

    private JPanel buildCenterPanel() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(BG);
        center.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel cpuLabel = sectionLabel("CPU  Hand");
        cpuHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        cpuHandPanel.setBackground(BG);
        cpuHandPanel.setPreferredSize(new Dimension(860, 90));

        JPanel table = buildTableArea();

        JLabel playerLabel = sectionLabel("Your Hand  (click a card to play)");
        playerHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        playerHandPanel.setBackground(BG);
        playerHandPanel.setPreferredSize(new Dimension(860, 100));

        center.add(cpuLabel);
        center.add(cpuHandPanel);
        center.add(Box.createVerticalStrut(4));
        center.add(table);
        center.add(Box.createVerticalStrut(4));
        center.add(playerLabel);
        center.add(playerHandPanel);

        return center;
    }

    private JPanel buildTableArea() {
        JPanel table = new JPanel(new GridBagLayout());
        table.setBackground(new Color(0x0D2818));
        table.setBorder(BorderFactory.createLineBorder(GOLD, 1));
        table.setPreferredSize(new Dimension(860, 120));
        table.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 20, 6, 20);

        cpuCardLabel = cardSlotLabel("CPU");
        g.gridx = 0; g.gridy = 0;
        table.add(cpuCardLabel, g);

        messageLabel = new JLabel("Press New Game to start", JLabel.CENTER);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));
        messageLabel.setPreferredSize(new Dimension(320, 80));
        g.gridx = 1; g.weightx = 1;
        table.add(messageLabel, g);

        playerCardLabel = cardSlotLabel("YOU");
        g.gridx = 2; g.weightx = 0;
        table.add(playerCardLabel, g);

        return table;
    }

    private JPanel buildLogPanel() {
        logArea = new JTextArea(4, 60);
        logArea.setEditable(false);
        logArea.setBackground(LOG_BG);
        logArea.setForeground(new Color(0xB0BEC5));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBorder(new EmptyBorder(4, 8, 4, 8));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0x2E7D32), 1));
        scroll.setPreferredSize(new Dimension(860, 80));
        return (JPanel) wrapInPanel(scroll);
    }

    private void showDifficultyDialog() {
        String[] opts = { "Easy", "Medium", "Hard" };
        int choice = JOptionPane.showOptionDialog(this,
                "Choose difficulty:", "New Game",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opts, opts[1]);
        if (choice < 0) choice = 1;
        difficulty = Difficulty.values()[choice];
        startNewGame();
    }

    private void startNewGame() {
        interactionLocked = false;

        Deck deck = new Deck();
        deck.shuffle();
        List<Card> pHand = deck.deal(10);
        List<Card> cHand = deck.deal(10);
        GameState.Turn first = new Random().nextBoolean()
                ? GameState.Turn.PLAYER : GameState.Turn.COMPUTER;

        state  = new GameState(pHand, cHand, first);
        engine = new GameEngine(state, new AIEngine(difficulty));
        trickLeader = first;

        logArea.setText("");
        clearTableCards();
        updateScore();

        if (first == GameState.Turn.COMPUTER) {
            interactionLocked = true;
            setMessage("CPU leads first — watch the CPU play.");
            delay(700, e -> doCpuLead());
        } else {
            setMessage("You lead! Click a card to play.");
            refreshPlayerHand(true);
        }
        refreshCpuHand();
    }

    private void doCpuLead() {
        if (state.isGameOver()) { endGame(); return; }

        try {
            trickLeader = GameState.Turn.COMPUTER;
            Card cpuCard = engine.computerLead();
            showCpuCard(cpuCard);

            boolean mustFollow = state.playerHand.stream()
                    .anyMatch(c -> c.getSuit() == cpuCard.getSuit());
            String hint = mustFollow
                    ? "You must follow suit: " + suitSymbol(cpuCard.getSuit())
                    : "No " + suitSymbol(cpuCard.getSuit()) + " — play anything";
            setMessage("CPU played " + cpuCard + ".  " + hint);

            interactionLocked = false; 
            refreshPlayerHand(true);

        } catch (Exception ex) {
            appendLog("[CPU lead error: " + ex.getMessage() + "]");
            interactionLocked = true;
            setMessage("Something went wrong — please start a New Game.");
        }
    }

  
    private void doCpuRespond(Card playerLead) {

        try {
            Card cpuCard = engine.computerRespond(playerLead);
            showCpuCard(cpuCard);
            setMessage("CPU responds with " + cpuCard + ". Resolving…");
            refreshCpuHand();

            delay(900, e -> finishTrick());

        } catch (Exception ex) {
            appendLog("[CPU respond error: " + ex.getMessage() + "]");
            interactionLocked = true;
            setMessage("Something went wrong — please start a New Game.");
        }
    }

  
    private void onPlayerCardClick(Card card) {
        if (interactionLocked) return;
        if (state == null) return;
        if (state.isGameOver() && state.playerPlayed == null && state.computerPlayed == null) return;

        try {
            if (trickLeader == GameState.Turn.PLAYER) {
                interactionLocked = true;
                engine.playerLead(card);
                showPlayerCard(card);
                setMessage("You played " + card + ". Waiting for CPU…");
                refreshPlayerHand(false);
                refreshCpuHand();

                delay(700, e -> doCpuRespond(card));

            } else {
                Card lead = state.computerPlayed;
                if (lead == null) {
                    return;
                }

                if (!engine.isLegalPlay(card, lead)) {
                    setMessage("Must follow suit: " + suitSymbol(lead.getSuit())
                            + "  — pick another card.");
                    return;
                }

                interactionLocked = true;
                engine.playerRespond(card);
                showPlayerCard(card);
                setMessage("You played " + card + ". Resolving…");
                refreshPlayerHand(false);

                delay(700, e -> finishTrick());
            }

        } catch (Exception ex) {
            appendLog("[Play error: " + ex.getMessage() + "]");
            interactionLocked = false;
            refreshPlayerHand(true); 
        }
    }
    private void finishTrick() {
        try {
            String result = engine.resolveTrick(trickLeader);
            appendLog(result);
            updateScore();
            refreshCpuHand();
            refreshPlayerHand(false); 

            if (state.isGameOver()) {
                endGame();
                return;
            }

            delay(1200, e -> startNextTrick());

        } catch (Exception ex) {
            appendLog("[Resolve error: " + ex.getMessage() + "]");
            interactionLocked = true;
            setMessage("Something went wrong — please start a New Game.");
        }
    }

    private void startNextTrick() {
      
        if (state.isGameOver()) { endGame(); return; }

        clearTableCards();
        trickLeader = state.currentTurn;

        if (trickLeader == GameState.Turn.COMPUTER) {
            if (state.computerHand.isEmpty()) { endGame(); return; }

            interactionLocked = true;
            setMessage("CPU leads this round…");
            delay(600, e -> doCpuLead());
        } else {
            if (state.playerHand.isEmpty()) { endGame(); return; }

            interactionLocked = false;
            setMessage("Your turn! Click a card to lead.");
            refreshCpuHand();
            refreshPlayerHand(true);
        }
    }

    private void endGame() {
        interactionLocked = true;
        GameState.Turn winner = state.getWinner();
        String winnerStr = (winner == GameState.Turn.PLAYER) ? "YOU WIN! 🎉" : "CPU wins.";
        setMessage("<html><b>" + winnerStr + "</b>  You: " + state.playerTricks
                + " tricks   CPU: " + state.computerTricks + " tricks</html>");
        refreshPlayerHand(false);
        appendLog("=== GAME OVER — " + winnerStr + " ===");
    }


    private void refreshPlayerHand(boolean clickable) {
        playerHandPanel.removeAll();
        Card cpuLead = (trickLeader == GameState.Turn.COMPUTER) ? state.computerPlayed : null;

        for (Card card : state.playerHand) {
            JLabel lbl = buildCardLabel(card);

            if (clickable) {
                boolean legal = (cpuLead == null) || engine.isLegalPlay(card, cpuLead);
                lbl.setEnabled(legal);

                lbl.addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) {
                        if (lbl.isEnabled() && !interactionLocked)
                            lbl.setBorder(BorderFactory.createLineBorder(GOLD, 2));
                    }
                    @Override public void mouseExited(MouseEvent e) {
                        lbl.setBorder(cardBorder());
                    }
                    @Override public void mouseClicked(MouseEvent e) {
                        if (lbl.isEnabled()) onPlayerCardClick(card);
                    }
                });
            }
            playerHandPanel.add(lbl);
        }
        playerHandPanel.revalidate();
        playerHandPanel.repaint();
    }

    private void refreshCpuHand() {
        cpuHandPanel.removeAll();
        for (int i = 0; i < state.computerHand.size(); i++)
            cpuHandPanel.add(buildCardBack());
        cpuHandPanel.revalidate();
        cpuHandPanel.repaint();
    }

    private void showCpuCard(Card c) {
        cpuCardLabel.setText(cardHtml(c));
        cpuCardLabel.setBorder(cardBorder());
    }

    private void showPlayerCard(Card c) {
        playerCardLabel.setText(cardHtml(c));
        playerCardLabel.setBorder(cardBorder());
    }

    private void clearTableCards() {
        cpuCardLabel.setText(
                "<html><center><font color='#555'>CPU<br>card</font></center></html>");
        cpuCardLabel.setBorder(
                BorderFactory.createDashedBorder(new Color(0x444444), 2, 4, 2, false));
        playerCardLabel.setText(
                "<html><center><font color='#555'>Your<br>card</font></center></html>");
        playerCardLabel.setBorder(
                BorderFactory.createDashedBorder(new Color(0x444444), 2, 4, 2, false));
    }

    private void updateScore() {
        scoreLabel.setText("Round " + state.round
                + "  |  You: " + state.playerTricks + " tricks"
                + "   CPU: " + state.computerTricks + " tricks"
                + "   [" + difficulty + "]");
    }

    private void setMessage(String msg) {
        messageLabel.setText("<html><center>" + msg + "</center></html>");
    }

    private void appendLog(String line) {
        logArea.append(line + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void delay(int ms, java.awt.event.ActionListener action) {
        javax.swing.Timer t = new javax.swing.Timer(ms, action);
        t.setRepeats(false);
        t.start();
    }


    private JLabel buildCardLabel(Card card) {
        JLabel lbl = new JLabel(cardHtml(card), JLabel.CENTER);
        lbl.setPreferredSize(new Dimension(52, 75));
        lbl.setBackground(CARD_BG);
        lbl.setOpaque(true);
        lbl.setBorder(cardBorder());
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return lbl;
    }

    private JLabel buildCardBack() {
        JLabel lbl = new JLabel("", JLabel.CENTER);
        lbl.setPreferredSize(new Dimension(36, 55));
        lbl.setBackground(CARD_BACK);
        lbl.setOpaque(true);
        lbl.setBorder(cardBorder());
        return lbl;
    }

    private JLabel cardSlotLabel(String title) {
        JLabel lbl = new JLabel(
                "<html><center><font color='#555'>" + title + "<br>card</font></center></html>",
                JLabel.CENTER);
        lbl.setPreferredSize(new Dimension(70, 95));
        lbl.setBackground(new Color(0x0D2818));
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createDashedBorder(new Color(0x444444), 2, 4, 2, false));
        return lbl;
    }

    private String cardHtml(Card c) {
        boolean red = c.getSuit() == Card.Suit.HEARTS || c.getSuit() == Card.Suit.DIAMONDS;
        String color = red ? "#C62828" : "#1A1A2E";
        return "<html><center><font color='" + color + "'>"
                + "<b>" + c.rank.symbol + "</b><br>" + suitSymbol(c.getSuit())
                + "</font></center></html>";
    }

    private String suitSymbol(Card.Suit suit) {
        return switch (suit) {
            case HEARTS   -> "♥";
            case DIAMONDS -> "♦";
            case CLUBS    -> "♣";
            case SPADES   -> "♠";
        };
    }

    private Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xBBBBBB), 1),
                new EmptyBorder(2, 4, 2, 4));
    }

    private void styleButton(JButton btn) {
        btn.setBackground(new Color(0x2E7D32));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Monospaced", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(5, 14, 5, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(0xA5D6A7));
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lbl.setBorder(new EmptyBorder(2, 2, 2, 2));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private Component wrapInPanel(Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(LOG_BG);
        p.add(c);
        return p;
    }
}
