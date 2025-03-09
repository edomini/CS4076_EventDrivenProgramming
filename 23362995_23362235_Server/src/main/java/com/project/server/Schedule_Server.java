package com.project.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Schedule_Server {
    private static ServerSocket serverSocket; //initialise server socket for client to contact
    private static final int PORT = 1234; //initialise port for client contact

    public static void main(String[] args) {
        System.out.println("Opening port...\n");
        try {
            serverSocket = new ServerSocket(PORT); //initialise the server socket to port 1234
            System.out.println("Connected to Port " + PORT);
        } catch (IOException ex) { //catch IO (port) errors
            System.out.println("Port connection error at port " + PORT);
            System.exit(1); //terminate the program
        }

        do {
            run(); //run the server
        } while (true);
    }

    private static void run() {
        Socket link = null; //initialise the link socket for communication with client
        boolean clientOK = true; //boolean for loop to allow continuous communication
        ArrayList<String> modules = new ArrayList<String>(5); //list of modules to make sure max is 5
        int moduleCount = 0;

        Lecture[][] schedule = new Lecture[Lecture.times.size()][Lecture.days.size()]; //schedule array for display
        for (Lecture[] row : schedule) {
            Arrays.fill(row, null); //initialise to null
        }

        try {
            BufferedReader in = null; //initialise message receiver
            PrintWriter out = null; // initialise message writer

            String response = ""; //message received from client

            do {
                try {
                    link = serverSocket.accept(); //wait for client to connect
                    System.out.println("Connection Successful\n");
                    in = new BufferedReader((new InputStreamReader((link.getInputStream())))); //set up message receiver
                    out = new PrintWriter(link.getOutputStream(), true); //set up message writer

                    response = ""; //reset response to empty string
                } catch (IOException ex) {
                    System.out.println("Failed to connect to client");
                    ex.printStackTrace(); //print stack trace for IO errors
                    System.exit(1); //terminate the program
                }

                String message = in.readLine().trim(); //take in the received message
                // message should be of the form: "ACTION (e.g. ADD), module code, day (e.g. Monday), time (e.g. 14:00), lecture room"

                // print message from client
                System.out.println("message received: " + message);

                //separate try block for IncorrectActionException so server keeps running after error
                try {
                    //code to interpret client requests here:
                    // add lecture
                    if (message.contains("ADD")) {
                        //split the message into module code, date, time, lecture room and create Lecture object
                        String[] parts = message.split(",");
                        Lecture lec = new Lecture(parts[1], parts[2], parts[3], parts[4]);

                        // check if module is already in list
                        if (!modules.contains(parts[1])) {
                            // if not, check if list size < 5
                            if (moduleCount < 5) {
                                //check if time slot is free
                                if (schedule[lec.getLecTimeNum()][lec.getDayNum()] == null) {
                                    //if free, add lecture to schedule
                                    schedule[lec.getLecTimeNum()][lec.getDayNum()] = lec;
                                    //add module to list
                                    modules.add(parts[1]);
                                    moduleCount++;
                                    //send message back
                                    response = "Success: Lecture added successfully.";
                                } else {
                                    //if not free, send error message
                                    throw new IncorrectActionException("Time slot taken. Cannot add this lecture to the schedule.");
                                }

                            } else {
                                throw new IncorrectActionException("Max 5 modules per course, cannot add another module.");
                            }
                        } else {
                            // add lecture to schedule
                            //check if room and time slot are free
                            if (schedule[lec.getLecTimeNum()][lec.getDayNum()] == null) {
                                //if free, add lecture to timetable
                                schedule[lec.getLecTimeNum()][lec.getDayNum()] = lec;
                                //send message back
                                response = "Success: Lecture added successfully.";
                            } else {
                                //if not free, send error message
                                throw new IncorrectActionException("Time slot taken. Cannot add this lecture to the schedule.");
                            }
                        }
                        // remove lecture
                    } else if (message.contains("REMOVE")) {
                        //split the message into module code, date, time, lecture room and create Lecture object
                        String[] parts = message.split(",");
                        Lecture lec = new Lecture(parts[1], parts[2], parts[3], parts[4]);

                        //check if the lecture exists
                        if (schedule[lec.getLecTimeNum()][lec.getDayNum()] != null &&
                                schedule[lec.getLecTimeNum()][lec.getDayNum()].getModuleCode().equals(lec.getModuleCode()) &&
                                schedule[lec.getLecTimeNum()][lec.getDayNum()].getRoomNum().equals(lec.getRoomNum())) {
                            //if it exists, remove lecture from timetable
                            schedule[lec.getLecTimeNum()][lec.getDayNum()] = null;
                            
                            //check if any other lectures of this module are in the schedule
                            boolean moreLecs = false;
                            outerloop:
                            for(Lecture[] i : schedule){
                                for(Lecture j : i){
                                    if(j != null && lec.getModuleCode().equals(j.getModuleCode())){
                                        moreLecs = true;
                                        break outerloop;
                                    }
                                }
                            }
                            //if no other lectures for this module:
                            if(!moreLecs){
                                //remove that module from saved list
                                modules.remove(lec.getModuleCode());
                                //decrement module counter so a new module can be added
                                moduleCount--;
                            }
                            //send message back
                            response = String.format("Success: Lecture removed successfully.\nDay: %s, Time: %s, Room: %s", lec.getDay(), lec.getLecTime(), lec.getRoomNum());
                        } else {
                            //if it doesn't exist, send error message
                            throw new IncorrectActionException("This lecture does not exist. Cannot remove from schedule.");
                        }
                        // display schedule
                    } else if (message.contains("DISPLAY")) {
                        if (moduleCount == 0) {
                            response = "Schedule empty.";
                        } else {
                            //parse the 2D array into a "send-able" format
                            for (Lecture[] i : schedule) {
                                for (Lecture j : i) {
                                    if (j != null) {
                                        response += j.toString();
                                    }
                                    //this adds each lecture as "module code, day number, lecture time, room number;" to the output string
                                    //it will have to separated by ";" to get the lectures, then by "," to get the module code, day, etc.
                                }
                            }
                        }
                        // import schedule from csv
                    } else if (message.contains("IMPORT")) {
                        //remove action word "IMPORT"
                        String[] importedEntries = message.split(",", 2)[1].split(";");

                        for (String entry : importedEntries) {
                            //split each module into parts and add them to the schedule if possible
                            String[] parts = entry.split(",");
                            Lecture lec = new Lecture(parts[0], parts[1], parts[2], parts[3]);

                            // check if module is already in list
                            if (!modules.contains(parts[0])) {
                                // if not, check if list size < 5
                                if (moduleCount < 5) {
                                    // add lecture to schedule
                                    //check if time slot is free
                                    if (schedule[lec.getLecTimeNum()][lec.getDayNum()] == null) {
                                        //if free, add lecture to timetable
                                        schedule[lec.getLecTimeNum()][lec.getDayNum()] = lec;
                                        // add module to list
                                        modules.add(parts[0]);
                                        moduleCount++;
                                    } else {
                                        //if not free, send error message
                                        throw new IncorrectActionException("Import Error: Time slot taken. Cannot add a lecture to the schedule.");
                                    }

                                } else {
                                    throw new IncorrectActionException("Module limit exceeded. Max 5 modules per course.");
                                }
                            } else {
                                // add lecture to schedule
                                //check if room and time slot are free
                                if (schedule[lec.getLecTimeNum()][lec.getDayNum()] == null) {
                                    //if free, add lecture to timetable
                                    schedule[lec.getLecTimeNum()][lec.getDayNum()] = lec;
                                } else {
                                    //if not free, send error message
                                    throw new IncorrectActionException("Import Error: Time slot taken. Cannot add a lecture to the schedule.");
                                }
                            }
                        }
                        response = "Success: Schedule imported successfully.";
                        //clear schedule
                    } else if (message.equals("CLEAR")) {
                        //reset module list and counter so a new 5 modules can be added
                        modules.clear();
                        moduleCount = 0;
                        
                        //set the schedule array back to null values
                        for (Lecture[] row : schedule) {
                            Arrays.fill(row, null); //reset to null
                        }
                            
                        //set message for client
                        response = "Success: Schedule cleared successfully.";
                        //shut down the program
                    } else if (message.equals("STOP")) {
                        //send "TERMINATE" message
                        response = "TERMINATE: Closing program...";
                        clientOK = false; //exit the loop and go to finally block
                        //other (disallowed) actions
                    } else { // if (message.contains("OTHER"))
                        throw new IncorrectActionException("Apologies, Amelia and Emily don't provide this service.");
                    }

                    out.println(response); //send the info back to the client
                    System.out.println("message sent: " + response);

                } catch (IncorrectActionException ex) {
                    System.out.println("IncorrectActionException: " + ex.getMessage());
                    //send this error to the client
                    out.println("IncorrectActionException: " + ex.getMessage());
                } finally {
                    System.out.println("\nClosing connection...\n");
                    try {
                        link.close(); //close the communication socket
                    } catch (IOException ex) {
                        System.out.println("Error disconnecting from port " + PORT); //catch port errors
                        System.exit(1); //terminate the program
                    }
                }

            } while (clientOK);

        } catch (IOException ex) {
            ex.printStackTrace(); //print stack trace for IO errors
        }
    }
}
