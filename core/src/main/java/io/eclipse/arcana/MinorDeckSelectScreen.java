package io.eclipse.arcana;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.eclipse.arcana.model.Suit;

public class MinorDeckSelectScreen implements Screen {
    private static final float WORLD_W = 1600f;
    private static final float WORLD_H = 1000f;
    private static final float FRAME_X = 7f;
    private static final float FRAME_Y = 4f;
    private static final float FRAME_W = 1586f;
    private static final float FRAME_H = 992f;
    private static final float CARD_W = 260f;
    private static final float CARD_H = 650f;
    private static final float CARD_Y = 175f;
    private static final float HOVER_SCALE = 1.045f;
    private static final float TRANSITION_SPEED = 1.8f;

    // Matches the order drawn in choose/background_choose.png.
    private static final Suit[] SUITS = {
        Suit.WANDS, Suit.SWORDS, Suit.PENTACLES, Suit.CUPS
    };
    private static final String[] IMAGE_NAMES = {
        "wands", "swords", "pentacles", "cups"
    };
    private static final float[] CARD_CENTERS = {
        FRAME_X + 292f, FRAME_X + 602f, FRAME_X + 912f, FRAME_X + 1222f
    };

    private final Core game;
    private final Rectangle[] panelBounds = new Rectangle[SUITS.length];
    private final float[] hoverAnim = new float[SUITS.length];
    private final Vector2 pointer = new Vector2();

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private ArcanaAssets assets;
    private Texture frame;
    private final Texture[] illustrations = new Texture[SUITS.length];
    private final Texture[] illustrationsBack = new Texture[SUITS.length];
    private final Texture[] suitLabels = new Texture[SUITS.length];
    private final Texture[] features = new Texture[SUITS.length];
    private final float[] flipAngle = new float[SUITS.length];

    private float fadeAlpha;
    private int hoveredIndex = -1;
    private int selectedIndex = -1;
    private float transitionAlpha;
    private boolean disposed;

    private Texture titleTexture;

    public MinorDeckSelectScreen(Core game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        batch = new SpriteBatch();
        assets = game.assets;
        frame = requireChooseTexture("background_choose");

        for (int i = 0; i < SUITS.length; i++) {
            illustrations[i] = requireChooseTexture(IMAGE_NAMES[i]);
            illustrationsBack[i] = requireChooseTexture(IMAGE_NAMES[i] + "_back");
            suitLabels[i] = requireChooseTexture(IMAGE_NAMES[i].toUpperCase() + "_transparent");
            features[i] = requireChooseTexture(IMAGE_NAMES[i].toUpperCase() + "_features");
            panelBounds[i] = new Rectangle(CARD_CENTERS[i] - CARD_W / 2f, CARD_Y, CARD_W, CARD_H);
            flipAngle[i] = 0f;
        }

        titleTexture = requireChooseTexture("Title_Choose");
    }

    private Texture requireChooseTexture(String name) {
        Texture texture = assets.chooseTexture(name);
        if (texture == null) {
            throw new com.badlogic.gdx.utils.GdxRuntimeException(
                "Choose screen asset was not loaded: choose/" + name + ".png");
        }
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    @Override
    public void render(float delta) {
        fadeAlpha = Math.min(1f, fadeAlpha + delta * 2.2f);
        updatePointer();
        updateHover(delta);
        handleInput();

        if (selectedIndex >= 0) {
            transitionAlpha = Math.min(1f, transitionAlpha + delta * TRANSITION_SPEED);
        }

        Gdx.gl.glClearColor(0.005f, 0.004f, 0.008f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        drawFrame();
        drawIllustrations();
        drawTransition();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        if (transitionAlpha >= 1f) {
            Suit suit = SUITS[selectedIndex];
            game.showDraft(suit);
            dispose();
        }
    }

    private void updatePointer() {
        pointer.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(pointer);
    }

    private void updateHover(float delta) {
        hoveredIndex = -1;
        if (selectedIndex < 0) {
            for (int i = 0; i < panelBounds.length; i++) {
                if (panelBounds[i].contains(pointer)) {
                    hoveredIndex = i;
                    break;
                }
            }
        }

        for (int i = 0; i < hoverAnim.length; i++) {
            float target = i == hoveredIndex || i == selectedIndex ? 1f : 0f;
            hoverAnim[i] += (target - hoverAnim[i]) * Math.min(1f, delta * 8f);

            float targetAngle = i == hoveredIndex || i == selectedIndex ? 180f : 0f;
            flipAngle[i] = MathUtils.lerp(flipAngle[i], targetAngle, Math.min(1f, delta * 8f));
        }
    }

    private void handleInput() {
        if (selectedIndex >= 0 || fadeAlpha < 0.85f || !Gdx.input.justTouched()) return;
        if (hoveredIndex >= 0) selectedIndex = hoveredIndex;
    }

    private void drawIllustrations() {
        batch.begin();

        batch.setColor(1f, 1f, 1f, fadeAlpha);
        float tw = 388f;
        float th = 140f;
        batch.draw(titleTexture, (WORLD_W - tw) / 2f, 835f, tw, th);
        batch.setColor(Color.WHITE);

        for (int i = 0; i < illustrations.length; i++) {
            Rectangle panel = panelBounds[i];
            float hover = Interpolation.smoother.apply(hoverAnim[i]);
            float scale = 1f + (HOVER_SCALE - 1f) * hover;

            float angle = flipAngle[i];
            float cos = MathUtils.cosDeg(angle);
            float widthScale = Math.abs(cos);

            // Native card aspect ratio: 793 / 1983 = 0.4
            float cardAspect = 793f / 1983f;
            float drawH = panel.height * scale;
            float drawW = drawH * cardAspect * widthScale;

            float drawX = panel.x + panel.width / 2f - drawW / 2f;
            float drawY = panel.y + panel.height / 2f - drawH / 2f;

            Texture texture = (angle < 90f) ? illustrationsBack[i] : illustrations[i];

            // 1) Draw 3D shadow (black tinted texture offset to bottom-right)
            float shadowOffset = (6f + 10f * hover) * scale;
            batch.setColor(0f, 0f, 0f, 0.35f * fadeAlpha);
            batch.draw(
                texture,
                drawX + shadowOffset, drawY - shadowOffset,
                drawW, drawH
            );

            // 2) Draw card front/back with hover tint
            float muted = 0.28f + hover * 0.72f;
            float blue = 0.34f + hover * 0.66f;
            batch.setColor(muted, muted, blue, fadeAlpha);
            batch.draw(
                texture,
                drawX, drawY,
                drawW, drawH
            );

            // 3) Draw suit label below the card (centered in the bottom space)
            Texture label = suitLabels[i];
            float lblH = 60f;
            float lblW = lblH * 3f; // 3:1 aspect ratio
            float lblX = CARD_CENTERS[i] - lblW / 2f;
            float lblY = 115f; // shifted up slightly to make room for features
            batch.draw(
                label,
                lblX, lblY,
                lblW, lblH
            );

            // 4) Draw features description under each card (small, centered in the bottom space)
            Texture feat = features[i];
            float featW = 280f;
            float featH = featW * 220f / 1600f; // 1600x220 aspect ratio
            float featX = CARD_CENTERS[i] - featW / 2f;
            float featY = 65f; // centered between Y=54 and Y=115
            batch.draw(
                feat,
                featX, featY,
                featW, featH
            );
        }
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void drawFrame() {
        batch.begin();
        batch.setColor(1f, 1f, 1f, fadeAlpha);
        batch.draw(frame, FRAME_X, FRAME_Y, FRAME_W, FRAME_H);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void drawTransition() {
        if (selectedIndex < 0) return;
        batch.begin();
        float fade = MathUtils.clamp((transitionAlpha - 0.45f) / 0.55f, 0f, 1f);
        batch.setColor(0f, 0f, 0f, fade);
        batch.draw(frame, 0f, 0f, WORLD_W, WORLD_H);
        batch.setColor(Color.WHITE);
        batch.end();
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
        batch.dispose();
    }
}
