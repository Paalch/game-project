package car.superfun.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.GamesClientStatusCodes;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import car.superfun.game.gameModes.raceMode.OpponentCar;


public class AndroidLauncher extends AndroidApplication {


    final static String TAG = "CarSuperFun";

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    final static int RC_WAITING_ROOM = 10002;

    // Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient = null;

    // Client used to interact with the real time multiplayer system.
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient = null;

    private RoomConfig mJoinedRoomConfig = null;

    private String mRoomId;

    // The participants in the currently active game
    ArrayList<Participant> mParticipants = null;

    // My participant ID in the currently active game
    String mMyId = null;

    private CarSuperFun carSuperFun;

    private int posX = 0;
    private int posY = 0;
    private float angle = 0;

    // Message buffer for sending messages
    byte[] mMsgBuf = new byte[14];


    Map<String, OpponentCar> mParticipantPosition = null;

    // Participants who sent us their final score.
    Set<String> mFinishedParticipants = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        this.carSuperFun =  new CarSuperFun(this);
        initialize(carSuperFun, config);
        mParticipantPosition = new HashMap<>();

        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        signInSilently();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult()");
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
            if (result.isSuccess()) {
                // The signed in account is stored in the result.
                GoogleSignInAccount signedInAccount = result.getSignInAccount();
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.signin_other_error);
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        }  else if (requestCode == RC_WAITING_ROOM) {
            // we got the result from the "waiting room" UI.
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "Starting game (waiting room returned OK).");
               // GameStateManager.getInstance().push(new RaceMode(this, false));

                for(Participant participant : mParticipants) {
                    mParticipantPosition.put(participant.getParticipantId(), new OpponentCar(new Vector2(), new World(new Vector2(), false)));
                }

            } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player indicated that they want to leave the room
                leaveRoom();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Dialog was cancelled (user pressed back key, for instance). In our game,
                // this means leaving the room too. In more elaborate games, this could mean
                // something else (like minimizing the waiting room UI).
                leaveRoom();
            }
        }
    }

    public void startSignInIntent() {
        Log.d(TAG, "startSignInIntent()");
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    public void signOut() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // at this point, the user is signed out.
                        Log.d(TAG, "signOut(): success");
                    }
                });
    }

    /**
     * Try to sign in without displaying dialogs to the user.
     * <p>
     * If the user has already signed in previously, it will not show dialog.
     */
    public void signInSilently() {
        Log.d(TAG, "signInSilently()");

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInSilently(): success");
                            onConnected(task.getResult());
                        } else {
                            Log.d(TAG, "signInSilently(): failure", task.getException());
                        }
                    }
                });
    }

    public boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    public void startQuickGame() {
        // auto-match criteria to invite one random automatch opponent.
        // You can also specify more opponents (up to 3).
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(1, 1, 0);

        // build the room config:
        mJoinedRoomConfig =
                RoomConfig.builder(mRoomUpdateCallback)
                        .setOnMessageReceivedListener(mMessageReceivedHandler)
                        .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler)
                        .setAutoMatchCriteria(autoMatchCriteria)
                        .build();

        // prevent screen from sleeping during handshake
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // create room:
        Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .create(mJoinedRoomConfig);
    }


    private RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {

        // Called when room has been created
        @Override
        public void onRoomCreated(int statusCode, Room room) {
            Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
                showGameError();
                return;
            }

            // save room ID so we can leave cleanly before the game starts.
            mRoomId = room.getRoomId();

            // show the waiting room UI
            showWaitingRoom(room);
        }

        // Called when room is fully connected.
        @Override
        public void onRoomConnected(int statusCode, Room room) {
            Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
                showGameError();
                return;
            }
            updateRoom(room);
        }

        @Override
        public void onJoinedRoom(int statusCode, Room room) {
            Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
                showGameError();
                return;
            }

            // show the waiting room UI
            showWaitingRoom(room);
        }

        // Called when we've successfully left the room (this happens a result of voluntarily leaving
        // via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
        @Override
        public void onLeftRoom(int statusCode, @NonNull String roomId) {
            // we have left the room; return to main screen.
            Log.d(TAG, "onLeftRoom, code " + statusCode);
            getMenuScreen();
        }
    };

    // Show error message about game being cancelled and return to main screen.
    void showGameError() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.game_problem))
                .setNeutralButton(android.R.string.ok, null).create();

        getMenuScreen();
    }

     /*
     * CALLBACKS SECTION. This section shows how we implement the several games
     * API callbacks.
     */

    private String mPlayerId;

    // The currently signed in account, used to check the account has changed outside of this activity when resuming.
    GoogleSignInAccount mSignedInAccount = null;

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs");
        if (mSignedInAccount != googleSignInAccount) {

            mSignedInAccount = googleSignInAccount;

            // update the clients
            mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(this, googleSignInAccount);

            // get the playerId from the PlayersClient
            PlayersClient playersClient = Games.getPlayersClient(this, googleSignInAccount);
            playersClient.getCurrentPlayer()
                    .addOnSuccessListener(new OnSuccessListener<Player>() {
                        @Override
                        public void onSuccess(Player player) {
                            mPlayerId = player.getPlayerId();

                            getMenuScreen();
                        }
                    })
                    .addOnFailureListener(createFailureListener("There was a problem getting the player id!"));
        }

    }


    private OnFailureListener createFailureListener(final String string) {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                handleException(e, string);
            }
        };
    }

    private RoomStatusUpdateCallback mRoomStatusCallbackHandler = new RoomStatusUpdateCallback() {
        // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
        // is connected yet).
        @Override
        public void onConnectedToRoom(Room room) {
            Log.d(TAG, "onConnectedToRoom.");

            //get participants and my ID:
            mParticipants = room.getParticipants();
            mMyId = room.getParticipantId(mPlayerId);

            // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
            if (mRoomId == null) {
                mRoomId = room.getRoomId();
            }

            // print out the list of participants (for debug purposes)
            Log.d(TAG, "Room ID: " + mRoomId);
            Log.d(TAG, "My ID " + mMyId);
            Log.d(TAG, "<< CONNECTED TO ROOM>>");
        }

        // Called when we get disconnected from the room. We return to the main screen.
        @Override
        public void onDisconnectedFromRoom(Room room) {
            mRoomId = null;
            mJoinedRoomConfig = null;
            showGameError();
        }


        // We treat most of the room update callbacks in the same way: we update our list of
        // participants and update the display. In a real game we would also have to check if that
        // change requires some action like removing the corresponding player avatar from the screen,
        // etc.
        @Override
        public void onPeerDeclined(Room room, @NonNull List<String> arg1) {
            updateRoom(room);
        }

        @Override
        public void onPeerInvitedToRoom(Room room, @NonNull List<String> arg1) {
            updateRoom(room);
        }

        @Override
        public void onP2PDisconnected(@NonNull String participant) {
        }

        @Override
        public void onP2PConnected(@NonNull String participant) {
        }

        @Override
        public void onPeerJoined(Room room, @NonNull List<String> arg1) {
            updateRoom(room);
        }

        @Override
        public void onPeerLeft(Room room, @NonNull List<String> peersWhoLeft) {
            updateRoom(room);
        }

        @Override
        public void onRoomAutoMatching(Room room) {
            updateRoom(room);
        }

        @Override
        public void onRoomConnecting(Room room) {
            updateRoom(room);
        }

        @Override
        public void onPeersConnected(Room room, @NonNull List<String> peers) {
            updateRoom(room);
        }

        @Override
        public void onPeersDisconnected(Room room, @NonNull List<String> peers) {
            updateRoom(room);
        }
    };

    // Leave the room.
    void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        stopKeepingScreenOn();
        if (mRoomId != null) {
            mRealTimeMultiplayerClient.leave(mJoinedRoomConfig, mRoomId)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mRoomId = null;
                            mJoinedRoomConfig = null;
                        }
                    });

        }
        getMenuScreen();
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void getMenuScreen() {
        // TODO show menu screen here
        Log.d(TAG, "Show menu screen");
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        mRealTimeMultiplayerClient.getWaitingRoomIntent(room, MIN_PLAYERS)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        // show waiting room UI
                        startActivityForResult(intent, RC_WAITING_ROOM);
                    }
                })
                .addOnFailureListener(createFailureListener("There was a problem getting the waiting room!"));
    }

    private void updateRoom(Room room) {
        // TODO show waiting room
        Log.d(TAG, "Do some stuff");
    }

    private void handleException(Exception exception, String details) {
        int status = 0;

        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            status = apiException.getStatusCode();
        }

        String errorString = null;
        switch (status) {
            case GamesCallbackStatusCodes.OK:
                break;
            case GamesClientStatusCodes.MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                errorString = getString(R.string.status_multiplayer_error_not_trusted_tester);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_ALREADY_REMATCHED:
                errorString = getString(R.string.match_error_already_rematched);
                break;
            case GamesClientStatusCodes.NETWORK_ERROR_OPERATION_FAILED:
                errorString = getString(R.string.network_error_operation_failed);
                break;
            case GamesClientStatusCodes.INTERNAL_ERROR:
                errorString = getString(R.string.internal_error);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_INACTIVE_MATCH:
                errorString = getString(R.string.match_error_inactive_match);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_LOCALLY_MODIFIED:
                errorString = getString(R.string.match_error_locally_modified);
                break;
            default:
                errorString = getString(R.string.unexpected_status, GamesClientStatusCodes.getStatusCodeString(status));
                break;
        }

        if (errorString == null) {
            return;
        }

        String message = getString(R.string.status_exception_error, details, status, exception);

        new AlertDialog.Builder(AndroidLauncher.this)
                .setTitle("Error")
                .setMessage(message + "\n" + errorString)
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

    // Called when we receive a real-time message from the network.
    // Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
    // indicating
    // whether it's a final or interim score. The second byte is the score.
    // There is also the
    // 'S' message, which indicates that the game should start.
    OnRealTimeMessageReceivedListener mMessageReceivedHandler = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            byte[] buf = realTimeMessage.getMessageData();
            String sender = realTimeMessage.getSenderParticipantId();

            if (buf[0] == 'F' || buf[0] == 'U') {
                byte[] bytes = new byte[4];

                for (byte byteValue = 0; byteValue<bytes.length; byteValue++) {
                    bytes[byteValue] = buf[2+byteValue];
                }

                posX = ByteBuffer.wrap(bytes).getInt();

                for (byte byteValue = 0; byteValue<bytes.length; byteValue++) {
                    bytes[byteValue] = buf[6+byteValue];
                }

                posY = ByteBuffer.wrap(bytes).getInt();

                for (byte byteValue = 0; byteValue<bytes.length; byteValue++) {
                    bytes[byteValue] = buf[10+byteValue];
                }

                angle = ByteBuffer.wrap(bytes).getFloat();

                // mParticipantPosition.get(realTimeMessage.getSenderParticipantId());

                // if it's a final score, mark this participant as having finished
                // the game
                if ((char) buf[0] == 'F') {
                    mFinishedParticipants.add(realTimeMessage.getSenderParticipantId());
                }
            }
        }
    };

    public int getPosX(){
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public float getAngle() {
        return angle;
    }

    public void broadcast(boolean finalScore, int score, Vector2 position, float angle) {

        // First byte in message indicates whether it's a final score or not
        mMsgBuf[0] = (byte) (finalScore ? 'F' : 'U');

        // Second byte is the score.
        mMsgBuf[1] = (byte) score;

        // Third byte is the x position.
        byte[] bytes = ByteBuffer.allocate(4).putInt((int) position.x).array();

        for (byte byteValue = 0; byteValue<bytes.length; byteValue++) {
            mMsgBuf[2+byteValue] = bytes[byteValue];
        }

        // Third byte is the x position.
        bytes = ByteBuffer.allocate(4).putInt((int) position.y).array();

        for (byte byteValue = 0; byteValue<bytes.length; byteValue++) {
            mMsgBuf[6+byteValue] = bytes[byteValue];
        }

        Log.d(TAG, Float.toString(angle));
        bytes = ByteBuffer.allocate(4).putFloat(angle).array();;

        for (byte byteValue = 0; byteValue<bytes.length; byteValue++) {
            mMsgBuf[10+byteValue] = bytes[byteValue];
        }

        // Log.d(TAG,"x: " +  Float.toString(position.x) + " y. " + Float.toString(position.y));

        // Send to every other participant.
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId)) {
                continue;
            }
            if (p.getStatus() != Participant.STATUS_JOINED) {
                continue;
            }
            if (finalScore) {
                // final score notification must be sent via reliable message
                mRealTimeMultiplayerClient.sendReliableMessage(mMsgBuf,
                        mRoomId, p.getParticipantId(), new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                            @Override
                            public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {
                                Log.d(TAG, "RealTime message sent");
                                Log.d(TAG, "  statusCode: " + statusCode);
                                Log.d(TAG, "  tokenId: " + tokenId);
                                Log.d(TAG, "  recipientParticipantId: " + recipientParticipantId);
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Integer>() {
                            @Override
                            public void onSuccess(Integer tokenId) {
                                Log.d(TAG, "Created a reliable message with tokenId: " + tokenId);
                            }
                        });
            } else {
                // it's an interim score notification, so we can use unreliable
                mRealTimeMultiplayerClient.sendUnreliableMessage(mMsgBuf, mRoomId,
                        p.getParticipantId());
            }
        }
    }

}


