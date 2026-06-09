package io.eclipse.arcana;

import com.badlogic.gdx.Game;
import io.eclipse.arcana.model.Suit;

public class Core extends Game {
    public final ArcanaAssets assets = new ArcanaAssets();

    private final DebugContext debugContext;
    private final DebugWindowOpener debugWindowOpener;

    public Core() {
        this(null);
    }

    public Core(DebugWindowOpener debugWindowOpener) {
        this.debugContext = new DebugContext();
        this.debugWindowOpener = debugWindowOpener;
    }

    @Override
    public void create() {
        setScreen(new LoadingScreen(this));
        if (debugWindowOpener != null) {
            debugWindowOpener.open(debugContext);
        }
    }

    public void startGame(Suit suit) {
        setScreen(new MainScreen(this, debugContext, suit));
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
