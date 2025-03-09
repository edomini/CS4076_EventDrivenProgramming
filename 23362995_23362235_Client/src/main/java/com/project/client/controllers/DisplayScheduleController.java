package com.project.client.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.application.Platform;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.project.client.TCP_Client;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DisplayScheduleController {
    public static final List<String> days = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
    public static final List<String> times = List.of("09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00");

    @FXML
    private GridPane scheduleGrid;

    @FXML
    private Button importButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button exportPDFButton;

    @FXML
    private Button exportCSVButton;

    @FXML
    private Button backButton;

    private final String[] timeSlots = {
            "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00", "12:00 - 13:00",
            "13:00 - 14:00", "14:00 - 15:00", "15:00 - 16:00", "16:00 - 17:00", "17:00 - 18:00"
    };

    @FXML
    public void initialize() {
        // 'Time' title label (0th col, 0th row)
        Label timeTitle = new Label("Time");
        timeTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        scheduleGrid.add(timeTitle, 0, 0);
        GridPane.setHalignment(timeTitle, HPos.CENTER); //center it over the time labels
        
        // time column (first column)
        for (int i = 0; i < timeSlots.length; i++) {
            Label timeLabel = new Label(timeSlots[i]);
            timeLabel.setStyle("-fx-font-size: 14px; -fx-alignment: center;");
            scheduleGrid.add(timeLabel, 0, i + 1); // column 0, row i+1
        }
        
        // days row (first row)
        for (int j = 1; j <= days.size(); j++){
            Label dayLabel = new Label(days.get(j - 1));
            dayLabel.setStyle("-fx-font-size: 16px;");
            scheduleGrid.add(dayLabel, j, 0); // column j, row 0
            GridPane.setHalignment(dayLabel, HPos.CENTER); // center it over the schedule cells
        }
        
        //add empty cells to each cell of grid pane
        populateEmptySchedule();

        // load the schedule
        fetchAndDisplaySchedule();
    }

    private void fetchAndDisplaySchedule() {
        new Thread(() -> {
            //send request to server
            System.out.println("\nmessage sent: DISPLAY");
            String response = TCP_Client.sendRequest("DISPLAY");

            //print server response
            if (response == null || response.isEmpty() || response.equals("Schedule empty.")){
                System.out.println("message received: Schedule empty.");
            } else {
                System.out.println("message received: " + response);
            }

            //update the schedule
            Platform.runLater(() -> {
                updateScheduleGrid(response);
            });
        }).start(); //start the thread
    }

    private void updateScheduleGrid(String response) {
        if (response == null || response.isEmpty()) {
            populateEmptySchedule();
            return;
        }

        //split string of schedule array from server
        String[] scheduleData = response.split(";");

        //split each lecture into its components
        for (String schedule : scheduleData) {
            String[] details = schedule.split(",");

            if (details.length == 4) {
                String module = details[0];
                String day = details[1];
                String time = details[2];
                String room = details[3];

                int row = times.indexOf(time) + 1;
                int col = days.indexOf(day) + 1;

                //add lecture to correct place in grid pane
                if (row != 0 && col != 0) {
                    Label moduleLabel = new Label(module + "\n" + room);
                    scheduleGrid.add(moduleLabel, col, row);
                    GridPane.setHalignment(moduleLabel, HPos.CENTER);
                } else {
                    System.out.println("Positioning error for schedule: position [" + row + "," + col + "]");
                    TCP_Client.showAlert("Error", "Positioning error for schedule: position [" + row + "," + col + "]");
                }
            }
        }
    }

    private void populateEmptySchedule() {
        // remove all the lecture slot labels, but keep the day names and times
        scheduleGrid.getChildren().removeIf(node -> {
            Integer colIndex = GridPane.getColumnIndex(node);
            Integer rowIndex = GridPane.getRowIndex(node);

            // Remove only the cells that are not part of the header (days/times)
            return colIndex != null && rowIndex != null && colIndex > 0 && rowIndex > 0;
        });
        for (int row = 1; row <= timeSlots.length; row++) {
            for (int col = 1; col <= 5; col++) {
                Label emptyCell = new Label("");
                emptyCell.setMinSize(150, 40); // Size logic for consistency
                emptyCell.setStyle("-fx-border-color: black; -fx-background-color: white;");
                scheduleGrid.add(emptyCell, col, row);
            }
        }
    }

    @FXML
    private void handleBackAction() {
        BaseController.switchScene((Stage) backButton.getScene().getWindow(), "front.fxml");//schedule ---> front
    }

    @FXML
    private void handleClear(){
        String message = "CLEAR";
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
                // make sure grid is cleared
                populateEmptySchedule();
                // update schedule with cleared cells
                fetchAndDisplaySchedule();
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> TCP_Client.showAlert("Error", "Failed to connect to server."));
        });

        // start the background thread
        new Thread(task).start();
    }

    @FXML
    private void handleImport(){
        // create file chooser to select csv file to import from
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        // show file chooser dialog and get the file
        File selectedFile = fileChooser.showOpenDialog(scheduleGrid.getScene().getWindow());

        if (selectedFile != null) {
            //read from and process the file
            readCSVFile(selectedFile);
        }
    }

    private void readCSVFile(File file){
        //create list to store read data
        List<String[]> timetableData = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            //read in values from csv
            while ((line = reader.readLine()) != null) {
                // split csv line by comma
                String[] data = line.split(",");

                // check if the line has correct number of columns (4)
                if (data.length != 4) {
                    TCP_Client.showAlert("Invalid CSV format", "Each row should have 4 columns: Module, Day, Time, Room.");
                    return;
                }

                // check if the day and time are valid
                String day = data[1].trim();
                String time = data[2].trim();

                if (!days.contains(day)) {
                    TCP_Client.showAlert("Invalid Day", "The day '" + day + "' is not a valid day.");
                    return;
                }

                if (!times.contains(time)) {
                    TCP_Client.showAlert("Invalid Time", "The time '" + time + "' is not a valid lecture time.");
                    return;
                }

                //if line is ok, add it to the timetableData list
                timetableData.add(data);
            }

            // if full csv is valid, add the data to the schedule
            //put in string format
            String formattedData = formatScheduleData(timetableData);

            //send info to server
            String message = "IMPORT," + formattedData;
            System.out.println("\nmessage sent: \n" + message);

            Task<String> task = new Task<>() {
                @Override
                protected String call() {
                    return TCP_Client.sendRequest(message); // run in background thread
                }
            };

            // when task is completed, process server response
            task.setOnSucceeded(event -> {
                String[] response = task.getValue().split(":", 2);
                System.out.println("message received: " + response[1].trim());

                // display response
                Platform.runLater(() -> {
                    TCP_Client.showAlert(response[0], response[1].trim());
                    //update schedule with imported data
                    fetchAndDisplaySchedule();
                });
            });

            task.setOnFailed(event -> {
                Platform.runLater(() -> TCP_Client.showAlert("Error", "Failed to connect to server."));
            });

            // start the background thread
            new Thread(task).start();

        } catch (IOException e) {
            TCP_Client.showAlert("File Read Error", "Error reading the CSV file.");
        }
    }

    private String formatScheduleData(List<String[]> timetableData) {
        StringBuilder formattedData = new StringBuilder();

        for (String[] entry : timetableData) {
            // ensure entry has  correct number of columns (4): module, day, time, room
            if (entry.length == 4) {
                String module = entry[0];
                String day = entry[1];
                String time = entry[2];
                String room = entry[3];

                // build the formatted string for this entry, format matches "add lecture" in server
                formattedData.append(module).append(",")
                        .append(day).append(",")
                        .append(time).append(",")
                        .append(room).append(";");

            } else {
                System.out.println("Invalid entry: " + Arrays.toString(entry));
            }
        }

        // remove semicolon at very end
        if (formattedData.length() > 0) {
            formattedData.deleteCharAt(formattedData.length() - 1);
        }

        //return full formatted schedule data
        return formattedData.toString();
    }

    @FXML
    private void handleExportCSV(){
        System.out.println("\nmessage sent: Export as CSV");

        // create a FileChooser to choose file name and save location
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        // show save dialog
        File file = fileChooser.showSaveDialog(new Stage());
        //File file = fileChooser.showSaveDialog(scheduleGrid.getScene().getWindow());

        //if valid file is chosen:
        if (file != null) {
            Task<String> task = new Task<>() {
                @Override
                protected String call() {
                    //display the schedule
                    return TCP_Client.sendRequest("DISPLAY"); // run in background thread
                }
            };

            // when task is completed, process server response
            task.setOnSucceeded(event -> {
                String response = task.getValue();

                if (response == null || response.isEmpty()) {
                    Platform.runLater(() -> TCP_Client.showAlert("Error", "No data received from the server."));
                    return;
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    // format data from server and write to csv
                    String formattedResponse = response.replace(";", "\n"); // convert to csv format
                    writer.write(formattedResponse);

                    // return success message
                    System.out.println("message received: Schedule exported successfully.");
                    Platform.runLater(() -> TCP_Client.showAlert("Success", "Schedule exported successfully."));
                } catch (IOException e) {
                    // return failure message
                    System.out.println("message received: Failed to export schedule.");
                    Platform.runLater(() -> TCP_Client.showAlert("Error", "Failed to export schedule"));
                    e.printStackTrace();
                }
            });

            task.setOnFailed(event -> Platform.runLater(() -> TCP_Client.showAlert("Error", "Failed to connect to server.")));

            // start background thread
            new Thread(task).start();

        }
    }

    @FXML
    private void handleExportPDF(){
        System.out.println("\nmessage sent: Export as PDF");

        // create printerJob object
        PrinterJob job = PrinterJob.createPrinterJob();

        if (job == null) {
            TCP_Client.showAlert("Error", "No printer job available");
            return;
        }

        // set page to landscape, small margins
        job.getJobSettings().setPageLayout(
                job.getPrinter().createPageLayout(
                        Paper.A4, PageOrientation.LANDSCAPE, Printer.MarginType.HARDWARE_MINIMUM
                )
        );

        // show printer dialog
        boolean proceed = job.showPrintDialog(scheduleGrid.getScene().getWindow());

        if (proceed) {
            //capture the grid pane
            boolean success = printNodeToPDF(scheduleGrid, job);

            if (success) {
                job.endJob();
                System.out.println("message received: Schedule exported successfully.");
                TCP_Client.showAlert("Success", "Schedule exported successfully");
            } else {
                System.out.println("message received: Failed to export schedule.");
                TCP_Client.showAlert("Error", "Failed to export schedule");
            }
        }
    }

    private boolean printNodeToPDF(Node node, PrinterJob job) {
        PageLayout pageLayout = job.getJobSettings().getPageLayout();

        // make sure schedule fits on page
        double padding = 40;
        double scaleX = pageLayout.getPrintableWidth() / (node.getBoundsInParent().getWidth() + padding);
        double scaleY = pageLayout.getPrintableHeight() / (node.getBoundsInParent().getHeight() + padding);
        double scale = Math.min(scaleX, scaleY); // scale down if needed

        //rescale schedule temporarily
        Scale scaleTransform = new Scale(scale, scale);
        node.getTransforms().add(scaleTransform);

        //save the schedule to pdf
        boolean success = job.printPage(node);

        //reset the schedule scale
        node.getTransforms().remove(scaleTransform);

        return success;
    }
}