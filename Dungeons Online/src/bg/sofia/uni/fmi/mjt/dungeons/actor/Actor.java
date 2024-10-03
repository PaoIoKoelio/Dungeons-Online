package bg.sofia.uni.fmi.mjt.dungeons.actor;

import bg.sofia.uni.fmi.mjt.dungeons.treasure.Spell;
import bg.sofia.uni.fmi.mjt.dungeons.treasure.Weapon;

public interface Actor {
    String getName();

    int getHealth();

    int getMana();

    boolean isAlive();

    void takeDamage(int damagePoints);

    int attack();

    Weapon getWeapon();

    Spell getSpell();
}
