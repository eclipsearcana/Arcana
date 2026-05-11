package io.eclipse.arcana.model.effect;

import java.util.HashMap;
import java.util.Map;

public class EffectRegistry {
    private static final Map<String, CardEffect> EFFECTS = new HashMap<>();

    static {
        EFFECTS.put("Wands/Ace", new MinorEffects.Wands.Ace());
    }

    public static CardEffect get(String cardId) {
        return EFFECTS.get(cardId);
    }
}
