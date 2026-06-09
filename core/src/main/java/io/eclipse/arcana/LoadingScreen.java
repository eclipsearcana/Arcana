package io.eclipse.arcana;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class LoadingScreen implements Screen {
    private static final float WORLD_W = 1600f;
    private static final float WORLD_H = 1000f;
    private static final float TITLE_FADE_SPEED = 0.75f;
    private static final float LOGO_FADE_SPEED = 1.5f;
    private static final float BAR_FADE_SPEED = 2.4f;

    private static final float LOGO_W = 820f;
    private static final float LOGO_CENTER_X = WORLD_W / 2f;
    private static final float LOGO_CENTER_Y = 680f;

    private static final float BAR_W = 980f;
    private static final float BAR_H = BAR_W * LoadingBar.SRC_H / LoadingBar.SRC_W;
    private static final float BAR_X = (WORLD_W - BAR_W) / 2f;
    private static final float BAR_Y = 196f;
    private static final float MENU_FADE_SPEED = 1.8f;
    private static final float BAR_FADE_OUT_SPEED = 2.8f;
    private static final float TRANSITION_SPEED = 1.2f;
    private static final float FLOAT_AMOUNT = 6f;
    private static final float FLOAT_SPEED = 0.8f;
    private static final int RAY_COUNT = 6;
    private static final float VIGNETTE_MIN = 0.15f;
    private static final float VIGNETTE_MAX = 0.35f;
    private static final float VIGNETTE_SPEED = 0.6f;

    private final Core game;

    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shape;
    private Stage stage;
    private LoadingBar loadingBar;

    private final Vector3 touchPos = new Vector3();
    private final Vector3 mousePos = new Vector3();

    private float startScale = 1f;
    private float settingsScale = 1f;
    private float exitScale = 1f;

    private static final float NORMAL_SCALE = 1f;
    private static final float HOVER_SCALE = 1.12f;
    private static final float SCALE_SPEED = 10f;

    private Rectangle startBounds;
    private Rectangle settingsBounds;
    private Rectangle exitBounds;

    private boolean isBright;
    private String folder;
    private String suffix;
    private float titleAlpha;
    private float logoAlpha;
    private float barAlpha;
    private float menuAlpha;
    private float transitionAlpha;
    private float minShowTime;
    private float floatTime;
    private float vignetteTime;
    private final float[] rayX = new float[RAY_COUNT];
    private final float[] rayAlpha = new float[RAY_COUNT];
    private final float[] rayWidth = new float[RAY_COUNT];
    private final float[] raySpeed = new float[RAY_COUNT];
    private boolean assetsFinished;
    private boolean loaded;
    private boolean transitioning;
    private boolean transitionComplete;
    private boolean disposed;
    private final Boolean fixedTitleVariant;

    public LoadingScreen(Core game) {
        this(game, null);
    }

    public LoadingScreen(Core game, Boolean fixedTitleVariant) {
        this.game = game;
        this.fixedTitleVariant = fixedTitleVariant;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        stage = new Stage(viewport, batch);
        loadingBar = new LoadingBar();
        loadingBar.setSize(BAR_W, BAR_H);
        loadingBar.setPosition(BAR_X, BAR_Y);
        loadingBar.setSmoothingSpeed(6f);
        loadingBar.setDrawEdgeGlow(true);
        stage.addActor(loadingBar);

        isBright = fixedTitleVariant != null ? fixedTitleVariant : MathUtils.random(0, 1) == 0;
        folder = isBright ? "TitleA/" : "TitleB/";
        suffix = isBright ? "A" : "B";

        for (int i = 0; i < RAY_COUNT; i++) {
            resetRay(i);
            rayAlpha[i] = MathUtils.random(0f, 1f);
        }

        game.assets.queueAll();
    }

    @Override
    public void render(float delta) {
        minShowTime += delta;
        if (!assetsFinished) assetsFinished = game.assets.updateStep();
        float progress = assetsFinished ? 1f : game.assets.progress();
        loadingBar.setProgress(progress);
        loadingBar.getColor().a = barAlpha;
        stage.act(delta);

        if (!loaded && assetsFinished && minShowTime >= 0.6f
            && loadingBar.getDisplayedProgress() >= 0.999f) {
            loaded = true;
            setupMenuBounds();
        }

        updateFade(delta);
        handleInput(delta);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shape.setProjectionMatrix(camera.combined);

        batch.begin();
        drawTitleBackground();
        drawLogo();
        drawMenu();
        batch.end();

        if (barAlpha > 0.01f) stage.draw();

        drawTitleEffects(delta);
        drawFadeToBlackOverlay();
        drawTransitionOverlay();

        if (transitionComplete) {
            game.showTutorial();
            dispose();
        }
    }

    private void updateFade(float delta) {
        if (titleTexture("title") != null) {
            titleAlpha = Math.min(1f, titleAlpha + TITLE_FADE_SPEED * delta);
        }
        if (titleTexture("logo") != null) {
            logoAlpha = Math.min(1f, logoAlpha + LOGO_FADE_SPEED * delta);
        }
        if (!loaded) {
            barAlpha = Math.min(1f, barAlpha + BAR_FADE_SPEED * delta);
        }
        if (loaded) {
            barAlpha = Math.max(0f, barAlpha - BAR_FADE_OUT_SPEED * delta);
            if (barAlpha < 0.75f) {
                menuAlpha = Math.min(1f, menuAlpha + MENU_FADE_SPEED * delta);
            }
        }
        if (transitioning) {
            transitionAlpha = Math.min(1f, transitionAlpha + TRANSITION_SPEED * delta);
            if (transitionAlpha >= 1f) {
                transitionComplete = true;
            }
        }
        if (loaded) {
            updateButtonScales(delta);
        }
    }

    private void handleInput(float delta) {
        if (!loaded || transitioning || menuAlpha < 0.95f || !Gdx.input.justTouched()) {
            return;
        }

        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        viewport.unproject(touchPos);

        if (startBounds.contains(touchPos.x, touchPos.y)) {
            transitioning = true;
        } else if (exitBounds.contains(touchPos.x, touchPos.y)) {
            Gdx.app.exit();
        }
    }

    private void drawTitleBackground() {
        Texture title = titleTexture("title");
        if (title == null) return;

        floatTime += Gdx.graphics.getDeltaTime();
        float offsetY = MathUtils.sin(floatTime * FLOAT_SPEED) * FLOAT_AMOUNT;
        float oversize = FLOAT_AMOUNT * 2f;

        batch.setColor(1f, 1f, 1f, titleAlpha);
        batch.draw(title, 0f, offsetY - oversize / 2f, WORLD_W, WORLD_H + oversize);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawMenu() {
        if (!loaded || menuAlpha <= 0f) return;

        Texture start = titleTexture("start");
        Texture settings = titleTexture("settings");
        Texture exit = titleTexture("exit");
        Texture star = titleTexture("star");
        if (start == null || settings == null || exit == null || star == null) return;

        batch.setColor(1f, 1f, 1f, menuAlpha);
        drawScaled(start, startBounds, startScale);
        drawCenteredNative(star, 800f, 365f);
        drawScaled(settings, settingsBounds, settingsScale);
        drawCenteredNative(star, 800f, 260f);
        drawScaled(exit, exitBounds, exitScale);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawLogo() {
        Texture logo = titleTexture("logo");
        if (logo == null) return;

        float logoH = LOGO_W * logo.getHeight() / logo.getWidth();
        float logoX = LOGO_CENTER_X - LOGO_W / 2f;
        float logoY = LOGO_CENTER_Y - logoH / 2f;

        batch.setColor(1f, 1f, 1f, logoAlpha);
        batch.draw(logo, logoX, logoY, LOGO_W, logoH);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawFadeToBlackOverlay() {
        float blackAlpha = 1f - Math.max(titleAlpha, logoAlpha * 0.72f);
        if (blackAlpha <= 0.01f) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, blackAlpha);
        shape.rect(0f, 0f, WORLD_W, WORLD_H);
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawTransitionOverlay() {
        if (transitionAlpha <= 0f) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, transitionAlpha);
        shape.rect(0f, 0f, WORLD_W, WORLD_H);
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawTitleEffects(float delta) {
        float effectAlpha = Math.max(titleAlpha, menuAlpha);
        if (effectAlpha <= 0.01f) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.setProjectionMatrix(camera.combined);

        if (isBright) {
            drawLightRays(delta, effectAlpha);
        } else {
            drawVignette(delta, effectAlpha);
        }

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void resetRay(int i) {
        rayX[i] = MathUtils.random(100f, 1500f);
        rayWidth[i] = MathUtils.random(60f, 180f);
        rayAlpha[i] = 0f;
        raySpeed[i] = MathUtils.random(0.15f, 0.35f);
    }

    private void drawLightRays(float delta, float effectAlpha) {
        for (int i = 0; i < RAY_COUNT; i++) {
            rayAlpha[i] += raySpeed[i] * delta;
            if (rayAlpha[i] > 2f) {
                resetRay(i);
            }
        }

        shape.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < RAY_COUNT; i++) {
            float alpha = 1f - Math.abs(rayAlpha[i] - 1f);
            alpha = Math.max(0f, alpha) * 0.18f * effectAlpha;

            shape.setColor(1f, 0.97f, 0.88f, alpha);
            shape.triangle(
                rayX[i], WORLD_H,
                rayX[i] - rayWidth[i], 0f,
                rayX[i] + rayWidth[i], 0f
            );
        }
        shape.end();
    }

    private void drawVignette(float delta, float effectAlpha) {
        vignetteTime += delta;

        float vignetteAlpha = VIGNETTE_MIN + (VIGNETTE_MAX - VIGNETTE_MIN)
            * (0.5f + 0.5f * MathUtils.sin(vignetteTime * VIGNETTE_SPEED));
        vignetteAlpha *= effectAlpha;

        com.badlogic.gdx.graphics.Color edge =
            new com.badlogic.gdx.graphics.Color(0.05f, 0.02f, 0.1f, vignetteAlpha);
        com.badlogic.gdx.graphics.Color clear =
            new com.badlogic.gdx.graphics.Color(0.05f, 0.02f, 0.1f, 0f);

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.rect(0f, 0f, 200f, WORLD_H, edge, clear, clear, edge);
        shape.rect(WORLD_W - 200f, 0f, 200f, WORLD_H, clear, edge, edge, clear);
        shape.rect(0f, WORLD_H - 150f, WORLD_W, 150f, clear, clear, edge, edge);
        shape.rect(0f, 0f, WORLD_W, 150f, edge, edge, clear, clear);
        shape.end();
    }

    private void setupMenuBounds() {
        startBounds = centeredNative(titleTexture("start"), 800f, 415f);
        settingsBounds = centeredNative(titleTexture("settings"), 800f, 305f);
        exitBounds = centeredNative(titleTexture("exit"), 800f, 200f);
    }

    private Rectangle centeredNative(Texture texture, float centerX, float centerY) {
        return new Rectangle(
            centerX - texture.getWidth() / 2f,
            centerY - texture.getHeight() / 2f,
            texture.getWidth(),
            texture.getHeight()
        );
    }

    private void drawCenteredNative(Texture texture, float centerX, float centerY) {
        drawTexture(texture, centeredNative(texture, centerX, centerY));
    }

    private void drawTexture(Texture texture, Rectangle bounds) {
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    private void drawScaled(Texture texture, Rectangle bounds, float scale) {
        float width = bounds.width * scale;
        float height = bounds.height * scale;
        float x = bounds.x + bounds.width / 2f - width / 2f;
        float y = bounds.y + bounds.height / 2f - height / 2f;
        batch.draw(texture, x, y, width, height);
    }

    private void updateButtonScales(float delta) {
        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        viewport.unproject(mousePos);

        float lerp = Math.min(1f, delta * SCALE_SPEED);
        startScale = MathUtils.lerp(
            startScale,
            startBounds.contains(mousePos.x, mousePos.y) ? HOVER_SCALE : NORMAL_SCALE,
            lerp
        );
        settingsScale = MathUtils.lerp(
            settingsScale,
            settingsBounds.contains(mousePos.x, mousePos.y) ? HOVER_SCALE : NORMAL_SCALE,
            lerp
        );
        exitScale = MathUtils.lerp(
            exitScale,
            exitBounds.contains(mousePos.x, mousePos.y) ? HOVER_SCALE : NORMAL_SCALE,
            lerp
        );
    }

    private Texture titleTexture(String name) {
        return game.assets.titleTexture(folder, name, suffix);
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
        if (disposed) return;
        disposed = true;
        loadingBar.dispose();
        stage.dispose();
        batch.dispose();
        shape.dispose();
    }
}
