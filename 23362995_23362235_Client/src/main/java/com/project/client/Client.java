package com.project.client;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String courseCode;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 1234;
    private boolean ignoreNextUpdate = false;

    public boolean connect(String code) {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            //send course code to server
            this.courseCode = code;
            out.println("courseCode:" + courseCode); //send course code to server

            //listenForUpdates();

            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void disconnect(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized String sendRequest(String request) {
        try {
            //send request to server
            out.println(request);

            // set flag to ignore next update
            if(request.contains("ADD") || request.contains("REMOVE") || request.contains("CLEAR")){
                ignoreNextUpdate = true;
            }

            //read response from server
            String response = in.readLine();

            //return response as a string
            return response.trim();

        } catch (IOException e) {
            e.printStackTrace();
            return "Error: Cannot connect to server.";
        }
    }

    public synchronized void readResponse(String message, Runnable callback) {
        //String ans = "";
        System.out.println("\nClient: " + message);

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return sendRequest(message); // run in background thread
            }
        };

        // when task is completed, process server response
        task.setOnSucceeded(event -> {
            String[] response = task.getValue().split(":");
            System.out.println("Server: " + task.getValue());
            System.out.println("Server: " + response[1].trim());

            // display response
            Platform.runLater(() -> {
                // show the alert and wait until the user dismisses it
                showAlert(response[0], response[1].trim());

                if(callback != null && !response[0].contains("Incorrect Action")){
                    callback.run(); // run the callback function
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showAlert("Error", "Failed to connect to server."));
        });

        // start the background thread
        new Thread(task).start();
    }

    /*
    //make sure the GUI updates when changes are made to shared schedule
    public void listenForUpdates() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if(message.equals("UPDATE")){
                        if(ignoreNextUpdate){
                            ignoreNextUpdate = false; // reset flag
                            continue; // ignore this update
                        }

                        //trigger fetchAndDisplaySchedule()
                        Platform.runLater(() -> {
                            DisplayScheduleController controller = DisplayScheduleController.getInstance();
                            if (controller != null) {
                                controller.fetchAndDisplaySchedule();
                            }
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Cannot update schedule.");
            }
        }).start();
    }
    */


    //show alerts for actions or errors
    public static void showAlert(String title, String message) {
        Alert alert;
        if (title.equals("Success") || title.equals("Message")) {
            alert = new Alert(Alert.AlertType.INFORMATION);
        } else {
            alert = new Alert(Alert.AlertType.ERROR);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
