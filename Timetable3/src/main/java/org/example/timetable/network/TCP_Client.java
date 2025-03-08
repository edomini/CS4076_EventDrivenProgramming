package org.example.timetable.network;

import javafx.scene.control.Alert;
import java.io.*;
import java.net.Socket;

public class TCP_Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 1234;

    public static String sendRequest(String request) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            //send request to server
            out.println(request);

            //read response from server
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
            }

            //return response as a string
            return response.toString().trim();

        } catch (IOException e) {
            e.printStackTrace();
            return "Error: Cannot connect to server.";
        }
    }

    //show alerts for actions or errors
    public static void showAlert(String title, String message) {
        Alert alert;
        if(title.equals("Success") || title.equals("Message")){
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
