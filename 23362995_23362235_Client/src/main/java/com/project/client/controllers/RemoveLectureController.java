package com.project.client.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import com.project.client.Client;

import java.io.IOException;

public class RemoveLectureController {

    @FXML
    private ComboBox<String> dateComboBox;

    @FXML
    private ComboBox<String> timeComboBox;

    @FXML
    private TextField moduleCodeField;

    @FXML
    private TextField roomCodeField;

    @FXML
    private Button cancelButton;

    @FXML
    private Button submitButton;
    
    @FXML
    public void initialize() {
        //  09:00 - 17:00
        for (int hour = 9; hour <= 17; hour++) {
            this.timeComboBox.getItems().add(String.format("%02d:00", hour));
        }
    }

    @FXML
    private void handleCancel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front.fxml")); // Cancel button -> front
            Pane root = loader.load();

            Scene scene = new Scene(root);

            Stage stage = (Stage) this.cancelButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSubmit() {
        String output = "";
        String moduleCode = this.moduleCodeField.getText().trim();
        String selectedDay = this.dateComboBox.getValue();
        String selectedTime = this.timeComboBox.getValue();
        String roomCode = this.roomCodeField.getText().trim();

        if (selectedDay == null || selectedTime == null || moduleCode.isEmpty() || roomCode.isEmpty()) {
            Client.showAlert("Invalid Input", "Please enter valid module code, day, time, and room number.");
            return;
        }

        // message should be of the form: "ACTION (e.g. ADD), module code, day (e.g. Monday), time (e.g. 14:00), lecture room"
        output = String.format("REMOVE,%s,%s,%s,%s", moduleCode, selectedDay, selectedTime, roomCode);
        System.out.println("\nClient: " + output);

        // create a background task
        String finalOutput = output;
        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return Client.sendRequest(finalOutput); // run in background thread
            }
        };

        // when task is completed, process server response
        task.setOnSucceeded(event -> {
            String[] response = task.getValue().split(":", 2);
            System.out.println("Server: " + response[1].trim());

            // display response
            Platform.runLater(() -> {
                Client.showAlert(response[0], response[1].trim());
                
                //if lecture is removed successfully, switch to schedule display
                if (response[0].equals("Success")) {
                    try {
                        BaseController.switchScene((Stage) submitButton.getScene().getWindow(), "display_schedule.fxml");
                    } catch (Exception e) {
                        System.out.println("Scene Switch Error: " + e.getMessage());
                        Client.showAlert("Scene Switch Error", e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        });

        // if task fails (usually connection error), display error alert
        task.setOnFailed(event -> {
            Platform.runLater(() -> Client.showAlert("Error", "Failed to connect to server."));
        });

        // start the background thread
        new Thread(task).start();
    }
}
