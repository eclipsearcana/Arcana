package io.eclipse.arcana.model;

public class Card {

    public enum ArcanaType { MAJOR, MINOR }

    public final String id;
    public final String name;
    public final ArcanaType type;
    public final Suit suit;
    public final int cost;
    public final boolean isExtinction;

    public boolean isRevealed = false;

    public int costModifier = 0;

    public boolean reversed;

    public boolean shouldFlipIllust() {
        if (!reversed) return false;
        if (type == ArcanaType.MAJOR) return true;
        String rank = id.substring(id.lastIndexOf('/') + 1);
        return rank.equals("Page") || rank.equals("Knight")
            || rank.equals("Queen") || rank.equals("King");
    }

    public int effectiveCost() {
        return Math.max(0, cost + costModifier);
    }

    // 광대
    public boolean isCloned = false;
    public boolean isHalfPower = false;

    // 마법사
    public float powerMultiplier = 1.0f;
    public boolean isIllusion = false;

    public Card copy() {
        Card newCard = new Card(this.id, this.name, this.type, this.suit, this.cost, this.isExtinction);
        newCard.isCloned = true;
        return newCard;
    }

    public Card(String id, String name, ArcanaType type, Suit suit, int cost, boolean isExtinction) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.suit = suit;
        this.cost = cost;
        this.isExtinction = isExtinction;
    }
}
