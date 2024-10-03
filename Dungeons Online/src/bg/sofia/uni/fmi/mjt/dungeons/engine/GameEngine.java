package bg.sofia.uni.fmi.mjt.dungeons.engine;

import bg.sofia.uni.fmi.mjt.dungeons.actor.Enemy;
import bg.sofia.uni.fmi.mjt.dungeons.actor.Hero;
import bg.sofia.uni.fmi.mjt.dungeons.actor.Position;
import bg.sofia.uni.fmi.mjt.dungeons.exception.BackpackCapacityExceededException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.GameIsFullException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.NoFreeSpaceException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.NoSuchPlayerException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.NoSuchItemException;
import bg.sofia.uni.fmi.mjt.dungeons.server.PlayerHandler;
import bg.sofia.uni.fmi.mjt.dungeons.treasure.Treasure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Arrays;

public class GameEngine {
    private static final String NULL_HERO_OR_ENEMY_IN_FIGHT_METHOD = "Null hero or enemy in fight method";
    private static final String NULL_HERO_IN_SEND_METHOD = "Null hero in send method";
    private static final String NULL_HERO_IN_ADD_HERO_METHOD = "Null hero in addHero method";
    private static final String NULL_PLAYER_IN_REMOVE_PLAYER_METHOD = "Null player in removePlayer method";
    private static final int FIRST_ROW = 0;
    private static final int WALL_INDEX = 0;
    private static final int POSITION_CHANGE = 1;
    private char[][] map;
    private List<Hero> players;
    private Enemy[] enemies;
    private Treasure[] treasures;
    private int height;
    private int width;
    private Map<Integer, PlayerHandler> playerHandlers;
    private int enemyCounter;
    private int treasureCounter;
    private static final int XP_PER_LEVEL = 50;
    private static final int MAX_PLAYERS = 9;
    private static final int RADIX = 10;
    private static final char MONSTER_SYMBOL = 'M';
    private static final char TREASURE_SYMBOL = 'T';
    private static final char MIN_CHARACTER_SYMBOL = '1';
    private static final char MAX_CHARACTER_SYMBOL = '9';
    private static final char OBSTACLE_SYMBOL = '#';
    private static final char EMPTY_SYMBOL = '.';
    private static final String YOU_DEFEATED_PLAYER_MESSAGE = "You defeated player ";
    private static final String INCORRECT_MOVE_MESSAGE = "Obstacle in the way";
    private static final String YOU_DIED_MESSAGE = "You died";
    private static final String FULL_GAME_MESSAGE = "Game is full.";
    private static final String ITEM_RECEIVED_MESSAGE = "You received ";
    private static final String ITEM_SENT_MESSAGE = "Item successfully sent";
    private static final String NO_SPACE_MESSAGE = "There is no space on the map";
    private static final String NO_AVAILABLE_ID_MESSAGE = "No free Id. The game is full currently";
    private static final String INVALID_INDEX_MESSAGE = "Invalid id for player.";
    private static final String SUCCESSFULLY_MOVED_MESSAGE = "Successfully moved";
    private static final String DEFEATED_MONSTER_MESSAGE = "Defeated monster ";
    private static final char ZERO_CHAR = '0';
    private final Object lock = new Object();

    public GameEngine(char[][] map, Enemy[] enemies, Treasure[] treasures) {
        this.map = map;
        this.height = map.length;
        this.width = map[FIRST_ROW].length;
        this.enemies = enemies;
        this.treasures = treasures;
        this.players = new ArrayList<>();
        this.playerHandlers = new ConcurrentHashMap<>();
    }

    public char[][] getMap() {
        return map;
    }

    private Position calculateFuturePosition(Position position, Direction direction) {
        return switch (direction) {
            case UP -> new Position(position.x(), position.y() - POSITION_CHANGE);
            case DOWN -> new Position(position.x(), position.y() + POSITION_CHANGE);
            case LEFT -> new Position(position.x() - POSITION_CHANGE, position.y());
            case RIGHT -> new Position(position.x() + POSITION_CHANGE, position.y());
        };
    }

    private boolean isValidPosition(Position position) {
        return position.y() >= WALL_INDEX && position.y() < height &&
                position.x() >= WALL_INDEX && position.x() < width &&
                map[position.y()][position.x()] != OBSTACLE_SYMBOL;
    }

    private void putDownNewEnemy() {
        for (int i = height - 1; i >= 0; i--) {
            for (int j = width - 1; j >= 0; j--) {
                if (map[i][j] == EMPTY_SYMBOL) {
                    map[i][j] = MONSTER_SYMBOL;
                    return;
                }
            }
        }
    }

    private boolean defeatMonster(Hero hero, Enemy enemy) throws NoFreeSpaceException {
        if (hero == null || enemy == null) {
            throw new IllegalArgumentException(NULL_HERO_OR_ENEMY_IN_FIGHT_METHOD);
        }
        while (hero.isAlive() && enemy.isAlive()) {
            enemy.takeDamage(hero.attack());
            if (enemy.isAlive()) {
                hero.takeDamage(enemy.attack());
            }
        }
        if (!hero.isAlive()) {
            hero.dieAndResurrect(findFreePos());
            return false;
        } else {
            hero.gainExperience(enemy.getLevel() * XP_PER_LEVEL);
            putDownNewEnemy();
            return true;
        }
    }

    private boolean defeatOpponent(Hero hero1, Hero hero2) throws NoFreeSpaceException {
        if (hero1 == null || hero2 == null) {
            throw new IllegalArgumentException(NULL_HERO_OR_ENEMY_IN_FIGHT_METHOD);
        }
        while (hero1.isAlive() && hero2.isAlive()) {
            hero2.takeDamage(hero1.attack());
            if (hero2.isAlive()) {
                hero1.takeDamage(hero2.attack());
            }
        }

        if (!hero1.isAlive()) {
            hero1.dieAndResurrect(findFreePos());
            hero2.gainExperience(hero1.getLevel() * XP_PER_LEVEL);
            playerHandlers.get(hero2.getId()).sendMessage(YOU_DEFEATED_PLAYER_MESSAGE + hero1.getId());
            return false;
        } else {
            hero2.dieAndResurrect(findFreePos());
            hero1.gainExperience(hero2.getLevel() * XP_PER_LEVEL);
            playerHandlers.get(hero2.getId()).sendMessage(YOU_DIED_MESSAGE);
            return true;
        }

    }

    public Collection<Hero> getPlayers() {
        return players;
    }

    public void addHero(PlayerHandler playerHandler) throws GameIsFullException {
        if (playerHandler == null) {
            throw new IllegalArgumentException(NULL_HERO_IN_ADD_HERO_METHOD);
        }
        if (players.size() == MAX_PLAYERS) {
            throw new GameIsFullException(FULL_GAME_MESSAGE);
        } else {
            Position heroPosition = playerHandler.getPlayerHero().getPosition();
            map[heroPosition.y()][heroPosition.x()] = Character.forDigit(playerHandler.getPlayerHero().getId(), RADIX);
            players.add(playerHandler.getPlayerHero());
            playerHandlers.put(playerHandler.getPlayerHero().getId(), playerHandler);
            broadcastGameMap();
        }
    }

    public String sendItem(Hero hero1, int hero2Id, int index) throws BackpackCapacityExceededException,
            NoSuchPlayerException, NoSuchItemException {
        if (hero1 == null) {
            throw new IllegalArgumentException(NULL_HERO_IN_SEND_METHOD);
        }
        Treasure treasure = hero1.getItem(index);
        Hero hero2 = findPlayer(hero2Id);
        hero2.pickUp(treasure);
        hero1.removeItem(index);
        playerHandlers.get(hero2.getId()).sendMessage(ITEM_RECEIVED_MESSAGE + treasure.getName());
        return ITEM_SENT_MESSAGE;
    }

    public void removePlayer(Hero player) {
        if (player == null) {
            throw new IllegalArgumentException(NULL_PLAYER_IN_REMOVE_PLAYER_METHOD);
        }
        players.remove(player);
        playerHandlers.remove(player.getId());
        map[player.getPosition().y()][player.getPosition().x()] = EMPTY_SYMBOL;
        broadcastGameMap();
    }

    public Position findFreePos() throws NoFreeSpaceException {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (map[i][j] == EMPTY_SYMBOL) {
                    return new Position(j, i);
                }
            }
        }
        throw new NoFreeSpaceException(NO_SPACE_MESSAGE);
    }

    public int findFreeId() throws GameIsFullException {
        boolean[] takenIds = new boolean[MAX_PLAYERS + 1];
        Arrays.fill(takenIds, false);
        for (Hero player : players) {
            takenIds[player.getId()] = true;
        }
        for (int i = 1; i < takenIds.length; i++) {
            if (!takenIds[i]) {
                return i;
            }
        }
        throw new GameIsFullException(NO_AVAILABLE_ID_MESSAGE);
    }

    public void broadcastGameMap() {
        for (PlayerHandler playerHandler : playerHandlers.values()) {
            playerHandler.sendGameMap();
        }
    }

    public Hero findPlayer(int id) throws NoSuchPlayerException {
        for (Hero player : players) {
            if (player.getId() == id) {
                return player;
            }
        }
        throw new NoSuchPlayerException(INVALID_INDEX_MESSAGE);
    }

    public String makeMove(Hero hero, Direction direction) throws BackpackCapacityExceededException,
            NoSuchPlayerException, NoFreeSpaceException {
        Position heroPosition = hero.getPosition();
        Position futurePosition = calculateFuturePosition(heroPosition, direction);
        String message = INCORRECT_MOVE_MESSAGE;
        if (!isValidPosition(futurePosition)) {
            return message;
        }
        char nextStep = map[futurePosition.y()][futurePosition.x()];
        if (nextStep == MONSTER_SYMBOL) {
            message = fightWithMonster(hero, futurePosition);
        } else if (nextStep == TREASURE_SYMBOL) {
            message = collectTreasure(hero, futurePosition);
        } else if (nextStep >= MIN_CHARACTER_SYMBOL && nextStep <= MAX_CHARACTER_SYMBOL) {
            try {
                message = fightWithPlayer(hero, findPlayer(nextStep - ZERO_CHAR), futurePosition);
            } catch (NoSuchPlayerException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        } else if (nextStep == EMPTY_SYMBOL) {
            message = moveToEmptyCell(futurePosition, hero);
        }
        map[heroPosition.y()][heroPosition.x()] = EMPTY_SYMBOL;
        broadcastGameMap();
        return message;
    }

    private String collectTreasure(Hero hero, Position position) throws BackpackCapacityExceededException {
        Treasure treasure = treasures[(treasureCounter++) % treasures.length];
        hero.setPosition(position);
        map[position.y()][position.x()] = Character.forDigit(hero.getId(), RADIX);
        return treasure.collect(hero);
    }

    private String moveToEmptyCell(Position position, Hero hero) {
        map[position.y()][position.x()] = Character.forDigit(hero.getId(), RADIX);
        hero.setPosition(position);
        return SUCCESSFULLY_MOVED_MESSAGE;
    }

    private String updateDyingHeroPosition(Hero hero) {
        Position newPosition = hero.getPosition();
        map[newPosition.y()][newPosition.x()] = Character.forDigit(hero.getId(), RADIX);
        return YOU_DIED_MESSAGE;
    }

    private String fightWithMonster(Hero hero, Position futurePosition) throws NoFreeSpaceException {
        Enemy enemy = enemies[(enemyCounter++) % enemies.length];
        if (defeatMonster(hero, enemy)) {
            hero.setPosition(futurePosition);
            map[futurePosition.y()][futurePosition.x()] = Character.forDigit(hero.getId(), RADIX);
            return DEFEATED_MONSTER_MESSAGE + enemy.getName();
        } else {
            return updateDyingHeroPosition(hero);
        }
    }

    private String fightWithPlayer(Hero hero, Hero opponent, Position futurePosition) throws NoFreeSpaceException {
        if (defeatOpponent(hero, opponent)) {
            hero.setPosition(futurePosition);
            map[opponent.getPosition().y()][opponent.getPosition().x()]
                    = Character.forDigit(opponent.getId(), RADIX);
            map[futurePosition.y()][futurePosition.x()] = Character.forDigit(hero.getId(), RADIX);
            return YOU_DEFEATED_PLAYER_MESSAGE + opponent.getId();
        } else {
            return updateDyingHeroPosition(hero);
        }
    }
}
