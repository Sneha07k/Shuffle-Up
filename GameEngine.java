import java.util.*;

public class GameEngine {

    private final GameState state;
    private final AIEngine ai;

    public GameEngine(GameState state, AIEngine ai) {
        this.state = state;
        this.ai = ai;
    }

    public boolean isLegalPlay(Card card, Card lead) {
        if (lead == null) return true;

        boolean hasSuit = state.playerHand.stream()
                .anyMatch(c -> c.getSuit() == lead.getSuit());

        if (hasSuit)
            return card.getSuit() == lead.getSuit();

        return true;
    }

    public void playerLead(Card card) {
        if (state.phase != GameState.Phase.LEAD)
            throw new IllegalStateException("Not in LEAD phase (phase=" + state.phase + ")");

        state.playerHand.remove(card);
        state.playerPlayed = card;
        state.phase = GameState.Phase.RESPOND;
        state.currentTurn = GameState.Turn.COMPUTER;

        ai.recordPlayedCard(card);
    }

    public void playerRespond(Card card) {
        if (state.phase != GameState.Phase.RESPOND || state.currentTurn != GameState.Turn.PLAYER)
            throw new IllegalStateException("Invalid player respond timing (phase=" + state.phase
                    + ", turn=" + state.currentTurn + ")");

        state.playerHand.remove(card);
        state.playerPlayed = card;

        ai.recordPlayedCard(card);
    }

    public Card computerLead() {
        if (state.phase != GameState.Phase.LEAD || state.currentTurn != GameState.Turn.COMPUTER)
            throw new IllegalStateException("Invalid computer lead timing (phase=" + state.phase
                    + ", turn=" + state.currentTurn + ")");

        if (state.computerHand.isEmpty())
            throw new IllegalStateException("Computer hand is empty — cannot lead");

        Card chosen = ai.chooseLead(state.computerHand);
        if (chosen == null)
            chosen = state.computerHand.get(0);

        state.computerHand.remove(chosen);
        state.computerPlayed = chosen;
        state.phase = GameState.Phase.RESPOND;
        state.currentTurn = GameState.Turn.PLAYER;

        ai.recordPlayedCard(chosen);
        return chosen;
    }

    public Card computerRespond(Card lead) {
        if (state.phase != GameState.Phase.RESPOND || state.currentTurn != GameState.Turn.COMPUTER)
            throw new IllegalStateException("Invalid computer respond timing (phase=" + state.phase
                    + ", turn=" + state.currentTurn + ")");

        if (lead == null)
            throw new IllegalStateException("Lead card is null");

        if (state.computerHand.isEmpty())
            throw new IllegalStateException("Computer hand is empty — cannot respond");

        Card chosen = ai.chooseResponse(state.computerHand, lead);
        if (chosen == null)
            chosen = state.computerHand.get(0);

        state.computerHand.remove(chosen);
        state.computerPlayed = chosen;

        ai.recordPlayedCard(chosen);
        ai.recordOpponentMove(lead, chosen);
        return chosen;
    }

    public String resolveTrick(GameState.Turn leader) {

        Card p = state.playerPlayed;
        Card c = state.computerPlayed;

        if (p == null || c == null)
            throw new IllegalStateException("Cannot resolve: a played card is null (p=" + p + ", c=" + c + ")");

      
        if (leader == GameState.Turn.PLAYER) {
            ai.recordOpponentMove(p, c); 
        } else {
            ai.recordOpponentMove(c, p); 
        }
        String result;

        if (p.getSuit() == c.getSuit()) {
            if (p.getValue() > c.getValue()) {
                state.playerWins();
                result = "You win (" + p + " beats " + c + ")";
            } else if (p.getValue() < c.getValue()) {
                state.computerWins();
                result = "CPU wins (" + c + " beats " + p + ")";
            } else {
                state.playerWins();
                result = "Tie — you keep the trick";
            }

        } else if (leader == GameState.Turn.PLAYER) {
            state.computerPicksUp();
            result = "CPU picks up (played off-suit)";

        } else {
            state.playerPicksUp();
            result = "You pick up (played off-suit)";
        }

        state.clearPlayedCards();
        state.log.add("Round " + state.round + ": " + result);

        return result;
    }
}