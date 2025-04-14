package com.project.server;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static ServerSocket serverSocket; //initialise server socket for client to contact
    private static final int PORT = 1234; //initialise port for client contact
    protected static int clientsConnected;
    protected static int offset;
    //use concurrent hash map as database to ensure thread safety
    protected static ConcurrentHashMap<String, Schedule> database;

    public static void main(String[] args) {
        database = new ConcurrentHashMap<String, Schedule>();
        System.out.println("Opening port...\n");

        try {
            serverSocket = new ServerSocket(PORT); //initialise the server socket to port 1234
            System.out.println("Server connected to port " + PORT);
        } catch (IOException ex) { //catch IO (port) errors
            System.out.println("Port connection error at port " + PORT);
            System.exit(1); //terminate the program
        }

        do {
            try {
                //accept client connection
                Socket link = serverSocket.accept();
                clientsConnected++;
                System.out.println("\nNew client connected. Total clients: " + clientsConnected);

                //read course code from client
                BufferedReader tempIn = new BufferedReader(new InputStreamReader(link.getInputStream()));
                String message = tempIn.readLine().trim();

                String course = "";
                if(message.contains("courseCode")){
                    course = message.split(":")[1].trim(); //get course code from message
                }

                //check if course already has schedule in database
                Schedule schedule = database.computeIfAbsent(course, k -> {
                    //if not, create a new schedule
                    return new Schedule();
                });

                //create a new thread for each client
                new Thread(new ClientHandler(link, schedule, clientsConnected + offset, course)).start();
            } catch (IOException ex){
                System.out.println("Failed to connect to client.");
            }
        } while (true);
    }
}
