package org.example.timetable.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML
    public Button loginButton;
    @FXML
    public void onLoginButtonClick() throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front.fxml"));//login ---> front
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}

