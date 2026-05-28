package io.eclipse.arcana;

import java.util.ArrayDeque;
import java.util.Queue;

import io.eclipse.arcana.model.GameState;

public class DebugContext {
    private GameState state;
    private final Queue<DebugCommand> commands = new ArrayDeque<>();

    public synchronized void setState(GameState state) {
        this.state = state;
    }

    public synchronized void clearState(GameState state) {
        if (this.state == state) {
            this.state = null;
            commands.clear();
        }
    }

    public synchronized GameState getState() {
        return state;
    }

    public synchronized void request(DebugCommand command) {
        commands.add(command);
    }

    public synchronized DebugCommand pollCommand() {
        return commands.poll();
    }
}
