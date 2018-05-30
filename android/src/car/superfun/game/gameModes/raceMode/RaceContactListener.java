package car.superfun.game.gameModes.raceMode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import car.superfun.game.GlobalVariables;

public class RaceContactListener implements ContactListener {

    /**
     * Called by Box2D each time a collision is started
     * @param contact
     */
    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        int bothCategoryBits = (fixtureA.getFilterData().categoryBits | fixtureB.getFilterData().categoryBits);

        // stop here if not one and only one LocalCar is involved
        if (((fixtureA.getFilterData().categoryBits ^ fixtureB.getFilterData().categoryBits) & GlobalVariables.PLAYER_ENTITY) != GlobalVariables.PLAYER_ENTITY) {
            return;
        }

        // Check if at least one of the fixtures is a goal
        if ((bothCategoryBits & RaceMode.GOAL_ENTITY) == RaceMode.GOAL_ENTITY) {
            beginGoalContact(contact);
        }

        // Check if at least one of the fixtures is a checkpoint
        if ((bothCategoryBits & RaceMode.CHECKPOINT_ENTITY) == RaceMode.CHECKPOINT_ENTITY) {
            beginCheckpointContact(contact);
        }

        if ((bothCategoryBits & 0b10000) == 0b10000) {
            LocalRaceCar localRaceCar = (fixtureA.getUserData() instanceof LocalRaceCar) ? (LocalRaceCar) fixtureA.getUserData() : (LocalRaceCar) fixtureB.getUserData();
            localRaceCar.getBody().applyForceToCenter(localRaceCar.getDirectionVector().scl(100), true);
        }
    }

    /**
     * Called by Box2D each time a collision ends.
     * @param contact
     */
    @Override
    public void endContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        int bothCategoryBits = (fixtureA.getFilterData().categoryBits | fixtureB.getFilterData().categoryBits);

        // stop here if not one and only one LocalCar is involved
        if (((fixtureA.getFilterData().categoryBits ^ fixtureB.getFilterData().categoryBits) & GlobalVariables.PLAYER_ENTITY) != GlobalVariables.PLAYER_ENTITY) {
            return;
        }

        if ((bothCategoryBits & RaceMode.TEST_ENTITY) == RaceMode.TEST_ENTITY) {
            LocalRaceCar localRaceCar = (fixtureA.getUserData() instanceof LocalRaceCar) ? (LocalRaceCar) fixtureA.getUserData() : (LocalRaceCar) fixtureB.getUserData();
            localRaceCar.rotateBy((float) (-Math.PI / 2));
        }

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    /**
     * Called when a LocalRaceCar collides with a goal line.
     * @param contact
     */
    private void beginGoalContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        LocalRaceCar localRaceCar = (fixtureA.getUserData() instanceof LocalRaceCar) ? (LocalRaceCar) fixtureA.getUserData() : (LocalRaceCar) fixtureB.getUserData();

        localRaceCar.passGoal();
    }

    /**
     * Called when a LocalRaceCar collides with a checkpoint.
     * @param contact
     */
    private void beginCheckpointContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        LocalRaceCar localRaceCar = (fixtureA.getUserData() instanceof LocalRaceCar) ? (LocalRaceCar) fixtureA.getUserData() : (LocalRaceCar) fixtureB.getUserData();
        int checkpointId = (fixtureA.getUserData() instanceof LocalRaceCar) ? (int) fixtureB.getBody().getUserData() : (int) fixtureA.getBody().getUserData();

        localRaceCar.passCheckpoint(checkpointId);
    }
}
