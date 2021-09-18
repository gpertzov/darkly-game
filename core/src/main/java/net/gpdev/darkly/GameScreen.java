package net.gpdev.darkly;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import net.gpdev.darkly.actions.Attack;
import net.gpdev.darkly.actions.EntityAction;
import net.gpdev.darkly.actions.Move;
import net.gpdev.darkly.actors.EnemyEntity;
import net.gpdev.darkly.actors.GameEntity;
import net.gpdev.darkly.actors.PlayerEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

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
    private static final String ENEMY_START = "ENEMY";
    private static final String ENEMY_SPRITE = "enemy";
    private static final float ENEMY_SPEED = 3;
    private static final float UI_PADDING = 4;
    private static final String HEALTH_TEXT = "Health";
    private static final String BATTERY_TEXT = "Battery";
    private static final String BAR_ID = "bar";
    private static final String FILLBAR_ID = "fillbar";
    private static final Color AMBIENT_LIGHT = new Color(0.01f, 0.01f, 0.02f, 1.0f);

    private enum State {
        PLAYING,
        GAME_OVER
    }

    private final DarklyGame game;
    private final Queue<EntityAction> actionQueue = new Queue<>();
    private final DiceRoller diceRoller = new DiceRoller(TimeUtils.millis());
    private State state;
    private PlayerEntity player;
    private EnemyEntity enemy;

    private Skin uiSkin;
    private Stage uiStage;
    private TextureAtlas uiAtlas;
    private FillBarWidget batteryLevel;
    private FillBarWidget healthLevel;
    private Label messageText;

    private OrthographicCamera camera;
    private ExtendViewport viewport;
    private SpriteBatch batch;
    private GameLevel level;
    private OrthogonalTiledMapRenderer mapRenderer;
    private TextureAtlas sprites;
    private TextureAtlas lights;
    private FrameBuffer lightBuffer;
    private TextureRegion fboTexturRegion;
    private Texture attackSheet;
    private Animation<TextureRegion> attackAnim;

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
        final TextureRegion lightTexture = lights.findRegion(SPOTLIGHT);
        final TextureRegion flashlightTexture = lights.findRegion(FLASHLIGHT);

        // Load attack animation
        attackSheet = new Texture(Gdx.files.internal("art/attacks.png"));
        final TextureRegion[][] attackFrames = TextureRegion.split(attackSheet, TILE_SIZE, TILE_SIZE);
        attackAnim = new Animation<>(0.08f, attackFrames[1]);

        // Setup player entity
        final Vector2 position = level.getPosition(PLAYER_START);
        final TextureRegion playerSprite = sprites.findRegion(PLAYER_SPRITE);
        final Rectangle boundingBox = new Rectangle((TILE_SIZE - PLAYER_WIDTH) / 2.0f, (TILE_SIZE - PLAYER_HEIGHT) / 2.0f,
                PLAYER_WIDTH, PLAYER_HEIGHT / 2.0f);
        player = new PlayerEntity(new Sprite(playerSprite), position, PLAYER_SPEED, boundingBox, true, new Sprite(flashlightTexture));
        final Sprite heroLight = new Sprite(lightTexture);
        heroLight.setColor(Color.LIGHT_GRAY);
        player.addLight(SPOTLIGHT, new Light(heroLight, true));

        // Setup enemy entity
        final Vector2 enemyPosition = level.getPosition(ENEMY_START);
        final TextureRegion enemyTexture = sprites.findRegion(ENEMY_SPRITE);
        final Sprite enemySprite = new Sprite(enemyTexture);
        enemy = new EnemyEntity(enemySprite, enemyPosition, ENEMY_SPEED, new Rectangle(0, 0, 1, 1), false);
        final Sprite enemyLight = new Sprite(lightTexture);
        enemyLight.setScale(3);
        enemy.addLight(SPOTLIGHT, new Light(enemyLight, true));
        enemy.setLevel(level);

        // Add entities to level
        level.addEntity(player);
        level.addEntity(enemy);

        // Setup viewport
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);

        state = State.PLAYING;

        // Input processing // TODO - Extract to class
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(final int keycode) {
                if (keycode == Keys.LEFT) {
                    player.updateDirection(new Vector2(-1f, 0));
                    return true;
                }
                if (keycode == Keys.RIGHT) {
                    player.updateDirection(new Vector2(1f, 0));
                    return true;
                }
                if (keycode == Keys.UP) {
                    player.updateDirection(new Vector2(0, 1f));
                    return true;
                }
                if (keycode == Keys.DOWN) {
                    player.updateDirection(new Vector2(0, -1f));
                    return true;
                }
                if (keycode == Keys.SPACE) {
                    player.toggleFlashlight();
                }
                return super.keyDown(keycode);
            }

            @Override
            public boolean keyUp(final int keycode) {
                if (keycode == Keys.ESCAPE) {
                    Gdx.app.exit();
                }

                if (keycode == Keys.LEFT) {
                    player.updateDirection(new Vector2(1f, 0));
                    return true;
                }
                if (keycode == Keys.RIGHT) {
                    player.updateDirection(new Vector2(-1f, 0));
                    return true;
                }
                if (keycode == Keys.UP) {
                    player.updateDirection(new Vector2(0, -1f));
                }
                if (keycode == Keys.DOWN) {
                    player.updateDirection(new Vector2(0, 1f));
                }
                return super.keyUp(keycode);
            }
        });
    }

    private void setupUI() {
        uiSkin = new Skin(Gdx.files.internal("art/uiskin.json"));
        uiStage = new Stage(new ScreenViewport());
        uiAtlas = new TextureAtlas("art/ui.atlas");
        final AtlasRegion barRegion = uiAtlas.findRegion(BAR_ID);
        final AtlasRegion fillbarRegion = uiAtlas.findRegion(FILLBAR_ID);
        final float widgetWidth = barRegion.getRegionWidth();
        final float widgetHeight = barRegion.getRegionHeight();

        // Battery level widget
        batteryLevel = new FillBarWidget(barRegion, fillbarRegion, Color.BLUE);

        // Health level widget
        healthLevel = new FillBarWidget(barRegion, fillbarRegion, Color.RED);

        // Layout
        final Table table = new Table(uiSkin);
        table.defaults().grow().pad(UI_PADDING);
        table.add(HEALTH_TEXT);
        table.add(healthLevel).size(widgetWidth, widgetHeight);
        table.row();
        table.add(BATTERY_TEXT);
        table.add(batteryLevel).size(widgetWidth, widgetHeight);
        table.pack();

        messageText = new Label("", uiSkin);

        uiStage.addActor(messageText);
        uiStage.addActor(table);
    }

    @Override
    public void render(final float delta) {
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
        batteryLevel.setValue(player.getBatteryLevel());
        healthLevel.setValue(player.getHealthLevel());
        uiStage.act(delta);

        // Render to display //
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Reset blending function
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Center camera over player's position
        final Vector2 playerPosition = player.getPosition();
        camera.position.set(playerPosition.x + 0.5f, playerPosition.y + 0.5f, 0f);
        viewport.apply(false);

        // Project entity lights to screen coordinates
        player.projectLights(camera);
        enemy.projectLights(camera);

        // Render map
        mapRenderer.setView(camera);
        mapRenderer.render();

        // Render entities
        final Vector2 enemyPosition = enemy.getPosition();
        final float attackTime = enemy.getAttackTime();
        batch.begin();
        batch.draw(player.getSprite(), playerPosition.x, playerPosition.y, 1, 1);
        if (attackTime > 0 && !attackAnim.isAnimationFinished(attackTime)) {
            final TextureRegion attackFrame = attackAnim.getKeyFrame(attackTime, false);
            batch.draw(attackFrame, playerPosition.x, playerPosition.y, 1, 1);
        }
        batch.draw(enemy.getSprite(), enemyPosition.x, enemyPosition.y, 1, 1);
        batch.end();

        camera.setToOrtho(false);
        batch.setProjectionMatrix(camera.combined);

        // Render to lighting FBO //
        lightBuffer.begin();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        // Ambient light color
        Gdx.gl.glClearColor(AMBIENT_LIGHT.r, AMBIENT_LIGHT.g, AMBIENT_LIGHT.b, AMBIENT_LIGHT.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render light sources to FBO
        final Collection<Light> entityLights = new ArrayList<>();
        entityLights.addAll(player.getLights());
        entityLights.addAll(enemy.getLights());
        batch.begin();
        for (final Light light : entityLights) {
            if (light.isEnabled()) {
                final Sprite sprite = light.getSprite();
                sprite.draw(batch);
            }
        }
        batch.end();
        lightBuffer.end();

        // Blend lighting FBO
        batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ZERO);
        batch.begin();
        batch.draw(fboTexturRegion, 0, 0);
        batch.end();

        // Render UI
        uiStage.draw();
    }

    private void update(final float delta) {
        // Update player entity
        final EntityAction playerAction = player.update(delta);
        actionQueue.addLast(playerAction);

        // Update enemy entity
        final EntityAction enemyAction = enemy.update(delta);
        actionQueue.addLast(enemyAction);

        // Handle action queue
        executeEntityActions();

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

    private void executeEntityActions() {
        final Iterator<EntityAction> actionIterator = actionQueue.iterator();
        while (actionIterator.hasNext()) {
            final EntityAction action = actionIterator.next();
            actionIterator.remove();
            switch (action.getType()) {
                case MOVE: {
                    final Move move = (Move) action;
                    final GameEntity entity = move.getSource();
                    final Vector2 destination = move.getDestination();
                    // Detect collisions with level
                    if (entity.isCollidable()) {
                        final Rectangle boundingBox = entity.getBoundingBox();
                        boundingBox.setPosition(destination);
                        if (level.isCollision(boundingBox)) {
                            break;
                        }
                    }
                    // Move entity to destination
                    entity.setPosition(destination);
                }
                break;
                case ATTACK: {
                    final Attack attack = (Attack) action;
                    final int rollSum = diceRoller.rollSum(3, 6);
                    if (rollSum <= attack.getAttackSkill()) {
                        attack.getTarget().reactTo(new TriggeredEvent(TriggeredEvent.Type.HARM, attack.getMaxDamage()));
                    }
                }
                break;
                case IDLE: {

                }
                break;
                default:
                    throw new RuntimeException("Invalid entity action: " + action.getType());
            }
        }
    }

    private void endGame(final String message) {
        messageText.setText(message);
        messageText.setFontScale(2.0f);
        messageText.setPosition((uiStage.getWidth() - messageText.getPrefWidth()) / 2.0f,
                (uiStage.getHeight() - messageText.getPrefHeight()) / 2.0f, Align.center);
        state = State.GAME_OVER;
    }

    @Override
    public void resize(final int width, final int height) {
        viewport.update(width, height);
        uiStage.getViewport().update(width, height, true);

        // Create lighting FBO
        if (lightBuffer != null) {
            lightBuffer.dispose();
        }
        lightBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        fboTexturRegion = new TextureRegion(lightBuffer.getColorBufferTexture());
        fboTexturRegion.flip(false, true);
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
        attackSheet.dispose();
    }
}
