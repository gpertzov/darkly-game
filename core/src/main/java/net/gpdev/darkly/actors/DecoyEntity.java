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

    private float health = 0.3f;

    public DecoyEntity(final Sprite sprite,
                       final Vector2 position,
                       final float speed,
                       final Rectangle boundingBox,
                       final boolean isCollidable,
                       final Sprite lightSprite) {
        super(sprite, position, speed, boundingBox, isCollidable);
        final Light light = new Light(lightSprite, true, true, LIGHT_DEFAULT_INTENSITY * 2);
        addLight("decoy", light);
    }

    @Override
    public EntityAction update(final float delta) {
        super.update(delta);

        if (health <= 0) {
            return new Destroy(this);
        }

        // TODO: Decay light intensity over time

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
