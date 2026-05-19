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

    // 호버
    private static final float HOVER_LIFT = GameConfig.HOVER_LIFT;
    private static final float HOVER_SPEED = GameConfig.HOVER_SPEED;

    // 애니메이션
    private static final float PLAY_TARGET_X = GameConfig.PLAY_TARGET_X;
    private static final float PLAY_TARGET_Y = GameConfig.PLAY_TARGET_Y;
    private static final float PLAY_SPEED = GameConfig.PLAY_SPEED;
    private static final float DRAW_SPEED = GameConfig.DRAW_SPEED;

    // 버튼
    private static final float BTN_W = GameConfig.BTN_W;
    private static final float BTN_H = GameConfig.BTN_H;
    private static final float BTN_X = GameConfig.BTN_X;

    private Card  drawingCard    = null;
    private float drawProgress   = 0f;
    private float drawEndX, drawEndY;

    private boolean showDebugPanel = false;

    private static final Color COL_BTN_NORMAL = new Color(0.15f, 0.15f, 0.25f, 1f);
    private static final Color COL_BTN_DANGER = new Color(0.25f, 0.08f, 0.08f, 1f);
    private static final Color COL_BTN_OK     = new Color(0.10f, 0.30f, 0.10f, 1f);
    private static final Color COL_BTN_BORDER = new Color(0.7f,  0.6f,  0.2f,  1f);

    // DEBUG PANEL
    private static final float PANEL_X     = 1390f;
    private static final float PANEL_Y     = 380f;
    private static final float PANEL_W     = 180f;
    private static final float PANEL_H     = 400f;
    private static final float PANEL_PAD   = 10f;
    private static final Color COL_PANEL_BG     = new Color(0.08f, 0.08f, 0.12f, 0.85f);
    private static final Color COL_PANEL_BORDER = new Color(0.7f,  0.6f,  0.2f,  1f);
    private static final Color COL_SECTION      = new Color(0.5f,  0.45f, 0.2f,  1f);

    private final Rectangle btnDraw    = new Rectangle(PANEL_X + 5f, PANEL_Y - 50f,  PANEL_W - 10f, BTN_H);
    private final Rectangle btnPhase   = new Rectangle(PANEL_X + 5f, PANEL_Y - 95f,  PANEL_W - 10f, BTN_H);
    private final Rectangle btnDebug   = new Rectangle(PANEL_X + 5f, PANEL_Y - 140f, PANEL_W - 10f, BTN_H);
    private final Rectangle btnRestart = new Rectangle(PANEL_X + 5f, PANEL_Y - 185f, PANEL_W - 10f, BTN_H);

    private final Core game;

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
    private float playStartX, playStartY;
    private float playProgress   = 0f;

    public MainScreen(Core game) {
        this.game = game;
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
    }

    @Override
    public void render(float delta) {
        state.update(delta);

        mouse.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mouse);

        updateHover(delta);
        updatePlayAnim(delta);
        updateDrawAnim(delta);

        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            showDebugPanel = !showDebugPanel;
        }

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
        drawButtonShapes();
        shape.end();

        batch.begin();
        drawDeck();
        drawField();
        drawDrawingCard();
        drawHandBatch(state.players[0].hand, HAND0_Y, false);
        drawHandBatch(state.players[1].hand, HAND1_Y, true);
        drawPlayingCard();
        drawHud();
        if (state.phase == GamePhase.GAME_OVER) drawGameOver();
        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
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
            playProgress  = 0f;
        }
    }

    private void updateDrawAnim(float delta) {
        if (drawingCard == null) return;
        drawProgress += DRAW_SPEED * delta;
        if (drawProgress >= 1f) {
            drawingCard = null;
            drawProgress = 0f;
        }
    }

    // 핸드 위치 계산
    private float handStartX(int count) {
        float totalWidth = count * CardRenderer.CARD_W
            + Math.max(0, count - 1) * GAP;
        return 800f - totalWidth / 2f;
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
            float x = startX + i * (CardRenderer.CARD_W + GAP);
            // 호버 리프트 적용 (P0 핸드만)
            float y = baseY + (isBack ? 0 : hoverLift(i));

            if (isBack) {
                Texture back = assets.cardBack();
                if (back == null) CardRenderer.drawBack(shape, x, y);
            } else {
                Texture illust = assets.cardIllust(hand.get(i));
                if (illust == null) CardRenderer.drawShape(shape, hand.get(i), x, y);
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
        drawPlayerField(state.players[0].field, FIELD_P0_Y);  // P0 아래
        drawPlayerField(state.players[1].field, FIELD_P1_Y);  // P1 위
    }

    private void drawPlayerField(Array<Card> field, float baseY) {
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

        // 보간으로 현재 위치 계산
        float t  = Interpolation.swingOut.apply(playProgress);
        float cx = playStartX + (PLAY_TARGET_X - playStartX) * t;
        float cy = playStartY + (PLAY_TARGET_Y - playStartY) * t;

        float scale = 1f + 0.15f * (float) Math.sin(playProgress * Math.PI);
        float w = CardRenderer.CARD_W * scale;
        float h = CardRenderer.CARD_H * scale;

        Texture illust = assets.cardIllust(playingCard);
        if (illust != null) {
            // 중앙 기준으로 그리기 위해 offset 계산
            batch.draw(illust, cx - w / 2f, cy - h / 2f, w, h);
        }
    }

    private void drawDrawingCard() {
        if (drawingCard == null) return;

        float t  = Interpolation.smooth.apply(drawProgress);
        // 덱 중앙에서 핸드 끝으로
        float cx = (DECK_X) + (drawEndX - DECK_X) * t;
        float cy = (DECK_Y) + (drawEndY - DECK_Y) * t;

        // 날아가면서 납작함 → 원래 크기로 복원
        // 덱에서 나올 땐 납작하게, 핸드에 도착하면 원래 비율
        float h = DECK_H + (CardRenderer.CARD_H - DECK_H) * t;
        float w = CardRenderer.CARD_W;

        Texture back = assets.cardBack();
        if (back != null) {
            batch.draw(back, cx - w / 2f, cy - h / 2f, w, h);
        }
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
        if (showDebugPanel) {
            shape.setColor(COL_PANEL_BG);
            shape.rect(PANEL_X, PANEL_Y, PANEL_W, PANEL_H);
            shape.setColor(COL_PANEL_BORDER);
            shape.rect(PANEL_X, PANEL_Y, PANEL_W, 1f);
            shape.rect(PANEL_X, PANEL_Y + PANEL_H, PANEL_W, 1f);
            shape.rect(PANEL_X, PANEL_Y,1f, PANEL_H);
            shape.rect(PANEL_X + PANEL_W - 1f, PANEL_Y, 1f, PANEL_H);
        }

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
        if (showDebugPanel) {
            // 패널 텍스트
            float lx = PANEL_X + PANEL_PAD;
            float ly = PANEL_Y + PANEL_H - PANEL_PAD;
            float lh = 18f;  // 줄 간격

            // 타이틀
            fonts.small.setColor(COL_PANEL_BORDER);
            fonts.small.draw(batch, "[ DEBUG ]", lx, ly);
            ly -= lh * 1.5f;

            // 구분선 역할 라벨
            fonts.small.setColor(COL_SECTION);
            fonts.small.draw(batch, "P0", lx, ly);
            ly -= lh;

            fonts.small.setColor(1f, 1f, 1f, 1f);
            fonts.small.draw(batch, "HP: " + state.players[0].hp, lx, ly); ly -= lh;
            fonts.small.draw(batch, "Cost: " + state.players[0].cost + "/" + state.players[0].costInit, lx, ly); ly -= lh;
            fonts.small.draw(batch, "Reversed: " + countReversed(state.players[0]), lx, ly); ly -= lh;
            fonts.small.draw(batch, "Field: " + state.players[0].field.size, lx, ly); ly -= lh * 1.5f;

            fonts.small.setColor(COL_SECTION);
            fonts.small.draw(batch, "P1", lx, ly);
            ly -= lh;

            fonts.small.setColor(1f, 1f, 1f, 1f);
            fonts.small.draw(batch, "HP: " + state.players[1].hp, lx, ly); ly -= lh;
            fonts.small.draw(batch, "Cost: " + state.players[1].cost + "/" + state.players[1].costInit, lx, ly); ly -= lh;
            fonts.small.draw(batch, "Reversed: " + countReversed(state.players[1]), lx, ly); ly -= lh;
            fonts.small.draw(batch, "Field: " + state.players[1].field.size, lx, ly); ly -= lh * 1.5f;

            fonts.small.setColor(COL_SECTION);
            fonts.small.draw(batch, "GAME", lx, ly);
            ly -= lh;

            fonts.small.setColor(1f, 1f, 1f, 1f);
            fonts.small.draw(batch, "Phase: " + state.phase, lx, ly); ly -= lh;
            fonts.small.draw(batch, "Turn: " + state.turnPhase, lx, ly); ly -= lh;
            fonts.small.draw(batch, String.format("Timer: %.1fs", state.turnTimer), lx, ly); ly -= lh;
            fonts.small.draw(batch, "Deck: " + state.currentPlayer().deck.size(), lx, ly); ly -= lh;
            fonts.small.draw(batch, "Active: P" + state.currentPlayerIndex, lx, ly);

            fonts.small.setColor(Color.WHITE);
        }

        // 버튼 라벨
        drawButtonLabels();

        fonts.small.setColor(0.4f, 0.4f, 0.4f, 1f);
        fonts.small.draw(batch, "[TAB] Debug", PANEL_X + PANEL_PAD, PANEL_Y - 195f);
        fonts.small.setColor(Color.WHITE);
    }

    // 역방향 카드 수 카운트
    private int countReversed(Player player) {
        int count = 0;
        for (Card c : player.hand) {
            if (c.reversed) count++;
        }
        return count;
    }

    // 입력 처리
    private void handleClick() {
        boolean left  = Gdx.input.justTouched();
        boolean right = Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);
        if (!left && !right) return;
        float wx = mouse.x, wy = mouse.y;

        if (state.phase == GamePhase.GAME_OVER) {
            if (left && btnRestart.contains(wx, wy))
                state.setupTest(state.players[0].chosenSuit);
            return;
        }

        if (left) {
            if (btnDraw.contains(wx, wy)) {
                if (state.currentPlayer().hand.size < GameConfig.HAND_MAX) {
                    Card drawn = state.drawCard(state.currentPlayer());
                    if (drawn != null) {
                        drawingCard = drawn;
                        drawProgress = 0f;
                        int handSize = Math.max(1, state.currentPlayer().hand.size);
                        int nextIdx = handSize - 1;
                        drawEndX = handStartX(handSize)
                            + nextIdx * (CardRenderer.CARD_W + GAP)
                            + CardRenderer.CARD_W / 2f;
                        drawEndY = HAND0_Y + CardRenderer.CARD_H / 2f;
                    }
                }
                return;
            }
            if (btnPhase.contains(wx, wy)) {
                state.advanceTurnPhase();
                return;
            }
            if (btnDebug.contains(wx, wy)) {
                state.players[1 - state.currentPlayerIndex].hp -= 25;
                state.checkWinCondition();
                return;
            }
        }

        // 카드 클릭
        Player current = state.currentPlayer();
        float  handY = (state.currentPlayerIndex == 0) ? HAND0_Y : HAND1_Y;
        int    idx = getHandIndexAt(current.hand, handY, wx, wy);

        if (idx < 0) return;

        if (left) {
            // 애니메이션 시작 — 카드 시작 위치 저장
            float startX = handStartX(current.hand.size)
                + idx * (CardRenderer.CARD_W + GAP);
            float startY = handY + hoverLift(idx);

            Card played = state.playCardFromHand(current, idx);
            if (played == null) return;

            playingCard = played;
            playStartX = startX + CardRenderer.CARD_W / 2f;  // 카드 중앙
            playStartY = startY + CardRenderer.CARD_H / 2f;
            playProgress = 0f;

        } else {
            current.hand.get(idx).reversed = !current.hand.get(idx).reversed;
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
        batch.dispose();
        shape.dispose();
        fonts.dispose();
        assets.dispose();
    }
}
