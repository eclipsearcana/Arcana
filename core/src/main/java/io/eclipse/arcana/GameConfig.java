package io.eclipse.arcana;

public class GameConfig {

    // ── 게임 규칙 ─────────────────────────────────────────────────────────────
    public static final int   PLAYER_HP_START   = 150;
    public static final int   COST_DEFAULT_INIT = 8;
    public static final int   COST_MAX          = 10;
    public static final float TURN_TIME         = 20f;
    public static final int   HAND_START_COUNT  = 5;
    public static final int   HAND_MAX = 6;
    public static final int   USED_CARD_RETURN_TURNS = 2;

    public static final float REVERSE_CHANCE_MINOR_NUM   = 0.35f;
    public static final float REVERSE_CHANCE_MINOR_COURT = 0.25f;
    public static final float REVERSE_CHANCE_MAJOR       = 0.20f;
    public static final int   REVERSE_GRACE_TURNS        = 1;
    public static final int   REVERSE_DAMAGE_TIER_1      = 5;
    public static final int   REVERSE_DAMAGE_TIER_2      = 10;
    public static final float REVERSE_DOOM_HP_RATIO      = 0.10f;

    // ── 개발 플래그 ───────────────────────────────────────────────────────────
    public static final boolean DEV_NO_COST_LIMIT = false;
    public static final boolean AI_OPPONENT_ENABLED = true;
    public static final boolean DEV_FORCE_REVERSE = false;

    // ── 월드 ──────────────────────────────────────────────────────────────────
    public static final float WORLD_W = 1600f;
    public static final float WORLD_H = 1000f;

    // ── 핸드 ──────────────────────────────────────────────────────────────────
    public static final float HAND_GAP  = -40f;
    public static final float HAND_P0_Y = -157f;
    public static final float HAND_P1_Y = 843f;

    // ── 호버 ──────────────────────────────────────────────────────────────────
    public static final float HOVER_LIFT  = 30f;
    public static final float HOVER_SPEED = 8f;

    // ── 카드 사용 애니메이션 ──────────────────────────────────────────────────
    public static final float PLAY_TARGET_X = 800f;
    public static final float PLAY_TARGET_Y = 420f;
    public static final float PLAY_SPEED    = 4f;

    // ── 드로우 애니메이션 ─────────────────────────────────────────────────────
    public static final float DRAW_SPEED = 3f;

    // ── 덱 ────────────────────────────────────────────────────────────────────
    public static final float DECK_X              = 350f;
    public static final float DECK_Y              = 550f;
    public static final float DECK_W_SCALE        = 0.8f;
    public static final float DECK_H_SCALE        = 0.6f;
    public static final int   DECK_LAYERS         = 7;
    public static final float DECK_LAYER_OFFSET_X = 2f;
    public static final float DECK_LAYER_OFFSET_Y = 4f;
    public static final float DECK_TAPER = 10f;
    public static final float DECK_LEAN = 30f;

    // ── 필드 ──────────────────────────────────────────────────────────────────
    public static final float FIELD_SCALE = 0.65f;
    public static final float FIELD_GAP   = 10f;
    public static final float FIELD_P0_Y  = 400f;
    public static final float FIELD_P1_Y  = 700f;

    // ── 버튼 ──────────────────────────────────────────────────────────────────
    public static final float BTN_W = 180f;
    public static final float BTN_H = 36f;
    public static final float BTN_X = 1390f;
}
