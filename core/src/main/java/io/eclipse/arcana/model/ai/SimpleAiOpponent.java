package io.eclipse.arcana.model.ai;

import java.util.Comparator;

import com.badlogic.gdx.utils.Array;

import io.eclipse.arcana.GameConfig;
import io.eclipse.arcana.model.Card;
import io.eclipse.arcana.model.GamePhase;
import io.eclipse.arcana.model.GameState;
import io.eclipse.arcana.model.Player;
import io.eclipse.arcana.model.Suit;
import io.eclipse.arcana.model.TurnPhase;
import io.eclipse.arcana.model.controller.PlayerController;

public class SimpleAiOpponent implements PlayerController {

    private static final float THINK_TIME = 0.65f;
    private static final float HP_MAX = GameConfig.PLAYER_HP_START;

    private final int playerIndex;
    private final BasicAiTurnPlanner turnPlanner = new BasicAiTurnPlanner();
    private float timer = THINK_TIME;

    public SimpleAiOpponent(int playerIndex) {
        this.playerIndex = playerIndex;
    }

    @Override
    public boolean controls(int playerIndex) {
        return this.playerIndex == playerIndex;
    }

    @Override
    public boolean acceptsHumanInput() {
        return false;
    }

    @Override
    public void reset() {
        timer = THINK_TIME;
    }

    @Override
    public void update(GameState state, int playerIndex, float delta, Actions actions) {
        if (!controls(playerIndex)) return;
        if (state.phase != GamePhase.MAIN) return;
        if (state.currentPlayerIndex != playerIndex) {
            reset();
            return;
        }
        if (actions.hasCardAnimation()) return;

        timer -= delta;
        if (timer > 0f) return;
        timer = THINK_TIME;

        if (state.pendingSelection != null) {
            completeSelection(state);
            return;
        }

        if (state.turnPhase == TurnPhase.DRAW || state.turnPhase == TurnPhase.END) {
            actions.advanceTurnPhase();
            return;
        }

        Player player = state.currentPlayer();
        if (player.stagedCards.size == 0) {
            stagePlayableCards(state, player);
        }

        if (player.stagedCards.size > 0) {
            state.resolveStagedCards(player);
        } else {
            actions.advanceTurnPhase();
        }
    }

    private void stagePlayableCards(GameState state, Player player) {
        BasicAiTurnPlan plan = turnPlanner.plan(buildPlayableCandidates(state, player), player);

        // GameState의 검증 로직을 그대로 사용하기 위해 hand index를 다시 찾아 스테이징합니다.
        for (Card card : plan.cardsToPlay) {
            int handIndex = indexOf(player.hand, card);
            if (handIndex < 0) continue;
            state.stageCardFromHand(player, handIndex);
        }
    }

    private Array<BasicAiTurnPlanner.Candidate> buildPlayableCandidates(GameState state, Player player) {
        Array<BasicAiTurnPlanner.Candidate> choices = new Array<>();

        for (int i = 0; i < player.hand.size; i++) {
            Card card = player.hand.get(i);
            if (card.lockedInHand) continue;
            if (player.majorBlocked && card.type == Card.ArcanaType.MAJOR) continue;
            int cost = state.effectiveCostFor(player, card);
            if (!GameConfig.DEV_NO_COST_LIMIT
                && !player.canOverpayCostWithHpThisTurn && cost > player.cost) continue;
            choices.add(new BasicAiTurnPlanner.Candidate(
                card, evaluateCard(card, cost, state, player), cost));
        }
        return choices;
    }

    private int evaluateCard(Card card, int cost, GameState state, Player me) {
        Player opp = state.getOpponent(me);
        boolean reversed = isEffectivelyReversed(card, me);

        int effectValue = estimateEffectValue(card, reversed, state, me, opp);

        // 코스트 효율: 낮은 비용의 카드를 선호
        int score = effectValue - cost * 4;

        // 역방향 카드를 내면 핸드에서 제거 → 역방향 페널티 감소
        if (card.reversed && card.isReversePenaltyActive()) {
            int penaltyCount = state.countReversePenaltyCards(me);
            score += 8 + penaltyCount * 5;
        }

        if (card.suit != null && isPage(card.id)) {
            int suitCount = countSuitInHand(me.hand, card.suit) - 1;
            if (suitCount > 0) score += suitCount * 5;
        }

        return score;
    }

    private boolean isEffectivelyReversed(Card card, Player p) {
        return card.reversed ^ p.effectsSwapped;
    }

    private int estimateEffectValue(Card card, boolean reversed,
                                    GameState state, Player me, Player opp) {
        float myHpR  = me.hp / HP_MAX;
        float opHpR  = opp.hp / HP_MAX;

        // 상대 HP 낮으면 데미지 가중, 내 HP 낮으면 힐 가중
        float dm = opHpR < 0.25f ? 1.5f : opHpR < 0.5f ? 1.2f : 1.0f;
        float hl = myHpR < 0.25f ? 1.8f : myHpR < 0.5f ? 1.3f : 0.8f;

        if (card.type == Card.ArcanaType.MINOR) {
            if (isCourtCard(card.id)) {
                return reversed
                    ? minorCourtReversed(card, state, me, opp, dm, hl)
                    : minorCourtUpright(card, state, me, opp, dm, hl);
            }
            return minorNumber(card, state, me, opp, dm, hl);
        }
        // 메이저 아르카나
        return reversed
            ? majorReversed(card, state, me, opp)
            : majorUpright(card, state, me, opp, dm, hl);
    }

    //  마이너 넘버 카드 (Ace–Ten): 정/역 효과 동일
    private int minorNumber(Card card, GameState state,
                            Player me, Player opp, float dm, float hl) {
        String rank = rank(card.id);
        if (card.suit == null) return 10;

        switch (card.suit) {
            case WANDS:     return wandsNumber(rank, me, dm);
            case CUPS:      return cupsNumber(rank, me, dm, hl);
            case SWORDS:    return swordsNumber(rank, dm);
            case PENTACLES: return pentaclesNumber(rank, me, dm);
            default:        return 10;
        }
    }

    private int wandsNumber(String rank, Player me, float dm) {
        switch (rank) {
            case "Ace":   return d(5, dm);
            case "Two":   return d(4, dm) + 5;
            case "Three": return d(4, dm) + 8;
            case "Four":  return d(3, dm) + 4;
            case "Five":  return d(8, dm) + 6;
            case "Six":   return d(8, dm) + 5;
            case "Seven": return d(8, dm) + 5;
            case "Eight": return d(12, dm);
            case "Nine": {
                float missingRatio = 1f - me.hp / HP_MAX;
                return d((int)(12 + missingRatio * 12), dm);
            }
            case "Ten":
                return d(12 + me.wandsPlayedThisTurn * 3, dm);
            default: return d(6, dm);
        }
    }

    private int cupsNumber(String rank, Player me, float dm, float hl) {
        switch (rank) {
            case "Ace":   return h(7, hl);
            case "Two":   return h(6, hl) + 8;
            case "Three": return 6;
            case "Four":  return d(3, dm) + 6;
            case "Five":  return h(11, hl) + d(5, dm);
            case "Six":   return 10;
            case "Seven": return 24;
            case "Eight": return h(13, hl) + 6;
            case "Nine":  return h(15, hl) + 5;
            case "Ten":   return h(me.cupsPlayedThisTurn * 4, hl);
            default: return h(8, hl);
        }
    }

    private int swordsNumber(String rank, float dm) {
        switch (rank) {
            case "Ace":   return d(4, dm);
            case "Two":   return d(3, dm) + 4;
            case "Three": return d(5, dm);
            case "Four":  return 8;
            case "Five":  return d(6, dm) + 8;
            case "Six":   return d(6, dm) + 6;
            case "Seven": return d(7, dm) + 10;
            case "Eight": return d(7, dm) + 13;
            case "Nine":  return d(10, dm) + 2;
            case "Ten":   return d(11, dm) + 15;
            default: return d(6, dm);
        }
    }

    private int pentaclesNumber(String rank, Player me, float dm) {
        switch (rank) {
            case "Ace":   return 10;
            case "Two":   return d(3, dm) + 5;
            case "Three": return 7;
            case "Four":  return 6;
            case "Five":  return d(6, dm) + 10;
            case "Six":   return 14;
            case "Seven": return d(fieldCostSum(me), dm);
            case "Eight": return d(7, dm) + 16;
            case "Nine":  return d(9, dm) + suitCostBonus(me.hand, Suit.PENTACLES);
            case "Ten":   return d(me.pentaclesPlayedThisTurn * 3, dm);
            default: return 8;
        }
    }

    // 마이너 코트 카드 (Page/Knight/Queen/King): 정방향
    private int minorCourtUpright(Card card, GameState state,
                                   Player me, Player opp, float dm, float hl) {
        String court = rank(card.id);
        if (card.suit == null) return 12;

        switch (card.suit) {
            case WANDS:
                switch (court) {
                    case "Page":   return suitCostBonus(me.hand, Suit.WANDS);
                    case "Knight": return d(14, dm) - 10;
                    case "Queen":  return d(13, dm) + suitCostBonus(me.hand, Suit.WANDS);
                    case "King":   return fieldSuitValue(me, Suit.WANDS, dm);
                }
                break;
            case CUPS:
                switch (court) {
                    case "Page":   return suitCostBonus(me.hand, Suit.CUPS);
                    case "Knight": return 12;
                    case "Queen":  return d(8, dm) + h(7, hl);
                    case "King":   return fieldSuitValue(me, Suit.CUPS, hl);
                }
                break;
            case SWORDS:
                switch (court) {
                    case "Page":   return suitCostBonus(me.hand, Suit.SWORDS);
                    case "Knight": return 20;
                    case "Queen":  return d(10, dm) + 4;
                    case "King":   return fieldSuitValue(me, Suit.SWORDS, dm);
                }
                break;
            case PENTACLES:
                switch (court) {
                    case "Page":   return suitCostBonus(me.hand, Suit.PENTACLES);
                    case "Knight": return d((me.cost * 3), dm);
                    case "Queen":  return d(9, dm) + 12;
                    case "King":   return fieldSuitValue(me, Suit.PENTACLES, dm);
                }
                break;
        }
        return 12;
    }

    // 마이너 코트 카드: 역방향
    private int minorCourtReversed(Card card, GameState state,
                                    Player me, Player opp, float dm, float hl) {
        String court = rank(card.id);
        if (card.suit == null) return 0;

        switch (card.suit) {
            case WANDS:
                switch (court) {
                    case "Page":   return -suitCostPenalty(me.hand, Suit.WANDS);
                    case "Knight": return d(20, dm) - 25;
                    case "Queen":  return d(17, dm) - 17;
                    case "King":   return d(6, dm);
                }
                break;
            case CUPS:
                switch (court) {
                    case "Page":   return -suitCostPenalty(me.hand, Suit.CUPS);
                    case "Knight": return 12 - 10;
                    case "Queen":  return d(11, dm) + 8;
                    case "King":   return 6;
                }
                break;
            case SWORDS:
                switch (court) {
                    case "Page":   return -suitCostPenalty(me.hand, Suit.SWORDS);
                    case "Knight": return d(13, dm) - 6;
                    case "Queen":  return d(11, dm) - 3;
                    case "King":   return -8;
                }
                break;
            case PENTACLES:
                switch (court) {
                    case "Page":   return -suitCostPenalty(me.hand, Suit.PENTACLES);
                    case "Knight": {
                        int c = me.cost;
                        return d((int)(c * 1.5f), dm) - c;
                    }
                    case "Queen":  return d(15, dm) - 16;
                    case "King":   return -fieldPentCostSum(me) * 2;
                }
                break;
        }
        return 0;
    }

    // 메이저 아르카나: 정방향
    private int majorUpright(Card card, GameState state,
                              Player me, Player opp, float dm, float hl) {
        float myHpR  = me.hp / HP_MAX;

        switch (card.id) {
            case "Fool":
                return me.hand.size > 1 ? 14 : 2;
            case "Magician":
                return me.hand.size > 1 ? 22 : 4;
            case "Priestess":
                return 12;
            case "Empress":
                return h(22, hl);
            case "Emperor":
                return 18;
            case "Hierophant":
                return 16;
            case "Lovers":
                return me.hand.size > 1 ? 14 : 4;
            case "Chariot":
                return me.hand.size >= 3 ? 20 : 8;
            case "Strength":
                return myHpR <= 0.35f ? d(6, dm) + 8 : d(3, dm) + 8;
            case "Hermit":
                return 14;
            case "Fortune":
                return opp.hand.size > me.hand.size ? 16 : 6;
            case "Justice":
                return opp.hand.size > me.hand.size ? 14 : 4;
            case "HangedMan":
                return myHpR > 0.4f ? 12 : 2;
            case "Death":
                return 10;
            case "Temperance":
                return state.countReversePenaltyCards(me) >= 2 ? 18 : 6;
            case "Devil":
                return me.cost < totalHandCost(me) ? 14 : 6;
            case "Tower":
                return 16;
            case "Star":
                return 16;
            case "Moon":
                return 10;
            case "Sun":
                return 15;
            case "Judgement":
                return me.removedCards.size >= 1 ? 14 : 2;
            case "World":
                return opp.hand.size > 0 ? 20 : 2;
            default: return 10;
        }
    }

    // 메이저 아르카나: 역방향
    private int majorReversed(Card card, GameState state,
                              Player me, Player opp) {
        boolean hasDebuffs = me.majorBlocked || me.effectsSwapped
            || me.drawBlocked || me.effectsNegatedThisTurn
            || me.costIncreasedOnPlay || me.handLockTurns > 0
            || me.fakeShieldTurns > 0;

        switch (card.id) {
            case "Fool":
                return -4;
            case "Magician":
                return me.hand.size > 1 ? 8 : 0;
            case "Priestess":
                return -6;
            case "Empress":
                return -10;
            case "Emperor":
                return 8;
            case "Hierophant":
                return -8;
            case "Lovers":
                return -6;
            case "Chariot":
                return me.hand.size >= 3 ? 8 : 4;
            case "Strength":
                return -12;
            case "Hermit":
                return -10;
            case "Fortune":
                return -8;
            case "Justice":
                return 16;
            case "HangedMan":
                return opp.hand.size > 0 ? 10 : 0;
            case "Death":
                return -6;
            case "Temperance":
                return state.countReversePenaltyCards(me) >= 3 ? 12 : -4;
            case "Devil":
                return hasDebuffs ? 18 : 3;
            case "Tower":
                return -12;
            case "Star":
                return -4;
            case "Moon":
                return 6;
            case "Sun":
                return -14;
            case "Judgement":
                return -6;
            case "World":
                return 10;
            default: return 0;
        }
    }

    private void completeSelection(GameState state) {
        GameState.CardSelectionRequest request = state.pendingSelection;
        Player me  = state.players[playerIndex];
        Player opp = state.getOpponent(me);

        boolean isOpponentCards = isFromCollection(request.candidates, opp.hand);
        boolean isMyCards = isFromCollection(request.candidates, me.hand);

        Array<CardChoice> choices = new Array<>();
        for (int i = 0; i < request.candidates.size; i++) {
            Card card = request.candidates.get(i);
            int score;
            if (isOpponentCards) {
                score = evaluateCardThreat(card, state, opp);
            } else if (isMyCards) {
                score = evaluateCardForBuff(card, state, me);
            } else {
                score = evaluateCard(card, card.effectiveCost(), state, me);
            }
            choices.add(new CardChoice(card, score));
        }
        choices.sort(Comparator.comparingInt(CardChoice::score).reversed());

        Array<Card> selected = new Array<>();
        for (int i = 0; i < choices.size && selected.size < request.count; i++) {
            selected.add(choices.get(i).card);
        }
        state.completeCardSelection(selected);
    }

    /** 상대 카드의 위협도: AI가 제거/훔칠 때 가장 무서운 카드를 고릅니다 */
    private int evaluateCardThreat(Card card, GameState state, Player owner) {
        int threat = 0;

        if (card.type == Card.ArcanaType.MAJOR) threat += 30;

        threat += card.cost * 5;

        if (card.isExtinction) threat -= 10;

        if (!card.reversed) threat += 10;

        if (isCourtCard(card.id)) threat += 8;
        return threat;
    }

    /** 자기 카드에 버프를 걸 때의 가치: 비싼 카드를 버프하는 게 유리 */
    private int evaluateCardForBuff(Card card, GameState state, Player me) {
        int value = 0;
        value += card.effectiveCost() * 8;
        if (card.type == Card.ArcanaType.MAJOR) value += 15;
        if (!card.reversed) value += 8;
        return value;
    }

    /** 데미지 스코어 계산 (가중치 적용) */
    private static int d(int baseDmg, float mult) {
        return Math.max(0, (int)(baseDmg * mult));
    }

    /** 힐 스코어 계산 (가중치 적용) */
    private static int h(int baseHeal, float mult) {
        return Math.max(0, (int)(baseHeal * mult));
    }

    /** 카드 ID에서 rank 부분 추출 ("Wands/Ace" → "Ace") */
    private static String rank(String id) {
        int slash = id.lastIndexOf('/');
        return slash >= 0 ? id.substring(slash + 1) : id;
    }

    /** 코트 카드 판별 */
    private static boolean isCourtCard(String id) {
        String r = rank(id);
        return r.equals("Page") || r.equals("Knight") || r.equals("Queen") || r.equals("King");
    }

    /** 페이지 카드 판별 */
    private static boolean isPage(String id) {
        return rank(id).equals("Page");
    }

    /** 핸드에서 특정 슈트 카드 수 세기 */
    private static int countSuitInHand(Array<Card> hand, Suit suit) {
        int count = 0;
        for (int i = 0; i < hand.size; i++) {
            if (hand.get(i).suit == suit) count++;
        }
        return count;
    }

    /** 핸드에서 특정 슈트 카드에 대한 코스트 감소 보너스 (페이지 효과) */
    private static int suitCostBonus(Array<Card> hand, Suit suit) {
        int count = countSuitInHand(hand, suit) - 1; // 페이지 자신 제외
        return Math.max(0, count * 4);
    }

    /** 핸드에서 특정 슈트 카드에 대한 코스트 증가 페널티 (역방향 페이지) */
    private static int suitCostPenalty(Array<Card> hand, Suit suit) {
        int count = countSuitInHand(hand, suit) - 1;
        return Math.max(0, count * 4);
    }

    /** 필드 카드 코스트 합 (펜타클 7번 등에 사용) */
    private static int fieldCostSum(Player player) {
        int sum = 0;
        for (int i = 0; i < player.field.size; i++) sum += player.field.get(i).cost;
        return Math.min(sum, 15);
    }

    /** 필드의 펜타클 코스트 합 (펜타클 킹에 사용) */
    private static int fieldPentCostSum(Player player) {
        int sum = 0;
        for (int i = 0; i < player.field.size; i++) {
            Card c = player.field.get(i);
            if (c.suit == Suit.PENTACLES) sum += c.cost;
        }
        return sum;
    }

    /** 필드에 깔린 특정 슈트의 가치 추정 (킹 리플레이 가치) */
    private static int fieldSuitValue(Player player, Suit suit, float mult) {
        int count = 0;
        for (int i = 0; i < player.field.size; i++) {
            if (player.field.get(i).suit == suit) count++;
        }
        return (int)(count * 8 * mult);
    }

    /** 핸드의 총 코스트 (악마 카드 평가에 사용) */
    private static int totalHandCost(Player player) {
        int sum = 0;
        for (int i = 0; i < player.hand.size; i++) sum += player.hand.get(i).effectiveCost();
        return sum;
    }

    /** 후보 카드들이 특정 컬렉션에 속하는지 판별 */
    private static boolean isFromCollection(Array<Card> candidates, Array<Card> collection) {
        if (candidates.size == 0 || collection.size == 0) return false;
        // 후보 중 절반 이상이 컬렉션에 속하면 해당 소속으로 판단
        int matches = 0;
        for (int i = 0; i < candidates.size; i++) {
            Card candidate = candidates.get(i);
            for (int j = 0; j < collection.size; j++) {
                if (candidate == collection.get(j)) { matches++; break; }
            }
        }
        return matches * 2 >= candidates.size;
    }

    /** 배열에서 카드의 인덱스 찾기 */
    private static int indexOf(Array<Card> cards, Card target) {
        for (int i = 0; i < cards.size; i++) {
            if (cards.get(i) == target) return i;
        }
        return -1;
    }

    private static class CardChoice {
        final Card card;
        final int score;

        CardChoice(Card card, int score) {
            this.card = card;
            this.score = score;
        }

        int score() {
            return score;
        }
    }
}
