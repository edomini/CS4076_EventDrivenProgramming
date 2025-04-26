package com.project.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static ServerSocket serverSocket; // server socket for client to connect
    private static ServerSocket updateSocket; // socket to send update prompts
    protected static int clientsConnected;
    protected static int offset;
    //use concurrent hash map as database to ensure thread safety
    public static ConcurrentHashMap<String, Schedule> database = new ConcurrentHashMap<>();

    // connect to ports and run server
    public static void startServer(int port) {
        System.out.println("Opening port...\n");

        try {
            serverSocket = new ServerSocket(port);
            updateSocket = new ServerSocket(port + 1);
            System.out.println("Server connected to port " + port);
            System.out.println("Update Socket connected to port " + (port + 1));
        } catch (IOException ex) {
            System.out.println("Port connection error at ports " + port + " and " + (port + 1));
            return;
        }

        while (true) {
            try {
                // accept client connections
                Socket link = serverSocket.accept();
                Socket updateLink = updateSocket.accept();
                clientsConnected++;
                System.out.println("\nNew client connected. Total clients: " + clientsConnected);

                // read course code from client
                BufferedReader tempIn = new BufferedReader(new InputStreamReader(link.getInputStream()));
                String message = tempIn.readLine().trim();

                String course = "";
                if (message.contains("courseCode")) {
                    course = message.split(":")[1].trim();
                }

                //check if course already has schedule in database
                Schedule schedule = database.computeIfAbsent(course, k -> {
                    //if not, create a new schedule
                    return new Schedule();
                });

                // create a new thread for each client
                new Thread(new ClientHandler(link, updateLink, schedule, clientsConnected + offset, course)).start();
            } catch (IOException ex) {
                System.out.println("Failed to connect to client.");
            }
        }
    }
}