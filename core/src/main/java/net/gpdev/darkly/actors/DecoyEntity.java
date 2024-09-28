package net.gpdev.darkly.actors;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import net.gpdev.darkly.Light;
import net.gpdev.darkly.TriggeredEvent;
import net.gpdev.darkly.actions.EntityAction;

import static net.gpdev.darkly.Light.LIGHT_DEFAULT_INTENSITY;

public class DecoyEntity extends GameEntity {

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
        return super.update(delta);
        // TODO: Decay light intensity over time
    }

    @Override
    public void reactTo(final TriggeredEvent event) {
        super.reactTo(event);
        // TODO: Handle attack damage
    }
}
