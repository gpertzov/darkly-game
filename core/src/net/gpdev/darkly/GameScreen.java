package net.gpdev.darkly;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.Collection;

import static com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import static net.gpdev.darkly.DarklyGame.FLASHLIGHT;

public class GameScreen extends ScreenAdapter {

    private static final String TAG = GameScreen.class.getSimpleName();

    private static final int VIEWPORT_WIDTH = 16;
    private static final int VIEWPORT_HEIGHT = 16;
    private static final int TILE_SIZE = 16;
    private static final float UNIT_SCALE = 1.0f / TILE_SIZE;
    private static final String PLAYER_START = "PLAYER";
    private static final String PLAYER_SPRITE = "player";
    private static final String SPOTLIGHT = "light";
    private static final float PLAYER_SPEED = 2;
    private static final float PLAYER_WIDTH = 14;
    private static final float PLAYER_HEIGHT = 16;
    private static final float UI_PADDING = 4;
    private static final String HEALTH_TEXT = "Health";
    private static final String BATTERY_TEXT = "Battery";
    private static final String BAR_ID = "bar";
    private static final String FILLBAR_ID = "fillbar";

    private enum State {
        PLAYING,
        GAME_OVER
    }

    private final DarklyGame game;
    private State state;
    private PlayerEntity player;

    private Skin uiSkin;
    private Stage uiStage;
    private TextureAtlas uiAtlas;
    private Image batteryLevel;
    private Image healthLevel;
    private Label messageText;

    private OrthographicCamera camera;
    private ExtendViewport viewport;
    private SpriteBatch batch;
    private GameLevel level;
    private OrthogonalTiledMapRenderer mapRenderer;
    private TextureAtlas sprites;
    private TextureAtlas lights;
    private FrameBuffer lightBuffer;
    private TextureRegion lightTexture;
    private TextureRegion flashlightTexture;
    private TextureRegion fboTexturRegion;

    public GameScreen(final DarklyGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Setup UI
        setupUI();

        // Init batch
        batch = new SpriteBatch();

        // Load level
        level = new GameLevel("maps/prototype.tmx", UNIT_SCALE);
        mapRenderer = new OrthogonalTiledMapRenderer(level.getMap(), level.getUnitScale(), batch);

        // Load sprites
        sprites = new TextureAtlas(Gdx.files.internal("art/sprites.atlas"));

        // Load lightmaps
        lights = new TextureAtlas(Gdx.files.internal("art/lights.atlas"));
        lightTexture = lights.findRegion(SPOTLIGHT);
        flashlightTexture = lights.findRegion(FLASHLIGHT);

        // Init player entity
        final Vector2 position = level.getPosition(PLAYER_START);
        final TextureRegion playerSprite = sprites.findRegion(PLAYER_SPRITE);
        final Rectangle boundingBox = new Rectangle((TILE_SIZE - PLAYER_WIDTH) / 2.0f, (TILE_SIZE - PLAYER_HEIGHT) / 2.0f,
                PLAYER_WIDTH, PLAYER_HEIGHT / 2.0f);
        player = new PlayerEntity(new Sprite(playerSprite), position, PLAYER_SPEED, boundingBox, new Sprite(flashlightTexture));
        final Sprite heroLight = new Sprite(lightTexture);
        heroLight.setColor(Color.LIGHT_GRAY);
        player.addLight(SPOTLIGHT, new Light(heroLight, true));

        // Setup viewport
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);

        state = State.PLAYING;

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
                    player.toggleFlashlight();
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

    private void setupUI() {
        uiSkin = new Skin(Gdx.files.internal("art/uiskin.json"));
        uiStage = new Stage(new ScreenViewport());
        uiAtlas = new TextureAtlas("art/ui.atlas");
        final TextureRegion barRegion = uiAtlas.findRegion(BAR_ID);
        final float widgetWidth = barRegion.getRegionWidth();
        final float widgetHeight = barRegion.getRegionHeight();
        final AtlasRegion fillbarRegion = uiAtlas.findRegion(FILLBAR_ID);

        // Battery bar widget
        final Image batteryBar = new Image(barRegion);
        batteryLevel = new Image(fillbarRegion);
        batteryLevel.setPosition(fillbarRegion.offsetX, fillbarRegion.offsetY);
        final WidgetGroup batteryWidget = new WidgetGroup();
        batteryWidget.addActor(batteryBar);
        batteryWidget.addActor(batteryLevel);
        batteryLevel.setColor(Color.BLUE);

        // Health bar widget
        final Image healthBar = new Image(barRegion);
        healthLevel = new Image(fillbarRegion);
        healthLevel.setPosition(fillbarRegion.offsetX, fillbarRegion.offsetY);
        final WidgetGroup healthWidget = new WidgetGroup();
        healthWidget.addActor(healthBar);
        healthWidget.addActor(healthLevel);
        healthLevel.setColor(Color.RED);

        // Layout
        final Table table = new Table(uiSkin);
        table.defaults().grow().pad(UI_PADDING);
        table.add(HEALTH_TEXT);
        table.add(healthWidget).size(widgetWidth, widgetHeight);
        table.row();
        table.add(BATTERY_TEXT);
        table.add(batteryWidget).size(widgetWidth, widgetHeight);
        table.pack();

        messageText = new Label("", uiSkin);

        uiStage.addActor(messageText);
        uiStage.addActor(table);
    }

    @Override
    public void render(float delta) {
        switch (state) {
            case PLAYING: {
                update(delta);
            }
            break;
            case GAME_OVER: {


            }
            break;
            default: {

            }
            break;
        }

        // Update UI
        batteryLevel.setWidth(batteryLevel.getPrefWidth() * player.getBatteryLevel());
        healthLevel.setWidth(healthLevel.getPrefWidth() * player.getHealthLevel());
        uiStage.act(delta);

        // Prepare lighting FBO //
        lightBuffer.begin();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        // Ambient light color
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.02f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render light sources to FBO
        final Collection<Light> playerLights = player.getLights();
        batch.begin();
        for (Light light : playerLights) {
            if (light.isEnabled()) {
                light.getSprite().draw(batch);
            }
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
        batch.draw(player.getSprite(), playerPosition.x, playerPosition.y, 1, 1);
        batch.end();

        // Blend lighting FBO
        camera.setToOrtho(false);
        batch.setProjectionMatrix(camera.combined);
        batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ZERO);
        batch.begin();
        batch.draw(fboTexturRegion, 0, 0);
        batch.end();

        // Render UI
        uiStage.draw();
    }

    private void update(float delta) {
        // Update player entity
        final Vector2 currentPlayerPosition = player.getPosition();
        player.update(delta);

        // Detect player's collisions with level
        final Rectangle boundingBox = player.getBoundingBox();
        if (level.isCollision(boundingBox)) {
            // Revert player's position update
            player.setPosition(currentPlayerPosition);
        }

        // Check level triggered events
        final Vector2 playerCenter = player.getPosition().add(0.5f, 0.5f);
        final TriggeredEvent event = level.consumeTrigger((int) playerCenter.x, (int) playerCenter.y);
        if (!consumeGameEvent(event)) {
            player.reactTo(event);
        }

        if (player.getHealthLevel() <= 0) {
            endGame("Too bad, Try again");
        }
    }

    private boolean consumeGameEvent(final TriggeredEvent event) {
        if (event == null) {
            return true;
        }

        if (event.getType() == TriggeredEvent.Type.WIN) {
            endGame("You Win!");
            return true;
        }

        return false;
    }

    private void endGame(final String message) {
        messageText.setText(message);
        messageText.setFontScale(2.0f);
        messageText.setPosition((uiStage.getWidth() - messageText.getPrefWidth()) / 2.0f,
                (uiStage.getHeight() - messageText.getPrefHeight()) / 2.0f, Align.center);
        state = State.GAME_OVER;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        uiStage.getViewport().update(width, height, true);

        // Create lighting FBO
        if (lightBuffer != null) {
            lightBuffer.dispose();
        }
        lightBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        fboTexturRegion = new TextureRegion(lightBuffer.getColorBufferTexture());
        fboTexturRegion.flip(false, true);

        // Notify entities
        player.onLightBufferChanged(lightBuffer);
    }

    @Override
    public void dispose() {
        uiStage.dispose();
        uiSkin.dispose();
        lightBuffer.dispose();
        lights.dispose();
        sprites.dispose();
        uiAtlas.dispose();
        level.dispose();
        batch.dispose();
    }
}
