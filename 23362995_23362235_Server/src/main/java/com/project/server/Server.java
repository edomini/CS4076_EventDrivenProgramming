package com.project.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static ServerSocket serverSocket; //initialise server socket for client to contact
    private static final int PORT = 1234; //initialise port for client contact
    protected static int clientsConnected;

    public static void main(String[] args) {
        System.out.println("Opening port...\n");

        try {
            serverSocket = new ServerSocket(PORT); //initialise the server socket to port 1234
            System.out.println("Server connected to port " + PORT);
        } catch (IOException ex) { //catch IO (port) errors
            System.out.println("Port connection error at port " + PORT);
            System.exit(1); //terminate the program
        }

        Schedule schedule = new Schedule();

        do {
            try {
                Socket link = serverSocket.accept();
                clientsConnected++;
                System.out.println("New client connected. Total clients: " + clientsConnected);
                new Thread(new ClientHandler(link, schedule)).start();
            } catch (IOException ex){
                System.out.println("Failed to connect to client.");
            }
        } while (true);
    }
}
