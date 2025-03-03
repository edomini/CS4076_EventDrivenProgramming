package org.example.timetable.Controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import org.example.timetable.network.TCP_Client;

import java.io.IOException;

public class RemoveLectureController {

    @FXML
    private ComboBox<String> dateComboBox;

    @FXML
    private ComboBox<String> timeComboBox;

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

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front.fxml")); // Cancel button -> front
            Pane root = loader.load();

            Scene scene = new Scene(root);

            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSubmit() throws IOException {

        String selectedDay = dateComboBox.getValue();
        String selectedTime = timeComboBox.getValue();
        String moduleCode = moduleCodeField.getText().trim();

        if (selectedDay == null || selectedTime == null || moduleCode.isEmpty()) {
            showAlert("Invalid Input", "Please enter valid day, time, and module code.");
            return;
        }

        String request = String.format("REMOVE %s %s %s", selectedDay, selectedTime, moduleCode);
        System.out.println("Request to remove lecture: " + request);

        String response = TCP_Client.sendRequest(request);
        System.out.println("Response from server: " + response);

        if (response.equals("Success")) {
            showAlert("Success", "Lecture successfully removed.");

            System.out.println("Switching to display schedule...");

            try {
                BaseController.switchScene((Stage) submitButton.getScene().getWindow(), "display_schedule.fxml");
            } catch (Exception e) {
                System.out.println("Error switching scenes: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Failure", "Failed to remove the lecture. Please try again.");
        }

        System.out.println("Lecture Removal Request Sent: " + "Day: " + selectedDay + ", Time: " + selectedTime + ", Module Code: " + moduleCode);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
