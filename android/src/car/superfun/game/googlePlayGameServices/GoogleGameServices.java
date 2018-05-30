package car.superfun.game.googlePlayGameServices;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.google.android.gms.games.multiplayer.Participant;

import java.util.ArrayList;

import car.superfun.game.cars.OpponentCarController;
import car.superfun.game.states.NewState;

public interface GoogleGameServices {

    boolean isSignedIn();
    void signOut();
    void startSignInIntent();

    void startQuickGame(NewState newState, int numberOfPlayers);
    void readyToStart(boolean isSinglePlayer);
    void leaveRoom();
    String getMyID();
    Participant getLocalParticipant();

    boolean gameStarted();
    ArrayList<Participant> getParticipants();
    Array<OpponentCarController> getOpponentCarControllers();
    long getStartTime();
    void broadcastState(Vector2 velocity, Vector2 position, float angle, float forward, float rotation);
    void broadcastScore(int score);

}
