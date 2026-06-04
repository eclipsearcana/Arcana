package io.eclipse.arcana;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class FirstScreen implements Screen {

    private final Core game;

    private SpriteBatch batch;
    private ShapeRenderer shape;
    private OrthographicCamera camera;
    private FitViewport viewport;

    private Texture titleTexture;
    private Texture logoTexture;
    private Texture startTexture;
    private Texture settingsTexture;
    private Texture exitTexture;
    private Texture starTexture;

    // 버튼 클릭 영역
    private Rectangle startBounds;
    private Rectangle settingsBounds;
    private Rectangle exitBounds;

    private final Vector3 touchPos = new Vector3();

    // true면 TitleA, false면 TitleB
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
        camera = new OrthographicCamera();
        viewport = new FitViewport(1600f, 1000f, camera);

        // TitleA와 TitleB 중 하나를 1/2 확률로 선택
        int pick = MathUtils.random(0, 1);
        isBright = (pick == 0);

        // 선택된 타이틀에 맞는 폴더와 파일 접미사 결정
        String folder = isBright ? "TitleA/" : "TitleB/";
        String suffix = isBright ? "A" : "B";

        // 선택된 디자인에 맞는 이미지 로드
        titleTexture = loadTexture(folder + "title" + suffix + ".png");
        logoTexture = loadTexture(folder + "logo" + suffix + ".png");
        startTexture = loadTexture(folder + "start" + suffix + ".png");
        settingsTexture = loadTexture(folder + "settings" + suffix + ".png");
        exitTexture = loadTexture(folder + "exit" + suffix + ".png");
        starTexture = loadTexture(folder + "star" + suffix + ".png");

        // 이미지를 확대/축소했을 때 부드럽게 보이도록 필터 적용
        setLinearFilter(titleTexture);
        setLinearFilter(logoTexture);
        setLinearFilter(startTexture);
        setLinearFilter(settingsTexture);
        setLinearFilter(exitTexture);
        setLinearFilter(starTexture);

        // 버튼 이미지가 그려질 위치와 클릭 가능한 영역
        startBounds = new Rectangle(725f, 382.5f, 150f, 65f);
        settingsBounds = new Rectangle(660f, 267.5f, 280f, 75f);
        exitBounds = new Rectangle(730f, 167.5f, 140f, 65f);

        for (int i = 0; i < RAY_COUNT; i++) {
            resetRay(i);
            rayAlpha[i] = MathUtils.random(0f, 1f);
        }
    }

    private void setLinearFilter(Texture texture) {
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    private Texture loadTexture(String path) {
        FileHandle file = ArcanaFiles.asset(path);
        if (!file.exists()) {
            throw new com.badlogic.gdx.utils.GdxRuntimeException("Missing title asset: " + path);
        }
        return new Texture(file);
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
                game.setScreen(new MinorDeckSelectScreen(game));
                return;
            }
        } else {
            handleInput();
        }

        if (alpha < 1f) {
            alpha = Math.min(1f, alpha + FADE_SPEED * delta);
        }

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

        // 로고와 메뉴 버튼 그리기
        if (!transitioning) {
            drawMenu();
        }

        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();

        // 애니메이션 오버레이
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.setProjectionMatrix(camera.combined);

        // TitleA면 빛줄기, TitleB면 어두운 비네트 효과
        if (isBright) {
            drawLightRays(delta);
        } else {
            drawVignette(delta);
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

    // 마우스 클릭 또는 터치 입력 처리
    private void handleInput() {
        // 페이드인이 끝나기 전에는 버튼 입력을 받지 않음
        if (alpha < 1f || !Gdx.input.justTouched()) {
            return;
        }

        // 화면 좌표를 1600 x 1000 기준 월드 좌표로 변환
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        viewport.unproject(touchPos);

        // start 버튼을 누르면 MainScreen으로 넘어가기 위한 페이드아웃 시작
        if (startBounds.contains(touchPos.x, touchPos.y)) {
            transitioning = true;
        }

        /* settings는 비워둠
        if (settingsBounds.contains(touchPos.x, touchPos.y)) {
            game.setScreen(new SettingsScreen(game));
        } */

        // exit 버튼을 누르면 게임 종료
        else if (exitBounds.contains(touchPos.x, touchPos.y)) {
            Gdx.app.exit();
        }
    }


    // 로고, start, settings, exit, 별 장식을 그림
    private void drawMenu() {
        batch.setColor(1f, 1f, 1f, alpha);

        // 로고
        batch.draw(logoTexture, 390f, 535f, 820f, 285f);

        // start
        batch.draw(startTexture, startBounds.x, startBounds.y, startBounds.width, startBounds.height);

        batch.draw(starTexture, 727.5f, 346f, 145f, 38f);

        // settings
        batch.draw(settingsTexture, settingsBounds.x, settingsBounds.y, settingsBounds.width, settingsBounds.height);

        batch.draw(starTexture, 727.5f, 241f, 145f, 38f);

        // exit
        batch.draw(exitTexture, exitBounds.x, exitBounds.y, exitBounds.width, exitBounds.height);
    }

    // TitleA에서 사용하는 햇빛 줄기 효과
    private void drawLightRays(float delta) {
        // 각 빛줄기의 알파 진행도 증가
        for (int i = 0; i < RAY_COUNT; i++) {
            rayAlpha[i] += raySpeed[i] * delta;

            // 한 번 나타났다 사라지면 새 위치에서 다시 시작
            if (rayAlpha[i] > 2f) {
                resetRay(i);
            }
        }

        shape.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < RAY_COUNT; i++) {
            // rayAlpha가 1에 가까울수록 선명하고, 0이나 2에 가까우면 투명
            float a = 1f - Math.abs(rayAlpha[i] - 1f);
            a = Math.max(0f, a) * 0.18f * alpha;

            shape.setColor(1f, 0.97f, 0.88f, a);

            // 위에서 아래로 퍼지는 삼각형을 빛줄기처럼 그림
            shape.triangle(
                rayX[i], 1000f,
                rayX[i] - rayWidth[i], 0f,
                rayX[i] + rayWidth[i], 0f
            );
        }

        shape.end();
    }

    // TitleB에서 사용하는 가장자리 어둡게 만드는 효과
    private void drawVignette(float delta) {
        vignetteTime += delta;

        // 비네트가 은은하게 진해졌다 약해졌다 하도록 알파값 변화
        float vigAlpha = VIGNETTE_MIN + (VIGNETTE_MAX - VIGNETTE_MIN)
            * (0.5f + 0.5f * MathUtils.sin(vignetteTime * VIGNETTE_SPEED));
        vigAlpha *= alpha;

        com.badlogic.gdx.graphics.Color edge =
            new com.badlogic.gdx.graphics.Color(0.05f, 0.02f, 0.1f, vigAlpha);
        com.badlogic.gdx.graphics.Color clear =
            new com.badlogic.gdx.graphics.Color(0.05f, 0.02f, 0.1f, 0f);

        shape.begin(ShapeRenderer.ShapeType.Filled);

        // 왼쪽, 오른쪽, 위쪽, 아래쪽에 그라데이션 사각형을 그려 가장자리를 어둡게 만듦
        shape.rect(0f, 0f, 200f, 1000f, edge, clear, clear, edge);
        shape.rect(1400f, 0f, 200f, 1000f, clear, edge, edge, clear);
        shape.rect(0f, 850f, 1600f, 150f, clear, clear, edge, edge);
        shape.rect(0f, 0f, 1600f, 150f, edge, edge, clear, clear);

        shape.end();
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
        logoTexture.dispose();
        startTexture.dispose();
        settingsTexture.dispose();
        exitTexture.dispose();
        starTexture.dispose();

    }
}
