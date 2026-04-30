package io.eclipse.arcana.model;

import com.badlogic.gdx.utils.Array;

public class Player {

    public int hp = 250;
    public Suit chosenSuit;
    public final Deck deck = new Deck();
    public final Array<Card> hand = new Array<>();
    public int reverseStage = 0;
}
