package com.project.client;

import javafx.scene.control.Alert;
import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 1234;

    public boolean connect() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public void disconnect(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String sendRequest(String request) {
        try {
            //send request to server
            out.println(request);

            //read response from server
            String response = in.readLine();

            //return response as a string
            return response.trim();

        } catch (IOException e) {
            e.printStackTrace();
            return "Error: Cannot connect to server.";
        }
    }

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
