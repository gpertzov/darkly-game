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
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import static com.badlogic.gdx.math.MathUtils.clamp;

public class GameScreen extends ScreenAdapter {

    private static final String TAG = GameScreen.class.getSimpleName();

    private static final int VIEWPORT_WIDTH = 16;
    private static final int VIEWPORT_HEIGHT = 16;
    private static final float UNIT_SCALE = 1 / 16f;
    private static final String POSITIONS_LAYER = "positions";
    private static final String STRUCTURES_LAYER = "structures";
    private static final String PLAYER_START = "PLAYER";
    private static final float PLAYER_SPEED = 2;

    private final DarklyGame game;

    private OrthographicCamera camera;
    private ExtendViewport viewport;
    private SpriteBatch batch;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private TextureAtlas sprites;

    private TiledMapTileLayer structuresLayer;
    private int mapWidth = 0;
    private int mapHeight = 0;

    // Player properties // TODO - Extract to class
    private Vector2 playerPosition;
    private Vector2 playerVelocity;
    private TextureRegion playerFrame;


    public GameScreen(final DarklyGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Init batch
        batch = new SpriteBatch();

        // Load map
        map = new TmxMapLoader().load("maps/prototype.tmx");
        structuresLayer = (TiledMapTileLayer) map.getLayers().get(STRUCTURES_LAYER);
        mapWidth = structuresLayer.getWidth();
        mapHeight = structuresLayer.getHeight();

        mapRenderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE, batch);

        // Setup viewport
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);

        // Obtain player's start position
        playerPosition = new Vector2(0, 0);
        MapLayer positionsLayer = map.getLayers().get(POSITIONS_LAYER);
        if (positionsLayer != null) {
            RectangleMapObject playerStartRect = (RectangleMapObject) positionsLayer.getObjects().get(PLAYER_START);
            if (playerStartRect != null) {
                playerStartRect.getRectangle().getPosition(playerPosition);
            }
        }
        playerPosition.scl(UNIT_SCALE);
        playerVelocity = new Vector2(0, 0);

        // Load sprites
        sprites = new TextureAtlas(Gdx.files.internal("art/sprites.atlas"));
        playerFrame = sprites.findRegion("player");

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

        // Update player's position // TODO - Collision Detection
        final Vector2 velocity = playerVelocity.cpy().scl(delta);
        final Vector2 nextPosition = playerPosition.cpy().add(velocity);
        nextPosition.x = clamp(nextPosition.x, 0, mapWidth - 1);
        nextPosition.y = clamp(nextPosition.y, 0, mapHeight - 1);
        playerPosition = nextPosition;

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
