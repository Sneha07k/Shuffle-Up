import java.util.*;

public class AIEngine {

    private final Difficulty difficulty;
    private final Random rng = new Random();

    private final Set<String> seenCards = new HashSet<>();

    public AIEngine(Difficulty d) {
        difficulty = d;
    }

    public void recordPlayedCard(Card c) {
        seenCards.add(key(c));
    }

    public Card chooseLead(List<Card> hand) {
        return switch (difficulty) {
            case EASY -> random(hand);
            case MEDIUM -> mediumLead(hand);
            case HARD -> hardLead(hand);
        };
    }

    public Card chooseResponse(List<Card> hand, Card lead) {
        return switch (difficulty) {
            case EASY -> easyResponse(hand, lead);
            case MEDIUM -> mediumResponse(hand, lead);
            case HARD -> hardResponse(hand, lead);
        };
    }

    private Card easyResponse(List<Card> hand, Card lead) {
        List<Card> same = sameSuit(hand, lead.getSuit());
        return same.isEmpty() ? random(hand) : random(same);
    }

    private Card mediumLead(List<Card> hand) {
        return lowest(hand);
    }

    private Card mediumResponse(List<Card> hand, Card lead) {
        List<Card> same = sameSuit(hand, lead.getSuit());
        if (!same.isEmpty()) {

            Card cheapWinner = cheapestWinner(same, lead);
            return cheapWinner != null ? cheapWinner : lowest(same);
        }

        return highest(hand);
    }

    private Card hardLead(List<Card> hand) {

        for (Card.Suit suit : Card.Suit.values()) {
            List<Card> inSuit = sameSuit(hand, suit);
            if (inSuit.isEmpty())
                continue;
            Card topInHand = highest(inSuit);
            if (isHighestRemaining(topInHand)) {
                return topInHand;
            }
        }

        Card.Suit longest = longestSuit(hand);
        return lowest(sameSuit(hand, longest));
    }

    private Card hardResponse(List<Card> hand, Card lead) {
        List<Card> same = sameSuit(hand, lead.getSuit());

        if (!same.isEmpty()) {

            for (Card c : same) {
                if (c.getValue() > lead.getValue() && isHighestRemaining(c)) {
                    return c;
                }
            }

            Card cheapWinner = cheapestWinner(same, lead);
            if (cheapWinner != null)
                return cheapWinner;

            return lowest(same);
        }

        Card.Suit longest = longestSuit(hand);
        return lowest(sameSuit(hand, longest));
    }

    private boolean isHighestRemaining(Card c) {
        for (Card.Rank r : Card.Rank.values()) {
            if (r.value > c.getValue()) {
                Card higher = new Card(c.getSuit(), r);
                if (!seenCards.contains(key(higher))) {
                    return false;
                }
            }
        }
        return true;
    }

    private Card cheapestWinner(List<Card> candidates, Card lead) {
        Card best = null;
        for (Card c : candidates) {
            if (c.getValue() > lead.getValue()) {
                if (best == null || c.getValue() < best.getValue())
                    best = c;
            }
        }
        return best;
    }

    private List<Card> sameSuit(List<Card> hand, Card.Suit suit) {
        List<Card> res = new ArrayList<>();
        for (Card c : hand)
            if (c.getSuit() == suit)
                res.add(c);
        return res;
    }

    private Card lowest(List<Card> cards) {
        return cards.stream().min(Comparator.comparingInt(Card::getValue)).orElse(cards.get(0));
    }

    private Card highest(List<Card> cards) {
        return cards.stream().max(Comparator.comparingInt(Card::getValue)).orElse(cards.get(0));
    }

    private Card random(List<Card> cards) {
        return cards.get(rng.nextInt(cards.size()));
    }

    private Card.Suit longestSuit(List<Card> hand) {
        Map<Card.Suit, Integer> count = new EnumMap<>(Card.Suit.class);
        for (Card c : hand)
            count.merge(c.getSuit(), 1, Integer::sum);
        return count.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Card.Suit.SPADES);
    }

    private String key(Card c) {
        return c.getSuit() + "_" + c.rank.value;
    }
}
