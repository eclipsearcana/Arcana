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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.eclipse.arcana.model.Card;
import io.eclipse.arcana.model.CardDefinitions;
import io.eclipse.arcana.model.Suit;

public class DraftScreen implements Screen {
    private static final float WORLD_W = 1600f;
    private static final float WORLD_H = 1000f;
    private static final float CARD_W = 240f;
    private static final float CARD_H = CARD_W * 2.09f;
    private static final float CARD_Y = 245f;
    private static final float CARD_GAP = 85f;

    private final Core game;
    private final Suit suit;
    private final Vector3 pointer = new Vector3();
    private final GlyphLayout layout = new GlyphLayout();
    private final Array<Card> majorPool = CardDefinitions.allMajor();
    private final Array<Card> candidates = new Array<>();
    private final Array<Card> playerDraftMajors = new Array<>();
    private final Array<Card> opponentDraftMajors = new Array<>();
    private final Rectangle[] cardBounds = new Rectangle[3];
    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shape;
    private FontManager fonts;
    private int draftRound = 1;
    private boolean disposed;

    public DraftScreen(Core game, Suit suit) {
        this.game = game;
        this.suit = suit;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        fonts = new FontManager(Gdx.graphics.getWidth() / WORLD_W);
        float totalWidth = CARD_W * 3f + CARD_GAP * 2f;
        float startX = (WORLD_W - totalWidth) / 2f;
        for (int i = 0; i < cardBounds.length; i++) {
            cardBounds[i] = new Rectangle(startX + i * (CARD_W + CARD_GAP), CARD_Y, CARD_W, CARD_H);
        }
        dealCandidates();
    }

    @Override
    public void render(float delta) {
        updatePointer();
        if (Gdx.input.justTouched()) {
            for (int i = 0; i < candidates.size; i++) {
                if (cardBounds[i].contains(pointer.x, pointer.y)) {
                    selectCandidate(i);
                    return;
                }
            }
        }

        Gdx.gl.glClearColor(0.01f, 0.01f, 0.035f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        Texture background = game.assets.draftBackground();
        if (background == null) background = game.assets.background();
        if (background != null) batch.draw(background, 0f, 0f, WORLD_W, WORLD_H);
        batch.end();

        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.01f, 0.008f, 0.035f, 0.66f);
        shape.rect(140f, 175f, 1320f, 690f);
        for (int i = 0; i < candidates.size; i++) {
            Rectangle bounds = cardBounds[i];
            boolean hovered = bounds.contains(pointer.x, pointer.y);
            shape.setColor(hovered
                ? new Color(0.60f, 0.26f, 0.92f, 0.82f)
                : new Color(0.22f, 0.10f, 0.40f, 0.72f));
            shape.rect(bounds.x - 8f, bounds.y - 8f, bounds.width + 16f, bounds.height + 16f);
        }
        shape.end();

        batch.begin();
        for (int i = 0; i < candidates.size; i++) {
            Card card = candidates.get(i);
            Rectangle bounds = cardBounds[i];
            Texture illustration = game.assets.cardIllust(card);
            if (illustration != null) {
                batch.draw(illustration, bounds.x, bounds.y, bounds.width, bounds.height);
            }
            fonts.normal.setColor(Color.WHITE);
            drawCenteredAt(fonts.normal, card.name, bounds.x + bounds.width / 2f, bounds.y - 25f);
        }
        fonts.title.setColor(0.92f, 0.78f, 1f, 1f);
        drawCentered(fonts.title, "MAJOR ARCANA DRAFT", 825f);
        fonts.normal.setColor(Color.WHITE);
        drawCentered(fonts.normal, "Choose one shared Major Arcana - Round " + draftRound + " / 2", 765f);
        drawCentered(fonts.normal, "Your starting suit: " + suit.name(), 205f);
        batch.end();
    }

    private void dealCandidates() {
        candidates.clear();
        majorPool.shuffle();
        for (int i = 0; i < 3 && i < majorPool.size; i++) {
            candidates.add(majorPool.get(i));
        }
    }

    private void selectCandidate(int selectedIndex) {
        Card selected = candidates.get(selectedIndex);
        playerDraftMajors.add(selected);
        majorPool.removeValue(selected, true);

        Array<Card> unselected = new Array<>();
        for (Card card : candidates) {
            if (card != selected) unselected.add(card);
        }
        unselected.shuffle();
        Card opponentCard = unselected.first();
        opponentDraftMajors.add(opponentCard);
        majorPool.removeValue(opponentCard, true);

        if (draftRound >= 2) {
            game.startGame(suit, playerDraftMajors, opponentDraftMajors);
            dispose();
            return;
        }
        draftRound++;
        dealCandidates();
    }

    private void drawCentered(com.badlogic.gdx.graphics.g2d.BitmapFont font, String text, float y) {
        drawCenteredAt(font, text, WORLD_W / 2f, y);
    }

    private void drawCenteredAt(com.badlogic.gdx.graphics.g2d.BitmapFont font, String text, float centerX, float y) {
        layout.setText(font, text);
        font.draw(batch, text, centerX - layout.width / 2f, y);
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
        if (disposed) return;
        disposed = true;
        if (batch != null) batch.dispose();
        if (shape != null) shape.dispose();
        if (fonts != null) fonts.dispose();
    }
}
