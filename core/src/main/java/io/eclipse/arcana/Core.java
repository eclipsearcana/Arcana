package io.eclipse.arcana;

import com.badlogic.gdx.Game;

public class Core extends Game {
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
        setScreen(new MainScreen(this, debugContext));
        if (debugWindowOpener != null) {
            debugWindowOpener.open(debugContext);
        }
    }
}
