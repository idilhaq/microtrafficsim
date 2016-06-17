package ZZZ_zoomable_microtrafficsim.osmcreator.geometry;

import javafx.scene.Group;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;

/**
 * @author Dominic Parga Cacheiro
 */
public class Arrow extends Group {

    private static double HEAD_LINE_LENGTH = 10;
    private static double HEAD_LINE_RADIANS = Math.toRadians(45);

    private Line line, destinationHeadTop, destinationHeadBottom, originHeadTop, originHeadBottom;

    public Arrow() {
        this(0, 0, 0, 0);
    }

    public Arrow(double startX, double startY, double endX, double endY) {
        line = new Line(0, 0, 0, 0);
        destinationHeadTop = new Line(0, 0, 0, 0);
        destinationHeadBottom = new Line(0, 0, 0, 0);
        originHeadTop = new Line(0, 0, 0, 0);
        originHeadBottom = new Line(0, 0, 0, 0);

        /* set caps */
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        destinationHeadTop.setStrokeLineCap(StrokeLineCap.ROUND);
        destinationHeadBottom.setStrokeLineCap(StrokeLineCap.ROUND);
        originHeadTop.setStrokeLineCap(StrokeLineCap.ROUND);
        originHeadBottom.setStrokeLineCap(StrokeLineCap.ROUND);

        /* add all */
        getChildren().addAll(line, destinationHeadTop, destinationHeadBottom, originHeadTop, originHeadBottom);

        setPoints(startX, startY, endX, endY);

        /* finish */
        setOriginHeadVisible(false);
        setDestinationHeadVisible(true);
    }

    public void setFill(Paint value) {
        line.setFill(value);
        destinationHeadTop.setFill(value);
        destinationHeadBottom.setFill(value);
        originHeadTop.setFill(value);
        originHeadBottom.setFill(value);
    }

    public void setStroke(Paint value) {
        line.setStroke(value);
        destinationHeadTop.setStroke(value);
        destinationHeadBottom.setStroke(value);
        originHeadTop.setStroke(value);
        originHeadBottom.setStroke(value);
    }

    public void setStrokeWidth(double value) {
        line.setStrokeWidth(value);
        destinationHeadTop.setStrokeWidth(value);
        destinationHeadBottom.setStrokeWidth(value);
        originHeadTop.setStrokeWidth(value);
        originHeadBottom.setStrokeWidth(value);
    }

    public double getStrokeWidth() {
        return line.getStrokeWidth();
    }

    public void setStrokeType(StrokeType type) {
        line.setStrokeType(type);
        destinationHeadTop.setStrokeType(type);
        destinationHeadBottom.setStrokeType(type);
        originHeadTop.setStrokeType(type);
        originHeadBottom.setStrokeType(type);
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

    /* dest head top */
        destinationHeadTop.setStartX(endX);
        destinationHeadTop.setStartY(endY);
        destinationHeadTop.setEndX(endX + Math.cos(lineRadians + HEAD_LINE_RADIANS) * HEAD_LINE_LENGTH);
        destinationHeadTop.setEndY(endY + Math.sin(lineRadians + HEAD_LINE_RADIANS) * HEAD_LINE_LENGTH);
    /* dest head bottom */
        destinationHeadBottom.setStartX(endX);
        destinationHeadBottom.setStartY(endY);
        destinationHeadBottom.setEndX(endX + Math.cos(lineRadians - HEAD_LINE_RADIANS) * HEAD_LINE_LENGTH);
        destinationHeadBottom.setEndY(endY + Math.sin(lineRadians - HEAD_LINE_RADIANS) * HEAD_LINE_LENGTH);

    /* origin head top */
        originHeadTop.setStartX(startX);
        originHeadTop.setStartY(startY);
        originHeadTop.setEndX(startX - Math.cos(lineRadians + HEAD_LINE_RADIANS) * HEAD_LINE_LENGTH);
        originHeadTop.setEndY(startY - Math.sin(lineRadians + HEAD_LINE_RADIANS) * HEAD_LINE_LENGTH);
    /* origin head bottom */
        originHeadBottom.setStartX(startX);
        originHeadBottom.setStartY(startY);
        originHeadBottom.setEndX(startX - Math.cos(lineRadians - HEAD_LINE_RADIANS) * HEAD_LINE_LENGTH);
        originHeadBottom.setEndY(startY - Math.sin(lineRadians - HEAD_LINE_RADIANS) * HEAD_LINE_LENGTH);

        line.setStartX(startX);
        line.setStartY(startY);
        line.setEndX(endX);
        line.setEndY(endY);
    }

    public void setOriginHeadVisible(boolean visible) {
        originHeadTop.setVisible(visible);
        originHeadBottom.setVisible(visible);
    }

    public void setDestinationHeadVisible(boolean visible) {
        destinationHeadTop.setVisible(visible);
        destinationHeadBottom.setVisible(visible);
    }
}
