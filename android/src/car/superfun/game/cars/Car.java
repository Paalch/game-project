package car.superfun.game.cars;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import car.superfun.game.GlobalVariables;

import static java.lang.Math.abs;

public abstract class Car {
    private float acceleration;
    private float steering;
    private float grip;

    protected CarController carController;
    protected float frameRotation;
    protected Body body;
    protected Sprite sprite;


    /**
     * Constructor
     * @param position
     * @param sprite
     * @param carController
     * @param world
     * @param filterCategoryBits
     */
    public Car(Vector2 position,
               Sprite sprite,
               CarController carController,
               World world,
               short filterCategoryBits){

        this.sprite = sprite;
        this.sprite.setPosition(position.x, position.y);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((sprite.getX() + sprite.getWidth() / 2) / GlobalVariables.PIXELS_TO_METERS,
                (sprite.getY() + sprite.getHeight() / 2) / GlobalVariables.PIXELS_TO_METERS);
        bodyDef.allowSleep = false;
        bodyDef.angularDamping = 0.9f;
        bodyDef.linearDamping = 0.5f;
        bodyDef.angle = (float) (-Math.PI / 2);
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox((sprite.getWidth() / 2) / GlobalVariables.PIXELS_TO_METERS, (sprite.getHeight() / 2) / GlobalVariables.PIXELS_TO_METERS);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.filter.categoryBits = filterCategoryBits;
        fixtureDef.filter.maskBits = GlobalVariables.ALL_ENTITIES;
        fixtureDef.restitution = 0.2f;
        body.createFixture(fixtureDef).setUserData(this);

        shape.dispose();

        acceleration = 25.0f;
        steering = 4.0f;
        grip = 10;

        this.carController = carController;
        frameRotation = 0;
    }


    /**
     * Update
     * @param dt
     */
    public void update(float dt) {
        Vector2 direction = this.getDirectionVector();
        frameRotation = carController.getRotation() * steering;

        float traction = abs(body.getLinearVelocity().dot(direction.cpy().rotate(90 + 45 * carController.getRotation())));
        float sidewaysVelocityDampening = (body.getLinearVelocity().len() > 0.1) ? (abs(body.getLinearVelocity().dot(direction) / body.getLinearVelocity().len()) / 4) + 0.75f : 1;

        if (traction < grip) {
            body.setLinearVelocity(body.getLinearVelocity().rotate(frameRotation).scl(sidewaysVelocityDampening));
        } else {
            float velocityRotator = frameRotation * (float) (Math.exp(grip / traction) / Math.exp(traction / grip));
            frameRotation = frameRotation * (float) (Math.log(grip) / Math.log(traction));
            body.setLinearVelocity(body.getLinearVelocity().rotate(velocityRotator).scl(sidewaysVelocityDampening));
        }
        body.applyForceToCenter(direction.scl(carController.getForward() * acceleration), true);
        body.setAngularVelocity(frameRotation);
    }


    /**
     * Render
     * @param sb
     */
    public void render(SpriteBatch sb) {
        sprite.setPosition((body.getTransform().getPosition().x * GlobalVariables.PIXELS_TO_METERS) - sprite.getWidth()/2 ,
                (body.getTransform().getPosition().y * GlobalVariables.PIXELS_TO_METERS) - sprite.getHeight()/2 );
        sprite.setRotation((float)Math.toDegrees(body.getAngle()));
        sb.draw(sprite,
                sprite.getX(),
                sprite.getY(),
                sprite.getOriginX(),
                sprite.getOriginY(),
                sprite.getWidth(),
                sprite.getHeight(),
                sprite.getScaleX(),
                sprite.getScaleY(),
                sprite.getRotation());
    }

    public Vector2 getVelocity() { return body.getLinearVelocity(); }

    public Body getBody() { return body; }

    public Vector2 getSpritePosition() { return new Vector2(sprite.getX(), sprite.getY()); }

    public float getAngle() { return body.getAngle(); }

    public Vector2 getDirectionVector() { return new Vector2(0,1).rotateRad(body.getAngle()); }

    public Vector2 getBodyPosition() { return body.getPosition(); }
}
