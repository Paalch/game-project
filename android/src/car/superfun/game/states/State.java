package car.superfun.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class State {

    protected State() {
    }

    //Updates the state
    public abstract void update(float dt);

    //Renders the objects in this state
    public abstract void render(SpriteBatch sb);

    //dispose
    public abstract void dispose();

    //Set input processor
    public void setInputProcessor(){
        Gdx.input.setInputProcessor(null);
    };
}
