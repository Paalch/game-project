package car.superfun.game.gameModes.raceMode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import java.util.Arrays;

import car.superfun.game.GlobalVariables;
import car.superfun.game.cars.LocalCarController;
import car.superfun.game.cars.LocalCar;

public class LocalRaceCar extends LocalCar {
    boolean[] passedCheckpoints;
    int completedRounds;

    private boolean doRotate;
    private float rotateBy;
    private RaceMode raceMode;

    /**
     *
     * @param position
     * @param localCarController
     * @param world
     * @param raceMode
     * @param amountOfCheckpoints
     * @param texturePath
     */
    public LocalRaceCar(Vector2 position, LocalCarController localCarController, World world,  RaceMode raceMode, int amountOfCheckpoints, String texturePath) {
        super(position, localCarController, world, texturePath);
        this.raceMode = raceMode;
        passedCheckpoints = new boolean[amountOfCheckpoints];
        Arrays.fill(passedCheckpoints, Boolean.FALSE);
        completedRounds = 0;
        doRotate = false;
        rotateBy = 0f;
    }

    /**
     *
     * @param checkpointId
     */
    public void passCheckpoint(int checkpointId) {
        passedCheckpoints[checkpointId] = true;
    }

    /**
     * Increments the counter counting how many rounds have been traversed iff all checkpoints are passed.
     */
    public void passGoal() {
        if (allCheckpointsPassed()) {
            completedRounds++;
            Arrays.fill(passedCheckpoints, Boolean.FALSE);
            if (completedRounds == 3) {
                raceMode.endGame();
            }
        }
    }

    /**
     * Only overrides the methods so that testing mode can be used.
     * In testing mode the car rotates when reaching certain sensors on the map,
     * which is used when making the car drive by itself.
     * @param dt
     */
    @Override
    public void update(float dt) {
        super.update(dt);
        if (GlobalVariables.TESTING_MODE && doRotate) {
            body.setTransform(body.getPosition(), body.getTransform().getRotation() + rotateBy);
            doRotate = false;
            rotateBy = 0f;
        }
    }

    private boolean allCheckpointsPassed() {
        for (boolean checkpointPassed : passedCheckpoints) {
            if (!checkpointPassed) {
                return false;
            }
        }
        return true;
    }

    /**
     * Only used in testing mode
     * @param angle
     */
    public void rotateBy(float angle) {
        if (!GlobalVariables.TESTING_MODE) {
            return;
        }
        doRotate = true;
        rotateBy = angle;
    }
}
