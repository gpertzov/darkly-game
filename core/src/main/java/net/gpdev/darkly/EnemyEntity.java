package net.gpdev.darkly;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

public class EnemyEntity extends GameEntity {

    // TODO - Load enemy attributes from Level
    private static final int DISTANCE_RANGE = 16;
    private static final int TARGET_RANGE = 3;
    private static final float INTERCEPT_RADIUS_2 = 1f;
    private static final float ATTACK_COOL_DOWN = 2f;
    private static final int ATTACK_DAMAGE = 10;

    private float distanceToMove = 0;
    private float attackTime = 0f;
    private ENEMY_STATE state;

    public enum ENEMY_STATE {
        SEEK,
        INTERCEPT,
        ATTACK
    }

    public EnemyEntity(final Sprite sprite,
                       final Vector2 position,
                       final float speed,
                       final Rectangle boundingBox) {
        super(sprite, position, speed, boundingBox);
    }

    public float getAttackTime() {
        return attackTime;
    }

    public int getAttackDamage() {
        return ATTACK_DAMAGE;
    }

    public ENEMY_STATE getState() {
        return state;
    }

    @Override
    public void update(final float delta) {
        super.update(delta);

        final List<GameEntity> targets = level.getEntitiesInRange(this, getPosition(), TARGET_RANGE);

        // Target in range
        if (!targets.isEmpty()) {
            // INTERCEPT //
            final GameEntity target = targets.get(0);
            final Vector2 dirToTarget = target.getPosition().sub(getPosition());
            if (dirToTarget.len2() > INTERCEPT_RADIUS_2) {
                setDirection(dirToTarget);
            } else {
                setDirection(Vector2.Zero);
            }

            // ATTACK //
            if (attackTime == 0 || attackTime > ATTACK_COOL_DOWN) {
                attackTime = 0f;
                state = ENEMY_STATE.ATTACK;
            } else {
                state = ENEMY_STATE.INTERCEPT;
            }
            attackTime += delta;
        } else {
            // SEEK //
            state = ENEMY_STATE.SEEK;

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
