package car.superfun.game.googlePlayGameServices;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import car.superfun.game.AndroidLauncher;
import car.superfun.game.CarSuperFun;
import car.superfun.game.menus.LoginMenu;
import car.superfun.game.states.GameStateManager;


public class SignIn  {


    // Used as a tag when printing message to console
    private final static String TAG = "SignIn";

    // reference to androidLauncher
    private AndroidLauncher androidLauncher;

    // Request code used to invoke sign in user interactions.
    public static final int SIGN_IN_ID = 9001;

    // Reference to the main render class
    private CarSuperFun carSuperFun;

    // setUpGame class is used if signlinet login was a success
    private SetUpGame setUpGame;

    // Client used to sign in with Google APIs
    private GoogleSignInClient googleSignInClient = null;

    /**
     * Takes in important refrences
     * @param androidLauncher
     * @param setUpGame
     * @param carSuperFun
     */
    public SignIn(AndroidLauncher androidLauncher, SetUpGame setUpGame, CarSuperFun carSuperFun) {
        this.setUpGame = setUpGame;
        this.androidLauncher = androidLauncher;
        this.carSuperFun = carSuperFun;
    }

    /**
     * Opens the signIn screen, result is returned to the androdLauncher instance.
     */
    public void startSignInIntent() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(androidLauncher, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        androidLauncher.startActivityForResult(intent, SIGN_IN_ID);
    }

    /**
     * Used to sign out the user
     */
    public void signOut() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(androidLauncher,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.signOut().addOnCompleteListener(androidLauncher,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // return to the LogInMenu
                        GameStateManager.getInstance().push(new LoginMenu(androidLauncher.googleGameServices));
                    }
                });
    }

    /**
     * Attempts to signIn the user withour showing the dialog box
     */
    public void signInSilently() {
        googleSignInClient.silentSignIn().addOnCompleteListener(androidLauncher,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                                setUpGame.onConnected(task.getResult());
                        }
                    }
                });
    }

    /**
     * Retunres true if the user is sign in, false otherwise
     * @return
     */
    public boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(androidLauncher) != null;
    }

    /**
     * attempts to set the signInClient to androidLauncher
     */
    public void setSignInClient(){
        googleSignInClient = GoogleSignIn.getClient(androidLauncher, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
    }
}
