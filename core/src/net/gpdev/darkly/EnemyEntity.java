package net.gpdev.darkly;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

public class EnemyEntity extends GameEntity {

    private static final int DISTANCE_RANGE = 16;
    private static final int TARGET_RANGE = 3;
    private float distanceToMove = 0;

    public EnemyEntity(final Sprite sprite,
                       final Vector2 position,
                       final float speed,
                       final Rectangle boundingBox) {
        super(sprite, position, speed, boundingBox);
    }

    @Override
    public void update(final float delta) {
        super.update(delta);

        final List<GameEntity> targets = level.getEntitiesInRange(this, getPosition(), TARGET_RANGE);

        if (!targets.isEmpty()) {
            // DESTROY //
            final GameEntity target = targets.get(0);
            final Vector2 dirToTarget = target.getPosition().sub(getPosition());
            setDirection(dirToTarget);
        } else {
            // SEEK //

            // Wander randomly
            if (distanceToMove <= 0 || getVelocity().isZero()) {
                // Pick a random direction
                final float xDir = MathUtils.random(-1, 1);
                final float yDir = MathUtils.random(-1, 1);
                setDirection(new Vector2(xDir, yDir));

                // Pick a random distance
                distanceToMove = MathUtils.random(DISTANCE_RANGE);
            }
        }

        // Update position
        final Vector2 scaledVelocity = getVelocity().scl(delta);
        final Vector2 newPosition = getPosition().add(scaledVelocity);

        // Move if not out of bounds
        final Rectangle bbox = getBoundingBox();
        if (!level.isOutOfBounds(new Rectangle(newPosition.x, newPosition.y, bbox.width, bbox.height))) {
            setPosition(newPosition);
            distanceToMove -= scaledVelocity.len();
        } else {
            distanceToMove = 0;
        }
    }
}
