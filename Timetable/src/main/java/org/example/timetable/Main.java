package org.example.timetable;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static javafx.application.Application.launch;

public class Main extends Application {


    @Override
    public void start(Stage stage) throws Exception {
        //FXML file
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));


        Scene scene = new Scene(root, 550, 650);


        //stage.setTitle("Login Page"); Instead of the name just use an image of miffy at the top of the page .
        stage.setScene(scene);
        stage.setMaximized(true);
        //stage.setFullScreen(true);
        //stage.setFullScreenExitHint("");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}