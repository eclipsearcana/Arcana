package io.eclipse.arcana.model.controller;

import io.eclipse.arcana.model.GameState;

// 플레이어 한 명을 조작하는 컨트롤러의 공통 계약입니다.
// Human, AI, Remote 구현체가 같은 GameState 조작 경로를 쓰도록 맞추는 것이 목적입니다.
public interface PlayerController {
    // 컨트롤러가 화면 계층에 요청할 수 있는 최소 동작만 노출합니다.
    // 이렇게 두면 AI나 Remote 컨트롤러가 MainScreen 전체에 의존하지 않습니다.
    interface Actions {
        boolean hasCardAnimation();
        void advanceTurnPhase();
    }

    // 이 컨트롤러가 담당하는 플레이어인지 확인합니다.
    boolean controls(int playerIndex);

    // 사람이 직접 클릭 입력으로 조작할 수 있는 컨트롤러인지 구분합니다.
    boolean acceptsHumanInput();

    // 재시작/턴 전환 등에서 내부 타이머와 임시 판단 상태를 초기화합니다.
    void reset();

    // 매 프레임 호출되는 플레이어 행동 진입점입니다.
    void update(GameState state, int playerIndex, float delta, Actions actions);
}
