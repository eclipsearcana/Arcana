package io.eclipse.arcana.model;

import com.badlogic.gdx.utils.Array;

public class Player {

    public int hp = 250;
    public Suit chosenSuit;
    public final Deck deck = new Deck();
    public final Array<Card> hand = new Array<>();
    public int reverseStage = 0;

    // 코스트
    public int cost = 6;
    public int costInit = 6;
    public int costMax = 10;
    public boolean carryOver = false; // 코스트 이월
    public final Array<Card> field = new Array<>();

    public boolean saveHalfCostNextTurn = false;

    // 드로우
    public int nextTurnDrawModifier = 0; // 음수일 때 드로우 감소
    public int nextTurnCostModifier = 0;

    // 메이저 카드
    public Card currentCard = null;

    public boolean mustPlayNextDraw = false; // 광대 카드
    public boolean majorBlocked = false; // 교황 카드
    public boolean effectsSwapped = false; // 교황 역카드
    public boolean cannotBeTargeted = false; // 타겟팅 불가
    public boolean forceReversedDraw = false;
    public boolean drawBlocked = false;
    public int playLimit = -1;
    public boolean costIncreasedOnPlay = false;
    public boolean allCardsCostZeroThisTurn = false;
    public boolean randomTargetsThisTurn = false;
    public boolean keepPlayedCardsInHandThisTurn = false;
    public boolean canOverpayCostWithHpThisTurn = false;
    public boolean drawOnEmptyCostThisTurn = false;
    public boolean mustSpendAllCostThisTurn = false;
    public boolean delayEffectsThisTurn = false;
    public int outgoingDamageBonus = 0;
    public int lowHpOutgoingDamageBonus = 0;
    public float effectFailChanceThisTurn = 0f;
    public boolean mirrorEffectsToSelfThisTurn = false;
    public boolean effectsNegatedThisTurn = false;
    public Player effectCostRefundReceiver = null;
    public int handLockTurns = 0;
    public int nextTurnFixedCost = -1;
    public int nextTurnCostMultiplier = 1;
    public int fakeShieldTurns = 0;
    public int fakeShieldBlockedDamage = 0;
    public final Array<GameState.PendingEffect> delayedEffects = new Array<>();

    public Array<Card> removedCards = new Array<>();

    // 마이너 카드
    public float reflectRatio = 0f;
    public int wandsPlayedThisTurn = 0;
    public int cupsPlayedThisTurn = 0;
    public int swordsPlayedThisTurn = 0;
    public int pentaclesPlayedThisTurn = 0;

    public int healMultiplier = 1;
    public boolean healBlocked = false;

    public float incomingDamageMultiplier = 1.0f;
    public int nextTurnPlayLimit = -1;
}
