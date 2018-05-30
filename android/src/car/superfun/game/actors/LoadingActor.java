package car.superfun.game.actors;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.actions.RotateByAction;

import static com.badlogic.gdx.scenes.scene2d.actions.RepeatAction.FOREVER;

public class LoadingActor extends Actor {
    private Sprite sprite;

    /*
     * The LoadingActor creates a rotating sprite of the image provided.
     */

    public LoadingActor(String path){
        this.sprite = new Sprite(new Texture(path));
        setBounds(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
        setOrigin(getWidth()/2, getHeight()/2);
        RotateByAction rba = new RotateByAction();
        rba.setAmount(-360f);
        rba.setDuration(1.35f);
        RepeatAction ra = new RepeatAction();
        ra.setAction(rba);
        ra.setCount(FOREVER);
        this.addAction(ra);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(sprite, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }
}
