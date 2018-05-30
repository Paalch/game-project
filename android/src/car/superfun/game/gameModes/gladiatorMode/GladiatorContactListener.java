package car.superfun.game.gameModes.gladiatorMode;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import car.superfun.game.GlobalVariables;


public class GladiatorContactListener implements ContactListener {
    Fixture fixtureA;
    Fixture fixtureB;
    Fixture userFixture;
    Fixture otherFixture;

    @Override
    public void beginContact(Contact contact) {
        fixtureA = contact.getFixtureA();
        fixtureB = contact.getFixtureB();

        // Return if none of the fixtures are your car.
        if (onlyOpponent()) {
            return;
        }

        // Set local car to userFixture.
        setFixtures();

        // Nothing should be done if the crash is with a regular wall.
        if (isRegularWalls()) {
            return;
        }

        // Choose what action to do depending on the other fixture.
        if (userFixture.getUserData() instanceof LocalGladiatorCar) {
            if (isDeathWalls()) {
                ((LocalGladiatorCar) userFixture.getUserData()).hitDeathWalls();
            } else if (isOpponentCar()) {
                ((LocalGladiatorCar) userFixture.getUserData()).hitOpponentCar();
            }
        }
    }


    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold){

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse){

    }


    // Set local car to userFixture and the other fixture to otherFixture.
    public void setFixtures() {
        if (fixtureA.getFilterData().categoryBits == GlobalVariables.PLAYER_ENTITY) {
            userFixture = fixtureA;
            otherFixture = fixtureB;
        } else {
            userFixture = fixtureB;
            otherFixture = fixtureA;
        }
    }


    public boolean onlyOpponent() {
        if (fixtureA.getFilterData().categoryBits == GlobalVariables.PLAYER_ENTITY || fixtureB.getFilterData().categoryBits == GlobalVariables.PLAYER_ENTITY) {
            return false;
        }
        return true;
    }


    public boolean isRegularWalls() {
        if (otherFixture.getFilterData().categoryBits == GlobalVariables.WALL_ENTITY) {
            return true;
        }
        return false;
    }


    public boolean isDeathWalls() {
        if (otherFixture.getFilterData().categoryBits == GladiatorMode.DEATH_ENTITY) {
            return true;
        }
        return false;
    }


    public boolean isOpponentCar() {
        if (otherFixture.getFilterData().categoryBits == GlobalVariables.OPPONENT_ENTITY) {
            return true;
        }
        return false;
    }
}
