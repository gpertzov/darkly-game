package net.gpdev.darkly.actors;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import net.gpdev.darkly.actions.Attack;
import net.gpdev.darkly.actions.EntityAction;
import net.gpdev.darkly.actions.Move;

import java.util.List;

import static net.gpdev.darkly.actions.Idle.IDLE_ACTION;

public class EnemyEntity extends GameEntity {

    // TODO - Load enemy attributes from Level
    private static final int DISTANCE_RANGE = 16;
    private static final int TARGET_RANGE = 3;
    private static final float INTERCEPT_RADIUS_2 = 1f;
    private static final float ATTACK_COOL_DOWN = 2f;
    private static final int ATTACK_SKILL = 12;
    private static final int ATTACK_DAMAGE = 10;

    private float distanceToMove = 0;
    private float attackTime = 0f;

    public enum ENEMY_STATE {
        SEEK,
        INTERCEPT,
        ATTACK
    }

    public EnemyEntity(final Sprite sprite,
                       final Vector2 position,
                       final float speed,
                       final Rectangle boundingBox,
                       final boolean isCollidable) {
        super(sprite, position, speed, boundingBox, isCollidable);
    }

    public float getAttackTime() {
        return attackTime;
    }

    @Override
    public EntityAction update(final float delta) {
        super.update(delta);

        EntityAction action = IDLE_ACTION;

        final List<GameEntity> targets = level.getEntitiesInRange(this, getPosition(), TARGET_RANGE);

        // Target in range
        ENEMY_STATE state;
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
                action = new Attack(this, target, ATTACK_SKILL, ATTACK_DAMAGE);
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

        if (state != ENEMY_STATE.ATTACK) {
            // Update position
            final Vector2 scaledVelocity = getVelocity().scl(delta);
            final Vector2 newPosition = getPosition().add(scaledVelocity);

            // Move if not out of bounds
            final Rectangle bbox = getBoundingBox();
            if (!level.isOutOfBounds(new Rectangle(newPosition.x, newPosition.y, bbox.width, bbox.height))) {
                distanceToMove -= scaledVelocity.len();
                action = new Move(this, newPosition);
            } else {
                distanceToMove = 0;
            }
        }
        return action;
    }
}
