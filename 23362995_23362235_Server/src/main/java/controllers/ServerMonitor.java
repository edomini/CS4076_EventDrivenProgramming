package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.project.server.ClientHandler;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.application.Platform;

public class ServerMonitor {
    private static ServerMonitor instance;
    public boolean viewMonitor = true;
    // lists of all connected clients and server log messages
    private static final List<String> connectedClients = Collections.synchronizedList(new ArrayList<>());
    private static final List<String> logMessages = Collections.synchronizedList(new ArrayList<>());

    @FXML
    private TextArea connectedClientsArea;  
    @FXML
    private TextArea logMessagesArea;  
    @FXML
    public Button stopButton;
    @FXML
    private Button refreshButton;

    // to allow for refresh
    public ServerMonitor() {
        instance = this;
    }
    public static ServerMonitor getInstance() {
        return instance;
    }

    // to check if server is running monitor page or display page
    public static boolean viewingMonitor() {
        return instance.viewMonitor;
    }
    public static void setViewingMonitor(boolean viewMonitor) {
        instance.viewMonitor = viewMonitor;
    }

    public static Stage getStage() {
        return (Stage) instance.logMessagesArea.getScene().getWindow();
    }

    //display all the clients and log messages
    public void initialize() {
        updateMonitor();
    }

    public static void addClient(String clientInfo) {
        connectedClients.add(clientInfo);
        log("Client connected: " + clientInfo);

        Platform.runLater(() -> {
            // update connected clients area
            instance.connectedClientsArea.appendText(clientInfo + "\n");
        });
    }

    public static void removeClient(String clientInfo) {
        connectedClients.remove(clientInfo);
        log("Client disconnected: " + clientInfo);

        // refresh client list to show only remaining clients
        Platform.runLater(() -> {
            StringBuilder clientsText = new StringBuilder();
            for (String client : connectedClients) {
                clientsText.append(client).append("\n");
            }
            instance.connectedClientsArea.setText(clientsText.toString());
        });
    }

    // create a copy
    public static List<String> getConnectedClients() {
        return new ArrayList<>(connectedClients);
    }

    // log the messages in the log area
    public static void log(String message) {
        String timestamp = "[" + java.time.LocalTime.now().withNano(0) + "] ";

        Platform.runLater(() -> {
            // update the log messages area in the JavaFX application thread
            instance.logMessagesArea.appendText(timestamp + message + "\n");
        });
        logMessages.add(timestamp + message);
    }

    // create a copy
    public static List<String> getLogs() {
        return new ArrayList<>(logMessages);
    }

    // stop the server and close application
    @FXML
    private void handleStopAction() {
        System.out.println("Stopping the server...");
        log("Server stopping...");

        Platform.runLater(() -> {
            ClientHandler.showAlert("TERMINATE", "Shutting down server...");
            System.exit(0);
        });
    }
    

    // refresh the connected clients and log messages
    public void updateMonitor() {
        // update clients
        StringBuilder clientsText = new StringBuilder();
        for (String client : connectedClients) {
            clientsText.append(client).append("\n");
        }
        connectedClientsArea.setText(clientsText.toString());
        connectedClientsArea.appendText("");
    
        // update log messages
        StringBuilder logsText = new StringBuilder();
        for (String log : logMessages) {
            logsText.append(log).append("\n");
        }
        logMessagesArea.setText(logsText.toString());
        logMessagesArea.appendText("");
    
        System.out.println("Updated monitor - Clients: " + connectedClients.size() + ", Logs: " + logMessages.size());
    }
    

    // refresh the GUI
    @FXML
    private void handleRefreshAction(){
        updateMonitor();
    }
}
