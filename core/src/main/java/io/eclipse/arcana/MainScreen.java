package io.eclipse.arcana;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.eclipse.arcana.model.*;
import io.eclipse.arcana.render.CardRenderer;

public class MainScreen implements Screen {
    // 레이아웃
    private static final float GAP = GameConfig.HAND_GAP;
    private static final float HAND0_Y = GameConfig.HAND_P0_Y;
    private static final float HAND1_Y = GameConfig.HAND_P1_Y;

    // 덱
    private static final float DECK_X = GameConfig.DECK_X;
    private static final float DECK_Y = GameConfig.DECK_Y;
    private static final float DECK_W = CardRenderer.CARD_W * GameConfig.DECK_W_SCALE;
    private static final float DECK_H = CardRenderer.CARD_H * GameConfig.DECK_H_SCALE;
    private static final int   DECK_LAYERS = GameConfig.DECK_LAYERS;

    // 필드
    private static final float FIELD_CARD_W = CardRenderer.CARD_W * GameConfig.FIELD_SCALE;
    private static final float FIELD_CARD_H = CardRenderer.CARD_H * GameConfig.FIELD_SCALE;
    private static final float FIELD_GAP = GameConfig.FIELD_GAP;
    private static final float FIELD_P0_Y = GameConfig.FIELD_P0_Y;
    private static final float FIELD_P1_Y = GameConfig.FIELD_P1_Y;

    private static final float GRAVE_X = 1085f;
    private static final float GRAVE_P0_Y = 245f;
    private static final float GRAVE_P1_Y = 575f;
    private static final float GRAVE_W = 270f;
    private static final float GRAVE_H = 130f;
    private static final float GRAVE_CARD_SCALE = 0.32f;
    private static final float GRAVE_CARD_GAP = 8f;

    // 호버
    private static final float HOVER_LIFT = GameConfig.HOVER_LIFT;
    private static final float HOVER_SPEED = GameConfig.HOVER_SPEED;

    // 애니메이션
    private static final float PLAY_TARGET_X = GameConfig.PLAY_TARGET_X;
    private static final float PLAY_TARGET_Y = GameConfig.PLAY_TARGET_Y;
    private static final float PLAY_SPEED = GameConfig.PLAY_SPEED;
    private static final float DRAW_SPEED = GameConfig.DRAW_SPEED;
    private static final float BURIAL_SPEED = 2.4f;

    // 버튼
    private static final float BTN_W = GameConfig.BTN_W;
    private static final float BTN_H = GameConfig.BTN_H;
    private static final float BTN_X = GameConfig.BTN_X;

    private Card drawingCard    = null;
    private int drawingOwnerIndex = -1;
    private float drawProgress   = 0f;
    private float drawStartX, drawStartY;
    private float drawEndX, drawEndY;

    private static final Color COL_BTN_NORMAL = new Color(0.15f, 0.15f, 0.25f, 1f);
    private static final Color COL_BTN_DANGER = new Color(0.25f, 0.08f, 0.08f, 1f);
    private static final Color COL_BTN_OK     = new Color(0.10f, 0.30f, 0.10f, 1f);
    private static final Color COL_BTN_BORDER = new Color(0.7f,  0.6f,  0.2f,  1f);
    private static final Color COL_GRAVE_SHADOW = new Color(0.005f, 0.008f, 0.025f, 0.58f);
    private static final Color COL_GRAVE_PLATE = new Color(0.025f, 0.035f, 0.10f, 0.78f);
    private static final Color COL_GRAVE_INNER = new Color(0.015f, 0.022f, 0.07f, 0.72f);
    private static final Color COL_GRAVE_GOLD = new Color(0.47f, 0.36f, 0.20f, 0.82f);
    private static final Color COL_GRAVE_GOLD_DIM = new Color(0.35f, 0.27f, 0.16f, 0.40f);
    private static final Color COL_GRAVE_STAR = new Color(0.60f, 0.48f, 0.27f, 0.70f);
    private static final Color COL_GRAVE_SLOT = new Color(0.01f, 0.015f, 0.05f, 0.76f);
    private static final Color COL_HP_FRAME = new Color(0.55f, 0.43f, 0.25f, 0.92f);
    private static final Color COL_HP_BAR_BG = new Color(0.025f, 0.018f, 0.065f, 0.96f);
    private static final Color COL_HP_BAR_TRAIL = new Color(0.82f, 0.30f, 0.95f, 0.42f);
    private static final Color COL_HP_BAR_FILL = new Color(0.48f, 0.12f, 0.88f, 1f);
    private static final Color COL_HP_BAR_CORE = new Color(0.88f, 0.45f, 1f, 0.92f);
    private static final Color COL_HP_BAR_LOW = new Color(0.76f, 0.20f, 0.25f, 1f);
    private static final Color COL_COST_GEM = new Color(0.54f, 0.20f, 0.98f, 1f);
    private static final Color COL_COST_GEM_CORE = new Color(0.88f, 0.62f, 1f, 1f);
    private static final Color COL_COST_EMPTY = new Color(0.045f, 0.04f, 0.09f, 0.96f);
    private static final Color COL_TOOLTIP_SHADOW = new Color(0f, 0f, 0f, 0.42f);
    private static final Color COL_TOOLTIP_BG = new Color(0.045f, 0.045f, 0.052f, 0.97f);
    private static final Color COL_TOOLTIP_LINE = new Color(1f, 1f, 1f, 0.10f);
    private static final Color COL_TOOLTIP_MUTED = new Color(0.67f, 0.68f, 0.72f, 1f);
    private static final Color COL_TOOLTIP_ACTIVE = new Color(1f, 0.83f, 0.35f, 1f);
    private static final Color COL_TOOLTIP_REVERSED = new Color(0.93f, 0.58f, 0.68f, 1f);
    private static final Color COL_SELECTION_PANEL = new Color(0.035f, 0.04f, 0.075f, 0.98f);
    private static final Color COL_SELECTION_BORDER = new Color(0.58f, 0.34f, 0.92f, 1f);
    private static final Color COL_SELECTION_HOVER = new Color(0.22f, 0.86f, 1f, 1f);
    private static final Color COL_SELECTION_SELECTED = new Color(0.30f, 0.95f, 0.58f, 1f);
    private static final Rectangle SELECTION_PANEL = new Rectangle(1050f, 278f, 320f, 126f);
    private static final Rectangle SELECTION_CONFIRM = new Rectangle(1250f, 292f, 96f, 34f);
    private static final float SELECTION_CARD_SCALE = 0.42f;
    private static final float SELECTION_CARD_GAP = 18f;
    private static final float SELECTION_LIFT = 42f;

    private static final float HP_BAR_X = 173f;
    private static final float HP_BAR_W = 261f;
    private static final float HP_BAR_H = 18f;
    private static final float HP_P0_Y = 252f;
    private static final float HP_P1_Y = 842f;
    private static final float HP_PANEL_X = 47f;
    private static final float HP_PANEL_W = 430f;
    private static final float HP_PANEL_H = HP_PANEL_W * 793f / 1983f;
    private static final float HP_PANEL_Y_OFFSET = -80f;
    private static final float RUNE_CENTER_X = HP_PANEL_X + HP_PANEL_W * (294f / 1983f);
    private static final float RUNE_CENTER_Y_OFFSET = HP_PANEL_Y_OFFSET
        + HP_PANEL_H * ((793f - 382f) / 793f);
    private static final float RUNE_SIZE = 84f;
    private static final float HP_FRAME_X = 156f;
    private static final float HP_FRAME_W = 296f;
    private static final float HP_FRAME_H = HP_FRAME_W / 4f;
    private static final float HP_FRAME_Y_OFFSET = -40f;
    private static final float HP_FILL_Y_OFFSET = -10f;
    private static final float COST_UI_X = 156f;
    private static final float COST_UI_W = 296f;
    private static final float COST_UI_H = COST_UI_W * 770f / 2041f;
    private static final float COST_UI_Y_OFFSET = -82f;
    private static final float[] COST_SLOT_RATIOS = {
        132f / 2041f, 330f / 2041f, 527f / 2041f, 724f / 2041f, 921f / 2041f,
        1119f / 2041f, 1316f / 2041f, 1513f / 2041f, 1709f / 2041f, 1906f / 2041f
    };

    // DEBUG PANEL
    private static final float PANEL_X     = 1390f;
    private static final float PANEL_Y     = 380f;
    private static final float PANEL_W     = 180f;
    private static final float TOOLTIP_W = 330f;
    private static final float TOOLTIP_H = 178f;
    private static final float TOOLTIP_PAD = 18f;
    private static final float TOOLTIP_RADIUS = 17f;
    private static final float TOOLTIP_LINE_H = 15f;
    private static final float TOOLTIP_TAIL_W = 16f;
    private static final float TOOLTIP_TAIL_H = 11f;

    private final Rectangle btnDraw    = new Rectangle(PANEL_X + 5f, PANEL_Y - 50f,  PANEL_W - 10f, BTN_H);
    private final Rectangle btnPhase   = new Rectangle(PANEL_X + 5f, PANEL_Y - 95f,  PANEL_W - 10f, BTN_H);
    private final Rectangle btnDebug   = new Rectangle(PANEL_X + 5f, PANEL_Y - 140f, PANEL_W - 10f, BTN_H);
    private final Rectangle btnRestart = new Rectangle(PANEL_X + 5f, PANEL_Y - 185f, PANEL_W - 10f, BTN_H);
    private static final float STAGED_CARD_SCALE = 0.44f;
    private static final float STAGED_CARD_GAP = 12f;
    private static final float STAGED_CENTER_X = 185f;
    private static final float STAGED_CENTER_Y = 510f;
    private static final float SELECTION_CARDS_Y = 430f;

    private final Core game;
    private final DebugContext debugContext;
    private final Suit selectedSuit;
    private final Array<Card> playerDraftMajors;
    private final Array<Card> opponentDraftMajors;

    private SpriteBatch   batch;
    private ShapeRenderer shape;
    private OrthographicCamera camera;
    private FitViewport   viewport;
    private FontManager   fonts;
    private ArcanaAssets  assets;  // ← 추가

    private GameState state;
    private final Vector2 touch = new Vector2();
    private final Vector2 mouse  = new Vector2();

    // 호버 상태
    private int hoveredIndex = -1;
    private final float[] hoverAnims = new float[10];
    private Card stagedSelectionCard;
    private GameState.CardSelectionRequest stagedSelectionRequest;
    private Card tooltipCard = null;
    private float tooltipAnim = 0f;
    private float tooltipAnimVelocity = 0f;
    private float tooltipAnchorX = GameConfig.WORLD_W / 2f;
    private float tooltipDisplayX = GameConfig.WORLD_W / 2f;
    private float tooltipDisplayVelocity = 0f;
    private final float[] displayedHp = new float[2];
    private final float[] trailingHp = new float[2];
    private final int[] previousHp = new int[2];
    private final float[] hpFlash = new float[2];
    private final int[] previousCost = new int[2];
    private final float[][] costGainAnim = new float[2][10];
    private final float[][] costSpendAnim = new float[2][10];
    private float hudTime = 0f;

    // 카드 사용 애니메이션 상태
    private Card  playingCard    = null;
    private int   playingOwnerIndex = -1;
    private boolean playingCardLandsInField = false;
    private float playStartX, playStartY;
    private float playEndX, playEndY;
    private float playStartScale = 1f, playEndScale = 1f;
    private float playProgress   = 0f;
    private final Array<BurialAnimation> burialAnims = new Array<>();
    private final Array<PlayAnimationRequest> queuedPlayAnimations = new Array<>();
    private final Array<Array<Card>> previousHands = new Array<>();
    private final Array<Array<Card>> previousFields = new Array<>();
    private final Array<Array<Card>> previousGraveyards = new Array<>();
    private final Array<Array<Card>> previousStagedCards = new Array<>();

    private static class BurialAnimation {
        final Card card;
        final int ownerIndex;
        final float startX, startY;
        float endX, endY;
        float progress;

        BurialAnimation(Card card, int ownerIndex, float startX, float startY) {
            this.card = card;
            this.ownerIndex = ownerIndex;
            this.startX = startX;
            this.startY = startY;
        }
    }

    private static class PlayAnimationRequest {
        final Card card;
        final Player owner;
        final int ownerIndex;
        final float startX, startY;

        PlayAnimationRequest(Card card, Player owner, int ownerIndex, float startX, float startY) {
            this.card = card;
            this.owner = owner;
            this.ownerIndex = ownerIndex;
            this.startX = startX;
            this.startY = startY;
        }
    }

    public MainScreen(Core game) {
        this(game, null, Suit.SWORDS);
    }

    public MainScreen(Core game, DebugContext debugContext) {
        this(game, debugContext, Suit.SWORDS);
    }

    public MainScreen(Core game, DebugContext debugContext, Suit selectedSuit) {
        this(game, debugContext, selectedSuit, null, null);
    }

    public MainScreen(Core game, DebugContext debugContext, Suit selectedSuit,
                      Array<Card> playerDraftMajors, Array<Card> opponentDraftMajors) {
        this.game = game;
        this.debugContext = debugContext;
        this.selectedSuit = selectedSuit;
        this.playerDraftMajors = playerDraftMajors;
        this.opponentDraftMajors = opponentDraftMajors;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(1600f, 1000f, camera);
        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        fonts = new FontManager(Gdx.graphics.getWidth() / 1600f);

        assets = game.assets;

        state = playerDraftMajors == null
            ? new GameState(selectedSuit)
            : new GameState(selectedSuit, playerDraftMajors, opponentDraftMajors);
        resetHpDisplay();
        resetCardZoneSnapshots();
        if (debugContext != null) {
            debugContext.setState(state);
        }
    }

    @Override
    public void render(float delta) {
        processDebugCommands();
        int beforePlayerIndex = state.currentPlayerIndex;
        TurnPhase beforeTurnPhase = state.turnPhase;
        int beforeHandSize = state.players[beforePlayerIndex].hand.size;
        Array<BurialAnimation> pendingBurials = captureBurialAnimationStarts(beforePlayerIndex, beforeTurnPhase);
        if (!isDebugMode()) {
            state.update(delta);
        }
        startBurialAnimationsAfterPhaseAdvance(beforePlayerIndex, beforeTurnPhase, pendingBurials);
        startDrawAnimationAfterPhaseAdvance(beforePlayerIndex, beforeTurnPhase, beforeHandSize);

        mouse.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mouse);

        updateHover(delta);
        updateTooltip(delta);
        updateHpDisplay(delta);
        updatePlayAnim(delta);
        updateDrawAnim(delta);
        updateBurialAnims(delta);

        handleClick();
        detectCardZoneTransitions();

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        camera.update();
        shape.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        Texture bg = assets.background();
        if (bg != null) {
            batch.draw(bg, 7f, 4f, 1586f, 992f);
        }
        drawHpPanels();
        batch.end();

        shape.begin(ShapeRenderer.ShapeType.Filled);
        drawHandShapes(state.players[0].hand, HAND0_Y, false);
        drawHandShapes(state.players[1].hand, HAND1_Y, true);
        drawStagedCardEffects();
        drawGraveyardZoneShapes();
        drawButtonShapes();
        drawHudShapes();
        shape.end();

        batch.begin();
        drawDeck();
        drawField();
        drawStagedCards();
        drawGraveyards();
        drawDrawingCard();
        drawHandBatch(state.players[0].hand, HAND0_Y, false);
        drawHandBatch(state.players[1].hand, HAND1_Y, true);
        drawPlayingCard();
        drawBurialAnimations();
        drawHud();
        if (state.phase == GamePhase.GAME_OVER) drawGameOver();
        batch.end();

        drawSelectionLayer();
        drawTooltipLayer();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void processDebugCommands() {
        if (debugContext == null) return;

        DebugCommand command;
        while ((command = debugContext.pollCommand()) != null) {
            switch (command) {
                case DRAW_CURRENT:
                    debugDrawCurrentCard();
                    break;
                case ADVANCE_PHASE:
                    advanceTurnPhase();
                    break;
                case DAMAGE_OPPONENT:
                    damageOpponent(25);
                    break;
                case RESTART:
                    restartGame();
                    break;
            }
        }
    }

    private boolean isDebugMode() {
        return debugContext != null;
    }

    private void updateHover(float delta) {
        syncSelectionState();
        if (state.currentPlayerIndex != 0) {
            // P1 턴이면 호버 없음
            hoveredIndex = -1;
        } else {
            hoveredIndex = getHandIndexAt(
                state.players[0].hand, HAND0_Y, mouse.x, mouse.y);
        }

        for (int i = 0; i < hoverAnims.length; i++) {
            float target = i == hoveredIndex ? 1f : 0f;
            hoverAnims[i] += (target - hoverAnims[i]) * HOVER_SPEED * delta;
            if (Math.abs(hoverAnims[i] - target) < 0.01f) hoverAnims[i] = target;
        }
    }

    private void updateTooltip(float delta) {
        Card hovered = hoveredCard();
        boolean visible = hovered != null && CardDescriptions.get(hovered.id) != null;

        if (visible) {
            if (tooltipCard != hovered) {
                tooltipAnim = Math.min(tooltipAnim, 0.72f);
            }
            tooltipCard = hovered;
            tooltipAnchorX = handCardCenterX(state.players[0].hand.size, hoveredIndex);
        }

        float target = visible ? 1f : 0f;
        tooltipAnimVelocity += (target - tooltipAnim) * 120f * delta;
        tooltipAnimVelocity *= (float) Math.exp(-18f * delta);
        tooltipAnim += tooltipAnimVelocity * delta;
        tooltipAnim = MathUtils.clamp(tooltipAnim, 0f, 1f);

        tooltipDisplayVelocity += (tooltipAnchorX - tooltipDisplayX) * 80f * delta;
        tooltipDisplayVelocity *= (float) Math.exp(-15f * delta);
        tooltipDisplayX += tooltipDisplayVelocity * delta;

        if (!visible && tooltipAnim < 0.002f && Math.abs(tooltipAnimVelocity) < 0.01f) {
            tooltipAnim = 0f;
            tooltipAnimVelocity = 0f;
            tooltipCard = null;
        }
    }

    private void resetHpDisplay() {
        for (int i = 0; i < state.players.length; i++) {
            displayedHp[i] = state.players[i].hp;
            trailingHp[i] = state.players[i].hp;
            previousHp[i] = state.players[i].hp;
            hpFlash[i] = 0f;
            previousCost[i] = state.players[i].cost;
            for (int slot = 0; slot < 10; slot++) {
                costGainAnim[i][slot] = 0f;
                costSpendAnim[i][slot] = 0f;
            }
        }
    }

    private void updateHpDisplay(float delta) {
        hudTime += delta;
        for (int i = 0; i < state.players.length; i++) {
            int hp = state.players[i].hp;
            if (hp < previousHp[i]) hpFlash[i] = 1f;
            previousHp[i] = hp;

            displayedHp[i] += (hp - displayedHp[i]) * Math.min(1f, delta * 14f);
            trailingHp[i] += (hp - trailingHp[i]) * Math.min(1f, delta * 3.2f);
            hpFlash[i] = Math.max(0f, hpFlash[i] - delta * 2.8f);

            int cost = MathUtils.clamp(state.players[i].cost, 0, 10);
            int oldCost = MathUtils.clamp(previousCost[i], 0, 10);
            if (cost > oldCost) {
                for (int slot = oldCost; slot < cost; slot++) costGainAnim[i][slot] = 1f;
            } else if (cost < oldCost) {
                for (int slot = cost; slot < oldCost; slot++) costSpendAnim[i][slot] = 1f;
            }
            previousCost[i] = cost;
            for (int slot = 0; slot < 10; slot++) {
                costGainAnim[i][slot] = Math.max(0f, costGainAnim[i][slot] - delta * 2.4f);
                costSpendAnim[i][slot] = Math.max(0f, costSpendAnim[i][slot] - delta * 3.0f);
            }
        }
    }

    // 카드 사용 애니메이션 업데이트
    // playProgress를 0→1로 증가, 1 되면 필드에 카드 배치 후 초기화
    private void updatePlayAnim(float delta) {
        if (playingCard == null) return;

        playProgress += PLAY_SPEED * delta;
        if (playProgress >= 1f) {
            // 애니메이션 완료 — 지금은 그냥 제거 (나중에 필드 배치 로직 연결)
            playProgress  = 1f;
            playingCard   = null;
            playingOwnerIndex = -1;
            playingCardLandsInField = false;
            playProgress  = 0f;
            if (queuedPlayAnimations.size > 0) {
                PlayAnimationRequest next = queuedPlayAnimations.removeIndex(0);
                beginPlayAnimation(next.card, next.owner, next.ownerIndex, next.startX, next.startY);
            }
        }
    }

    private void updateDrawAnim(float delta) {
        if (drawingCard == null) return;
        drawProgress += DRAW_SPEED * delta;
        if (drawProgress >= 1f) {
            drawingCard = null;
            drawingOwnerIndex = -1;
            drawProgress = 0f;
        }
    }

    private void updateBurialAnims(float delta) {
        for (int i = burialAnims.size - 1; i >= 0; i--) {
            BurialAnimation anim = burialAnims.get(i);
            anim.progress += BURIAL_SPEED * delta;
            if (anim.progress >= 1f) {
                burialAnims.removeIndex(i);
            }
        }
    }

    // 핸드 위치 계산
    private float handStartX(int count) {
        float totalWidth = count * CardRenderer.CARD_W
            + Math.max(0, count - 1) * GAP;
        return 800f - totalWidth / 2f;
    }

    private float handCardCenterX(int count, int index) {
        return handStartX(count)
            + index * (CardRenderer.CARD_W + GAP)
            + CardRenderer.CARD_W / 2f;
    }

    private float handCardCenterY(float baseY, int index) {
        return baseY + hoverLift(index) + selectionLift(index) + CardRenderer.CARD_H / 2f;
    }

    private float fieldCardCenterX(Array<Card> field, int index) {
        float totalWidth = field.size * FIELD_CARD_W
            + Math.max(0, field.size - 1) * FIELD_GAP;
        float fieldMaxLean = 18f;
        float startX = 800f - totalWidth / 2f - fieldMaxLean / 2f;
        return startX + index * (FIELD_CARD_W + FIELD_GAP) + FIELD_CARD_W / 2f;
    }

    private float graveyardCardCenterX(Player player, int ownerIndex, Card card) {
        float cardW = CardRenderer.CARD_W * GRAVE_CARD_SCALE;
        int index = graveyardIndexOf(player, card);
        float x = GRAVE_X;
        if (index >= 0 && index < 4) {
            return x + 12f + index * (cardW + GRAVE_CARD_GAP) + cardW / 2f;
        }
        return x + GRAVE_W - 30f;
    }

    private float graveyardCardCenterY(int ownerIndex, Card card) {
        float cardH = CardRenderer.CARD_H * GRAVE_CARD_SCALE;
        float y = ownerIndex == 1 ? GRAVE_P1_Y : GRAVE_P0_Y;
        return y + 14f + cardH / 2f;
    }

    private int graveyardIndexOf(Player player, Card target) {
        for (int i = 0; i < player.graveyard.size; i++) {
            if (player.graveyard.get(i).card == target) return i;
        }
        return -1;
    }

    private int indexOfIdentity(Array<Card> cards, Card target) {
        for (int i = 0; i < cards.size; i++) {
            if (cards.get(i) == target) return i;
        }
        return -1;
    }

    // 특정 좌표가 핸드의 몇 번째 카드인지 반환 (-1이면 없음)
    private int getHandIndexAt(Array<Card> hand, float baseY, float wx, float wy) {
        float startX = handStartX(hand.size);
        // 뒤에서부터 체크 — 앞 카드가 위에 그려지므로 앞 카드 우선
        for (int i = hand.size - 1; i >= 0; i--) {
            float cx = startX + i * (CardRenderer.CARD_W + GAP);
            float cy = baseY + selectionLift(i);
            if (wx >= cx && wx <= cx + CardRenderer.CARD_W
                && wy >= cy && wy <= cy + CardRenderer.CARD_H + HOVER_LIFT) {
                return i;
            }
        }
        return -1;
    }

    // Shape 패스
    private void drawHandShapes(Array<Card> hand, float baseY, boolean isBack) {
        float startX = handStartX(hand.size);
        for (int i = 0; i < hand.size; i++) {
            Card card = hand.get(i);
            if (isCardHiddenInHand(card)) continue;

            float x = startX + i * (CardRenderer.CARD_W + GAP);
            // 호버 리프트 적용 (P0 핸드만)
            float y = baseY + (isBack ? 0 : hoverLift(i) + selectionLift(i));

            if (isBack) {
                Texture back = assets.cardBack();
                if (back == null) CardRenderer.drawBack(shape, x, y);
            } else {
                CardRenderer.drawEffectBorder(shape, card, x, y,
                    state.players[0].allCardsCostZeroThisTurn);
                if (isHandSelectionCandidate(card)) {
                    shape.setColor(card == stagedSelectionCard
                        ? COL_SELECTION_SELECTED : COL_SELECTION_HOVER);
                    shape.rect(x - 7f, y - 7f, CardRenderer.CARD_W + 14f, CardRenderer.CARD_H + 14f);
                    shape.setColor(0.03f, 0.03f, 0.05f, 1f);
                    shape.rect(x - 3f, y - 3f, CardRenderer.CARD_W + 6f, CardRenderer.CARD_H + 6f);
                }
                Texture illust = assets.cardIllust(card);
                if (illust == null) CardRenderer.drawShape(shape, card, x, y);
            }
        }
    }

    // Batch 패스
    private void drawHandBatch(Array<Card> hand, float baseY, boolean isBack) {
        float startX = handStartX(hand.size);
        Texture back = assets.cardBack();

        for (int i = 0; i < hand.size; i++) {
            float x = startX + i * (CardRenderer.CARD_W + GAP);
            float y = baseY + (isBack ? 0 : hoverLift(i) + selectionLift(i));
            Card  card = hand.get(i);
            if (isCardHiddenInHand(card)) continue;

            if (isBack) {
                if (back != null) CardRenderer.drawIllust(batch, back, x, y, card.reversed);
                continue;
            }

            Texture illust  = assets.cardIllust(card);
            int effectiveCost = state.effectiveCostFor(state.players[0], card);
            Texture costTex = assets.cardCost(card, effectiveCost);

            if (illust != null) {
                CardRenderer.drawIllust(batch, illust, x, y, card.shouldFlipIllust());
                CardRenderer.drawCost(batch, fonts.small, costTex, card, effectiveCost, x, y, costSize(card));
            } else {
                CardRenderer.drawText(batch, fonts.small, card, x, y);
            }
        }
    }

    private Card hoveredCard() {
        if (state == null || state.currentPlayerIndex != 0 || hoveredIndex < 0) return null;
        Player player = state.players[0];
        if (hoveredIndex >= player.hand.size) return null;
        Card card = player.hand.get(hoveredIndex);
        return isCardHiddenInHand(card) ? null : card;
    }

    private Rectangle tooltipBounds() {
        if (tooltipCard == null || tooltipAnim <= 0f) return null;

        float eased = Interpolation.smoother.apply(tooltipAnim);
        float x = MathUtils.clamp(tooltipDisplayX - TOOLTIP_W / 2f,
            40f, GameConfig.WORLD_W - TOOLTIP_W - 40f);
        float baseY = MathUtils.clamp(HAND0_Y + CardRenderer.CARD_H + 58f,
            245f, GameConfig.WORLD_H - TOOLTIP_H - 40f);
        float y = baseY - (1f - eased) * 12f;
        return new Rectangle(x, y, TOOLTIP_W, TOOLTIP_H);
    }

    private void drawTooltipLayer() {
        if (tooltipCard == null || tooltipAnim <= 0f) return;

        shape.begin(ShapeRenderer.ShapeType.Filled);
        drawTooltipShape();
        shape.end();

        batch.begin();
        drawTooltipText();
        batch.end();
    }

    private void drawTooltipShape() {
        Rectangle r = tooltipBounds();
        if (r == null) return;

        float alpha = Interpolation.smoother.apply(tooltipAnim);
        float tailX = MathUtils.clamp(tooltipDisplayX, r.x + TOOLTIP_RADIUS + TOOLTIP_TAIL_W,
            r.x + r.width - TOOLTIP_RADIUS - TOOLTIP_TAIL_W);

        setTooltipShapeColor(COL_TOOLTIP_SHADOW, alpha);
        drawRoundedRect(r.x + 8f, r.y - 10f, r.width, r.height, TOOLTIP_RADIUS);
        shape.triangle(tailX - TOOLTIP_TAIL_W / 2f + 8f, r.y - 10f,
            tailX + TOOLTIP_TAIL_W / 2f + 8f, r.y - 10f,
            tailX + 8f, r.y - TOOLTIP_TAIL_H - 10f);

        setTooltipShapeColor(COL_TOOLTIP_BG, alpha);
        drawRoundedRect(r.x, r.y, r.width, r.height, TOOLTIP_RADIUS);
        shape.triangle(tailX - TOOLTIP_TAIL_W / 2f, r.y + 1f,
            tailX + TOOLTIP_TAIL_W / 2f, r.y + 1f,
            tailX, r.y - TOOLTIP_TAIL_H);

        setTooltipShapeColor(COL_TOOLTIP_LINE, alpha);
        shape.rect(r.x + TOOLTIP_PAD, r.y + r.height - 49f,
            r.width - TOOLTIP_PAD * 2f, 1f);
    }

    private void drawRoundedRect(float x, float y, float width, float height, float radius) {
        shape.rect(x + radius, y, width - radius * 2f, height);
        shape.rect(x, y + radius, width, height - radius * 2f);
        shape.circle(x + radius, y + radius, radius, 24);
        shape.circle(x + width - radius, y + radius, radius, 24);
        shape.circle(x + radius, y + height - radius, radius, 24);
        shape.circle(x + width - radius, y + height - radius, radius, 24);
    }

    private void setTooltipShapeColor(Color color, float alpha) {
        shape.setColor(color.r, color.g, color.b, color.a * alpha);
    }

    private void drawTooltipText() {
        Card card = tooltipCard;
        CardDescriptions.Entry entry = card == null ? null : CardDescriptions.get(card.id);
        if (card == null || entry == null) return;

        Rectangle r = tooltipBounds();
        if (r == null) return;

        Player player = state.players[0];
        boolean useReversed = player.effectsSwapped ? !card.reversed : card.reversed;
        String direction = useReversed ? "역방향" : "정방향";
        String alternateDirection = useReversed ? "정방향" : "역방향";
        String alternate = useReversed ? entry.upright : entry.reversed;
        String cardState = card.reversed ? "역방향 카드" : "정방향 카드";
        String current = CardDescriptions.currentText(card.id, card.reversed, player.effectsSwapped);

        float x = r.x + TOOLTIP_PAD;
        float textW = r.width - TOOLTIP_PAD * 2f;
        float alpha = Interpolation.smoother.apply(tooltipAnim);

        fonts.tooltipTitle.setColor(1f, 0.97f, 0.91f, alpha);
        fonts.tooltipTitle.draw(batch, card.name, x, r.y + r.height - 16f);

        setTooltipFontColor(fonts.tooltipBody, COL_TOOLTIP_MUTED, alpha);
        fonts.tooltipBody.draw(batch,
            "COST " + state.effectiveCostFor(player, card) + "   |   " + cardState
                + (player.effectsSwapped ? "   |   효과 반전 중" : ""),
            x, r.y + r.height - 35f);

        setTooltipFontColor(fonts.tooltipBody, COL_TOOLTIP_ACTIVE, alpha);
        fonts.tooltipBody.draw(batch, "현재 적용 · " + direction, x, r.y + 114f);
        setTooltipFontColor(fonts.tooltipBody, Color.WHITE, alpha);
        drawWrappedText(current, x, r.y + 96f, textW, TOOLTIP_LINE_H, fonts.tooltipBody);

        setTooltipFontColor(fonts.tooltipBody,
            useReversed ? COL_TOOLTIP_ACTIVE : COL_TOOLTIP_REVERSED, alpha * 0.88f);
        fonts.tooltipBody.draw(batch, "반대 효과 · " + alternateDirection, x, r.y + 46f);
        setTooltipFontColor(fonts.tooltipBody, COL_TOOLTIP_MUTED, alpha);
        drawWrappedText(alternate, x, r.y + 28f, textW, TOOLTIP_LINE_H, fonts.tooltipBody);
        fonts.tooltipBody.setColor(Color.WHITE);
        fonts.tooltipTitle.setColor(Color.WHITE);
    }

    private void setTooltipFontColor(com.badlogic.gdx.graphics.g2d.BitmapFont font,
                                     Color color, float alpha) {
        font.setColor(color.r, color.g, color.b, color.a * alpha);
    }

    private float drawWrappedText(String text, float x, float y, float maxWidth, float lineHeight,
                                  com.badlogic.gdx.graphics.g2d.BitmapFont font) {
        String remaining = text;
        GlyphLayout layout = new GlyphLayout();
        while (remaining.length() > 0) {
            int cut = fitText(remaining, maxWidth, font, layout);
            String line = remaining.substring(0, cut);
            font.draw(batch, line, x, y);
            remaining = remaining.substring(cut).trim();
            y -= lineHeight;
        }
        return y;
    }

    private int fitText(String text, float maxWidth, com.badlogic.gdx.graphics.g2d.BitmapFont font,
                        GlyphLayout layout) {
        layout.setText(font, text);
        if (layout.width <= maxWidth) return text.length();

        int low = 1;
        int high = text.length();
        while (low < high) {
            int mid = (low + high + 1) / 2;
            layout.setText(font, text.substring(0, mid));
            if (layout.width <= maxWidth) low = mid;
            else high = mid - 1;
        }

        int cut = Math.max(1, low);
        for (int i = cut; i > Math.max(1, cut - 14); i--) {
            if (Character.isWhitespace(text.charAt(i - 1))) return i;
        }
        return cut;
    }

    private void drawDeck() {
        Texture back = assets.cardBack();
        if (back == null) return;

        for (int i = DECK_LAYERS - 1; i >= 0; i--) {
            float offsetX = i * GameConfig.DECK_LAYER_OFFSET_X;
            float offsetY = -i * GameConfig.DECK_LAYER_OFFSET_Y;
            float brightness = 1f - i * 0.15f;
            float alpha = 1f;
            int color = toFloatBits(brightness, brightness, brightness, alpha);

            drawCardPerspective(back,
                DECK_X - DECK_W / 2f + offsetX,
                DECK_Y - DECK_H / 2f + offsetY,
                DECK_W, DECK_H,
                GameConfig.DECK_TAPER,
                GameConfig.DECK_LEAN);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawCardPerspective(Texture tex,
                                     float x, float y,   // 좌하 기준
                                     float w, float h,
                                     float taper,
                                     float lean) {
        float u = 0f, v = 0f, u2 = 1f, v2 = 1f;

        // 좌하 기준 좌표 → 중앙 기준으로 변환
        float cx = x + w / 2f;
        float cy = y + h / 2f;

        float x0 = cx - w / 2f,                y0 = cy - h / 2f;  // 좌하
        float x1 = cx + w / 2f,                y1 = cy - h / 2f;  // 우하
        float x2 = cx + w / 2f - taper + lean, y2 = cy + h / 2f;  // 우상
        float x3 = cx - w / 2f + taper + lean, y3 = cy + h / 2f;  // 좌상

        float cf = Float.intBitsToFloat(toFloatBits(1f, 1f, 1f, 1f));

        float[] verts = {
            x0, y0, cf, u,  v2,
            x1, y1, cf, u2, v2,
            x2, y2, cf, u2, v,
            x3, y3, cf, u,  v,
        };
        batch.draw(tex, verts, 0, verts.length);
    }

    // color float bits 변환 유틸
    private int toFloatBits(float r, float g, float b, float a) {
        int ri = (int)(r * 255) & 0xFF;
        int gi = (int)(g * 255) & 0xFF;
        int bi = (int)(b * 255) & 0xFF;
        int ai = (int)(a * 255) & 0xFF;
        return (ai << 24) | (bi << 16) | (gi << 8) | ri;
    }

    private void drawField() {
        drawPlayerField(state.players[0].field, FIELD_P0_Y, 0);  // P0 아래
        drawPlayerField(state.players[1].field, FIELD_P1_Y, 1);  // P1 위
    }

    private float stagedCardWidth() {
        return CardRenderer.CARD_W * STAGED_CARD_SCALE;
    }

    private float stagedCardHeight() {
        return CardRenderer.CARD_H * STAGED_CARD_SCALE;
    }

    private float stagedStartX(Array<Card> cards) {
        float totalWidth = cards.size * stagedCardWidth()
            + Math.max(0, cards.size - 1) * STAGED_CARD_GAP;
        return STAGED_CENTER_X - totalWidth / 2f;
    }

    private Rectangle stagedCardBounds(Array<Card> cards, int index) {
        return new Rectangle(
            stagedStartX(cards) + index * (stagedCardWidth() + STAGED_CARD_GAP),
            STAGED_CENTER_Y - stagedCardHeight() / 2f,
            stagedCardWidth(),
            stagedCardHeight());
    }

    private int stagedCardIndexAt(Player player, float x, float y) {
        for (int i = player.stagedCards.size - 1; i >= 0; i--) {
            if (stagedCardBounds(player.stagedCards, i).contains(x, y)) return i;
        }
        return -1;
    }

    private void drawStagedCardEffects() {
        Player player = state.currentPlayer();
        if (player.stagedCards.size == 0) return;

        for (int i = 0; i < player.stagedCards.size; i++) {
            Rectangle bounds = stagedCardBounds(player.stagedCards, i);
            float cardPulse = 0.55f + 0.25f * MathUtils.sin(hudTime * 4.4f + i * 0.9f);
            shape.setColor(COL_SELECTION_HOVER.r, COL_SELECTION_HOVER.g,
                COL_SELECTION_HOVER.b, cardPulse * 0.08f);
            shape.rect(bounds.x - 13f, bounds.y - 13f, bounds.width + 26f, bounds.height + 26f);
            shape.setColor(COL_SELECTION_SELECTED.r, COL_SELECTION_SELECTED.g,
                COL_SELECTION_SELECTED.b, cardPulse * 0.20f);
            shape.rect(bounds.x - 8f, bounds.y - 8f, bounds.width + 16f, bounds.height + 16f);
            drawCardGlowFrame(bounds, COL_SELECTION_SELECTED, cardPulse);

            for (int p = 0; p < 4; p++) {
                float angle = hudTime * (1.6f + p * 0.12f) + i * 1.7f + p;
                float radius = 14f + p * 6f;
                float sparkX = bounds.x + bounds.width / 2f + MathUtils.cos(angle) * radius;
                float sparkY = bounds.y + bounds.height / 2f + MathUtils.sin(angle * 1.21f) * radius * 0.78f;
                shape.setColor(COL_SELECTION_HOVER.r, COL_SELECTION_HOVER.g,
                    COL_SELECTION_HOVER.b, 0.18f + cardPulse * 0.20f);
                shape.circle(sparkX, sparkY, 1.6f + p * 0.15f, 10);
            }
        }
    }

    private void drawCardGlowFrame(Rectangle bounds, Color color, float intensity) {
        shape.setColor(color.r, color.g, color.b, 0.34f + intensity * 0.18f);
        drawFrameLines(bounds.x - 4f, bounds.y - 4f, bounds.width + 8f, bounds.height + 8f, 2f);
        shape.setColor(0.03f, 0.025f, 0.08f, 0.94f);
        shape.rect(bounds.x - 1f, bounds.y - 1f, bounds.width + 2f, bounds.height + 2f);

        float corner = Math.min(16f, bounds.width * 0.22f);
        shape.setColor(color.r, color.g, color.b, 0.72f + intensity * 0.20f);
        drawFrameCorners(bounds.x - 5f, bounds.y - 5f, bounds.width + 10f, bounds.height + 10f, corner, 2f);
    }

    private void drawFrameLines(float x, float y, float width, float height, float thickness) {
        shape.rect(x, y, width, thickness);
        shape.rect(x, y + height - thickness, width, thickness);
        shape.rect(x, y, thickness, height);
        shape.rect(x + width - thickness, y, thickness, height);
    }

    private void drawFrameCorners(float x, float y, float width, float height, float length, float thickness) {
        shape.rect(x, y, length, thickness);
        shape.rect(x, y, thickness, length);
        shape.rect(x + width - length, y, length, thickness);
        shape.rect(x + width - thickness, y, thickness, length);
        shape.rect(x, y + height - thickness, length, thickness);
        shape.rect(x, y + height - length, thickness, length);
        shape.rect(x + width - length, y + height - thickness, length, thickness);
        shape.rect(x + width - thickness, y + height - length, thickness, length);
    }

    private void drawStagedCards() {
        Player player = state.currentPlayer();
        if (player.stagedCards.size == 0) return;

        for (int i = 0; i < player.stagedCards.size; i++) {
            Card card = player.stagedCards.get(i);
            Rectangle bounds = stagedCardBounds(player.stagedCards, i);
            Texture illust = assets.cardIllust(card);
            if (illust != null) {
                batch.draw(illust, bounds.x, bounds.y, bounds.width, bounds.height,
                    0, 0, illust.getWidth(), illust.getHeight(), false, card.shouldFlipIllust());
            }

            int effectiveCost = state.effectiveCostFor(player, card);
            Texture costTexture = assets.cardCost(card, effectiveCost);
            float costSize = 36f;
            float costX = bounds.x - 5f;
            float costY = bounds.y + bounds.height - costSize + 5f;
            if (costTexture != null) {
                batch.draw(costTexture, costX, costY, costSize, costSize);
            }
            if (costTexture == null) {
                String value = String.valueOf(effectiveCost);
                GlyphLayout valueLayout = new GlyphLayout(fonts.small, value);
                fonts.small.setColor(Color.WHITE);
                fonts.small.draw(batch, value,
                    costX + costSize / 2f - valueLayout.width / 2f,
                    costY + costSize / 2f + valueLayout.height / 2f);
                fonts.small.setColor(Color.WHITE);
            }
        }

        String cost = state.stagedCost(player) + " / " + player.cost;
        GlyphLayout layout = new GlyphLayout(fonts.small, cost);
        fonts.small.setColor(COL_SELECTION_HOVER);
        fonts.small.draw(batch, cost, STAGED_CENTER_X - layout.width / 2f,
            STAGED_CENTER_Y - stagedCardHeight() / 2f - 12f);
        fonts.small.setColor(Color.WHITE);
    }

    private void drawGraveyardZoneShapes() {
        drawGraveyardZoneShape(GRAVE_X, GRAVE_P0_Y);
        drawGraveyardZoneShape(GRAVE_X, GRAVE_P1_Y);
    }

    private void drawGraveyardZoneShape(float x, float y) {
        shape.setColor(COL_GRAVE_SHADOW);
        drawAstralPlate(x + 5f, y - 5f, GRAVE_W, GRAVE_H);
        shape.setColor(COL_GRAVE_GOLD);
        drawAstralPlate(x, y, GRAVE_W, GRAVE_H);
        shape.setColor(COL_GRAVE_PLATE);
        drawAstralPlate(x + 2f, y + 2f, GRAVE_W - 4f, GRAVE_H - 4f);
        shape.setColor(COL_GRAVE_GOLD_DIM);
        drawAstralPlate(x + 8f, y + 8f, GRAVE_W - 16f, GRAVE_H - 16f);
        shape.setColor(COL_GRAVE_INNER);
        drawAstralPlate(x + 10f, y + 10f, GRAVE_W - 20f, GRAVE_H - 20f);

        drawGraveSlots(x, y);
        drawAstralMark(x + GRAVE_W - 27f, y + GRAVE_H - 23f);
    }

    private void drawAstralPlate(float x, float y, float width, float height) {
        float cut = 12f;
        shape.rect(x + cut, y, width - cut * 2f, height);
        shape.rect(x, y + cut, width, height - cut * 2f);
        shape.triangle(x, y + cut, x + cut, y, x + cut, y + cut);
        shape.triangle(x + width, y + cut, x + width - cut, y, x + width - cut, y + cut);
        shape.triangle(x, y + height - cut, x + cut, y + height, x + cut, y + height - cut);
        shape.triangle(x + width, y + height - cut, x + width - cut, y + height,
            x + width - cut, y + height - cut);
    }

    private void drawGraveSlots(float x, float y) {
        float cardW = CardRenderer.CARD_W * GRAVE_CARD_SCALE;
        float cardH = CardRenderer.CARD_H * GRAVE_CARD_SCALE;
        float cardX = x + 12f;
        float cardY = y + 14f;

        for (int i = 0; i < 4; i++) {
            float slotX = cardX + i * (cardW + GRAVE_CARD_GAP);
            shape.setColor(COL_GRAVE_SLOT);
            drawAstralPlate(slotX - 2f, cardY - 2f, cardW + 4f, cardH + 4f);
            shape.setColor(COL_GRAVE_GOLD_DIM);
            shape.rect(slotX + 4f, cardY - 4f, cardW - 8f, 1f);
        }
    }

    private void drawAstralMark(float cx, float cy) {
        shape.setColor(COL_GRAVE_STAR);
        shape.circle(cx, cy, 7f, 20);
        shape.setColor(COL_GRAVE_INNER);
        shape.circle(cx, cy, 4f, 20);
        shape.setColor(COL_GRAVE_STAR);
        shape.triangle(cx, cy + 14f, cx - 3f, cy + 5f, cx + 3f, cy + 5f);
        shape.triangle(cx, cy - 14f, cx - 3f, cy - 5f, cx + 3f, cy - 5f);
        shape.triangle(cx - 14f, cy, cx - 5f, cy - 3f, cx - 5f, cy + 3f);
        shape.triangle(cx + 14f, cy, cx + 5f, cy - 3f, cx + 5f, cy + 3f);
    }

    private void drawGraveyards() {
        drawPlayerGraveyard(state.players[0], GRAVE_X, GRAVE_P0_Y);
        drawPlayerGraveyard(state.players[1], GRAVE_X, GRAVE_P1_Y);
    }

    private void drawPlayerGraveyard(Player player, float x, float y) {
        float cardW = CardRenderer.CARD_W * GRAVE_CARD_SCALE;
        float cardH = CardRenderer.CARD_H * GRAVE_CARD_SCALE;
        float cardX = x + 12f;
        float cardY = y + 14f;
        int shown = 0;
        int drawableCount = countDrawableGraveyardCards(player);
        for (int i = 0; i < player.graveyard.size && shown < 4; i++) {
            GameState.GraveyardCard buried = player.graveyard.get(i);
            if (isCardBeingBuried(buried.card)) continue;

            drawGraveyardCard(buried.card, cardX + shown * (cardW + GRAVE_CARD_GAP), cardY, cardW, cardH);
            float counterX = cardX + shown * (cardW + GRAVE_CARD_GAP) + cardW - 8f;
            float counterY = cardY + cardH - 5f;
            fonts.small.setColor(0.82f, 0.68f, 0.42f, 1f);
            fonts.small.draw(batch, String.valueOf(buried.turnsRemaining), counterX, counterY);
            shown++;
        }

        if (drawableCount > shown) {
            fonts.small.setColor(0.70f, 0.58f, 0.36f, 1f);
            fonts.small.draw(batch, "+" + (drawableCount - shown),
                x + GRAVE_W - 42f, y + 24f);
        }
        fonts.small.setColor(Color.WHITE);
    }

    private int countDrawableGraveyardCards(Player player) {
        int count = 0;
        for (GameState.GraveyardCard buried : player.graveyard) {
            if (!isCardBeingBuried(buried.card)) count++;
        }
        return count;
    }

    private void drawGraveyardCard(Card card, float x, float y, float w, float h) {
        Texture tex = assets.cardIllust(card);
        if (tex == null) tex = assets.cardBack();
        if (tex == null) return;

        batch.setColor(0.68f, 0.68f, 0.72f, 0.78f);
        batch.draw(tex,
            x, y, w, h,
            0, 0, tex.getWidth(), tex.getHeight(),
            false, card.shouldFlipIllust());
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawBurialAnimations() {
        for (BurialAnimation anim : burialAnims) {
            float p = MathUtils.clamp(anim.progress, 0f, 1f);
            float t = Interpolation.smoother.apply(p);
            float arc = MathUtils.sin(p * MathUtils.PI) * 45f;
            float cx = anim.startX + (anim.endX - anim.startX) * t;
            float cy = anim.startY + (anim.endY - anim.startY) * t + arc;
            float scale = GameConfig.FIELD_SCALE + (GRAVE_CARD_SCALE - GameConfig.FIELD_SCALE) * t;
            float alpha = 1f - 0.18f * t;
            float direction = anim.ownerIndex == 1 ? -1f : 1f;
            float rotation = direction * MathUtils.sin(p * MathUtils.PI) * 7f;

            drawMovingCard(anim.card, cx, cy,
                CardRenderer.CARD_W * scale,
                CardRenderer.CARD_H * scale,
                rotation, true, alpha);
        }
    }

    private void drawPlayerField(Array<Card> field, float baseY, int ownerIndex) {
        if (field.isEmpty()) return;

        float totalWidth = field.size * FIELD_CARD_W
            + Math.max(0, field.size - 1) * FIELD_GAP;
        float fieldMaxLean = 18f;
        float startX = 800f - totalWidth / 2f - fieldMaxLean / 2f;
        float centerX = 800f;

        for (int i = 0; i < field.size; i++) {
            float x = startX + i * (FIELD_CARD_W + FIELD_GAP);
            float y = baseY - FIELD_CARD_H / 2f;
            Card card = field.get(i);
            if (isCardHiddenInField(card, ownerIndex)) continue;

            float cardCenterX = x + FIELD_CARD_W / 2f;
            float distFromCenter = (cardCenterX - centerX) / (totalWidth / 2f + 1f);
            float lean  = -distFromCenter * 18f;
            float taper = Math.abs(distFromCenter) * 8f;

            Texture illust = assets.cardIllust(card);
            Texture tex = (illust != null) ? illust : assets.cardBack();
            if (tex == null) continue;

            if (illust == null) batch.setColor(0.2f, 0.2f, 0.3f, 1f);

            if (card.shouldFlipIllust()) {
                batch.draw(tex,
                    x, y, FIELD_CARD_W, FIELD_CARD_H,
                    0, 0, tex.getWidth(), tex.getHeight(),
                    true, true);
            } else {
                drawCardPerspective(tex, x, y, FIELD_CARD_W, FIELD_CARD_H, taper, lean);
            }

            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    // 날아가는 카드 렌더
    private void drawPlayingCard() {
        if (playingCard == null) return;

        float p = MathUtils.clamp(playProgress, 0f, 1f);
        float t = Interpolation.smoother.apply(p);
        float arc = MathUtils.sin(p * MathUtils.PI) * 90f;
        float cx = playStartX + (playEndX - playStartX) * t;
        float cy = playStartY + (playEndY - playStartY) * t + arc;

        float scale = playStartScale + (playEndScale - playStartScale) * t;
        scale += 0.08f * MathUtils.sin(p * MathUtils.PI);
        float w = CardRenderer.CARD_W * scale;
        float h = CardRenderer.CARD_H * scale;

        float direction = playingOwnerIndex == 1 ? -1f : 1f;
        float rotation = direction * MathUtils.sin(p * MathUtils.PI) * 5f;
        drawMovingCard(playingCard, cx, cy, w, h, rotation, true, 1f);
    }

    private void drawDrawingCard() {
        if (drawingCard == null) return;

        float p = MathUtils.clamp(drawProgress, 0f, 1f);
        float t = Interpolation.smoother.apply(p);
        float arc = MathUtils.sin(p * MathUtils.PI) * 55f;
        float cx = drawStartX + (drawEndX - drawStartX) * t;
        float cy = drawStartY + (drawEndY - drawStartY) * t + arc;

        float h = DECK_H + (CardRenderer.CARD_H - DECK_H) * t;
        float w = DECK_W + (CardRenderer.CARD_W - DECK_W) * t;

        boolean reveal = drawingOwnerIndex == 0 && p >= 0.58f;
        float flipWidth = 1f;
        if (drawingOwnerIndex == 0) {
            if (p < 0.58f) {
                flipWidth = 1f - 0.82f * (p / 0.58f);
            } else {
                flipWidth = 0.18f + 0.82f * ((p - 0.58f) / 0.42f);
            }
        }
        drawMovingCard(drawingCard, cx, cy, w * flipWidth, h, 0f, reveal, 1f);
    }

    private void drawMovingCard(Card card, float cx, float cy, float w, float h,
                                float rotation, boolean faceUp, float alpha) {
        Texture tex = faceUp ? assets.cardIllust(card) : assets.cardBack();
        if (tex == null) tex = assets.cardBack();
        if (tex == null) return;

        boolean flipY = faceUp && card.shouldFlipIllust();
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(tex,
            cx - w / 2f, cy - h / 2f,
            w / 2f, h / 2f,
            w, h,
            1f, 1f,
            rotation,
            0, 0, tex.getWidth(), tex.getHeight(),
            false, flipY);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private boolean isCardHiddenInHand(Card card) {
        return card == drawingCard || card == playingCard;
    }

    private boolean isCardHiddenInField(Card card, int ownerIndex) {
        return playingCardLandsInField
            && ownerIndex == playingOwnerIndex
            && card == playingCard;
    }

    private boolean isCardBeingBuried(Card card) {
        for (BurialAnimation anim : burialAnims) {
            if (anim.card == card) return true;
        }
        return false;
    }

    // 호버 리프트 값 반환
    // i번째 카드의 현재 hoverAnim 값으로 Y 오프셋 계산
    private float hoverLift(int i) {
        if (i >= hoverAnims.length) return 0f;
        return hoverAnims[i] * HOVER_LIFT;
    }

    private float selectionLift(int index) {
        if (index >= state.players[0].hand.size) return 0f;
        Card card = state.players[0].hand.get(index);
        if (state.pendingSelection == null) return 0f;
        return card == stagedSelectionCard ? SELECTION_LIFT : 0f;
    }

    // 코스트 사이즈
    private float costSize(Card card) {
        if (card.type == Card.ArcanaType.MAJOR) return 48f;
        switch (card.cost) {
            case 1:  return 72f;
            case 2:  return 68f;
            case 3:  return 58f;
            case 4:  return 58f;
            default: return 44f;
        }
    }

    // 버튼 렌더링
    private void drawButtonShapes() {
        if (state.phase == GamePhase.GAME_OVER) {
            drawBtn(btnRestart, COL_BTN_OK);
            return;
        }
        if (state.currentPlayer().stagedCards.size > 0) {
            drawStartButton(btnDraw);
        } else {
            drawBtn(btnDraw,  COL_BTN_NORMAL);
        }
        drawBtn(btnPhase, COL_BTN_NORMAL);
        drawBtn(btnDebug, COL_BTN_DANGER);
    }

    private void drawBtn(Rectangle r, Color bg) {
        shape.setColor(COL_BTN_BORDER);
        shape.rect(r.x, r.y, r.width, r.height);
        shape.setColor(bg);
        shape.rect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
    }

    private void drawStartButton(Rectangle r) {
        float pulse = 0.62f + 0.25f * MathUtils.sin(hudTime * 4.2f);
        boolean hovered = r.contains(mouse);
        Color line = hovered ? COL_SELECTION_SELECTED : COL_SELECTION_HOVER;

        shape.setColor(0f, 0f, 0f, 0.42f);
        drawAstralPlate(r.x + 4f, r.y - 4f, r.width, r.height);
        shape.setColor(line.r, line.g, line.b, 0.12f + pulse * 0.12f);
        drawAstralPlate(r.x - 3f, r.y - 3f, r.width + 6f, r.height + 6f);
        shape.setColor(0.025f, 0.03f, 0.085f, 0.98f);
        drawAstralPlate(r.x, r.y, r.width, r.height);
        shape.setColor(line.r, line.g, line.b, 0.25f + pulse * 0.18f);
        drawFrameCorners(r.x + 3f, r.y + 3f, r.width - 6f, r.height - 6f, 24f, 2f);
        shape.setColor(line.r, line.g, line.b, 0.08f + pulse * 0.08f);
        shape.rect(r.x + 48f, r.y + 6f, r.width - 58f, r.height - 12f);

        float cx = r.x + 28f;
        float cy = r.y + r.height / 2f;
        shape.setColor(line.r, line.g, line.b, 0.20f + pulse * 0.22f);
        shape.circle(cx, cy, 18f + pulse * 3f, 32);
        shape.setColor(0.02f, 0.025f, 0.07f, 1f);
        shape.circle(cx, cy, 13f, 32);
        shape.setColor(line);
        shape.triangle(cx - 4f, cy - 8f, cx - 4f, cy + 8f, cx + 10f, cy);
    }

    private void drawButtonLabels() {
        fonts.small.setColor(1f, 1f, 1f, 1f);
        if (state.phase == GamePhase.GAME_OVER) {
            drawBtnLabel("다시 시작", btnRestart);
        } else {
            drawBtnLabel(state.currentPlayer().stagedCards.size > 0 ? "시작" : "드로우", btnDraw);
            drawBtnLabel("페이즈 진행", btnPhase);
            drawBtnLabel("디버그: -25HP", btnDebug);
        }
        fonts.small.setColor(Color.WHITE);
    }

    private void drawBtnLabel(String text, Rectangle r) {
        GlyphLayout gl = new GlyphLayout(fonts.small, text);
        float offset = state.currentPlayer().stagedCards.size > 0 && r == btnDraw ? 15f : 0f;
        fonts.small.draw(batch, text,
            r.x + (r.width  - gl.width)  / 2f + offset,
            r.y + (r.height + gl.height) / 2f);
    }

    // HUD
    private void drawHudShapes() {
        drawHpBarShape(0, HP_P0_Y);
        drawHpBarShape(1, HP_P1_Y);
    }

    private void drawHpPanels() {
        Texture panel = assets.statFrame();
        if (panel == null) return;

        batch.draw(panel, HP_PANEL_X, HP_P0_Y + HP_PANEL_Y_OFFSET, HP_PANEL_W, HP_PANEL_H);
        batch.draw(panel, HP_PANEL_X, HP_P1_Y + HP_PANEL_Y_OFFSET, HP_PANEL_W, HP_PANEL_H);
        drawPlayerRune(0, HP_P0_Y);
        drawPlayerRune(1, HP_P1_Y);
    }

    private void drawPlayerRune(int playerIndex, float y) {
        Texture rune = assets.rune(state.players[playerIndex].chosenSuit);
        if (rune == null) return;

        float runeX = RUNE_CENTER_X - RUNE_SIZE / 2f;
        float runeY = y + RUNE_CENTER_Y_OFFSET - RUNE_SIZE / 2f;
        batch.draw(rune, runeX, runeY, RUNE_SIZE, RUNE_SIZE);
    }

    private void drawHpBarShape(int playerIndex, float y) {
        boolean active = state.phase == GamePhase.MAIN && state.currentPlayerIndex == playerIndex;
        float hpRatio = MathUtils.clamp(displayedHp[playerIndex] / GameConfig.PLAYER_HP_START, 0f, 1f);
        float trailRatio = MathUtils.clamp(trailingHp[playerIndex] / GameConfig.PLAYER_HP_START, 0f, 1f);
        float flash = hpFlash[playerIndex];
        float barY = y + HP_FILL_Y_OFFSET;

        shape.setColor(COL_HP_BAR_BG);
        drawAstralMeter(HP_BAR_X, barY, HP_BAR_W, HP_BAR_H);

        shape.setColor(COL_HP_BAR_TRAIL);
        drawAstralMeter(HP_BAR_X + 2f, barY + 2f, (HP_BAR_W - 4f) * trailRatio, HP_BAR_H - 4f);
        Color fill = hpRatio <= 0.25f ? COL_HP_BAR_LOW : COL_HP_BAR_FILL;
        shape.setColor(fill.r, fill.g, fill.b, 0.92f + flash * 0.08f);
        drawAstralMeter(HP_BAR_X + 2f, barY + 2f, (HP_BAR_W - 4f) * hpRatio, HP_BAR_H - 4f);
        shape.setColor(COL_HP_BAR_CORE.r, COL_HP_BAR_CORE.g, COL_HP_BAR_CORE.b,
            COL_HP_BAR_CORE.a + flash * 0.18f);
        drawAstralMeter(HP_BAR_X + 10f, barY + HP_BAR_H - 6f,
            Math.max(0f, (HP_BAR_W - 20f) * hpRatio), 3f);

        Player player = state.players[playerIndex];
        for (int i = 0; i < player.costMax; i++) {
            drawCostSlot(playerIndex, i, y, i < player.cost, active);
        }
    }

    private void drawCostSlot(int playerIndex, int index, float y, boolean filled, boolean active) {
        if (index < 0 || index >= COST_SLOT_RATIOS.length) return;
        float cx = COST_UI_X + COST_UI_W * COST_SLOT_RATIOS[index];
        float cy = y + COST_UI_Y_OFFSET + COST_UI_H * (380f / 770f);
        float pulse = active && filled ? 0.8f + 0.2f * MathUtils.sin(hudTime * 4f + index * 0.45f) : 0.55f;
        float gain = costGainAnim[playerIndex][index];
        float spend = costSpendAnim[playerIndex][index];
        Color gem = filled ? COL_COST_GEM : COL_COST_EMPTY;

        float slotScale = gain > 0f ? 0.88f + (1f - gain) * 0.12f : 1f;
        shape.setColor(gem.r, gem.g, gem.b, filled ? 0.52f + pulse * 0.38f : 0.88f);
        drawDiamond(cx, cy, 10f * slotScale, 10f * slotScale);
        if (filled) {
            shape.setColor(COL_COST_GEM_CORE.r, COL_COST_GEM_CORE.g, COL_COST_GEM_CORE.b, pulse * 0.72f);
            drawDiamond(cx, cy, 4.5f * slotScale, 7f * slotScale);
        }
        if (gain > 0f) {
            shape.setColor(COL_COST_GEM_CORE.r, COL_COST_GEM_CORE.g, COL_COST_GEM_CORE.b, gain * 0.55f);
            drawDiamond(cx, cy, 7f + gain * 4f, 9f + gain * 4f);
        }
        if (spend > 0f) {
            shape.setColor(COL_HP_BAR_LOW.r, COL_HP_BAR_LOW.g, COL_HP_BAR_LOW.b, spend * 0.38f);
            drawDiamond(cx, cy, 10f + (1f - spend) * 3f, 10f + (1f - spend) * 3f);
        }
    }

    private void drawDiamond(float cx, float cy, float halfW, float halfH) {
        shape.triangle(cx, cy + halfH, cx - halfW, cy, cx, cy - halfH);
        shape.triangle(cx, cy + halfH, cx + halfW, cy, cx, cy - halfH);
    }

    private void drawAstralMeter(float x, float y, float width, float height) {
        if (width <= 0f || height <= 0f) return;
        float cut = Math.min(height * 0.45f, width * 0.12f);
        shape.rect(x + cut, y, Math.max(0f, width - cut * 2f), height);
        shape.triangle(x, y + height / 2f, x + cut, y, x + cut, y + height);
        shape.triangle(x + width, y + height / 2f, x + width - cut, y,
            x + width - cut, y + height);
    }

    private void drawRoundedBar(float x, float y, float width, float height) {
        if (width <= 0f || height <= 0f) return;
        float radius = Math.min(height / 2f, width / 2f);
        shape.rect(x + radius, y, Math.max(0f, width - radius * 2f), height);
        shape.circle(x + radius, y + height / 2f, radius, 20);
        shape.circle(x + width - radius, y + height / 2f, radius, 20);
    }

    private void drawGameOver() {
        fonts.title.setColor(1f, 0.84f, 0f, 1f);
        String msg = "Player " + state.winnerIndex + " Wins!";
        GlyphLayout layout = new GlyphLayout(fonts.title, msg);
        fonts.title.draw(batch, msg, 800f - layout.width / 2f, 560f);
        fonts.title.setColor(Color.WHITE);
    }

    private void drawHud() {
        drawHpBarFrames();
        drawCostFrames();
        drawButtonLabels();
        drawHpBarText(0, HP_P0_Y);
        drawHpBarText(1, HP_P1_Y);
    }

    private void drawHpBarFrames() {
        Texture frame = assets.hpBar();
        if (frame == null) return;

        batch.draw(frame, HP_FRAME_X, HP_P0_Y + HP_FRAME_Y_OFFSET, HP_FRAME_W, HP_FRAME_H);
        batch.draw(frame, HP_FRAME_X, HP_P1_Y + HP_FRAME_Y_OFFSET, HP_FRAME_W, HP_FRAME_H);
    }

    private void drawCostFrames() {
        Texture frame = assets.costUi();
        if (frame == null) return;

        batch.draw(frame, COST_UI_X, HP_P0_Y + COST_UI_Y_OFFSET, COST_UI_W, COST_UI_H);
        batch.draw(frame, COST_UI_X, HP_P1_Y + COST_UI_Y_OFFSET, COST_UI_W, COST_UI_H);
    }

    private void drawHpBarText(int playerIndex, float y) {
        Player player = state.players[playerIndex];
        String value = player.hp + " / " + GameConfig.PLAYER_HP_START;
        float originalScaleX = fonts.small.getData().scaleX;
        float originalScaleY = fonts.small.getData().scaleY;
        fonts.small.getData().setScale(originalScaleX * 0.80f, originalScaleY * 0.80f);

        fonts.small.setColor(0.72f, 0.61f, 0.42f, 0.95f);
        fonts.small.draw(batch, "PLAYER " + (playerIndex + 1), HP_BAR_X, y + 47f);
        fonts.small.draw(batch, "HP  " + value, HP_BAR_X, y + 27f);
        fonts.small.getData().setScale(originalScaleX, originalScaleY);
        fonts.small.setColor(Color.WHITE);
    }

    private Array<BurialAnimation> captureBurialAnimationStarts(int ownerIndex, TurnPhase turnPhase) {
        Array<BurialAnimation> animations = new Array<>();
        if (turnPhase != TurnPhase.END || ownerIndex < 0 || ownerIndex >= state.players.length) {
            return animations;
        }

        Player owner = state.players[ownerIndex];
        float startY = ownerIndex == 1 ? FIELD_P1_Y : FIELD_P0_Y;
        for (int i = 0; i < owner.field.size; i++) {
            Card card = owner.field.get(i);
            animations.add(new BurialAnimation(
                card,
                ownerIndex,
                fieldCardCenterX(owner.field, i),
                startY));
        }
        return animations;
    }

    private void startBurialAnimationsAfterPhaseAdvance(int ownerIndex, TurnPhase previousTurnPhase,
                                                        Array<BurialAnimation> animations) {
        if (previousTurnPhase != TurnPhase.END || animations.size == 0) return;
        if (state.phase != GamePhase.MAIN || state.currentPlayerIndex == ownerIndex) return;
        startBurialAnimations(animations);
    }

    private void startBurialAnimations(Array<BurialAnimation> animations) {
        for (BurialAnimation anim : animations) {
            Player owner = state.players[anim.ownerIndex];
            if (graveyardIndexOf(owner, anim.card) < 0) continue;

            anim.endX = graveyardCardCenterX(owner, anim.ownerIndex, anim.card);
            anim.endY = graveyardCardCenterY(anim.ownerIndex, anim.card);
            anim.progress = 0f;
            burialAnims.add(anim);
        }
    }

    private void debugDrawCurrentCard() {
        if (hasCardAnimation()) return;

        Player player = state.currentPlayer();
        if (player.hand.size >= GameConfig.HAND_MAX) return;

        Card drawn = state.drawCard(player);
        if (drawn == null) return;

        startDrawAnimation(drawn, state.playerIndex(player));
    }

    private void advanceTurnPhase() {
        if (hasCardAnimation()) return;

        int playerIndex = state.currentPlayerIndex;
        TurnPhase turnPhase = state.turnPhase;
        int handSize = state.players[playerIndex].hand.size;
        Array<BurialAnimation> pendingBurials = captureBurialAnimationStarts(playerIndex, turnPhase);
        state.advanceTurnPhase();
        startBurialAnimationsAfterPhaseAdvance(playerIndex, turnPhase, pendingBurials);
        startDrawAnimationAfterPhaseAdvance(playerIndex, turnPhase, handSize);
    }

    private void startDrawAnimationAfterPhaseAdvance(int playerIndex, TurnPhase turnPhase, int handSize) {
        if (hasCardAnimation()) return;
        if (turnPhase != TurnPhase.DRAW) return;
        if (state.phase != GamePhase.MAIN || state.turnPhase != TurnPhase.ACTION) return;
        if (state.currentPlayerIndex != playerIndex) return;

        Player player = state.players[playerIndex];
        if (player.hand.size <= handSize) return;
        startDrawAnimation(player.hand.get(player.hand.size - 1), playerIndex);
    }

    private void startDrawAnimation(Card drawn, int ownerIndex) {
        if (drawn == null || ownerIndex < 0) return;

        Player owner = state.players[ownerIndex];
        int index = indexOfIdentity(owner.hand, drawn);
        if (index < 0) return;

        float handY = ownerIndex == 0 ? HAND0_Y : HAND1_Y;
        drawingCard = drawn;
        drawingOwnerIndex = ownerIndex;
        drawProgress = 0f;
        drawStartX = DECK_X;
        drawStartY = DECK_Y;
        drawEndX = handCardCenterX(owner.hand.size, index);
        drawEndY = handY + CardRenderer.CARD_H / 2f;
    }

    private void startPlayAnimation(Card played, Player owner, int ownerIndex,
                                    float startCenterX, float startCenterY) {
        if (played == null) return;
        if (playingCard != null) {
            queuedPlayAnimations.add(new PlayAnimationRequest(
                played, owner, ownerIndex, startCenterX, startCenterY));
            return;
        }
        beginPlayAnimation(played, owner, ownerIndex, startCenterX, startCenterY);
    }

    private void beginPlayAnimation(Card played, Player owner, int ownerIndex,
                                    float startCenterX, float startCenterY) {

        playingCard = played;
        playingOwnerIndex = ownerIndex;
        playingCardLandsInField = false;
        playStartX = startCenterX;
        playStartY = startCenterY;
        playEndX = PLAY_TARGET_X;
        playEndY = PLAY_TARGET_Y;
        playStartScale = 1f;
        playEndScale = 1f;
        playProgress = 0f;

        int fieldIndex = indexOfIdentity(owner.field, played);
        if (fieldIndex >= 0) {
            playingCardLandsInField = true;
            playEndX = fieldCardCenterX(owner.field, fieldIndex);
            playEndY = ownerIndex == 1 ? FIELD_P1_Y : FIELD_P0_Y;
            playEndScale = GameConfig.FIELD_SCALE;
            return;
        }

        int handIndex = indexOfIdentity(owner.hand, played);
        if (handIndex >= 0) {
            float handY = ownerIndex == 1 ? HAND1_Y : HAND0_Y;
            playEndX = handCardCenterX(owner.hand.size, handIndex);
            playEndY = handY + CardRenderer.CARD_H / 2f;
        }
    }

    private void detectCardZoneTransitions() {
        if (!hasCardAnimation()) {
            for (int ownerIndex = 0; ownerIndex < state.players.length; ownerIndex++) {
                Player player = state.players[ownerIndex];
                Array<Card> oldHand = previousHands.get(ownerIndex);
                Array<Card> oldField = previousFields.get(ownerIndex);
                Array<Card> oldGraveyard = previousGraveyards.get(ownerIndex);
                Array<Card> oldStaged = previousStagedCards.get(ownerIndex);

                boolean foundPlayedCard = false;
                for (Card played : player.field) {
                    if (indexOfIdentity(oldField, played) >= 0) continue;
                    int oldIndex = indexOfIdentity(oldHand, played);
                    int oldStagedIndex = indexOfIdentity(oldStaged, played);
                    float startX;
                    float startY;
                    if (oldIndex >= 0) {
                        float handY = ownerIndex == 0 ? HAND0_Y : HAND1_Y;
                        startX = handCardCenterX(oldHand.size, oldIndex);
                        startY = handY + CardRenderer.CARD_H / 2f;
                    } else if (oldStagedIndex >= 0) {
                        Rectangle bounds = stagedCardBounds(oldStaged, oldStagedIndex);
                        startX = bounds.x + bounds.width / 2f;
                        startY = bounds.y + bounds.height / 2f;
                    } else {
                        startX = DECK_X;
                        startY = DECK_Y + DECK_H / 2f;
                    }
                    startPlayAnimation(played, player, ownerIndex, startX, startY);
                    foundPlayedCard = true;
                }
                if (foundPlayedCard) break;

                Card drawn = firstNewCard(player.hand, oldHand);
                if (drawn != null && indexOfIdentity(oldStaged, drawn) < 0) {
                    startDrawAnimation(drawn, ownerIndex);
                    break;
                }

                Array<BurialAnimation> burials = new Array<>();
                for (Card card : oldField) {
                    if (indexOfIdentity(player.field, card) >= 0
                        || indexOfIdentity(oldGraveyard, card) >= 0
                        || graveyardIndexOf(player, card) < 0) continue;
                    int oldIndex = indexOfIdentity(oldField, card);
                    burials.add(new BurialAnimation(card, ownerIndex,
                        fieldCardCenterX(oldField, oldIndex),
                        ownerIndex == 1 ? FIELD_P1_Y : FIELD_P0_Y));
                }
                if (burials.size > 0) {
                    startBurialAnimations(burials);
                    break;
                }
            }
        }
        resetCardZoneSnapshots();
    }

    private Card firstNewCard(Array<Card> current, Array<Card> previous) {
        for (Card card : current) {
            if (indexOfIdentity(previous, card) < 0) return card;
        }
        return null;
    }

    private void resetCardZoneSnapshots() {
        previousHands.clear();
        previousFields.clear();
        previousGraveyards.clear();
        previousStagedCards.clear();
        for (Player player : state.players) {
            Array<Card> hand = new Array<>();
            hand.addAll(player.hand);
            previousHands.add(hand);

            Array<Card> field = new Array<>();
            field.addAll(player.field);
            previousFields.add(field);

            Array<Card> graveyard = new Array<>();
            for (GameState.GraveyardCard buried : player.graveyard) graveyard.add(buried.card);
            previousGraveyards.add(graveyard);

            Array<Card> staged = new Array<>();
            staged.addAll(player.stagedCards);
            previousStagedCards.add(staged);
        }
    }

    private boolean hasCardAnimation() {
        return drawingCard != null || playingCard != null
            || queuedPlayAnimations.size > 0 || burialAnims.size > 0;
    }

    private void clearCardAnimations() {
        drawingCard = null;
        drawingOwnerIndex = -1;
        drawProgress = 0f;
        playingCard = null;
        playingOwnerIndex = -1;
        playingCardLandsInField = false;
        playProgress = 0f;
        queuedPlayAnimations.clear();
        burialAnims.clear();
    }

    private void damageOpponent(int amount) {
        state.players[1 - state.currentPlayerIndex].hp -= amount;
        state.checkWinCondition();
    }

    private void restartGame() {
        clearCardAnimations();
        stagedSelectionCard = null;
        stagedSelectionRequest = null;
        state.setupTest(state.players[0].chosenSuit);
        resetHpDisplay();
        resetCardZoneSnapshots();
    }

    private void drawSelectionLayer() {
        GameState.CardSelectionRequest request = state.pendingSelection;
        if (request == null) return;

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(COL_TOOLTIP_SHADOW);
        drawRoundedRect(SELECTION_PANEL.x + 7f, SELECTION_PANEL.y - 8f,
            SELECTION_PANEL.width, SELECTION_PANEL.height, 12f);
        shape.setColor(COL_SELECTION_PANEL);
        drawRoundedRect(SELECTION_PANEL.x, SELECTION_PANEL.y,
            SELECTION_PANEL.width, SELECTION_PANEL.height, 12f);
        shape.setColor(COL_TOOLTIP_LINE);
        shape.rect(SELECTION_PANEL.x + 16f, SELECTION_PANEL.y + 48f,
            SELECTION_PANEL.width - 32f, 1f);
        float confirmX = SELECTION_CONFIRM.x + SELECTION_CONFIRM.width / 2f;
        float confirmY = SELECTION_CONFIRM.y + SELECTION_CONFIRM.height / 2f;
        Color confirmColor = stagedSelectionCard == null ? COL_TOOLTIP_LINE : COL_SELECTION_SELECTED;
        float confirmPulse = stagedSelectionCard == null ? 0f : MathUtils.sin(hudTime * 4f) * 2f;
        shape.setColor(confirmColor.r, confirmColor.g, confirmColor.b,
            stagedSelectionCard == null ? 0.16f : 0.28f);
        shape.circle(confirmX, confirmY, 23f + confirmPulse, 32);
        shape.setColor(COL_SELECTION_PANEL);
        shape.circle(confirmX, confirmY, 17f, 32);
        shape.setColor(confirmColor);
        shape.triangle(confirmX - 5f, confirmY - 8f,
            confirmX - 5f, confirmY + 8f, confirmX + 9f, confirmY);

        for (int i = 0; i < request.candidates.size; i++) {
            if (isCardInPlayerHand(request.candidates.get(i))) continue;
            Rectangle bounds = selectionCardBounds(request.candidates.size, i);
            Color cardColor = request.candidates.get(i) == stagedSelectionCard
                ? COL_SELECTION_SELECTED
                : bounds.contains(mouse) ? COL_SELECTION_HOVER : COL_SELECTION_BORDER;
            float pulse = 0.45f + 0.35f * MathUtils.sin(hudTime * 3.5f + i * 0.7f);
            shape.setColor(cardColor.r, cardColor.g, cardColor.b,
                request.candidates.get(i) == stagedSelectionCard ? 0.18f + pulse * 0.12f : 0.08f);
            shape.rect(bounds.x - 10f, bounds.y - 10f, bounds.width + 20f, bounds.height + 20f);
            drawCardGlowFrame(bounds, cardColor, pulse);
        }
        shape.end();

        batch.begin();
        fonts.tooltipTitle.setColor(Color.WHITE);
        fonts.tooltipTitle.draw(batch, request.title, SELECTION_PANEL.x + 16f,
            SELECTION_PANEL.y + SELECTION_PANEL.height - 18f);
        fonts.tooltipBody.setColor(COL_TOOLTIP_MUTED);
        fonts.tooltipBody.draw(batch, request.instruction, SELECTION_PANEL.x + 16f,
            SELECTION_PANEL.y + SELECTION_PANEL.height - 42f);
        for (int i = 0; i < request.candidates.size; i++) {
            Card card = request.candidates.get(i);
            if (isCardInPlayerHand(card)) continue;
            Rectangle bounds = selectionCardBounds(request.candidates.size, i);
            Texture illust = assets.cardIllust(card);
            if (illust != null) {
                batch.draw(illust, bounds.x, bounds.y, bounds.width, bounds.height,
                    0, 0, illust.getWidth(), illust.getHeight(), false, card.shouldFlipIllust());
            }
        }
        fonts.small.setColor(Color.WHITE);
        fonts.normal.setColor(Color.WHITE);
        fonts.tooltipBody.setColor(Color.WHITE);
        fonts.tooltipTitle.setColor(Color.WHITE);
        batch.end();
    }

    private Rectangle selectionCardBounds(int count, int index) {
        float width = CardRenderer.CARD_W * SELECTION_CARD_SCALE;
        float height = CardRenderer.CARD_H * SELECTION_CARD_SCALE;
        float total = count * width + Math.max(0, count - 1) * SELECTION_CARD_GAP;
        float startX = GameConfig.WORLD_W / 2f - total / 2f;
        return new Rectangle(startX + index * (width + SELECTION_CARD_GAP),
            SELECTION_CARDS_Y, width, height);
    }

    private boolean handleSelectionClick(boolean left) {
        GameState.CardSelectionRequest request = state.pendingSelection;
        if (request == null) return false;
        if (!left) return true;

        if (SELECTION_CONFIRM.contains(mouse) && stagedSelectionCard != null) {
            Array<Card> selected = new Array<>();
            selected.add(stagedSelectionCard);
            state.completeCardSelection(selected);
            return true;
        }

        int handIndex = getHandIndexAt(state.players[0].hand, HAND0_Y, mouse.x, mouse.y);
        if (handIndex >= 0) {
            Card card = state.players[0].hand.get(handIndex);
            if (isHandSelectionCandidate(card)) stagedSelectionCard = card;
            return true;
        }

        for (int i = 0; i < request.candidates.size; i++) {
            Card card = request.candidates.get(i);
            if (isCardInPlayerHand(card)) continue;
            if (selectionCardBounds(request.candidates.size, i).contains(mouse)) {
                stagedSelectionCard = card;
                return true;
            }
        }
        return true;
    }

    private void syncSelectionState() {
        if (state.pendingSelection == stagedSelectionRequest) return;
        stagedSelectionRequest = state.pendingSelection;
        stagedSelectionCard = null;
    }

    private boolean isHandSelectionCandidate(Card card) {
        if (card == null || state.pendingSelection == null) return false;
        for (Card candidate : state.pendingSelection.candidates) {
            if (candidate == card) return true;
        }
        return false;
    }

    private boolean isCardInPlayerHand(Card card) {
        return indexOfIdentity(state.players[0].hand, card) >= 0;
    }

    // 입력 처리
    private void handleClick() {
        boolean left  = Gdx.input.justTouched();
        boolean right = Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);
        if (!left && !right) return;
        float wx = mouse.x, wy = mouse.y;

        if (handleSelectionClick(left)) return;

        if (state.phase == GamePhase.GAME_OVER) {
            if (left && btnRestart.contains(wx, wy))
                restartGame();
            return;
        }

        if (hasCardAnimation()) return;

        if (left) {
            if (btnDraw.contains(wx, wy)) {
                Player current = state.currentPlayer();
                if (current.stagedCards.size > 0) {
                    state.resolveStagedCards(current);
                } else {
                    debugDrawCurrentCard();
                }
                return;
            }
            if (btnPhase.contains(wx, wy)) {
                advanceTurnPhase();
                return;
            }
            if (btnDebug.contains(wx, wy)) {
                damageOpponent(25);
                return;
            }
        }

        // 카드 클릭
        Player current = state.currentPlayer();
        int stagedIndex = stagedCardIndexAt(current, wx, wy);
        if (left && stagedIndex >= 0) {
            state.unstageCard(current, stagedIndex);
            return;
        }

        float  handY = (state.currentPlayerIndex == 0) ? HAND0_Y : HAND1_Y;
        int    idx = getHandIndexAt(current.hand, handY, wx, wy);

        if (idx < 0) return;

        if (left) {
            state.stageCardFromHand(current, idx);
        } else {
            Card card = current.hand.get(idx);
            card.setReversed(!card.reversed, false);
        }
    }

    // Lifecycle
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        if (debugContext != null) {
            debugContext.clearState(state);
        }
        batch.dispose();
        shape.dispose();
        fonts.dispose();
    }
}
