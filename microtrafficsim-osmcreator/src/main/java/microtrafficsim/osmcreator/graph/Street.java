package microtrafficsim.osmcreator.graph;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.shape.StrokeType;
import microtrafficsim.utils.hashing.FNVHashBuilder;
import microtrafficsim.utils.hashing.HashBuilder;
import microtrafficsim.osmcreator.Constants;
import microtrafficsim.osmcreator.geometry.Arrow;
import microtrafficsim.osmcreator.user.gestures.selection.ColoredSelectable;

/**
 * @author Dominic Parga Cacheiro
 */
public abstract class Street extends Arrow implements ColoredSelectable {
    private class Binded {
        private DoubleProperty startXProperty, startYProperty, endXProperty, endYProperty;
        private void bind(DoubleProperty startX, DoubleProperty startY, DoubleProperty endX, DoubleProperty endY) {
            // todo Max
            /* origin */
            startXProperty = new SimpleDoubleProperty();
            startXProperty.addListener((observable, oldValue, newValue) -> {
                double radians = Math.atan2(
                        endY.doubleValue() - startY.doubleValue(),
                        endX.doubleValue() - newValue.doubleValue());
                double versatzX = Constants.CROSSROAD_RADIUS * Math.cos(radians) + Constants.STREET_STROKE_WIDTH;
                double versatzY = Constants.CROSSROAD_RADIUS * Math.sin(radians) + Constants.STREET_STROKE_WIDTH;
                setStartX(newValue.doubleValue() + versatzX);
                setStartY(startY.doubleValue() + versatzY);
                setEndX(endX.doubleValue() - versatzX);
                setEndY(endY.doubleValue() - versatzY);
            });
            startYProperty = new SimpleDoubleProperty();
            startYProperty.addListener((observable, oldValue, newValue) -> {
                double radians = Math.atan2(
                        endY.doubleValue() - newValue.doubleValue(),
                        endX.doubleValue() - startX.doubleValue());
                double versatzX = Constants.CROSSROAD_RADIUS * Math.cos(radians) + Constants.STREET_STROKE_WIDTH;
                double versatzY = Constants.CROSSROAD_RADIUS * Math.sin(radians) + Constants.STREET_STROKE_WIDTH;
                setStartX(startX.doubleValue() + versatzX);
                setStartY(newValue.doubleValue() + versatzY);
                setEndX(endX.doubleValue() - versatzX);
                setEndY(endY.doubleValue() - versatzY);
            });
            startXProperty.bind(startX);
            startYProperty.bind(startY);


            /* destination */
            endXProperty = new SimpleDoubleProperty();
            endXProperty.addListener((observable, oldValue, newValue) -> {
                double radians = Math.atan2(
                        endY.doubleValue() - startY.doubleValue(),
                        newValue.doubleValue() - startX.doubleValue());
                double versatzX = Constants.CROSSROAD_RADIUS * Math.cos(radians) + Constants.STREET_STROKE_WIDTH;
                double versatzY = Constants.CROSSROAD_RADIUS * Math.sin(radians) + Constants.STREET_STROKE_WIDTH;
                setStartX(startX.doubleValue() + versatzX);
                setStartY(startY.doubleValue() + versatzY);
                setEndX(newValue.doubleValue() - versatzX);
                setEndY(endY.doubleValue() - versatzY);
            });
            endYProperty = new SimpleDoubleProperty();
            endYProperty.addListener((observable, oldValue, newValue) -> {
                double radians = Math.atan2(
                        newValue.doubleValue() - startY.doubleValue(),
                        endX.doubleValue() - startX.doubleValue());
                double versatzX = Constants.CROSSROAD_RADIUS * Math.cos(radians) + Constants.STREET_STROKE_WIDTH;
                double versatzY = Constants.CROSSROAD_RADIUS * Math.sin(radians) + Constants.STREET_STROKE_WIDTH;
                setStartX(startX.doubleValue() + versatzX);
                setStartY(startY.doubleValue() + versatzY);
                setEndX(endX.doubleValue() - versatzX);
                setEndY(newValue.doubleValue() - versatzY);
            });
            endXProperty.bind(endX);
            endYProperty.bind(endY);
        }
        public void unbind() {
            startXProperty.unbind();
            startYProperty.unbind();
            endXProperty.unbind();
            endYProperty.unbind();
        }
    }

    public final Crossroad origin, destination;
    private final Binded binded;
    private StreetDirection direction;
    private boolean isSelected;
    public long ID;

    /**
     * NOTE: INCLUSIVE POSITION BINDING!
     * @param origin
     * @param destination
     */
    public Street(Crossroad origin, Crossroad destination) {
        super(origin.getCenterX(), origin.getCenterY(), destination.getCenterX(), destination.getCenterY());
        this.origin = origin;
        this.destination = destination;
        direction = StreetDirection.FORWARDS;

//    setLook(); // todo delete
        setSelected(false);

        binded = new Binded();
        binded.bind(
                origin.translateXProperty(),
                origin.translateYProperty(),
                destination.translateXProperty(),
                destination.translateYProperty());

    }

    public void unbind() {
        binded.unbind();
    }

    public void removeFromCrossroads() {
        origin.remove(this);
        destination.remove(this);
    }

    public StreetDirection getStreetDirectionFrom(Crossroad crossroad) {
        if (direction == StreetDirection.BIDIRECTIONAL)
            return StreetDirection.BIDIRECTIONAL;
        if (crossroad == origin)
            return StreetDirection.FORWARDS;
        if (crossroad == destination)
            return StreetDirection.BACKWARDS;
        return null;
    }

    public void mergeStreetDirection(StreetDirection direction) {
        this.direction = StreetDirection.merge(this.direction, direction);
        switch (this.direction) {
            case FORWARDS:
                setOriginHeadVisible(false);
                setDestinationHeadVisible(true);
                break;
            case BACKWARDS:
                setOriginHeadVisible(true);
                setDestinationHeadVisible(false);
                break;
            case BIDIRECTIONAL:
                setOriginHeadVisible(true);
                setDestinationHeadVisible(true);
                break;
        }
    }

    /*
    |================|
    | (i) Selectable |
    |================|
    */
    @Override
    public void setSelected(boolean value) {
        isSelected = value;
        if (isSelected) {
            setFill(getColorSelected());
            setStroke(getStrokeColorSelected());
            setStrokeWidth(Constants.STREET_STROKE_WIDTH);
            setStrokeType(StrokeType.OUTSIDE);
        } else {
            setFill(getColorUnselected());
            setStroke(getStrokeColorUnselected());
            setStrokeWidth(Constants.STREET_STROKE_WIDTH);
            setStrokeType(StrokeType.OUTSIDE);
        }
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    /*
    |============|
    | (c) Object |
    |============|
    */
    /**
     * @return hash(origin, destination) xor hash(destination, origin), so the order of the crossroads doesn't matter
     */
    @Override
    public int hashCode() {
        HashBuilder hashBuilder = new FNVHashBuilder();
        // forwards
        int hash = hashBuilder.add(origin).add(destination).getHash();
        hashBuilder.reset();
        // backwards
        hash = hash ^ hashBuilder.add(destination).add(origin).getHash();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Street && hashCode() == obj.hashCode();
    }
}
