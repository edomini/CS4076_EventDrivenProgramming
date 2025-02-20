import java.io.*;
import java.net.*;
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
        //HashMap<String, Lecture> schedule = new HashMap<>();

        Lecture[][] schedule = new Lecture[Lecture.times.size()][Lecture.days.size()];
        for (Lecture[] row : schedule) {
            Arrays.fill(row, null);
        }

        try {
            link = serverSocket.accept(); //wait for client to connect
        } catch (IOException ex) {
            ex.printStackTrace(); //print stack trace for IO errors
            System.exit(1); //terminate the program
        }

        try {
            boolean clientOK = true;
            BufferedReader in = new BufferedReader((new InputStreamReader((link.getInputStream())))); //set up message receiver
            PrintWriter out = new PrintWriter(link.getOutputStream(), true); //set up message writer

            String response = "";

            do {
                String message = in.readLine(); //take in the received message
                // message should be of the form: "ACTION (e.g. ADD), module code, day (e.g. Monday), time (e.g. 14:00), lecture room"

                //separate try block for IncorrectActionException so server keeps running after error
                try {
                    //code to interpret client requests here:
                    if (message.contains("ADD")) {
                        //parse the parts into module code, date, time, lecture room and create Lecture object
                        String[] parts = message.split(",");
                        Lecture lec = new Lecture(parts[1], parts[2], parts[3], parts[4]);

                        //check if room and time slot are free
                        if (schedule[lec.getLecTime()][lec.getDayNum()] == null) {
                            //if free, add lecture to timetable
                            schedule[lec.getLecTime()][lec.getDayNum()] = lec;
                            //send message back
                            response = "Lecture added successfully.";
                        } else {
                            //if not free, send error message
                            response = "Could not add this lecture to the schedule.";
                        }

                    } else if (message.contains("REMOVE")) {
                        //parse the parts into module code, date, time, lecture room and create Lecture object
                        String[] parts = message.split(",");
                        Lecture lec = new Lecture(parts[1], parts[2], parts[3], parts[4]);

                        //check if the lecture exists
                        if (schedule[lec.getLecTime()][lec.getDayNum()].equals(lec)) {
                            //if it exists, remove lecture from timetable
                            schedule[lec.getLecTime()][lec.getDayNum()] = null;
                            //send message back
                            response = "Lecture removed successfully.";
                        } else {
                            //if it doesn't exist, send error message
                            response = "This lecture does not exist.";
                        }
                    } else if (message.contains("DISPLAY")) {
                        //parse the 2D array into a "send-able" format
                        for (Lecture[] i : schedule) {
                            for (Lecture j : i) {
                                response += j.toString();
                                //this adds each lecture as "module code, day number, lecture time, room number\n" to the output string
                                //it will have to separated by "\n" to get the lectures, then by ", " to get the module code, day, etc.
                            }
                        }
                    } else if (message.equals("STOP")) {
                        //send "TERMINATE" message
                        response = "TERMINATE";
                        clientOK = false; //exit the loop and go to finally block
                    } else {
                        throw new IncorrectActionException("This action is not permitted.");
                    }

                    out.println(response); //send the info back to the client

                } catch (IncorrectActionException ex) {
                    System.out.println(ex.getMessage());
                    //send this error to the client somehow
                    out.println("ERROR: " + ex.getMessage());
                }

            } while (clientOK);

        } catch (IOException ex) {
            ex.printStackTrace(); //print stack trace for IO errors
        } finally {
            System.out.println("\n Closing connection...\n");
            try {
                link.close(); //close the communication socket
            } catch (IOException ex) {
                System.out.println("Error disconnecting from port " + PORT); //catch port errors
                System.exit(1); //terminate the program
            }
        }
    }
}
