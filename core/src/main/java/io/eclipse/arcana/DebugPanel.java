package io.eclipse.arcana;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.eclipse.arcana.model.Card;
import io.eclipse.arcana.model.GamePhase;
import io.eclipse.arcana.model.GameState;
import io.eclipse.arcana.model.Player;

public class DebugPanel extends ApplicationAdapter {
    private static final float WORLD_W = 760f;
    private static final float WORLD_H = 860f;

    private static final float PAD = 18f;
    private static final float SECTION_GAP = 14f;
    private static final float LOG_X = 18f;
    private static final float LOG_Y = 22f;
    private static final float LOG_W = 724f;
    private static final float LOG_H = 420f;
    private static final float LOG_LINE_H = 18f;
    private static final float FONT_DPI_BOOST = 1.12f;
    private static final float MAX_FONT_SCALE_OVER_WINDOW = 1.42f;

    private static final Color BACKGROUND = new Color(0.045f, 0.05f, 0.06f, 1f);
    private static final Color PANEL = new Color(0.09f, 0.10f, 0.12f, 1f);
    private static final Color PANEL_ALT = new Color(0.12f, 0.13f, 0.15f, 1f);
    private static final Color BORDER = new Color(0.42f, 0.36f, 0.18f, 1f);
    private static final Color TEXT = new Color(0.92f, 0.91f, 0.86f, 1f);
    private static final Color MUTED = new Color(0.58f, 0.60f, 0.62f, 1f);
    private static final Color ACCENT = new Color(0.86f, 0.68f, 0.28f, 1f);
    private static final Color DANGER = new Color(0.42f, 0.12f, 0.12f, 1f);
    private static final Color BUTTON = new Color(0.15f, 0.17f, 0.22f, 1f);
    private static final Color BUTTON_HOVER = new Color(0.20f, 0.23f, 0.30f, 1f);

    private final DebugContext context;
    private final Vector2 pointer = new Vector2();
    private final GlyphLayout layout = new GlyphLayout();

    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shape;
    private FontManager fonts;

    private Rectangle drawButton;
    private Rectangle phaseButton;
    private Rectangle damageButton;
    private Rectangle restartButton;

    private int logScroll;

    public DebugPanel(DebugContext context) {
        this.context = context;
    }

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        fonts = new FontManager(debugFontPxPerUnit());

        float buttonY = 470f;
        drawButton = new Rectangle(24f, buttonY, 164f, 42f);
        phaseButton = new Rectangle(204f, buttonY, 164f, 42f);
        damageButton = new Rectangle(392f, buttonY, 164f, 42f);
        restartButton = new Rectangle(572f, buttonY, 164f, 42f);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                logScroll += amountY > 0f ? 4 : -4;
                return true;
            }
        });
    }

    private float debugFontPxPerUnit() {
        float windowPxPerUnit = Math.max(1f, Gdx.graphics.getWidth()) / WORLD_W;
        float bufferPxPerUnit = Math.max(1f, Gdx.graphics.getBackBufferWidth()) / WORLD_W;
        float boosted = Math.max(windowPxPerUnit, bufferPxPerUnit) * FONT_DPI_BOOST;
        return Math.min(boosted, windowPxPerUnit * MAX_FONT_SCALE_OVER_WINDOW);
    }

    @Override
    public void render() {
        pointer.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(pointer);

        GameState state = context.getState();
        List<String> logs = state == null ? Collections.<String>emptyList() : state.debugLogSnapshot();
        updateInput(logs.size());
        clampLogScroll(logs.size());

        Gdx.gl.glClearColor(BACKGROUND.r, BACKGROUND.g, BACKGROUND.b, BACKGROUND.a);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        camera.update();
        shape.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        shape.begin(ShapeRenderer.ShapeType.Filled);
        drawPanels();
        drawButtonShape(drawButton, BUTTON);
        drawButtonShape(phaseButton, BUTTON);
        drawButtonShape(damageButton, DANGER);
        drawButtonShape(restartButton, state != null && state.phase == GamePhase.GAME_OVER ? ACCENT : BUTTON);
        shape.end();

        batch.begin();
        drawHeader(state);
        if (state == null) {
            drawText("Waiting for game state...", PAD, 792f, MUTED, fonts.normal);
        } else {
            drawGameState(state);
        }
        drawCentered("Draw", drawButton, TEXT);
        drawCentered("Next Phase", phaseButton, TEXT);
        drawCentered("-25 HP", damageButton, TEXT);
        drawCentered("Restart", restartButton, TEXT);
        drawLog(logs);
        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void updateInput(int logCount) {
        if (Gdx.input.justTouched() && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            handleClick(pointer.x, pointer.y);
        }

        int page = Math.max(1, visibleLogLines() - 2);
        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_UP)) logScroll += page;
        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_DOWN)) logScroll -= page;
        if (Gdx.input.isKeyJustPressed(Input.Keys.HOME)) logScroll = Math.max(0, logCount - visibleLogLines());
        if (Gdx.input.isKeyJustPressed(Input.Keys.END)) logScroll = 0;
    }

    private void handleClick(float x, float y) {
        if (drawButton.contains(x, y)) {
            context.request(DebugCommand.DRAW_CURRENT);
        } else if (phaseButton.contains(x, y)) {
            context.request(DebugCommand.ADVANCE_PHASE);
        } else if (damageButton.contains(x, y)) {
            context.request(DebugCommand.DAMAGE_OPPONENT);
        } else if (restartButton.contains(x, y)) {
            context.request(DebugCommand.RESTART);
        }
    }

    private void drawPanels() {
        drawPanel(14f, 768f, 732f, 70f);
        drawPanel(14f, 588f, 354f, 160f);
        drawPanel(392f, 588f, 354f, 160f);
        drawPanel(14f, 528f, 732f, 46f);
        drawPanel(14f, 454f, 732f, 76f);
        drawPanel(LOG_X - 4f, LOG_Y - 4f, LOG_W + 8f, LOG_H + 8f);

        shape.setColor(PANEL_ALT);
        shape.rect(14f, 812f, 732f, 26f);
        shape.rect(14f, 722f, 354f, 26f);
        shape.rect(392f, 722f, 354f, 26f);
        shape.rect(LOG_X - 4f, LOG_Y + LOG_H - 30f, LOG_W + 8f, 34f);
    }

    private void drawPanel(float x, float y, float w, float h) {
        shape.setColor(PANEL);
        shape.rect(x, y, w, h);
        shape.setColor(BORDER);
        shape.rect(x, y, w, 1f);
        shape.rect(x, y + h - 1f, w, 1f);
        shape.rect(x, y, 1f, h);
        shape.rect(x + w - 1f, y, 1f, h);
    }

    private void drawButtonShape(Rectangle rect, Color color) {
        Color fill = rect.contains(pointer) ? BUTTON_HOVER : color;
        shape.setColor(BORDER);
        shape.rect(rect.x, rect.y, rect.width, rect.height);
        shape.setColor(fill);
        shape.rect(rect.x + 1f, rect.y + 1f, rect.width - 2f, rect.height - 2f);
    }

    private void drawHeader(GameState state) {
        drawText("ARCANA DEBUG", PAD, 830f, ACCENT, fonts.normal);
        if (state == null) return;

        drawText("Active P" + state.currentPlayerIndex, PAD, 798f, TEXT, fonts.small);
        drawText("Phase " + state.phase, 140f, 798f, TEXT, fonts.small);
        drawText("Turn " + state.turnPhase, 300f, 798f, TEXT, fonts.small);
        drawText("Timer " + String.format(Locale.ROOT, "%.1f", state.turnTimer), 455f, 798f, TEXT, fonts.small);
        drawText("Round " + state.roundCount, 600f, 798f, TEXT, fonts.small);
    }

    private void drawGameState(GameState state) {
        drawPlayer("P0", state.players[0], 28f, 728f);
        drawPlayer("P1", state.players[1], 406f, 728f);

        drawText("TABLE", 28f, 558f, MUTED, fonts.small);
        drawText("Winner: " + (state.winnerIndex < 0 ? "-" : "P" + state.winnerIndex), 116f, 558f, TEXT, fonts.small);
        drawText("Current deck: " + state.currentPlayer().deck.size(), 250f, 558f, TEXT, fonts.small);
        drawText("Current hand: " + state.currentPlayer().hand.size, 420f, 558f, TEXT, fonts.small);
        drawText("Current grave: " + state.currentPlayer().graveyard.size, 590f, 558f, TEXT, fonts.small);
    }

    private void drawPlayer(String label, Player player, float x, float topY) {
        drawText(label, x, topY, MUTED, fonts.small);
        drawText("HP " + player.hp, x, topY - 28f, TEXT, fonts.small);
        drawText("Cost " + player.cost + "/" + player.costInit + " max " + player.costMax, x + 116f, topY - 28f, TEXT, fonts.small);
        drawText("Hand " + player.hand.size, x, topY - 52f, TEXT, fonts.small);
        drawText("Field " + player.field.size, x + 92f, topY - 52f, TEXT, fonts.small);
        drawText("Grave " + player.graveyard.size, x + 174f, topY - 52f, TEXT, fonts.small);
        drawText("Deck " + player.deck.size(), x + 262f, topY - 52f, TEXT, fonts.small);
        drawText("Reverse A/G/T " + countActiveReversed(player)
            + "/" + countGraceReversed(player)
            + "/" + countReversed(player), x, topY - 76f, TEXT, fonts.small);
        drawText("Suit " + player.chosenSuit, x + 116f, topY - 76f, TEXT, fonts.small);
        drawText("Flags " + activeFlags(player), x, topY - 104f, TEXT, fonts.small);
    }

    private String activeFlags(Player player) {
        String flags = "";
        if (player.drawBlocked) flags += "draw ";
        if (player.majorBlocked) flags += "major ";
        if (player.effectsNegatedThisTurn) flags += "negate ";
        if (player.delayEffectsThisTurn) flags += "delay ";
        if (player.mirrorEffectsToSelfThisTurn) flags += "mirror ";
        if (player.cannotBeTargeted) flags += "untarget ";
        if (player.fakeShieldTurns > 0) flags += "shield(" + player.fakeShieldTurns + ") ";
        return flags.isEmpty() ? "-" : flags.trim();
    }

    private int countReversed(Player player) {
        int count = 0;
        for (Card card : player.hand) {
            if (card.reversed) count++;
        }
        return count;
    }

    private int countGraceReversed(Player player) {
        int count = 0;
        for (Card card : player.hand) {
            if (card.isReverseGraceActive()) count++;
        }
        return count;
    }

    private int countActiveReversed(Player player) {
        int count = 0;
        for (Card card : player.hand) {
            if (card.isReversePenaltyActive()) count++;
        }
        return count;
    }

    private void drawLog(List<String> logs) {
        drawText("CARD / EFFECT LOG", LOG_X + 8f, LOG_Y + LOG_H - 12f, ACCENT, fonts.small);
        drawText(logRangeLabel(logs), LOG_X + LOG_W - 160f, LOG_Y + LOG_H - 12f, MUTED, fonts.small);

        if (logs.isEmpty()) {
            drawText("No logs yet.", LOG_X + 8f, LOG_Y + LOG_H - 48f, MUTED, fonts.small);
            return;
        }

        int endExclusive = Math.max(0, logs.size() - logScroll);
        int start = Math.max(0, endExclusive - visibleLogLines());
        float y = LOG_Y + LOG_H - 48f;

        for (int i = start; i < endExclusive && y > LOG_Y + 16f; i++) {
            y = drawWrappedLogLine(logs.get(i), LOG_X + 8f, y, LOG_W - 18f);
        }
    }

    private String logRangeLabel(List<String> logs) {
        if (logs.isEmpty()) return "0 / 0";
        int endExclusive = Math.max(0, logs.size() - logScroll);
        int start = Math.max(0, endExclusive - visibleLogLines());
        return (start + 1) + "-" + endExclusive + " / " + logs.size();
    }

    private float drawWrappedLogLine(String text, float x, float y, float maxWidth) {
        String remaining = text;
        while (!remaining.isEmpty() && y > LOG_Y + 16f) {
            int cut = fitChars(remaining, maxWidth);
            String line = remaining.substring(0, cut);
            drawText(line, x, y, TEXT, fonts.small);
            remaining = remaining.substring(cut).trim();
            y -= LOG_LINE_H;
        }
        return y;
    }

    private int fitChars(String text, float maxWidth) {
        layout.setText(fonts.small, text);
        if (layout.width <= maxWidth) return text.length();

        int low = 1;
        int high = text.length();
        while (low < high) {
            int mid = (low + high + 1) / 2;
            layout.setText(fonts.small, text.substring(0, mid));
            if (layout.width <= maxWidth) low = mid;
            else high = mid - 1;
        }

        int cut = Math.max(1, low);
        for (int i = cut; i > Math.max(1, cut - 18); i--) {
            if (Character.isWhitespace(text.charAt(i - 1))) return i;
        }
        return cut;
    }

    private int visibleLogLines() {
        return Math.max(1, (int) ((LOG_H - 58f) / LOG_LINE_H));
    }

    private void clampLogScroll(int logCount) {
        int maxScroll = Math.max(0, logCount - visibleLogLines());
        if (logScroll < 0) logScroll = 0;
        if (logScroll > maxScroll) logScroll = maxScroll;
    }

    private void drawCentered(String text, Rectangle rect, Color color) {
        fonts.small.setColor(color);
        layout.setText(fonts.small, text);
        fonts.small.draw(batch, text,
            rect.x + (rect.width - layout.width) / 2f,
            rect.y + (rect.height + layout.height) / 2f);
    }

    private void drawText(String text, float x, float y, Color color, BitmapFont font) {
        font.setColor(color);
        font.draw(batch, text, x, y);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shape.dispose();
        fonts.dispose();
    }
}
