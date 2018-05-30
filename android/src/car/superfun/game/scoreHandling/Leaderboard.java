package car.superfun.game.scoreHandling;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
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

import java.util.ArrayList;
import java.util.Map;

import car.superfun.game.actors.ButtonActor;
import car.superfun.game.menus.MainMenu;
import car.superfun.game.states.GameStateManager;
import car.superfun.game.states.State;

public class Leaderboard extends State implements HandlesScore {

    private Texture background;
    private Stage stage;
    private ArrayList<Player> playerList = new ArrayList<>();
    private Table table;
    private Skin headerSkin, skin;
    private ButtonActor backButton;

    private boolean isPositive;
    private ScoreFormatter formatter;

    private boolean doUpdate = false;

    /**
     * Instantiates the leaderboard without filling it with content.
     * @param formatter
     * @param isPositive
     */
    public Leaderboard(ScoreFormatter formatter, boolean isPositive) {
        this.stage = new Stage(new ScreenViewport());
        background = new Texture("background.png");
        this.isPositive = isPositive;
        this.formatter = formatter;

        backButton = new ButtonActor("menu-buttons/back.png");
        backButton.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                GameStateManager.getInstance().pop();
                return true;
            }
        });

        table = new Table();
        table.setWidth(stage.getWidth());
        table.align(Align.center|Align.top);
        table.setPosition(0, stage.getHeight());
        headerSkin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        headerSkin.getFont("default-font").getData().setScale(6f,6f);
        skin.getFont("default-font").getData().setScale(4f,4f);

        stage.addActor(table);
    }

    /**
     * Calls the first constructor to instantiate the leaderboard, then fills it with the supplied scores.
     * @param formatter
     * @param isPositive
     * @param scores
     */
    public Leaderboard(ScoreFormatter formatter, boolean isPositive, Map<String, Integer> scores) {
        this(formatter, isPositive);
        for (Map.Entry<String, Integer> scoreEntry : scores.entrySet()) {
            placePlayer(scoreEntry.getKey(), scoreEntry.getValue());
        }
        fillTable();
    }

    /**
     * update
     * @param dt
     */
    @Override
    public void update(float dt) {
        if (doUpdate) {
            table.clearChildren();
            fillTable();
            Gdx.app.log("leaderboard", "update");
            for (Player player : playerList) {
                Gdx.app.log("player", player.getName() + ": " + player.getScore());
            }
            doUpdate = false;
        }
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
     * Populates the table with elements, such as player information and a button.
     * To update the table with new data, just call the function again.
     */
    public void fillTable(){
        table.add(backButton).expandX().left().colspan(3).padBottom(-60).padLeft(stage.getWidth()/50).padTop(stage.getHeight()/30);
        table.row();
        table.add(new Label("Leaderboard", headerSkin)).center().colspan(3);
        table.row();
        table.add(new Label("Position", skin));
        table.add(new Label("Nickname", skin));
        table.add(new Label(formatter.scoreString(), skin));
        if (!playerList.isEmpty()){
            int pos = 1;
            if (isPositive) {
                for (int i = playerList.size()-1; i>= 0; i--) {
                    table.row();
                    table.add(new Label(Integer.toString(pos)+".", skin));
                    table.add(new Label(playerList.get(i).getName(), skin));
                    table.add(new Label(formatter.formatScore(playerList.get(i).getScore()), skin));
                    pos += 1;
                }
            } else {
                for (Player player : playerList) {
                    table.row();
                    table.add(new Label(Integer.toString(pos)+".", skin));
                    table.add(new Label(player.getName(), skin));
                    table.add(new Label(formatter.formatScore(player.getScore()), skin));
                    pos += 1;
                }
            }
        }
    }

    /**
     * Adds player and score/time to playerList
     * @param name
     * @param score
     */
    private void placePlayer(String name, int score){
        Player player = new Player(name, score);
        playerList.add(player);
    }

    /**
     * Handles new player score
     * @param player
     * @param score
     */
    @Override
    public void handleScore(String player, int score) {
        placePlayer(player, score);
        doUpdate = true;
    }

    /**
     * dispose
     */
    @Override
    public void dispose() {
        stage.dispose();
        background.dispose();
        playerList.clear();
    }

    @Override
    public void setInputProcessor() {
        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Player class used to store name and score/time
     */
    private class Player {
        private String name;
        private int score;

        public Player(String name, int score) {
            this.name = name;
            this.score = score;
        }

        public String getName() {
            return name;
        }

        public int getScore() {
            return score;
        }
    }
}
