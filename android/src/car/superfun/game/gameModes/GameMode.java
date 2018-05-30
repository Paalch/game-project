package car.superfun.game.gameModes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import car.superfun.game.GlobalVariables;
import car.superfun.game.googlePlayGameServices.GoogleGameServices;
import car.superfun.game.maps.MapLoader;
import car.superfun.game.maps.TrackBuilder;
import car.superfun.game.cars.CarController;
import car.superfun.game.cars.LocalCarController;
import car.superfun.game.cars.OpponentCarController;
import car.superfun.game.scoreHandling.HandlesScore;
import car.superfun.game.states.State;


public abstract class GameMode extends State implements HandlesScore {

    protected OrthographicCamera camera;
    protected SpriteBatch camBatch;

    protected Array<Vector2> startPositions;

    protected LocalCarController localCarController;

    protected boolean singlePlayer;

    protected World world;

    protected TiledMap tiledMap;
    protected TiledMapRenderer tiledMapRenderer;

    protected GoogleGameServices googleGameServices;

    protected Map<String, Integer> scoreTable = new HashMap<>();

    /**
     * Constructor
     * @param mapPath
     * @param googleGameServices
     * @param isSinglePlayer
     */
    protected GameMode(String mapPath, GoogleGameServices googleGameServices, boolean isSinglePlayer) {
        this.googleGameServices = googleGameServices;
        this.singlePlayer = isSinglePlayer;

        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.zoom = camera.zoom * 1.4f * (3 / Gdx.graphics.getDensity());
        camera.update();

        /* Using this specific SpriteBatch constructor is supposedly forcing VBO mode for mesh rendering,
           which seems to have a huge impact on performance. */
        ShaderProgram shader = SpriteBatch.createDefaultShader();
        camBatch = new SpriteBatch(1024, shader);

        world = new World(new Vector2(0, 0), true);            Gdx.app.log("You won!!!", ".. but you also died.. shit happens!");

        tiledMap = new MapLoader(world).load(mapPath);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, camBatch);

        startPositions = TrackBuilder.getPoints(tiledMap, "starting_points");

        localCarController = new LocalCarController(googleGameServices.getLocalParticipant());

        setUpMap();

        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());

        googleGameServices.readyToStart(isSinglePlayer);
    }

    /**
     * Sets the cars start positions
     * @param callback
     */
    protected void setStartPositions(SetStartPositionCallback callback) {
        ArrayList<CarController> carControllers = new ArrayList<>();
        for (CarController carController : googleGameServices.getOpponentCarControllers()) {
            carControllers.add(carController);
        }

        carControllers.add(localCarController);
        Collections.sort(carControllers);

        for (int i = 0; i < carControllers.size(); i++) {
            if (carControllers.get(i) instanceof OpponentCarController) {
                callback.addOpponentCar(startPositions.get(i), (OpponentCarController) carControllers.get(i));
            } else if (carControllers.get(i) instanceof LocalCarController) {
                callback.addLocalCar(startPositions.get(i), localCarController, this);
            }
        }
    }

    /**
     * Initiates the start positions
     * @param layer
     */
    protected void initiateStartPositions(String layer) {
        startPositions = TrackBuilder.getPoints(tiledMap, layer);
    }

    public void render(SpriteBatch sb) {
        // Update the camera position
        camera.update();

        // render based on the camera position
        camBatch.setProjectionMatrix(camera.combined);
        this.renderWithCamera(camBatch, camera);
        this.renderHud(sb);
    }

    protected void setUpMap() {
        // Set the normal walls
        FixtureDef wallDef = new FixtureDef();
        wallDef.filter.categoryBits = GlobalVariables.WALL_ENTITY;
        wallDef.filter.maskBits = GlobalVariables.PLAYER_ENTITY | GlobalVariables.OPPONENT_ENTITY;
        TrackBuilder.buildLayer(tiledMap, world, "walls", wallDef);
    }

    @Override
    public void handleScore(String senderName, int score) {
        scoreTable.put(senderName, score);
    }

    protected abstract void renderWithCamera(SpriteBatch sb, OrthographicCamera camera);

    protected abstract void renderHud(SpriteBatch sb);

    public abstract void endGame();

    public interface SetStartPositionCallback {
        void addOpponentCar(Vector2 position, OpponentCarController opponentCarController);
        void addLocalCar(Vector2 position, LocalCarController localCarController, GameMode thisGameMode);
    }
}
