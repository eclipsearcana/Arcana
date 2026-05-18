package io.eclipse.arcana.model;

import com.badlogic.gdx.utils.Array;
import io.eclipse.arcana.GameConfig;
import io.eclipse.arcana.model.effect.CardEffect;
import io.eclipse.arcana.model.effect.EffectRegistry;

public class GameState {
    public static class PendingEffect {
        public final Card card;
        public final Player caster;
        public final Player target;
        public final boolean effectsSwapped;
        public final boolean mirrorEffect;

        public PendingEffect(Card card, Player caster, Player target, boolean effectsSwapped, boolean mirrorEffect) {
            this.card = card;
            this.caster = caster;
            this.target = target;
            this.effectsSwapped = effectsSwapped;
            this.mirrorEffect = mirrorEffect;
        }
    }

    public final Player[] players = { new Player(), new Player() };
    public int currentPlayerIndex = 0;
    public GamePhase phase = GamePhase.DRAFT;
    public TurnPhase turnPhase = TurnPhase.DRAW;
    public float turnTimer = GameConfig.TURN_TIME;
    public int winnerIndex = -1; // -1 = 승자 없음

    public int roundCount = 0;

    public GameState() {
        setupTest(Suit.PENTACLES);
    }

    public static void applyReverseChance(Card card) {
        if (GameConfig.DEV_FORCE_REVERSE) {
            card.reversed = true;
            return;
        }

        float chance;
        if (card.type == Card.ArcanaType.MAJOR) {
            chance = GameConfig.REVERSE_CHANCE_MAJOR;
        } else {
            String id = card.id;
            boolean isCourt = id.contains("Page") || id.contains("Knight")
                            || id.contains("Queen") || id.contains("King");
            chance = isCourt ? GameConfig.REVERSE_CHANCE_MINOR_COURT
                             : GameConfig.REVERSE_CHANCE_MINOR_NUM;
        }
        card.reversed = (Math.random() < chance);
    }

    public Card drawCard(Player player) {
        return drawCard(player, false);
    }

    public Card drawCardIgnoringLocks(Player player) {
        return drawCard(player, true);
    }

    private Card drawCard(Player player, boolean ignoreLocks) {
        if (player.hand.size >= GameConfig.HAND_MAX) return null;
        if (!ignoreLocks && (player.drawBlocked || player.handLockTurns > 0)) return null;

        Card drawn = player.deck.draw();
        if (drawn != null) {
            applyReverseChance(drawn);
            if (player.forceReversedDraw) drawn.reversed = true;

            if (player.mustPlayNextDraw) {
                player.mustPlayNextDraw = false;
                player.hand.add(drawn);
                forcePlayCardFromHand(player, player.hand.size - 1);
                System.out.println("[강제 발동] " + drawn.name + " 카드가 즉시 사용되었습니다.");
            } else {
                player.hand.add(drawn);
            }
        }
        return drawn;
    }

    public void setupTest(Suit suit) {
        for (int i = 0; i < players.length; i++) {
            Player p = players[i];
            p.hp = GameConfig.PLAYER_HP_START;
            p.cost = GameConfig.COST_DEFAULT_INIT;
            p.costInit = GameConfig.COST_DEFAULT_INIT;
            p.carryOver = false;
            p.costIncreasedOnPlay = false;
            p.allCardsCostZeroThisTurn = false;
            p.randomTargetsThisTurn = false;
            p.keepPlayedCardsInHandThisTurn = false;
            p.canOverpayCostWithHpThisTurn = false;
            p.drawOnEmptyCostThisTurn = false;
            p.mustSpendAllCostThisTurn = false;
            p.delayEffectsThisTurn = false;
            p.outgoingDamageBonus = 0;
            p.lowHpOutgoingDamageBonus = 0;
            p.effectFailChanceThisTurn = 0f;
            p.mirrorEffectsToSelfThisTurn = false;
            p.effectsNegatedThisTurn = false;
            p.effectCostRefundReceiver = null;
            p.handLockTurns = 0;
            p.nextTurnFixedCost = -1;
            p.nextTurnCostMultiplier = 1;
            p.fakeShieldTurns = 0;
            p.fakeShieldBlockedDamage = 0;
            p.delayedEffects.clear();
            p.majorBlocked = false;
            p.effectsSwapped = false;
            p.cannotBeTargeted = false;
            p.forceReversedDraw = false;
            p.drawBlocked = false;

            p.nextTurnDrawModifier = 0;
            p.nextTurnPlayLimit = -1;
            p.playLimit = -1;
            p.wandsPlayedThisTurn = 0;
            p.cupsPlayedThisTurn = 0;
            p.swordsPlayedThisTurn = 0;
            p.pentaclesPlayedThisTurn = 0;

            p.field.clear();

            p.chosenSuit = suit;
            p.hand.clear();

            p.deck.clear();

            Array<Card> allCards = CardDefinitions.allMajor();
            allCards.addAll(CardDefinitions.allMinor(suit));
            allCards.shuffle();

            for (Card c : allCards) p.deck.add(c);

            for (int j = 0; j < 5; j++) {
                Card drawn = p.deck.draw();
                if (drawn != null) {
                    applyReverseChance(drawn);
                    p.hand.add(drawn);
                }
            }
        }
        currentPlayerIndex = 0;
        phase = GamePhase.MAIN;
        turnPhase = TurnPhase.DRAW;
        turnTimer = GameConfig.TURN_TIME;
    }

    public Player currentPlayer() {
        return players[currentPlayerIndex];
    }

    public int effectiveCostFor(Player player, Card card) {
        return player.allCardsCostZeroThisTurn ? 0 : card.effectiveCost();
    }

    public Card playCardFromHand(Player player, int handIndex) {
        return playCardFromHand(player, handIndex, false);
    }

    public Card forcePlayCardFromHand(Player player, int handIndex) {
        return playCardFromHand(player, handIndex, true);
    }

    private Card playCardFromHand(Player player, int handIndex, boolean forced) {
        if (handIndex < 0 || handIndex >= player.hand.size) return null;

        Card card = player.hand.get(handIndex);
        int paidCost = forced ? 0 : effectiveCostFor(player, card);

        if (!forced) {
            if (phase != GamePhase.MAIN || turnPhase != TurnPhase.ACTION) return null;
            if (player != currentPlayer()) return null;
            if (player.playLimit == 0) return null;
            if (player.majorBlocked && card.type == Card.ArcanaType.MAJOR) return null;
            if (!GameConfig.DEV_NO_COST_LIMIT && !player.canOverpayCostWithHpThisTurn && player.cost < paidCost) {
                return null;
            }

            if (player.canOverpayCostWithHpThisTurn && paidCost > player.cost) {
                int overpaid = paidCost - player.cost;
                player.cost = 0;
                player.hp = Math.max(0, player.hp - overpaid);
            } else {
                player.cost -= paidCost;
            }
        }

        boolean keepPlayedCard = player.keepPlayedCardsInHandThisTurn || player.handLockTurns > 0;
        boolean effectsSwapped = player.effectsSwapped;
        boolean mirrorEffect = player.mirrorEffectsToSelfThisTurn;
        boolean effectNegated = player.effectsNegatedThisTurn;
        boolean delayEffect = player.delayEffectsThisTurn;
        Player refundReceiver = player.effectCostRefundReceiver;
        float failChance = player.effectFailChanceThisTurn;

        player.hand.removeIndex(handIndex);
        countSuitPlayed(player, card);

        CardEffect effect = EffectRegistry.get(card.id);
        if (effect != null) {
            if (effectNegated) {
                if (refundReceiver != null) {
                    refundReceiver.cost = Math.min(refundReceiver.cost + paidCost, refundReceiver.costMax);
                }
                System.out.println("[정의 역방향] " + card.name + " 효과 무효화");
            } else if (failChance > 0f && Math.random() < failChance) {
                System.out.println("[힘 역방향] " + card.name + " 효과 발동 실패");
            } else {
                Player target = chooseTarget(player);
                if (delayEffect) {
                    player.delayedEffects.add(new PendingEffect(card, player, target, effectsSwapped, mirrorEffect));
                    System.out.println("[심판 역방향] " + card.name + " 효과가 1턴 지연");
                } else {
                    executeCardEffect(effect, card, player, target, effectsSwapped);
                    if (mirrorEffect) {
                        executeCardEffect(effect, card, player, player, effectsSwapped);
                    }
                }
            }
        }

        if (card.isExtinction) {
            player.removedCards.add(card);
            System.out.println("[시스템] " + card.name + " 카드가 소멸되었습니다.");
        } else if (keepPlayedCard) {
            player.hand.add(card);
        } else {
            player.field.add(card);
        }

        if (player.costIncreasedOnPlay) {
            int increase = new java.util.Random().nextInt(2) + 1;
            card.costModifier += increase;
        }

        if (!forced && player.playLimit > 0) player.playLimit--;
        if (!forced) applyPostPlayTriggers(player);
        checkWinCondition();
        return card;
    }

    private void applyPostPlayTriggers(Player player) {
        if (player.drawOnEmptyCostThisTurn && player.cost <= 0) {
            player.drawOnEmptyCostThisTurn = false;
            player.nextTurnCostMultiplier = Math.max(player.nextTurnCostMultiplier, 2);
            drawCard(player);
            System.out.println("[별] 코스트 전부 소모: 1장 드로우, 다음 턴 코스트 2배");
        }
    }

    private void executeCardEffect(CardEffect effect, Card card, Player caster, Player target, boolean effectsSwapped) {
        Card previousCard = caster.currentCard;
        caster.currentCard = card;
        boolean useReversed = effectsSwapped ? !card.reversed : card.reversed;

        if (useReversed) effect.executeReversed(this, caster, target);
        else effect.executeUpright(this, caster, target);

        caster.currentCard = previousCard;
    }

    private void countSuitPlayed(Player player, Card card) {
        if (card.suit == Suit.WANDS) player.wandsPlayedThisTurn++;
        if (card.suit == Suit.CUPS) player.cupsPlayedThisTurn++;
        if (card.suit == Suit.SWORDS) player.swordsPlayedThisTurn++;
        if (card.suit == Suit.PENTACLES) player.pentaclesPlayedThisTurn++;
    }

    public Player chooseTarget(Player caster) {
        Player target = caster.randomTargetsThisTurn
            ? players[new java.util.Random().nextInt(players.length)]
            : getOpponent(caster);

        if (target.cannotBeTargeted && target != caster) return caster;
        return target;
    }

    public void resetTableCards() {
        for (Player player : players) {
            for (Card card : player.field) {
                player.deck.add(card);
            }
            player.field.clear();
            player.deck.shuffle();
        }
    }

    public void refillHand(Player player, int count) {
        player.hand.clear();
        for (int i = 0; i < count; i++) {
            drawCardIgnoringLocks(player);
        }
    }

    private void resolveDelayedEffects(Player player) {
        if (player.delayedEffects.size == 0) return;

        Array<PendingEffect> pending = new Array<>();
        pending.addAll(player.delayedEffects);
        player.delayedEffects.clear();

        for (PendingEffect delayed : pending) {
            CardEffect effect = EffectRegistry.get(delayed.card.id);
            if (effect == null) continue;

            executeCardEffect(effect, delayed.card, delayed.caster, delayed.target, delayed.effectsSwapped);
            if (delayed.mirrorEffect) {
                executeCardEffect(effect, delayed.card, delayed.caster, delayed.caster, delayed.effectsSwapped);
            }
        }

        checkWinCondition();
    }

    private void expireFakeShield(Player player) {
        if (player.fakeShieldTurns <= 0) return;

        player.fakeShieldTurns--;
        if (player.fakeShieldTurns == 0) {
            int blocked = player.fakeShieldBlockedDamage;
            player.fakeShieldBlockedDamage = 0;
            if (blocked > 0) {
                int backlash = blocked + Math.max(1, blocked / 2);
                player.hp = Math.max(0, player.hp - backlash);
                System.out.println("[별 역방향] 가짜 보호막 소멸: " + backlash + " 피해");
                checkWinCondition();
            }
        }
    }

    public void clearDebuffs(Player player) {
        player.majorBlocked = false;
        player.effectsNegatedThisTurn = false;
        player.effectCostRefundReceiver = null;
        player.drawBlocked = false;
        player.forceReversedDraw = false;
        player.mustPlayNextDraw = false;
        player.costIncreasedOnPlay = false;
        player.effectFailChanceThisTurn = 0f;
        player.randomTargetsThisTurn = false;
        player.playLimit = -1;
        player.nextTurnPlayLimit = -1;
        player.handLockTurns = 0;
        player.healBlocked = false;
        player.incomingDamageMultiplier = Math.min(player.incomingDamageMultiplier, 1.0f);
        if (player.nextTurnDrawModifier < 0) player.nextTurnDrawModifier = 0;
        if (player.nextTurnCostModifier < 0) player.nextTurnCostModifier = 0;
        player.nextTurnFixedCost = -1;
    }

    public void checkWinCondition() {
        for (int i = 0; i < players.length; i++) {
            if (players[i].hp <= 0) {
                players[i].hp = 0;
                winnerIndex = 1 - i;
                phase = GamePhase.GAME_OVER;
                return;
            }
        }
    }

    public void update(float delta) {
        if (phase != GamePhase.MAIN) return;
        turnTimer -= delta;
        if (turnTimer <= 0f) advanceTurnPhase();
    }

    public void advanceTurnPhase() {
        switch (turnPhase) {
            case DRAW:
                Player p = currentPlayer();
                p.cannotBeTargeted = false;
                p.playLimit = p.nextTurnPlayLimit;
                p.nextTurnPlayLimit = -1;

                resolveDelayedEffects(p);

                int drawCount = 1 + p.nextTurnDrawModifier;
                p.nextTurnDrawModifier = 0;
                drawCount = Math.max(0, drawCount);
                for (int i = 0; i < drawCount; i++) {
                    drawCard(p);
                }
                p.drawBlocked = false;
                if (p.handLockTurns > 0) p.handLockTurns--;
                expireFakeShield(p);

                int startCost = (p.nextTurnFixedCost >= 0)
                    ? p.nextTurnFixedCost
                    : p.costInit + p.nextTurnCostModifier;
                startCost *= Math.max(1, p.nextTurnCostMultiplier);
                p.cost = Math.min(Math.max(0, startCost), p.costMax);
                p.nextTurnFixedCost = -1;
                p.nextTurnCostModifier = 0;
                p.nextTurnCostMultiplier = 1;

                turnPhase = TurnPhase.ACTION;
                turnTimer = GameConfig.TURN_TIME;

                if (GameConfig.DEV_AUTO_P1 && currentPlayerIndex == 1) {
                    Player p1 = currentPlayer();
                    int count = Math.min(3, p1.hand.size);
                    for (int i = 0; i < count; i++) {
                        p1.field.add(p1.hand.removeIndex(0));
                    }
                }
                break;
            case ACTION:
                turnPhase = TurnPhase.END;
                turnTimer = GameConfig.TURN_TIME;
                break;
            case END:
                Player current = currentPlayer();

                if (current.saveHalfCostNextTurn) {
                    current.nextTurnCostModifier += (current.cost / 2);
                    current.saveHalfCostNextTurn = false;
                }

                if (current.mustSpendAllCostThisTurn && current.cost > 0) {
                    current.hp = Math.max(0, current.hp - current.cost);
                    current.cost = 0;
                    checkWinCondition();
                }

                current.costIncreasedOnPlay = false;
                current.allCardsCostZeroThisTurn = false;
                current.randomTargetsThisTurn = false;
                current.keepPlayedCardsInHandThisTurn = false;
                current.canOverpayCostWithHpThisTurn = false;
                current.drawOnEmptyCostThisTurn = false;
                current.mustSpendAllCostThisTurn = false;
                current.delayEffectsThisTurn = false;
                current.outgoingDamageBonus = 0;
                current.lowHpOutgoingDamageBonus = 0;
                current.effectFailChanceThisTurn = 0f;
                current.mirrorEffectsToSelfThisTurn = false;
                current.effectsNegatedThisTurn = false;
                current.effectCostRefundReceiver = null;
                current.majorBlocked = false;
                current.effectsSwapped = false;
                current.forceReversedDraw = false;
                current.playLimit = -1;

                current.wandsPlayedThisTurn = 0;
                current.cupsPlayedThisTurn = 0;
                current.swordsPlayedThisTurn = 0;
                current.pentaclesPlayedThisTurn = 0;
                current.reflectRatio = 0;

                current.field.clear();

                if (!current.carryOver) {
                    current.cost = Math.min(current.costInit, current.costMax);
                }

                current.carryOver = false;
                for (Card c : current.hand) c.costModifier = 0;

                currentPlayerIndex = 1 - currentPlayerIndex;

                Player next = currentPlayer();

                roundCount++;
                turnPhase = TurnPhase.DRAW;
                turnTimer = GameConfig.TURN_TIME;
                break;
        }
    }

    public Player getOpponent(Player player) {
        // 내가 players[0]이면 players[1]을, 내가 players[1]이면 players[0]을 반환
        return (player == players[0]) ? players[1] : players[0];
    }
}
