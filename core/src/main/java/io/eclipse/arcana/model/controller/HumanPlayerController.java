package io.eclipse.arcana.model.controller;

import io.eclipse.arcana.model.GameState;

// 화면 클릭 입력으로 조작되는 플레이어 컨트롤러입니다.
// 실제 클릭 처리는 MainScreen에 남겨두고, 슬롯 구조상 "사람이 조작한다"는 사실만 표현합니다.
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
