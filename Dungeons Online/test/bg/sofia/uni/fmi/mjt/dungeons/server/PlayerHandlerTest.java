package bg.sofia.uni.fmi.mjt.dungeons.server;

import bg.sofia.uni.fmi.mjt.dungeons.actor.Enemy;
import bg.sofia.uni.fmi.mjt.dungeons.engine.GameEngine;
import bg.sofia.uni.fmi.mjt.dungeons.exception.BackpackCapacityExceededException;
import bg.sofia.uni.fmi.mjt.dungeons.treasure.HealthPotion;
import bg.sofia.uni.fmi.mjt.dungeons.treasure.Spell;
import bg.sofia.uni.fmi.mjt.dungeons.treasure.Treasure;
import bg.sofia.uni.fmi.mjt.dungeons.treasure.Weapon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;

public class PlayerHandlerTest {
    private GameEngine map;
    private Enemy[] enemies;
    private Treasure[] treasures;

    @BeforeEach
    void initializeMap() {
        char[][] charMap = {{'T', '#', '.', '.'},
                {'M', '#', '.', '.'},
                {'T', 'M', '.', '.'},
                {'.', '.', '.', '.'}};
        enemies = new Enemy[2];
        enemies[0] = new Enemy("Grendel", 0, 1, 100, 100, new Weapon("Claws", 1, 4), null, 4);
        enemies[1] = new Enemy("Baba Yaga", 10, 40, 30, 400, null, new Spell("fireball", 300, 10, 10), 10);
        treasures = new Treasure[2];
        treasures[0] = new HealthPotion(100);
        treasures[1] = new Weapon("axe", 100, 3);
        map = new GameEngine(charMap, enemies, treasures);
    }

    @Test
    void testHandleInvalidMoveCommand() throws IOException {
        Socket socket1 = Mockito.mock();

        InputStream mockInputStream1 = new ByteArrayInputStream("MOVE ASDF".getBytes());
        OutputStream mockOutputStream1 = new ByteArrayOutputStream();

        Mockito.when(socket1.getInputStream()).thenReturn(mockInputStream1);
        Mockito.when(socket1.getOutputStream()).thenReturn(mockOutputStream1);

        PlayerHandler playerHandler = new PlayerHandler(socket1, map);

        playerHandler.run();

        Assertions.assertTrue(mockOutputStream1.toString().trim().endsWith("Invalid move command. Please specify direction (up, down, left, right)."), "Should print out correct message when no direction is specified.");
    }

    @Test
    void testHandleMoveCommand() throws IOException {
        Socket socket1 = Mockito.mock();

        InputStream mockInputStream1 = new ByteArrayInputStream("MOVE DOWN".getBytes());
        OutputStream mockOutputStream1 = new ByteArrayOutputStream();

        Mockito.when(socket1.getInputStream()).thenReturn(mockInputStream1);
        Mockito.when(socket1.getOutputStream()).thenReturn(mockOutputStream1);

        PlayerHandler playerHandler = new PlayerHandler(socket1, map);

        playerHandler.run();

        Assertions.assertTrue(mockOutputStream1.toString().trim().endsWith("Successfully moved"), "Should print out correct message when no direction is specified.");
    }

    @Test
    void testHandleSendCommandWithInvalidIndex() throws IOException {
        Socket socket1 = Mockito.mock();

        InputStream mockInputStream1 = new ByteArrayInputStream("SEND 1 1".getBytes());
        OutputStream mockOutputStream1 = new ByteArrayOutputStream();

        Mockito.when(socket1.getInputStream()).thenReturn(mockInputStream1);
        Mockito.when(socket1.getOutputStream()).thenReturn(mockOutputStream1);

        PlayerHandler playerHandler = new PlayerHandler(socket1, map);

        playerHandler.run();

        Assertions.assertTrue(mockOutputStream1.toString().trim().endsWith("Invalid index"), "Should print correct message when no item with such index exists");
    }

    @Test
    void testHandleSendCommandWithWrongArgument() throws IOException {
        Socket socket1 = Mockito.mock();

        InputStream mockInputStream1 = new ByteArrayInputStream("SEND zdsgad asdf".getBytes());
        OutputStream mockOutputStream1 = new ByteArrayOutputStream();

        Mockito.when(socket1.getInputStream()).thenReturn(mockInputStream1);
        Mockito.when(socket1.getOutputStream()).thenReturn(mockOutputStream1);

        PlayerHandler playerHandler = new PlayerHandler(socket1, map);

        playerHandler.run();

        Assertions.assertTrue(mockOutputStream1.toString().trim().endsWith("Invalid argument."), "Should print out correct message when arguments are invalid");
    }

    @Test
    void testHandleStatusCommand() throws IOException {
        Socket socket1 = Mockito.mock();

        InputStream mockInputStream1 = new ByteArrayInputStream("STATUS".getBytes());
        OutputStream mockOutputStream1 = new ByteArrayOutputStream();

        Mockito.when(socket1.getInputStream()).thenReturn(mockInputStream1);
        Mockito.when(socket1.getOutputStream()).thenReturn(mockOutputStream1);

        PlayerHandler playerHandler = new PlayerHandler(socket1, map);

        playerHandler.run();

        String string1 = mockOutputStream1.toString();
        String string2 = """
                Hero Status:
                Level: 1
                Health: 100
                Mana: 100
                Attack: 50
                Defence: 50""";

        String normalizedString1 = string1.replaceAll("\\s+", "");
        String normalizedString2 = string2.replaceAll("\\s+", "");


        Assertions.assertTrue(normalizedString1.contains(normalizedString2), "Should correctly send status of player");
    }

    @Test
    void testBackpackCommand() throws IOException, BackpackCapacityExceededException {
        Socket socket1 = Mockito.mock();

        InputStream mockInputStream1 = new ByteArrayInputStream("BACKPACK".getBytes());
        OutputStream mockOutputStream1 = new ByteArrayOutputStream();

        Mockito.when(socket1.getInputStream()).thenReturn(mockInputStream1);
        Mockito.when(socket1.getOutputStream()).thenReturn(mockOutputStream1);

        PlayerHandler playerHandler = new PlayerHandler(socket1, map);

        playerHandler.getPlayerHero().pickUp(new HealthPotion(100));

        playerHandler.run();

        Assertions.assertTrue(mockOutputStream1.toString().trim().endsWith("BACKPACK:\n" +
                "Health Potion+100"), "Should print out backpack in a correct way");
    }


    @Test
    void testUseCommandWithWrongIndex() throws IOException {
        Socket socket1 = Mockito.mock();

        InputStream mockInputStream1 = new ByteArrayInputStream("USE 0".getBytes());
        OutputStream mockOutputStream1 = new ByteArrayOutputStream();

        Mockito.when(socket1.getInputStream()).thenReturn(mockInputStream1);
        Mockito.when(socket1.getOutputStream()).thenReturn(mockOutputStream1);

        PlayerHandler playerHandler = new PlayerHandler(socket1, map);


        playerHandler.run();

        Assertions.assertTrue(mockOutputStream1.toString().trim().endsWith("Invalid index"), "Should print out correct message when no item exists with that index");
    }

    @Test
    void testUseCommandWithWrongArgument() throws IOException {
        Socket socket1 = Mockito.mock();

        InputStream mockInputStream1 = new ByteArrayInputStream("USE asdf".getBytes());
        OutputStream mockOutputStream1 = new ByteArrayOutputStream();

        Mockito.when(socket1.getInputStream()).thenReturn(mockInputStream1);
        Mockito.when(socket1.getOutputStream()).thenReturn(mockOutputStream1);

        PlayerHandler playerHandler = new PlayerHandler(socket1, map);


        playerHandler.run();

        Assertions.assertTrue(mockOutputStream1.toString().trim().endsWith("Invalid argument."), "Should print out correct message when invalid arguments are used with use command");
    }

    @Test
    void testExitCommand() throws IOException {
        Socket socket1 = Mockito.mock();

        InputStream mockInputStream1 = new ByteArrayInputStream("exit".getBytes());
        OutputStream mockOutputStream1 = new ByteArrayOutputStream();

        Mockito.when(socket1.getInputStream()).thenReturn(mockInputStream1);
        Mockito.when(socket1.getOutputStream()).thenReturn(mockOutputStream1);

        PlayerHandler playerHandler = new PlayerHandler(socket1, map);


        playerHandler.run();

        Assertions.assertTrue(mockOutputStream1.toString().trim().endsWith("Exiting the game..."), "Should print out correct message when exiting game");
    }

}
