package car.superfun.game.maps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import car.superfun.game.GlobalVariables;
import car.superfun.game.maps.UserDataCreater;

public class TrackBuilder {

    /**
     * Used to get starting points on the map for the cars.
     * @param map
     * @param layerName
     * @return
     */
    public static Array<Vector2> getPoints(Map map, String layerName) {
        MapObjects mapObjects = map.getLayers().get(layerName).getObjects();
        Array<Vector2> points = new Array<>();

        for (MapObject mapObject : mapObjects) {
            float x = Float.parseFloat(mapObject.getProperties().get("x").toString());
            float y = Float.parseFloat(mapObject.getProperties().get("y").toString());
            points.add(new Vector2(x, y));
        }
        return points;
    }

    /**
     * Build a layer from the tiled map.
     * @param map
     * @param world
     * @param layerName
     * @param fixtureDef
     * @return
     */
    public static Array<Body> buildLayer(Map map, World world, String layerName, FixtureDef fixtureDef) {
        MapObjects mapObjects = map.getLayers().get(layerName).getObjects();
        Array<Body> bodies = new Array<>();

        for (MapObject mapObject : mapObjects) {
            Shape shape;
            try {
                shape = getShape(mapObject);
            } catch (ClassNotFoundException ex) {
                Gdx.app.error("Can't find class for mapObject", mapObject.toString());
                return bodies;
            }

            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;

            fixtureDef.shape = shape;

            Body body = world.createBody(bodyDef);
            body.createFixture(fixtureDef);
            bodies.add(body);
            shape.dispose();
        }

        return bodies;
    }

    /**
     * Build a layer from the tiled map, with user data.
     * @param map
     * @param world
     * @param layerName
     * @param fixtureDef
     * @param userDataCreater
     * @return
     */
    public static Array<Body> buildLayerWithUserData(Map map, World world, String layerName, FixtureDef fixtureDef, UserDataCreater userDataCreater) {
        MapObjects mapObjects = map.getLayers().get(layerName).getObjects();
        Array<Body> bodies = new Array<>();

        for (MapObject mapObject : mapObjects) {
            Shape shape;
            try {
                shape = getShape(mapObject);
            } catch (ClassNotFoundException ex) {
                Gdx.app.error("Can't find class for mapObject", mapObject.toString());
                return bodies;
            }

            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;

            fixtureDef.shape = shape;

            Body body = world.createBody(bodyDef);
            body.setUserData(userDataCreater.getUserData());
            body.createFixture(fixtureDef);
            bodies.add(body);
            shape.dispose();
        }

        return bodies;
    }

    /**
     * Get the specific shape from the tile object.
     * @param mapObject
     * @return
     * @throws ClassNotFoundException
     */
    private static Shape getShape(MapObject mapObject) throws ClassNotFoundException {
        Shape shape;
        if (mapObject instanceof PolylineMapObject) { // Test polylines first, since this is the objects we are actually using
            shape = getPolyline((PolylineMapObject)mapObject);
        }
        /*
         * NOT IN USE
        else if (mapObject instanceof TextureMapObject) {
            shape = getTile((TiledMapTileMapObject) mapObject);
        }
        else if (mapObject instanceof PolygonMapObject) {
            shape = getPolygon((PolygonMapObject)mapObject);
        }
        else if (mapObject instanceof CircleMapObject) {
            shape = getCircle((CircleMapObject)mapObject);
        }
        */
        else {
            throw new ClassNotFoundException("Cannot find class for mapObject");
        }
        return shape;
    }

    private static ChainShape getPolyline(PolylineMapObject polylineMapObject) {
        float[] vertices = polylineMapObject.getPolyline().getTransformedVertices();
        Vector2[] worldVertices = new Vector2[vertices.length / 2];

        for (int i = 0; i < vertices.length / 2; ++i) {
            worldVertices[i] = new Vector2();
            worldVertices[i].x = vertices[i * 2] / GlobalVariables.PIXELS_TO_METERS;
            worldVertices[i].y = vertices[i * 2 + 1] / GlobalVariables.PIXELS_TO_METERS;
        }

        ChainShape chainShape = new ChainShape();
        chainShape.createChain(worldVertices);
        return chainShape;
    }

    /**
     * NOT IN USE
     * This is not being used yet, but will be used to register object tiles from Tiled in the future
     * @param tileObject
     * @return

    private static PolygonShape getTile(TiledMapTileMapObject tileObject) {

        float width = tileObject.getTextureRegion().getRegionWidth() / (2 * GlobalVariables.PIXELS_TO_METERS);
        float height = tileObject.getTextureRegion().getRegionHeight() / (2 * GlobalVariables.PIXELS_TO_METERS);

        Vector2 center = new Vector2((tileObject.getX() / GlobalVariables.PIXELS_TO_METERS) + width, (tileObject.getY() / GlobalVariables.PIXELS_TO_METERS) + height);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width, height, center, 0f);
        return shape;
    }
     */



    /**
     * NOT IN USE
     * Not in used at the present time.
     * Nor is it possible to register rectangle objects from Tiled now, so when that will be implemented this method might be used.
     * @param rectangleObject
     * @return
    private static PolygonShape getRectangle(RectangleMapObject rectangleObject) {
        Rectangle rectangle;
        PolygonShape polygonShape;
        Vector2 size;

        rectangle = rectangleObject.getRectangle();
        polygonShape = new PolygonShape();
        size = new Vector2((rectangle.x + rectangle.width * 0.5f) / GlobalVariables.PIXELS_TO_METERS,(rectangle.y + rectangle.height * 0.5f ) / GlobalVariables.PIXELS_TO_METERS);
        polygonShape.setAsBox(rectangle.width * 0.5f / GlobalVariables.PIXELS_TO_METERS, rectangle.height * 0.5f / GlobalVariables.PIXELS_TO_METERS, size, 0.0f);
        return polygonShape;
    }
     */


    /**
     * NOT IN USE
     * This has not been tested, as we don't use circles yet.
     * @param circleMapObject
     * @return
    private static CircleShape getCircle(CircleMapObject circleMapObject) {
        Circle circle;
        CircleShape circleShape;

        circle = circleMapObject.getCircle();
        circleShape = new CircleShape();
        circleShape.setRadius(circle.radius / GlobalVariables.PIXELS_TO_METERS);
        circleShape.setPosition(new Vector2(circle.x / GlobalVariables.PIXELS_TO_METERS, circle.y / GlobalVariables.PIXELS_TO_METERS));
        return circleShape;
    }
     */

    /**
     * NOT IN USE
     * There seems to be something fishy about this method, but we are not using polygon/rectangle map objects yet.
     * @param polygonMapObject
     * @return
    private static PolygonShape getPolygon(PolygonMapObject polygonMapObject) {
        PolygonShape polygonShape;
        float[] vertices;
        float[] worldVertices;

        polygonShape = new PolygonShape();
        vertices = polygonMapObject.getPolygon().getTransformedVertices();
        worldVertices = new float[vertices.length];

        for (int i = 0; i < vertices.length; ++i) {
            worldVertices[i] = vertices[i] / GlobalVariables.PIXELS_TO_METERS;
        }

        polygonShape.set(worldVertices);
        return polygonShape;
    }
     */


    /**
     * Gets the polyline shapes
     * @param polylineMapObject
     * @return
     */
}