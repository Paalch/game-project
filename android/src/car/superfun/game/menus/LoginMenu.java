package car.superfun.game.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import car.superfun.game.googlePlayGameServices.GoogleGameServices;
import car.superfun.game.actors.ButtonActor;
import car.superfun.game.states.GameStateManager;
import car.superfun.game.states.State;

public class LoginMenu extends State{
    private Stage stage;
    private Texture background;
    private final GoogleGameServices googleGameServices;

    /**
     * Constructor
     * @param googleGameServices
     */

    public LoginMenu(final GoogleGameServices googleGameServices){
        this.googleGameServices = googleGameServices;
        stage = new Stage(new ScreenViewport());
        background = new Texture("background.png");

        ButtonActor loginButton = new ButtonActor("menu-buttons/google.png");
        loginButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                googleGameServices.startSignInIntent();
                return true;
            }
        });

        loginButton.setPosition(stage.getWidth()/2 - loginButton.getWidth()/2, stage.getHeight()/2 - loginButton.getHeight()/2);
        stage.addActor(loginButton);
    }


    private void tryNextWindow() {
        if(googleGameServices.isSignedIn()) {
            GameStateManager.getInstance().push(new MainMenu(googleGameServices));
        }
    }

    @Override
    public void update(float dt) {
        tryNextWindow();
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.begin();
        sb.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        sb.end();
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void dispose() {
        background.dispose();
        stage.dispose();
    }

    @Override
    public void setInputProcessor() {
        Gdx.input.setInputProcessor(stage);
    }
}
