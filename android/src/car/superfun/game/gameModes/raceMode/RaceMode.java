package car.superfun.game.gameModes.raceMode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;
import com.instacart.library.truetime.TrueTime;

import car.superfun.game.GlobalVariables;
import car.superfun.game.googlePlayGameServices.GoogleGameServices;
import car.superfun.game.maps.TrackBuilder;
import car.superfun.game.maps.UserDataCreater;
import car.superfun.game.cars.LocalCarController;
import car.superfun.game.cars.OpponentCar;
import car.superfun.game.cars.OpponentCarController;
import car.superfun.game.gameModes.GameMode;
import car.superfun.game.scoreHandling.Leaderboard;
import car.superfun.game.scoreHandling.ScoreFormatter;
import car.superfun.game.states.GameStateManager;

public class RaceMode extends GameMode {

    public static final int GOAL_ENTITY = 0b1 << 8;
    public static final int CHECKPOINT_ENTITY = 0b1 << 9;
    public static final int TEST_ENTITY = 0b1 << 10;

    private final static String MAP_PATH = "tiled_maps/decentMap.tmx";

    private final Music raceSong;

    // Car textures
    private final static String[] TEXTURE_PATHS = new String[]{
            "racing-pack/PNG/Cars/car_yellow_5.png",
            "racing-pack/PNG/Cars/car_blue_5.png",
            "racing-pack/PNG/Cars/car_red_5.png",
            "racing-pack/PNG/Cars/car_green_5.png",
    };

    private Array<OpponentCar> opponentCars = new Array<>();

    // The car controlled by this phones owner
    private LocalRaceCar localRaceCar;

    private int amountOfCheckpoints;

    /**
     * @param googleGameServices
     * @param singlePlayer
     */
    public RaceMode(GoogleGameServices googleGameServices, boolean singlePlayer) {
        super(MAP_PATH, googleGameServices, singlePlayer);

        // Audio
        raceSong = Gdx.audio.newMusic(Gdx.files.internal("sounds/raceMode.ogg"));

        raceSong.setLooping(true);
        raceSong.setVolume(GlobalVariables.MUSIC_VOLUME);
        raceSong.play();

        world.setContactListener(new RaceContactListener());

        setUpMap();

        // place all participants cars in the map
        setStartPositions(startPositionCallback);

        // If testing mode is enabled testing layer will be built
        if (GlobalVariables.TESTING_MODE) {
            FixtureDef testDef = new FixtureDef();
            testDef.filter.categoryBits = TEST_ENTITY;
            testDef.filter.maskBits = GlobalVariables.PLAYER_ENTITY;
            testDef.isSensor = true;
            TrackBuilder.buildLayer(tiledMap, world, "test", testDef);
        }
    }

    /**
     * Load and set up tiledMap
     */
    protected void setUpMap() {
        super.setUpMap();
        // Set the goal line
        FixtureDef goalDef = new FixtureDef();
        goalDef.filter.categoryBits = GOAL_ENTITY;
        goalDef.filter.maskBits = GlobalVariables.PLAYER_ENTITY;
        goalDef.isSensor = true;

        TrackBuilder.buildLayer(tiledMap, world, "goal_line", goalDef);

        // Set the checkpoints around the map
        FixtureDef checkpointDef = new FixtureDef();
        checkpointDef.filter.categoryBits = CHECKPOINT_ENTITY;
        checkpointDef.filter.maskBits = GlobalVariables.PLAYER_ENTITY;
        checkpointDef.isSensor = true;

        // returns the amount of checkpoints for the given map
        amountOfCheckpoints = TrackBuilder.buildLayerWithUserData(tiledMap, world, "checkpoints", checkpointDef, new checkpointUserData()).size;
    }

    /**
     * Updates all the dynamic elements in the game, always propagating deltatime.
     * @param dt
     */
    @Override
    public void update(float dt) {
        camera.position.set(localRaceCar.getSpritePosition().add(localRaceCar.getVelocity().scl(10f)), 0);
        camera.up.set(localRaceCar.getDirectionVector(), 0);

        if (!googleGameServices.gameStarted() && !singlePlayer) {
            return;
        }
        for (OpponentCar car : opponentCars) {
            car.update(dt);
        }
        localRaceCar.update(dt);
        world.step(dt, 2, 1); // Using deltaTime

        localCarController.update();
        if (!singlePlayer) {
            googleGameServices.broadcastState(
                    localRaceCar.getVelocity(),
                    localRaceCar.getBodyPosition(),
                    localRaceCar.getAngle(),
                    localCarController.getForward(),
                    localCarController.getRotation());

        }
    }

    /**
     * Renders the objects relative to the camera. I.e. all objects in the gameworld.
     * This method is called by the superclass, GameMode.
     * @param sb
     * @param camera
     */
    @Override
    public void renderWithCamera(SpriteBatch sb, OrthographicCamera camera) {
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
        sb.begin();
        localRaceCar.render(sb);
        for (OpponentCar car : opponentCars) {
            car.render(sb);
        }
        sb.end();
    }

    /**
     * Renders the objects that have a static position on the screen. I.e. the HUD.
     * This method is called by the superclass, GameMode.
     * @param sb
     */
    @Override
    public void renderHud(SpriteBatch sb) {
        localCarController.render(sb);
    }

    /**
     * Dispose any objects that must be disposed manually.
     */
    @Override
    public void dispose() {
        raceSong.stop();
        raceSong.dispose();
    }

    /**
     * End the game and move to the Leaderboard state to display score.
     * This method is called by LocalRaceCar when the map is traversed for the last time.
     */
    @Override
    public void endGame() {
        int timeSinceStart = (int) ((TrueTime.now().getTime() - googleGameServices.getStartTime()) % 2147483648L);
        if (singlePlayer) {
            return;
        }
        googleGameServices.broadcastScore(timeSinceStart);
        scoreTable.put(googleGameServices.getLocalParticipant().getDisplayName(), timeSinceStart);
        Leaderboard leaderboard = new Leaderboard(scoreFormatter, false, scoreTable);
        GameStateManager.getInstance().set(leaderboard);
    }

    /**
     * Callback used to create the opponent cars and the localCar.
     */
    private SetStartPositionCallback startPositionCallback = new SetStartPositionCallback() {
        // Used to set different color to different players cars
        private int texturePathIndex = 0;

        @Override
        public void addOpponentCar(Vector2 position, OpponentCarController opponentCarController) {
            opponentCars.add(new OpponentCar(position, opponentCarController, world, TEXTURE_PATHS[texturePathIndex]));
            incrementTexturePath();
        }

        @Override
        public void addLocalCar(Vector2 position, LocalCarController localCarController, GameMode thisGameMode) {
            localRaceCar = new LocalRaceCar(position, localCarController, world, (RaceMode) thisGameMode, amountOfCheckpoints, TEXTURE_PATHS[texturePathIndex]);
            incrementTexturePath();
        }

        private void incrementTexturePath() {
            if (texturePathIndex < TEXTURE_PATHS.length) {
                texturePathIndex++;
            }
        }
    };

    /**
     * A callback for formatting score. Makes sure to format the RaceMode score as minutes, seconds and millis.
     */
    private ScoreFormatter scoreFormatter = new ScoreFormatter() {
        @Override
        public String formatScore(int ms) {
            String milliseconds = Integer.toString(ms%1000);
            while (milliseconds.length() < 3){
                milliseconds = "0" + milliseconds;
            }
            String seconds = Integer.toString((ms/1000)%60);
            while (seconds.length() < 2) {
                seconds = "0" + seconds;
            }
            int minutes = (ms/(1000*60))%60;
            String time;
            if (!(minutes == 0)) {
                time = minutes + ":" + seconds + "," + milliseconds;
            } else {
                time = seconds + "," + milliseconds;
            }
            return time;
        }

        @Override
        public String scoreString() {
            return "Time";
        }
    };

    /**
     * The purpose of this class is simply to let us pass a function to TrackBuilder.buildLayerWithUserData.
     * What will happen is that each checkpoint object in Box2d gets an unique id as its userdata.
     * This is important when making sure that the user has indeed traversed the whole map.
     */
    private class checkpointUserData implements UserDataCreater {
        private int id;

        public checkpointUserData() {
            id = 0;
        }

        public Object getUserData() {
            return id++;
        }
    }
}

