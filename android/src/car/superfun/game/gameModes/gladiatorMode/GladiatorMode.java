package car.superfun.game.gameModes.gladiatorMode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;

import car.superfun.game.GlobalVariables;
import car.superfun.game.googlePlayGameServices.GoogleGameServices;
import car.superfun.game.maps.TrackBuilder;
import car.superfun.game.cars.LocalCarController;
import car.superfun.game.cars.OpponentCar;
import car.superfun.game.cars.OpponentCarController;
import car.superfun.game.gameModes.GameMode;
import car.superfun.game.scoreHandling.Leaderboard;
import car.superfun.game.scoreHandling.ScoreFormatter;
import car.superfun.game.states.GameStateManager;



public class GladiatorMode extends GameMode {


    // Path to map
    private final static String MAP_PATH = "tiled_maps/gladiator.tmx";

    // Car textures
    private final static String[] TEXTURE_PATHS = new String[]{
            "racing-pack/PNG/Cars/car_black_5.png",
            "racing-pack/PNG/Cars/car_blue_5.png",
            "racing-pack/PNG/Cars/car_red_5.png",
            "racing-pack/PNG/Cars/car_yellow_5.png",
    };

    // Filters
    static final short DEATH_ENTITY = 0b1 << 8;
    static final short BOOST_ZONE = 0b1 << 9;

    // Music and sounds
    private final Sound dustWallCrash;
    private final Music gladiatorSong;

    private Array<OpponentCar> opponentCars = new Array<OpponentCar>();

    private LocalGladiatorCar localCar;

    private int score = 5;
    /*
    Boost is not yet integrated.
    private float boost = 10;
    */
    private boolean endGameNextUpdate = false;

    /**
     * Constructor
     * @param googleGameServices
     * @param isSinglePlayer
     */
    public GladiatorMode(GoogleGameServices googleGameServices, boolean isSinglePlayer) {
        super(MAP_PATH, googleGameServices, isSinglePlayer);

        // Audio
        dustWallCrash = Gdx.audio.newSound(Gdx.files.internal("sounds/crash_in_dirt_wall.ogg"));
        gladiatorSong = Gdx.audio.newMusic(Gdx.files.internal("sounds/gladiatorMode.ogg"));
        gladiatorSong.setLooping(true);
        gladiatorSong.setVolume(GlobalVariables.MUSIC_VOLUME);
        gladiatorSong.play();

        world.setContactListener(new GladiatorContactListener());

        // Build the map with collision lines and spawn points.
        setUpMap();

        // place all participants cars in the map
        setStartPositions(setStartPositionsCallback);
    }

    /**
     * Set the car on its place in the map and gives it a unique color.
     */
    private SetStartPositionCallback setStartPositionsCallback = new SetStartPositionCallback() {
        // Used to set different color to different players cars
        private int texturePathIndex = 0;

        @Override
        public void addOpponentCar(Vector2 position, OpponentCarController opponentCarController) {
            opponentCars.add(new OpponentCar(position, opponentCarController, world, TEXTURE_PATHS[texturePathIndex]));
            incrementTexturePath();
        }

        @Override
        public void addLocalCar(Vector2 position, LocalCarController localCarController, GameMode thisGameMode) {
            localCar = new LocalGladiatorCar((GladiatorMode) thisGameMode, position, localCarController, world, score, dustWallCrash, TEXTURE_PATHS[texturePathIndex]);
            incrementTexturePath();
        }

        private void incrementTexturePath() {
            if (texturePathIndex < TEXTURE_PATHS.length) {
                texturePathIndex++;
            }
        }
    };


    /**
     * Build the maps fixtures
     */
    protected void setUpMap() {
        super.setUpMap();

        // Set the death walls
        FixtureDef deathZoneDef = new FixtureDef();
        deathZoneDef.restitution = 2f;
        deathZoneDef.filter.categoryBits = GladiatorMode.DEATH_ENTITY;
        deathZoneDef.filter.maskBits = GlobalVariables.PLAYER_ENTITY;
        TrackBuilder.buildLayer(tiledMap, world, "dirt_barrier", deathZoneDef);

        // Set the boost charging zone
        FixtureDef boostZone = new FixtureDef();
        boostZone.isSensor = true;
        boostZone.filter.categoryBits = BOOST_ZONE;
        boostZone.filter.maskBits = GlobalVariables.PLAYER_ENTITY;
        TrackBuilder.buildLayer(tiledMap, world, "boost_zone", boostZone);


        // Get starting positions for map
        initiateStartPositions("starting_points");
    }


    /**
     * Update
     * @param dt
     */
    @Override
    public void update(float dt) {
        if (!googleGameServices.gameStarted() && !singlePlayer) {
            return;
        }

        // Updates the opponents.
        for (OpponentCar car : opponentCars) {
            car.update(dt);
        }
        localCar.update(dt);

        world.step(dt, 2, 1); // Using deltaTime

        // Update the camera
        camera.position.set(localCar.getSpritePosition(), 0);
        camera.position.set(localCar.getSpritePosition().add(localCar.getVelocity().scl(10f)), 0);
        camera.up.set(localCar.getDirectionVector(), 0);

        // Update local car and broadcast if not singleplayer.
        localCarController.update();
        if (!singlePlayer) {
            googleGameServices.broadcastState(
                    localCar.getVelocity(),
                    localCar.getBodyPosition(),
                    localCar.getAngle(),
                    localCarController.getForward(),
                    localCarController.getRotation());
        }

        // Check if game is finished.
        if (endGameNextUpdate) {
            endGame();
        }
    }

    /**
     * Renders objects that had a static position in the gameworld. Is called by superclass
     * @param sb
     * @param camera
     */
    @Override
    public void renderWithCamera(SpriteBatch sb, OrthographicCamera camera) {
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
        sb.begin();
        localCar.render(sb);
        for (OpponentCar car : opponentCars) {
            car.render(sb);
        }
        sb.end();
    }

    /**
     * Renders objects that have a static position on the screen. Is called by superclass
     * @param sb
     */
    @Override
    public void renderHud(SpriteBatch sb) {
        localCarController.render(sb);
    }

    /**
     * Disposes the music when game mode is over.
     */
    @Override
    public void dispose() {
        gladiatorSong.stop();
        gladiatorSong.dispose();
    }

    public void setScore(int score) {
        this.score = score;
    }

    /**
     * End the game
     */
    @Override
    public void endGame() {
        if (singlePlayer) {
            return;
        }
        googleGameServices.broadcastScore(score);
        scoreTable.put(googleGameServices.getLocalParticipant().getDisplayName(), score);
        Leaderboard leaderboard = new Leaderboard(scoreFormatter, true, scoreTable);
        GameStateManager.getInstance().set(leaderboard);
        this.dispose();
    }

    /**
     * Handles the score
     * @param senderName
     * @param score
     */
    @Override
    public void handleScore(String senderName, int score) {
        super.handleScore(senderName, score);
        endGameNextUpdate = true;
    }

    /**
     *  A callback for formatting score. Makes sure to format the GladiatorMode score as lives left
     */
    private ScoreFormatter scoreFormatter = new ScoreFormatter() {
        @Override
        public String formatScore(int livesLeft) {
            return "" + livesLeft;
        }

        @Override
        public String scoreString() {
            return "Lives left";
        }
    };
}
