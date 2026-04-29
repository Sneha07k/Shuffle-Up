import java.util.*;
public class AIEngine {

    private final Difficulty difficulty;
    private final Random rng = new Random();

    private final Set<String> seenCards = new HashSet<>();
    private final Map<Card.Suit, Integer> opponentWeakSuit = new EnumMap<>(Card.Suit.class);

    public AIEngine(Difficulty d) {
        difficulty = d;
    }

    public void recordPlayedCard(Card c) {
        if (c != null)
            seenCards.add(key(c));
    }

    public void recordOpponentMove(Card lead, Card opponentCard) {
        if (lead != null && opponentCard != null &&
                opponentCard.getSuit() != lead.getSuit()) {
            opponentWeakSuit.merge(lead.getSuit(), 1, Integer::sum);
        }
    }

   public Card chooseLead(List<Card> hand) {
    if (hand == null || hand.isEmpty()) return null;
    Card c = switch (difficulty) {
        case EASY -> random(hand);
        case MEDIUM -> hardLead(hand);
        case HARD -> bestMove(hand, null);
    };
    return (c != null) ? c : hand.get(0); 
}

public Card chooseResponse(List<Card> hand, Card lead) {
    if (hand == null || hand.isEmpty()) return null;

    Card c = switch (difficulty) {
        case EASY -> easyResponse(hand, lead);
        case MEDIUM -> hardResponse(hand, lead);
        case HARD -> {
            List<Card> valid = filterValidMoves(hand, lead);
            if (valid.isEmpty()) valid = hand;
            yield bestMove(valid, lead);
        }
    };

    return (c != null) ? c : hand.get(0); 
}

    private Card bestMove(List<Card> hand, Card lead) {
        if (hand == null || hand.isEmpty()) return null;

        Card best = hand.get(0); 
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Card move : hand) {
            double score = evaluateMove(move, hand, lead);
            if (score > bestScore) {
                bestScore = score;
                best = move;
            }
        }

        return best;
    }

    private double evaluateMove(Card move, List<Card> hand, Card lead) {
        double score = 0;

        if (lead != null && move.getSuit() == lead.getSuit()) {
            if (move.getValue() > lead.getValue()) score += 50;
            else score -= 10;
        }

        score -= move.getValue() * 1.5;
        score += probabilityHighest(move) * 40;
        score += simulateFuture(move, hand);

        int suitCount = sameSuit(hand, move.getSuit()).size();
        score += suitCount * 3;

        score += opponentWeakSuit.getOrDefault(move.getSuit(), 0) * 10;

        return score;
    }

    private List<Card> filterValidMoves(List<Card> hand, Card lead) {
        if (lead == null) return hand;

        List<Card> same = sameSuit(hand, lead.getSuit());
        return same.isEmpty() ? hand : same;
    }

    private double simulateFuture(Card move, List<Card> hand) {
        List<Card> newHand = new ArrayList<>(hand);
        newHand.remove(move);

        if (newHand.isEmpty()) return 0;

        int sum = 0;
        for (Card c : newHand) sum += c.getValue();

        return (double) sum / newHand.size();
    }

    private double probabilityHighest(Card c) {
        int higherRemaining = 0;
        int totalUnknown = 52 - seenCards.size();

        for (Card.Rank r : Card.Rank.values()) {
            if (r.value > c.getValue()) {
                Card higher = new Card(c.getSuit(), r);
                if (!seenCards.contains(key(higher))) {
                    higherRemaining++;
                }
            }
        }

        if (totalUnknown == 0) return 1.0;
        return 1.0 - ((double) higherRemaining / totalUnknown);
    }

    private Card easyResponse(List<Card> hand, Card lead) {
        List<Card> same = sameSuit(hand, lead.getSuit());
        return same.isEmpty() ? random(hand) : random(same);
    }

    private Card hardLead(List<Card> hand) {
        for (Card.Suit suit : Card.Suit.values()) {
            List<Card> inSuit = sameSuit(hand, suit);
            if (inSuit.isEmpty()) continue;

            Card top = highest(inSuit);
            if (isHighestRemaining(top)) return top;
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
            if (cheapWinner != null) return cheapWinner;

            return lowest(same);
        }

        Card.Suit longest = longestSuit(hand);
        return lowest(sameSuit(hand, longest));
    }

    private Card cheapestWinner(List<Card> candidates, Card lead) {
        Card best = null;
        for (Card c : candidates) {
            if (c.getValue() > lead.getValue()) {
                if (best == null || c.getValue() < best.getValue()) {
                    best = c;
                }
            }
        }
        return best;
    }

    private List<Card> sameSuit(List<Card> hand, Card.Suit suit) {
        List<Card> res = new ArrayList<>();
        for (Card c : hand) {
            if (c.getSuit() == suit) res.add(c);
        }
        return res;
    }

    private Card lowest(List<Card> cards) {
        if (cards == null || cards.isEmpty()) return null;
        return cards.stream().min(Comparator.comparingInt(Card::getValue)).orElse(cards.get(0));
    }

    private Card highest(List<Card> cards) {
        if (cards == null || cards.isEmpty()) return null;
        return cards.stream().max(Comparator.comparingInt(Card::getValue)).orElse(cards.get(0));
    }

    private Card random(List<Card> cards) {
        if (cards == null || cards.isEmpty()) return null;
        return cards.get(rng.nextInt(cards.size()));
    }

    private Card.Suit longestSuit(List<Card> hand) {
        Map<Card.Suit, Integer> count = new EnumMap<>(Card.Suit.class);
        for (Card c : hand) {
            count.merge(c.getSuit(), 1, Integer::sum);
        }
        return count.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Card.Suit.SPADES);
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

    private String key(Card c) {
        return c.getSuit() + "_" + c.getValue();
    }
}
