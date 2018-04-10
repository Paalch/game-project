package car.superfun.game.car;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import car.superfun.game.GlobalVariables;

public class LocalCar extends Car {

    public LocalCar(Vector2 position, CarController carController, World world) {
        super(position,
                new Sprite(new Texture("racing-pack/PNG/Cars/car_blue_5.png")),
                carController,
                world,
                GlobalVariables.PLAYER_ENTITY);
    }
}
