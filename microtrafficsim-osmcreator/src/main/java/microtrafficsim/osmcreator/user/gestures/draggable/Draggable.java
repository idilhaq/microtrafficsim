package microtrafficsim.osmcreator.user.gestures.draggable;


import javafx.scene.Parent;

/**
 * @author Dominic Parga Cacheiro
 */
public interface Draggable {
    void prepareDragging(Parent root, double x, double y);
    void drag(Parent pane, double x, double y);
}
