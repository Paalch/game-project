package car.superfun.game.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import car.superfun.game.googlePlayGameServices.GoogleGameServices;
import car.superfun.game.states.NewState;
import car.superfun.game.actors.ButtonActor;
import car.superfun.game.gameModes.gladiatorMode.GladiatorMode;
import car.superfun.game.gameModes.raceMode.RaceMode;
import car.superfun.game.states.GameStateManager;
import car.superfun.game.states.State;

public class GameSettings extends State{

    private Texture background;
    private Stage stage;

    /**
     * Constructor
     * Creates a menu where the user can choose how many players he wants to play with.
     * @param newState
     * @param googleGameServices
     */
    public GameSettings(final NewState newState, final GoogleGameServices googleGameServices){
        this.stage = new Stage(new ScreenViewport());

        background = new Texture("background.png");

        Table table = new Table();
        table.setWidth(stage.getWidth());
        table.align(Align.center|Align.top);

        table.setPosition(0, Gdx.graphics.getHeight());

        Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        skin.getFont("default-font").getData().setScale(4f,4f);

        ButtonActor backButton = new ButtonActor("menu-buttons/back.png");
        backButton.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                GameStateManager.getInstance().pop();
                return true;
            }
        });

        ButtonActor singleplayerButton = new ButtonActor("numbers/one.png");
        singleplayerButton.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(newState == NewState.GLADIATOR_MODE){
                    GameStateManager.getInstance().set(new GladiatorMode(googleGameServices, true));
                }else if(newState == NewState.RACE_MODE){
                    GameStateManager.getInstance().set(new RaceMode(googleGameServices, true));
                }
                return true;
            }
        });

        ButtonActor twoplayerButton = new ButtonActor("numbers/two.png");
        twoplayerButton.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                googleGameServices.startQuickGame(newState, 1);
                GameStateManager.getInstance().set(new LoadingScreen());
                return true;
            }
        });

        ButtonActor threeplayerButton = new ButtonActor("numbers/three.png");
        threeplayerButton.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                //GlobalVariables.SINGLE_PLAYER = false;
                googleGameServices.startQuickGame(newState, 2);
                GameStateManager.getInstance().set(new LoadingScreen());
                return true;
            }
        });

        ButtonActor fourplayerButton = new ButtonActor("numbers/four.png");
        fourplayerButton.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                //GlobalVariables.SINGLE_PLAYER = false;
                googleGameServices.startQuickGame(newState, 3);
                GameStateManager.getInstance().set(new LoadingScreen());
                return true;
            }
        });

        table.add(backButton).expandX().left().colspan(4).padLeft(stage.getWidth()/50).padTop(stage.getHeight()/30);
        table.row();
        table.add(new Label("Number of players", skin)).center().colspan(4).padBottom(120);
        table.row();
        table.add(singleplayerButton);
        table.add(twoplayerButton);
        table.add(threeplayerButton);
        table.add(fourplayerButton);

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
     * dispose
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
