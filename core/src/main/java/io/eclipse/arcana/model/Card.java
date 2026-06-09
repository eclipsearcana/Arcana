package io.eclipse.arcana.model;

import io.eclipse.arcana.GameConfig;

public class Card {

    public enum ArcanaType { MAJOR, MINOR }
    public enum EffectMark { NONE, POSITIVE, NEGATIVE, LOCKED, SPECIAL }

    public final String id;
    public final String name;
    public final ArcanaType type;
    public final Suit suit;
    public final int cost;
    public final int reversedCost;
    public final boolean isExtinction;

    // The player whose deck this physical card belongs to. Hand swaps and steals do not change it.
    public int ownerIndex = -1;

    public boolean isRevealed = false;

    public int costModifier = 0;
    public int turnCostModifier = 0;
    public boolean lockedInHand = false;
    public EffectMark effectMark = EffectMark.NONE;

    public boolean reversed;
    public int reverseGraceTurns = 0;

    public void setReversed(boolean reversed, boolean grantGrace) {
        this.reversed = reversed;
        if (!reversed) {
            reverseGraceTurns = 0;
        } else {
            reverseGraceTurns = grantGrace ? GameConfig.REVERSE_GRACE_TURNS : 0;
        }
    }

    public void refreshReverseGrace() {
        if (reversed) reverseGraceTurns = GameConfig.REVERSE_GRACE_TURNS;
    }

    public void tickReverseGrace() {
        if (reversed && reverseGraceTurns > 0) reverseGraceTurns--;
    }

    public boolean isReverseGraceActive() {
        return reversed && reverseGraceTurns > 0;
    }

    public boolean isReversePenaltyActive() {
        return reversed && reverseGraceTurns <= 0;
    }

    public boolean shouldFlipIllust() {
        if (!reversed) return false;
        if (type == ArcanaType.MAJOR) return true;
        String rank = id.substring(id.lastIndexOf('/') + 1);
        return rank.equals("Page") || rank.equals("Knight")
            || rank.equals("Queen") || rank.equals("King");
    }

    public int effectiveCost() {
        return Math.max(0, baseCost() + costModifier + turnCostModifier);
    }

    public int baseCost() {
        return reversed ? reversedCost : cost;
    }

    // 광대
    public boolean isCloned = false;
    public boolean isHalfPower = false;

    // 마법사
    public float powerMultiplier = 1.0f;
    public boolean powerMultiplierExpiresEndOfTurn = false;
    public boolean isIllusion = false;

    public Card copy() {
        Card newCard = new Card(
            this.id, this.name, this.type, this.suit, this.cost, this.reversedCost, this.isExtinction);
        newCard.isCloned = true;
        newCard.reversed = this.reversed;
        newCard.effectMark = EffectMark.SPECIAL;
        return newCard;
    }

    public Card(String id, String name, ArcanaType type, Suit suit, int cost, boolean isExtinction) {
        this(id, name, type, suit, cost, cost, isExtinction);
    }

    public Card(String id, String name, ArcanaType type, Suit suit,
                int cost, int reversedCost, boolean isExtinction) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.suit = suit;
        this.cost = cost;
        this.reversedCost = reversedCost;
        this.isExtinction = isExtinction;
    }
}
