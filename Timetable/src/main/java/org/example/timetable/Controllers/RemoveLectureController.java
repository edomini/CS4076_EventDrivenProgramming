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
    private TextField removeRoomField;

    @FXML
    private TextField removeModuleCodeField;

    @FXML
    private Button cancelButton;

    @FXML
    private Button submitButton;

    @FXML
    public void initialize() {
        // Populating timeComboBox with available time slots (09:00 - 17:00)
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
        // Get values from the form
        String selectedDay = dateComboBox.getValue();
        String selectedTime = timeComboBox.getValue();
        String moduleCode = removeModuleCodeField.getText().trim();
        String room = removeRoomField.getText().trim();  // Get room number from the input field

        // Validation: Ensure all fields are filled out, including room number
        if (selectedDay == null || selectedTime == null || moduleCode.isEmpty() || room.isEmpty()) {
            if (room.isEmpty()) {
                showAlert("Room Number Missing", "Please enter the room number.");
            } else {
                showAlert("Invalid Input", "Please enter valid day, time, module code, and room.");
            }
            return;
        }

        // Prepare the request string with the room number included
        String request = String.format("REMOVE %s %s %s %s", selectedDay, selectedTime, moduleCode, room);
        System.out.println("Request to remove lecture: " + request);

        // Send the request to the server
        String response = TCP_Client.sendRequest(request);
        System.out.println("Response from server: " + response);

        if (response.equals("Success")) {
            // Show success alert and switch to the display schedule screen
            showAlert("Success", "Lecture successfully removed.");
            BaseController.switchScene((Stage) submitButton.getScene().getWindow(), "display_schedule.fxml");//submit---> schedule
        } else {
            // Show failure alert if removal fails
            showAlert("Failure", "Failed to remove the lecture. Please try again.");
        }

        System.out.println("Lecture Removal Request Sent: " + "Day: " + selectedDay + ", Time: " + selectedTime + ", Module Code: " + moduleCode + ", Room: " + room);
    }

    // Helper method to show alert messages
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}













