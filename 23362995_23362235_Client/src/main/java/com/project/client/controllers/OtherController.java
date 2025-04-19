package com.project.client.controllers;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import com.project.client.Client;

public class OtherController {
    private Client client;

    @FXML
    private Button backButton;

    @FXML
    private Button stopButton;

    @FXML
    private Button otherButton;

    @FXML
    private Button earlyLecturesButton;

    public void setClient(Client client) {
        // Set the client instance if needed
        this.client = client;
    }

    @FXML
    private void handleStop() {
        String message = "STOP";
        client.readResponse(message, () -> {
            // stop the music
            BackgroundMusicController.getInstance().stop();
            // then close the program successfully
            System.exit(0);
        });
    }

    @FXML
    private void handleOther(){
        String message = "OTHER";
        client.readResponse(message, null);
    }

    @FXML
    private void handleBack() {
        BaseController.switchScene((Stage) backButton.getScene().getWindow(), "front.fxml", client); //other ---> front
    }

    @FXML
    private void handleEarlyLectures() {
        String message = "EARLY";
        client.readResponse(message, () -> {
            try {
                // switch to schedule display if not already on it
                BaseController.switchScene((Stage) earlyLecturesButton.getScene().getWindow(), "display_schedule.fxml", client);
            } catch (Exception e) {
                System.out.println("Scene Switch Error: " + e.getMessage());
                Client.showAlert("Scene Switch Error", e.getMessage());
                e.printStackTrace();
            }});
    }
}
