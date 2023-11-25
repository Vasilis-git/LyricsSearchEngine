package gr.uop;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class SearchEngine extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        SearchEngineWindow mainWindow = new SearchEngineWindow();
        scene = new Scene(mainWindow, 640, 610);
        stage.setMinHeight(640);
        stage.setMinWidth(610);
        stage.setScene(scene);
        stage.setTitle("FALSE Search Engine");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}