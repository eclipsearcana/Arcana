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

/**
 * AI 상대 구현체.
 * 카드 효과를 인식하고, HP/상황에 따라 전략을 전환합니다.
 * <ul>
 *   <li>마이너 넘버 카드: 실제 데미지/힐/경제 효과 기반 스코어링</li>
 *   <li>코트 카드: 정방향/역방향 효과를 구분하여 평가</li>
 *   <li>메이저 아르카나: 전략적 가치 + 상황 의존 보정</li>
 *   <li>시너지: 페이지 → 동일 슈트, 10번 카드 → 슈트 플레이 카운트</li>
 *   <li>역방향 페널티: 역방향 카드 제거 인센티브</li>
 * </ul>
 */
public class SimpleAiOpponent implements PlayerController {

    private static final float THINK_TIME = 0.65f;
    private static final float HP_MAX     = GameConfig.PLAYER_HP_START;

    private final int playerIndex;
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

        // 바로 행동하면 기계적으로 보여서, 짧은 생각 시간을 둡니다.
        timer -= delta;
        if (timer > 0f) return;
        timer = THINK_TIME;

        // 선택형 카드 효과가 pending 상태면 턴 진행보다 선택 해결이 먼저입니다.
        if (state.pendingSelection != null) {
            completeSelection(state);
            return;
        }

        // DRAW/END는 현재 별도 판단이 없으므로 자동으로 다음 페이즈로 넘깁니다.
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

    // ════════════════════════════════════════════════════════════════
    //  카드 스테이징
    // ════════════════════════════════════════════════════════════════

    private void stagePlayableCards(GameState state, Player player) {
        Array<CardChoice> choices = buildPlayableChoices(state, player);
        choices.sort(Comparator.comparingInt(CardChoice::score).reversed());

        // GameState의 검증 로직을 그대로 사용하기 위해 hand index를 다시 찾아 스테이징합니다.
        for (CardChoice choice : choices) {
            int handIndex = indexOf(player.hand, choice.card);
            if (handIndex < 0) continue;
            state.stageCardFromHand(player, handIndex);
        }
    }

    private Array<CardChoice> buildPlayableChoices(GameState state, Player player) {
        Array<CardChoice> choices = new Array<>();

        // 현재 턴에 낼 수 없는 카드는 후보에서 제외합니다.
        for (int i = 0; i < player.hand.size; i++) {
            Card card = player.hand.get(i);
            if (card.lockedInHand) continue;
            if (player.majorBlocked && card.type == Card.ArcanaType.MAJOR) continue;
            int cost = state.effectiveCostFor(player, card);
            if (!GameConfig.DEV_NO_COST_LIMIT
                && !player.canOverpayCostWithHpThisTurn && cost > player.cost) continue;
            choices.add(new CardChoice(card, evaluateCard(card, cost, state, player)));
        }
        return choices;
    }

    // ════════════════════════════════════════════════════════════════
    //  카드 평가 (메인 로직)
    // ════════════════════════════════════════════════════════════════

    private int evaluateCard(Card card, int cost, GameState state, Player me) {
        Player opp = state.getOpponent(me);
        boolean reversed = isEffectivelyReversed(card, me);

        // 카드 효과로부터 예상되는 가치
        int effectValue = estimateEffectValue(card, reversed, state, me, opp);

        // 코스트 효율: 낮은 비용의 카드를 약간 선호
        int score = effectValue - cost * 4;

        // 역방향 카드를 내면 핸드에서 제거 → 역방향 페널티 감소
        if (card.reversed && card.isReversePenaltyActive()) {
            int penaltyCount = state.countReversePenaltyCards(me);
            score += 8 + penaltyCount * 5;
        }

        // 페이지 카드 시너지: 핸드에 같은 슈트 카드가 많을수록 보너스
        if (card.suit != null && isPage(card.id)) {
            int suitCount = countSuitInHand(me.hand, card.suit) - 1; // 자기 자신 제외
            if (suitCount > 0) score += suitCount * 5;
        }

        return score;
    }

    /** effectsSwapped 상태를 반영한 실제 효과 방향 */
    private boolean isEffectivelyReversed(Card card, Player p) {
        return card.reversed ^ p.effectsSwapped;
    }

    // ════════════════════════════════════════════════════════════════
    //  효과 가치 추정 — 카드 종류별 분기
    // ════════════════════════════════════════════════════════════════

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
            ? majorReversed(card, state, me, opp, dm, hl)
            : majorUpright(card, state, me, opp, dm, hl);
    }

    // ────────────────────────────────────────────
    //  마이너 넘버 카드 (Ace–Ten): 정/역 효과 동일
    // ────────────────────────────────────────────

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
            case "Ace":   return d(5, dm);                                   // 데미지 5
            case "Two":   return d(4, dm) + 5;                               // 데미지 4 + 코스트 +1
            case "Three": return d(4, dm) + 8;                               // 데미지 4 + 드로우 1
            case "Four":  return d(3, dm) + 4;                               // 데미지 3 + 힐 5
            case "Five":  return d(8, dm) + 6;                               // 데미지 8 + 상대 드로우 -1
            case "Six":   return d(8, dm) + 5;                               // 데미지 8 + 코스트 +1
            case "Seven": return d(8, dm) + 5;                               // 데미지 8 + 반사 30%
            case "Eight": return d(12, dm);                                  // 데미지 6×2
            case "Nine": {                                                   // 데미지 12 + 잃은 HP 비례 보너스
                float missingRatio = 1f - me.hp / HP_MAX;
                return d((int)(12 + missingRatio * 12), dm);
            }
            case "Ten":                                                      // 데미지 12 + 완드 카운트×3
                return d(12 + me.wandsPlayedThisTurn * 3, dm);
            default: return d(6, dm);
        }
    }

    private int cupsNumber(String rank, Player me, float dm, float hl) {
        switch (rank) {
            case "Ace":   return h(7, hl);                                   // 힐 7
            case "Two":   return h(6, hl) + 8;                               // 힐 6 + 드로우 1
            case "Three": return 6;                                          // 인터랙티브: 카드 1장 코스트 -1
            case "Four":  return d(3, dm) + 6;                               // 데미지 3 + 다음 드로우 +1
            case "Five":  return h(11, hl) + d(5, dm);                       // 힐 11 + 데미지 5
            case "Six":   return 10;                                         // 인터랙티브: 카드 2장 코스트 -1
            case "Seven": return 24;                                         // 드로우 3
            case "Eight": return h(13, hl) + 6;                              // 힐 13 + 상대 드로우 -1
            case "Nine":  return h(15, hl) + 5;                              // 힐 15 + 코스트 +1
            case "Ten":   return h(me.cupsPlayedThisTurn * 4, hl);           // 힐 컵 카운트×4
            default: return h(8, hl);
        }
    }

    private int swordsNumber(String rank, float dm) {
        switch (rank) {
            case "Ace":   return d(4, dm);                                   // 데미지 4
            case "Two":   return d(3, dm) + 4;                               // 데미지 3 + 상대 코스트 -1
            case "Three": return d(5, dm);                                   // 데미지 5
            case "Four":  return 8;                                          // 받는 데미지 ×0.5
            case "Five":  return d(6, dm) + 8;                               // 데미지 6 + 상대 카드 1장 버리기
            case "Six":   return d(6, dm) + 6;                               // 데미지 6 + 상대 드로우 -1
            case "Seven": return d(7, dm) + 10;                              // 데미지 7 + 상대 카드 1장 훔치기
            case "Eight": return d(7, dm) + 13;                              // 데미지 7 + 상대 코스트 -2, 자신 +1
            case "Nine":  return d(10, dm) + 2;                              // 데미지 10 + 카드 공개
            case "Ten":   return d(11, dm) + 15;                             // 데미지 11 + 플레이 제한 1장
            default: return d(6, dm);
        }
    }

    private int pentaclesNumber(String rank, Player me, float dm) {
        switch (rank) {
            case "Ace":   return 10;                                         // 코스트 +2
            case "Two":   return d(3, dm) + 5;                               // 데미지 3 + 코스트 +1
            case "Three": return 7;                                          // 인터랙티브: 넘버 카드 1장 코스트 0
            case "Four":  return 6;                                          // 남은 코스트 절반 이월
            case "Five":  return d(6, dm) + 10;                              // 데미지 6 + 코스트 +2
            case "Six":   return 14;                                         // 상대 코스트 -2, 자신 +2
            case "Seven": return d(fieldCostSum(me), dm);                    // 데미지 = 필드 카드 코스트 합
            case "Eight": return d(7, dm) + 16;                              // 데미지 7 + 다음 턴 코스트 +4
            case "Nine":  return d(9, dm) + suitCostBonus(me.hand, Suit.PENTACLES); // 데미지 9 + 펜타클 코스트 -1
            case "Ten":   return d(me.pentaclesPlayedThisTurn * 3, dm);      // 데미지 = 펜타클 카운트×3
            default: return 8;
        }
    }

    // ────────────────────────────────────────────
    //  마이너 코트 카드 (Page/Knight/Queen/King): 정방향
    // ────────────────────────────────────────────

    private int minorCourtUpright(Card card, GameState state,
                                   Player me, Player opp, float dm, float hl) {
        String court = rank(card.id);
        if (card.suit == null) return 12;

        switch (card.suit) {
            case WANDS:
                switch (court) {
                    case "Page":   return suitCostBonus(me.hand, Suit.WANDS);    // 완드 코스트 -1
                    case "Knight": return d(14, dm) - 10;                        // 데미지 14, 자해 10
                    case "Queen":  return d(13, dm) + suitCostBonus(me.hand, Suit.WANDS);
                    case "King":   return fieldSuitValue(me, Suit.WANDS, dm);    // 필드 완드 리플레이
                }
                break;
            case CUPS:
                switch (court) {
                    case "Page":   return suitCostBonus(me.hand, Suit.CUPS);
                    case "Knight": return 12;                                    // 힐 ×2
                    case "Queen":  return d(8, dm) + h(7, hl);
                    case "King":   return fieldSuitValue(me, Suit.CUPS, hl);     // 필드 컵 힐 리플레이
                }
                break;
            case SWORDS:
                switch (court) {
                    case "Page":   return suitCostBonus(me.hand, Suit.SWORDS);
                    case "Knight": return 20;                                    // 상대 카드 1장 제거 (인터랙티브)
                    case "Queen":  return d(10, dm) + 4;                         // 데미지 10 + 공개 3장
                    case "King":   return fieldSuitValue(me, Suit.SWORDS, dm);
                }
                break;
            case PENTACLES:
                switch (court) {
                    case "Page":   return suitCostBonus(me.hand, Suit.PENTACLES);
                    case "Knight": return d((int)(me.cost * 3), dm);             // 50%확률 ×6 per cost
                    case "Queen":  return d(9, dm) + 12;                         // 데미지 9 + 상대 코스트 -3
                    case "King":   return fieldSuitValue(me, Suit.PENTACLES, dm);
                }
                break;
        }
        return 12;
    }

    // ────────────────────────────────────────────
    //  마이너 코트 카드: 역방향
    // ────────────────────────────────────────────

    private int minorCourtReversed(Card card, GameState state,
                                    Player me, Player opp, float dm, float hl) {
        String court = rank(card.id);
        if (card.suit == null) return 0;

        switch (card.suit) {
            case WANDS:
                switch (court) {
                    case "Page":   return -suitCostPenalty(me.hand, Suit.WANDS);   // 완드 코스트 +1 (불리)
                    case "Knight": return d(20, dm) - 25;                          // 데미지 20, 자해 25
                    case "Queen":  return d(17, dm) - 17;                          // 데미지 17, 자해 17
                    case "King":   return d(6, dm);                                // 마지막 완드만 리플레이
                }
                break;
            case CUPS:
                switch (court) {
                    case "Page":   return -suitCostPenalty(me.hand, Suit.CUPS);
                    case "Knight": return 12 - 10;                                 // 힐 ×2이지만 카드 2장 버리기
                    case "Queen":  return d(11, dm) + 8;                           // 데미지 11 + 상대 힐 차단
                    case "King":   return 6;                                       // 컵 리플레이 + 코스트 0 (리스크)
                }
                break;
            case SWORDS:
                switch (court) {
                    case "Page":   return -suitCostPenalty(me.hand, Suit.SWORDS);
                    case "Knight": return d(13, dm) - 6;                           // 데미지 13, 자신 카드 1장 버리기
                    case "Queen":  return d(11, dm) - 3;                           // 데미지 11 + 자신 카드 공개
                    case "King":   return -8;                                      // 간섭을 자신에게 리플레이 (불리)
                }
                break;
            case PENTACLES:
                switch (court) {
                    case "Page":   return -suitCostPenalty(me.hand, Suit.PENTACLES);
                    case "Knight": {                                               // 코스트 소모 → 데미지 1.5배, 자해
                        int c = me.cost;
                        return d((int)(c * 1.5f), dm) - c;
                    }
                    case "Queen":  return d(15, dm) - 16;                          // 데미지 15, 핸드 버리기+드로우 차단
                    case "King":   return -fieldPentCostSum(me) * 2;               // 자해: 필드 펜타클 코스트 합 ×2
                }
                break;
        }
        return 0;
    }

    // ────────────────────────────────────────────
    //  메이저 아르카나: 정방향
    // ────────────────────────────────────────────

    private int majorUpright(Card card, GameState state,
                              Player me, Player opp, float dm, float hl) {
        float myHpR  = me.hp / HP_MAX;

        switch (card.id) {
            case "Fool":                                                    // 핸드 카드 복제 (하프파워)
                return me.hand.size > 1 ? 14 : 2;
            case "Magician":                                                // 카드 1장 코스트 0 + 파워 ×2
                return me.hand.size > 1 ? 22 : 4;
            case "Priestess":                                               // 덱 상위 3장 미리보기, 1장 가져오기
                return 12;
            case "Empress":                                                 // 힐 15% (≈22HP)
                return h(22, hl);
            case "Emperor":                                                 // 상대 다음 턴 카드 1장 제한
                return 18;
            case "Hierophant":                                              // 상대 메이저 카드 차단
                return 16;
            case "Lovers":                                                  // 자신+상대 카드 1장씩 코스트 0
                return me.hand.size > 1 ? 14 : 4;
            case "Chariot":                                                 // 카드 사용 후 핸드에 유지
                return me.hand.size >= 3 ? 20 : 8;
            case "Strength":                                                // 데미지 +3 (+3 더 at HP≤35%)
                return myHpR <= 0.35f ? d(6, dm) + 8 : d(3, dm) + 8;
            case "Hermit":                                                  // 타겟팅 불가
                return 14;
            case "Fortune":                                                 // 핸드 교환
                return opp.hand.size > me.hand.size ? 16 : 6;
            case "Justice":                                                 // 상대 핸드 크기 맞추기
                return opp.hand.size > me.hand.size ? 14 : 4;
            case "HangedMan":                                               // HP 10% 희생, 핸드 버리고 새로 드로우
                return myHpR > 0.4f ? 12 : 2;
            case "Death":                                                   // 필드/묘지 초기화, 양쪽 5장 드로우 (소멸)
                return 10;
            case "Temperance":                                              // 역방향 비율 균형 조정
                return state.countReversePenaltyCards(me) >= 2 ? 18 : 6;
            case "Devil":                                                   // HP로 코스트 초과 지불 가능
                return me.cost < totalHandCost(me) ? 14 : 6;
            case "Tower":                                                   // 상대 다음 드로우 차단 (소멸)
                return 16;
            case "Star":                                                    // 코스트 전부 사용 시: 드로우 1 + 다음 턴 코스트 ×2
                return 16;
            case "Moon":                                                    // 양쪽 핸드 숨기고 랜덤 1장씩 강제
                return 10;
            case "Sun":                                                     // 코스트 +3
                return 15;
            case "Judgement":                                                // 제거된 카드 최대 2장 부활
                return me.removedCards.size >= 1 ? 14 : 2;
            case "World":                                                   // 상대 핸드 공개 + 1장 훔치기 (소멸)
                return opp.hand.size > 0 ? 20 : 2;
            default: return 10;
        }
    }

    // ────────────────────────────────────────────
    //  메이저 아르카나: 역방향
    // ────────────────────────────────────────────

    private int majorReversed(Card card, GameState state,
                               Player me, Player opp, float dm, float hl) {
        float myHpR  = me.hp / HP_MAX;
        boolean hasDebuffs = me.majorBlocked || me.effectsSwapped
            || me.drawBlocked || me.effectsNegatedThisTurn
            || me.costIncreasedOnPlay || me.handLockTurns > 0
            || me.fakeShieldTurns > 0;

        switch (card.id) {
            case "Fool":                                                    // 다음 드로우 강제 사용
                return -4;
            case "Magician":                                                // 환상 복사 (50% 실패) (소멸)
                return me.hand.size > 1 ? 8 : 0;
            case "Priestess":                                               // 자신 카드 2장 공개
                return -6;
            case "Empress":                                                 // 카드 사용 시 코스트 +1~2
                return -10;
            case "Emperor":                                                 // 전부 코스트 0이지만 타겟 랜덤
                return 8;
            case "Hierophant":                                              // 정/역 효과 교환
                return -8;
            case "Lovers":                                                  // 랜덤 자기 카드 2장 중 1장 강제 사용
                return -6;
            case "Chariot":                                                 // 핸드 버리고 같은 수 드로우
                return me.hand.size >= 3 ? 8 : 4;
            case "Strength":                                                // 50% 확률로 효과 실패
                return -12;
            case "Hermit":                                                  // 다음 드로우 전부 역방향
                return -10;
            case "Fortune":                                                 // 카드 효과가 자신에게도 적용
                return -8;
            case "Justice":                                                 // 상대 효과 무효화, 코스트 자신에게 환불
                return 16;
            case "HangedMan":                                               // 상대 카드 1장 잠금
                return opp.hand.size > 0 ? 10 : 0;
            case "Death":                                                   // 양쪽 핸드 2턴 잠금 (소멸)
                return -6;
            case "Temperance":                                              // 전부 정/역 반전
                return state.countReversePenaltyCards(me) >= 3 ? 12 : -4;
            case "Devil":                                                   // 자신 디버프 전부 해제
                return hasDebuffs ? 18 : 3;
            case "Tower":                                                   // 자신 코스트 랜덤 1~6 (소멸)
                return -12;
            case "Star":                                                    // 가짜 방패 2턴 (데미지 흡수 후 역풍)
                return -4;
            case "Moon":                                                    // 상대 카드 2장 공개
                return 6;
            case "Sun":                                                     // 남은 코스트 = HP 피해
                return -14;
            case "Judgement":                                                // 카드 효과 1턴 지연
                return -6;
            case "World":                                                   // 상대 다음 턴 코스트 3 고정 (소멸)
                return 10;
            default: return 0;
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  인터랙티브 선택 처리
    // ════════════════════════════════════════════════════════════════

    private void completeSelection(GameState state) {
        GameState.CardSelectionRequest request = state.pendingSelection;
        Player me  = state.players[playerIndex];
        Player opp = state.getOpponent(me);

        // 후보 카드가 상대 핸드 소속인지 판별
        boolean isOpponentCards = isFromCollection(request.candidates, opp.hand);
        // 후보 카드가 자기 핸드 소속인지 판별
        boolean isMyCards = isFromCollection(request.candidates, me.hand);

        Array<CardChoice> choices = new Array<>();
        for (int i = 0; i < request.candidates.size; i++) {
            Card card = request.candidates.get(i);
            int score;
            if (isOpponentCards) {
                // 상대 카드 중 선택: 가장 위협적인 카드를 고르기 (제거/훔치기)
                score = evaluateCardThreat(card, state, opp);
            } else if (isMyCards) {
                // 자기 카드 중 선택: 가장 가치 높은 카드를 고르기 (버프 대상)
                score = evaluateCardForBuff(card, state, me);
            } else {
                // 덱/기타 소스: 일반 카드 가치 평가
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
        // 메이저 카드는 일반적으로 더 위협적
        if (card.type == Card.ArcanaType.MAJOR) threat += 30;
        // 코스트가 높은 카드가 더 강력
        threat += card.cost * 5;
        // 소멸 카드는 일회성이므로 제거 가치 낮음
        if (card.isExtinction) threat -= 10;
        // 정방향이면 더 위협적
        if (!card.reversed) threat += 10;
        // 코트 카드 우선
        if (isCourtCard(card.id)) threat += 8;
        return threat;
    }

    /** 자기 카드에 버프를 걸 때의 가치: 비싼 카드를 버프하는 게 유리 */
    private int evaluateCardForBuff(Card card, GameState state, Player me) {
        int value = 0;
        // 코스트가 높은 카드일수록 코스트 0/파워업 효과가 크다
        value += card.effectiveCost() * 8;
        // 메이저 카드 버프 우선
        if (card.type == Card.ArcanaType.MAJOR) value += 15;
        // 정방향 카드 우선
        if (!card.reversed) value += 8;
        return value;
    }

    // ════════════════════════════════════════════════════════════════
    //  유틸리티 함수들
    // ════════════════════════════════════════════════════════════════

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

    // ════════════════════════════════════════════════════════════════
    //  내부 자료형
    // ════════════════════════════════════════════════════════════════

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
