package net.gpdev.darkly;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class EnemyEntity extends GameEntity {
    public EnemyEntity(Sprite sprite, Vector2 position, float speed, Rectangle boundingBox) {
        super(sprite, position, speed, boundingBox);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }
}
