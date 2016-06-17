package ZZZ_NEU_microtrafficsim.osmcreator.user.gestures;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * Listeners for making the nodes draggable via left mouse button. Considers if parent is zoomed.
 */
public class NodeGestures {

    private DragContext dragContext = new DragContext();

    private Node root;

    public NodeGestures( Node root) {
        this.root = root;

    }

    public EventHandler<MouseEvent> getOnMousePressedEventHandler() {
        return onMousePressedEventHandler;
    }

    public EventHandler<MouseEvent> getOnMouseDraggedEventHandler() {
        return onMouseDraggedEventHandler;
    }

    private EventHandler<MouseEvent> onMousePressedEventHandler = event -> {

        // left mouse button => dragging
        if( !event.isPrimaryButtonDown())
            return;

        dragContext.mouseAnchorX = event.getSceneX();
        dragContext.mouseAnchorY = event.getSceneY();

        Node node = (Node) event.getSource();

        dragContext.translateAnchorX = node.getTranslateX();
        dragContext.translateAnchorY = node.getTranslateY();

    };

    private EventHandler<MouseEvent> onMouseDraggedEventHandler = event -> {
        // left mouse button => dragging
        if(!event.isPrimaryButtonDown())
            return;

        /* save important data for recalculating new position*/
        double scaleX = root.getScaleX();
        double scaleY = root.getScaleY();
        Node node = (Node) event.getSource();
        // important in case that node ends up over another node
        Bounds oldBounds = node.localToParent(node.getBoundsInLocal());

//        System.out.println(oldBounds);

        double translateX = dragContext.translateAnchorX + ((event.getSceneX() - dragContext.mouseAnchorX) / scaleX);
        double translateY = dragContext.translateAnchorY + ((event.getSceneY() - dragContext.mouseAnchorY) / scaleY);

//        translateX = MathUtils.clamp(
//                translateX,
//                node.getParent().getBoundsInLocal().getMinX(), // todo
//                node.getParent().getBoundsInLocal().getMaxX()); // todo
//        translateY = MathUtils.clamp(
//                translateY,
//                node.getParent().getBoundsInLocal().getMinY(), // todo
//                node.getParent().getBoundsInLocal().getMaxY()); // todo
        node.setTranslateX(translateX);
        node.setTranslateY(translateY);
//        System.out.println(translateX);
//        System.out.println(translateY);
//        System.out.println(((Circle)node).getCenterX());
//        System.out.println(node);
//        System.out.println();

        event.consume();
    };
}