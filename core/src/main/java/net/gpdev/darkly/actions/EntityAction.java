package net.gpdev.darkly.actions;

import net.gpdev.darkly.actors.GameEntity;

public abstract class EntityAction {
    public enum Type {
        MOVE,
        ATTACK
    }

    private final Type type;
    private final GameEntity source;
    private final GameEntity target;

    public EntityAction(final Type type, final GameEntity source, final GameEntity target) {
        this.type = type;
        this.source = source;
        this.target = target;
    }

    public Type getType() {
        return type;
    }

    public GameEntity getSource() {
        return source;
    }

    public GameEntity getTarget() {
        return target;
    }
}
