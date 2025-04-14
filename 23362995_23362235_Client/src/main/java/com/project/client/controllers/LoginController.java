package com.project.client.controllers;

import com.project.client.Client;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    private Client client;

    @FXML
    public TextField courseCodeField;

    @FXML
    public Button loginButton;

    @FXML
    public void onLoginButtonClick() throws IOException {
        //get course code from text field
        String courseCode = courseCodeField.getText().trim();

        //check if course code is empty
        if(courseCode.isEmpty()){
            Client.showAlert("Error", "Please enter a course code.");
            return;
        }

        //establish connection with server
        client = new Client();
        boolean isConnected = client.connect(courseCode);

        if(!isConnected){
            Client.showAlert("Error", "Failed to connect to server.");
            System.exit(1);
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front.fxml"));//login ---> front
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}

