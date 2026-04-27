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

    public List<String> log = new ArrayList<>();

    public GameState(List<Card> playerHand, List<Card> computerHand, Turn firstTurn) {
        this.playerHand = new ArrayList<>(playerHand);
        this.computerHand = new ArrayList<>(computerHand);
        this.currentTurn = firstTurn;
        this.phase = Phase.LEAD;
    }

    public void playerWins() {
        playerTricks++;
        currentTurn = Turn.PLAYER;
        clearPlayedCards();
        round++;
    }

    public void computerWins() {
        computerTricks++;
        currentTurn = Turn.COMPUTER;
        clearPlayedCards();
        round++;
    }

    public void playerPicksUp() {
        playerHand.add(playerPlayed);
        playerHand.add(computerPlayed);
        Collections.sort(playerHand);
        currentTurn = Turn.COMPUTER;
        clearPlayedCards();
        round++;
    }

    public void computerPicksUp() {
        computerHand.add(playerPlayed);
        computerHand.add(computerPlayed);
        Collections.sort(computerHand);
        currentTurn = Turn.PLAYER;
        clearPlayedCards();
        round++;
    }

    private void clearPlayedCards() {
        playerPlayed = null;
        computerPlayed = null;
        phase = Phase.LEAD;
    }

    public boolean isGameOver() {
        return playerHand.isEmpty() || computerHand.isEmpty();
    }

    public Turn getWinner() {
        if (playerHand.isEmpty())
            return Turn.PLAYER;
        if (computerHand.isEmpty())
            return Turn.COMPUTER;
        return null;
    }
}
