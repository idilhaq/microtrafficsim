package ZZZ_zoomable_microtrafficsim.osmcreator.graph;

import ZZZ_zoomable_microtrafficsim.osmcreator.Constants;
import javafx.scene.layout.Pane;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;

/**
 * @author Dominic Parga Cacheiro
 */
public class OSMGraphPane extends Pane {

    private Projection projection;

    public OSMGraphPane() {
        projection = new MercatorProjection();

        Rect2d projectedMaximumBounds = projection.getProjectedMaximumBounds();
        double width = projectedMaximumBounds.xmax - projectedMaximumBounds.xmin;
        double height = projectedMaximumBounds.ymax - projectedMaximumBounds.ymin;
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);

        setStyle(Constants.GRAPH_BACKGROUND_STYLE);
    }

    public void center() {
        setTranslateX(getScene().getWidth() / 2 - getPrefWidth() / 2);
        setTranslateY(getScene().getHeight() / 2 - getPrefHeight() / 2);
    }
}
