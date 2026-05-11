package io.eclipse.arcana.model.effect;

import io.eclipse.arcana.GameConfig;
import io.eclipse.arcana.model.GameState;
import io.eclipse.arcana.model.Player;

public abstract class BaseCardEffect implements CardEffect {
    /** 공통 유틸 */
    protected void damage(Player caster, Player target, int amount) {
        target.hp -= amount;
        target.hp = Math.max(0, target.hp);

        if (target.reflectDamage > 0 && amount > 0 && caster != null) {
            caster.hp -= target.reflectDamage;
            caster.hp = Math.max(0, caster.hp);
        }
    }

    protected void heal(Player caster, int amount) {
        caster.hp = Math.min(caster.hp + amount, 250);
    }

    protected void addCost(Player caster, int amount) {
        caster.cost = Math.min(caster.cost + amount, caster.costMax);
    }

    protected void reduceCost(Player target, int amount) {
        target.cost = Math.max(0, target.cost - amount);
    }

    protected float hpRatio(Player player) {
        return (float) player.hp / GameConfig.PLAYER_HP_START;
    }

    @Override
    public void executeReversed(GameState state, Player caster, Player target) {
        executeUpright(state, caster, target);
    }
}
