import java.util.*;

public class Deck {

    private final List<Card> cards = new ArrayList<>();

    public Deck() {
        for (Card.Suit s : Card.Suit.values())
            for (Card.Rank r : Card.Rank.values())
                cards.add(new Card(s, r));
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public List<Card> deal(int n) {
        List<Card> hand = new ArrayList<>(cards.subList(0, n));
        cards.subList(0, n).clear();
        Collections.sort(hand);
        return hand;
    }
}
