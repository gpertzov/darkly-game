package net.gpdev.darkly;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
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
    private static final int TILE_SIZE = 16;
    private static final float UNIT_SCALE = 1.0f / TILE_SIZE;
    private static final String POSITIONS_LAYER = "positions";
    private static final String COLLISION_LAYER = "collision";
    private static final String PLAYER_START = "PLAYER";
    private static final String PLAYER_SPRITE = "player";
    private static final float PLAYER_SPEED = 2;
    private static final float PLAYER_WIDTH = 14;
    private static final float PLAYER_HEIGHT = 16;

    private final DarklyGame game;
    private GameEntity player;

    private OrthographicCamera camera;
    private ExtendViewport viewport;
    private SpriteBatch batch;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private MapLayer collisionLayer;
    private TextureAtlas sprites;
    private TextureAtlas lights;
    private FrameBuffer lightBuffer;
    private TextureRegion lightTexture;
    private TextureRegion flashlightTexture;
    private Sprite heroLight;
    private Sprite flashlight;
    private boolean isFlashlightOn = false;
    private float flashlightRotation = 0;
    private TextureRegion fboTexturRegion;

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

        // Load lightmaps
        lights = new TextureAtlas(Gdx.files.internal("art/lights.atlas"));
        lightTexture = lights.findRegion("light");
        flashlightTexture = lights.findRegion("flashlight");
        heroLight = new Sprite(lightTexture);
        flashlight = new Sprite(flashlightTexture);

        // Setup viewport
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);

        // Init player entity
        final Vector2 position = getMapPosition(PLAYER_START).scl(UNIT_SCALE);
        final TextureRegion sprite = sprites.findRegion(PLAYER_SPRITE);
        final Rectangle boundingBox = new Rectangle((TILE_SIZE - PLAYER_WIDTH) / 2.0f, (TILE_SIZE - PLAYER_HEIGHT) / 2.0f,
                PLAYER_WIDTH, PLAYER_HEIGHT / 2.0f);
        player = new GameEntity(sprite, position, PLAYER_SPEED, boundingBox);

        // Input processing // TODO - Extract to class
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Keys.LEFT) {
                    player.updateDirection(-1f, 0);
                    return true;
                }
                if (keycode == Keys.RIGHT) {
                    player.updateDirection(1f, 0);
                    return true;
                }
                if (keycode == Keys.UP) {
                    player.updateDirection(0, 1f);
                    return true;
                }
                if (keycode == Keys.DOWN) {
                    player.updateDirection(0, -1f);
                    return true;
                }
                if (keycode == Keys.SPACE) {
                    isFlashlightOn = !isFlashlightOn;
                }
                return super.keyDown(keycode);
            }

            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Keys.ESCAPE) {
                    Gdx.app.exit();
                }

                if (keycode == Keys.LEFT) {
                    player.updateDirection(1f, 0);
                    return true;
                }
                if (keycode == Keys.RIGHT) {
                    player.updateDirection(-1f, 0);
                    return true;
                }
                if (keycode == Keys.UP) {
                    player.updateDirection(0, -1f);
                }
                if (keycode == Keys.DOWN) {
                    player.updateDirection(0, 1f);
                }
                return super.keyUp(keycode);
            }
        });
    }

    @Override
    public void render(float delta) {

        // Detect collisions with map
        final Vector2 playerVelocity = player.getVelocity();
        final Vector2 velocity = playerVelocity.scl(delta);
        final Vector2 nextPosition = player.getPosition().add(velocity);
        final boolean isCollision = isCollision(nextPosition);

        // Update player's position
        if (!isCollision) {
            player.setPosition(nextPosition);
        }

        // Compute flashlight rotation
        if (!playerVelocity.isZero()) {
            flashlightRotation = playerVelocity.angle() - 90;
        }

        // Prepare lighting FBO //
        lightBuffer.begin();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        // Ambient light color
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render light sources to FBO
        batch.begin();
        heroLight.draw(batch);
        if (isFlashlightOn) {
            flashlight.setRotation(flashlightRotation);
            flashlight.draw(batch);
        }
        batch.end();

        lightBuffer.end();

        // Render to display //
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Center camera over player's position
        final Vector2 playerPosition = player.getPosition();
        camera.position.set(playerPosition.x + 0.5f, playerPosition.y + 0.5f, 0f);
        viewport.apply(false);

        // Reset blending function
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Render map
        mapRenderer.setView(camera);
        mapRenderer.render();

        // Render player
        batch.begin();
        batch.draw(player.getFrame(), playerPosition.x, playerPosition.y, 1, 1);
        batch.end();

        // Blend lighting FBO
        camera.setToOrtho(false);
        batch.setProjectionMatrix(camera.combined);
        batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ZERO);
        batch.begin();
        batch.draw(fboTexturRegion, 0, 0);
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
        if (collisionLayer != null) {
            final MapObjects objects = collisionLayer.getObjects();
            for (final MapObject object : objects) {
                if (object instanceof RectangleMapObject) {
                    final Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    if (player.isCollision(nextPosition.x / UNIT_SCALE, nextPosition.y / UNIT_SCALE, rect)) {
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

        // Create lighting FBO
        if (lightBuffer != null) {
            lightBuffer.dispose();
        }
        lightBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        fboTexturRegion = new TextureRegion(lightBuffer.getColorBufferTexture());
        fboTexturRegion.flip(false, true);

        // Pre-compute light sources positions
        heroLight.setPosition((lightBuffer.getWidth() - lightTexture.getRegionWidth()) / 2.0f,
                (lightBuffer.getHeight() - lightTexture.getRegionHeight()) / 2.0f);
        flashlight.setPosition((lightBuffer.getWidth() - flashlightTexture.getRegionWidth()) / 2.0f,
                (lightBuffer.getHeight() - flashlightTexture.getRegionHeight()) / 2.0f);
    }

    @Override
    public void dispose() {
        lightBuffer.dispose();
        lights.dispose();
        sprites.dispose();
        map.dispose();
        batch.dispose();
    }
}
