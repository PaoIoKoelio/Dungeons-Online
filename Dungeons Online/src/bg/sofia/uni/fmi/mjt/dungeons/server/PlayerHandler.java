package bg.sofia.uni.fmi.mjt.dungeons.server;

import bg.sofia.uni.fmi.mjt.dungeons.engine.Direction;
import bg.sofia.uni.fmi.mjt.dungeons.engine.GameEngine;
import bg.sofia.uni.fmi.mjt.dungeons.actor.Hero;
import bg.sofia.uni.fmi.mjt.dungeons.exception.BackpackCapacityExceededException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.GameIsFullException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.NoFreeSpaceException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.NoSuchPlayerException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.WrongMoveArgumentException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.NoSuchItemException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PlayerHandler implements Runnable {
    private static final int FIRST_TOKEN = 0;
    private static final int SECOND_TOKEN = 1;
    private static final int THIRD_TOKEN = 2;
    private static final String PLAYER_QUIT_BECAUSE_OF_FULL_MAP = "Player quit because of full map";
    private Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private GameEngine gameMap;
    private Hero playerHero;
    private boolean running;
    private final Object lock = new Object();
    private static final int SEND_COMMAND_TOKENS = 3;
    private static final int MOVE_AND_USE_COMMAND_TOKENS = 2;
    private static final String HERO_STATUS = "Hero Status:";
    private static final String LEVEL = "Level: ";
    private static final String HEALTH = "Health: ";
    private static final String MANA = "Mana: ";
    private static final String ATTACK = "Attack: ";
    private static final String DEFENCE = "Defence: ";
    private static final String INVALID_MOVE_COMMAND_MESSAGE =
            "Invalid move command. Please specify direction (up, down, left, right).";
    private static final String EXITING_GAME_MESSAGE_BECAUSE_FULL = "Exiting game. Try again later.";
    private static final String UNKNOWN_COMMAND_MESSAGE = "Unknown command: ";
    private static final String EXITING_GAME_BY_PLAYER = "Exiting the game...";
    private static final String MAP_MESSAGE_BEGINNING = "MAP:\n";
    private static final String NEW_LINE_SYMBOL = "\n";
    private static final String SPACE_SYMBOL = " ";
    private static final String WELCOME_MESSAGE = "Welcome to Dungeons Online!";
    private static final String COULDNT_MAKE_MOVE_MESSAGE = "Couldn't make move.";
    private static final String INVALID_ARGUMENT = "Invalid argument.";
    private static final String MOVE = "MOVE";
    private static final String BACKPACK = "BACKPACK";
    private static final String STATUS = "STATUS";
    private static final String SEND = "SEND";
    private static final String USE = "USE";
    private static final String EXIT = "EXIT";
    private static final String DOWN = "DOWN";
    private static final String UP = "UP";
    private static final String LEFT = "LEFT";
    private static final String RIGHT = "RIGHT";
    private static final String HERO_NAME = "heroName";
    private static final String INVALID_SEND_MESSAGE = "Invalid send command. Please specify player id and item";
    private static final String NO_SUCH_PLAYER_MESSAGE = "Couldn't make exchange. No such player.";
    private static final String INVALID_USE_COMMAND = "Invalid use command.";
    private static final String ERROR_MESSAGE = "An error occurred: ";

    public PlayerHandler(Socket clientSocket, GameEngine gameMap) throws IOException {
        this.clientSocket = clientSocket;
        this.gameMap = gameMap;
        this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.output = new PrintWriter(clientSocket.getOutputStream(), true);
        this.running = true;
        initializePlayer();
    }

    public Hero getPlayerHero() {
        return playerHero;
    }

    public void sendGameMap() {
        char[][] map = gameMap.getMap();
        StringBuilder sb = new StringBuilder();
        for (char[] row : map) {
            for (char cell : row) {
                sb.append(cell).append(SPACE_SYMBOL);
            }
            sb.append(NEW_LINE_SYMBOL);
        }
        output.println(MAP_MESSAGE_BEGINNING + sb);
    }

    private void initializePlayer() {
        output.println(WELCOME_MESSAGE);

        synchronized (lock) {
            try {
                this.playerHero = new Hero(HERO_NAME, gameMap.findFreePos(), gameMap.findFreeId());
                gameMap.addHero(this);
            } catch (NoFreeSpaceException | GameIsFullException e) {
                sendMessage(e.getMessage());
                sendMessage(EXITING_GAME_MESSAGE_BECAUSE_FULL);
                running = false;
            }
        }
    }

    private void handleCommand(String command) {
        String[] tokens = command.split(SPACE_SYMBOL);
        String action = tokens[FIRST_TOKEN].toUpperCase();
        switch (action) {
            case MOVE -> handleMove(tokens);
            case STATUS -> sendHeroStatus();
            case BACKPACK -> showBackpack();
            case SEND -> handleExchange(tokens);
            case USE -> useItem(tokens);
            case EXIT -> handleQuit();
            default -> output.println(UNKNOWN_COMMAND_MESSAGE + action);
        }
    }

    private void handleQuit() {
        synchronized (lock) {
            running = false;
            output.println(EXITING_GAME_BY_PLAYER);
            gameMap.removePlayer(playerHero);
        }
    }

    private void handleMove(String[] tokens) {
        if (tokens.length < MOVE_AND_USE_COMMAND_TOKENS) {
            output.println(INVALID_MOVE_COMMAND_MESSAGE);
        }
        synchronized (lock) {
            try {
                String message = gameMap.makeMove(playerHero, parseDirection(tokens[SECOND_TOKEN].toUpperCase()));
                output.println(message);
            } catch (BackpackCapacityExceededException | WrongMoveArgumentException e) {
                sendMessage(e.getMessage());
            } catch (NoFreeSpaceException e) {
                sendMessage(e.getMessage());
                sendMessage(EXITING_GAME_MESSAGE_BECAUSE_FULL);
                running = false;
                System.out.println(PLAYER_QUIT_BECAUSE_OF_FULL_MAP);
                handleQuit();
            } catch (NoSuchPlayerException e) {
                sendMessage(COULDNT_MAKE_MOVE_MESSAGE);
            }
        }
    }

    private Direction parseDirection(String directionString) throws WrongMoveArgumentException {
        return switch (directionString) {
            case DOWN -> Direction.DOWN;
            case UP -> Direction.UP;
            case LEFT -> Direction.LEFT;
            case RIGHT -> Direction.RIGHT;
            default -> throw new WrongMoveArgumentException(INVALID_MOVE_COMMAND_MESSAGE);
        };
    }

    private void handleExchange(String[] tokens) {
        if (tokens.length < SEND_COMMAND_TOKENS) {
            output.println(INVALID_SEND_MESSAGE);
        }
        if (isNumber(tokens[SECOND_TOKEN]) && isNumber(tokens[THIRD_TOKEN])) {
            synchronized (lock) {
                try {
                    output.println(gameMap.sendItem(playerHero, Integer.parseInt(tokens[SECOND_TOKEN]),
                            Integer.parseInt(tokens[THIRD_TOKEN])));
                } catch (BackpackCapacityExceededException | NoSuchItemException e) {
                    sendMessage(e.getMessage());
                } catch (NoSuchPlayerException e) {
                    sendMessage(NO_SUCH_PLAYER_MESSAGE);
                }
            }
        } else {
            output.println(INVALID_ARGUMENT);
        }
    }

    private void showBackpack() {
        sendMessage(playerHero.getBackpackItems());
    }

    private boolean isNumber(String n) {
        try {
            Integer.parseInt(n);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private void useItem(String[] tokens) {
        if (tokens.length < MOVE_AND_USE_COMMAND_TOKENS) {
            output.println(INVALID_USE_COMMAND);
        }
        if (isNumber(tokens[SECOND_TOKEN])) {
            synchronized (lock) {
                try {
                    output.println(playerHero.useItem(Integer.parseInt(tokens[SECOND_TOKEN])));
                } catch (NoSuchItemException e) {
                    sendMessage(e.getMessage());
                }
            }
        } else {
            output.println(INVALID_ARGUMENT);
        }
    }

    private void sendHeroStatus() {
        output.println(HERO_STATUS);
        output.println(LEVEL + playerHero.getLevel());
        output.println(HEALTH + playerHero.getHealth());
        output.println(MANA + playerHero.getMana());
        output.println(ATTACK + playerHero.getAttack());
        output.println(DEFENCE + playerHero.getDefence());
    }

    public void sendMessage(String message) {
        output.println(message);
    }

    @Override
    public void run() {
        try {
            String command;
            while (running && (command = input.readLine()) != null) {
                handleCommand(command);
            }
        } catch (Exception e) {
            output.println(ERROR_MESSAGE);
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
