package net.gpdev.darkly;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class GameEntity {

    // TODO - Extract into separate components //
    // Graphics
    private TextureRegion frame;

    // Physics
    private final float speed;
    private final Rectangle boundingBox;
    private Vector2 position;
    private Vector2 velocity;


    public GameEntity(final TextureRegion frame,
                      final Vector2 position,
                      final float speed,
                      final Rectangle boundingBox) {
        this.frame = frame;
        this.position = position;
        this.speed = speed;
        this.boundingBox = boundingBox;

        velocity = new Vector2(0, 0);

    }

    public TextureRegion getFrame() {
        return frame;
    }

    public Vector2 getPosition() {
        return position.cpy();
    }

    public void setPosition(final Vector2 position) {
        this.position = position;
    }

    public Vector2 getVelocity() {
        return velocity.cpy();
    }

    public boolean isCollision(final float x, final float y, final Rectangle target) {
        final Rectangle rect = new Rectangle(
                x + boundingBox.x, y + boundingBox.y,
                boundingBox.width, boundingBox.height);

        return rect.overlaps(target);
    }

    public void updateDirection(final float x, final float y) {
        velocity.x += x * speed;
        velocity.y += y * speed;
    }
}
