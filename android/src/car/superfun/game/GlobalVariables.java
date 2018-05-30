package car.superfun.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;


public final class GlobalVariables {

    // Common Filters
    public static final short ALL_ENTITIES = -1;  // -1 means all ones, all entities

    public static final short PLAYER_ENTITY = 0b0001;
    public static final short WALL_ENTITY = 0b0010;
    public static final short OPPONENT_ENTITY = 0b0100;

    // Game mode specific filters are made from 0b 0000 0001 0000 0000 and up.
    // I.e. leave the first 8 entity bits for globals

    // For converting between coordinate systems
    public static final float PIXELS_TO_METERS = 100f;

    // Volume
    public static float MUSIC_VOLUME = 1f;
    public static float SOUND_VOLUME = 1f;

    // Enable testing mode to make localCar drive by itself
    public static final boolean TESTING_MODE = false;

    public static final boolean SINGLE_PLAYER = false;

    public static final int START_DELAY_MS = 1500;

    // Below are some logging methods / class that are useful for debug and tweaking purposes

    public static void logVector(Vector2 vector, String tag) {
        Gdx.app.log(tag, "(" + vector.x + ", " + vector.y + ")");
    }

    public static void logVector(Vector2 vector) {
        logVector(vector, "Vector log: ");
    }

    public class AvgLogger {

        private float[] ringBuffer;
        private int index;
        private float worstValue;

        private final String tag;
        private final float precision;
        private final boolean logAll;

        public AvgLogger(int bufferLength, String tag, float precision, boolean logAll) {
            ringBuffer = new float[bufferLength];
            index = 0;
            this.tag = tag;
            worstValue = 0;
            this.precision = precision;
            this.logAll = logAll;
        }

        public void log(float value) {
            ringBuffer[index] = value;
            if (index == ringBuffer.length - 1) {
                index = 0;
            } else {
                index++;
            }
            logAvg();
        }

        private void logAvg() {
            float sum = 0;
            for (float f : ringBuffer) {
                sum = sum + f;
            }
            float avg = sum / ringBuffer.length;
            if (logAll) {
                Gdx.app.log(tag, "" + avg);
            }
            if (avg > worstValue) {
                worstValue = avg;
                Gdx.app.log(tag, "new worst value: " + avg);
            }
            worstValue -= precision;
        }
    }
}
