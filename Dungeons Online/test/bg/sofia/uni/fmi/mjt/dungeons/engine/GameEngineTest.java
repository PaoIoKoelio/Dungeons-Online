package bg.sofia.uni.fmi.mjt.dungeons.engine;

import bg.sofia.uni.fmi.mjt.dungeons.server.PlayerHandler;
import bg.sofia.uni.fmi.mjt.dungeons.actor.Hero;
import bg.sofia.uni.fmi.mjt.dungeons.actor.Position;
import bg.sofia.uni.fmi.mjt.dungeons.actor.Enemy;
import bg.sofia.uni.fmi.mjt.dungeons.exception.*;
import bg.sofia.uni.fmi.mjt.dungeons.treasure.*;
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
import java.util.List;


public class GameEngineTest {
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
    void testFindFreePos() throws NoFreeSpaceException {
        Assertions.assertEquals(map.findFreePos(), new Position(2, 0), "Find free pos should find the first free position in the map from left to right and then up to down");
    }

    @Test
    void testFindFreePosWhenMapIsFull() {
        char[][] charMap = {{'#', '#'}};
        GameEngine gameMap = new GameEngine(charMap, null, null);
        Assertions.assertThrows(NoFreeSpaceException.class, () -> gameMap.findFreePos(), "Should throw NoFreeSpaceException when the map has no empty spaces");
    }

    @Test
    void testAddPlayerToFullGame() throws GameIsFullException {
        PlayerHandler mockPlayerHandler = Mockito.mock();
        Hero mockHero = Mockito.mock(Hero.class);
        Mockito.when(mockHero.getId()).thenReturn(1);
        Mockito.when(mockHero.getPosition()).thenReturn(new Position(0, 0));

        Mockito.when(mockPlayerHandler.getPlayerHero()).thenReturn(mockHero);
        for (int i = 0; i < 9; i++) {
            map.addHero(mockPlayerHandler);
        }

        Assertions.assertThrows(GameIsFullException.class, () -> map.addHero(mockPlayerHandler), "When trying to add a player when we've already added 9 should throw GameIsFullException");
    }


    @Test
    void testAddPlayer() throws GameIsFullException {
        PlayerHandler mockPlayerHandler = Mockito.mock();
        Hero mockHero = Mockito.mock(Hero.class);
        Mockito.when(mockHero.getId()).thenReturn(1);
        Mockito.when(mockHero.getPosition()).thenReturn(new Position(0, 0));

        Mockito.when(mockPlayerHandler.getPlayerHero()).thenReturn(mockHero);
        map.addHero(mockPlayerHandler);

        Assertions.assertTrue(map.getPlayers().contains(mockHero), "Should add the hero successfully");
    }

    @Test
    void testAddPlayerWithNullPlayerHandler() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> map.addHero(null), "Trying to add a hero with a null playerHandler should throw illegalArgumentException");
    }

    @Test
    void testSendItemSendCorrectMessage() throws NoSuchPlayerException, BackpackCapacityExceededException, NoSuchItemException, IOException {
        Socket socket1 = Mockito.mock(Socket.class);
        Socket socket2 = Mockito.mock(Socket.class);

        InputStream mockInputStream1 = new ByteArrayInputStream("mock input 1".getBytes());
        OutputStream mockOutputStream1 = new ByteArrayOutputStream();

        InputStream mockInputStream2 = new ByteArrayInputStream("mock input 2".getBytes());
        OutputStream mockOutputStream2 = new ByteArrayOutputStream();

        Mockito.when(socket1.getInputStream()).thenReturn(mockInputStream1);
        Mockito.when(socket1.getOutputStream()).thenReturn(mockOutputStream1);

        Mockito.when(socket2.getInputStream()).thenReturn(mockInputStream2);
        Mockito.when(socket2.getOutputStream()).thenReturn(mockOutputStream2);

        PlayerHandler playerHandler1 = new PlayerHandler(socket1, map);
        PlayerHandler playerHandler2 = new PlayerHandler(socket2, map);

        Hero hero1 = map.findPlayer(1);
        Hero hero2 = map.findPlayer(2);

        hero1.pickUp(new HealthPotion(100));
        String result = map.sendItem(hero1, hero2.getId(), 0);

        String expectedMessage = "You received Health Potion+100";
        String actualMessage = mockOutputStream2.toString().trim();
        Assertions.assertTrue(actualMessage.endsWith(expectedMessage), "The message sent to the receiving player should be correct.");
    }

    @Test
    void testSendItemReturnsCorrectMessage() throws NoSuchPlayerException, NoSuchItemException, BackpackCapacityExceededException, IOException, GameIsFullException {
        Socket socket1 = Mockito.mock(Socket.class);
        Socket socket2 = Mockito.mock(Socket.class);

        InputStream mockInputStream1 = new ByteArrayInputStream("mock input 1".getBytes());
        OutputStream mockOutputStream1 = new ByteArrayOutputStream();

        InputStream mockInputStream2 = new ByteArrayInputStream("mock input 2".getBytes());
        OutputStream mockOutputStream2 = new ByteArrayOutputStream();

        Mockito.when(socket1.getInputStream()).thenReturn(mockInputStream1);
        Mockito.when(socket1.getOutputStream()).thenReturn(mockOutputStream1);

        Mockito.when(socket2.getInputStream()).thenReturn(mockInputStream2);
        Mockito.when(socket2.getOutputStream()).thenReturn(mockOutputStream2);

        PlayerHandler playerHandler1 = new PlayerHandler(socket1, map);
        PlayerHandler playerHandler2 = new PlayerHandler(socket2, map);

        Hero hero1 = map.findPlayer(1);
        Hero hero2 = map.findPlayer(2);

        hero1.pickUp(new HealthPotion(100));
        String result = map.sendItem(hero1, hero2.getId(), 0);

        Assertions.assertEquals(result, "Item successfully sent", "Correct message should be returned to sender");
    }

    @Test
    void testSendItemFromNullHero() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> map.sendItem(null, 1, 1));
    }

    @Test
    void testSendItemWithIndexOutOfBounds() {
        Hero hero = new Hero("name", new Position(1, 1), 1);

        Assertions.assertThrows(NoSuchItemException.class, () -> map.sendItem(hero, 1, 10), "Should throw NoSuchItemException when index is out of bounds");
    }

    @Test
    void testSendItemToHeroThatDoesntExist() {
        Hero hero = Mockito.mock();
        Mockito.when(hero.getBackpack()).thenReturn(List.of(new HealthPotion(100)));

        Assertions.assertThrows(NoSuchPlayerException.class, () -> map.sendItem(hero, 7, 0), "Should throw no such player exception when no player with this id is in the game");
    }

    @Test
    void testMoveToEmptyCell() throws NoSuchPlayerException, NoFreeSpaceException, BackpackCapacityExceededException {
        Hero hero = Mockito.mock();
        Mockito.when(hero.getPosition()).thenReturn(new Position(2, 1));

        Assertions.assertEquals("Successfully moved", map.makeMove(hero, Direction.UP), "Should return correct message when player moves on empty cell.");
    }

    @Test
    void testTryToMoveTowardsObstacle() throws NoSuchPlayerException, NoFreeSpaceException, BackpackCapacityExceededException {
        Hero hero = Mockito.mock();
        Mockito.when(hero.getPosition()).thenReturn(new Position(2, 0));

        Assertions.assertEquals("Obstacle in the way", map.makeMove(hero, Direction.LEFT), "Should return correct message when hero tried to move towards obstacle");
    }


    @Test
    void testMoveToEmptyCellUpdatesHeroPosition() throws NoSuchPlayerException, NoFreeSpaceException, BackpackCapacityExceededException {
        Hero hero = new Hero("name", new Position(2, 0), 1);
        map.makeMove(hero, Direction.RIGHT);
        Assertions.assertEquals(hero.getPosition(), new Position(3, 0), "Should correctly update hero position when he moves to empty cell");
    }

    @Test
    void testTryToMoveOutOfMap() throws NoSuchPlayerException, NoFreeSpaceException, BackpackCapacityExceededException {
        Hero hero = Mockito.mock();
        Mockito.when(hero.getPosition()).thenReturn(new Position(2, 0));

        Assertions.assertEquals("Obstacle in the way", map.makeMove(hero, Direction.UP), "Should return correct message when player tries to move out of map bounds");
    }

    @Test
    void testMoveTowardsTreasureReturnsCorrectString() throws NoSuchPlayerException, NoFreeSpaceException, BackpackCapacityExceededException {
        Hero hero1 = new Hero("name", new Position(1, 2), 1);

        Assertions.assertEquals("You found a health potion!", map.makeMove(hero1, Direction.LEFT), "Should return correct message for the item found(health potion)");
    }

    @Test
    void testMoveTowardsTreasureAddsItToBackpack() throws NoSuchPlayerException, NoFreeSpaceException, BackpackCapacityExceededException {
        Hero hero1 = new Hero("name", new Position(1, 2), 1);
        map.makeMove(hero1, Direction.LEFT);
        Assertions.assertEquals(hero1.getBackpack().getFirst(), treasures[0], "Should add the found item to the backpack");
    }

    @Test
    void testMoveTowardsTreasureUpdatedPosition() throws NoSuchPlayerException, NoFreeSpaceException, BackpackCapacityExceededException {
        Hero hero1 = new Hero("name", new Position(1, 2), 1);
        map.makeMove(hero1, Direction.LEFT);
        Assertions.assertEquals(hero1.getPosition(), new Position(0, 2), "Should update heroes position to that of the treasure he picked up");
    }

    @Test
    void testMoveTowardsWeakerMonsterUpdatedPosition() throws NoSuchPlayerException, NoFreeSpaceException, BackpackCapacityExceededException {
        Hero hero1 = new Hero("name", new Position(2, 2), 1);
        map.makeMove(hero1, Direction.LEFT);
        Assertions.assertEquals(hero1.getPosition(), new Position(1, 2), "Hero should move to monster cell when he kills it");
    }

    @Test
    void testMoveTowardsWeakerMonsterReturnsCorrectMessage() throws NoSuchPlayerException, NoFreeSpaceException, BackpackCapacityExceededException {
        Hero hero1 = new Hero("name", new Position(2, 2), 1);
        Assertions.assertEquals("Defeated monster Grendel", map.makeMove(hero1, Direction.LEFT), "Correct message should be returned when you defeat monster");
    }

    private int checkSymbolCount(char[][] map, char symbol) {
        int counter = 0;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == symbol) {
                    counter++;
                }
            }
        }
        return counter;
    }

    @Test
    void testDefeatingMonsterPutsDownANewOne() throws NoSuchPlayerException, NoFreeSpaceException, BackpackCapacityExceededException {
        Hero hero1 = new Hero("name", new Position(2, 2), 1);
        map.makeMove(hero1, Direction.LEFT);
        Assertions.assertEquals(checkSymbolCount(map.getMap(), 'M'), 2, "When a monster is killed another one should be placed on the map");
    }

    private void swapMonsters(Enemy[] enemies) {
        Enemy temp = enemies[0];
        enemies[0] = enemies[1];
        enemies[1] = temp;

    }

    @Test
    void testMoveTowardsStrongerMonsterReturnsCorrectMessage() throws NoSuchPlayerException, NoFreeSpaceException, BackpackCapacityExceededException {
        Hero hero1 = new Hero("name", new Position(2, 2), 1);
        swapMonsters(enemies);
        Assertions.assertEquals("You died", map.makeMove(hero1, Direction.LEFT), "Correct message should be returned when you are defeated by player");
    }

    @Test
    void testMoveTowardsStrongerMonsterPutsHeroAtNewPosition() throws NoSuchPlayerException, NoFreeSpaceException, BackpackCapacityExceededException {
        Hero hero1 = new Hero("name", new Position(2, 2), 1);
        swapMonsters(enemies);
        map.makeMove(hero1, Direction.LEFT);
        Assertions.assertEquals(checkSymbolCount(map.getMap(), '1'), 1, "Hero should be put again on the map when he dies");
    }

    @Test
    void testDyingToMonsterResetsHeroLevel() throws NoSuchPlayerException, NoFreeSpaceException, BackpackCapacityExceededException {
        Hero hero1 = new Hero("name", new Position(2, 2), 1);
        swapMonsters(enemies);
        hero1.gainExperience(1000);
        map.makeMove(hero1, Direction.LEFT);
        Assertions.assertEquals(hero1.getLevel(), 1, "Hero's level should be set back to 1 after death.");
    }

    @Test
    void testMoveTowardsAnotherStrongerPlayer() throws IOException, NoSuchPlayerException, NoFreeSpaceException, BackpackCapacityExceededException {
        Socket socket1 = Mockito.mock(Socket.class);

        InputStream mockInputStream1 = new ByteArrayInputStream("mock input 1".getBytes());
        OutputStream mockOutputStream1 = new ByteArrayOutputStream();

        Mockito.when(socket1.getInputStream()).thenReturn(mockInputStream1);
        Mockito.when(socket1.getOutputStream()).thenReturn(mockOutputStream1);

        PlayerHandler playerHandler1 = new PlayerHandler(socket1, map);

        Hero hero1 = map.findPlayer(1);

        Hero hero = new Hero("name", new Position(3, 0), 2);
        for (int i = 0; i < 7; i++) {
            hero1.gainExperience(1000);
        }
        Assertions.assertEquals(map.makeMove(hero, Direction.LEFT), "You died", "Correct message should be sent when the player died in a fight with another player, which he didn't initiate.");
    }

    @Test
    void testMoveTowardsAnotherWeakerPlayer() throws NoSuchPlayerException, NoFreeSpaceException, BackpackCapacityExceededException, IOException {
        Socket socket1 = Mockito.mock(Socket.class);

        InputStream mockInputStream1 = new ByteArrayInputStream("mock input 1".getBytes());
        OutputStream mockOutputStream1 = new ByteArrayOutputStream();

        Mockito.when(socket1.getInputStream()).thenReturn(mockInputStream1);
        Mockito.when(socket1.getOutputStream()).thenReturn(mockOutputStream1);

        PlayerHandler playerHandler1 = new PlayerHandler(socket1, map);

        Hero hero1 = map.findPlayer(1);

        Hero hero = new Hero("name", new Position(3, 0), 2);
        Assertions.assertEquals(map.makeMove(hero, Direction.LEFT), "You defeated player 1", "Correct message should be returned player defeats another player.");

    }

    @Test
    void testMoveTowardsAnotherWeakerPlayerSendMessage() throws NoSuchPlayerException, NoFreeSpaceException, BackpackCapacityExceededException, IOException {
        Socket socket1 = Mockito.mock(Socket.class);
        Socket socket2 = Mockito.mock(Socket.class);

        InputStream mockInputStream1 = new ByteArrayInputStream("mock input 1".getBytes());
        OutputStream mockOutputStream1 = new ByteArrayOutputStream();

        InputStream mockInputStream2 = new ByteArrayInputStream("mock input 2".getBytes());
        OutputStream mockOutputStream2 = new ByteArrayOutputStream();

        Mockito.when(socket1.getInputStream()).thenReturn(mockInputStream1);
        Mockito.when(socket1.getOutputStream()).thenReturn(mockOutputStream1);

        Mockito.when(socket2.getInputStream()).thenReturn(mockInputStream2);
        Mockito.when(socket2.getOutputStream()).thenReturn(mockOutputStream2);

        PlayerHandler playerHandler1 = new PlayerHandler(socket1, map);
        PlayerHandler playerHandler2 = new PlayerHandler(socket2, map);

        Hero hero1 = map.findPlayer(1);
        Hero hero2 = map.findPlayer(2);

        map.makeMove(hero2, Direction.LEFT);

        String actualMessage = mockOutputStream1.toString().trim();
        Assertions.assertTrue(actualMessage.contains("You died"), "A message should be sent to the dying player who didnt initiate combat should be sent.");
    }

    @Test
    void testMoveTowardsAnotherStrongerPlayerSendMessage() throws NoSuchPlayerException, NoFreeSpaceException, BackpackCapacityExceededException, IOException {
        Socket socket1 = Mockito.mock(Socket.class);
        Socket socket2 = Mockito.mock(Socket.class);

        InputStream mockInputStream1 = new ByteArrayInputStream("mock input 1".getBytes());
        OutputStream mockOutputStream1 = new ByteArrayOutputStream();

        InputStream mockInputStream2 = new ByteArrayInputStream("mock input 2".getBytes());
        OutputStream mockOutputStream2 = new ByteArrayOutputStream();

        Mockito.when(socket1.getInputStream()).thenReturn(mockInputStream1);
        Mockito.when(socket1.getOutputStream()).thenReturn(mockOutputStream1);

        Mockito.when(socket2.getInputStream()).thenReturn(mockInputStream2);
        Mockito.when(socket2.getOutputStream()).thenReturn(mockOutputStream2);

        PlayerHandler playerHandler1 = new PlayerHandler(socket1, map);
        PlayerHandler playerHandler2 = new PlayerHandler(socket2, map);

        Hero hero1 = map.findPlayer(1);
        Hero hero2 = map.findPlayer(2);

        for (int i = 0; i < 7; i++) {
            hero1.gainExperience(1000);
        }

        map.makeMove(hero2, Direction.LEFT);

        Assertions.assertTrue(mockOutputStream1.toString().trim().contains("You defeated player 2"), "A message should be sent to the victorious player who didnt initiate combat should be sent.");
    }


    @Test
    void testRemovePlayer() throws GameIsFullException {
        PlayerHandler mockPlayerHandler = Mockito.mock();
        Hero mockHero = Mockito.mock(Hero.class);
        Mockito.when(mockHero.getId()).thenReturn(1);
        Mockito.when(mockHero.getPosition()).thenReturn(new Position(0, 0));

        Mockito.when(mockPlayerHandler.getPlayerHero()).thenReturn(mockHero);
        map.addHero(mockPlayerHandler);

        map.removePlayer(mockHero);

        Assertions.assertEquals(map.getPlayers().size(), 0, "Removing the only player in the game should leave players array empty");
    }

    @Test
    void testFindFreeId() throws GameIsFullException {
        PlayerHandler mockPlayerHandler = Mockito.mock();
        Hero mockHero = Mockito.mock(Hero.class);
        Mockito.when(mockHero.getId()).thenReturn(1);
        Mockito.when(mockHero.getPosition()).thenReturn(new Position(0, 0));

        Mockito.when(mockPlayerHandler.getPlayerHero()).thenReturn(mockHero);
        map.addHero(mockPlayerHandler);

        Assertions.assertEquals(map.findFreeId(), 2, "FindFreeId() should return the first free id");
    }

    @Test
    void testRemoveNullPlayer() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> map.removePlayer(null), "Null player in remove method should throw IllegalArgumentException");
    }
}
