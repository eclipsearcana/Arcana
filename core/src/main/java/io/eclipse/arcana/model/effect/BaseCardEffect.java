package io.eclipse.arcana.model.effect;

import io.eclipse.arcana.GameConfig;
import io.eclipse.arcana.model.Card;
import io.eclipse.arcana.model.GameState;
import io.eclipse.arcana.model.Player;

public abstract class BaseCardEffect implements CardEffect {
    protected int getScaledAmount(Player caster, int amount) {
        if (caster.currentCard == null) return amount;

        Card c = caster.currentCard;
        float multiplier = c.powerMultiplier;

        if (c.isHalfPower) multiplier *= 0.5f;

        if (c.isIllusion) {
            if (new java.util.Random().nextBoolean()) {
                System.out.println("(!) 환영이 연기처럼 사라졌습니다.");
                return 0;
            }
        }

        return Math.max(0, Math.round(amount * multiplier));
    }

    /** 공통 유틸 */
    protected void damage(Player caster, Player target, int amount) {
        int adjustedAmount = amount;
        if (caster != null) {
            adjustedAmount += caster.outgoingDamageBonus;
            if (hpRatio(caster) <= 0.35f) {
                adjustedAmount += caster.lowHpOutgoingDamageBonus;
            }
        }

        // 광대 효과 등 수치 보정 적용
        int scaled = getScaledAmount(caster, adjustedAmount);

        // 2. 방어력/디버프 등 배율 적용
        int finalAmount = Math.round(scaled * target.incomingDamageMultiplier);

        if (target.fakeShieldTurns > 0 && finalAmount > 0) {
            target.fakeShieldBlockedDamage += finalAmount;
            System.out.println("[별 역방향] 가짜 보호막이 피해 " + finalAmount + " 차단");
            return;
        }

        target.hp -= finalAmount;
        target.hp = Math.max(0, target.hp);

        // 반사 로직
        if (target.reflectRatio > 0 && finalAmount > 0 && caster != null) {
            int reflected = Math.round(finalAmount * target.reflectRatio);
            caster.hp = Math.max(0, caster.hp - reflected);
            target.reflectRatio = 0;
        }
    }

    protected void heal(Player caster, int amount) {
        if (caster.healBlocked) return;

        // 회복량도 절반 적용
        int scaled = getScaledAmount(caster, amount);
        int finalHeal = scaled * caster.healMultiplier;

        caster.hp = Math.min(caster.hp + finalHeal, GameConfig.PLAYER_HP_START);
    }

    protected void addCost(Player caster, int amount) {
        // 코스트 수급량도 절반 적용
        int scaled = getScaledAmount(caster, amount);
        caster.cost = Math.min(caster.cost + scaled, caster.costMax);
    }

    protected void reduceCost(Player target, int amount) {
        // 상대 코스트를 깎는 '방해' 수치도 절반 적용할지 선택 가능
        int scaled = getScaledAmount(null, amount); // 시전자 정보가 필요하면 caster 전달
        target.cost = Math.max(0, target.cost - scaled);
    }

    protected float hpRatio(Player player) {
        return (float) player.hp / GameConfig.PLAYER_HP_START;
    }

    @Override
    public void executeReversed(GameState state, Player caster, Player target) {
        executeUpright(state, caster, target);
    }
}
