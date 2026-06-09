package io.eclipse.arcana.model.ai;

import com.badlogic.gdx.utils.Array;

import io.eclipse.arcana.model.Card;

public class BasicAiTurnPlan {
    public final Array<Card> cardsToPlay = new Array<>();
    public int expectedScore;
}
