package io.eclipse.arcana;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.eclipse.arcana.model.Card;
import io.eclipse.arcana.model.Suit;

public class Core extends Game {

    public final ArcanaAssets assets = new ArcanaAssets();

    private final DebugContext debugContext;
    private final DebugWindowOpener debugWindowOpener;

    public Core() {
        this(null);
    }

    public Core(DebugWindowOpener debugWindowOpener) {
        this.debugContext = debugWindowOpener == null ? null : new DebugContext();
        this.debugWindowOpener = debugWindowOpener;
    }

    @Override
    public void create() {
        setScreen(new LoadingScreen(this));
        if (debugWindowOpener != null) {
            debugWindowOpener.open(debugContext);
        }
    }

    @Override
    public void render() {
        super.render();
    }

    public void startGame(Suit suit) {
        setScreen(new MainScreen(this, debugContext, suit));
    }

    public void startGame(Suit suit, Array<Card> playerDraftMajors, Array<Card> opponentDraftMajors) {
        setScreen(new MainScreen(this, debugContext, suit, playerDraftMajors, opponentDraftMajors));
    }

    public void showTutorial() {
        setScreen(new TutorialScreen(this));
    }

    public void showDeckSelect() {
        setScreen(new MinorDeckSelectScreen(this));
    }

    public void showDraft(Suit suit) {
        setScreen(new DraftScreen(this, suit));
    }

    public void showTitleScreen() {
        setScreen(new LoadingScreen(this));
    }

    public void showTitleScreen(boolean isBright) {
        setScreen(new LoadingScreen(this, isBright));
    }

    @Override
    public void dispose() {
        super.dispose();
        assets.dispose();
    }
}
