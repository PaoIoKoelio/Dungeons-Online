package bg.sofia.uni.fmi.mjt.dungeons.treasure;

import bg.sofia.uni.fmi.mjt.dungeons.actor.Hero;
import bg.sofia.uni.fmi.mjt.dungeons.exception.BackpackCapacityExceededException;

public class Spell implements Treasure {

    private static final String DAMAGE = " Damage:";
    private static final String MANA_COST = " ManaCost:";
    private static final String LEVEL = " Level:";
    private static final String FOUND_A_SPELL = "You found a spell! Damage:";
    private static final String EQUIPPED_SPELL = "Equipped Spell. ";
    private static final String LEVEL_TOO_LOW = "Couldn't equip! Level too low!";
    private String name;
    private int damage;
    private int manaCost;
    private int level;

    public Spell(String name, int damage, int manaCost, int level) {
        this.name = name + DAMAGE + damage + MANA_COST + manaCost + LEVEL + level;
        this.damage = damage;
        this.manaCost = manaCost;
        this.level = level;
    }

    @Override
    public String collect(Hero hero) throws BackpackCapacityExceededException {
        hero.pickUp(this);
        return FOUND_A_SPELL + damage + MANA_COST + manaCost + LEVEL + level;
    }

    @Override
    public String use(Hero hero) {
        if (hero.getLevel() >= level) {
            hero.learn(this);
            return EQUIPPED_SPELL + name;
        }
        return LEVEL_TOO_LOW;
    }

    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    public int getManaCost() {
        return manaCost;
    }

    public int getLevel() {
        return level;
    }
}
