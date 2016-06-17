package ZZZ_NEU_microtrafficsim.osmcreator;

import javafx.scene.paint.Color;

/**
 * @author Dominic Parga Cacheiro
 */
public class Constants {

    /* general */
    public static final int INITIALIZE_SCREEN_WIDTH = 1200;
    public static final int INITIALIZE_SCREEN_HEIGHT = 675;

    /* selection */
    public static final double SELECTION_STROKE_WIDTH = 1;
    public static final Color SELECTION_STROKE_COLOR = Color.BLUE;
    public static final Color SELECTION_FILL_COLOR = Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6);

    /* graph */
    public static final String GRAPH_BACKGROUND_STYLE = "-fx-background-color: lightgrey; -fx-border-color: blue;";

    /* scaling */
    public static final double MAX_ZOOM_LEVEL = 19;
    public static final double MIN_ZOOM_LEVEL = 0;
    public static final double ZOOM_LEVEL_FACTOR = 0.01;

    /* crossroads */
    public static final double CROSSROAD_RADIUS = 10;
    public static final int CROSSROAD_STROKE_WIDTH = 0;
    public static final Color CROSSROAD_COLOR_UNSEL = Color.GOLD;
    public static final Color CROSSROAD_STROKE_COLOR_UNSEL = Color.GOLD;
    public static final Color CROSSROAD_COLOR_SEL = Color.ROSYBROWN;
    public static final Color CROSSROAD_STROKE_COLOR_SEL = Color.ROSYBROWN;
}
