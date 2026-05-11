package io.eclipse.arcana.model;

import com.badlogic.gdx.utils.Array;

public class CardDefinitions {

    public static Array<Card> allMajor() {
        Array<Card> cards = new Array<>();
        cards.add(major("Fool",       "The Fool",         3, false));  // 0
        cards.add(major("Magician",   "The Magician",     5, true));   // 1
        cards.add(major("Priestess",  "High Priestess",   2, false));  // 2
        cards.add(major("Empress",    "The Empress",      2, false));  // 3
        cards.add(major("Emperor",    "The Emperor",      4, false));  // 4
        cards.add(major("Hierophant", "The Hierophant",   4, false));  // 5
        cards.add(major("Lovers",     "The Lovers",       3, false));  // 6
        cards.add(major("Chariot",    "The Chariot",      3, false));  // 7
        cards.add(major("Strength",   "Strength",         3, false));  // 8
        cards.add(major("Hermit",     "The Hermit",       4, false));  // 9
        cards.add(major("Fortune",    "Wheel of Fortune", 4, false));  // 10
        cards.add(major("Justice",    "Justice",          3, false));  // 11
        cards.add(major("HangedMan",  "The Hanged Man",   3, false));  // 12
        cards.add(major("Death",      "Death",            6, true));   // 13
        cards.add(major("Temperance", "Temperance",       2, false));  // 14
        cards.add(major("Devil",      "The Devil",        3, false));  // 15
        cards.add(major("Tower",      "The Tower",        4, true));   // 16
        cards.add(major("Star",       "The Star",         4, false));  // 17
        cards.add(major("Moon",       "The Moon",         3, false));  // 18
        cards.add(major("Sun",        "The Sun",          3, false));  // 19
        cards.add(major("Judgement",  "Judgement",        4, false));  // 20
        cards.add(major("World",      "The World",        6, true));   // 21
        return cards;
    }

    public static Array<Card> allMinor(Suit suit) {
        Array<Card> cards = new Array<>();
        String suitName = cap(suit.name()); // "Cups", "Wands" 등

        // id 구조: "Cups/Ace", "Wands/Two" 등
        cards.add(minor(suit, suitName + "/Ace",    "Ace",    1));
        cards.add(minor(suit, suitName + "/Two",    "2",      1));
        cards.add(minor(suit, suitName + "/Three",  "3",      1));
        cards.add(minor(suit, suitName + "/Four",   "4",      1));
        cards.add(minor(suit, suitName + "/Five",   "5",      2));
        cards.add(minor(suit, suitName + "/Six",    "6",      2));
        cards.add(minor(suit, suitName + "/Seven",  "7",      2));
        cards.add(minor(suit, suitName + "/Eight",  "8",      2));
        cards.add(minor(suit, suitName + "/Nine",   "9",      3));
        cards.add(minor(suit, suitName + "/Ten",    "10",     3));
        cards.add(minor(suit, suitName + "/Page",   "Page",   4));
        cards.add(minor(suit, suitName + "/Knight", "Knight", 4));
        cards.add(minor(suit, suitName + "/Queen",  "Queen",  4));
        cards.add(minor(suit, suitName + "/King",   "King",   4));
        return cards;
    }

    private static Card major(String id, String name, int cost, boolean isExtinction) {
        return new Card(id, name, Card.ArcanaType.MAJOR, null, cost, isExtinction);
    }

    private static Card minor(Suit suit, String id, String rankLabel, int cost) {
        String name = cap(suit.name()) + " " + rankLabel;
        return new Card(id, name, Card.ArcanaType.MINOR, suit, cost, false);
    }

    private static String cap(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}
