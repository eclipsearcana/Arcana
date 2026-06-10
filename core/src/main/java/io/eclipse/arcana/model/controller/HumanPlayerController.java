package io.eclipse.arcana.model.controller;

import io.eclipse.arcana.model.GameState;

public class HumanPlayerController implements PlayerController {
    private final int playerIndex;

    public HumanPlayerController(int playerIndex) {
        this.playerIndex = playerIndex;
    }

    @Override
    public boolean controls(int playerIndex) {
        return this.playerIndex == playerIndex;
    }

    @Override
    public boolean acceptsHumanInput() {
        return true;
    }

    @Override
    public void reset() {
    }

    @Override
    public void update(GameState state, int playerIndex, float delta, Actions actions) {
    }
}
