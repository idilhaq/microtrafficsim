package ZZZ_NEU_microtrafficsim.osmcreator.user.selection;

import javafx.geometry.Bounds;

/**
 * @author Dominic Parga Cacheiro
 */
public interface Selectable {
    void setSelected(boolean selected);
    boolean isSelected();
    Bounds getBoundsInParent();
}
