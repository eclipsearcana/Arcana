package io.eclipse.arcana.model;

public class Card {

    public enum ArcanaType { MAJOR, MINOR }

    public final String id;
    public final String name;
    public final ArcanaType type;
    public final Suit suit;
    public final int cost;
    public final boolean isExtinction;

    public boolean reversed;

    public Card(String id, String name, ArcanaType type, Suit suit, int cost, boolean isExtinction) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.suit = suit;
        this.cost = cost;
        this.isExtinction = isExtinction;
    }
}
