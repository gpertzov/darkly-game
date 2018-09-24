package net.gpdev.darkly.actors;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import net.gpdev.darkly.GameLevel;
import net.gpdev.darkly.Light;
import net.gpdev.darkly.TriggeredEvent;
import net.gpdev.darkly.actions.EntityAction;

import java.util.*;

public class GameEntity {

    // TODO - Extract into separate components //
    // Graphics
    private final Sprite sprite;
    private final Map<String, Light> lights;

    // Physics
    private final float speed;
    private final Rectangle boundingBox;
    private final boolean isCollidable;
    private Vector2 position;
    private Vector2 velocity;

    // World representation
    protected GameLevel level;


    public GameEntity(final Sprite sprite,
                      final Vector2 position,
                      final float speed,
                      final Rectangle boundingBox,
                      final boolean isCollidable) {
        this.sprite = sprite;
        this.position = position;
        this.speed = speed;
        this.boundingBox = boundingBox;
        this.isCollidable = isCollidable;

        velocity = new Vector2(0, 0);
        lights = new HashMap<>();

    }

    public TextureRegion getSprite() {
        return sprite;
    }

    public Vector2 getPosition() {
        return position.cpy();
    }

    public void setPosition(final Vector2 position) {
        this.position = position;
        boundingBox.x = position.x;
        boundingBox.y = position.y;
    }

    public void setLevel(final GameLevel level) {
        this.level = level;
    }

    public Vector2 getVelocity() {
        return velocity.cpy();
    }

    public Rectangle getBoundingBox() {
        return new Rectangle(boundingBox);
    }

    public boolean isCollidable() {
        return isCollidable;
    }

    public void addLight(final String name, final Light light) {
        lights.put(name, light);
    }

    public Collection<Light> getLights() {
        return Collections.unmodifiableCollection(lights.values());
    }

    public Optional<EntityAction> update(final float delta) {
        return Optional.empty();
    }

    public void reactTo(final TriggeredEvent event) {

    }

    public void updateDirection(final Vector2 direction) {
        final Vector2 normDirection = direction.cpy().nor();
        velocity.x += normDirection.x * speed;
        velocity.y += normDirection.y * speed;
    }

    public void setDirection(final Vector2 direction) {
        final Vector2 normDirection = direction.cpy().nor();
        velocity.x = normDirection.x * speed;
        velocity.y = normDirection.y * speed;
    }

    public void projectLights(final Camera camera) {
        // Project lights to screen coordinates
        final Vector3 screenCoords = camera.project(new Vector3(position.x + 0.5f, position.y + 0.5f, 0));
        for (final Light light : lights.values()) {
            final Sprite sprite = light.getSprite();
            sprite.setPosition(screenCoords.x - sprite.getWidth() / 2.0f,
                    screenCoords.y - sprite.getHeight() / 2.0f);
        }
    }
}
