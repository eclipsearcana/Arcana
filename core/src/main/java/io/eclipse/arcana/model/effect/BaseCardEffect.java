package io.eclipse.arcana.model.effect;

import io.eclipse.arcana.GameConfig;
import io.eclipse.arcana.model.Card;
import io.eclipse.arcana.model.GameState;
import io.eclipse.arcana.model.Player;

public abstract class BaseCardEffect implements CardEffect {
    protected int getScaledAmount(Player caster, int amount) {
        if (caster == null || caster.currentCard == null) return amount;

        Card card = caster.currentCard;
        float multiplier = card.powerMultiplier;

        if (card.isHalfPower) multiplier *= 0.5f;

        return Math.max(0, Math.round(amount * multiplier));
    }

    protected void damage(Player caster, Player target, int amount) {
        int adjustedAmount = amount;
        if (caster != null) {
            adjustedAmount += caster.outgoingDamageBonus;
            if (hpRatio(caster) <= 0.35f) {
                adjustedAmount += caster.lowHpOutgoingDamageBonus;
            }
        }

        int scaled = getScaledAmount(caster, adjustedAmount);
        int finalAmount = Math.round(scaled * target.incomingDamageMultiplier);

        if (target.fakeShieldTurns > 0 && finalAmount > 0) {
            target.fakeShieldBlockedDamage += finalAmount;
            GameState.logActive("[별 역방향] 가짜 보호막이 피해 " + finalAmount + " 차단");
            return;
        }

        int beforeHp = target.hp;
        target.hp = Math.max(0, target.hp - finalAmount);
        GameState.logActive("[피해] " + playerLabel(caster) + " -> " + playerLabel(target)
            + " " + finalAmount + " 피해 (" + beforeHp + " -> " + target.hp + ")");

        if (target.reflectRatio > 0 && finalAmount > 0 && caster != null && caster != target) {
            int reflected = Math.round(finalAmount * target.reflectRatio);
            int casterBeforeHp = caster.hp;
            caster.hp = Math.max(0, caster.hp - reflected);
            target.reflectRatio = 0;
            GameState.logActive("[반사] " + playerLabel(target) + " -> " + playerLabel(caster)
                + " " + reflected + " 피해 (" + casterBeforeHp + " -> " + caster.hp + ")");
        }
    }

    protected void heal(Player caster, int amount) {
        if (caster.healBlocked) {
            caster.healBlocked = false;
            GameState.logActive("[회복 차단] " + playerLabel(caster));
            return;
        }

        int scaled = getScaledAmount(caster, amount);
        int finalHeal = scaled * caster.healMultiplier;

        int beforeHp = caster.hp;
        caster.hp = Math.min(caster.hp + finalHeal, GameConfig.PLAYER_HP_START);
        GameState.logActive("[회복] " + playerLabel(caster)
            + " " + finalHeal + " 회복 (" + beforeHp + " -> " + caster.hp + ")");
    }

    protected void addCost(Player caster, int amount) {
        int scaled = getScaledAmount(caster, amount);
        int beforeCost = caster.cost;
        caster.cost = Math.min(caster.cost + scaled, caster.costMax);
        GameState.logActive("[코스트 증가] " + playerLabel(caster)
            + " +" + scaled + " (" + beforeCost + " -> " + caster.cost + ")");
    }

    protected void growCost(Player caster, int amount) {
        int scaled = getScaledAmount(caster, amount);
        int beforeCost = caster.cost;
        int beforeInit = caster.costInit;
        caster.costInit = Math.min(caster.costInit + scaled, caster.costMax);
        caster.cost = Math.min(caster.cost + scaled, caster.costMax);
        GameState.logActive("[COST GROWTH] " + playerLabel(caster)
            + " current " + beforeCost + " -> " + caster.cost
            + " / base " + beforeInit + " -> " + caster.costInit);
    }

    protected void reduceCost(Player target, int amount) {
        int scaled = getScaledAmount(null, amount);
        int beforeCost = target.cost;
        target.cost = Math.max(0, target.cost - scaled);
        GameState.logActive("[코스트 감소] " + playerLabel(target)
            + " -" + scaled + " (" + beforeCost + " -> " + target.cost + ")");
    }

    protected float hpRatio(Player player) {
        return (float) player.hp / GameConfig.PLAYER_HP_START;
    }

    protected void sacrificeHp(Player player, int amount) {
        int beforeHp = player.hp;
        player.hp = Math.max(0, player.hp - Math.max(0, amount));
        GameState.logActive("[HP sacrifice] " + playerLabel(player)
            + " " + amount + " (" + beforeHp + " -> " + player.hp + ")");
    }

    private String playerLabel(Player player) {
        GameState state = GameState.activeLogState();
        if (state == null || player == null) return "Unknown";
        return state.playerLabel(player);
    }

    @Override
    public void executeReversed(GameState state, Player caster, Player target) {
        executeUpright(state, caster, target);
    }
}
