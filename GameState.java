import java.util.*;

public class GameState {

    public enum Turn {
        PLAYER, COMPUTER
    }

    public enum Phase {
        LEAD, RESPOND
    }

    public List<Card> playerHand;
    public List<Card> computerHand;

    public Turn currentTurn;
    public Phase phase;

    public Card playerPlayed;
    public Card computerPlayed;

    public int playerTricks = 0;
    public int computerTricks = 0;
    public int round = 1;

    public Turn lastTrickWinner = null;

    public List<String> log = new ArrayList<>();

    public GameState(List<Card> playerHand, List<Card> computerHand, Turn firstTurn) {
        this.playerHand = new ArrayList<>(playerHand);
        this.computerHand = new ArrayList<>(computerHand);
        this.currentTurn = firstTurn;
        this.phase = Phase.LEAD;
    }

    public void playerWins() {
        playerTricks++;
        lastTrickWinner = Turn.PLAYER;
        currentTurn = Turn.PLAYER;
        round++;
    }

    public void computerWins() {
        computerTricks++;
        lastTrickWinner = Turn.COMPUTER;
        currentTurn = Turn.COMPUTER;
        round++;
    }

    public void playerPicksUp() {
        if (playerPlayed != null)
            playerHand.add(playerPlayed);
        if (computerPlayed != null)
            playerHand.add(computerPlayed);

        Collections.sort(playerHand);
        currentTurn = Turn.COMPUTER;
        round++;
    }

    public void computerPicksUp() {
        if (playerPlayed != null)
            computerHand.add(playerPlayed);
        if (computerPlayed != null)
            computerHand.add(computerPlayed);

        Collections.sort(computerHand);
        currentTurn = Turn.PLAYER;
        round++;
    }

    public void clearPlayedCards() {
        playerPlayed = null;
        computerPlayed = null;
        phase = Phase.LEAD;
    }

    public boolean isGameOver() {
        return playerHand.isEmpty() || computerHand.isEmpty();
    }

    public Turn getWinner() {
        boolean pEmpty = playerHand.isEmpty();
        boolean cEmpty = computerHand.isEmpty();

        if (pEmpty && cEmpty) {
            if (playerTricks > computerTricks) return Turn.PLAYER;
            if (computerTricks > playerTricks) return Turn.COMPUTER;
            return lastTrickWinner != null ? lastTrickWinner : Turn.PLAYER;
        }

        if (pEmpty) return Turn.PLAYER;
        if (cEmpty) return Turn.COMPUTER;
        return null; 
    }
}
