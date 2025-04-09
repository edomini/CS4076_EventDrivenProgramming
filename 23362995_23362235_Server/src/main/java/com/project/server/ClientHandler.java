package com.project.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket link;
    private final Schedule schedule;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket, Schedule sch) {
        this.link = socket;
        this.schedule = sch;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader((new InputStreamReader((link.getInputStream())))); //set up message receiver
            out = new PrintWriter(link.getOutputStream(), true); //set up message writer

            String message = "";
            while((message = in.readLine().trim()) != null){
                System.out.println("\nmessage received: " + message);

                String response = processRequest(message);
                out.println(response);
                System.out.println("message sent: " + response);

                if(message.equalsIgnoreCase("STOP")){
                    break;
                }
            }

            System.out.println("Client disconnected.");
            link.close();
            Server.clientsConnected--;

        } catch (IOException ex) {
            System.out.println("Connection error: " + ex.getMessage());
        }
    }

    private String processRequest(String message){
        //remove action from message
        String[] str = message.split(",", 2);
        String action = str[0].toUpperCase();

        try {
            switch (action) {
                case "ADD":
                    String[] addParts = str[1].split(",");
                    Lecture addLec = new Lecture(addParts[0], addParts[1], addParts[2], addParts[3]);
                    return schedule.addLecture(addLec);
                case "REMOVE":
                    String[] remParts = str[1].split(",");
                    Lecture remLec = new Lecture(remParts[0], remParts[1], remParts[2], remParts[3]);
                    return schedule.removeLecture(remLec);
                case "DISPLAY":
                    return schedule.displaySchedule();
                case "IMPORT":
                    String[] importedEntries = str[1].split(";");

                    for (String entry : importedEntries) {
                        //split each module into parts and add them to the schedule if possible
                        String[] parts = entry.split(",");
                        Lecture lec = new Lecture(parts[0], parts[1], parts[2], parts[3]);
                        schedule.addLecture(lec);
                    }
                    return "Success: Schedule imported successfully.";
                case "CLEAR":
                    return schedule.clearSchedule();
                case "STOP":
                    return "TERMINATE: Closing connection...";
                default:
                    throw new IncorrectActionException("Apologies, Amelia and Emily don't provide this service.");
            }
        } catch (IncorrectActionException ex) {
            return "Incorrect Action: " + ex.getMessage();
        }
    }
}
