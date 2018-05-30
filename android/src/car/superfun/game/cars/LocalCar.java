package car.superfun.game.cars;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import car.superfun.game.GlobalVariables;

public class LocalCar extends Car {

    /**
     * Constructor
     * @param position
     * @param carController
     * @param world
     * @param texturePath
     */
    public LocalCar(Vector2 position, CarController carController, World world, String texturePath) {
        super(position,
                new Sprite(new Texture(texturePath)),
                carController,
                world,
                GlobalVariables.PLAYER_ENTITY);
    }
}
