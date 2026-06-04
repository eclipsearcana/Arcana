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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.eclipse.arcana.model.Card;
import io.eclipse.arcana.model.CardDefinitions;
import io.eclipse.arcana.model.Suit;
import io.eclipse.arcana.render.CardRenderer;

public class MinorDeckSelectScreen implements Screen {
    private static final float WORLD_W = 1600f;
    private static final float WORLD_H = 1000f;

    private static final float CARD_SCALE = 1.28f;
    private static final float CARD_W = CardRenderer.CARD_W * CARD_SCALE;
    private static final float CARD_H = CardRenderer.CARD_H * CARD_SCALE;
    private static final float CARD_Y = 330f;
    private static final float CARD_GAP = 72f;
    private static final Suit[] SUITS = {
        Suit.WANDS, Suit.CUPS, Suit.SWORDS, Suit.PENTACLES
    };

    private final Core game;
    private final Rectangle[] deckBounds = new Rectangle[SUITS.length];
    private final Vector2 pointer = new Vector2();
    private final GlyphLayout layout = new GlyphLayout();

    private SpriteBatch batch;
    private ShapeRenderer shape;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private FontManager fonts;
    private ArcanaAssets assets;

    private float fadeAlpha = 0f;

    public MinorDeckSelectScreen(Core game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        fonts = new FontManager(Gdx.graphics.getWidth() / WORLD_W);
        assets = new ArcanaAssets();
        assets.finishLoading();

        float totalW = SUITS.length * CARD_W + (SUITS.length - 1) * CARD_GAP;
        float x = (WORLD_W - totalW) / 2f;
        for (int i = 0; i < SUITS.length; i++) {
            deckBounds[i] = new Rectangle(x + i * (CARD_W + CARD_GAP), CARD_Y, CARD_W, CARD_H);
        }
    }

    @Override
    public void render(float delta) {
        fadeAlpha = Math.min(1f, fadeAlpha + delta * 2.5f);
        updatePointer();
        handleInput();

        Gdx.gl.glClearColor(0.015f, 0.012f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shape.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        drawBackground();
        drawCards();
        drawText();
    }

    private void updatePointer() {
        pointer.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(pointer);
    }

    private void handleInput() {
        if (!Gdx.input.justTouched()) return;
        for (int i = 0; i < deckBounds.length; i++) {
            if (deckBounds[i].contains(pointer)) {
                game.startGame(SUITS[i]);
                return;
            }
        }
    }

    private void drawBackground() {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.015f, 0.012f, 0.02f, 1f);
        shape.rect(0f, 0f, WORLD_W, WORLD_H);
        shape.setColor(0.11f, 0.08f, 0.13f, 0.72f * fadeAlpha);
        shape.rect(0f, 0f, WORLD_W, 260f);
        shape.setColor(0.08f, 0.075f, 0.11f, 0.65f * fadeAlpha);
        shape.rect(0f, 760f, WORLD_W, 240f);
        shape.end();
    }

    private void drawCards() {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < SUITS.length; i++) {
            Rectangle rect = deckBounds[i];
            boolean hovered = rect.contains(pointer);
            float lift = hovered ? 24f : 0f;

            shape.setColor(0f, 0f, 0f, 0.34f * fadeAlpha);
            shape.rect(rect.x + 12f, rect.y - 18f, rect.width, 18f);
            shape.setColor(suitColor(SUITS[i], hovered ? 1f : 0.7f));
            shape.rect(rect.x - 5f, rect.y + lift - 5f, rect.width + 10f, rect.height + 10f);
            shape.setColor(0.035f, 0.035f, 0.048f, fadeAlpha);
            shape.rect(rect.x, rect.y + lift, rect.width, rect.height);
        }
        shape.end();

        batch.begin();
        batch.setColor(1f, 1f, 1f, fadeAlpha);
        for (int i = 0; i < SUITS.length; i++) {
            Rectangle rect = deckBounds[i];
            boolean hovered = rect.contains(pointer);
            float lift = hovered ? 24f : 0f;
            Card preview = CardDefinitions.allMinor(SUITS[i]).first();
            Texture illust = assets.cardIllust(preview);
            if (illust != null) {
                batch.draw(illust, rect.x, rect.y + lift, rect.width, rect.height);
            }
        }
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void drawText() {
        batch.begin();
        fonts.title.setColor(0.95f, 0.88f, 0.68f, fadeAlpha);
        drawCentered(fonts.title, "마이너 덱 선택", WORLD_W / 2f, 885f);

        fonts.normal.setColor(0.78f, 0.76f, 0.82f, fadeAlpha);
        drawCentered(fonts.normal, "시작할 마이너 아르카나 문양을 고르세요", WORLD_W / 2f, 825f);

        for (int i = 0; i < SUITS.length; i++) {
            Rectangle rect = deckBounds[i];
            boolean hovered = rect.contains(pointer);
            fonts.normal.setColor(hovered ? Color.WHITE : new Color(0.82f, 0.80f, 0.76f, fadeAlpha));
            drawCentered(fonts.normal, suitLabel(SUITS[i]), rect.x + rect.width / 2f, 260f);
        }
        fonts.normal.setColor(Color.WHITE);
        fonts.title.setColor(Color.WHITE);
        batch.end();
    }

    private void drawCentered(com.badlogic.gdx.graphics.g2d.BitmapFont font, String text, float centerX, float baselineY) {
        layout.setText(font, text);
        font.draw(batch, text, centerX - layout.width / 2f, baselineY);
    }

    private String suitLabel(Suit suit) {
        switch (suit) {
            case WANDS: return "WANDS";
            case CUPS: return "CUPS";
            case SWORDS: return "SWORDS";
            case PENTACLES: return "PENTACLES";
            default: return suit.name();
        }
    }

    private Color suitColor(Suit suit, float alpha) {
        switch (suit) {
            case WANDS: return new Color(0.88f, 0.38f, 0.12f, alpha);
            case CUPS: return new Color(0.76f, 0.22f, 0.42f, alpha);
            case SWORDS: return new Color(0.32f, 0.52f, 0.82f, alpha);
            case PENTACLES: return new Color(0.24f, 0.66f, 0.32f, alpha);
            default: return new Color(0.8f, 0.8f, 0.8f, alpha);
        }
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
        fonts.dispose();
        assets.dispose();
    }
}
