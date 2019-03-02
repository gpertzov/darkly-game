package net.gpdev.darkly.actors;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import net.gpdev.darkly.Light;
import net.gpdev.darkly.TriggeredEvent;
import net.gpdev.darkly.actions.EntityAction;
import net.gpdev.darkly.actions.Move;

import static net.gpdev.darkly.DarklyGame.FLASHLIGHT;
import static net.gpdev.darkly.actions.Idle.IDLE_ACTION;

public class PlayerEntity extends GameEntity {

    private static final float BATTERY_DRAIN = 0.06f;

    private float flashlightRotation = 0f;
    private float health = 1f;
    private float battery = 1f;
    private final Light flashlight;

    public PlayerEntity(final Sprite sprite,
                        final Vector2 position,
                        final float speed,
                        final Rectangle boundingBox,
                        final boolean isCollidable,
                        final Sprite flashlightSprite) {
        super(sprite, position, speed, boundingBox, isCollidable);
        this.flashlight = new Light(flashlightSprite, false, true);
        addLight(FLASHLIGHT, flashlight);
    }

    public void toggleFlashlight() {
        flashlight.toggle();
    }

    public float getHealthLevel() {
        return health;
    }

    public float getBatteryLevel() {
        return battery;
    }

    @Override
    public EntityAction update(final float delta) {
        super.update(delta);

        // Compute flashlight rotation
        final Vector2 curVelocity = getVelocity();
        if (!curVelocity.isZero()) {
            flashlightRotation = curVelocity.angle() - 90;
        }
        flashlight.getSprite().setRotation(flashlightRotation);

        // Deplete battery
        if (flashlight.isEnabled()) {
            battery -= (BATTERY_DRAIN * delta);
        }

        // Turn off flashlight when battery is depleted
        if (battery <= 0) {
            battery = 0;
            flashlight.setEnabled(false);
        }

        // Update position
        final Vector2 velocity = getVelocity().scl(delta);
        if (velocity.isZero()) {
            return IDLE_ACTION;
        }
        return new Move(this, getPosition().add(velocity));
    }

    @Override
    public void reactTo(final TriggeredEvent event) {
        super.reactTo(event);

        final float amount = event.getAmount() / 100f;
        final TriggeredEvent.Type eventType = event.getType();

        switch (eventType) {
            case CHARGE: {
                battery += amount;
            }
            break;
            case HEAL: {
                health += amount;
            }
            break;
            case HARM: {
                health -= amount;
            }
            break;
            default: {

            }
            break;
        }

        battery = MathUtils.clamp(battery, 0, 1);
        health = MathUtils.clamp(health, 0, 1);
    }
}
