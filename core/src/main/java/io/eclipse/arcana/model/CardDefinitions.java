package io.eclipse.arcana.model;

import com.badlogic.gdx.utils.Array;
import io.eclipse.arcana.model.effect.BaseCardEffect;
import io.eclipse.arcana.model.effect.InteractiveEffects;
import io.eclipse.arcana.model.effect.MajorEffects;
import io.eclipse.arcana.model.effect.MinorEffects;

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

    public static BaseCardEffect getMajorEffect(String id) {
        if (id == null || id.contains("/")) return null;

        switch (id) {
            case "Fool":     return new InteractiveEffects.Fool();
            case "Magician": return new InteractiveEffects.Magician();
            case "Priestess": return new InteractiveEffects.Priestess();
            case "Empress":  return new MajorEffects.Empress();
            case "Emperor":  return new MajorEffects.Emperor();
            case "Hierophant": return new MajorEffects.Hierophant();
            case "Lovers":   return new InteractiveEffects.Lovers();
            case "Chariot":  return new InteractiveEffects.Chariot();
            case "Strength": return new MajorEffects.Strength();
            case "Hermit":   return new MajorEffects.Hermit();
            case "Fortune":  return new MajorEffects.Fortune();
            case "Justice":  return new InteractiveEffects.Justice();
            case "HangedMan": return new InteractiveEffects.HangedMan();
            case "Death":    return new MajorEffects.Death();
            case "Temperance": return new MajorEffects.Temperance();
            case "Devil":    return new MajorEffects.Devil();
            case "Tower":    return new MajorEffects.Tower();
            case "Star":     return new MajorEffects.Star();
            case "Moon":     return new MajorEffects.Moon();
            case "Sun":      return new MajorEffects.Sun();
            case "Judgement": return new MajorEffects.Judgement();
            case "World":    return new InteractiveEffects.World();
            default:         return null;
        }
    }

    public static BaseCardEffect getMinorEffect(String id) {
        if (id == null || !id.contains("/")) return null;

        String[] parts = id.split("/");
        String suit = parts[0]; // "Wands", "Cups", "Swords", "Pentacles"
        String rank = parts[1]; // "Ace", "Two", "Three" ... "King"

        switch (suit) {
            case "Wands":
                switch (rank) {
                    case "Ace":    return new MinorEffects.Wands.Ace();
                    case "Two":    return new MinorEffects.Wands.Two();
                    case "Three":  return new MinorEffects.Wands.Three();
                    case "Four":   return new MinorEffects.Wands.Four();
                    case "Five":   return new MinorEffects.Wands.Five();
                    case "Six":    return new MinorEffects.Wands.Six();
                    case "Seven":  return new MinorEffects.Wands.Seven();
                    case "Eight":  return new MinorEffects.Wands.Eight();
                    case "Nine":   return new MinorEffects.Wands.Nine();
                    case "Ten":    return new MinorEffects.Wands.Ten();
                    case "Page":   return new MinorEffects.Wands.Page();
                    case "Knight": return new MinorEffects.Wands.Knight();
                    case "Queen":  return new MinorEffects.Wands.Queen();
                    case "King":   return new MinorEffects.Wands.King();
                }
                break;

            case "Cups":
                switch (rank) {
                    case "Ace":    return new MinorEffects.Cups.Ace();
                    case "Two":    return new MinorEffects.Cups.Two();
                    case "Three":  return new InteractiveEffects.CupsThree();
                    case "Four":   return new MinorEffects.Cups.Four();
                    case "Five":   return new MinorEffects.Cups.Five();
                    case "Six":    return new InteractiveEffects.CupsSix();
                    case "Seven":  return new MinorEffects.Cups.Seven();
                    case "Eight":  return new MinorEffects.Cups.Eight();
                    case "Nine":   return new MinorEffects.Cups.Nine();
                    case "Ten":    return new MinorEffects.Cups.Ten();
                    case "Page":   return new MinorEffects.Cups.Page();
                    case "Knight": return new MinorEffects.Cups.Knight();
                    case "Queen":  return new MinorEffects.Cups.Queen();
                    case "King":   return new MinorEffects.Cups.King();
                }
                break;

            case "Swords":
                switch (rank) {
                    case "Ace":    return new MinorEffects.Swords.Ace();
                    case "Two":    return new MinorEffects.Swords.Two();
                    case "Three":  return new MinorEffects.Swords.Three();
                    case "Four":   return new MinorEffects.Swords.Four();
                    case "Five":   return new MinorEffects.Swords.Five();
                    case "Six":    return new MinorEffects.Swords.Six();
                    case "Seven":  return new MinorEffects.Swords.Seven();
                    case "Eight":  return new MinorEffects.Swords.Eight();
                    case "Nine":   return new MinorEffects.Swords.Nine();
                    case "Ten":    return new MinorEffects.Swords.Ten();
                    case "Page":   return new MinorEffects.Swords.Page();
                    case "Knight": return new InteractiveEffects.SwordsKnight();
                    case "Queen":  return new MinorEffects.Swords.Queen();
                    case "King":   return new MinorEffects.Swords.King();
                }
                break;

            case "Pentacles":
                switch (rank) {
                    case "Ace":    return new MinorEffects.Pentacles.Ace();
                    case "Two":    return new MinorEffects.Pentacles.Two();
                    case "Three":  return new InteractiveEffects.PentaclesThree();
                    case "Four":   return new MinorEffects.Pentacles.Four();
                    case "Five":   return new MinorEffects.Pentacles.Five();
                    case "Six":    return new MinorEffects.Pentacles.Six();
                    case "Seven":  return new MinorEffects.Pentacles.Seven();
                    case "Eight":  return new MinorEffects.Pentacles.Eight();
                    case "Nine":   return new MinorEffects.Pentacles.Nine();
                    case "Ten":    return new MinorEffects.Pentacles.Ten();
                    case "Page":   return new MinorEffects.Pentacles.Page();
                    case "Knight": return new MinorEffects.Pentacles.Knight();
                    case "Queen":  return new MinorEffects.Pentacles.Queen();
                    case "King":   return new MinorEffects.Pentacles.King();
                }
                break;
        }
        return null;
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
