package bg.sofia.uni.fmi.mjt.dungeons.server;

import bg.sofia.uni.fmi.mjt.dungeons.actor.Enemy;
import bg.sofia.uni.fmi.mjt.dungeons.engine.GameEngine;
import bg.sofia.uni.fmi.mjt.dungeons.treasure.HealthPotion;
import bg.sofia.uni.fmi.mjt.dungeons.treasure.Treasure;
import bg.sofia.uni.fmi.mjt.dungeons.treasure.Weapon;

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final String CLIENT_CONNECTED = "Client connected: ";
    private ServerSocket serverSocket;
    private ExecutorService executor;
    private GameEngine gameMap;
    private static final int SERVER_PORT = 1234;
    private static final int THREAD_COUNT = 10;

    public Server(int port, GameEngine gameMap) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.executor = Executors.newFixedThreadPool(THREAD_COUNT);
        this.gameMap = gameMap;
    }

    public void start() {
        try {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println(CLIENT_CONNECTED + clientSocket.getInetAddress());
                    executor.submit(new PlayerHandler(clientSocket, gameMap));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            executor.shutdown();
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        char[][] charMap = {{'.', '#', '.', '.'},
                {'M', 'T', '.', '.'}};
        Enemy[] enemies = new Enemy[1];
        enemies[0] = new Enemy("Grendel", 0, 0, 100, 100, new Weapon("Claws", 1, 7), null, 1);
        Treasure[] treasures = new Treasure[1];
        treasures[0] = new HealthPotion(100);
        Server server = new Server(SERVER_PORT, new GameEngine(charMap, enemies, treasures));
        server.start();
    }

}

