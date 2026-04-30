package io.eclipse.arcana.model;

import com.badlogic.gdx.utils.Array;

public class CardDefinitions {

    public static Array<Card> allMajor() {
        Array<Card> cards = new Array<>();
        // 소멸 카드(1, 13, 16, 21)
        cards.add(major(0,  "The Fool",            1, false));
        cards.add(major(1,  "The Magician",        3, true));
        cards.add(major(2,  "High Priestess",      2, false));
        cards.add(major(3,  "The Empress",         2, false));
        cards.add(major(4,  "The Emperor",         2, false));
        cards.add(major(5,  "The Hierophant",      2, false));
        cards.add(major(6,  "The Lovers",          2, false));
        cards.add(major(7,  "The Chariot",         3, false));
        cards.add(major(8,  "Strength",            2, false));
        cards.add(major(9,  "The Hermit",          2, false));
        cards.add(major(10, "Wheel of Fortune",    3, false));
        cards.add(major(11, "Justice",             2, false));
        cards.add(major(12, "The Hanged Man",      2, false));
        cards.add(major(13, "Death",               4, true));
        cards.add(major(14, "Temperance",          2, false));
        cards.add(major(15, "The Devil",           3, false));
        cards.add(major(16, "The Tower",           4, true));
        cards.add(major(17, "The Star",            2, false));
        cards.add(major(18, "The Moon",            3, false));
        cards.add(major(19, "The Sun",             3, false));
        cards.add(major(20, "Judgement",           3, false));
        cards.add(major(21, "The World",           4, true));
        return cards;
    }

    public static Array<Card> allMinor(Suit suit) {
        Array<Card> cards = new Array<>();
        String s = cap(suit.name());
        cards.add(minor(suit, "A",      s + " A",      1));
        for (int i = 2; i <= 5;  i++) cards.add(minor(suit, "" + i, s + " " + i, i <= 3 ? 1 : 2));
        for (int i = 6; i <= 9;  i++) cards.add(minor(suit, "" + i, s + " " + i, i <= 7 ? 2 : 3));
        cards.add(minor(suit, "10",     s + " 10",     3));
        cards.add(minor(suit, "page",   s + " Page",   2));
        cards.add(minor(suit, "knight", s + " Knight", 3));
        cards.add(minor(suit, "queen",  s + " Queen",  3));
        cards.add(minor(suit, "king",   s + " King",   3));
        return cards;
    }

    private static Card major(int num, String name, int cost, boolean isExtinction) {
        return new Card(
            String.format("major_%02d", num),
            name,
            Card.ArcanaType.MAJOR,
            null,
            cost,
            isExtinction
        );
    }

    private static Card minor(Suit suit, String rankId, String name, int cost) {
        return new Card(
            suit.name().toLowerCase() + "_" + rankId,
            name,
            Card.ArcanaType.MINOR,
            suit,
            cost,
            false
        );
    }

    private static String cap(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}
