package org.example.timetable.Controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

public class AddLectureController {

    @FXML
    private ComboBox<String> dateComboBox;

    @FXML
    private ComboBox<String> timeComboBox;

    @FXML
    private TextField addModuleCodeField;

    @FXML
    private TextField addRoomField;

    @FXML
    private Button cancelButton;

    @FXML
    private Button submitButton;// Submit ---->not done yet

    @FXML
    public void initialize() {
        //09:00 - 17:00
        for (int hour = 9; hour <= 17; hour++) {
            timeComboBox.getItems().add(String.format("%02d:00", hour));
        }
    }

    @FXML
    private void handleCancel() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/front.fxml"));//cancel ---> front
            Pane root = loader.load();


            Scene scene = new Scene(root);

            // current ---> set scene to front
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSubmit() throws IOException {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/display_schedule.fxml"));//submit ---> schedule
            Pane root = loader.load();


            Scene scene = new Scene(root);

            // current ---> set scene to front
            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String selectedDay = dateComboBox.getValue();
        String selectedTime = timeComboBox.getValue();
        String moduleCode = addModuleCodeField.getText().trim();
        String roomFieldText = addRoomField.getText().trim();


        if (selectedDay == null || selectedTime == null || moduleCode.isEmpty() || roomFieldText.isEmpty()) {
            showAlert("Invalid Input", "Please enter valid day, time, module code and room number .");
            return;
        }


        System.out.println("Lecture Added: " + "day:" + selectedDay + " " + "time:" + selectedTime + " " + "module code: "+ moduleCode + " " +"room number: "+ roomFieldText);


    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
