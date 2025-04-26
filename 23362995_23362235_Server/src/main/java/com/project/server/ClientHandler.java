package com.project.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import controllers.ServerDisplayScheduleController;
import javafx.scene.control.Alert;

public class ClientHandler implements Runnable {
    private final Socket link;
    private final Socket updateLink;
    private final Schedule schedule;
    private final int clientNumber;
    private String courseCode;
    private BufferedReader in;
    private PrintWriter out;
    protected PrintWriter updateOut;

    public ClientHandler(Socket socket, Socket updateSocket, Schedule schedule, int clientNumber, String courseCode) {
        this.link = socket;
        this.updateLink = updateSocket;
        this.schedule = schedule;
        this.clientNumber = clientNumber;
        this.courseCode = courseCode;

        //add client to schedule
        schedule.addClient(this);

        //add client to monitor
        ServerMonitor.addClient(this.toString()); 
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader((new InputStreamReader((link.getInputStream())))); //set up message receiver
            out = new PrintWriter(link.getOutputStream(), true); //set up message writer
            updateOut = new PrintWriter(updateLink.getOutputStream(), true); //set up update writer

            String message = "";
            while((message = in.readLine().trim()) != null){
                System.out.println("\n" + this.toString() + ": " + message);

                String response = processRequest(message);
                out.println(response);
                System.out.println("Server: " + response);

                if(message.equalsIgnoreCase("STOP")){
                    break;
                }
            }

            System.out.printf("\nClient %s disconnected. Total clients: %d\n\n", this.toString(), (Server.clientsConnected - 1));
            link.close();
            synchronized (this) {
                Server.clientsConnected--;
                Server.offset++;
            }
            schedule.removeClient(this);


            ServerMonitor.removeClient(this.toString()); // Track disconnection

        } catch (IOException ex) {
            System.out.println("Connection error: " + ex.getMessage());
        }
    }

    private String processRequest(String message){
        //remove action from message
        String[] str = message.split(",", 2);
        String action = str[0].toUpperCase();

        //check if other client GUIs need to be updated
        boolean mod = false;

        try {
            switch (action) {
                case "ADD":
                    String[] addParts = str[1].split(",");
                    Lecture addLec = new Lecture(addParts[0], addParts[1], addParts[2], addParts[3]);
                    
                    mod = true;
                    ServerMonitor.log(this.toString() + " added a lecture: " + str[1]);
                    return schedule.addLecture(addLec);
                case "REMOVE":
                    String[] remParts = str[1].split(",");
                    Lecture remLec = new Lecture(remParts[0], remParts[1], remParts[2], remParts[3]);
                    
                    mod = true;
                    ServerMonitor.log(this.toString() + " removed a lecture: " + str[1]);
                    return schedule.removeLecture(remLec);
                case "DISPLAY":
                    ServerMonitor.log(this.toString() + " viewed the schedule.");
                    return schedule.displaySchedule();
                case "IMPORT":
                    ServerMonitor.log(this.toString() + " imported a schedule.");
                    return schedule.importSchedule(str[1]);
                case "CLEAR":
                    ServerMonitor.log(this.toString() + " cleared the schedule.");
                    return schedule.clearSchedule();
                case "EARLY":
                    mod = true;
                    ServerMonitor.log(this.toString() + " requested early lectures adjustment.");
                    return schedule.earlyLectures();
                case "SERVER_SCHEDULE":
                    ServerMonitor.log(this.toString() + " requested server schedule.");
                    return schedule.displayServerSchedule(str[1]);
                case "STOP":
                    return "TERMINATE: Closing connection...";
                default:
                    throw new IncorrectActionException("Apologies, Amelia and Emily don't provide this service.");
            }
        } catch (IncorrectActionException ex) {
            return "Incorrect Action: " + ex.getMessage();
        } finally {
            //send message "UPDATE" to all clients w same course code
            if(mod) {
                broadcast("UPDATE");
            }
            mod = false;
        }
    }

    // send update message to both client and server GUIs
    public void broadcast(String message) {
        for (ClientHandler client : schedule.getClients()) {
            if (client != this) { // don't send to self
                client.updateOut.println(message);
            }
        }

        ServerDisplayScheduleController controller = ServerDisplayScheduleController.getInstance();
        if (controller != null) {
            // refresh the GUI
            controller.fetchAndDisplaySchedule();
        }
    }

    @Override
    public String toString() {
        return "Client " + clientNumber + " (" + courseCode + ")";
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