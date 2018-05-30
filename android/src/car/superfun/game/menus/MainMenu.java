package car.superfun.game.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import car.superfun.game.googlePlayGameServices.GoogleGameServices;
import car.superfun.game.states.NewState;
import car.superfun.game.actors.ButtonActor;
import car.superfun.game.states.GameStateManager;
import car.superfun.game.states.State;

public class MainMenu extends State {
    private Stage stage;
    private Texture background;

    /**
     * Constructor
     * Creates a main menu where the user can choose a game mode or go to settings
     * @param googleGameServices
     */
    public MainMenu(final GoogleGameServices googleGameServices) {
        background = new Texture("background.png");
        stage = new Stage(new ScreenViewport());

        // Create a scene2d table to make it easier to position elements
        Table table = new Table();
        table.setWidth(stage.getWidth());
        table.align(Align.center | Align.top);
        table.setPosition(0, Gdx.graphics.getHeight());

        // Initialize buttons needed
        ButtonActor settingsButton = new ButtonActor("menu-buttons/settings.png");
        settingsButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                GameStateManager.getInstance().push(new SettingsMenu(googleGameServices));
                return true;
            }
        });

        ButtonActor raceButton = new ButtonActor("menu-buttons/race.png");
        raceButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                GameStateManager.getInstance().push(new GameSettings(NewState.RACE_MODE, googleGameServices));
                return true;
            }
        });

        ButtonActor gladiatorButton = new ButtonActor("menu-buttons/gladiator.png");
        gladiatorButton.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                GameStateManager.getInstance().push(new GameSettings(NewState.GLADIATOR_MODE, googleGameServices));
                return true;
            }
        });

        // Add buttons to table
        table.add(settingsButton).expandX().top().right().padBottom(-settingsButton.getHeight()).padRight(stage.getWidth()/50).padTop(stage.getHeight()/30);
        table.row();
        table.add(raceButton).padTop(stage.getHeight()/5).center();
        table.row();
        table.add(gladiatorButton).padTop(stage.getHeight()/6).center();


        stage.addActor(table);
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
        sb.begin();
        sb.draw(background, 0, 0, stage.getWidth(), stage.getHeight());
        sb.end();
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    /**
     * Disposes background and stage
     */
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
