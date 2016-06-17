package ZZZ_NEU_microtrafficsim.osmcreator.geometry;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;
import ZZZ_NEU_microtrafficsim.osmcreator.model.GraphModel;
import ZZZ_NEU_microtrafficsim.osmcreator.model.crossroads.Crossroad;
import ZZZ_NEU_microtrafficsim.osmcreator.user.gestures.NodeGestures;

/**
 * @author Dominic Parga Cacheiro
 */
public class GraphPane extends Region {

    private GraphModel model;
    private Projection projection;
    private NodeGestures nodeGestures;

    private Group transform;

    public GraphPane() {
        model = new GraphModel();
        projection = new MercatorProjection();
        nodeGestures = new NodeGestures(this);

        Rect2d projectedMaximumBounds = projection.getProjectedMaximumBounds();
        double width = projectedMaximumBounds.xmax - projectedMaximumBounds.xmin;
        double height = projectedMaximumBounds.ymax - projectedMaximumBounds.ymin;
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);

        setScaleY(-1);              // invert y-axis

        transform = new Group();    // translation
        transform.setTranslateX(-projectedMaximumBounds.xmin);
        transform.setTranslateY(-projectedMaximumBounds.ymin);
        getChildren().add(transform);
    }

    public void addNewCrossroad(double x, double y) {
        Rect2d bounds = projection.getProjectedMaximumBounds();
        x += bounds.xmin;
        y += bounds.ymin;

        System.out.println(x + ", " + y);       // TODO: remove

        /* mouse event sends position of graphpane's local coordinate system */
        Crossroad newCrossroad = model.createCrossroad(0, 0);
        newCrossroad.setTranslateX(x);
        newCrossroad.setTranslateY(y);
        transform.getChildren().add(newCrossroad);

        /* bind for better look */
        DoubleProperty scaleX = new SimpleDoubleProperty();
        scaleX.addListener((observable, oldValue, newValue) -> {
            newCrossroad.setScaleX(1/newValue.doubleValue());
        });
        scaleX.bind(scaleXProperty());
        DoubleProperty scaleY = new SimpleDoubleProperty();
        scaleY.addListener((observable, oldValue, newValue) -> {
            newCrossroad.setScaleY(1/newValue.doubleValue());
        });
        scaleY.bind(scaleXProperty());


        /* add event listeners */
        newCrossroad.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            nodeGestures.getOnMousePressedEventHandler().handle(mouseEvent);
            newCrossroad.setSelected(true);
        });
        newCrossroad.addEventHandler(MouseEvent.MOUSE_DRAGGED, nodeGestures.getOnMouseDraggedEventHandler());
        newCrossroad.addEventHandler(MouseEvent.MOUSE_CLICKED, Event::consume);
    }

    public void center() {
        setTranslateX(getScene().getWidth() / 2 - getPrefWidth() / 2);
        setTranslateY(getScene().getHeight() / 2 - getPrefHeight() / 2);
    }
}
