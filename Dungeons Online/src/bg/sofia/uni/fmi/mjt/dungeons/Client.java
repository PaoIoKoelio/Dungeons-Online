package bg.sofia.uni.fmi.mjt.dungeons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 1234;
    private static final String CONNECTED_TO_SERVER = "Connected to the server.";
    private static final String EXITING_GAME = "Exiting the game...";
    private static final String EXIT = "exit";
    private static final String PROBLEM_WITH_NETWORK = "There is a problem with the network communication";

    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println(CONNECTED_TO_SERVER);

            while (true) {
                if (serverInput.ready()) {
                    String serverResponse = serverInput.readLine();
                    System.out.println(serverResponse);
                }

                if (userInput.ready()) {
                    String userCommand = userInput.readLine();
                    if (userCommand.equalsIgnoreCase(EXIT)) {
                        out.println(userCommand);
                        System.out.println(EXITING_GAME);
                        break;
                    }
                    out.println(userCommand);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(PROBLEM_WITH_NETWORK, e);
        }
    }
}
