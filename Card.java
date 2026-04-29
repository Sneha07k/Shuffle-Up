public class Card implements Comparable<Card> {

    public enum Suit {
        SPADES, HEARTS, DIAMONDS, CLUBS
    }

    public enum Rank {
        TWO(2, "2"), THREE(3, "3"), FOUR(4, "4"), FIVE(5, "5"),
        SIX(6, "6"), SEVEN(7, "7"), EIGHT(8, "8"), NINE(9, "9"),
        TEN(10, "10"), JACK(11, "J"), QUEEN(12, "Q"), KING(13, "K"), ACE(14, "A");

        public final int value;
        public final String symbol;

        Rank(int v, String s) {
            value = v;
            symbol = s;
        }
    }

    public final Suit suit;
    public final Rank rank;

    public Card(Suit s, Rank r) {
        suit = s;
        rank = r;
    }

    public int getValue() {
        return rank.value;
    }

    public Suit getSuit() {
        return suit;
    }

    @Override
    public int compareTo(Card other) {
        int suitCmp = this.suit.ordinal() - other.suit.ordinal();
        if (suitCmp != 0)
            return suitCmp;
        return this.rank.value - other.rank.value;
    }

    @Override
    public String toString() {
        String s = switch (suit) {
            case HEARTS -> "♥";
            case DIAMONDS -> "♦";
            case CLUBS -> "♣";
            case SPADES -> "♠";
        };
        return rank.symbol + s;
    }
}

