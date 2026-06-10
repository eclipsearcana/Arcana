package io.eclipse.arcana.model.controller;

import io.eclipse.arcana.model.GameState;

public interface PlayerController {
    interface Actions {
        boolean hasCardAnimation();
        void advanceTurnPhase();
    }

    boolean controls(int playerIndex);

    boolean acceptsHumanInput();

    void reset();

    void update(GameState state, int playerIndex, float delta, Actions actions);
}
