package org.example.timetable.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import java.io.IOException;

public class OtherController {

    @FXML
    private Button exportButton;

    @FXML
    private Button backButton;


    @FXML
    private void handleExportTimetable() {
        System.out.println("Export to PDF ");
        // have to figure out
    }

    @FXML
    private void handleBack() {

        goToFrontPage();
    }

    private void goToFrontPage() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();


            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

