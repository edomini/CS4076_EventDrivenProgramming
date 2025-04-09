package com.project.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private static ServerSocket serverSocket; //initialise server socket for client to contact
    private static final int PORT = 1234; //initialise port for client contact
    protected static int clientsConnected;
    private static ArrayList<Schedule> database;

    public static void main(String[] args) {
        database = new ArrayList<Schedule>();
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
                System.out.println("New client connected. Total clients: " + clientsConnected);

                //create a new schedule for each client
                Schedule schedule = new Schedule();
                database.add(schedule); //add schedule to database

                //create a new thread for each client
                new Thread(new ClientHandler(link, schedule)).start();
            } catch (IOException ex){
                System.out.println("Failed to connect to client.");
            }
        } while (true);
    }
}
