package io.eclipse.arcana;

import com.badlogic.gdx.Game;

public class Core extends Game {
    @Override
    public void create() {
        // setScreen(new FirstScreen(this)); // 출시 시 활성화
        setScreen(new MainScreen(this));
    }
}
