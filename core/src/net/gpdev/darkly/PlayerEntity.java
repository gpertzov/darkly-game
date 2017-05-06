package net.gpdev.darkly;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import static net.gpdev.darkly.DarklyGame.FLASHLIGHT;

public class PlayerEntity extends GameEntity {

    private float flashlightRotation = 0;
    private int health = 100;
    private int battery = 100;

    public PlayerEntity(Sprite sprite, Vector2 position, float speed, Rectangle boundingBox) {
        super(sprite, position, speed, boundingBox);
    }

    public void toggleFlashlight() {
        lights.get(FLASHLIGHT).toggle();
    }

    public int getHealthLevel() {
        return health;
    }

    public int getBatteryLevel() {
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
    }
}
