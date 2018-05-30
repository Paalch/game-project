package car.superfun.game.actors;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.DelayAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.actions.TouchableAction;
import com.badlogic.gdx.scenes.scene2d.actions.VisibleAction;

public class DelayedButtonActor extends Actor {
    private Sprite sprite;

    /*
     * The DelayedButtonActor creates a simple button from a provided image.
     * It is initially invisible and uninteractable, but appears after x amount of seconds.
     * What happens when it is tapped, has to be set when it is initialized.
     */

    public DelayedButtonActor(String path, float seconds){
        this.sprite = new Sprite(new Texture(path));
        setBounds(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());

        VisibleAction setInvisible = new VisibleAction();
        setInvisible.setVisible(false);

        TouchableAction setUntouchable = new TouchableAction();
        setUntouchable.setTouchable(Touchable.disabled);

        DelayAction delay = new DelayAction(seconds);

        VisibleAction setVisible = new VisibleAction();
        setVisible.setVisible(true);

        TouchableAction setTouchable = new TouchableAction();
        setTouchable.setTouchable(Touchable.enabled);

        SequenceAction actionSequence = new SequenceAction(setInvisible, setUntouchable, delay, setVisible, setTouchable);

        this.addAction(actionSequence);
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
