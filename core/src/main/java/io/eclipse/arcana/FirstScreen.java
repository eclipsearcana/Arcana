package io.eclipse.arcana;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class FirstScreen implements Screen {

    private final Core game;

    private SpriteBatch batch;
    private ShapeRenderer shape;
    private Texture titleTexture;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private BitmapFont font;
    private GlyphLayout layout;

    private boolean isBright;

    // 페이드인
    private float alpha = 0f;
    private static final float FADE_SPEED = 0.5f;

    // 부유 효과
    private float floatTime = 0f;
    private static final float FLOAT_AMOUNT = 6f;
    private static final float FLOAT_SPEED = 0.8f;

    // 햇빛 줄기 (TitleA)
    private static final int RAY_COUNT = 6;
    private float[] rayX = new float[RAY_COUNT];
    private float[] rayAlpha = new float[RAY_COUNT];
    private float[] rayWidth = new float[RAY_COUNT];
    private float[] raySpeed = new float[RAY_COUNT];

    // 비네트 pulse (TitleB)
    private float vignetteTime = 0f;
    private static final float VIGNETTE_MIN = 0.15f;
    private static final float VIGNETTE_MAX = 0.35f;
    private static final float VIGNETTE_SPEED = 0.6f;

    // "Press any key" 텍스트 깜빡임
    private float blinkTime = 0f;
    private static final float BLINK_SPEED = 1.5f;

    // 화면 전환
    private boolean transitioning = false;
    private float transitionAlpha = 0f;
    private static final float TRANSITION_SPEED = 1.2f;

    public FirstScreen(Core game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(2f);
        layout = new GlyphLayout();
        camera = new OrthographicCamera();
        viewport = new FitViewport(1600f, 1000f, camera);

        String[] titles = {"titleA.png", "titleB.png"};
        int pick = (int)(Math.random() * titles.length);
        isBright = (pick == 0);
        titleTexture = new Texture(Gdx.files.internal(titles[pick]));
        titleTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        for (int i = 0; i < RAY_COUNT; i++) {
            resetRay(i);
            rayAlpha[i] = MathUtils.random(0f, 1f);
        }
    }

    private void resetRay(int i) {
        rayX[i] = MathUtils.random(100f, 1500f);
        rayWidth[i] = MathUtils.random(60f, 180f);
        rayAlpha[i] = 0f;
        raySpeed[i] = MathUtils.random(0.15f, 0.35f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 화면 전환 처리
        if (transitioning) {
            transitionAlpha = Math.min(1f, transitionAlpha + TRANSITION_SPEED * delta);
            if (transitionAlpha >= 1f) {
                game.setScreen(new MainScreen(game));
                return;
            }
        } else {
            // 키 입력 감지 (페이드인 끝난 후에만)
            if (alpha >= 1f && (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY))) {
                transitioning = true;
            }
        }

        // 페이드인
        if (alpha < 1f) alpha = Math.min(1f, alpha + FADE_SPEED * delta);

        // 부유
        floatTime += delta;
        float offsetY = MathUtils.sin(floatTime * FLOAT_SPEED) * FLOAT_AMOUNT;
        float oversize = FLOAT_AMOUNT * 2f;

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 배경
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(titleTexture, 0f, offsetY - oversize / 2f, 1600f, 1000f + oversize);

        // "Press any key" 텍스트 렌더링
        if (!transitioning) {
            blinkTime += delta;
            float blinkAlpha = 0.5f + 0.5f * MathUtils.sin(blinkTime * BLINK_SPEED * MathUtils.PI);
            blinkAlpha *= alpha;
            font.setColor(1f, 1f, 1f, blinkAlpha);
            layout.setText(font, "Press any key");
            font.draw(batch, "Press any key",
                (1600f - layout.width) / 2f,
                120f);
        }

        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();

        // 애니메이션 오버레이
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.setProjectionMatrix(camera.combined);

        if (isBright) {
            for (int i = 0; i < RAY_COUNT; i++) {
                rayAlpha[i] += raySpeed[i] * delta;
                if (rayAlpha[i] > 2f) resetRay(i);
            }
            shape.begin(ShapeRenderer.ShapeType.Filled);
            for (int i = 0; i < RAY_COUNT; i++) {
                float a = 1f - Math.abs(rayAlpha[i] - 1f);
                a = Math.max(0f, a) * 0.18f * alpha;
                shape.setColor(1f, 0.97f, 0.88f, a);
                shape.triangle(rayX[i], 1000f, rayX[i] - rayWidth[i], 0f, rayX[i] + rayWidth[i], 0f);
            }
            shape.end();
        } else {
            vignetteTime += delta;
            float vigAlpha = VIGNETTE_MIN + (VIGNETTE_MAX - VIGNETTE_MIN)
                * (0.5f + 0.5f * MathUtils.sin(vignetteTime * VIGNETTE_SPEED));
            vigAlpha *= alpha;

            com.badlogic.gdx.graphics.Color edge = new com.badlogic.gdx.graphics.Color(0.05f, 0.02f, 0.1f, vigAlpha);
            com.badlogic.gdx.graphics.Color clear = new com.badlogic.gdx.graphics.Color(0.05f, 0.02f, 0.1f, 0f);

            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.rect(0f, 0f, 200f, 1000f, edge, clear, clear, edge);
            shape.rect(1400f, 0f, 200f, 1000f, clear, edge, edge, clear);
            shape.rect(0f, 850f, 1600f, 150f, clear, clear, edge, edge);
            shape.rect(0f, 0f, 1600f, 150f, edge, edge, clear, clear);
            shape.end();
        }

        // 전환 페이드아웃
        if (transitioning) {
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(0f, 0f, 0f, transitionAlpha);
            shape.rect(0f, 0f, 1600f, 1000f);
            shape.end();
        }

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

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
        titleTexture.dispose();
        font.dispose();

    }
}
