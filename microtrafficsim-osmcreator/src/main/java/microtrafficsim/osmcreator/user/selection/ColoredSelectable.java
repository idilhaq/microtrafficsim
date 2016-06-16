package microtrafficsim.osmcreator.user.selection;

import javafx.scene.paint.Color;

/**
 * @author Dominic Parga Cacheiro
 */
public interface ColoredSelectable extends Selectable {
  Color getColorSelected();
  Color getStrokeColorSelected();
  Color getColorUnselected();
  Color getStrokeColorUnselected();
}
