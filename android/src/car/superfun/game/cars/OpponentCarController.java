package car.superfun.game.cars;

import android.support.annotation.NonNull;

import com.badlogic.gdx.math.Vector2;
import com.google.android.gms.games.multiplayer.Participant;

public class OpponentCarController implements CarController {

    public float forward;
    public float rotation;
    private Participant participant;
    private OpponentCar controlledCar;


    /**
     * Constructor
     * @param participant
     */
    public OpponentCarController(Participant participant) {
        forward = 0;
        rotation = 0;
        this.participant = participant;
    }

    /**
     * Set the car.
     * @param controlledCar
     */
    public void setControlledCar(OpponentCar controlledCar) {
        this.controlledCar = controlledCar;
    }

    public void setForward(float forward) {
        this.forward = forward;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }


    /**
     * Set the speed and angle
     * @param forward
     * @param rotation
     */
    public void setForwardAndRotation(float forward, float rotation) {
        this.forward = forward;
        this.rotation = rotation;
    }


    /**
     * Set the car movement details.
     * @param position
     * @param angle
     * @param velocity
     * @param timeDiff
     */
    public void setCarMovement(Vector2 position, float angle, Vector2 velocity, int timeDiff) {
        controlledCar.setMovement(position, angle, velocity, timeDiff);
    }

    public OpponentCar getControlledCar() {
        return controlledCar;
    }

    public boolean hasControlledCar() {
        return (controlledCar != null);
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
    public int compareTo(@NonNull CarController carController) {
        return participant.getParticipantId().compareTo(carController.getParticipant().getParticipantId());
    }

}
