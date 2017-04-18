package net.gpdev.darkly;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class GameScreen extends ScreenAdapter {

    private static final int VIEWPORT_WIDTH = 16;
    private static final int VIEWPORT_HEIGHT = 16;
    private static final float UNIT_SCALE = 1 / 16f;
    private static final String POSITIONS_LAYER = "positions";
    private static final String PLAYER_START = "PLAYER";

    private final DarklyGame game;

    private OrthographicCamera camera;
    private ExtendViewport viewport;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private TextureAtlas sprites;

    private Vector2 playerPosition;
    private TextureRegion playerFrame;


    public GameScreen(final DarklyGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Load map
        map = new TmxMapLoader().load("maps/prototype.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE);

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

        // Load sprites
        sprites = new TextureAtlas(Gdx.files.internal("art/sprites.atlas"));
        playerFrame = sprites.findRegion("player");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Position camera over player position
        camera.position.set(playerPosition.x, playerPosition.y, 0f);
        viewport.apply(false);

        // Render map
        mapRenderer.setView(camera);
        mapRenderer.render();

        // Render player
        final Batch batch = mapRenderer.getBatch();
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
    }
}
