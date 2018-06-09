package net.gpdev.darkly;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapProperties;

public class TriggeredEvent {
    private static final String TAG = TriggeredEvent.class.getSimpleName();

    public enum Type {
        HEAL,
        HARM,
        CHARGE,
        WIN
    }

    private Type type;
    private int amount;

    public TriggeredEvent(final Type type, final int amount) {
        this.type = type;
        this.amount = amount;
    }

    public static TriggeredEvent fromProperties(final MapProperties properties) {
        TriggeredEvent event = null;
        final String eventType = properties.get("event", String.class);
        if (eventType == null) {
            return null;
        }

        try {
            final Type type = Type.valueOf(eventType);
            final int amount = properties.get("amount", 0, Integer.class);
            event = new TriggeredEvent(type, amount);
        } catch (IllegalArgumentException e) {
            Gdx.app.error(TAG, "Invalid event type: " + eventType);
        }

        return event;
    }

    public Type getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }
}
