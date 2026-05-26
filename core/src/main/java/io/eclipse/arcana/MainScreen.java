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
    private static final float DECK_LAYER_OFFSET_X = GameConfig.DECK_LAYER_OFFSET_X;
    private static final float DECK_LAYER_OFFSET_Y = GameConfig.DECK_LAYER_OFFSET_Y;

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

    private Card  drawingCard    = null;
    private int   drawingOwnerIndex = -1;
    private float drawProgress   = 0f;
    private float drawStartX, drawStartY;
    private float drawEndX, drawEndY;

    private static final Color COL_BTN_NORMAL = new Color(0.15f, 0.15f, 0.25f, 1f);
    private static final Color COL_BTN_DANGER = new Color(0.25f, 0.08f, 0.08f, 1f);
    private static final Color COL_BTN_OK     = new Color(0.10f, 0.30f, 0.10f, 1f);
    private static final Color COL_BTN_BORDER = new Color(0.7f,  0.6f,  0.2f,  1f);
    private static final Color COL_GRAVE_BG = new Color(0.04f, 0.04f, 0.06f, 0.68f);
    private static final Color COL_GRAVE_BORDER = new Color(0.38f, 0.33f, 0.22f, 0.85f);

    // DEBUG PANEL
    private static final float PANEL_X     = 1390f;
    private static final float PANEL_Y     = 380f;
    private static final float PANEL_W     = 180f;

    private final Rectangle btnDraw    = new Rectangle(PANEL_X + 5f, PANEL_Y - 50f,  PANEL_W - 10f, BTN_H);
    private final Rectangle btnPhase   = new Rectangle(PANEL_X + 5f, PANEL_Y - 95f,  PANEL_W - 10f, BTN_H);
    private final Rectangle btnDebug   = new Rectangle(PANEL_X + 5f, PANEL_Y - 140f, PANEL_W - 10f, BTN_H);
    private final Rectangle btnRestart = new Rectangle(PANEL_X + 5f, PANEL_Y - 185f, PANEL_W - 10f, BTN_H);

    private final Core game;
    private final DebugContext debugContext;

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

    // 카드 사용 애니메이션 상태
    private Card  playingCard    = null;
    private int   playingOwnerIndex = -1;
    private boolean playingCardLandsInField = false;
    private float playStartX, playStartY;
    private float playEndX, playEndY;
    private float playStartScale = 1f, playEndScale = 1f;
    private float playProgress   = 0f;
    private final Array<BurialAnimation> burialAnims = new Array<>();

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

    public MainScreen(Core game) {
        this(game, null);
    }

    public MainScreen(Core game, DebugContext debugContext) {
        this.game = game;
        this.debugContext = debugContext;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(1600f, 1000f, camera);
        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        fonts = new FontManager(Gdx.graphics.getWidth() / 1600f);

        assets = new ArcanaAssets();
        assets.finishLoading();

        state = new GameState();
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
        state.update(delta);
        startBurialAnimationsAfterPhaseAdvance(beforePlayerIndex, beforeTurnPhase, pendingBurials);
        startDrawAnimationAfterPhaseAdvance(beforePlayerIndex, beforeTurnPhase, beforeHandSize);

        mouse.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mouse);

        updateHover(delta);
        updatePlayAnim(delta);
        updateDrawAnim(delta);
        updateBurialAnims(delta);

        handleClick();

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
        batch.end();

        shape.begin(ShapeRenderer.ShapeType.Filled);
        drawHandShapes(state.players[0].hand, HAND0_Y, false);
        drawHandShapes(state.players[1].hand, HAND1_Y, true);
        drawGraveyardZoneShapes();
        drawButtonShapes();
        shape.end();

        batch.begin();
        drawDeck();
        drawField();
        drawGraveyards();
        drawDrawingCard();
        drawHandBatch(state.players[0].hand, HAND0_Y, false);
        drawHandBatch(state.players[1].hand, HAND1_Y, true);
        drawPlayingCard();
        drawBurialAnimations();
        drawHud();
        if (state.phase == GamePhase.GAME_OVER) drawGameOver();
        batch.end();

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

    private void updateHover(float delta) {
        if (state.currentPlayerIndex != 0) {
            // P1 턴이면 호버 없음
            hoveredIndex = -1;
        } else {
            hoveredIndex = getHandIndexAt(
                state.players[0].hand, HAND0_Y, mouse.x, mouse.y);
        }

        for (int i = 0; i < hoverAnims.length; i++) {
            float target = (i == hoveredIndex) ? 1f : 0f;
            hoverAnims[i] += (target - hoverAnims[i]) * HOVER_SPEED * delta;
            if (Math.abs(hoverAnims[i] - target) < 0.01f) hoverAnims[i] = target;
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
        return baseY + hoverLift(index) + CardRenderer.CARD_H / 2f;
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
            if (wx >= cx && wx <= cx + CardRenderer.CARD_W
                && wy >= baseY && wy <= baseY + CardRenderer.CARD_H) {
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
            float y = baseY + (isBack ? 0 : hoverLift(i));

            if (isBack) {
                Texture back = assets.cardBack();
                if (back == null) CardRenderer.drawBack(shape, x, y);
            } else {
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
            float y = baseY + (isBack ? 0 : hoverLift(i));
            Card  card = hand.get(i);
            if (isCardHiddenInHand(card)) continue;

            if (isBack) {
                if (back != null) CardRenderer.drawIllust(batch, back, x, y, card.reversed);
                continue;
            }

            Texture illust  = assets.cardIllust(card);
            Texture costTex = assets.cardCost(card);

            if (illust != null) {
                CardRenderer.drawIllust(batch, illust, x, y, card.shouldFlipIllust());
                CardRenderer.drawCost(batch, fonts.small, costTex, card, x, y, costSize(card));
            } else {
                CardRenderer.drawText(batch, fonts.small, card, x, y);
            }
        }
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

    private void drawGraveyardZoneShapes() {
        drawGraveyardZoneShape(GRAVE_X, GRAVE_P0_Y);
        drawGraveyardZoneShape(GRAVE_X, GRAVE_P1_Y);
    }

    private void drawGraveyardZoneShape(float x, float y) {
        shape.setColor(COL_GRAVE_BORDER);
        shape.rect(x, y, GRAVE_W, GRAVE_H);
        shape.setColor(COL_GRAVE_BG);
        shape.rect(x + 2f, y + 2f, GRAVE_W - 4f, GRAVE_H - 4f);
    }

    private void drawGraveyards() {
        drawPlayerGraveyard(state.players[0], "P0 무덤", GRAVE_X, GRAVE_P0_Y);
        drawPlayerGraveyard(state.players[1], "P1 무덤", GRAVE_X, GRAVE_P1_Y);
    }

    private void drawPlayerGraveyard(Player player, String label, float x, float y) {
        fonts.small.setColor(0.82f, 0.76f, 0.62f, 1f);
        fonts.small.draw(batch, label + " " + player.graveyard.size, x + 10f, y + GRAVE_H - 10f);

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
            fonts.small.setColor(1f, 0.92f, 0.68f, 1f);
            fonts.small.draw(batch, String.valueOf(buried.turnsRemaining),
                cardX + shown * (cardW + GRAVE_CARD_GAP) + cardW - 12f,
                cardY + cardH - 6f);
            shown++;
        }

        if (drawableCount > shown) {
            fonts.small.setColor(0.75f, 0.72f, 0.66f, 1f);
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
        drawBtn(btnDraw,  COL_BTN_NORMAL);
        drawBtn(btnPhase, COL_BTN_NORMAL);
        drawBtn(btnDebug, COL_BTN_DANGER);
    }

    private void drawBtn(Rectangle r, Color bg) {
        shape.setColor(COL_BTN_BORDER);
        shape.rect(r.x, r.y, r.width, r.height);
        shape.setColor(bg);
        shape.rect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
    }

    private void drawButtonLabels() {
        fonts.small.setColor(1f, 1f, 1f, 1f);
        if (state.phase == GamePhase.GAME_OVER) {
            drawBtnLabel("다시 시작", btnRestart);
        } else {
            drawBtnLabel("드로우", btnDraw);
            drawBtnLabel("페이즈 진행", btnPhase);
            drawBtnLabel("디버그: -25HP", btnDebug);
        }
        fonts.small.setColor(Color.WHITE);
    }

    private void drawBtnLabel(String text, Rectangle r) {
        GlyphLayout gl = new GlyphLayout(fonts.small, text);
        fonts.small.draw(batch, text,
            r.x + (r.width  - gl.width)  / 2f,
            r.y + (r.height + gl.height) / 2f);
    }

    // HUD
    private void drawGameOver() {
        fonts.title.setColor(1f, 0.84f, 0f, 1f);
        String msg = "Player " + state.winnerIndex + " Wins!";
        GlyphLayout layout = new GlyphLayout(fonts.title, msg);
        fonts.title.draw(batch, msg, 800f - layout.width / 2f, 560f);
        fonts.title.setColor(Color.WHITE);
    }

    private void drawHud() {
        drawButtonLabels();
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

    private boolean hasCardAnimation() {
        return drawingCard != null || playingCard != null || burialAnims.size > 0;
    }

    private void clearCardAnimations() {
        drawingCard = null;
        drawingOwnerIndex = -1;
        drawProgress = 0f;
        playingCard = null;
        playingOwnerIndex = -1;
        playingCardLandsInField = false;
        playProgress = 0f;
        burialAnims.clear();
    }

    private void damageOpponent(int amount) {
        state.players[1 - state.currentPlayerIndex].hp -= amount;
        state.checkWinCondition();
    }

    private void restartGame() {
        clearCardAnimations();
        state.setupTest(state.players[0].chosenSuit);
    }

    // 입력 처리
    private void handleClick() {
        boolean left  = Gdx.input.justTouched();
        boolean right = Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);
        if (!left && !right) return;
        float wx = mouse.x, wy = mouse.y;

        if (state.phase == GamePhase.GAME_OVER) {
            if (left && btnRestart.contains(wx, wy))
                restartGame();
            return;
        }

        if (hasCardAnimation()) return;

        if (left) {
            if (btnDraw.contains(wx, wy)) {
                debugDrawCurrentCard();
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
        float  handY = (state.currentPlayerIndex == 0) ? HAND0_Y : HAND1_Y;
        int    idx = getHandIndexAt(current.hand, handY, wx, wy);

        if (idx < 0) return;

        if (left) {
            int ownerIndex = state.playerIndex(current);
            float startCenterX = handCardCenterX(current.hand.size, idx);
            float startCenterY = handCardCenterY(handY, idx);

            Card played = state.playCardFromHand(current, idx);
            if (played == null) return;

            startPlayAnimation(played, current, ownerIndex, startCenterX, startCenterY);

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
        assets.dispose();
    }
}
