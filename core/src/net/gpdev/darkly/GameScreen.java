package net.gpdev.darkly;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class GameScreen extends ScreenAdapter {

    private static final String TAG = GameScreen.class.getSimpleName();

    private static final int VIEWPORT_WIDTH = 16;
    private static final int VIEWPORT_HEIGHT = 16;
    private static final float UNIT_SCALE = 1 / 16f;
    private static final String POSITIONS_LAYER = "positions";
    private static final String COLLISION_LAYER = "collision";
    private static final String PLAYER_START = "PLAYER";
    private static final float PLAYER_SPEED = 2;
    private static final float PLAYER_WIDTH = 14;
    private static final float PLAYER_HEIGHT = 16;

    private final DarklyGame game;

    private OrthographicCamera camera;
    private ExtendViewport viewport;
    private SpriteBatch batch;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private MapLayer collisionLayer;
    private TextureAtlas sprites;

    // Player properties // TODO - Extract to class
    private Vector2 playerPosition;
    private Vector2 playerVelocity;
    private TextureRegion playerFrame;
    private Rectangle playerBoundingBox;


    public GameScreen(final DarklyGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Init batch
        batch = new SpriteBatch();

        // Load map
        map = new TmxMapLoader().load("maps/prototype.tmx");
        collisionLayer = map.getLayers().get(COLLISION_LAYER);
        mapRenderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE, batch);

        // Load sprites
        sprites = new TextureAtlas(Gdx.files.internal("art/sprites.atlas"));

        // Setup viewport
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);

        // Init player entity
        playerPosition = getMapPosition(PLAYER_START);
        playerPosition.scl(UNIT_SCALE);
        playerVelocity = new Vector2(0, 0);
        playerFrame = sprites.findRegion("player");
        playerBoundingBox = new Rectangle(0, 0, PLAYER_WIDTH, PLAYER_HEIGHT / 2.0f);

        // Input processing // TODO - Extract to class
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Keys.LEFT) {
                    playerVelocity.sub(PLAYER_SPEED, 0);
                    return true;
                }
                if (keycode == Keys.RIGHT) {
                    playerVelocity.add(PLAYER_SPEED, 0);
                    return true;
                }
                if (keycode == Keys.UP) {
                    playerVelocity.add(0, PLAYER_SPEED);
                    return true;
                }
                if (keycode == Keys.DOWN) {
                    playerVelocity.sub(0, PLAYER_SPEED);
                    return true;
                }
                return super.keyDown(keycode);
            }

            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Keys.ESCAPE) {
                    Gdx.app.exit();
                }

                if (keycode == Keys.LEFT) {
                    playerVelocity.add(PLAYER_SPEED, 0);
                    return true;
                }
                if (keycode == Keys.RIGHT) {
                    playerVelocity.sub(PLAYER_SPEED, 0);
                    return true;
                }
                if (keycode == Keys.UP) {
                    playerVelocity.sub(0, PLAYER_SPEED);
                }
                if (keycode == Keys.DOWN) {
                    playerVelocity.add(0, PLAYER_SPEED);
                }
                return super.keyUp(keycode);
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Detect collisions with map
        final Vector2 velocity = playerVelocity.cpy().scl(delta);
        final Vector2 nextPosition = playerPosition.cpy().add(velocity);
        final boolean isCollision = isCollision(nextPosition);

        // Update player's position
        if (!isCollision) {
            playerPosition = nextPosition;
        }

        // Position camera over player position
        camera.position.set(playerPosition.x, playerPosition.y, 0f);
        viewport.apply(false);

        // Render map
        mapRenderer.setView(camera);
        mapRenderer.render();

        // Render player
        batch.begin();
        batch.draw(playerFrame, playerPosition.x, playerPosition.y, 1, 1);
        batch.end();
    }

    private Vector2 getMapPosition(final String positionId) {
        Vector2 position = new Vector2(0, 0);
        MapLayer positionsLayer = map.getLayers().get(POSITIONS_LAYER);
        if (positionsLayer != null) {
            try {
                final MapObjects objects = positionsLayer.getObjects();
                final RectangleMapObject positionRect = (RectangleMapObject) objects.get(positionId);
                positionRect.getRectangle().getPosition(position);
            } catch (Exception e) {
                Gdx.app.error(TAG, "Failed to obtain position " + positionId);
            }
        } else {
            Gdx.app.debug(TAG, "Map has no positions layer");
        }
        return position;
    }

    private boolean isCollision(Vector2 nextPosition) {
        final Rectangle boundingBox = new Rectangle(nextPosition.x / UNIT_SCALE, nextPosition.y / UNIT_SCALE,
                playerBoundingBox.width, playerBoundingBox.height);
        if (collisionLayer != null) {
            final MapObjects objects = collisionLayer.getObjects();
            for (final MapObject object : objects) {
                if (object instanceof RectangleMapObject) {
                    final Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    if (boundingBox.overlaps(rect)) {
                        return true;
                    }
                } else {
                    Gdx.app.debug(TAG, "Unsupported collision object type");
                }
            }
        }
        return false;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        sprites.dispose();
        map.dispose();
        batch.dispose();
    }
}
