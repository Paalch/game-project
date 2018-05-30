package car.superfun.game.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import car.superfun.game.actors.DelayedButtonActor;
import car.superfun.game.actors.LoadingActor;
import car.superfun.game.states.GameStateManager;
import car.superfun.game.states.State;

public class LoadingScreen extends State {
    private Stage stage;

    /**
     * Constructor
     * Creates a loading screen
     */
    public LoadingScreen(){
        this.stage = new Stage(new ScreenViewport());

        /*
         * Creates a delayed button (see Actors/DelayedButtonActor) to prevent users
         * from getting stuck in the loading screen
         */
        DelayedButtonActor backButton = new DelayedButtonActor("menu-buttons/backWhite.png", 5f);
        backButton.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                GameStateManager.getInstance().pop();
                return true;
            }
        });
        backButton.setPosition(stage.getWidth()/50,(stage.getHeight()-backButton.getHeight())-(stage.getHeight()/30));

        // Creates the loading actor (spinning image)
        LoadingActor loadingActor = new LoadingActor("loading.png");
        loadingActor.setPosition((stage.getWidth()/2)-(loadingActor.getWidth()/2), (stage.getHeight()/2)-(loadingActor.getHeight()/2));

        stage.addActor(backButton);
        stage.addActor(loadingActor);
    }

    /**
     * update
     * @param dt
     */
    @Override
    public void update(float dt) {
    }

    /**
     * render
     * @param sb
     */
    @Override
    public void render(SpriteBatch sb) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    /**
     * dispose
     */
    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void setInputProcessor() {
        Gdx.input.setInputProcessor(stage);
    }
}