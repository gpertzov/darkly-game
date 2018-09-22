package net.gpdev.darkly.actions;

import com.badlogic.gdx.math.Vector2;
import net.gpdev.darkly.actors.GameEntity;

public class Move extends EntityAction {

    private final Vector2 destination;

    public Move(final GameEntity source, final Vector2 destination) {
        super(Type.MOVE, source, source);
        this.destination = destination;
    }

    public Vector2 getDestination() {
        return destination;
    }
}
