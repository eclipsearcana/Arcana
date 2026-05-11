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

    // 드로우
    public int nextTurnDrawModifier = 0; // 음수일 때 드로우 감소

    // 마이너 카드
    public int reflectDamage = 0;
    public int wandsPlayedThisTurn = 0;
    public int cupsPlayedThisTurn = 0;
    public int swordsPlayedThisTurn = 0;
    public int pentaclesPlayedThisTurn = 0;

}
