package net.gpdev.darkly;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
    private static final String SPAWN_LAYER = "spawn";
    private static final String PLAYER_SPAWN = "PLAYER";

    private final DarklyGame game;

    private OrthographicCamera camera;
    private ExtendViewport viewport;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    private Vector2 playerPosition;

    public GameScreen(final DarklyGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Load map
        map = new TmxMapLoader().load("maps/prototype.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE);

        // Setup viewport
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);

        // Obtain player's start position
        playerPosition = new Vector2(0, 0);
        MapLayer spawnLayer = map.getLayers().get(SPAWN_LAYER);
        if (spawnLayer != null) {
            RectangleMapObject playerStartRect = (RectangleMapObject) spawnLayer.getObjects().get(PLAYER_SPAWN);
            if (playerStartRect != null) {
                playerStartRect.getRectangle().getPosition(playerPosition);
            }
        }
        playerPosition.scl(UNIT_SCALE);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Position camera over player position
        camera.position.set(playerPosition.x, playerPosition.y, 0f);
        viewport.apply(false);

        // Render
        renderer.setView(camera);
        renderer.render();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        map.dispose();
    }
}
