package car.superfun.game;

import android.util.Log;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import car.superfun.game.gameModes.GameMode;
import car.superfun.game.gameModes.gladiatorMode.GladiatorMode;
import car.superfun.game.gameModes.raceMode.RaceMode;
import car.superfun.game.googlePlayGameServices.GoogleGameServices;
import car.superfun.game.menus.LoadingScreen;
import car.superfun.game.menus.LoginMenu;
import car.superfun.game.menus.MainMenu;
import car.superfun.game.states.GameStateManager;
import car.superfun.game.states.NewState;


public class CarSuperFun extends ApplicationAdapter {

    private GameStateManager gsm;
    private SpriteBatch batch;
    private GoogleGameServices googleGameServices;
    private AndroidLauncher androidLauncher;

    private boolean justPressedBack;

    // list of states that will be pushed to the gsm, the next time the resume method is called
    ArrayList<NewState> statesToBeCreated = new ArrayList<>();


    /**
     * Set up the main render class(this)
     * @param googleGameServices
     * @param androidLauncher
     */
    public CarSuperFun(GoogleGameServices googleGameServices, AndroidLauncher androidLauncher) {
        this.googleGameServices = googleGameServices;
        this.androidLauncher = androidLauncher;
    }


    /**
     *  When the class is initiated this method is called
     */
    @Override
    public void create() {

        // Creates a new sprite batch
        batch = new SpriteBatch();

        // Places the GameStateManger in the GSM variable
        gsm = GameStateManager.getInstance();

        //sets the background color (not usually seen) to black
        Gdx.gl.glClearColor(0, 0, 0, 1);

        // pushes the LoginMenu to the gsm, such that it is the first state to be rendered
        gsm.push(new LoginMenu(googleGameServices));

        // Take control of the back button
        Gdx.input.setCatchBackKey(true);
        justPressedBack = false;
    }


    /**
     * On resume is called every time the app is started again after it has been paused,
     * it can be if the user changes the current running app on his/hers phone
     * or if a different activity is called, such as the GPGS waiting lobby
     */
    @Override
    public void resume() {
        super.resume();

        // Initiate all states in the enum list
        for (NewState state : statesToBeCreated) {
            switch (state) {
                case RACE_MODE:
                    GameStateManager.getInstance().set(new RaceMode(googleGameServices, GlobalVariables.SINGLE_PLAYER));
                    break;
                case MAIN_MENU:
                    GameStateManager.getInstance().push(new MainMenu(googleGameServices));
                    break;
                case LOGIN_MENU:
                    GameStateManager.getInstance().push(new LoginMenu(googleGameServices));
                    break;
                case GLADIATOR_MODE:
                    GameStateManager.getInstance().set(new GladiatorMode(googleGameServices, GlobalVariables.SINGLE_PLAYER));
                    break;
            }
        }
        statesToBeCreated.clear();
    }

    /**
     * Renders the scene
     * by updating and rendering the current state given by the gsm
     */
    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // calles the update function in gsm
        gsm.update(Gdx.graphics.getDeltaTime());

        // render the batch
        gsm.render(batch);

        // Pop the game state when pressing back.
        if (Gdx.input.isKeyPressed(Input.Keys.BACK) && !justPressedBack) {

            // If the state is main menu close the game
            if (gsm.peek() instanceof MainMenu) {
                androidLauncher.finish();
                System.exit(0);

                // If the state is loading screen do not pop
            } else if (gsm.peek() instanceof LoadingScreen) {
                return;

                // pop the screen and disable the button for the next .8 sec
            } else {
                justPressedBack = true;
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        justPressedBack = false;
                    }
                }, 800);
                // if you are in a active game, also call the leaveRoom method,
                // to leave the game room
                if (gsm.peek() instanceof GameMode) {
                    googleGameServices.leaveRoom();
                }
                gsm.pop();
            }
        }
    }

    /**
     * Tells the gsm to depose its assets
     */
    @Override
    public void dispose() {
        gsm.dispose();
        batch.dispose();
    }

    /**
     * Adds the enum corresponding to the class that will be initiated on next resume
     * @param newState
     */
    public void createNewState(NewState newState) {
        statesToBeCreated.add(newState);
    }
}
