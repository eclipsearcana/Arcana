package io.eclipse.arcana;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class TutorialScreen implements Screen {
    private static final float WORLD_W = 1600f;
    private static final float WORLD_H = 1000f;
    private static final Rectangle PREVIOUS = new Rectangle(250f, 105f, 220f, 62f);
    private static final Rectangle NEXT = new Rectangle(1130f, 105f, 220f, 62f);
    private static final Rectangle PANEL = new Rectangle(210f, 190f, 1180f, 650f);

    private static final String[] TITLES = {
        "승리 조건", "턴 진행", "카드 규칙", "화면 정보", "덱 구성"
    };

    private static final String[] SUBTITLES = {
        "상대의 HP를 0으로 만들면 승리합니다.",
        "드로우, 카드 선택, 실행 순서로 턴이 진행됩니다.",
        "모든 카드는 코스트와 정방향·역방향 효과를 가집니다.",
        "체력, 코스트, 상태 효과와 무덤을 확인하세요.",
        "문양을 고르고 메이저 카드를 드래프트합니다."
    };

    private static final String[][] BULLETS = {
        {
            "카드를 사용해 피해를 주고 자신의 HP를 관리하세요.",
            "상대의 상태와 남은 코스트를 확인하며 다음 수를 준비하세요."
        },
        {
            "보유 코스트 안에서 사용할 카드를 선택합니다.",
            "행동을 마치면 페이즈 진행 버튼으로 상대 턴을 시작합니다."
        },
        {
            "카드 방향에 따라 적용되는 효과가 달라질 수 있습니다.",
            "카드에 마우스를 올리면 현재 효과와 반대 효과를 확인할 수 있습니다."
        },
        {
            "체력바 주변 아이콘은 현재 적용 중인 버프와 디버프입니다.",
            "사용한 카드는 무덤으로 이동하며, 일부 효과로 다시 돌아올 수 있습니다."
        },
        {
            "선택한 문양이 마이너 덱의 중심이 됩니다.",
            "메이저 카드 드래프트가 끝나면 바로 게임이 시작됩니다."
        }
    };

    private final Core game;
    private final Vector3 pointer = new Vector3();
    private final GlyphLayout layout = new GlyphLayout();
    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shape;
    private FontManager fonts;
    private int page;

    public TutorialScreen(Core game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        fonts = new FontManager(Gdx.graphics.getWidth() / WORLD_W);
    }

    @Override
    public void render(float delta) {
        updatePointer();
        if (handleInput()) {
            return;
        }

        Gdx.gl.glClearColor(0.015f, 0.018f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        camera.update();

        drawBackground();
        drawPanels();
        drawText();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private boolean handleInput() {
        boolean clicked = Gdx.input.justTouched();
        if (page > 0 && ((clicked && PREVIOUS.contains(pointer.x, pointer.y))
            || Gdx.input.isKeyJustPressed(Input.Keys.LEFT))) {
            page--;
            return false;
        }

        if ((clicked && NEXT.contains(pointer.x, pointer.y))
            || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)
            || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (page < TITLES.length - 1) {
                page++;
            } else {
                game.showDeckSelect();
                dispose();
                return true;
            }
        }
        return false;
    }

    private void drawBackground() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        Texture background = game.assets.background();
        if (background != null) {
            batch.setColor(0.28f, 0.28f, 0.4f, 1f);
            batch.draw(background, 0f, 0f, WORLD_W, WORLD_H);
            batch.setColor(Color.WHITE);
        }
        batch.end();
    }

    private void drawPanels() {
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.008f, 0.008f, 0.025f, 0.9f);
        shape.rect(PANEL.x, PANEL.y, PANEL.width, PANEL.height);

        shape.setColor(0.48f, 0.22f, 0.75f, 0.15f);
        shape.rect(PANEL.x + 54f, PANEL.y + 118f, 5f, 325f);
        shape.rect(PANEL.x + 760f, PANEL.y + 110f, 1f, 390f);

        drawButton(PREVIOUS, page > 0, page > 0 && PREVIOUS.contains(pointer.x, pointer.y));
        drawButton(NEXT, true, NEXT.contains(pointer.x, pointer.y));

        for (int i = 0; i < TITLES.length; i++) {
            shape.setColor(i == page
                ? new Color(0.7f, 0.38f, 0.95f, 1f)
                : new Color(0.35f, 0.3f, 0.45f, 0.65f));
            shape.circle(720f + i * 40f, 137f, i == page ? 6f : 4f, 20);
        }
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0.58f, 0.3f, 0.82f, 0.8f);
        shape.rect(PANEL.x, PANEL.y, PANEL.width, PANEL.height);
        drawCornerAccents(PANEL);
        shape.end();
    }

    private void drawButton(Rectangle bounds, boolean enabled, boolean hovered) {
        if (!enabled) {
            shape.setColor(0.08f, 0.07f, 0.12f, 0.4f);
        } else if (hovered) {
            shape.setColor(0.42f, 0.17f, 0.64f, 0.95f);
        } else {
            shape.setColor(0.2f, 0.09f, 0.32f, 0.9f);
        }
        shape.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    private void drawCornerAccents(Rectangle bounds) {
        float length = 28f;
        shape.line(bounds.x, bounds.y + length, bounds.x, bounds.y);
        shape.line(bounds.x, bounds.y, bounds.x + length, bounds.y);
        shape.line(bounds.x + bounds.width - length, bounds.y, bounds.x + bounds.width, bounds.y);
        shape.line(bounds.x + bounds.width, bounds.y, bounds.x + bounds.width, bounds.y + length);
        shape.line(bounds.x, bounds.y + bounds.height - length, bounds.x, bounds.y + bounds.height);
        shape.line(bounds.x, bounds.y + bounds.height, bounds.x + length, bounds.y + bounds.height);
        shape.line(bounds.x + bounds.width - length, bounds.y + bounds.height, bounds.x + bounds.width,
            bounds.y + bounds.height);
        shape.line(bounds.x + bounds.width, bounds.y + bounds.height, bounds.x + bounds.width,
            bounds.y + bounds.height - length);
    }

    private void drawText() {
        batch.begin();
        fonts.small.setColor(0.64f, 0.48f, 0.82f, 1f);
        fonts.small.draw(batch, "ARCANA GUIDE", PANEL.x + 60f, PANEL.y + PANEL.height - 55f);

        fonts.title.setColor(0.95f, 0.9f, 1f, 1f);
        fonts.title.draw(batch, TITLES[page], PANEL.x + 60f, PANEL.y + PANEL.height -  90f);

        fonts.normal.setColor(0.72f, 0.68f, 0.78f, 1f);
        fonts.normal.draw(batch, SUBTITLES[page], PANEL.x + 62f, PANEL.y + PANEL.height - 165f);

        float bulletY = PANEL.y + PANEL.height - 285f;
        for (String bullet : BULLETS[page]) {
            fonts.normal.setColor(0.72f, 0.4f, 0.94f, 1f);
            fonts.normal.draw(batch, "◆", PANEL.x + 72f, bulletY);
            fonts.normal.setColor(0.92f, 0.9f, 0.96f, 1f);
            fonts.normal.draw(batch, bullet, PANEL.x + 110f, bulletY, 610f, Align.left, true);
            bulletY -= 120f;
        }

        fonts.title.setColor(0.52f, 0.28f, 0.72f, 0.52f);
        drawCentered(fonts.title, String.format("%02d", page + 1), PANEL.x + 765f, PANEL.y + 500f, 355f);
        fonts.normal.setColor(0.74f, 0.67f, 0.82f, 0.8f);
        drawCentered(fonts.normal, TITLES[page], PANEL.x + 765f, PANEL.y + 390f, 355f);
        fonts.small.setColor(0.5f, 0.45f, 0.58f, 0.9f);
        drawCentered(fonts.small, (page + 1) + " / " + TITLES.length, PANEL.x + 765f, PANEL.y + 350f, 355f);

        fonts.normal.setColor(page > 0 ? Color.WHITE : new Color(0.4f, 0.38f, 0.45f, 1f));
        drawCentered(fonts.normal, "이전", PREVIOUS.x, PREVIOUS.y + 40f, PREVIOUS.width);
        fonts.normal.setColor(1f, 0.88f, 0.55f, 1f);
        drawCentered(fonts.normal, page == TITLES.length - 1 ? "덱 선택" : "다음", NEXT.x, NEXT.y + 40f, NEXT.width);
        batch.end();
    }

    private void drawCentered(BitmapFont font, String text, float x, float y, float width) {
        layout.setText(font, text);
        font.draw(batch, text, x + (width - layout.width) / 2f, y);
    }

    private void updatePointer() {
        pointer.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        viewport.unproject(pointer);
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shape != null) shape.dispose();
        if (fonts != null) fonts.dispose();
        batch = null;
        shape = null;
        fonts = null;
    }
}
