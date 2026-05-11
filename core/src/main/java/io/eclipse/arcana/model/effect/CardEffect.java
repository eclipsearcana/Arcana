package io.eclipse.arcana.model.effect;

import io.eclipse.arcana.model.GameState;
import io.eclipse.arcana.model.Player;

public interface CardEffect {
    /** 정방향 효과 */
    void executeUpright(GameState state, Player player, Player target);
    /** 역방향 효과 */
    void executeReversed(GameState state, Player player, Player target);
}
