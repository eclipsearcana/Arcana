package io.eclipse.arcana.model.ai;

import java.util.Comparator;

import com.badlogic.gdx.utils.Array;

import io.eclipse.arcana.GameConfig;
import io.eclipse.arcana.model.Card;
import io.eclipse.arcana.model.Player;

public class BasicAiTurnPlanner {
    public static class Candidate {
        public final Card card;
        public final int score;
        public final int cost;

        public Candidate(Card card, int score, int cost) {
            this.card = card;
            this.score = score;
            this.cost = cost;
        }
    }

    public BasicAiTurnPlan plan(Array<Candidate> candidates, Player player) {
        candidates.sort(Comparator.comparingInt((Candidate candidate) -> candidate.score).reversed());

        BasicAiTurnPlan plan = new BasicAiTurnPlan();
        int remainingCost = player.cost;
        int remainingPlays = player.playLimit;

        for (Candidate candidate : candidates) {
            if (candidate.score <= 0) break;
            if (!GameConfig.DEV_NO_COST_LIMIT
                && !player.canOverpayCostWithHpThisTurn
                && candidate.cost > remainingCost) continue;
            if (remainingPlays == 0) break;

            plan.cardsToPlay.add(candidate.card);
            plan.expectedScore += candidate.score;
            remainingCost = Math.max(0, remainingCost - candidate.cost);
            if (remainingPlays > 0) remainingPlays--;
        }
        return plan;
    }
}
