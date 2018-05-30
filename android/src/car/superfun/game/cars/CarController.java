package car.superfun.game.cars;

import com.google.android.gms.games.multiplayer.Participant;

public interface CarController extends Comparable<CarController> {

    public Participant getParticipant();

    float getForward();
    float getRotation();
}
