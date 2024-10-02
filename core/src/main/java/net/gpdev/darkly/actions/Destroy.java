package net.gpdev.darkly.actions;

import net.gpdev.darkly.actors.GameEntity;

public class Destroy extends EntityAction {
    public Destroy(final GameEntity source) {
        super(Type.DESTROY, source, null);
    }
}
