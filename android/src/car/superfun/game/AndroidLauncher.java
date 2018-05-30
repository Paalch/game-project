package car.superfun.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.multiplayer.Participant;
import com.instacart.library.truetime.TrueTime;

import java.io.IOException;
import java.util.ArrayList;

import car.superfun.game.cars.OpponentCarController;
import car.superfun.game.googlePlayGameServices.Communicator;
import car.superfun.game.googlePlayGameServices.GoogleGameServices;
import car.superfun.game.googlePlayGameServices.SetUpGame;
import car.superfun.game.googlePlayGameServices.SignIn;
import car.superfun.game.states.NewState;

public class AndroidLauncher extends AndroidApplication {

    // Define variables used for googlePlayGameServices
    public SetUpGame setUpGame;
    public SignIn signIn;
    public Communicator communicator;

    // Define the Application Adapter, used for rendering and updating the screen
    private CarSuperFun carSuperFun;

    // Enum class used to keep track of what game mode to choose.
    private NewState newState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Creates a new Application adapter, CarSuperFun
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        this.carSuperFun = new CarSuperFun(googleGameServices, this);
        initialize(carSuperFun, config);

        // Init google game services classes
        this.setUpGame = new SetUpGame(this);
        this.signIn = new SignIn(this, setUpGame, carSuperFun);
        this.communicator = setUpGame.getCommunicator();

        // Tries to set the logged in GPGS client
        signIn.setSignInClient();

        // Creates a new synchronized clock
        clockSynchronizer.start();
        Gdx.graphics.setContinuousRendering(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Try to signInSilienry in case, something have happent while the game was "offline"
        signIn.signInSilently();
    }


    /**
     * Method is used when requests are returned from GPGS
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // If intent is signIn
        if (requestCode == signIn.SIGN_IN_ID) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
            // If the intent was a success
            if (result.isSuccess()) {
                // As soon as the rendering is resumed,  send the user to the main menu
                carSuperFun.createNewState(NewState.MAIN_MENU);
                // if something went wrong with the login, print there was an issue..
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.signin_other_error);
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
            // if the intent is waitingRoom
        } else if (requestCode == SetUpGame.RC_WAITING_ROOM) {
            // if new waiting room is created
            if (resultCode == Activity.RESULT_OK) {
                setUpGame.waitingRoomReady();
                // Create the gamemode, specified in the new state.
                carSuperFun.createNewState(newState);
                // if anything went wrong leave the room.
            } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                setUpGame.leaveRoom();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                setUpGame.leaveRoom();
            }
        }
    }


    /**
     * Create a thread for initalizing TrueTime, a library which runs NTP and stores the time difference
     * needed to output a synchronized time.
     * This must be a new thread because TrueTime.initialize() must be run on a separate thread.
     * After this initialization TrueTime.now().getTime() can be used to get the synchronized time,
     * which we can rely on being the same on all devices running this game.
     */
    private Thread clockSynchronizer = new Thread() {
        public void run() {
            if (!TrueTime.isInitialized()) {
                try {
                    TrueTime.build()
                            .withNtpHost("time.google.com")
                            .withLoggingEnabled(false)
                            .withConnectionTimeout(3_1428)
                            .initialize();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    };

    /**
     * Sets the next state to be added to the gsm
     * @param newState
     */
    public void setNewState(NewState newState) {
        this.newState = newState;
    }


    /**
     *  Creates a callback based on the GoogleGameServices interfaced, used for anything multilayer
     *  related outside this class
     */
    public GoogleGameServices googleGameServices = new GoogleGameServices() {

        @Override
        public void broadcastState(Vector2 velocity, Vector2 position, float angle, float forward, float rotation) {
            communicator.broadcastState(velocity, position, angle, forward, rotation);
        }

        @Override
        public void broadcastScore(int score) {
            communicator.broadcastScore(score);
        }

        @Override
        public Array<OpponentCarController> getOpponentCarControllers() {
            return communicator.getOpponentCarControllers();
        }

        @Override
        public boolean isSignedIn() {
            return signIn.isSignedIn();
        }

        @Override
        public void signOut() {
            signIn.signOut();
        }

        @Override
        public void startSignInIntent() {
            signIn.startSignInIntent();
        }

        @Override
        public void startQuickGame(NewState newState, int numberOfPlayers) {
            setUpGame.startQuickGame(newState, numberOfPlayers);
        }

        @Override
        public void leaveRoom() {
            setUpGame.leaveRoom();
        }

        @Override
        public void readyToStart(boolean isSinglePlayer) {
            communicator.readyToStart(isSinglePlayer);
        }

        @Override
        public boolean gameStarted() {
            return communicator.gameStarted;
        }

        @Override
        public long getStartTime() {
            return communicator.startTime;
        }

        public ArrayList<Participant> getParticipants () {
            return setUpGame.participants;
        }

        @Override
        public String getMyID () {
            return setUpGame.myId;
        }

        @Override
        public Participant getLocalParticipant() {
            return setUpGame.getLocalParticipant();
        }
    };
}