package net.gpdev.darkly;

import com.badlogic.gdx.Game;

public class DarklyGame extends Game {

    public static final String FLASHLIGHT = "flashlight";

    @Override
    public void create() {
        this.setScreen(new GameScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
