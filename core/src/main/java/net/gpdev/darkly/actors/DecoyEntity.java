package net.gpdev.darkly.actors;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import net.gpdev.darkly.Light;
import net.gpdev.darkly.TriggeredEvent;
import net.gpdev.darkly.actions.Destroy;
import net.gpdev.darkly.actions.EntityAction;

import static net.gpdev.darkly.Light.LIGHT_DEFAULT_INTENSITY;
import static net.gpdev.darkly.actions.Idle.IDLE_ACTION;

public class DecoyEntity extends GameEntity {

    private static final float INTENSITY_DECAY_FACTOR = 0.2f;
    private float health = 0.3f;
    private final Light light;

    public DecoyEntity(final Sprite sprite,
                       final Vector2 position,
                       final float speed,
                       final Rectangle boundingBox,
                       final boolean isCollidable,
                       final Sprite lightSprite) {
        super(sprite, position, speed, boundingBox, isCollidable);
        light = new Light(lightSprite, true, true, LIGHT_DEFAULT_INTENSITY * 2);
        addLight("decoy", light);
    }

    @Override
    public EntityAction update(final float delta) {
        super.update(delta);

        if (health <= 0) {
            return new Destroy(this);
        }

        final float intensity = light.getIntensity();
        if (intensity <= 0) {
            return new Destroy(this);
        }

        light.setIntensity(intensity - (delta * INTENSITY_DECAY_FACTOR));

        return IDLE_ACTION;
    }

    @Override
    public void reactTo(final TriggeredEvent event) {
        super.reactTo(event);

        final float amount = event.getAmount() / 100f;
        final TriggeredEvent.Type eventType = event.getType();

        if (TriggeredEvent.Type.HARM.equals(eventType)) {
            health -= amount;
        }
    }
}
