package microtrafficsim.osmcreator;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import microtrafficsim.osmcreator.graph.Crossroad;
import microtrafficsim.osmcreator.graph.OSMGraphModel;
import microtrafficsim.osmcreator.graph.Street;
import microtrafficsim.osmcreator.graph.crossroads.GoldCrossroad;
import microtrafficsim.osmcreator.graph.streets.GoldStreet;
import microtrafficsim.osmcreator.osm.OSMCreator;
import microtrafficsim.osmcreator.user.controller.UserEvent;
import microtrafficsim.osmcreator.user.controller.UserInputController;
import microtrafficsim.osmcreator.user.controller.UserState;
import microtrafficsim.osmcreator.user.gestures.selection.Selectable;
import microtrafficsim.osmcreator.user.gestures.selection.Selection;
import microtrafficsim.osmcreator.user.gestures.selection.impl.RubberBandSelection;

import java.util.Set;

/**
 * @author Dominic Parga Cacheiro
 */
public class Main extends Application implements UserInputController {

    /* quick fix */
    private boolean selectedCrossroadWasSelected;

    private Pane graph;
    private Group crossroadGroup;
    private Group streetGroup;
    // model
    OSMGraphModel model;
    // user interaction
    private UserState userState;
    private double zoomLevel;
    // selection
    private Selection<Crossroad> selection;
    private Street selectedStreet;
    // osm
    private OSMCreator osmcreator;

    public Main() {
        // model
        model = new OSMGraphModel();

        // user interaction
        zoomLevel = 0;

        // selection
        selection = new RubberBandSelection<>();

        // osm
        osmcreator = new OSMCreator();
    }

    public static void main(String[] args) {
        launch(args);
    }

    /*
    |=================|
    | (c) Application |
    |=================|
    */
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("MicroTrafficSim - OSM creator");
        graph = new Pane();
        crossroadGroup = new Group();
        streetGroup = new Group();
        graph.getChildren().add(crossroadGroup);
        crossroadGroup.getChildren().add(streetGroup);



        /* set graph */
        double width = Constants.INITIALZE_SCREEN_WIDTH;
        double height = Constants.INITIALZE_SCREEN_HEIGHT;
        graph.setPrefSize(width, height);
        graph.setMinSize(width, height);
        graph.setMaxSize(width, height);



        /* prepare crossroad group for dragging groups of crossroads */
        // todo remove
//        crossroadGroup.addEventHandler(MouseEvent.MOUSE_DRAGGED,
//                mouseEvent -> transiate(UserEvent.MOVE_CROSSROADS, mouseEvent, null));



        /* init scene */
        Group group = new Group();
        group.getChildren().add(graph);
        Scene scene = new Scene(
                group,
                Constants.INITIALZE_SCREEN_WIDTH,
                Constants.INITIALZE_SCREEN_HEIGHT,
                Constants.SCENE_COLOR
        );



        /* prepare scene for user input */
        scene.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.BACK_SPACE))
                transiate(UserEvent.DELETE, keyEvent, null);
            else if (keyEvent.getCode().equals(KeyCode.S))
                osmcreator.createOSMFile(primaryStage, graph, model.getStreets());
        });



        /* prepare graph for input */
        PauseTransition clickSceneChiller = new PauseTransition(Duration.millis(Constants.PAUSE_TRANSITION_MILLIS));
        graph.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            switch (mouseEvent.getButton()) {
                case PRIMARY:
                    clickSceneChiller.setOnFinished(event -> transiate(UserEvent.CLICK_SCENE, mouseEvent, null));
                    clickSceneChiller.playFromStart();
                    break;
                case SECONDARY:
                    transiate(UserEvent.RIGHT_CLICK, mouseEvent, null);
                    break;
            }
        });
        graph.addEventHandler(MouseEvent.DRAG_DETECTED, mouseEvent -> {
            clickSceneChiller.stop();
            transiate(UserEvent.START_SELECTION, mouseEvent, null);
        });
        graph.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEvent -> transiate(UserEvent.HOLD_SELECTION, mouseEvent, null));
        graph.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> transiate(UserEvent.STOP_SELECTION, mouseEvent, null));



        /* finish */
        scene.setCursor(Cursor.DEFAULT);
        setUserState(UserState.READY);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /*
    |====================|
    | (i) UserController |
    |====================|
    */
    @Override
    public void setUserState(UserState userState) {
        this.userState = userState;
    }

    @Override
    public void transiate(UserEvent userEvent, InputEvent inputEvent, Node clickedNode) {
        UserState previousState = userState;

        switch (userEvent) {
            case CLICK_SCENE:
                switch (userState) {
                    case READY:
                        MouseEvent mouseEvent = (MouseEvent) inputEvent;
                        createCrossroad(mouseEvent.getX(), mouseEvent.getY());
                        setUserState(UserState.READY);
                        break;
                    case READY_DILIGENT:
                        mouseEvent = (MouseEvent) inputEvent;
                        createCrossroad(mouseEvent.getX(), mouseEvent.getY());
                        setUserState(UserState.READY_DILIGENT);
                        break;
                    case SELECTION_ACTIVE:
                    case SELECTION_ACTIVE_DILIGENT:
                        return;
                    case CROSSROADS_SELECTED:
                        selection.unselectAll();
                        setUserState(UserState.READY);
                        break;
                    case CROSSROADS_SELECTED_DILIGENT:
                        mouseEvent = (MouseEvent) inputEvent;
                        Crossroad newCrossroad = createCrossroad(mouseEvent.getX(), mouseEvent.getY());
                        createStreetsTo(newCrossroad);
                        selection.unselectAll();
                        selection.select(newCrossroad);
                        setUserState(UserState.CROSSROADS_SELECTED_DILIGENT);
                        break;
                    case MOVING_ACTIVE:
                    case MOVING_ACTIVE_DILIGENT:
                        return;
                    case STREET_SELECTED:
                        unselectCurrentStreet();
                        setUserState(UserState.READY);
                        break;
                    case STREET_SELECTED_DILIGENT:
                        unselectCurrentStreet();
                        setUserState(UserState.READY_DILIGENT);
                        break;
                }
                break;


            case PRESS_CROSSROAD:
                Crossroad newCrossroad = (Crossroad)clickedNode;
                selectedCrossroadWasSelected = newCrossroad.isSelected();
                switch (userState) {
                    case READY:
                        selection.select(newCrossroad);
                        setUserState(UserState.CROSSROADS_SELECTED);
                        break;
                    case READY_DILIGENT:
                        selection.select(newCrossroad);
                        setUserState(UserState.CROSSROADS_SELECTED_DILIGENT);
                        break;
                    case SELECTION_ACTIVE:
                    case SELECTION_ACTIVE_DILIGENT:
                        return;
                    case CROSSROADS_SELECTED:
                        selection.select(newCrossroad);
                        setUserState(UserState.CROSSROADS_SELECTED);
                        break;
                    case CROSSROADS_SELECTED_DILIGENT:
                        selection.select(newCrossroad);
                        setUserState(UserState.CROSSROADS_SELECTED_DILIGENT);
                        break;
                    case MOVING_ACTIVE:
                    case MOVING_ACTIVE_DILIGENT:
                        return;
                    case STREET_SELECTED:
                        unselectCurrentStreet();
                        selection.select(newCrossroad);
                        setUserState(UserState.CROSSROADS_SELECTED);
                        break;
                    case STREET_SELECTED_DILIGENT:
                        unselectCurrentStreet();
                        selection.select(newCrossroad);
                        setUserState(UserState.CROSSROADS_SELECTED_DILIGENT);
                        break;
                }
                break;


            case MOVE_CROSSROADS:
                switch (userState) {
                    case READY:
                    case READY_DILIGENT:
                    case SELECTION_ACTIVE:
                    case SELECTION_ACTIVE_DILIGENT:
                        return;
                    case CROSSROADS_SELECTED:
                        newCrossroad = (Crossroad)clickedNode;
                        if (!selectedCrossroadWasSelected) {
                            selection.unselectAll();
                            selection.select(newCrossroad);
                        }
                        MouseEvent mouseEvent = (MouseEvent)inputEvent;
                        selection.getSelectedItems().forEach(crossroad -> {
                            crossroad.prepareDragging(graph, mouseEvent.getSceneX(), mouseEvent.getSceneY());
                        });
                        setUserState(UserState.MOVING_ACTIVE);
                        break;
                    case CROSSROADS_SELECTED_DILIGENT:
                        newCrossroad = (Crossroad)clickedNode;
                        if (!selectedCrossroadWasSelected) {
                            selection.unselectAll();
                            selection.select(newCrossroad);
                        }
                        mouseEvent = (MouseEvent)inputEvent;
                        selection.getSelectedItems().forEach(crossroad -> {
                            crossroad.prepareDragging(graph, mouseEvent.getSceneX(), mouseEvent.getSceneY());
                        });
                        setUserState(UserState.MOVING_ACTIVE_DILIGENT);
                        break;
                    case MOVING_ACTIVE:
                    case MOVING_ACTIVE_DILIGENT:
                        mouseEvent = (MouseEvent)inputEvent;
                        selection.getSelectedItems().forEach(crossroad -> {
                            crossroad.drag(graph, mouseEvent.getSceneX(), mouseEvent.getSceneY());
                        });
                        break;
                    case STREET_SELECTED:
                    case STREET_SELECTED_DILIGENT:
                        return;
                }
                break;


            case RELEASE_CROSSROAD:
                switch (userState) {
                    case READY:
                    case READY_DILIGENT:
                    case SELECTION_ACTIVE:
                    case SELECTION_ACTIVE_DILIGENT:
                        return;
                    case CROSSROADS_SELECTED:
                        newCrossroad = (Crossroad)clickedNode;
                        selection.unselectAll();
                        selection.select(newCrossroad);
                        setUserState(UserState.CROSSROADS_SELECTED);
                        break;
                    case CROSSROADS_SELECTED_DILIGENT:
                        newCrossroad = (Crossroad)clickedNode;
                        createStreetsTo(newCrossroad);
                        selection.unselectAll();
                        selection.select(newCrossroad);
                        setUserState(UserState.CROSSROADS_SELECTED_DILIGENT);
                        break;
                    case MOVING_ACTIVE:
                        setUserState(UserState.CROSSROADS_SELECTED);
                        break;
                    case MOVING_ACTIVE_DILIGENT:
                        setUserState(UserState.CROSSROADS_SELECTED_DILIGENT);
                        break;
                    case STREET_SELECTED:
                    case STREET_SELECTED_DILIGENT:
                        return;
                }
                break;


            case START_SELECTION:
                switch (userState) {
                    case READY:
                        MouseEvent mouseEvent = (MouseEvent)inputEvent;
                        selection.startSelection(graph, mouseEvent.getX(), mouseEvent.getY());
                        setUserState(UserState.SELECTION_ACTIVE);
                        break;
                    case READY_DILIGENT:
                        mouseEvent = (MouseEvent)inputEvent;
                        selection.startSelection(graph, mouseEvent.getX(), mouseEvent.getY());
                        setUserState(UserState.SELECTION_ACTIVE_DILIGENT);
                        break;
                    case SELECTION_ACTIVE:
                    case SELECTION_ACTIVE_DILIGENT:
                        return;
                    case CROSSROADS_SELECTED:
                        mouseEvent = (MouseEvent)inputEvent;
                        selection.startSelection(graph, mouseEvent.getX(), mouseEvent.getY());
                        setUserState(UserState.SELECTION_ACTIVE);
                        break;
                    case CROSSROADS_SELECTED_DILIGENT:
                        mouseEvent = (MouseEvent)inputEvent;
                        selection.startSelection(graph, mouseEvent.getX(), mouseEvent.getY());
                        setUserState(UserState.SELECTION_ACTIVE_DILIGENT);
                        break;
                    case MOVING_ACTIVE:
                    case MOVING_ACTIVE_DILIGENT:
                        return;
                    case STREET_SELECTED:
                        unselectCurrentStreet();
                        mouseEvent = (MouseEvent)inputEvent;
                        selection.startSelection(graph, mouseEvent.getX(), mouseEvent.getY());
                        setUserState(UserState.SELECTION_ACTIVE);
                        break;
                    case STREET_SELECTED_DILIGENT:
                        unselectCurrentStreet();
                        mouseEvent = (MouseEvent)inputEvent;
                        selection.startSelection(graph, mouseEvent.getX(), mouseEvent.getY());
                        setUserState(UserState.SELECTION_ACTIVE_DILIGENT);
                        break;
                }
                break;


            case HOLD_SELECTION:
                switch (userState) {
                    case READY:
                    case READY_DILIGENT:
                        return;
                    case SELECTION_ACTIVE:
                    case SELECTION_ACTIVE_DILIGENT:
                        MouseEvent mouseEvent = (MouseEvent)inputEvent;
                        selection.holdSelection(mouseEvent.getX(), mouseEvent.getY());
                        break;
                    case CROSSROADS_SELECTED:
                    case CROSSROADS_SELECTED_DILIGENT:
                    case MOVING_ACTIVE:
                    case MOVING_ACTIVE_DILIGENT:
                    case STREET_SELECTED:
                    case STREET_SELECTED_DILIGENT:
                        return;
                }
                break;


            case STOP_SELECTION:
                switch (userState) {
                    case READY:
                    case READY_DILIGENT:
                        return;
                    case SELECTION_ACTIVE:
                        selection.stopSelection(graph, model);
                        if (selection.getSelectedItems().isEmpty())
                            setUserState(UserState.READY);
                        else
                            setUserState(UserState.CROSSROADS_SELECTED);
                        break;
                    case SELECTION_ACTIVE_DILIGENT:
                        selection.stopSelection(graph, model);
                        if (selection.getSelectedItems().isEmpty())
                            setUserState(UserState.READY_DILIGENT);
                        else
                            setUserState(UserState.CROSSROADS_SELECTED_DILIGENT);
                        break;
                    case CROSSROADS_SELECTED:
                    case CROSSROADS_SELECTED_DILIGENT:
                    case MOVING_ACTIVE:
                    case MOVING_ACTIVE_DILIGENT:
                    case STREET_SELECTED:
                    case STREET_SELECTED_DILIGENT:
                        return;
                }
                break;


            case CLICK_STREET:
                switch (userState) {
                    case READY:
                        select((Street)clickedNode);
                        setUserState(UserState.STREET_SELECTED);
                        break;
                    case READY_DILIGENT:
                        Street clickedStreet = (Street)clickedNode;
                        select(clickedStreet);
                        setUserState(UserState.STREET_SELECTED_DILIGENT);
                        break;
                    case SELECTION_ACTIVE:
                        return;
                    case CROSSROADS_SELECTED:
                        clickedStreet = (Street)clickedNode;
                        selection.unselectAll();
                        select(clickedStreet);
                        setUserState(UserState.STREET_SELECTED);
                        break;
                    case CROSSROADS_SELECTED_DILIGENT:
                        clickedStreet = (Street)clickedNode;
                        selection.unselectAll();
                        select(clickedStreet);
                        setUserState(UserState.STREET_SELECTED_DILIGENT);
                        break;
                    case MOVING_ACTIVE:
                    case MOVING_ACTIVE_DILIGENT:
                        return;
                    case STREET_SELECTED:
                        clickedStreet = (Street)clickedNode;
                        if (!clickedStreet.isSelected()) {
                            selection.unselectAll();
                            select(clickedStreet);
                            setUserState(UserState.STREET_SELECTED);
                        }
                        break;
                    case STREET_SELECTED_DILIGENT:
                        clickedStreet = (Street)clickedNode;
                        if (!clickedStreet.isSelected()) {
                            selection.unselectAll();
                            select(clickedStreet);
                            setUserState(UserState.STREET_SELECTED_DILIGENT);
                        }
                        break;
                }
                break;


            case DELETE:
                switch (userState) {
                    case READY:
                    case READY_DILIGENT:
                        return;
                    case SELECTION_ACTIVE:
                    case SELECTION_ACTIVE_DILIGENT:
                        return;
                    case CROSSROADS_SELECTED:
                        deleteSelectedCrossroads();
                        selection.unselectAll();
                        setUserState(UserState.READY);
                        break;
                    case CROSSROADS_SELECTED_DILIGENT:
                        deleteSelectedCrossroads();
                        selection.unselectAll();
                        setUserState(UserState.READY_DILIGENT);
                        break;
                    case MOVING_ACTIVE:
                    case MOVING_ACTIVE_DILIGENT:
                        return;
                    case STREET_SELECTED:
                        deleteSelectedStreet();
                        unselectCurrentStreet();
                        setUserState(UserState.READY);
                        break;
                    case STREET_SELECTED_DILIGENT:
                        deleteSelectedStreet();
                        unselectCurrentStreet();
                        setUserState(UserState.READY_DILIGENT);
                        break;
                }
                break;


            case RIGHT_CLICK:
                switch (userState) {
                    case READY:
                        setUserState(UserState.READY_DILIGENT);
                        break;
                    case READY_DILIGENT:
                        setUserState(UserState.READY);
                        break;
                    case SELECTION_ACTIVE:
                        setUserState(UserState.SELECTION_ACTIVE_DILIGENT);
                        break;
                    case SELECTION_ACTIVE_DILIGENT:
                        setUserState(UserState.SELECTION_ACTIVE);
                        break;
                    case CROSSROADS_SELECTED:
                        setUserState(UserState.CROSSROADS_SELECTED_DILIGENT);
                        break;
                    case CROSSROADS_SELECTED_DILIGENT:
                        setUserState(UserState.CROSSROADS_SELECTED);
                        break;
                    case MOVING_ACTIVE:
                        setUserState(UserState.MOVING_ACTIVE_DILIGENT);
                        break;
                    case MOVING_ACTIVE_DILIGENT:
                        setUserState(UserState.MOVING_ACTIVE);
                        break;
                    case STREET_SELECTED:
                        setUserState(UserState.STREET_SELECTED_DILIGENT);
                        break;
                    case STREET_SELECTED_DILIGENT:

                        setUserState(UserState.STREET_SELECTED);
                        break;
                }
                break;
        }
        inputEvent.consume();
        if (Constants.SHOW_USER_EVENTS) {
            System.out.println(previousState + " ---" + userEvent + "---> " + userState);
        }
    }

    /* crossroads */
    private Crossroad createCrossroad(double x, double y) {
        Crossroad crossroad = new GoldCrossroad(this, x, y);

        /* add event handlers */
        crossroad.addEventHandler(MouseEvent.MOUSE_PRESSED,
                mouseEvent -> transiate(UserEvent.PRESS_CROSSROAD, mouseEvent, crossroad));
        crossroad.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                mouseEvent -> transiate(UserEvent.MOVE_CROSSROADS, mouseEvent, crossroad));
        crossroad.addEventHandler(MouseEvent.MOUSE_RELEASED,
                mouseEvent -> transiate(UserEvent.RELEASE_CROSSROAD, mouseEvent, crossroad));

        /* bind for better look */
        DoubleProperty scaleX = new SimpleDoubleProperty();
        scaleX.addListener((observable, oldValue, newValue) -> {
            crossroad.setScaleX(1/newValue.doubleValue());
        });
        scaleX.bind(graph.scaleXProperty());
        DoubleProperty scaleY = new SimpleDoubleProperty();
        scaleY.addListener((observable, oldValue, newValue) -> {
            crossroad.setScaleY(1/newValue.doubleValue());
        });
        scaleY.bind(graph.scaleYProperty());

        /* add */
        crossroadGroup.getChildren().add(crossroad);
        model.getSelectables().add(crossroad);
        return crossroad;
    }

    private void deleteSelectedCrossroads() {
        Set<Crossroad> selectedItems = selection.getSelectedItems();
        for (Selectable selectable : selectedItems) {
            Crossroad selectedCrossroad = (Crossroad)selectable;
      /* remove from model */
            Set<Street> streetSet = selectedCrossroad.getStreets();
            streetSet.forEach(street -> {
                street.removeFromCrossroads();
                street.unbind();
            });
      /* remove from stage */
            streetGroup.getChildren().removeAll(streetSet);
            model.getStreets().removeAll(streetSet);
        }
        crossroadGroup.getChildren().removeAll(selectedItems);
        model.getSelectables().removeAll(selectedItems);
    }

    /* streets */
    private void createStreetsTo(Crossroad destination) {
        for (Selectable selectable : selection.getSelectedItems()) {
            Crossroad origin = (Crossroad)selectable;
            createStreet(origin, destination);
        }
    }

    private void createStreet(Crossroad origin, Crossroad destination) {
        Street street = new GoldStreet(origin, destination);
        boolean successOrigin = origin.add(street);
        boolean successDestination = destination.add(street);
        if (successOrigin && successDestination) {
            streetGroup.getChildren().add(street);
            model.getStreets().add(street);

            /* add event handler */
            street.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> transiate(UserEvent.CLICK_STREET, mouseEvent, street));

            /* bind for better look */
            DoubleProperty scaleX = new SimpleDoubleProperty();
            scaleX.addListener((observable, oldValue, newValue) -> {
                street.setScaleX(1/newValue.doubleValue());
            });
            scaleX.bind(graph.scaleXProperty());
            DoubleProperty scaleY = new SimpleDoubleProperty();
            scaleY.addListener((observable, oldValue, newValue) -> {
                street.setScaleY(1/newValue.doubleValue());
            });
            scaleY.bind(graph.scaleYProperty());
        } else {
            street.unbind();
        }
    }

    private void select(Street street) {
        selectedStreet = street;
        selectedStreet.setSelected(true);
    }

    private void unselectCurrentStreet() {
        selectedStreet.setSelected(false);
        selectedStreet = null;
    }

    private void deleteSelectedStreet() {
    /* remove from model */
        selectedStreet.removeFromCrossroads();
        selectedStreet.unbind();
    /* remove from stage */
        streetGroup.getChildren().remove(selectedStreet);
        model.getStreets().remove(selectedStreet);
    }
}
