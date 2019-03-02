package net.gpdev.darkly;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import net.gpdev.darkly.actors.GameEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class GameLevel implements Disposable {
    private static final String TAG = GameLevel.class.getSimpleName();

    private static final String POSITIONS_LAYER = "positions";
    private static final String COLLISION_LAYER = "collision";
    private static final String TRIGGERS_LAYER = "triggers";

    private final TiledMap map;
    private final float unitScale;
    private final MapLayer positionsLayer;
    private final MapLayer collisionLayer;
    private final TiledMapTileLayer triggersLayer;
    private final int mapWidth;
    private final int mapHeight;

    private final List<GameEntity> entities;

    public GameLevel(final String path, final float unitScale) {
        this.unitScale = unitScale;

        map = new TmxMapLoader().load(path);
        final TiledMapTileLayer mapTileLayer = (TiledMapTileLayer) map.getLayers().get(0);
        if (mapTileLayer == null) {
            Gdx.app.error(TAG, "Failed to get base tiles layer");
            throw new RuntimeException();
        }
        mapWidth = mapTileLayer.getWidth();
        mapHeight = mapTileLayer.getHeight();
        positionsLayer = map.getLayers().get(POSITIONS_LAYER);
        collisionLayer = map.getLayers().get(COLLISION_LAYER);
        triggersLayer = (TiledMapTileLayer) map.getLayers().get(TRIGGERS_LAYER);

        entities = new ArrayList<>();
    }

    public float getUnitScale() {
        return unitScale;
    }

    public TiledMap getMap() {
        return map;
    }

    public Vector2 getPosition(final String positionId) {
        final Vector2 position = new Vector2(0, 0);
        if (positionsLayer != null) {
            try {
                final MapObjects objects = positionsLayer.getObjects();
                final RectangleMapObject positionRect = (RectangleMapObject) objects.get(positionId);
                positionRect.getRectangle().getPosition(position);
            } catch (final Exception e) {
                Gdx.app.error(TAG, "Failed to obtain position '" + positionId + "': " + e.getMessage());
            }
        } else {
            Gdx.app.error(TAG, "Map has no positions layer");
        }
        return position.scl(unitScale);
    }

    public void addEntity(final GameEntity entity) {
        entities.add(entity);
    }

    public List<GameEntity> getEntitiesInRange(final GameEntity self, final Vector2 position, final float range) {
        final float range2 = range * range;
        return entities.stream()
                .filter(entity -> entity != self && entity.getPosition().dst2(position) <= range2)
                .collect(Collectors.toList());
    }

    public List<Vector2> getFunctionalLightsPositions(final GameEntity self, final Vector2 position) {
        final List<Vector2> lightPositions = new ArrayList<>();
        for (final GameEntity entity : entities) {
            if (entity == self) {
                continue;
            }
            final Collection<Light> lights = entity.getLights();
            for (final Light light : lights) {
                if (light.isFunctional() && light.isEnabled()) {
                    lightPositions.add(entity.getPosition());
                }
            }
        }
        return lightPositions;
    }

    public boolean isOutOfBounds(final Rectangle rect) {
        return rect.x < 0 || (rect.x + rect.width) >= mapWidth || rect.y < 0 || (rect.y + rect.height) >= mapHeight;
    }

    public boolean isCollision(final Rectangle boundingBox) {
        if (collisionLayer != null) {
            final Rectangle scaledBoundingBox = new Rectangle(boundingBox);
            scaledBoundingBox.x = boundingBox.x / unitScale;
            scaledBoundingBox.y = boundingBox.y / unitScale;
            final MapObjects objects = collisionLayer.getObjects();
            for (final MapObject object : objects) {
                if (object instanceof RectangleMapObject) {
                    final Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    if (rect.overlaps(scaledBoundingBox)) {
                        return true;
                    }
                } else {
                    Gdx.app.debug(TAG, "Unsupported collision object type");
                }
            }
        }
        return false;
    }

    public TriggeredEvent consumeTrigger(final int x, final int y) {
        final TiledMapTileLayer.Cell cell = triggersLayer.getCell(x, y);
        if (cell != null) {
            final MapProperties tileProperties = cell.getTile().getProperties();
            triggersLayer.setCell(x, y, null);
            return TriggeredEvent.fromProperties(tileProperties);
        }
        return null;
    }

    @Override
    public void dispose() {
        map.dispose();
    }
}
