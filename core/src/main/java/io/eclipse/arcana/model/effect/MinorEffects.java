package io.eclipse.arcana.model.effect;

import com.badlogic.gdx.utils.Array;
import io.eclipse.arcana.model.*;

import java.util.Random;

public class MinorEffects {
    // Wands
    public static class Wands {
        public static class Ace extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 5);
            }
        }

        public static class Two extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 4);
                caster.nextTurnCostModifier += 1;
            }
        }

        public static class Three extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 4);
                state.drawCard(caster);
            }
        }

        public static class Four extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 3);
                heal(caster, 5);
            }
        }

        public static class Five extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 8);
                target.nextTurnDrawModifier -= 1;
            }
        }

        public static class Six extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 8);
                addCost(caster, 1);
            }
        }

        public static class Seven extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 8);

                caster.reflectRatio = 0.3f;
            }
        }

        public static class Eight extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 6);
                damage(caster, target, 6);
            }
        }

        public static class Nine extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                int base = 12;
                int bonus = (int)((1f - hpRatio(caster)) * 12);
                damage(caster, target, base + bonus);
            }
        }

        public static class Ten extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                int bonus = caster.wandsPlayedThisTurn * 3;
                damage(caster, target, 12 + bonus);
            }
        }

        public static class Page extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                for (Card c : caster.hand) {
                    if (c.suit == Suit.WANDS) c.costModifier -= 1;
                }
            }

            @Override
            public void executeReversed(GameState state, Player caster, Player target) {
                for (Card c : caster.hand) {
                    if (c.suit == Suit.WANDS) c.costModifier += 1;
                }
            }
        }

        public static class Knight extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 14);
                damage(caster, caster, 10);
            }

            @Override
            public void executeReversed(GameState state, Player caster, Player target) {
                damage(caster, target, 20);
                damage(caster, caster, 25);
            }
        }

        public static class Queen extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 13);
                for (Card c : caster.hand) {
                    if (c.suit == Suit.WANDS) c.costModifier -= 1;
                }
            }

            @Override
            public void executeReversed(GameState state, Player caster, Player target) {
                damage(caster, target, 17);
                damage(caster, caster, 17);
            }
        }

        public static class King extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                for (Card c : caster.field) {
                    if (c.suit == Suit.WANDS && !c.id.endsWith("/King")) {
                        CardEffect effect = EffectRegistry.get(c.id);
                        if (effect != null) {
                            effect.executeUpright(state, caster, target);
                        }
                    }
                }
            }

            @Override
            public void executeReversed(GameState state, Player caster, Player target) {
                for (int i = caster.field.size - 1; i >= 0; i--) {
                    Card lastCard = caster.field.get(i);

                    if (lastCard.suit == Suit.WANDS && !lastCard.id.endsWith("/King")) {
                        CardEffect effect = EffectRegistry.get(lastCard.id);
                        if (effect != null) {
                            effect.executeUpright(state, caster, target);
                        }
                        break;
                    }
                }
            }
        }
    }

    // Cups
    public static class Cups {
        public static class Ace extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                heal(caster, 7);
            }
        }

        public static class Two extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                heal(caster, 6);
                state.drawCard(caster);
            }
        }

        public static class Three extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                if (caster.hand.size > 0) {
                    Random rand = new Random();
                    caster.hand.get(rand.nextInt(caster.hand.size)).costModifier -= 1;
                }
            }
        }

        public static class Four extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 3);
                caster.nextTurnDrawModifier += 1;
            }
        }

        public static class Five extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                heal(caster, 11);
                damage(caster, target, 5);
            }
        }

        public static class Six extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                int size = caster.hand.size;

                if (size == 0) return;

                if (size == 1) {
                    caster.hand.get(0).costModifier -= 1;
                    return;
                }

                Random rand = new Random();
                int idx1 = rand.nextInt(size);
                int idx2;

                do {
                    idx2 = rand.nextInt(size);
                } while (idx1 == idx2);

                caster.hand.get(idx1).costModifier -= 1;
                caster.hand.get(idx2).costModifier -= 1;
            }
        }

        public static class Seven extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                for (int i = 0; i < 3; i++) {
                    state.drawCard(caster);
                }
            }
        }

        public static class Eight extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                heal(caster, 13);
                target.nextTurnDrawModifier -= 1;
            }
        }

        public static class Nine extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                heal(caster, 15);
                addCost(caster, 1);
            }
        }

        public static class Ten extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                int bonus = caster.cupsPlayedThisTurn * 4;
                heal(caster, bonus);
            }
        }

        public static class Page extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                for (Card c : caster.hand) {
                    if (c.suit == Suit.CUPS) c.costModifier -= 1;
                }
            }

            @Override
            public void executeReversed(GameState state, Player caster, Player target) {
                for (Card c : caster.hand) {
                    if (c.suit == Suit.CUPS) c.costModifier += 1;
                }
            }
        }

        public static class Knight extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                caster.healMultiplier = 2;
            }

            @Override
            public void executeReversed(GameState state, Player caster, Player target) {
                caster.healMultiplier = 2;
                for (int i = 0; i < 2; i++) {
                    if (caster.hand.size > 0) caster.hand.removeIndex(new Random().nextInt(caster.hand.size));
                }
            }
        }

        public static class Queen extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 8);
                heal(caster, 7);
            }

            @Override
            public void executeReversed(GameState state, Player caster, Player target) {
                damage(caster, target, 11);
                target.healBlocked = true;
            }
        }

        public static class King extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                for (Card c : caster.field) {
                    if (c.suit == Suit.CUPS && !c.id.endsWith("/King")) {
                        if (c.id.endsWith("/Ace")) heal(caster, 7);
                        else if (c.id.endsWith("/Two")) heal(caster, 6);
                        else if (c.id.endsWith("/Five")) heal(caster, 11);
                        else if (c.id.endsWith("/Eight")) heal(caster, 13);
                        else if (c.id.endsWith("/Nine")) heal(caster, 15);
                        else if (c.id.endsWith("/Ten")) heal(caster, caster.cupsPlayedThisTurn * 4);
                        else if (c.id.endsWith("/Queen")) heal(caster, 7);
                    }
                }
            }

            @Override
            public void executeReversed(GameState state, Player caster, Player target) {
                for (Card c : caster.field) {
                    if (c.suit == Suit.CUPS && !c.id.endsWith("/King")) {
                        CardEffect effect = EffectRegistry.get(c.id);
                        if (effect != null) effect.executeUpright(state, caster, target);
                    }
                }
                caster.cost = 0;
            }
        }
    }

    // Swords
    public static class Swords {
        public static class Ace extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 4);
            }
        }

        public static class Two extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 3);
                reduceCost(caster, 1);
            }
        }

        public static class Three extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 5);
            }
        }

        public static class Four extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                caster.incomingDamageMultiplier = 0.5f;
            }
        }

        public static class Five extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 6);
                if (target.hand.size > 0) {
                    Random rand = new Random();
                    target.hand.removeIndex(rand.nextInt(target.hand.size));
                }
            }
        }

        public static class Six extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 6);
                target.nextTurnDrawModifier -= 1;
            }
        }

        public static class Seven extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 7);
                if (target.hand.size > 0) {
                    Card stolen = target.hand.removeIndex(new Random().nextInt(target.hand.size));
                    stolen.isRevealed = true;
                    state.addTransferredCardToHand(caster, stolen);
                }
            }
        }

        public static class Eight extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 7);
                reduceCost(target, 2);
                addCost(caster, 1);

            }
        }

        public static class Nine extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 10);
                revealRandomCards(target.hand, 1);
            }
        }

        public static class Ten extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 11);
                target.nextTurnPlayLimit = 1;
            }
        }

        public static class Page extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                for (Card c : caster.hand) {
                    if (c.suit == Suit.SWORDS) c.costModifier -= 1;
                }
            }

            @Override
            public void executeReversed(GameState state, Player caster, Player target) {
                for (Card c : caster.hand) {
                    if (c.suit == Suit.SWORDS) c.costModifier += 1;
                }
            }
        }

        public static class Knight extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                if (target.hand.size > 0) {
                    Array<Card> unrevealed = new Array<>();
                    for (Card c : target.hand) if (!c.isRevealed) unrevealed.add(c);

                    unrevealed.shuffle();
                    int revealCount = Math.min(2, unrevealed.size);
                    for (int i = 0; i < revealCount; i++) {
                        unrevealed.get(i).isRevealed = true;
                    }

                    target.hand.removeIndex(new Random().nextInt(target.hand.size));
                }
            }

            @Override
            public void executeReversed(GameState state, Player caster, Player target) {
                damage(caster, target, 13);
                if (caster.hand.size > 0) {
                    caster.hand.removeIndex(new Random().nextInt(caster.hand.size));
                }
            }
        }

        public static class Queen extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 10);
                revealRandomCards(target.hand, 3);
            }

            @Override
            public void executeReversed(GameState state, Player caster, Player target) {
                damage(caster, target, 11);
                revealRandomCards(caster.hand, 2);
            }
        }

        public static class King extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                for (Card c : caster.field) {
                    if (c.suit == Suit.SWORDS && !c.id.endsWith("/King")) {
                        CardEffect effect = EffectRegistry.get(c.id);
                        if (effect != null) effect.executeUpright(state, caster, target);
                    }
                }
            }

            @Override
            public void executeReversed(GameState state, Player caster, Player target) {
                for (int i = caster.field.size - 1; i >= 0; i--) {
                    Card lastCard = caster.field.get(i);
                    if (lastCard.suit == Suit.SWORDS && !lastCard.id.endsWith("/King")) {
                        CardEffect effect = EffectRegistry.get(lastCard.id);
                        if (effect != null) effect.executeUpright(state, caster, caster);
                        break;
                    }
                }
            }
        }
    }

    // Pentacles
    public static class Pentacles {
        public static class Ace extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                addCost(caster, 2);
            }
        }

        public static class Two extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 3);
                addCost(caster, 1);
            }
        }

        public static class Three extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                Array<Card> numberCards = new Array<>();
                for (Card c : caster.hand) {
                    boolean isCourt = c.id.contains("Page") || c.id.contains("Knight")
                        || c.id.contains("Queen") || c.id.contains("King");
                    if (c.type == Card.ArcanaType.MINOR && !isCourt) {
                        numberCards.add(c);
                    }
                }

                if (numberCards.size > 0) {
                    Card targetCard = numberCards.get(new Random().nextInt(numberCards.size));
                    // 코스트 수식: Math.max(0, cost + costModifier) 이므로
                    // costModifier를 -cost로 설정하면 코스트가 0이 됨
                    targetCard.costModifier = -targetCard.cost;
                }
            }
        }

        public static class Four extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                caster.saveHalfCostNextTurn = true;
            }
        }

        public static class Five extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 6);
                addCost(caster, 2);
            }
        }

        public static class Six extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                reduceCost(target, 2);
                addCost(caster, 2);
            }
        }

        public static class Seven extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                int costSum = 0;
                for (Card c : caster.field) {
                    costSum += c.cost;
                }
                int finalDamage = Math.min(costSum, 15);
                damage(caster, target, finalDamage);
            }
        }

        public static class Eight extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 7);
                caster.nextTurnCostModifier += 4;
            }
        }

        public static class Nine extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                for (Card c : caster.hand) {
                    if (c.suit == Suit.PENTACLES) c.costModifier -= 1;
                }
                damage(caster, target, 9);
            }
        }

        public static class Ten extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, caster.pentaclesPlayedThisTurn * 3);
            }
        }

        public static class Page extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                for (Card c : caster.hand) {
                    if (c.suit == Suit.PENTACLES) c.costModifier -= 1;
                }
            }

            @Override
            public void executeReversed(GameState state, Player caster, Player target) {
                for (Card c : caster.hand) {
                    if (c.suit == Suit.PENTACLES) c.costModifier += 1;
                }
            }
        }

        public static class Knight extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                int consumed = caster.cost;
                caster.cost = 0;

                int totalDamage = 0;
                Random rand = new Random();
                for (int i = 0; i < consumed; i++) {
                    if (rand.nextBoolean()) {
                        totalDamage += 6;
                    }
                }
                damage(caster, target, totalDamage);
            }

            @Override
            public void executeReversed(GameState state, Player caster, Player target) {
                int consumed = caster.cost;
                caster.cost = 0;

                // 데미지: 소모한 코스트 * 1.5 (소수점 버림)
                damage(caster, target, (int)(consumed * 1.5f));

                caster.hp -= consumed;
                caster.hp = Math.max(0, caster.hp);
            }
        }

        public static class Queen extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 9);
                target.nextTurnCostModifier -= 3;
            }

            @Override
            public void executeReversed(GameState state, Player caster, Player target) {
                damage(caster, target, 15);
                caster.hand.clear();
                caster.nextTurnDrawModifier = -99;
            }
        }

        public static class King extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                int sum = 0;
                for (Card c : caster.field) {
                    if (c.suit == Suit.PENTACLES && !c.id.endsWith("/King")) {
                        sum += c.cost;
                    }
                }
                damage(caster, target, sum * 3);
            }

            @Override
            public void executeReversed(GameState state, Player caster, Player target) {
                int sum = 0;
                for (Card c : caster.field) {
                    if (c.suit == Suit.PENTACLES && !c.id.endsWith("/King")) {
                        sum += c.cost;
                    }
                }
                damage(caster, caster, sum * 2);
            }
        }
    }

    private static void revealRandomCards(Array<Card> hand, int count) {
        Array<Card> hiddenCards = new Array<>();
        for (Card c : hand) {
            if (!c.isRevealed) hiddenCards.add(c);
        }

        hiddenCards.shuffle();
        int revealCount = Math.min(count, hiddenCards.size);
        for (int i = 0; i < revealCount; i++) {
            hiddenCards.get(i).isRevealed = true;
        }
    }
}
