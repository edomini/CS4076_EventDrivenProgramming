package org.example.timetable.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.example.timetable.network.TCP_Client;

public class DisplayScheduleController {

    @FXML
    private GridPane scheduleGrid;

    @FXML
    private Button backButton;


    private final String[] timeSlots = {
            "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00", "12:00 - 13:00",
            "13:00 - 14:00", "14:00 - 15:00", "15:00 - 16:00", "16:00 - 17:00", "17:00 - 18:00"
    };

    @FXML
    public void initialize() {
        // time column (first column)
        for (int i = 0; i < timeSlots.length; i++) {
            Label timeLabel = new Label(timeSlots[i]);
            timeLabel.setStyle("-fx-font-size: 14px; -fx-alignment: center;");
            scheduleGrid.add(timeLabel, 0, i + 1); // Column 0, Row i+1
        }

        populateEmptySchedule();

        fetchAndDisplaySchedule();
    }

    private void fetchAndDisplaySchedule() {

        new Thread(() -> {
            String response = TCP_Client.sendRequest("GET_SCHEDULE");

            System.out.println("Server Response: " + response);

            Platform.runLater(() -> {
                updateScheduleGrid(response);
            });
        }).start();
    }

    private void updateScheduleGrid(String response) {

        if (response == null || response.isEmpty()) {
            System.err.println("Received empty or null response from the server");
            return;
        }

        String[] scheduleData = response.split(";");

        for (String schedule : scheduleData) {
            String[] details = schedule.split(",");

            if (details.length == 3) {
                String day = details[0];
                String time = details[1];
                String module = details[2];


                int row = getRowForTimeSlot(time);
                int col = getColumnForDay(day);

                if (row != -1 && col != -1) {

                    Label moduleLabel = new Label(module);
                    scheduleGrid.add(moduleLabel, col, row);
                }
            }
        }
    }

    private int getRowForTimeSlot(String time) {
        for (int i = 0; i < timeSlots.length; i++) {
            if (timeSlots[i].equals(time)) {
                return i + 1; // Row starts from 1
            }
        }
        return -1;
    }

    private int getColumnForDay(String day) {
        switch (day) {
            case "Monday": return 1;
            case "Tuesday": return 2;
            case "Wednesday": return 3;
            case "Thursday": return 4;
            case "Friday": return 5;
            default: return -1;
        }
    }

    @FXML
    private void handleBackAction() {

        BaseController.switchScene((Stage) backButton.getScene().getWindow(), "front.fxml");//schedule ---> front
    }

    private void populateEmptySchedule() {
        for (int row = 1; row <= timeSlots.length; row++) {
            for (int col = 1; col <= 5; col++) {
                Label emptyCell = new Label("");
                emptyCell.setMinSize(150, 40); // Size logic for consistency
                emptyCell.setStyle("-fx-border-color: black; -fx-background-color: white;");
                scheduleGrid.add(emptyCell, col, row);
            }
        }
    }
}
