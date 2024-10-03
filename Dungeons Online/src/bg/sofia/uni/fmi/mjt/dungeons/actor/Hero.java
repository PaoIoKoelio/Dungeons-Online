package bg.sofia.uni.fmi.mjt.dungeons.actor;

import bg.sofia.uni.fmi.mjt.dungeons.exception.BackpackCapacityExceededException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.NoSuchItemException;
import bg.sofia.uni.fmi.mjt.dungeons.treasure.Spell;
import bg.sofia.uni.fmi.mjt.dungeons.treasure.Treasure;
import bg.sofia.uni.fmi.mjt.dungeons.treasure.Weapon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Hero extends CharacterBase {
    private static final String NULL_SPELL = "Null spell in equip spell.";
    private static final String NULL_TREASURE = "Null treasure in pickUp method.";
    private static final String NULL_WEAPON = "Null weapon in equip method";
    private int experience;
    private int xpForNextLvl;
    private List<Treasure> backpack;
    private Position position;
    private int id;
    private static final int DEFENCE_POINTS = 50;
    private static final int ATTACK_POINTS = 50;
    private static final int HEALTH_POINTS = 100;
    private static final int MANA_POINTS = 100;
    private static final int BACKPACK_SIZE = 10;
    private static final int FIRST_LEVEL = 1;
    private static final int XP_AT_START = 0;
    private static final int XP_NEEDED_TO_LVL_UP = 100;
    private static final int XP_INCREASE = 50;
    private static final int MIN_INDEX = 0;
    private static final int TREASURE_XP = 10;
    private static final int MIN_HEALTH_AND_MANA = 0;
    private static final String INVALID_INDEX_MESSAGE = "Invalid index";
    private static final String BACKPACK_MESSAGE_BEGINNING = "BACKPACK:\n";
    private static final String NEW_LINE_SYMBOL = "\n";
    private static final String BACKPACK_CAPACITY_EXCEEDED_MESSAGE = "Backpack capacity exceeded";
    private static final String NEGATIVE_HP_MESSAGE = "Trying to add negative health points";
    private static final String NEGATIVE_MANA_MESSAGE = "Trying to add negative mana points";

    public Hero(String name, Position position, int id) {
        super(name, ATTACK_POINTS, DEFENCE_POINTS, HEALTH_POINTS, MANA_POINTS, null, null, FIRST_LEVEL);
        this.experience = XP_AT_START;
        this.backpack = new ArrayList<>();
        this.position = position;
        this.id = id;
        this.xpForNextLvl = XP_NEEDED_TO_LVL_UP;
    }

    private void dropRandomItem() {
        if (!backpack.isEmpty()) {
            Random random = new Random();
            int index = random.nextInt(backpack.size());
            backpack.remove(index);
        }
    }

    private void addItemToBackpack(Treasure item) {
        if (backpack.size() < BACKPACK_SIZE) {
            backpack.add(item);
        }
    }

    public void removeItem(int index) throws NoSuchItemException {
        if (index < backpack.size() && index >= MIN_INDEX) {
            backpack.remove(index);
        } else {
            throw new NoSuchItemException(INVALID_INDEX_MESSAGE);
        }
    }

    public Treasure getItem(int index) throws NoSuchItemException {
        if (index >= backpack.size() || index < MIN_INDEX) {
            throw new NoSuchItemException(INVALID_INDEX_MESSAGE);
        }
        return backpack.get(index);
    }

    public String getBackpackItems() {
        StringBuilder items = new StringBuilder();
        for (Treasure treasure : backpack) {
            items.append(treasure.getName());
            items.append(NEW_LINE_SYMBOL);
        }
        return BACKPACK_MESSAGE_BEGINNING + items.toString();
    }

    public String useItem(int index) throws NoSuchItemException {
        if (index >= MIN_INDEX && index < backpack.size()) {
            Treasure item = backpack.get(index);
            return item.use(this);
        } else {
            throw new NoSuchItemException(INVALID_INDEX_MESSAGE);
        }
    }

    public void dieAndResurrect(Position position) {
        this.health = maxHealth;
        this.level = FIRST_LEVEL;
        this.experience = XP_AT_START;
        this.attack = ATTACK_POINTS;
        this.health = HEALTH_POINTS;
        this.defence = DEFENCE_POINTS;
        this.mana = MANA_POINTS;
        this.dropRandomItem();
        this.position = position;
    }

    public int getId() {
        return id;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public List<Treasure> getBackpack() {
        return backpack;
    }

    public void takeHealing(int healthPotion) {
        if (healthPotion < MIN_HEALTH_AND_MANA) {
            throw new IllegalArgumentException(NEGATIVE_HP_MESSAGE);
        }
        if (health + healthPotion > maxHealth) {
            health = maxHealth;
        } else {
            health += healthPotion;
        }
    }

    public void takeMana(int manaPoints) {
        if (manaPoints < MIN_HEALTH_AND_MANA) {
            throw new IllegalArgumentException(NEGATIVE_MANA_MESSAGE);
        }
        if (mana + manaPoints > maxMana) {
            mana = maxMana;
        } else {
            mana += manaPoints;
        }
    }

    public void gainExperience(int xp) {
        experience += xp;
        while (experience >= xpForNextLvl) {
            experience -= xpForNextLvl;
            xpForNextLvl += XP_INCREASE;
            levelUp();
        }
    }

    public void pickUp(Treasure treasure) throws BackpackCapacityExceededException {
        if (treasure == null) {
            throw new IllegalArgumentException(NULL_TREASURE);
        }
        if (backpack.size() == BACKPACK_SIZE) {
            throw new BackpackCapacityExceededException(BACKPACK_CAPACITY_EXCEEDED_MESSAGE);
        }
        gainExperience(TREASURE_XP);
        addItemToBackpack(treasure);
    }

    public void equip(Weapon weapon) {
        if (weapon == null) {
            throw new IllegalArgumentException(NULL_WEAPON);
        }
        if (weapon.getLevel() <= level) {
            this.weapon = weapon;
        }
    }

    public void learn(Spell spell) {
        if (spell == null) {
            throw new IllegalArgumentException(NULL_SPELL);
        }
        if (spell.getLevel() <= level) {
            this.spell = spell;
        }
    }
}
