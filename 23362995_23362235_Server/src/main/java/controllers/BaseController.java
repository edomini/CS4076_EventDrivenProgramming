package controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;

import com.project.server.Schedule;

import java.io.IOException;

public class BaseController {
    public static void switchScene(Stage stage, String fxmlFile, Schedule schedule) {
        try {
            FXMLLoader loader = new FXMLLoader(BaseController.class.getResource("/fxml/" + fxmlFile));
            BorderPane root = loader.load();

            if (fxmlFile.equals("server_display_schedule.fxml")) {
                ServerDisplayScheduleController serverDisplayScheduleController = loader.getController();
                // set schedule before initialisation
                serverDisplayScheduleController.setSchedule(schedule);
            }

            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.show();
                        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}