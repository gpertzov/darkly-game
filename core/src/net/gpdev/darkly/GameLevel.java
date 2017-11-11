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

public class GameLevel implements Disposable {
    private static final String TAG = GameLevel.class.getSimpleName();

    private static final String POSITIONS_LAYER = "positions";
    private static final String COLLISION_LAYER = "collision";
    private static final String TRIGGERS_LAYER = "triggers";

    private final TiledMap map;
    private final float unitScale;
    private MapLayer positionsLayer;
    private MapLayer collisionLayer;
    private TiledMapTileLayer triggersLayer;
    private final int mapWidth;
    private final int mapHeight;

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
    }

    public float getUnitScale() {
        return unitScale;
    }

    public TiledMap getMap() {
        return map;
    }

    public Vector2 getPosition(final String positionId) {
        Vector2 position = new Vector2(0, 0);
        if (positionsLayer != null) {
            try {
                final MapObjects objects = positionsLayer.getObjects();
                final RectangleMapObject positionRect = (RectangleMapObject) objects.get(positionId);
                positionRect.getRectangle().getPosition(position);
            } catch (Exception e) {
                Gdx.app.error(TAG, "Failed to obtain position '" + positionId + "': " + e.getMessage());
            }
        } else {
            Gdx.app.error(TAG, "Map has no positions layer");
        }
        return position.scl(unitScale);
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
