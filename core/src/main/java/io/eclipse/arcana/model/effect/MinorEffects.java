package io.eclipse.arcana.model.effect;

import io.eclipse.arcana.model.Card;
import io.eclipse.arcana.model.GameState;
import io.eclipse.arcana.model.Player;
import io.eclipse.arcana.model.Suit;

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
                addCost(caster, 1);
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

                caster.reflectDamage = 5;
            }
        }

        public static class Eight extends BaseCardEffect {
            @Override
            public void executeUpright(GameState state, Player caster, Player target) {
                damage(caster, target, 6 * 2);

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
                damage(caster, target, 10);
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

            }

            @Override
            public void executeReversed(GameState state, Player caster, Player target) {

            }
        }
    }
}
