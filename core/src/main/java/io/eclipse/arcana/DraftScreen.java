package io.eclipse.arcana;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.eclipse.arcana.model.Card;
import io.eclipse.arcana.model.CardDefinitions;
import io.eclipse.arcana.model.Suit;

public class DraftScreen implements Screen {
    private static final float WORLD_W = 1600f;
    private static final float WORLD_H = 1000f;
    private static final float CARD_W = 235f;
    private static final float CARD_H = CARD_W * 2.09f;
    private static final float CARD_Y = 280f;
    private static final float CARD_GAP = 118f;
    private static final float HOVER_LIFT = 12f;
    private static final float SELECT_LIFT = 20f;
    private static final float ANIM_SPEED = 8f;
    private static final float INTRO_SPEED = 2.8f;
    private static final float CONFIRM_TIME = 0.28f;
    private static final Rectangle CONFIRM_BUTTON = new Rectangle(620f, 80f, 360f, 90f);

    private static final Color GOLD = new Color(0.78f, 0.57f, 0.25f, 1f);
    private static final Color GOLD_BRIGHT = new Color(1f, 0.80f, 0.43f, 1f);
    private static final Color PURPLE = new Color(0.48f, 0.18f, 0.76f, 1f);
    private static final Color MUTED = new Color(0.72f, 0.72f, 0.78f, 1f);

    private final Core game;
    private final Suit suit;
    private final Vector3 pointer = new Vector3();
    private final GlyphLayout layout = new GlyphLayout();
    private final Array<Card> majorPool = CardDefinitions.allMajor();
    private final Array<Card> candidates = new Array<>();
    private final Array<Card> playerDraftMajors = new Array<>();
    private final Array<Card> opponentDraftMajors = new Array<>();
    private final Rectangle[] cardBounds = new Rectangle[3];
    private final float[] hoverAnim = new float[3];
    private final float[] revealAnim = new float[3];

    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shape;
    private FontManager fonts;
    private int draftRound = 1;
    private int hoveredIndex = -1;
    private int selectedIndex = -1;
    private float introAlpha;
    private float confirmTimer;
    private float titleBaseScale;
    private float normalBaseScale;
    private boolean confirming;
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
        titleBaseScale = fonts.title.getData().scaleX;
        normalBaseScale = fonts.normal.getData().scaleX;

        float totalWidth = CARD_W * 3f + CARD_GAP * 2f;
        float startX = (WORLD_W - totalWidth) / 2f;
        for (int i = 0; i < cardBounds.length; i++) {
            cardBounds[i] = new Rectangle(startX + i * (CARD_W + CARD_GAP), CARD_Y, CARD_W, CARD_H);
        }
        majorPool.shuffle();
        dealCandidates();
    }

    @Override
    public void render(float delta) {
        updateState(delta);
        if (disposed) return;
        handleInput();
        if (disposed) return;

        Gdx.gl.glClearColor(0.005f, 0.006f, 0.025f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shape.setProjectionMatrix(camera.combined);

        drawBackground();
        drawShapes();
        drawContent();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void updateState(float delta) {
        updatePointer();
        introAlpha = Math.min(1f, introAlpha + delta * INTRO_SPEED);

        hoveredIndex = -1;
        if (!confirming) {
            for (int i = 0; i < candidates.size; i++) {
                if (displayBounds(i).contains(pointer.x, pointer.y)) {
                    hoveredIndex = i;
                    break;
                }
            }
        }

        for (int i = 0; i < candidates.size; i++) {
            float target = i == hoveredIndex || i == selectedIndex ? 1f : 0f;
            hoverAnim[i] = MathUtils.lerp(hoverAnim[i], target, Math.min(1f, delta * ANIM_SPEED));
            float delay = i * 0.11f;
            revealAnim[i] = MathUtils.clamp((introAlpha - delay) / (1f - delay), 0f, 1f);
        }

        if (confirming) {
            confirmTimer += delta;
            if (confirmTimer >= CONFIRM_TIME) commitSelection();
        }
    }

    private void handleInput() {
        if (confirming || !Gdx.input.justTouched()) return;

        for (int i = 0; i < candidates.size; i++) {
            if (displayBounds(i).contains(pointer.x, pointer.y)) {
                selectedIndex = i;
                return;
            }
        }

        if (selectedIndex >= 0 && CONFIRM_BUTTON.contains(pointer.x, pointer.y)) {
            confirming = true;
            confirmTimer = 0f;
        }
    }

    private void drawBackground() {
        batch.begin();
        Texture background = game.assets.draftBackground();
        if (background != null) {
            batch.setColor(0.88f, 0.88f, 0.88f, 1f);
            batch.draw(background, 0f, 0f, WORLD_W, WORLD_H);
            batch.setColor(Color.WHITE);
        }
        batch.end();
    }

    private void drawShapes() {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        drawHeaderDecoration();
        for (int i = 0; i < candidates.size; i++) drawCardGlow(i);
        drawInstructionDecoration();
        drawConfirmButton();
        shape.end();
    }

    private void drawHeaderDecoration() {
        shape.setColor(GOLD.r, GOLD.g, GOLD.b, 0.55f * introAlpha);
        shape.rect(520f, 805f, 210f, 1.5f);
        shape.rect(870f, 805f, 210f, 1.5f);
        drawDiamond(800f, 806f, 7f, GOLD_BRIGHT, 0.85f * introAlpha);
        drawDiamond(748f, 806f, 3.5f, GOLD, 0.70f * introAlpha);
        drawDiamond(852f, 806f, 3.5f, GOLD, 0.70f * introAlpha);
    }

    private void drawCardGlow(int index) {
        Rectangle bounds = displayBounds(index);
        float reveal = Interpolation.pow2Out.apply(revealAnim[index]);
        float shrink = confirming ? MathUtils.clamp(1f - confirmTimer / CONFIRM_TIME, 0f, 1f) : 1f;
        float alpha = reveal * introAlpha * shrink;
        boolean selected = index == selectedIndex;
        boolean hovered = index == hoveredIndex;
        if (selected || hovered) {
            drawDiamond(bounds.x + bounds.width / 2f, bounds.y + bounds.height + 17f,
                selected ? 10f : 6f, GOLD_BRIGHT, selected ? alpha : alpha * 0.72f);
        }
    }

    private void drawInstructionDecoration() {
        if (selectedIndex >= 0) return;
        shape.setColor(GOLD.r, GOLD.g, GOLD.b, 0.52f);
        shape.rect(585f, 205f, 90f, 1.3f);
        shape.rect(925f, 205f, 90f, 1.3f);
        drawDiamond(565f, 205f, 4f, GOLD_BRIGHT, 0.82f);
        drawDiamond(1035f, 205f, 4f, GOLD_BRIGHT, 0.82f);
    }

    private void drawConfirmButton() {
        if (game.assets.draftSelectButton() != null) return;
        boolean enabled = selectedIndex >= 0 && !confirming;
        boolean hovered = enabled && CONFIRM_BUTTON.contains(pointer.x, pointer.y);
        Color fill = enabled ? PURPLE : new Color(0.10f, 0.08f, 0.16f, 1f);
        Color border = enabled ? GOLD_BRIGHT : new Color(0.33f, 0.28f, 0.38f, 1f);

        if (hovered) {
            shape.setColor(GOLD_BRIGHT.r, GOLD_BRIGHT.g, GOLD_BRIGHT.b, 0.12f);
            shape.rect(CONFIRM_BUTTON.x - 12f, CONFIRM_BUTTON.y - 10f,
                CONFIRM_BUTTON.width + 24f, CONFIRM_BUTTON.height + 20f);
        }
        shape.setColor(border.r, border.g, border.b, enabled ? 0.90f : 0.55f);
        shape.rect(CONFIRM_BUTTON.x, CONFIRM_BUTTON.y, CONFIRM_BUTTON.width, CONFIRM_BUTTON.height);
        shape.setColor(fill.r, fill.g, fill.b, hovered ? 0.95f : 0.78f);
        shape.rect(CONFIRM_BUTTON.x + 3f, CONFIRM_BUTTON.y + 3f,
            CONFIRM_BUTTON.width - 6f, CONFIRM_BUTTON.height - 6f);
        drawDiamond(CONFIRM_BUTTON.x, CONFIRM_BUTTON.y + CONFIRM_BUTTON.height / 2f,
            13f, border, enabled ? 0.95f : 0.55f);
        drawDiamond(CONFIRM_BUTTON.x + CONFIRM_BUTTON.width,
            CONFIRM_BUTTON.y + CONFIRM_BUTTON.height / 2f,
            13f, border, enabled ? 0.95f : 0.55f);
    }

    private void drawContent() {
        batch.begin();
        for (int i = 0; i < candidates.size; i++) drawCard(i);

        Texture titleTexture = game.assets.draftTitle();
        if (titleTexture != null) {
            batch.setColor(1f, 1f, 1f, introAlpha);
            float w = 440f;
            float h = 110f;
            batch.draw(titleTexture, (WORLD_W - w) / 2f, 858f, w, h);
            batch.setColor(Color.WHITE);
        } else {
            setFontScale(fonts.title, 1.22f);
            fonts.title.setColor(GOLD_BRIGHT.r, GOLD_BRIGHT.g, GOLD_BRIGHT.b, introAlpha);
            drawCentered(fonts.title, "MAJOR ARCANA DRAFT", 905f);
            setFontScale(fonts.title, 1f);
        }

        Texture roundTexture = game.assets.draftRound(draftRound);
        if (roundTexture != null) {
            batch.setColor(1f, 1f, 1f, introAlpha);
            float rw = 240f;
            float rh = 60f;
            batch.draw(roundTexture, (WORLD_W - rw) / 2f, 810f, rw, rh);
            batch.setColor(Color.WHITE);
        } else {
            fonts.normal.setColor(GOLD_BRIGHT.r, GOLD_BRIGHT.g, GOLD_BRIGHT.b, introAlpha);
            drawCentered(fonts.normal, "ROUND " + draftRound + " / 2", 825f);
        }

        if (selectedIndex < 0) {
            fonts.normal.setColor(MUTED);
            drawCentered(fonts.normal, "하나를 선택하세요", 205f);
        } else {
            Card card = candidates.get(selectedIndex);
            io.eclipse.arcana.model.CardDescriptions.Entry descEntry = io.eclipse.arcana.model.CardDescriptions.get(card.id);
            String desc = descEntry != null ? descEntry.upright : "";
            setFontScale(fonts.normal, 0.82f);
            fonts.normal.setColor(1f, 1f, 1f, introAlpha);
            fonts.normal.draw(batch, desc, 300f, 205f, 1000f, Align.center, true);
            setFontScale(fonts.normal, 1f);
        }

        Texture btnTexture = game.assets.draftSelectButton();
        if (btnTexture != null) {
            boolean enabled = selectedIndex >= 0 && !confirming;
            boolean hovered = enabled && CONFIRM_BUTTON.contains(pointer.x, pointer.y);

            if (enabled) {
                if (hovered) {
                    batch.setColor(1f, 1f, 1f, 1f);
                } else {
                    batch.setColor(0.85f, 0.85f, 0.9f, 0.9f);
                }
            } else {
                batch.setColor(0.3f, 0.3f, 0.35f, 0.5f);
            }
            batch.draw(btnTexture, CONFIRM_BUTTON.x, CONFIRM_BUTTON.y, CONFIRM_BUTTON.width, CONFIRM_BUTTON.height);
            batch.setColor(Color.WHITE);
        } else {
            fonts.normal.setColor(selectedIndex >= 0 ? Color.WHITE : MUTED);
            setFontScale(fonts.normal, 1.12f);
            drawCentered(fonts.normal, confirming ? "..." : "선택",
                CONFIRM_BUTTON.y + 44f);
            setFontScale(fonts.normal, 1f);
        }
        batch.end();
    }

    private void drawCard(int index) {
        Card card = candidates.get(index);
        Rectangle bounds = displayBounds(index);
        float reveal = Interpolation.pow2Out.apply(revealAnim[index]);
        float shrink = confirming ? MathUtils.clamp(1f - confirmTimer / CONFIRM_TIME, 0f, 1f) : 1f;
        float scale = (0.84f + reveal * 0.16f) * shrink;
        float width = bounds.width * scale;
        float height = bounds.height * scale;
        float x = bounds.x + (bounds.width - width) / 2f;
        float y = bounds.y + (bounds.height - height) / 2f;

        Texture illustration = game.assets.cardIllust(card);
        if (illustration != null) {
            batch.setColor(1f, 1f, 1f, reveal * introAlpha);
            batch.draw(illustration, x, y, width, height);
            batch.setColor(Color.WHITE);
        }

        fonts.normal.setColor(GOLD_BRIGHT.r, GOLD_BRIGHT.g, GOLD_BRIGHT.b, reveal * introAlpha);
        drawCenteredAt(fonts.normal, card.name.toUpperCase(), bounds.x + bounds.width / 2f, CARD_Y - 24f);
    }

    private Rectangle displayBounds(int index) {
        Rectangle base = cardBounds[index];
        float lift = hoverAnim[index] * (index == selectedIndex ? SELECT_LIFT : HOVER_LIFT);
        return new Rectangle(base.x, base.y + lift, base.width, base.height);
    }

    private void dealCandidates() {
        candidates.clear();
        selectedIndex = -1;
        hoveredIndex = -1;
        introAlpha = 0f;
        for (int i = 0; i < revealAnim.length; i++) {
            revealAnim[i] = 0f;
            hoverAnim[i] = 0f;
        }
        majorPool.shuffle();
        for (int i = 0; i < 3 && i < majorPool.size; i++) candidates.add(majorPool.get(i));
    }

    private void commitSelection() {
        confirming = false;
        confirmTimer = 0f;
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

    private void drawFrame(float x, float y, float width, float height, float thickness) {
        shape.rect(x, y, width, thickness);
        shape.rect(x, y + height - thickness, width, thickness);
        shape.rect(x, y, thickness, height);
        shape.rect(x + width - thickness, y, thickness, height);
    }

    private void drawDiamond(float cx, float cy, float radius, Color color, float alpha) {
        shape.setColor(color.r, color.g, color.b, alpha);
        float[] vertices = { cx, cy + radius, cx + radius, cy, cx, cy - radius, cx - radius, cy };
        shape.triangle(vertices[0], vertices[1], vertices[2], vertices[3], vertices[4], vertices[5]);
        shape.triangle(vertices[0], vertices[1], vertices[4], vertices[5], vertices[6], vertices[7]);
    }

    private void setFontScale(BitmapFont font, float scale) {
        float baseScale = font == fonts.title ? titleBaseScale : normalBaseScale;
        font.getData().setScale(baseScale * scale);
    }

    private void drawCentered(BitmapFont font, String text, float y) {
        drawCenteredAt(font, text, WORLD_W / 2f, y);
    }

    private void drawCenteredAt(BitmapFont font, String text, float centerX, float y) {
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
