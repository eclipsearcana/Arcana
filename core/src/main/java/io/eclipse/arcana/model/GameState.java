package io.eclipse.arcana.model;

import com.badlogic.gdx.utils.Array;

public class GameState {

    public static final float TURN_TIME = 20f;

    public final Player[] players = { new Player(), new Player() };
    public int currentPlayerIndex = 0;
    public GamePhase phase = GamePhase.DRAFT;
    public TurnPhase turnPhase = TurnPhase.DRAW;
    public float turnTimer = TURN_TIME;
    public int winnerIndex = -1; // -1 = 승자 없음

    public GameState() {
        setupTest(Suit.WANDS);
    }

    public void setupTest(Suit suit) {
        for (int i = 0; i < players.length; i++) {
            Player p = players[i];
            p.hp = 250;
            p.chosenSuit = suit;
            p.hand.clear();

            Array<Card> deck = CardDefinitions.allMajor();
            deck.addAll(CardDefinitions.allMinor(suit));
            deck.shuffle();
            for (Card c : deck) p.deck.add(c);

            // 시작 핸드 5장 배분
            for (int j = 0; j < 5; j++) {
                Card drawn = p.deck.draw();
                if (drawn != null) p.hand.add(drawn);
            }
        }
        currentPlayerIndex = 0;
        phase = GamePhase.MAIN;
        turnPhase = TurnPhase.DRAW;
        turnTimer = TURN_TIME;
    }

    public Player currentPlayer() {
        return players[currentPlayerIndex];
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
                turnPhase = TurnPhase.ACTION;
                turnTimer = TURN_TIME;
                break;
            case ACTION:
                turnPhase = TurnPhase.END;
                turnTimer = TURN_TIME;
                break;
            case END:
                currentPlayerIndex = 1 - currentPlayerIndex;
                turnPhase = TurnPhase.DRAW;
                turnTimer = TURN_TIME;
                break;
        }
    }
}
