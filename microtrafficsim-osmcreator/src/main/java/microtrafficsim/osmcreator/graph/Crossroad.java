package microtrafficsim.osmcreator.graph;

import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import microtrafficsim.osmcreator.Constants;
import microtrafficsim.osmcreator.user.gestures.draggable.DragContext;
import microtrafficsim.osmcreator.user.gestures.draggable.Draggable;
import microtrafficsim.osmcreator.user.gestures.selection.ColoredSelectable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A draggable anchor displayed around a point.
 *
 * @author Dominic Parga Cacheiro
 */
public abstract class Crossroad extends Circle implements Draggable, ColoredSelectable {

    private Map<Street, Street> streets;
    private boolean isSelected;
    public long ID;

    public Crossroad(double x, double y) {
        super(0, 0, Constants.CROSSROAD_RADIUS);
        setTranslateX(x);
        setTranslateY(y);
        streets = new HashMap<>();
        dragContext = new DragContext();
        setSelected(false);
    }

    /**
     *
     * @return {@code true} if street didn't exist before
     */
    public boolean add(Street street) {
        Street original = streets.get(street);
        if (original != null) {
            StreetDirection other = street.getStreetDirectionFrom(this);
            // if this crossroad should add itself as origin (destination) when it is already destination (origin)
            if (original.origin != this)
                other = StreetDirection.invert(other);
            original.mergeStreetDirection(other);
            return false;
        } else {
            streets.put(street, street);
            return true;
        }
    }

    void remove(Street street) {
        streets.remove(street);
    }

    public Set<Street> getStreets() {
        return new HashSet<Street>(streets.keySet());
    }

    /*
    |===============|
    | (i) Draggable |
    |===============|
    */
    private DragContext dragContext;

    @Override
    public void prepareDragging(Parent pane, double x, double y) {
        dragContext.mouseAnchorX = x;
        dragContext.mouseAnchorY = y;

        dragContext.translateAnchorX = getTranslateX();
        dragContext.translateAnchorY = getTranslateY();
    }

    @Override
    public void drag(Parent pane, double x, double y) {
        /* save important data for recalculating new position*/
        double scaleX = pane.getScaleX();
        double scaleY = pane.getScaleY();
        // important in case that node ends up over another node
        Bounds oldBounds = localToParent(getBoundsInLocal());

//        System.out.println(oldBounds);

        double translateX = dragContext.translateAnchorX + ((x - dragContext.mouseAnchorX) / scaleX);
        double translateY = dragContext.translateAnchorY + ((y - dragContext.mouseAnchorY) / scaleY);

//        translateX = MathUtils.clamp(
//                translateX,
//                node.getParent().getBoundsInLocal().getMinX(), // todo
//                node.getParent().getBoundsInLocal().getMaxX()); // todo
//        translateY = MathUtils.clamp(
//                translateY,
//                node.getParent().getBoundsInLocal().getMinY(), // todo
//                node.getParent().getBoundsInLocal().getMaxY()); // todo
        setTranslateX(translateX);
        setTranslateY(translateY);
//        System.out.println(translateX);
//        System.out.println(translateY);
//        System.out.println(((Circle)node).getCenterX());
//        System.out.println(node);
//        System.out.println();
    }




    //    @Override todo delete
//    public DragDelta getDragDelta() {
//        return dragDelta;
//    }
//
//    @Override
//    public void setDragDelta(double x, double y) {
//        dragDelta.x = x;
//        dragDelta.y = y;
//    }
//
//    @Override
//    public double getDragX() {
//        return getCenterX();
//    }
//
//    @Override
//    public void setDragX(double x) {
//        setCenterX(x);
//    }
//
//    @Override
//    public double getDragY() {
//        return getCenterY();
//    }
//
//    @Override
//    public void setDragY(double y) {
//        setCenterY(y);
//    }
    
    /*
    |================|
    | (i) Selectable |
    |================|
    */
    @Override
    public void refreshLook() {
        if (isSelected) {
            setFill(getColorSelected());
            setStroke(getStrokeColorSelected());
            setStrokeWidth(Constants.CROSSROAD_STROKE_WIDTH_SEL);
            setStrokeType(StrokeType.CENTERED);
        } else {
            setFill(getColorUnselected());
            setStroke(getStrokeColorUnselected());
            setStrokeWidth(Constants.CROSSROAD_STROKE_WIDTH_UNSEL);
            setStrokeType(StrokeType.CENTERED);
        }
    }

    @Override
    public void setSelected(boolean value) {
        isSelected = value;
        refreshLook();
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }
}
