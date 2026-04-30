package io.eclipse.arcana;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.eclipse.arcana.model.Card;
import io.eclipse.arcana.model.GamePhase;
import io.eclipse.arcana.model.GameState;
import io.eclipse.arcana.model.Player;
import io.eclipse.arcana.render.CardRenderer;

public class MainScreen implements Screen {

    private static final float GAP = -8f;
    private static final float HAND0_Y = -42f;
    private static final float HAND1_Y = 816f;

    private static final float BTN_W = 180f;
    private static final float BTN_H = 36f;
    private static final float BTN_X = 1390f;

    private static final Color COL_BTN_NORMAL = new Color(0.15f, 0.15f, 0.25f, 1f);
    private static final Color COL_BTN_DANGER = new Color(0.25f, 0.08f, 0.08f, 1f);
    private static final Color COL_BTN_OK     = new Color(0.10f, 0.30f, 0.10f, 1f);
    private static final Color COL_BTN_BORDER = new Color(0.7f,  0.6f,  0.2f,  1f);

    private final Rectangle btnDraw    = new Rectangle(BTN_X, 560f, BTN_W, BTN_H);
    private final Rectangle btnPhase   = new Rectangle(BTN_X, 510f, BTN_W, BTN_H);
    private final Rectangle btnDebug   = new Rectangle(BTN_X, 460f, BTN_W, BTN_H);
    private final Rectangle btnRestart = new Rectangle(710f,  460f, BTN_W, BTN_H);

    private final Core game;

    private SpriteBatch batch;
    private ShapeRenderer shape;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private FontManager fonts;

    private GameState state;
    private final Vector2 touch = new Vector2();

    public MainScreen(Core game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(1600f, 1000f, camera);
        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        // HdpiMode.Pixels 기준: 물리 픽셀 / 월드 너비 = 유닛당 픽셀 수
        fonts = new FontManager(Gdx.graphics.getWidth() / 1600f);

        state = new GameState();
    }

    @Override
    public void render(float delta) {
        state.update(delta);
        handleClick();

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        camera.update();
        shape.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Shape 패스 — 카드 배경/테두리 전체 + 버튼
        shape.begin(ShapeRenderer.ShapeType.Filled);
        drawHandShapes(state.players[0].hand, HAND0_Y, false);
        drawHandShapes(state.players[1].hand, HAND1_Y, true);
        drawButtonShapes();
        shape.end();

        // 텍스트 + HUD 패스
        batch.begin();
        drawHandText(state.players[0].hand, HAND0_Y, false);
        drawHandText(state.players[1].hand, HAND1_Y, true);
        drawHud();
        drawButtonLabels();
        if (state.phase == GamePhase.GAME_OVER) drawGameOver();
        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // ── 핸드 위치 계산 ────────────────────────────────────────────────────────
    private float handStartX(int count) {
        float totalWidth = count * CardRenderer.CARD_W + Math.max(0, count - 1) * GAP;
        return 800f - totalWidth / 2f;
    }

    // ── 카드 렌더링 ──────────────────────────────────────────────────────────

    private void drawHandShapes(Array<Card> hand, float baseY, boolean isBack) {
        float startX = handStartX(hand.size);
        for (int i = 0; i < hand.size; i++) {
            float x = startX + i * (CardRenderer.CARD_W + GAP);
            if (isBack) {
                CardRenderer.drawBack(shape, x, baseY);
            } else {
                CardRenderer.drawShape(shape, hand.get(i), x, baseY);
            }
        }
    }

    private void drawHandText(Array<Card> hand, float baseY, boolean isBack) {
        if (isBack) return;
        float startX = handStartX(hand.size);
        for (int i = 0; i < hand.size; i++) {
            CardRenderer.drawText(batch, fonts.small, hand.get(i), startX + i * (CardRenderer.CARD_W + GAP), baseY);
        }
    }

    // ── 버튼 렌더링 ──────────────────────────────────────────────────────────
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
        fonts.small.draw(batch, text, r.x + (r.width - gl.width) / 2f, r.y + (r.height + gl.height) / 2f);
    }

    // ── HUD / 게임오버 ──────────────────────────────────────────────────────

    private void drawGameOver() {
        fonts.title.setColor(1f, 0.84f, 0f, 1f);
        String msg = "Player " + state.winnerIndex + " Wins!";
        GlyphLayout layout = new GlyphLayout(fonts.title, msg);
        fonts.title.draw(batch, msg, 800f - layout.width / 2f, 560f);
        fonts.title.setColor(Color.WHITE);
    }

    private void drawHud() {
        fonts.normal.setColor(1f, 1f, 1f, 1f);
        fonts.normal.draw(batch,
            String.format("P0  HP: %d   |   P1  HP: %d", state.players[0].hp, state.players[1].hp),
            20f, 540f);
        fonts.normal.draw(batch,
            String.format("Phase: %s   Turn: %s   Timer: %.1fs   Active: P%d",
                state.phase, state.turnPhase, state.turnTimer, state.currentPlayerIndex),
            20f, 515f);

        fonts.small.setColor(0.55f, 0.55f, 0.55f, 1f);
        fonts.small.draw(batch, "좌클릭: 카드 플레이   우클릭: Reversed 토글", 20f, 492f);
        fonts.small.setColor(Color.WHITE);

        fonts.normal.setColor(1f, 0.84f, 0f, 1f);
        if (state.currentPlayerIndex == 0) {
            fonts.normal.draw(batch, "▲ P0 (your turn)", 680f, 175f);
        } else {
            fonts.normal.draw(batch, "▼ P1 (your turn)", 680f, 845f);
        }
        fonts.normal.setColor(Color.WHITE);
    }

    // ── 입력 처리 ────────────────────────────────────────────────────────────

    private void handleClick() {
        boolean left  = Gdx.input.justTouched();
        boolean right = Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);
        if (!left && !right) return;

        touch.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(touch);
        float wx = touch.x, wy = touch.y;

        if (state.phase == GamePhase.GAME_OVER) {
            if (left && btnRestart.contains(wx, wy))
                state.setupTest(state.players[0].chosenSuit);
            return;
        }

        // 버튼 클릭 (좌클릭만)
        if (left) {
            if (btnDraw.contains(wx, wy)) {
                Card drawn = state.currentPlayer().deck.draw();
                if (drawn != null) state.currentPlayer().hand.add(drawn);
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

        // 현재 플레이어 핸드 카드 클릭
        Player current = state.currentPlayer();
        float handY  = (state.currentPlayerIndex == 0) ? HAND0_Y : HAND1_Y;
        float startX = handStartX(current.hand.size);

        for (int i = 0; i < current.hand.size; i++) {
            float cx = startX + i * (CardRenderer.CARD_W + GAP);
            if (wx >= cx && wx <= cx + CardRenderer.CARD_W
             && wy >= handY && wy <= handY + CardRenderer.CARD_H) {
                if (left) {
                    current.hand.removeIndex(i);
                    state.checkWinCondition();
                } else {
                    current.hand.get(i).reversed = !current.hand.get(i).reversed;
                }
                return;
            }
        }
    }

    // ── 생명주기 ─────────────────────────────────────────────────────────────

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shape.dispose();
        fonts.dispose();
    }
}
