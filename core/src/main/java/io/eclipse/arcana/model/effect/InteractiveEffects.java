package io.eclipse.arcana.model.effect;

import com.badlogic.gdx.utils.Array;
import io.eclipse.arcana.model.Card;
import io.eclipse.arcana.model.GameState;
import io.eclipse.arcana.model.Player;

public final class InteractiveEffects {
    private InteractiveEffects() {
    }

    public static class Fool extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            if (caster.hand.size >= io.eclipse.arcana.GameConfig.HAND_MAX) return;
            state.requestCardSelection("The Fool", "복제할 손패 카드 1장을 선택하세요.",
                copy(caster.hand), 1, selected -> {
                    if (selected.size == 0 || caster.hand.size >= io.eclipse.arcana.GameConfig.HAND_MAX) return;
                    Card original = selected.first();
                    Card clone = original.copy();
                    clone.ownerIndex = state.playerIndex(caster);
                    clone.costModifier = -clone.cost;
                    clone.isHalfPower = true;
                    clone.effectMark = Card.EffectMark.SPECIAL;
                    caster.hand.add(clone);
                    state.log("[The Fool] " + original.name + " copied");
                });
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            caster.mustPlayNextDraw = true;
        }
    }

    public static class Magician extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            state.requestCardSelection("The Magician", "계약할 손패 카드 1장을 선택하세요.",
                copy(caster.hand), 1, selected -> {
                    if (selected.size == 0) return;
                    Card card = selected.first();
                    card.costModifier = -card.cost;
                    card.powerMultiplier = 2f;
                    card.effectMark = Card.EffectMark.POSITIVE;
                });
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            if (caster.hand.size >= io.eclipse.arcana.GameConfig.HAND_MAX) return;
            state.requestCardSelection("The Magician Reversed", "환영으로 복제할 카드 1장을 선택하세요.",
                copy(caster.hand), 1, selected -> {
                    if (selected.size == 0 || caster.hand.size >= io.eclipse.arcana.GameConfig.HAND_MAX) return;
                    Card illusion = selected.first().copy();
                    illusion.ownerIndex = state.playerIndex(caster);
                    illusion.isIllusion = true;
                    illusion.costModifier = -illusion.cost;
                    illusion.effectMark = Card.EffectMark.SPECIAL;
                    caster.hand.add(illusion);
                });
        }
    }

    public static class Priestess extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            Array<Card> preview = new Array<>();
            for (int i = 0; i < 3; i++) {
                Card card = caster.deck.draw();
                if (card != null) preview.add(card);
            }

            state.requestCardSelection("High Priestess", "덱 위 카드 중 가져올 카드 1장을 선택하세요.",
                preview, 1, selected -> {
                    Card chosen = selected.size > 0 ? selected.first() : null;
                    for (Card card : preview) {
                        if (card == chosen) state.addDrawnCardToHand(caster, card);
                        else caster.deck.addBottom(card);
                    }
                });
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            Array<Card> choices = copy(caster.hand);
            choices.shuffle();
            for (int i = 0; i < Math.min(2, choices.size); i++) {
                choices.get(i).isRevealed = true;
                choices.get(i).effectMark = Card.EffectMark.NEGATIVE;
            }
        }
    }

    public static class Lovers extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            state.requestCardSelection("The Lovers", "0 코스트로 만들 내 카드 1장을 선택하세요.",
                copy(caster.hand), 1, ownSelected -> {
                    markCostZero(ownSelected);
                    state.requestCardSelection("The Lovers", "0 코스트로 만들 상대 카드 1장을 선택하세요.",
                        copy(target.hand), 1, targetSelected -> markCostZero(targetSelected));
                });
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            Array<Card> choices = copy(caster.hand);
            choices.shuffle();
            while (choices.size > 2) choices.pop();
            state.requestCardSelection("The Lovers Reversed", "강제로 사용할 카드 1장을 선택하세요.",
                choices, 1, selected -> {
                    if (selected.size == 0) return;
                    Card chosen = selected.first();
                    int index = identityIndex(caster.hand, chosen);
                    if (index >= 0) state.forcePlayCardFromHand(caster, index);
                });
        }
    }

    public static class HangedMan extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            int amount = Math.max(1, (int) (caster.hp * 0.1f));
            sacrificeHp(caster, amount);

            int count = caster.hand.size;
            state.discardHand(caster);
            for (int i = 0; i < count; i++) {
                state.drawCardIgnoringLocks(caster);
            }
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            state.requestCardSelection("The Hanged Man Reversed", "이번 턴 손패에 묶을 카드 1장을 선택하세요.",
                copy(caster.hand), 1, selected -> {
                    if (selected.size == 0) return;
                    Card card = selected.first();
                    card.lockedInHand = true;
                    card.effectMark = Card.EffectMark.LOCKED;
                });
        }
    }

    public static class Chariot extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            caster.keepPlayedCardsInHandThisTurn = true;
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            int count = caster.hand.size;
            state.discardHand(caster);
            for (int i = 0; i < count; i++) state.drawCardIgnoringLocks(caster);
        }
    }

    public static class Justice extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            int desired = Math.min(caster.hand.size, io.eclipse.arcana.GameConfig.HAND_MAX);
            while (target.hand.size > desired) state.discardRandomCard(target);
            while (target.hand.size < desired) {
                int before = target.hand.size;
                state.drawCardIgnoringLocks(target);
                if (target.hand.size == before) break;
            }
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            target.effectsNegatedThisTurn = true;
            target.effectCostRefundReceiver = caster;
        }
    }

    public static class World extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            for (Card card : target.hand) card.isRevealed = true;
            if (caster.hand.size >= io.eclipse.arcana.GameConfig.HAND_MAX) return;
            state.requestCardSelection("The World", "가져올 상대 카드 1장을 선택하세요.",
                copy(target.hand), 1, selected -> {
                    if (selected.size == 0) return;
                    Card chosen = selected.first();
                    int index = identityIndex(target.hand, chosen);
                    if (index >= 0) {
                        target.hand.removeIndex(index);
                        chosen.effectMark = Card.EffectMark.SPECIAL;
                        state.addTransferredCardToHand(caster, chosen);
                    }
                });
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            target.nextTurnFixedCost = 3;
        }
    }

    public static class SwordsKnight extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            Array<Card> choices = copy(target.hand);
            choices.shuffle();
            while (choices.size > 2) choices.pop();
            for (Card card : choices) card.isRevealed = true;

            state.requestCardSelection("Knight of Swords", "버릴 공개 카드 1장을 선택하세요.",
                choices, 1, selected -> {
                    if (selected.size == 0) return;
                    Card chosen = selected.first();
                    int index = identityIndex(target.hand, chosen);
                    if (index >= 0) {
                        state.discardCardFromHand(target, chosen);
                    }
                });
        }

        @Override
        public void executeReversed(GameState state, Player caster, Player target) {
            damage(caster, target, 13);
            state.requestCardSelection("Knight of Swords Reversed", "버릴 내 손패 카드 1장을 선택하세요.",
                copy(caster.hand), 1, selected -> {
                    if (selected.size > 0) state.discardCardFromHand(caster, selected.first());
                });
        }
    }

    public static class CupsThree extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            chooseCostReducedCard(state, "Three of Cups", caster, copy(caster.hand), 1);
        }
    }

    public static class CupsSix extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            Array<Card> choices = copy(caster.hand);
            chooseCostReducedCard(state, "Six of Cups", caster, choices, 2);
        }
    }

    public static class PentaclesThree extends BaseCardEffect {
        @Override
        public void executeUpright(GameState state, Player caster, Player target) {
            Array<Card> choices = new Array<>();
            for (Card card : caster.hand) {
                String rank = card.id.substring(card.id.lastIndexOf('/') + 1);
                boolean court = rank.equals("Page") || rank.equals("Knight")
                    || rank.equals("Queen") || rank.equals("King");
                if (card.type == Card.ArcanaType.MINOR && !court) choices.add(card);
            }
            state.requestCardSelection("Three of Pentacles", "0 코스트로 만들 숫자 카드 1장을 선택하세요.",
                chooseUpToThree(choices), 1, selected -> {
                    if (selected.size == 0) return;
                    Card card = selected.first();
                    card.turnCostModifier = -card.cost;
                    card.effectMark = Card.EffectMark.POSITIVE;
                });
        }
    }

    private static Array<Card> copy(Array<Card> cards) {
        Array<Card> result = new Array<>();
        result.addAll(cards);
        return result;
    }

    private static Array<Card> chooseUpToThree(Array<Card> cards) {
        Array<Card> result = copy(cards);
        result.shuffle();
        while (result.size > 3) result.pop();
        return result;
    }

    private static void markCostZero(Array<Card> selected) {
        if (selected.size == 0) return;
        Card card = selected.first();
        card.turnCostModifier = -card.cost;
        card.effectMark = Card.EffectMark.POSITIVE;
    }

    private static void chooseCostReducedCard(GameState state, String title, Player caster,
                                              Array<Card> choices, int remaining) {
        if (remaining <= 0 || choices.size == 0) return;
        state.requestCardSelection(title, "코스트를 1 줄일 카드 1장을 선택하세요.",
            chooseUpToThree(choices), 1, selected -> {
                if (selected.size == 0) return;
                Card card = selected.first();
                card.turnCostModifier -= 1;
                card.effectMark = Card.EffectMark.POSITIVE;
                choices.removeValue(card, true);
                chooseCostReducedCard(state, title, caster, choices, remaining - 1);
            });
    }

    private static int identityIndex(Array<Card> cards, Card target) {
        for (int i = 0; i < cards.size; i++) {
            if (cards.get(i) == target) return i;
        }
        return -1;
    }
}
