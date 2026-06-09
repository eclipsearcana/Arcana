package io.eclipse.arcana.model.effect;

import io.eclipse.arcana.GameConfig;
import io.eclipse.arcana.model.Card;
import io.eclipse.arcana.model.GameState;
import io.eclipse.arcana.model.Player;

import java.util.Random;

public class MajorEffects {
    // 광대: The Fool
    public static class Fool extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            if (caster.hand.size > 0 && caster.hand.size < GameConfig.HAND_MAX) {
                Random rand = new Random();
                int idx = rand.nextInt(caster.hand.size);
                Card original = caster.hand.get(idx);

                // 카드 복제
                Card clone = original.copy();
                clone.ownerIndex = state.playerIndex(caster);

                // 코스트 0 + 효과 절반
                clone.costModifier = -clone.cost;
                clone.isHalfPower = true;

                caster.hand.add(clone);
                state.log("[광대] " + original.name + " 복제 완료 (0코스트)");
            }
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            caster.mustPlayNextDraw = true;
            state.log("[광대 역방향] 다음 드로우 카드는 즉시 강제 사용.");
        }
    }

    // 마법사: The Magician
    public static class Magician extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            if (caster.hand.size > 0 && caster.hand.size < GameConfig.HAND_MAX) {
                Card targetCard = caster.hand.get(new java.util.Random().nextInt(caster.hand.size));

                targetCard.costModifier = -targetCard.cost;
                targetCard.powerMultiplier = 2.0f;

                state.log("[마법사] " + targetCard.name + "와 계약했습니다.");
            }
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            if (caster.hand.size > 0) {
                Card original = caster.hand.get(new java.util.Random().nextInt(caster.hand.size));
                Card illusion = original.copy();
                illusion.ownerIndex = state.playerIndex(caster);

                illusion.isIllusion = true;
                illusion.costModifier = -illusion.cost;

                caster.hand.add(illusion);
                state.log("[마법사 역방향] " + original.name + "의 환영을 생성했습니다.");
            }
        }
    }

    // 고위 여사제: The High Priestess
    public static class Priestness extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            java.util.List<Card> preview = new java.util.ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Card c = caster.deck.draw();
                if (c != null) preview.add(c);
            }

            if (!preview.isEmpty()) {
                state.addDrawnCardToHand(caster, preview.get(0));
                for (int i = 1; i < preview.size(); i++) {
                    caster.deck.addBottom(preview.get(i));
                }
            }

            state.log("[여사제] 덱 상위 3장 중 1장 선택");
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            int count = Math.min(2, caster.hand.size);
            java.util.Random rand = new java.util.Random();

            for (int i = 0; i < count; i++) {
                int idx = rand.nextInt(caster.hand.size);
                Card revealed = caster.hand.get(idx);
                revealed.isRevealed = true;
                state.log("[여사제 역방향] 공개: " + revealed.name);
            }
        }
    }

    // 여황제: The Empress
    public static class Empress extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            int amount = Math.round(GameConfig.PLAYER_HP_START * 0.15f);
            heal(caster, amount);
            state.log("[여황제] HP " + amount + " 회복");
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            caster.costIncreasedOnPlay = true;
            state.log("[여황제 역방향] 이번 턴 카드 사용 시 코스트 1~2 증가");
        }
    }

    // 황제: The Emperor
    public static class Emperor extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            target.nextTurnPlayLimit = 1;
            state.log("[황제] 상대의 다음 턴 카드 사용 수를 1장으로 제한");
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            caster.allCardsCostZeroThisTurn = true;
            caster.randomTargetsThisTurn = true;
            state.log("[황제 역방향] 이번 턴 모든 카드 코스트 0, 타겟 랜덤");
        }
    }

    // 교황: The Hierophant
    public static class Hierophant extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            target.majorBlocked = true;
            state.log("[교황] 상대는 다음 행동 턴 동안 메이저 카드를 사용할 수 없음");
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            caster.effectsSwapped = true;
            state.log("[교황 역방향] 이번 턴 내 카드의 정/역방향 효과가 뒤바뀜");
        }
    }

    // 연인: The Lovers
    public static class Lovers extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            Card casterCard = setRandomCardCostZero(caster);
            Card targetCard = setRandomCardCostZero(target);

            if (casterCard != null) state.log("[연인] 내 " + casterCard.name + " 코스트 0");
            if (targetCard != null) state.log("[연인] 상대 " + targetCard.name + " 코스트 0");
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            if (caster.hand.size == 0) return;

            Random rand = new Random();
            int first = rand.nextInt(caster.hand.size);
            int second = first;
            if (caster.hand.size > 1) {
                do {
                    second = rand.nextInt(caster.hand.size);
                } while (second == first);
            }

            int chosen = (caster.hand.size > 1 && rand.nextBoolean()) ? second : first;
            Card forced = caster.hand.get(chosen);
            state.forcePlayCardFromHand(caster, chosen);
            state.log("[연인 역방향] " + forced.name + " 강제 사용");
        }
    }

    // 전차: The Chariot
    public static class Chariot extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            caster.keepPlayedCardsInHandThisTurn = true;
            state.log("[전차] 이번 턴 이후 사용한 카드는 손패로 유지");
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            int drawCount = caster.hand.size;
            state.discardHand(caster);
            for (int i = 0; i < drawCount; i++) state.drawCardIgnoringLocks(caster);
            state.log("[전차 역방향] 손패 " + drawCount + "장 교체");
        }
    }

    // 힘: Strength
    public static class Strength extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            caster.outgoingDamageBonus += 3;
            caster.lowHpOutgoingDamageBonus += 3;
            state.log("[힘] 이번 턴 데미지 +3, HP 35% 이하이면 추가 +3");
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            caster.effectFailChanceThisTurn = 0.5f;
            state.log("[힘 역방향] 이번 턴 카드 효과가 50% 확률로 실패");
        }
    }

    // 은둔자: The Hermit
    public static class Hermit extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            caster.cannotBeTargeted = true;
            state.log("[은둔자] 상대가 나를 타겟팅할 수 없음");
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            caster.forceNextDrawReversed = true;
            state.log("[은둔자 역방향] 다음 드로우 카드는 모두 역방향");
        }
    }

    // 운명의 수레바퀴: Wheel of Fortune
    public static class Fortune extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            java.util.ArrayList<Card> casterCards = new java.util.ArrayList<>();
            for (Card c : caster.hand) casterCards.add(c);

            caster.hand.clear();
            caster.hand.addAll(target.hand);
            state.refreshTransferredHandGrace(caster);

            target.hand.clear();
            for (Card c : casterCards) target.hand.add(c);
            state.refreshTransferredHandGrace(target);

            state.log("[운명의 수레바퀴] 상대와 손패 교환");
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            caster.mirrorEffectsToSelfThisTurn = true;
            state.log("[운명의 수레바퀴 역방향] 이번 턴 카드 효과가 나에게도 적용");
        }
    }

    // 정의: Justice
    public static class Justice extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            int desiredSize = Math.min(caster.hand.size, GameConfig.HAND_MAX);

            while (target.hand.size > desiredSize) {
                state.discardRandomCard(target);
            }

            while (target.hand.size < desiredSize && target.hand.size < GameConfig.HAND_MAX) {
                int before = target.hand.size;
                state.drawCard(target);
                if (target.hand.size == before) break;
            }

            state.log("[정의] 상대 손패를 " + desiredSize + "장으로 조정");
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            target.effectsNegatedThisTurn = true;
            target.effectCostRefundReceiver = caster;
            state.log("[정의 역방향] 상대 카드 효과 무효화, 코스트 회복");
        }
    }

    // 매달린 사람: The Hanged Man
    public static class HangedMan extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            int amount = Math.max(1, (int) (caster.hp * 0.1f));
            sacrificeHp(caster, amount);
            int cardCount = caster.hand.size;
            state.discardHand(caster);
            for (int i = 0; i < cardCount; i++) state.drawCardIgnoringLocks(caster);

            state.log("[매달린 사람] HP 10% 희생 및 손패 재드로우");
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            com.badlogic.gdx.utils.Array<Card> choices = new com.badlogic.gdx.utils.Array<>();
            choices.addAll(caster.hand);
            choices.shuffle();
            while (choices.size > 3) choices.pop();
            state.requestCardSelection("The Hanged Man Reversed", "이번 턴 손패에 묶을 카드 1장을 선택하세요.",
                choices, 1, selected -> {
                    if (selected.size == 0) return;
                    Card card = selected.first();
                    card.lockedInHand = true;
                    card.effectMark = Card.EffectMark.LOCKED;
                });
        }
    }

    // 죽음: Death
    public static class Death extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            state.resetTableCards();
            state.refillHand(caster, 5);
            state.refillHand(target, 5);
            state.log("[죽음] 테이블 리셋, 양쪽 손패 5장 재드로우");
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            caster.handLockTurns = Math.max(caster.handLockTurns, 2);
            target.handLockTurns = Math.max(target.handLockTurns, 2);
            state.log("[죽음 역방향] 양쪽 모두 2턴 동안 현재 손패로 버팀");
        }
    }

    // 절제: Temperance
    public static class Temperance extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            balanceReversed(caster);
            state.log("[절제] 손패 정/역방향 균형 조정");
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            for (Card c : caster.hand) c.setReversed(!c.reversed, false);
            state.log("[절제 역방향] 손패 정/역방향 반전");
        }
    }

    // 악마: The Devil
    public static class Devil extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            caster.canOverpayCostWithHpThisTurn = true;
            state.log("[악마] 이번 턴 코스트 초과 사용 가능, 초과분 HP 소모");
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            state.clearDebuffs(caster);
            state.log("[악마 역방향] 내 제한/디버프 해제");
        }
    }

    // 탑: The Tower
    public static class Tower extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            target.drawBlocked = true;
            state.log("[탑] 상대 다음 드로우 불가");
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            caster.cost = new Random().nextInt(6) + 1;
            state.log("[탑 역방향] 내 코스트가 " + caster.cost + "로 설정");
        }
    }

    // 별: The Star
    public static class Star extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            caster.drawOnEmptyCostThisTurn = true;
            state.log("[별] 이번 턴 코스트를 전부 소모하면 1장 드로우 및 다음 턴 코스트 2배");
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            caster.fakeShieldTurns = Math.max(caster.fakeShieldTurns, 2);
            caster.fakeShieldBlockedDamage = 0;
            state.log("[별 역방향] 2턴 가짜 보호막 생성");
        }
    }

    // 달: The Moon
    public static class Moon extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            hideHand(target);
            forceRandomCard(state, target);
            state.log("[달] 상대 손패 은폐, 상대 랜덤 1장 강제 사용");
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            revealRandomCards(target, 2);
            state.log("[달 역방향] 상대 손패 중 랜덤 2장 공개");
        }
    }

    // 태양: The Sun
    public static class Sun extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            caster.cost = Math.min(caster.cost + 3, caster.costMax);
            state.log("[태양] 이번 턴 코스트 +3");
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            caster.mustSpendAllCostThisTurn = true;
            state.log("[태양 역방향] 턴 종료 시 남은 코스트만큼 HP 소모");
        }
    }

    // 심판: Judgement
    public static class Judgement extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            reviveRemovedCards(state, caster, 2);
            state.log("[심판] 소멸 카드 최대 2장 부활");
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            caster.delayEffectsThisTurn = true;
            state.log("[심판 역방향] 이번 턴 이후 카드 효과 1턴 지연");
        }
    }

    // 세계: The World
    public static class World extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            for (Card c : target.hand) c.isRevealed = true;

            if (target.hand.size > 0 && caster.hand.size < GameConfig.HAND_MAX) {
                Card stolen = target.hand.removeIndex(new Random().nextInt(target.hand.size));
                state.addTransferredCardToHand(caster, stolen);
                state.log("[세계] 상대 " + stolen.name + " 카드를 가져옴");
            }
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            target.nextTurnFixedCost = 3;
            state.log("[세계 역방향] 상대 다음 턴 시작 코스트 3");
        }
    }

    private static Card setRandomCardCostZero(Player player) {
        if (player.hand.size == 0) return null;

        Card card = player.hand.get(new Random().nextInt(player.hand.size));
        card.costModifier = -card.cost;
        return card;
    }

    private static void balanceReversed(Player player) {
        java.util.ArrayList<Card> cards = new java.util.ArrayList<>();
        for (Card c : player.hand) cards.add(c);
        java.util.Collections.shuffle(cards);

        int reversedTarget = player.hand.size / 2;
        for (int i = 0; i < cards.size(); i++) {
            cards.get(i).setReversed(i < reversedTarget, false);
        }
    }

    private static void hideHand(Player player) {
        for (Card c : player.hand) c.isRevealed = false;
    }

    private static void revealRandomCards(Player player, int count) {
        java.util.ArrayList<Card> hidden = new java.util.ArrayList<>();
        for (Card c : player.hand) {
            if (!c.isRevealed) hidden.add(c);
        }

        java.util.Collections.shuffle(hidden);
        int revealCount = Math.min(count, hidden.size());
        for (int i = 0; i < revealCount; i++) {
            hidden.get(i).isRevealed = true;
        }
    }

    private static void forceRandomCard(GameState state, Player player) {
        if (player.hand.size == 0) return;
        int idx = new Random().nextInt(player.hand.size);
        state.forcePlayCardFromHand(player, idx);
    }

    private static void reviveRemovedCards(GameState state, Player caster, int count) {
        for (int i = 0; i < count; i++) {
            java.util.ArrayList<Player> owners = new java.util.ArrayList<>();
            for (Player player : state.players) {
                if (player.removedCards.size > 0) owners.add(player);
            }
            if (owners.isEmpty()) return;

            Player owner = owners.get(new Random().nextInt(owners.size()));
            Card revived = owner.removedCards.removeIndex(new Random().nextInt(owner.removedCards.size));
            caster.field.add(revived);
        }
    }
}
