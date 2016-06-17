package ZZZ_zoomable_microtrafficsim.osmcreator.user.gestures.draggable;


import ZZZ_zoomable_microtrafficsim.osmcreator.graph.OSMGraphPane;

/**
 * @author Dominic Parga Cacheiro
 */
public interface Draggable {
    void prepareDragging(OSMGraphPane root, double x, double y);
    void drag(OSMGraphPane pane, double x, double y);





//  DragDelta getDragDelta();
//  void setDragDelta(double x, double y);

//  double getDragX();
//  void setDragX(double x);
//  double getDragY();
//  void setDragY(double y);
//  default void updateDragDelta(MouseEvent mouseEvent) {
//    setDragDelta(
//            getDragX() - mouseEvent.getSceneX(),
//            getDragY() - mouseEvent.getSceneY());
//  }
//  default void updateDragPosition(MouseEvent mouseEvent) {
//    double newX = mouseEvent.getSceneX() + getDragDelta().x;
//    if (newX > 0 && newX < getScene().getWidth())
//    setDragX(newX);
//    double newY = mouseEvent.getSceneY() + getDragDelta().y;
//    if (newY > 0 && newY < getScene().getHeight())
//    setDragY(newY);
//  }
}
