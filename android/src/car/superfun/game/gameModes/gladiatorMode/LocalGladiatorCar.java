package car.superfun.game.gameModes.gladiatorMode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import car.superfun.game.GlobalVariables;
import car.superfun.game.cars.LocalCarController;
import car.superfun.game.cars.LocalCar;

import static java.lang.Math.abs;


public class LocalGladiatorCar extends LocalCar {

    private int score;
    private Sound dustWallCrash;
    GladiatorMode gameClass;


    public LocalGladiatorCar(GladiatorMode gameClass, Vector2 position, LocalCarController localCarController, World world, Integer score, Sound dustWallCrash, String texturePath) {
        super(position, localCarController, world, texturePath);
        this.score = score;
        this.dustWallCrash = dustWallCrash;
        this.gameClass = gameClass;
    }


    // Is called from GladiatorContactListener.
    public void hitDeathWalls() {
        score -= 1;
        dustWallCrash.play(GlobalVariables.SOUND_VOLUME);
        gameClass.setScore(score);
        if (score <= 0) {
            gameClass.endGame();
        }
    }


    public void hitOpponentCar() {
        // TODO: Let the cars crash and bounce.
    }


    public int getScore() {
        return score;
    }
}
