package net.gpdev.darkly;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import static net.gpdev.darkly.DarklyGame.FLASHLIGHT;

public class PlayerEntity extends GameEntity {

    private static final float BATTERY_DRAIN = 0.06f;

    private float flashlightRotation = 0f;
    private float health = 1f;
    private float battery = 1f;
    private Light flashlight;

    public PlayerEntity(Sprite sprite,
                        Vector2 position,
                        float speed,
                        Rectangle boundingBox,
                        Sprite flashlightSprite) {
        super(sprite, position, speed, boundingBox);
        this.flashlight = new Light(flashlightSprite, false);
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
    public void update(float delta) {
        super.update(delta);

        // Compute flashlight rotation
        if (!velocity.isZero()) {
            flashlightRotation = velocity.angle() - 90;
        }
        flashlight.getSprite().setRotation(flashlightRotation);

        // Update position
        final Vector2 velocity = getVelocity().scl(delta);
        setPosition(getPosition().add(velocity));

        // Deplete battery
        if (flashlight.isEnabled()) {
            battery -= (BATTERY_DRAIN * delta);
        }

        // Turn off flashlight when battery is depleted
        if (battery <= 0) {
            battery = 0;
            flashlight.setEnabled(false);
        }
    }

    @Override
    public void reactTo(TriggeredEvent event) {
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
