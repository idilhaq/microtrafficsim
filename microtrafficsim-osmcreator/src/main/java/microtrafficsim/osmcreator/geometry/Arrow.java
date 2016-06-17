package microtrafficsim.osmcreator.geometry;

import javafx.scene.Group;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import microtrafficsim.osmcreator.Constants;

/**
 * @author Dominic Parga Cacheiro
 */
public class Arrow extends Group {

    private Line line;
    private Polygon originHead, destinationHead;

    public Arrow() {
        this(0, 0, 0, 0);
    }

    public Arrow(double startX, double startY, double endX, double endY) {
        line = new Line(0, 0, 0, 0);
        originHead = new Polygon();
        destinationHead = new Polygon();

        /* set caps */
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        originHead.setStrokeLineCap(StrokeLineCap.ROUND);
        destinationHead.setStrokeLineCap(StrokeLineCap.ROUND);

        /* set stroke type */
        line.setStrokeType(StrokeType.CENTERED);
        originHead.setStrokeType(StrokeType.CENTERED);
        destinationHead.setStrokeType(StrokeType.CENTERED);

        /* add all */
        getChildren().addAll(line, originHead, destinationHead);

        setPoints(startX, startY, endX, endY);

        /* finish */
        setOriginHeadVisible(false);
        setDestinationHeadVisible(true);
    }

    public void setFill(Paint value) {
        line.setFill(value);
        originHead.setFill(value);
        destinationHead.setFill(value);
    }

    public void setStroke(Paint value) {
        line.setStroke(value);
        originHead.setStroke(value);
        destinationHead.setStroke(value);
    }

    public void setStrokeWidth(double value) {
        line.setStrokeWidth(value);
        originHead.setStrokeWidth(value);
        destinationHead.setStrokeWidth(value);
    }

    public double getStrokeWidth() {
        return line.getStrokeWidth();
    }

    public void setStrokeType(StrokeType type) {
        line.setStrokeType(type);
        originHead.setStrokeType(type);
        destinationHead.setStrokeType(type);
    }

    public void setStartX(double startX) {
        setPoints(startX, line.getStartY(), line.getEndX(), line.getEndY());
    }

    public void setStartY(double startY) {
        setPoints(line.getStartX(), startY, line.getEndX(), line.getEndY());
    }

    public void setEndX(double endX) {
        setPoints(line.getStartX(), line.getStartY(), endX, line.getEndY());
    }

    public void setEndY(double endY) {
        setPoints(line.getStartX(), line.getStartY(), line.getEndX(), endY);
    }

    public void setPoints(double startX, double startY, double endX, double endY) {
        double lineRadians = Math.atan2(startY - endY, startX - endX);

        double x0 = endX;
        double y0 = endY;
        double x1 = endX + Math.cos(lineRadians + Constants.ARROW_HEAD_LINE_RADIANS) * Constants.ARROW_HEAD_LINE_LENGTH;
        double y1 = endY + Math.sin(lineRadians + Constants.ARROW_HEAD_LINE_RADIANS) * Constants.ARROW_HEAD_LINE_LENGTH;
        double x2 = endX + Math.cos(lineRadians - Constants.ARROW_HEAD_LINE_RADIANS) * Constants.ARROW_HEAD_LINE_LENGTH;
        double y2 = endY + Math.sin(lineRadians - Constants.ARROW_HEAD_LINE_RADIANS) * Constants.ARROW_HEAD_LINE_LENGTH;
        destinationHead.getPoints().setAll(
                x0, y0,
                x1, y1,
                x2, y2
        );

        x0 = startX;
        y0 = startY;
        x1 = startX - Math.cos(lineRadians + Constants.ARROW_HEAD_LINE_RADIANS) * Constants.ARROW_HEAD_LINE_LENGTH;
        y1 = startY - Math.sin(lineRadians + Constants.ARROW_HEAD_LINE_RADIANS) * Constants.ARROW_HEAD_LINE_LENGTH;
        x2 = startX - Math.cos(lineRadians - Constants.ARROW_HEAD_LINE_RADIANS) * Constants.ARROW_HEAD_LINE_LENGTH;
        y2 = startY - Math.sin(lineRadians - Constants.ARROW_HEAD_LINE_RADIANS) * Constants.ARROW_HEAD_LINE_LENGTH;
        originHead.getPoints().setAll(
                x0, y0,
                x1, y1,
                x2, y2
        );

        line.setStartX(startX);
        line.setStartY(startY);
        line.setEndX(endX);
        line.setEndY(endY);
    }

    public void setOriginHeadVisible(boolean visible) {
        originHead.setVisible(visible);
    }

    public void setDestinationHeadVisible(boolean visible) {
        destinationHead.setVisible(visible);
    }
}
