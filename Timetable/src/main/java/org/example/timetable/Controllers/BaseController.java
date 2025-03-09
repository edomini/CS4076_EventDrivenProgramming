package org.example.timetable.Controllers;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class BaseController {


    public static void switchScene(Stage stage, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(BaseController.class.getResource("/fxml/" + fxmlFile));
            Pane root = loader.load();

            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.show();

            BackgroundMusic.getInstance().play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

