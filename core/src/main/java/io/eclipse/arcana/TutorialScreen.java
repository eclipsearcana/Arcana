package io.eclipse.arcana;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class TutorialScreen implements Screen {
    private static final float WORLD_W = 1600f;
    private static final float WORLD_H = 1000f;
    private static final Rectangle CONTINUE = new Rectangle(650f, 110f, 300f, 74f);

    private final Core game;
    private final Vector3 pointer = new Vector3();
    private final GlyphLayout layout = new GlyphLayout();
    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shape;
    private FontManager fonts;

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
        if (Gdx.input.justTouched() && CONTINUE.contains(pointer.x, pointer.y)) {
            game.showDeckSelect();
            dispose();
            return;
        }

        Gdx.gl.glClearColor(0.015f, 0.018f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        Texture background = game.assets.background();
        if (background != null) {
            batch.setColor(0.35f, 0.35f, 0.45f, 1f);
            batch.draw(background, 0f, 0f, WORLD_W, WORLD_H);
            batch.setColor(Color.WHITE);
        }
        batch.end();

        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.015f, 0.012f, 0.05f, 0.92f);
        shape.rect(250f, 210f, 1100f, 620f);
        shape.setColor(CONTINUE.contains(pointer.x, pointer.y)
            ? new Color(0.48f, 0.18f, 0.72f, 1f)
            : new Color(0.24f, 0.09f, 0.42f, 1f));
        shape.rect(CONTINUE.x, CONTINUE.y, CONTINUE.width, CONTINUE.height);
        shape.end();

        batch.begin();
        fonts.title.setColor(0.92f, 0.78f, 1f, 1f);
        drawCentered(fonts.title, "TUTORIAL", 750f);
        fonts.normal.setColor(Color.WHITE);
        drawCentered(fonts.normal, "Learn the rules, card effects, and turn flow here.", 590f);
        drawCentered(fonts.normal, "This screen is ready for tutorial pages and interactions.", 530f);
        fonts.normal.setColor(1f, 0.88f, 0.45f, 1f);
        drawCentered(fonts.normal, "CONTINUE", 158f);
        batch.end();
    }

    private void drawCentered(com.badlogic.gdx.graphics.g2d.BitmapFont font, String text, float y) {
        layout.setText(font, text);
        font.draw(batch, text, (WORLD_W - layout.width) / 2f, y);
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
