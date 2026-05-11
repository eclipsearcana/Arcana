package io.eclipse.arcana.model;

import com.badlogic.gdx.utils.Array;
import io.eclipse.arcana.GameConfig;

public class GameState {
    public final Player[] players = { new Player(), new Player() };
    public int currentPlayerIndex = 0;
    public GamePhase phase = GamePhase.DRAFT;
    public TurnPhase turnPhase = TurnPhase.DRAW;
    public float turnTimer = GameConfig.TURN_TIME;
    public int winnerIndex = -1; // -1 = 승자 없음

    public int roundCount = 0;

    public GameState() {
        setupTest(Suit.WANDS);
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

    public void drawCard(Player player) {
        if (player.hand.size >= GameConfig.HAND_MAX) return;
        Card drawn = player.deck.draw();
        if (drawn != null) {
            applyReverseChance(drawn);
            player.hand.add(drawn);
        }
    }

    public void setupTest(Suit suit) {
        for (int i = 0; i < players.length; i++) {
            Player p = players[i];
            p.hp = GameConfig.PLAYER_HP_START;
            p.cost = GameConfig.COST_DEFAULT_INIT;
            p.costInit = GameConfig.COST_DEFAULT_INIT;
            p.carryOver = false;

            p.nextTurnDrawModifier = 0;
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
                int drawCount = 1 + currentPlayer().nextTurnDrawModifier;
                currentPlayer().nextTurnDrawModifier = 0;
                drawCount = Math.max(0, drawCount);
                for (int i = 0; i < drawCount; i++) {
                    drawCard(currentPlayer());
                }

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
                Player p = currentPlayer();
                if (!p.carryOver) {
                    p.cost = Math.min(p.costInit, p.costMax);
                }
                p.carryOver = false;
                currentPlayerIndex = 1 - currentPlayerIndex;
                for (Card c : p.hand) c.costModifier = 0;
                Player next = currentPlayer();
                next.cost = Math.min(next.costInit, next.costMax);

                roundCount++;
                if (roundCount % 2 == 0) {
                    players[0].field.clear();
                    players[1].field.clear();
                }

                turnPhase = TurnPhase.DRAW;
                turnTimer = GameConfig.TURN_TIME;
                break;
        }
    }
}
