package microtrafficsim.osmcreator;

import javafx.scene.paint.Color;

/**
 * @author Dominic Parga Cacheiro
 */
public final class Constants {

//    public static final int INITIALZE_SCREEN_WIDTH = 1200;
//    public static final int INITIALZE_SCREEN_HEIGHT = 675;
    public static final int INITIALZE_SCREEN_WIDTH = 1600;
    public static final int INITIALZE_SCREEN_HEIGHT = 900;
    public static final int PAUSE_TRANSITION_MILLIS = 80;

    public static final int METERS_PER_PIXEL = 1;
    public static final String GRAPH_BACKGROUND_STYLE = "-fx-background-color: lightgrey; -fx-border-color: blue;";

    public static final boolean SCROLL_ENABLED = true;
    public static final boolean SHOW_USER_EVENTS = false;

    public static final Color SCENE_COLOR = Color.ALICEBLUE;

    /* zoomable */
    public static final double MIN_ZOOM_LEVEL = 0;
    public static final double MAX_ZOOM_LEVEL = 19;
    public static final double ZOOM_LEVEL_FACTOR = 0.01;

    /* selection rectangle */
    public static final double SELECTION_STROKE_WIDTH = 1;
    public static final Color SELECTION_STROKE_COLOR = Color.BLUE;
    public static final Color SELECTION_FILL_COLOR = Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6);

    /* crossroads */
    public static final double CROSSROAD_RADIUS = 5;
    public static final int CROSSROAD_STROKE_WIDTH_UNSEL= 4;
    public static final int CROSSROAD_STROKE_WIDTH_SEL = 8;
    public static final Color CROSSROAD_COLOR_SEL = Color.rgb(214, 30, 36);
    public static final Color CROSSROAD_COLOR_UNSEL = Color.rgb(214, 30, 36);

    /* streets */
    public static final int STREET_STROKE_WIDTH_UNSEL= 3;
    public static final int STREET_STROKE_WIDTH_SEL = 4;
    public static final Color STREET_COLOR_MOTORWAY = Color.rgb(237, 120, 52);
    public static final Color STREET_COLOR_PRIMARY = Color.rgb(255, 197, 38);
    public static final Color STREET_COLOR_SECONDARY = Color.rgb(143, 166, 63);
    public static final Color STREET_COLOR_RESIDENTIAL = Color.rgb(0, 166, 155);

    /* geometry: Arrow */
    public static double ARROW_HEAD_LINE_LENGTH = 12;
    public static double ARROW_HEAD_LINE_RADIANS = Math.toRadians(30);
}
