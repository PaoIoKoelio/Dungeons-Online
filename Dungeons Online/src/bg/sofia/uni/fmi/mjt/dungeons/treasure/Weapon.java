package bg.sofia.uni.fmi.mjt.dungeons.treasure;

import bg.sofia.uni.fmi.mjt.dungeons.actor.Hero;
import bg.sofia.uni.fmi.mjt.dungeons.exception.BackpackCapacityExceededException;

public class Weapon implements Treasure {

    private String name;
    private int damage;
    private int level;
    private static final String DAMAGE = " Damage:";
    private static final String LEVEL = " Level:";
    private static final String FOUND_A_WEAPON = "You found a weapon! Damage:";
    private static final String EQUIPPED_WEAPON = "Equipped weapon. ";
    private static final String COULDNT_EQUIP = "Couldn't equip! Level too low!";

    public Weapon(String name, int damage, int level) {
        this.name = name + DAMAGE + damage + LEVEL + level;
        this.damage = damage;
        this.level = level;
    }

    @Override
    public String collect(Hero hero) throws BackpackCapacityExceededException {
        hero.pickUp(this);
        return FOUND_A_WEAPON + damage + LEVEL + level;
    }

    @Override
    public String use(Hero hero) {
        if (hero.getLevel() >= level) {
            hero.equip(this);
            return EQUIPPED_WEAPON + name;
        }
        return COULDNT_EQUIP;
    }

    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    public int getLevel() {
        return level;
    }
}
