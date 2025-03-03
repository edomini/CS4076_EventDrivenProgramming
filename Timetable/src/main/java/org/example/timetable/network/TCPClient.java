package org.example.timetable.network;

import java.io.*;
import java.net.Socket;

public class TCP_Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static String sendRequest(String request) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(request);

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
            }

            return response.toString().trim();

        } catch (IOException e) {
            e.printStackTrace();
            return "Error: Cannot connect to server.";
        }
    }
}
