package ZZZ_microtrafficsim.osmcreator;

import javafx.scene.paint.Color;

/**
 * @author Dominic Parga Cacheiro
 */
public final class Constants {

  public static final int INITIALZE_SCREEN_WIDTH = 1200;
  public static final int INITIALZE_SCREEN_HEIGHT = 675;
  public static final int PAUSE_TRANSITION_MILLIS = 80;

  public static final int METERS_PER_PIXEL = 1;

  public static final boolean SCROLL_ENABLED = true;
  public static final boolean SHOW_USER_EVENTS = false;

  public static final Color SCENE_COLOR = Color.ALICEBLUE;

  /* selection rectangle */
  public static final double SELECTION_STROKE_WIDTH = 1;
  public static final Color SELECTION_STROKE_COLOR = Color.BLUE;
  public static final Color SELECTION_FILL_COLOR = Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6);

  /* crossroads */
  public static final double CROSSROAD_RADIUS = 10;
  public static final int CROSSROAD_STROKE_WIDTH = 0;
  public static final Color CROSSROAD_COLOR_UNSEL = Color.GOLD;
  public static final Color CROSSROAD_COLOR_SEL = Color.ROSYBROWN;

  /* streets */
  public static final int STREET_STROKE_WIDTH = 2;
  public static final Color STREET_COLOR_UNSEL = Color.GOLD;//.deriveColor(1,1,1,0.5);
  public static final Color STREET_COLOR_SEL = Color.ROSYBROWN;
}
