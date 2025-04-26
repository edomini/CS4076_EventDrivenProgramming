package controllers;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.project.server.Schedule;
import com.project.server.Lecture;
import com.project.server.Server;
import com.project.server.ServerMonitor;
import com.project.server.ClientHandler;

import java.util.concurrent.ConcurrentHashMap;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ServerDisplayScheduleController {
    private static ServerDisplayScheduleController instance;
    public static final List<String> days = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
    public static final List<String> times = List.of("09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00");
    private Schedule schedule;
    private Lecture[][] scheduleArray;
    private String courseCode;

    @FXML
    private GridPane scheduleGrid;

    @FXML
    private Label timetableLabel;

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

    // Setter for schedule
    public void setSchedule(Schedule schedule) {
        //MAKE SURE TO SEND SCHEDULE TO CONTROLLER
        instance = this;
        this.schedule = schedule;
        this.scheduleArray = schedule.getSchedule();

        courseCode = "Error";
        for(ConcurrentHashMap.Entry<String, Schedule> entry : Server.database.entrySet()) {
            if (schedule.equals(entry.getValue())) {
                courseCode = entry.getKey();
                break;
            }
        }

        timetableLabel.setText(courseCode + " Timetable");
        fetchAndDisplaySchedule();
    }

    public static ServerDisplayScheduleController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        Label timeTitle = new Label("Time");
        timeTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        scheduleGrid.add(timeTitle, 0, 0);
        GridPane.setHalignment(timeTitle, HPos.CENTER); // Center it over the time labels

        // time column (first column)
        for (int i = 0; i < timeSlots.length; i++) {
            Label timeLabel = new Label(timeSlots[i]);
            timeLabel.setStyle("-fx-font-size: 14px;");
            scheduleGrid.add(timeLabel, 0, i + 1);
            GridPane.setHalignment(timeLabel, HPos.CENTER);
        }

        // days row (first row)
        for (int j = 1; j <= days.size(); j++) {
            Label dayLabel = new Label(days.get(j - 1));
            dayLabel.setStyle("-fx-font-size: 16px;");
            scheduleGrid.add(dayLabel, j, 0);
            GridPane.setHalignment(dayLabel, HPos.CENTER);
        }

        fetchAndDisplaySchedule();
    }

    public synchronized void fetchAndDisplaySchedule() {
        new Thread(() -> updateScheduleGrid(scheduleArray)).start();
    }

    private synchronized void updateScheduleGrid(Lecture[][] schedule) {
        Platform.runLater(() -> populateEmptySchedule());

        for (Lecture[] lecList : schedule) {
            for (Lecture lec : lecList) {
                if (lec != null) {
                    String module = lec.getModuleCode();
                    String day = lec.getDay();
                    String time = lec.getLecTime();
                    String room = lec.getRoomNum();

                    int row = times.indexOf(time) + 1;
                    int col = days.indexOf(day) + 1;

                    Platform.runLater(() -> {
                        // Add lecture to correct place in grid pane
                        if (row != 0 && col != 0) {
                            Label moduleLabel = new Label(module + "\n" + room);
                            scheduleGrid.add(moduleLabel, col, row);
                            GridPane.setHalignment(moduleLabel, HPos.CENTER);
                        } else {
                            System.out.println("Positioning error for schedule: position [" + row + "," + col + "]");
                        }
                    });
                }
            }
        }
    }

    private synchronized void populateEmptySchedule() {
        // Remove all the lecture slot labels, but keep the day names and times
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

    // Handle the BACK action (goes back to monitor)
    @FXML
    private void handleBackAction() {
        BaseController.switchScene((Stage) backButton.getScene().getWindow(), "server_monitor.fxml", null);
    }

    // Handle the clear action (clear the schedule)
    @FXML
    private void handleClear() {
        schedule.clearSchedule();
        Platform.runLater(() -> fetchAndDisplaySchedule());  // refresh the schedule

        System.out.println("Server: Schedule cleared successfully.");
        Platform.runLater(() -> ClientHandler.showAlert("Success", "Schedule cleared successfully."));
        ServerMonitor.log(String.format("Server cleared the %s schedule.", courseCode));
    }

    // Handle importing schedule from CSV
    @FXML
    private void handleImport() {
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

    private void readCSVFile(File file) {
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
                    ClientHandler.showAlert("Invalid CSV format", "Each row should have 4 columns: Module, Day, Time, Room.");
                    return;
                }

                // check if the day and time are valid
                String day = data[1].trim();
                String time = data[2].trim();

                if (!days.contains(day)) {
                    ClientHandler.showAlert("Invalid Day", "The day '" + day + "' is not a valid day.");
                    return;
                }

                if (!times.contains(time)) {
                    ClientHandler.showAlert("Invalid Time", "The time '" + time + "' is not a valid lecture time.");
                    return;
                }

                //if line is ok, add it to the timetableData list
                timetableData.add(data);
            }

            // if full csv is valid, add the data to the schedule
            //put in string format
            String formattedData = formatScheduleData(timetableData);

            //send info to serverGUI
            System.out.println("Server: " + schedule.importSchedule(formattedData));

            Platform.runLater(() -> {
                ClientHandler.showAlert("Success", "Schedule imported successfully.");
                ServerMonitor.log(String.format("Server imported a schedule for %s.", courseCode));
                fetchAndDisplaySchedule();
            });
            

        } catch (IOException e) {
            ClientHandler.showAlert("File Read Error", "Error reading the CSV file.");
        }
    }

    private String formatScheduleData(List<String[]> timetableData) {
        StringBuilder formattedData = new StringBuilder();
        for (String[] entry : timetableData) {
            if (entry.length == 4) {
                formattedData.append(String.join(",", entry)).append(";");
            } else {
                System.out.println("Invalid entry: " + Arrays.toString(entry));
            }
        }

        // remove semicolon at very end
        if (formattedData.length() > 0) {
            formattedData.deleteCharAt(formattedData.length() - 1);
        }

        return formattedData.toString();
    }

    // Handle exporting schedule to CSV
    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            Task<String> task = new Task<>() {
                @Override
                protected String call() {
                    return schedule.displaySchedule();
                }
            };

            task.setOnSucceeded(event -> {
                String response = task.getValue();

                if (response == null || response.isEmpty()) {
                    Platform.runLater(() -> ClientHandler.showAlert("Export Error", "No schedule data to export."));
                    return;
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    String formattedResponse = response.replace(";", "\n");
                    writer.write(formattedResponse);

                    System.out.println("Server: Schedule exported to CSV successfully.");
                    ServerMonitor.log(String.format("Server exported the %s schedule to CSV.", courseCode));
                    Platform.runLater(() -> ClientHandler.showAlert("Success", "Schedule exported to CSV successfully."));
                } catch (IOException e) {
                    System.out.println("Server: Failed to export schedule to CSV.");
                    ServerMonitor.log(String.format("Server failed to export the %s schedule to CSV.", courseCode));
                    Platform.runLater(() -> ClientHandler.showAlert("Error", "Failed to export schedule to CSV."));
                    e.printStackTrace();
                }
            });

            new Thread(task).start();
        }
    }

    // Handle exporting schedule to PDF
    @FXML
    private void handleExportPDF() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            System.out.println("No printer job available");
            return;
        }

        job.getJobSettings().setPageLayout(job.getPrinter().createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, Printer.MarginType.HARDWARE_MINIMUM));
        boolean proceed = job.showPrintDialog(scheduleGrid.getScene().getWindow());

        if (proceed) {
            boolean success = printNodeToPDF(scheduleGrid, job);
            if (success) {
                job.endJob();
                System.out.println("Server: Schedule exported to PDF successfully.");
                ServerMonitor.log(String.format("Server exported the %s schedule to PDF.", courseCode));
                ClientHandler.showAlert("Success", "Schedule exported to PDF successfully.");
            } else {
                System.out.println("Server: Failed to export schedule to PDF.");
                ServerMonitor.log(String.format("Server failed to export the  %s schedule to PDF.", courseCode));
                ClientHandler.showAlert("Error", "Failed to export schedule to PDF.");
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


