package car.superfun.game.cars;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.google.android.gms.games.multiplayer.Participant;

import car.superfun.game.GlobalVariables;

public class LocalCarController implements CarController {

    public float slider1Position;
    public float slider2Position;

    public float forward; // Value range is [-1, 1]
    public float rotation; // Value range is [-1, 1], where -1 means left and 1 means right

    private Texture slider1Texture;
    private Texture slider2Texture;

    private Texture knob1Texture;
    private Texture knob2Texture;

    private int knobRadius = 50;
    private int sliderWidth = 120;

    private float forwardOffset;

    private Participant participant;

    public LocalCarController(Participant participant) {
        this();
        this.participant = participant;

    }

    /**
     * Constructor
     */
    public LocalCarController() {
        slider1Position = Gdx.graphics.getHeight() / 2;
        slider2Position = Gdx.graphics.getHeight() / 2;

        slider1Texture = new Texture("slider.png");
        slider2Texture = new Texture("slider.png");

        knob1Texture = new Texture("slider_knob.png");
        knob2Texture = new Texture("slider_knob.png");


        if (GlobalVariables.TESTING_MODE) {
            forwardOffset = 0.4f;
        } else {
            forwardOffset = 0f;
        }
    }

    /**
     * Update
     */
    public void update() {
        boolean slider1Touched = false;
        boolean slider2Touched = false;
        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isTouched(i)) {
                Vector2 justTouched = new Vector2(Gdx.input.getX(i), Gdx.input.getY(i) * (-1) + Gdx.graphics.getHeight());
                if (justTouched.x < Gdx.graphics.getWidth() / 4) {
                    slider1Touched = true;
                    slider1Position =
                            (((Gdx.graphics.getHeight() / 2) - 50 > justTouched.y)
                                    || (justTouched.y > (Gdx.graphics.getHeight() / 2) + 50))
                                    ? justTouched.y :
                                    Gdx.graphics.getHeight() / 2;
                }
                else if (justTouched.x > 3 * Gdx.graphics.getWidth() / 4) {
                    slider2Touched = true;
                    slider2Position =
                            (((Gdx.graphics.getHeight() / 2) - 50 > justTouched.y)
                                    || (justTouched.y > (Gdx.graphics.getHeight() / 2) + 50))
                                    ? justTouched.y :
                                    Gdx.graphics.getHeight() / 2;
                }
            }
        }
        if (!slider1Touched) {
            slider1Position = slider1Position - (slider1Position - Gdx.graphics.getHeight() / 2) / 5;
        }
        if (!slider2Touched) {
            slider2Position = slider2Position - (slider2Position - Gdx.graphics.getHeight() / 2) / 5;
        }

        forward = Math.max(-1f, Math.min(1f, forwardOffset + (slider1Position + slider2Position - Gdx.graphics.getHeight()) / (Gdx.graphics.getHeight() * 0.8f)));
        rotation = Math.max(-1f, Math.min(1f, (slider2Position - slider1Position) / (Gdx.graphics.getHeight() * 0.8f)));
    }


    /**
     * Render
     * @param sb
     */
    public void render(SpriteBatch sb) {
        sb.begin();

        sb.draw(slider1Texture, 0, 0, slider1Texture.getWidth(), Gdx.graphics.getHeight());
        sb.draw(slider2Texture, Gdx.graphics.getWidth() - sliderWidth, 0, slider1Texture.getWidth(), Gdx.graphics.getHeight());

        sb.draw(knob1Texture, (sliderWidth / 2) - knobRadius, slider1Position - knobRadius);
        sb.draw(knob2Texture, Gdx.graphics.getWidth() - (sliderWidth / 2) - knobRadius, slider2Position - knobRadius);

        sb.end();
    }

    @Override
    public Participant getParticipant() {
        return participant;
    }

    @Override
    public float getForward() {
        return forward;
    }

    @Override
    public float getRotation() {
        return rotation;
    }


    @Override
    public int compareTo(CarController carController) {
        return this.participant.getParticipantId().compareTo(carController.getParticipant().getParticipantId());
    }
}
