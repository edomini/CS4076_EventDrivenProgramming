package org.example.timetable.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class FrontController {
    @FXML
    private Button addLectureButton, removeLectureButton, displayScheduleButton, otherButton;

    @FXML
    private void handleAddLecture() {
        BaseController.switchScene((Stage) addLectureButton.getScene().getWindow(), "add_lecture.fxml");
    }

    @FXML
    private void handleRemoveLecture() {
        BaseController.switchScene((Stage) removeLectureButton.getScene().getWindow(), "remove_lecture.fxml");
    }

    @FXML
    private void handleDisplaySchedule() {
        BaseController.switchScene((Stage) displayScheduleButton.getScene().getWindow(), "display_schedule.fxml");
    }

    @FXML
    private void handleOther() {
        BaseController.switchScene((Stage) otherButton.getScene().getWindow(), "other.fxml");
    }
}