package car.superfun.game.gameModes;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;

import car.superfun.game.CarControls.CarController;
import car.superfun.game.TrackBuilder;
import car.superfun.game.physicalObjects.LocalCar;

/**
 * Created by matss on 21-Mar-18.
 */

public class GladiatorMode extends GameMode {

    TiledMap tiledMap;
    TiledMapRenderer tiledMapRenderer;

    private CarController carController;
    private LocalCar localCar;
    private int score;

    public GladiatorMode() {
        super();

        score = 5;
        carController = new CarController();
        localCar = new LocalCar(new Vector2(6000, 6000), carController, world);
        tiledMap = new TmxMapLoader().load("tiled_maps/gladiator.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        TrackBuilder.buildWalls(tiledMap, 100f, world);
        TrackBuilder.buildDeathZone(tiledMap, 100f, world);
    }

    @Override
    public void handleInput() {

    }

    public void beginContact(Contact contact) {

    }

    @Override
    public void update(float dt) {
        world.step(1f/60f, 6, 2);
        carController.update();
        localCar.update(dt);
        camera.position.set(localCar.getPosition(), 0);
        camera.position.set(localCar.getPosition().add(localCar.getVelocity().scl(10f)), 0);
        camera.up.set(localCar.getDirectionVector(), 0);
    }

    // Renders objects that had a static position in the gameworld. Is called by superclass
    @Override
    public void renderWithCamera(SpriteBatch sb, OrthographicCamera camera) {
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
        localCar.render(sb);
    }

    // Renders objects that have a static position on the screen. Is called by superclass
    @Override
    public void renderHud(SpriteBatch sb) {
        carController.render(sb);
    }

    @Override
    public void dispose() {
    }

    @Override
    public void endGame() {
        // TODO: Implement a proper way to exit the game
    }
}
