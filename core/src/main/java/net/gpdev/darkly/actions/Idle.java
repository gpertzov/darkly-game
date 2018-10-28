package net.gpdev.darkly.actions;

public class Idle extends EntityAction {

    public static EntityAction IDLE_ACTION = new Idle();

    private Idle() {
        super(Type.IDLE, null, null);
    }
}
