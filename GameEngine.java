import java.util.Collections;

public class GameEngine {

    private final GameState state;
    private final AIEngine ai;

    public GameEngine(GameState state, AIEngine ai) {
        this.state = state;
        this.ai = ai;
    }

    public void playerLead(Card card) {
        state.playerHand.remove(card);
        state.playerPlayed = card;
        state.phase = GameState.Phase.RESPOND;
        ai.recordPlayedCard(card);
    }

    public void playerRespond(Card card) {
        state.playerHand.remove(card);
        state.playerPlayed = card;
        ai.recordPlayedCard(card);
    }

    public Card computerLead() {
        Card chosen = ai.chooseLead(state.computerHand);
        state.computerHand.remove(chosen);
        state.computerPlayed = chosen;
        state.phase = GameState.Phase.RESPOND;
        ai.recordPlayedCard(chosen);
        return chosen;
    }

    public Card computerRespond(Card lead) {
        Card chosen = ai.chooseResponse(state.computerHand, lead);
        state.computerHand.remove(chosen);
        state.computerPlayed = chosen;
        ai.recordPlayedCard(chosen);
        return chosen;
    }

    public boolean isLegalPlay(Card card, Card lead) {

        boolean hasSuit = state.playerHand.stream()
                .anyMatch(c -> c.getSuit() == lead.getSuit());
        if (hasSuit)
            return card.getSuit() == lead.getSuit();
        return true;
    }

    public String resolveTrick(GameState.Turn leader) {
        Card p = state.playerPlayed;
        Card c = state.computerPlayed;
        String result;

        if (p.getSuit() == c.getSuit()) {

            int cmp = p.getValue() - c.getValue();
            if (cmp > 0) {
                state.playerWins();
                result = "You win the trick  (" + p + " beats " + c + ")";
            } else if (cmp < 0) {
                state.computerWins();
                result = "CPU wins the trick  (" + c + " beats " + p + ")";
            } else {

                state.playerWins();
                result = "Tie — both cards discarded";
            }
        } else if (leader == GameState.Turn.PLAYER) {

            state.computerPicksUp();
            result = "CPU had no " + p.getSuit() + " — CPU picks up both cards";
        } else {

            state.playerPicksUp();
            result = "You had no " + c.getSuit() + " — You pick up both cards";
        }

        state.log.add("Round " + (state.round - 1) + ": " + result);
        return result;
    }
}
