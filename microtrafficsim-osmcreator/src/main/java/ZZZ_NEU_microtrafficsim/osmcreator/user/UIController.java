package ZZZ_NEU_microtrafficsim.osmcreator.user;

import ZZZ_NEU_microtrafficsim.osmcreator.Constants;
import ZZZ_NEU_microtrafficsim.osmcreator.geometry.GraphPane;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import microtrafficsim.math.MathUtils;

/**
 * @author Dominic Parga Cacheiro
 */
public class UIController {
    private class Wrapper<T> {
        T value;
        public Wrapper() {}
        public Wrapper(T value) {
            this.value = value;
        }
    }
    private class DragContext {

        double mouseAnchorX;
        double mouseAnchorY;

        double translateAnchorX;
        double translateAnchorY;
    }

    public void addAllEventHandlers(Scene scene, GraphPane graph) {
        /* declarations */
        DragContext dragContext = new DragContext();
        Wrapper<Double> zoomLevel = new Wrapper<>(0d);





        /* scene events */
        Wrapper<Boolean> sceneDragActive = new Wrapper<>();
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            sceneDragActive.value = false;

            /* prepare drag */
            if(mouseEvent.isSecondaryButtonDown()) {
                dragContext.mouseAnchorX = mouseEvent.getSceneX();
                dragContext.mouseAnchorY = mouseEvent.getSceneY();

                dragContext.translateAnchorX = graph.getTranslateX();
                dragContext.translateAnchorY = graph.getTranslateY();
            }
        });


        scene.addEventHandler(MouseEvent.DRAG_DETECTED, event -> {
            sceneDragActive.value = true;
        });


        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEvent -> {
            if (sceneDragActive.value) {
                if(mouseEvent.isSecondaryButtonDown()) {
                    graph.setTranslateX(dragContext.translateAnchorX + mouseEvent.getSceneX() - dragContext.mouseAnchorX);
                    graph.setTranslateY(dragContext.translateAnchorY + mouseEvent.getSceneY() - dragContext.mouseAnchorY);

                    mouseEvent.consume();
                }
            }
        });


        scene.addEventHandler(ScrollEvent.ANY, scrollEvent -> {
            /* save and clamp new scale */
            double oldZoomLevel = zoomLevel.value;
            zoomLevel.value = MathUtils.clamp(zoomLevel.value + scrollEvent.getDeltaY() * Constants.ZOOM_LEVEL_FACTOR,
                    Constants.MIN_ZOOM_LEVEL,
                    Constants.MAX_ZOOM_LEVEL);
            double scale = Math.pow(2, zoomLevel.value);
            double oldScale = graph.getScaleX();

            /* save delta depending on mouse movement */
            double dx = (scrollEvent.getSceneX() - (graph.getBoundsInParent().getWidth()/2 + graph.getBoundsInParent().getMinX()));
            double dy = (scrollEvent.getSceneY() - (graph.getBoundsInParent().getHeight()/2 + graph.getBoundsInParent().getMinY()));

            /* update scale */
            graph.setScaleX(scale);
            graph.setScaleY(scale);

            /* update pivot */
            graph.setTranslateX(graph.getTranslateX() - (scale / oldScale - 1) * dx);
            graph.setTranslateY(graph.getTranslateY() - (scale / oldScale - 1) * dy);
        });






        /* graph events */
        graph.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            if (!sceneDragActive.value) {
                graph.addNewCrossroad(mouseEvent.getX(), mouseEvent.getY());
            }
        });
    }
}
