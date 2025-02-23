package org.example.timetable.Controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

public class RemoveLectureController {

    @FXML
    private ComboBox<String> dateComboBox; // Weekday ComboBox

    @FXML
    private ComboBox<String> timeComboBox; // Time ComboBox

    @FXML
    private TextField moduleCodeField;

    @FXML
    private Button cancelButton;

    @FXML
    private Button submitButton;

    @FXML
    public void initialize() {
        //  09:00 - 17:00
        for (int hour = 9; hour <= 17; hour++) {
            timeComboBox.getItems().add(String.format("%02d:00", hour));
        }
    }

    @FXML
    private void handleCancel() {

        try {
            // Load the front page FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml//front.fxml"));// Cancel button -----> front
            Pane root = loader.load();

            //  new scene with the front layout
            Scene scene = new Scene(root);

            // current ---> set scene to front
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSubmit() throws IOException {//Not done do later

        String selectedDay = dateComboBox.getValue();
        String selectedTime = timeComboBox.getValue();
        String moduleCode = moduleCodeField.getText().trim();


        if (selectedDay == null || selectedTime == null || moduleCode.isEmpty()) {
            showAlert("Invalid Input", "Please enter valid day, time, and module code.");
            return;
        }


        System.out.println("Lecture Removed: " + "day:" + selectedDay + " " + "time:" + selectedTime + " " + "module code: "+ moduleCode);


    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

