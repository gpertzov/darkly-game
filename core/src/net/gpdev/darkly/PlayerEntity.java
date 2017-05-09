package net.gpdev.darkly;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import static net.gpdev.darkly.DarklyGame.FLASHLIGHT;

public class PlayerEntity extends GameEntity {

    private static final float BATTERY_DRAIN = 0.1f;

    private float flashlightRotation = 0f;
    private float health = 1f;
    private float battery = 1f;

    public PlayerEntity(Sprite sprite, Vector2 position, float speed, Rectangle boundingBox) {
        super(sprite, position, speed, boundingBox);
    }

    public void toggleFlashlight() {
        lights.get(FLASHLIGHT).toggle();
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
        lights.get(FLASHLIGHT).getSprite().setRotation(flashlightRotation);

        // Update position
        final Vector2 velocity = getVelocity().scl(delta);
        setPosition(getPosition().add(velocity));

        // Deplete battery
        final Light flashlight = lights.get(FLASHLIGHT);
        if (flashlight.isEnabled()) {
            battery -= (BATTERY_DRAIN * delta);
        }

        // Turn off flashlight when battery is depleted
        if (battery <= 0) {
            battery = 0;
            flashlight.setEnabled(false);
        }
    }
}
