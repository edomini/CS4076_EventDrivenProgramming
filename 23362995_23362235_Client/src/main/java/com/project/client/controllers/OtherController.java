package com.project.client.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import com.project.client.TCP_Client;

import java.io.IOException;

public class OtherController {

    @FXML
    private Button backButton;

    @FXML
    private Button stopButton;

    @FXML
    private Button otherButton;

    @FXML
    private void handleStop() {
        String message = "STOP";
        System.out.println("\nmessage sent: " + message);

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return TCP_Client.sendRequest(message); // run in background thread
            }
        };

        // when task is completed, process server response
        task.setOnSucceeded(event -> {
            String[] response = task.getValue().split(":");
            System.out.println("message received: " + response[1].trim());

            // display response
            Platform.runLater(() -> {
                // show the alert and wait until the user dismisses it
                TCP_Client.showAlert(response[0], response[1].trim());
                //stop the music
                BackgroundMusicController.getInstance().stop();
                // then close the program successfully
                System.exit(0);
            });
        });

        task.setOnFailed(event -> Platform.runLater(() -> TCP_Client.showAlert("Error", "Failed to connect to server.")));

        // start the background thread
        new Thread(task).start();
    }

    @FXML
    private void handleOther(){
        String message = "OTHER";
        System.out.println("\nmessage sent: " + message);

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return TCP_Client.sendRequest(message); // run in background thread
            }
        };

        // when task is completed, process server response
        task.setOnSucceeded(event -> {
            String[] response = task.getValue().split(":");
            System.out.println("message received: " + response[1].trim());

            // display response
            Platform.runLater(() -> TCP_Client.showAlert(response[0], response[1].trim()));
        });

        task.setOnFailed(event -> Platform.runLater(() -> TCP_Client.showAlert("Error", "Failed to connect to server.")));

        // start the background thread
        new Thread(task).start();
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
