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

public class AddLectureController {
    private Client client;

    @FXML
    private ComboBox<String> dateComboBox; // Date

    @FXML
    private ComboBox<String> timeComboBox; // Time

    @FXML
    private TextField moduleCodeField; // Module

    @FXML
    private TextField roomCodeField; // Room

    @FXML
    private Button cancelButton; // Cancel

    @FXML
    private Button submitButton; // Submit

    @FXML
    public void initialize() {
        //09:00 - 17:00
        for (int hour = 9; hour <= 17; hour++) {
            this.timeComboBox.getItems().add(String.format("%02d:00", hour));
        }
    }

    public void setClient(Client client) {
        // Set the client instance if needed
        this.client = client;
    }

    @FXML
    private void handleCancel() {
        try {
            // load the front page FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front.fxml"));//cancel ---> front
            Pane root = loader.load();

            // new scene with the front layout
            Scene scene = new Scene(root);

            // current ---> set scene to front
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
        output = String.format("ADD,%s,%s,%s,%s", moduleCode, selectedDay, selectedTime, roomCode);
        System.out.println("\nClient: " + output);


        // create a background task
        String finalOutput = output;
        client.readResponse(finalOutput, () -> {
            try {
                // switch to schedule display if not already on it
                BaseController.switchScene((Stage) submitButton.getScene().getWindow(), "display_schedule.fxml", client);
            } catch (Exception e) {
                System.out.println("Scene Switch Error: " + e.getMessage());
                Client.showAlert("Scene Switch Error", e.getMessage());
                e.printStackTrace();
            }
        });

        /*
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
                
                //if lecture is added successfully, switch to schedule display
                if (response[0].equals("Success")) {
                    try {
                        // switch to schedule display if not already on it
                        BaseController.switchScene((Stage) submitButton.getScene().getWindow(), "display_schedule.fxml", client);
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
        */
    }
}
