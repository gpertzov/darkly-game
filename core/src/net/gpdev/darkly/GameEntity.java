package net.gpdev.darkly;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GameEntity {

    // TODO - Extract into separate components //
    // Graphics
    private Sprite sprite;
    protected Map<String, Light> lights;

    // Physics
    private final float speed;
    private final Rectangle boundingBox;
    private Vector2 position;
    protected Vector2 velocity;


    public GameEntity(final Sprite sprite,
                      final Vector2 position,
                      final float speed,
                      final Rectangle boundingBox) {
        this.sprite = sprite;
        this.position = position;
        this.speed = speed;
        this.boundingBox = boundingBox;

        velocity = new Vector2(0, 0);
        lights = new HashMap<String, Light>();

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

    public Vector2 getVelocity() {
        return velocity.cpy();
    }

    public Rectangle getBoundingBox() {
        return new Rectangle(boundingBox);
    }

    public void addLight(final String name, final Light light) {
        lights.put(name, light);
    }

    public Collection<Light> getLights() {
        return Collections.unmodifiableCollection(lights.values());
    }

    public void onLightBufferChanged(final FrameBuffer fbo) {
        // Pre-compute lightmaps positions
        for (final Light light : lights.values()) {
            final Sprite sprite = light.getSprite();
            sprite.setPosition((fbo.getWidth() - sprite.getWidth()) / 2.0f,
                    (fbo.getHeight() - sprite.getHeight()) / 2.0f);
        }
    }

    public void update(float delta) {

    }

    public void updateDirection(final float x, final float y) {
        velocity.x += x * speed;
        velocity.y += y * speed;
    }
}
