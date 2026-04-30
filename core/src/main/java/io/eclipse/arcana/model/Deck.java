package io.eclipse.arcana.model;

import com.badlogic.gdx.utils.Array;

public class Deck {

    private final Array<Card> cards = new Array<>();

    public void add(Card card) {
        cards.add(card);
    }

    public void shuffle() {
        cards.shuffle();
    }

    public Card draw() {
        if (cards.isEmpty()) return null;
        return cards.removeIndex(cards.size - 1);
    }

    public int size() {
        return cards.size;
    }
}
