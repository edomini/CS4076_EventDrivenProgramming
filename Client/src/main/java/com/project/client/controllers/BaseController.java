package com.project.client.controllers;


import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class BaseController {
    public static void switchScene(Stage stage, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(BaseController.class.getResource("/fxml/" + fxmlFile));
            Pane root = loader.load();
            Scene scene = new Scene(root);

            boolean wasFullScreen = stage.isFullScreen();
            stage.setScene(scene);
            stage.setFullScreen(wasFullScreen);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
/*
Full Screen only works for loginpage for some reason ----> figure that out
 */