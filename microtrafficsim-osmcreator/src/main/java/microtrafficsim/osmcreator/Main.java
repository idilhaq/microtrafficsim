package microtrafficsim.osmcreator;

import javafx.application.Application;
import javafx.scene.*;
import javafx.stage.Stage;
import microtrafficsim.osmcreator.geometry.GraphPane;
import microtrafficsim.osmcreator.user.UIController;

/**
 * @author Dominic Parga Cacheiro
 */
public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Group group = new Group();

        /* prepare zoomable/draggable root pane */
        GraphPane graph = new GraphPane();
        graph.setStyle(Constants.GRAPH_BACKGROUND_STYLE);
//    DoubleProperty testResize = new SimpleDoubleProperty();
//    testResize.addListener((observable, oldValue, newValue) -> pane.setPrefWidth(newValue.doubleValue() - 100));
//    testResize.bind(primaryStage.widthProperty());

        /* finish scene graph */
        group.getChildren().add(graph);

        /* create scene */
        Scene scene = new Scene(group, Constants.INITIALIZE_SCREEN_WIDTH, Constants.INITIALIZE_SCREEN_HEIGHT);

        /* create gestures */
        UIController uiController = new UIController();
        uiController.addAllEventHandlers(scene, graph);

        /* finish */
        graph.center();
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
