package io.eclipse.arcana.model.effect;

import io.eclipse.arcana.model.CardDefinitions;

public class EffectRegistry {
    public static CardEffect get(String cardId) {
        CardEffect effect = CardDefinitions.getMajorEffect(cardId);
        if (effect != null) return effect;

        return CardDefinitions.getMinorEffect(cardId);
    }
}
